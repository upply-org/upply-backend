package com.upply.job;

import com.upply.common.NotificationEventType;
import com.upply.config.KafkaConfig;
import com.upply.exception.custom.BusinessLogicException;
import com.upply.job.dto.ParsedJobResponse;
import com.upply.job.dto.PostJobEvent;
import com.upply.job.enums.*;
import com.upply.notification.dto.DispatchPayload;
import com.upply.notification.dto.NotificationEvent;
import com.upply.profile.skill.Skill;
import com.upply.user.User;
import com.upply.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.upply.config.KafkaConfig.NOTIFICATION_EVENTS;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostJobConsumer {
    private final JobService jobService;
    private final JobParserService jobParserService;
    private final JobMatchingService jobMatchingService;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final KafkaTemplate<String, NotificationEvent> notificationKafkaTemplate;

    @KafkaListener(topics = KafkaConfig.JOB_POSTING_TOPIC,
            groupId = "post-job-group")
    @Transactional
    public void consumePostJobEvent(PostJobEvent event) {
        log.info("Received PostJobEvent for job source: {}, jobId: {}, userId: {}",
                event.getJobSource(), event.getJobId(), event.getUserId());

        if (event.getJobSource() == JobSource.INTERNAL) {
            processInternalJob(event);
        } else if (event.getJobSource() == JobSource.EXTERNAL) {
            processExternalJob(event);
        }
    }

    private void processExternalJob(PostJobEvent event) {
        log.info("Processing external job for userId: {}", event.getUserId());

        User user = userRepository.getById(event.getUserId());
        
        ParsedJobResponse parsed = jobParserService.parse(event.getImportJobRequest().getDescriptionText());
        log.debug("Parsed job response: title={}, type={}, seniority={}", 
                parsed.title(), parsed.type(), parsed.seniority());

        if(parsed.title() == null || parsed.title().isBlank()){
            log.error("Could not extract job title from description for userId: {}", event.getUserId());
            throw new BusinessLogicException("Could not extract job title from description");
        }

        JobType type      = jobParserService.resolveType(parsed.type());
        JobSeniority seniority = jobParserService.resolveSeniority(parsed.seniority());
        JobModel model     = jobParserService.resolveModel(parsed.model());

        Set<Skill> skills = jobParserService.resolveSkills(parsed.skills());

        String applicationLink = parsed.applicationLink();

        Job job = Job.builder()
                .title(parsed.title())
                .type(type)
                .seniority(seniority)
                .model(model)
                .status(JobStatus.OPEN)
                .source(JobSource.EXTERNAL)
                .location(parsed.location())
                .description(parsed.description())
                .organizationName(parsed.organization())
                .applicationLink(applicationLink)
                .skills(skills)
                .build();
        job.setPostedBy(user);

        Job savedJob = jobRepository.save(job);
        log.info("Saved job with id: {} for userId: {}", savedJob.getId(), event.getUserId());

        try {
            jobMatchingService.storeJobEmbedding(savedJob);
            log.info("Successfully stored embedding for jobId: {}", savedJob.getId());
            notifyMatchingUsers(savedJob);
        } catch (Exception e) {
            log.error("Failed to store embedding for jobId: {}, error: {}", savedJob.getId(), e.getMessage());
            throw new BusinessLogicException("Failed to store embedding, job not saved: " + e.getMessage());
        }
    }

    private static final String FRONTEND_BASE_URL = "https://www.upply.tech/jobs";

    private void processInternalJob(PostJobEvent event) {
        log.info("Processing internal job with jobId: {}", event.getJobId());

        Job job = jobRepository.getById(event.getJobId());
        log.debug("Retrieved job: id={}, postedByUserId={}", 
                job.getId(), job.getPostedBy().getId());

        jobMatchingService.storeJobEmbedding(job);
        log.info("Successfully stored embedding for jobId: {}", job.getId());

        notifyJobPostedSuccessfully(job);
        notifyMatchingUsers(job);
    }

    private void notifyJobPostedSuccessfully(Job job) {
        String jobUrl = FRONTEND_BASE_URL + "?id=" + job.getId();
        NotificationEvent notificationEvent = new NotificationEvent(
                UUID.randomUUID().toString(),
                NotificationEventType.JOB_POSTED_SUCCESSFULLY,
                job.getPostedBy().getId(),
                List.of(DispatchPayload.Channel.EMAIL),
                Map.of(
                        "jobTitle", job.getTitle(),
                        "jobType", job.getType().name(),
                        "seniority", job.getSeniority().name(),
                        "location", job.getLocation() != null ? job.getLocation() : "Not specified",
                        "jobUrl", jobUrl
                )
        );

        notificationKafkaTemplate.send(NOTIFICATION_EVENTS,
                        String.valueOf(job.getId()), notificationEvent)
                .exceptionally(ex -> {
                    log.error("Failed to publish job posted notification for job {}",
                            job.getId(), ex);
                    return null;
                });
        log.info("Successfully published job posted notification for jobId: {}", job.getId());
    }

    private void notifyMatchingUsers(Job job) {
        List<User> matchingUsers = jobMatchingService.findMatchingUsers(job, 20);
        log.info("Found {} matching users for jobId: {}", matchingUsers.size(), job.getId());

        String jobUrl = FRONTEND_BASE_URL + "?id=" + job.getId();
        String jobCompany = job.getOrganizationName() != null ? job.getOrganizationName() : "Upply";

        for (User user : matchingUsers) {
            if (!user.isAccountActivated() || user.getEmail() == null) {
                continue;
            }

            NotificationEvent notificationEvent = new NotificationEvent(
                    UUID.randomUUID().toString(),
                    NotificationEventType.NEW_MATCHED_JOBS,
                    user.getId(),
                    List.of(DispatchPayload.Channel.EMAIL, DispatchPayload.Channel.PUSH),
                    Map.of(
                            "jobId", job.getId(),
                            "jobTitle", job.getTitle(),
                            "jobCompany", jobCompany,
                            "jobUrl", jobUrl
                    )
            );

            notificationKafkaTemplate.send(NOTIFICATION_EVENTS,
                            String.valueOf(user.getId()), notificationEvent)
                    .exceptionally(ex -> {
                        log.error("Failed to publish matched jobs notification for user {}",
                                user.getId(), ex);
                        return null;
                    });
            log.debug("Published notification for user {} for job {}", user.getId(), job.getId());
        }
    }
}
