package com.upply.application;

import com.upply.application.dto.ApplicationMatchEvent;
import com.upply.exception.custom.ResourceNotFoundException;
import com.upply.job.Job;
import com.upply.job.JobMatchingService;
import com.upply.job.JobRepository;
import com.upply.profile.resume.AzureStorageService;
import com.upply.profile.resume.Resume;
import com.upply.profile.resume.chunks.ResumeVectorService;
import com.upply.user.User;
import com.upply.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static com.upply.config.KafkaConfig.APPLICATION_MATCH_CALC_TOPIC;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationMatchConsumer {
    private final JobMatchingService jobMatchingService;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final AzureStorageService azureStorageService;
    private final ApplicationSummaryService applicationSummaryService;
    private final ResumeVectorService resumeVectorService;


    @KafkaListener(
            topics = APPLICATION_MATCH_CALC_TOPIC,
            groupId = "application-matching-group"
    )
    @Transactional
    public void handleMatchCalc(ApplicationMatchEvent event) {
        log.info("Received match event for applicationId: {}", event.getApplicationId());

        Application application = applicationRepository.findById(event.getApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + event.getApplicationId()));

        User user = userRepository.findById(event.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("user not found: " + event.getUserId()));

        Job job = jobRepository.findById(event.getJobId())
                .orElseThrow(() -> new RuntimeException("Job not found: " + event.getJobId()));

        Resume resume = application.getResume();

        String resumeTxt = resume != null && resume.getBlobName() != null
                ? applicationSummaryService.extractText(azureStorageService.downloadFile(resume.getBlobName()))
                : "Resume not available.";

        Long applicationId = application.getId();
        Long applicationApplicantId = application.getApplicant().getId();
        Long applicationJobId = application.getJob().getId();
        Long resumeId = resume.getId();

        calcMatch(application, user, job, resumeTxt);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    storeResumeEmbedding(applicationId, applicationApplicantId, applicationJobId, resumeId, resumeTxt);
                } catch (Exception e) {
                    log.warn("Failed to store resume embedding for applicationId: {}, continuing",
                            event.getApplicationId(), e);
                }
            }
        });

    }

    private void calcMatch(Application application, User user, Job job, String resumeTxt) {
        try {
            double score = jobMatchingService.calculateMatchScore(user, job);
            application.setMatchingRatio(score);

            try {
                String summary = applicationSummaryService.callAi(score, job, resumeTxt);
                application.setSummary(summary);
            } catch (Exception ex) {
                log.warn("AI summary generation failed for applicationId: {}, proceeding without summary", application.getId(), ex);
            }

            applicationRepository.save(application);
        } catch (Exception e) {
            log.error("Failed to process application match", e);
        }
    }

    private void storeResumeEmbedding(Long applicationId, Long applicationApplicantId, Long applicationJobId, Long resumeId, String resumeTxt) {
        resumeVectorService.storeResumeEmbedding(applicationId, applicationApplicantId, applicationJobId, resumeId, resumeTxt);
    }

}
