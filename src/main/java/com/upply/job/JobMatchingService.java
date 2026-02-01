package com.upply.job;

import com.upply.user.User;
import com.upply.user.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobMatchingService {

    private final VectorStore vectorStore;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;



    private String buildJobContent(Job job) {

        StringBuilder content = new StringBuilder();

        content.append("Job Title: ").append(job.getTitle()).append(". ");

        List<String> skillList = jobRepository.findJobSkillNames(job.getId());

        String skillNames = String.join(", ", skillList);

        content.append("Required Skills: ").append(skillNames).append(".");

        return content.toString();
    }
    private String buildUserProfile(User user) {

        StringBuilder profile = new StringBuilder();

        List<String> skillList = userRepository.findUserSkillNames(user.getId());

        String skillNames = String.join(", ", skillList);

        profile.append("User Skills: ").append(skillNames).append(".");

        return profile.toString();
    }

    public void storeJobEmbedding(Job job) {

        try {
            String jobContent = buildJobContent(job);

            Document document = new Document(
                    String.valueOf(job.getId()),
                    jobContent,
                    Map.of(
                            "jobId", job.getId(),
                            "title", job.getTitle(),
                            "type", job.getType().name(),
                            "seniority", job.getSeniority().name(),
                            "model", job.getModel().name(),
                            "location", job.getLocation() != null ? job.getLocation() : "",
                            "status", job.getStatus().name()
                    )
            );

            vectorStore.add(List.of(document));
            log.info("Stored embedding for job ID: {}", job.getId());
        }

        catch (Exception e) {
            log.error("Error storing embedding for job ID: {}", job.getId(), e);
            throw new RuntimeException("Failed to store embedding", e);
        }

    }
    public void deleteJobEmbedding(Long jobId) {

        try {
            vectorStore.delete(List.of(String.valueOf(jobId)));
            log.info("Deleted embedding for job ID: {}", jobId);
        }

        catch (Exception e) {
            log.error("Error deleting embedding for job ID: {}", jobId, e);
        }
    }



    // Container class to hold Job and its similarity score
    @Getter
    @AllArgsConstructor
    public static class JobWithScore {
        private final Job job;
        private final Double score;
    }

    public List<JobWithScore> findSimilarJobs(User user, int topK) {

        try {
            String userProfile = buildUserProfile(user);
            // log.info(userProfile);

            SearchRequest searchRequest = SearchRequest.builder()
                    .query(userProfile) // The text to search for
                    .topK(topK) // Number of results
                    // .filterExpression("status == 'OPEN'")
                    .similarityThreshold(0.85) // Minimum similarity score (0.0 to 1.0)
                    .build();

            List<Document> similarDocuments = vectorStore.similaritySearch(searchRequest);

            // Get job IDs from Redis (already in similarity order)
            List<Long> jobIds = similarDocuments.stream()
                    .map(doc -> Long.valueOf(doc.getId()))
                    .toList();



            // Fetch all jobs from database (order is random)
            Map<Long, Job> jobMap = jobRepository.findAllById(jobIds)
                    .stream()
                    .collect(Collectors.toMap(Job::getId, job -> job));

            Map<Long, Double> scoreMap = similarDocuments.stream()
                    .collect(Collectors.toMap(
                            doc -> Long.valueOf(doc.getId()),
                            doc -> doc.getScore() == null ? 0.0 : doc.getScore()
                    ));



            // streaming on jobIds because it's the only preserved order
            return jobIds.stream()
                    .map(id -> {
                        Job job = jobMap.get(id);

                        if(job != null) {
                            Double score = scoreMap.get(id);
                            return new JobWithScore(job, score);
                        }

                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        catch (Exception e) {
            log.error("Error finding similar jobs for user ID: {}", user.getId(), e);
            throw new RuntimeException("Failed to find similar jobs", e);
        }

    }
}
