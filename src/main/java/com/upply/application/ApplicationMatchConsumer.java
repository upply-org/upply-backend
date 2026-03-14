package com.upply.application;

import com.upply.application.dto.ApplicationMatchEvent;
import com.upply.exception.custom.ResourceNotFoundException;
import com.upply.job.Job;
import com.upply.job.JobMatchingService;
import com.upply.job.JobRepository;
import com.upply.profile.resume.AzureStorageService;
import com.upply.profile.resume.Resume;
import com.upply.user.User;
import com.upply.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.upply.config.KafkaConfig.APPLICATION_MATCH_CALC_TOPIC;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ApplicationMatchConsumer {
    private final JobMatchingService jobMatchingService;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final AzureStorageService azureStorageService;
    private final ApplicationSummaryService applicationSummaryService;

    @KafkaListener(
            topics = APPLICATION_MATCH_CALC_TOPIC,
            groupId = "application-matching-group"
    )
    public void handleMatchCalc(ApplicationMatchEvent event) {
        log.info("Received match event for applicationId: {}", event.getApplicationId());
        try {
            Application application = applicationRepository.findById(event.getApplicationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + event.getApplicationId()));

            User user = userRepository.findById(event.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("user not found: " + event.getUserId()));

            Job job = jobRepository.findById(event.getJobId())
                    .orElseThrow(() -> new RuntimeException("Job not found: " + event.getJobId()));

            double score = jobMatchingService.calculateMatchScore(user, job);
            application.setMatchingRatio(score);
            Resume resume = application.getResume();
            application.setSummary(null);
            try {
                String resumeTxt = resume != null && resume.getBlobName() != null
                        ? applicationSummaryService.extractText(azureStorageService.downloadFile(resume.getBlobName()))
                        : "Resume not available.";
                String summary = applicationSummaryService.callAi(score, job, user, resumeTxt);
                application.setSummary(summary);
            } catch (Exception ex) {
                log.warn("AI summary generation failed for applicationId: {}, proceeding without summary", event.getApplicationId(), ex);
            }

        } catch (Exception e) {
            log.error("Failed to process match for applicationId: {}", event.getApplicationId(), e);
        }
    }
}
