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
        } catch (Exception e) {
            log.error("Failed to store embedding for jobId: {}, error: {}", savedJob.getId(), e.getMessage());
            throw new BusinessLogicException("Failed to store embedding, job not saved: " + e.getMessage());
        }
    }

    private void processInternalJob(PostJobEvent event) {
        log.info("Processing internal job with jobId: {}", event.getJobId());

        Job job = jobRepository.getById(event.getJobId());
        log.debug("Retrieved job: id={}, postedByUserId={}", 
                job.getId(), job.getPostedBy().getId());

        jobMatchingService.storeJobEmbedding(job);
        log.info("Successfully stored embedding for jobId: {}", job.getId());

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
                        "jobUrl", "TODO" + job.getId() //TODO
                )
        );

        notificationKafkaTemplate.send(NOTIFICATION_EVENTS,
                        String.valueOf(job.getId()), notificationEvent)
                .exceptionally(ex -> {
                    log.error("Failed to publish submission notification for application {}",
                            job.getId(), ex);
                    return null;
                });
        log.info("Successfully published notification event for jobId: {}", job.getId());
    }
}
