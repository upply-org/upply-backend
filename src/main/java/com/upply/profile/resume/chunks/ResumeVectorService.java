package com.upply.profile.resume.chunks;

import com.upply.profile.resume.dto.ParsedResumeResponse;
import com.upply.profile.resume.parse.ResumeParserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ResumeVectorService {
    private final VectorStore vectorStore;
    private final ResumeParserService resumeParserService;

    public ResumeVectorService(@Qualifier("resumeVectorStore") VectorStore vectorStore,
                               ResumeParserService resumeParserService) {
        this.vectorStore = vectorStore;
        this.resumeParserService = resumeParserService;
    }

    public void storeResumeEmbedding(Long applicationId, Long applicationApplicantId, Long applicationJobId, Long resumeId, String resumeTxt) {

        try {
            ParsedResumeResponse parsed = resumeParserService.callAi(resumeTxt);

            List<Chunk> chunks = buildResumeChunks(parsed);

            List<Document> documents = new ArrayList<>();

            for (int i = 0; i < chunks.size(); i++) {
                Chunk chunk = chunks.get(i);

                Document doc = new Document(
                        resumeId + "-" + i,
                        chunk.content,
                        Map.of(
                                "applicationId", String.valueOf(applicationId),
                                "userId", String.valueOf(applicationApplicantId),
                                "jobId", String.valueOf(applicationJobId),
                                "chunkType", chunk.type,
                                "chunkIndex", String.valueOf(i)
                        )
                );

                documents.add(doc);
            }

            vectorStore.add(documents);

            log.info("Stored {} chunks for application ID: {}", documents.size(), applicationId);

        } catch (Exception e) {
            log.error("Error storing resume embedding for application ID: {}", applicationId, e);
            throw new RuntimeException("Failed to store resume embeddings", e);
        }
    }

    private List<Chunk> buildResumeChunks(ParsedResumeResponse parsed) {
        List<Chunk> chunks = new ArrayList<>();

        // Skills
        if (parsed.skills() != null && !parsed.skills().isEmpty()) {
            chunks.add(new Chunk(
                    "skills",
                    "Candidate skilled in " + String.join(", ", parsed.skills())
            ));
        }

        // Experience
        if (parsed.experiences() != null) {
            for (var exp : parsed.experiences()) {
                String dates = formatDateRange(exp.startDate(), exp.endDate());

                chunks.add(new Chunk("experience", """
                        %s working at %s. %s. %s
                        """.formatted(
                        exp.title(),
                        exp.organization(),
                        dates,
                        exp.description() != null ? exp.description() : ""
                )));
            }
        }

        // Projects
        if (parsed.projects() != null) {
            for (var proj : parsed.projects()) {
                String dates = formatDateRange(proj.startDate(), proj.endDate());

                chunks.add(new Chunk("project", """
                        Built project %s. %s. %s
                        """.formatted(
                        proj.title(),
                        dates,
                        proj.description() != null ? proj.description() : ""
                )));
            }
        }

        // Education
        if (parsed.university() != null && !parsed.university().isBlank()) {
            chunks.add(new Chunk(
                    "education",
                    "Studied at " + parsed.university()
            ));
        }

        return chunks;
    }

    private String formatDateRange(Date start, Date end) {
        if (start == null) return "";
        String startStr = formatDate(start);
        if (end == null) return "Period: " + startStr + " - Present";
        return "Period: " + startStr + " - " + formatDate(end);
    }

    private String formatDate(Date date) {
        if (date == null) return "";
        java.time.LocalDate localDate;
        if (date instanceof java.util.Date) {
            localDate = ((java.util.Date) date).toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate();
        } else {
            return date.toString();
        }
        return localDate.getMonthValue() + "/" + localDate.getYear();
    }

    private record Chunk(String type, String content) {
    }
}
