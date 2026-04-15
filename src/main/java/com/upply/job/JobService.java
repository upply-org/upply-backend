package com.upply.job;

import com.upply.application.Application;
import com.upply.application.ApplicationRepository;
import com.upply.application.dto.ApplicationMapper;
import com.upply.application.dto.ApplicationResponse;
import com.upply.application.enums.ApplicationStatus;
import com.upply.common.NotificationEventType;
import com.upply.common.PageResponse;
import com.upply.exception.custom.BusinessLogicException;
import com.upply.exception.custom.OperationNotPermittedException;
import com.upply.exception.custom.ResourceNotFoundException;
import com.upply.job.dto.*;
import com.upply.job.enums.*;
import com.upply.notification.dto.DispatchPayload;
import com.upply.notification.dto.NotificationEvent;
import com.upply.job.dto.ParsedJobResponse;
import com.upply.profile.skill.Skill;
import com.upply.profile.skill.SkillRepository;
import com.upply.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.upply.config.KafkaConfig.NOTIFICATION_EVENTS;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobService {

    private final JobMapper jobMapper;
    private final ApplicationMapper applicationMapper;
    private final JobRepository jobRepository;
    private final SkillRepository skillRepository;
    private final ApplicationRepository applicationRepository;
    private final JobMatchingService jobMatchingService;
    private final ApplicationExcelExportService applicationExcelExportService;
    private final ExportTaskMapper exportTaskMapper;
    private final KafkaTemplate<String, NotificationEvent> notificationKafkaTemplate;
    private final JobParserService jobParserService;
    //TODO: use key-value database like redis!!
    private final Map<String, ExportTask> exportTasks = new ConcurrentHashMap<>();
    @Value("${app.export.task-expire-seconds}")
    private int TASK_EXPIRE_TIME;

    @Transactional
    public JobResponse createJob(@Valid JobRequest request, Authentication connectedUser) {

        Set<Skill> skills = new HashSet<>(
                skillRepository.findAllById(request.getSkillIds()));

        if (skills.size() != request.getSkillIds().size()) {
            throw new ResourceNotFoundException("One or more skills do not exist");
        }

        Job job = jobMapper.toJob(request, skills);

        User user = (User) connectedUser.getPrincipal();
        job.setPostedBy(user);

        Job savedJob = jobRepository.save(job);

        // Store job embedding
        jobMatchingService.storeJobEmbedding(savedJob);

        NotificationEvent notificationEvent = new NotificationEvent(
                UUID.randomUUID().toString(),
                NotificationEventType.JOB_POSTED_SUCCESSFULLY,
                savedJob.getPostedBy().getId(),
                List.of(DispatchPayload.Channel.EMAIL),
                Map.of(
                        "jobTitle",  savedJob.getTitle(),
                        "jobType",   savedJob.getType().name(),
                        "seniority", savedJob.getSeniority().name(),
                        "location",  savedJob.getLocation() != null ? savedJob.getLocation() : "Remote",
                        "jobUrl",    "TODO" + savedJob.getId() //TODO
                )
        );

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                notificationKafkaTemplate.send(NOTIFICATION_EVENTS,
                                String.valueOf(savedJob.getId()), notificationEvent)
                        .exceptionally(ex -> {
                            log.error("Failed to publish submission notification for application {}",
                                    savedJob.getId(), ex);
                            return null;
                        });
            }
        });

        return jobMapper.toJobResponse(savedJob);
    }

    @Transactional
    public JobResponse importExternalJob(ImportJobRequest request, Authentication connectedUser) {

        ParsedJobResponse parsed = jobParserService.parse(request.getDescriptionText());

        if(parsed.title() == null || parsed.title().isBlank()){
            throw new BusinessLogicException("Could not extract job title from description");
        }

        JobType      type      = jobParserService.resolveType(parsed.type());
        JobSeniority seniority = jobParserService.resolveSeniority(parsed.seniority());
        JobModel     model     = jobParserService.resolveModel(parsed.model());

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

        User user = (User) connectedUser.getPrincipal();
        job.setPostedBy(user);

        Job savedJob = jobRepository.save(job);

        try {
            jobMatchingService.storeJobEmbedding(savedJob);
        } catch (Exception e) {
            throw new BusinessLogicException("Failed to store embedding, job not saved: " + e.getMessage());
        }

        return jobMapper.toJobResponse(savedJob);
    }

    @Transactional(readOnly = true)
    public JobResponse getJob(Long id) {

        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job with ID " + id + " not found"));

        return jobMapper.toJobResponse(job);
    }

    public PageResponse<JobListResponse> getAllOpenJobs(int pageNumber, int size, Authentication connectedUser) {

        Pageable pageable = PageRequest.of(pageNumber, size, Sort.by("createdDate").descending());

        Page<Job> jobs = jobRepository.findByStatus(JobStatus.OPEN, pageable);

        List<JobListResponse> jobResponses = jobs.stream()
                .map(jobMapper::toJobListResponse)
                .toList();

        return new PageResponse<>(
                jobResponses,
                jobs.getNumber(),
                jobs.getSize(),
                jobs.getTotalElements(),
                jobs.getTotalPages(),
                jobs.isFirst(),
                jobs.isLast());
    }

    public PageResponse<JobListResponse> searchJobs(int pageNumber, int size, JobFilter filter) {

        Pageable pageable = PageRequest.of(pageNumber, size, Sort.by("createdDate").descending());
        Specification<Job> spec = JobSpecification.withFilters(filter);

        Page<Job> jobs = jobRepository.findAll(spec, pageable);

        List<JobListResponse> jobResponses = jobs.stream()
                .map(jobMapper::toJobListResponse)
                .toList();

        return new PageResponse<>(
                jobResponses,
                jobs.getNumber(),
                jobs.getSize(),
                jobs.getTotalElements(),
                jobs.getTotalPages(),
                jobs.isFirst(),
                jobs.isLast());
    }

    public List<MatchedJobListResponse> getMatchedJobs(Authentication connectedUser) {

        User user = (User) connectedUser.getPrincipal();

        List<JobMatchingService.JobWithScore> matchedJobsWithScores = jobMatchingService.findSimilarJobs(user, 50);

        return matchedJobsWithScores.stream()
                .map(jobWithScore -> jobMapper.toMatchedJobListResponse(
                        jobWithScore.getJob(),
                        jobWithScore.getScore()))
                .toList();
    }

    @Transactional
    public JobResponse updateJob(Long id, JobUpdateRequest request, Authentication connectedUser) {

        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job with ID " + id + " not found"));

        User user = ((User) connectedUser.getPrincipal());

        if (!Objects.equals(job.getPostedBy().getId(), user.getId())) {
            throw new OperationNotPermittedException("You are not permitted to update this job");
        }

        if (request.getTitle() != null) {
            job.setTitle(request.getTitle());
        }

        if (request.getType() != null) {
            job.setType(JobType.fromApiValue(request.getType()));
        }

        if (request.getSeniority() != null) {
            job.setSeniority(JobSeniority.fromApiValue(request.getSeniority()));
        }

        if (request.getModel() != null) {
            job.setModel(JobModel.fromApiValue(request.getModel()));
        }

        if (request.getLocation() != null) {
            job.setLocation(request.getLocation());
        }

        if (request.getDescription() != null) {
            job.setDescription(request.getDescription());
        }

        if (request.getSkillIds() != null) {
            Set<Skill> skills = new HashSet<>(skillRepository.findAllById(request.getSkillIds()));
            if (skills.size() != request.getSkillIds().size()) {
                throw new ResourceNotFoundException("One or more skills do not exist");
            }
            job.setSkills(skills);
        }

        Job savedJob = jobRepository.save(job);

        // Update job embedding
        jobMatchingService.storeJobEmbedding(savedJob);

        return jobMapper.toJobResponse(savedJob);
    }

    @Transactional
    public JobResponse closeJob(Long id, Authentication connectedUser) {

        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job with ID " + id + " not found"));

        User user = ((User) connectedUser.getPrincipal());

        if (!Objects.equals(job.getPostedBy().getId(), user.getId())) {
            throw new OperationNotPermittedException("You are not permitted to close this job");
        }

        if (job.getStatus() == JobStatus.CLOSED) {
            throw new BusinessLogicException("Job with ID " + id + " is already closed");
        }

        job.setStatus(JobStatus.CLOSED);

        Job savedJob = jobRepository.save(job);

        // Delete embedding from vector store
        jobMatchingService.deleteJobEmbedding(savedJob.getId());

        return jobMapper.toJobResponse(savedJob);
    }

    @Transactional
    public JobResponse pauseJob(Long id, Authentication connectedUser) {

        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job with ID " + id + " not found"));

        User user = ((User) connectedUser.getPrincipal());

        if (!Objects.equals(job.getPostedBy().getId(), user.getId())) {
            throw new OperationNotPermittedException("You are not permitted to pause this job");
        }

        if (job.getStatus() == JobStatus.CLOSED) {
            throw new BusinessLogicException("Job with ID " + id + " is closed and cannot be paused");
        }

        if (job.getStatus() == JobStatus.PAUSED) {
            throw new BusinessLogicException("Job with ID " + id + " is already paused");
        }

        job.setStatus(JobStatus.PAUSED);

        Job savedJob = jobRepository.save(job);

        // Update embedding with PAUSED status (for filtering)
        jobMatchingService.storeJobEmbedding(savedJob);

        return jobMapper.toJobResponse(savedJob);
    }

    @Transactional
    public JobResponse resumeJob(Long id, Authentication connectedUser) {

        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job with ID " + id + " not found"));

        User user = ((User) connectedUser.getPrincipal());

        if (!Objects.equals(job.getPostedBy().getId(), user.getId())) {
            throw new OperationNotPermittedException("You are not permitted to resume this job");
        }

        if (job.getStatus() == JobStatus.CLOSED) {
            throw new BusinessLogicException("Job with ID " + id + " is closed and cannot be resumed");
        }

        if (job.getStatus() == JobStatus.OPEN) {
            throw new BusinessLogicException("Job with ID " + id + " is already OPEN");
        }

        job.setStatus(JobStatus.OPEN);

        Job savedJob = jobRepository.save(job);

        // Update embedding with OPEN status (for filtering)
        jobMatchingService.storeJobEmbedding(savedJob);

        NotificationEvent notificationEvent = new NotificationEvent(
                UUID.randomUUID().toString(),
                NotificationEventType.JOB_POSTED_SUCCESSFULLY,
                savedJob.getPostedBy().getId(),
                List.of(DispatchPayload.Channel.EMAIL),
                Map.of(
                        "jobTitle",  savedJob.getTitle(),
                        "jobType",   savedJob.getType().name(),
                        "seniority", savedJob.getSeniority().name(),
                        "location",  savedJob.getLocation() != null ? savedJob.getLocation() : "Remote",
                        "jobUrl",    "TODO" + savedJob.getId() //TODO
                )
        );

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                notificationKafkaTemplate.send(NOTIFICATION_EVENTS,
                                String.valueOf(savedJob.getId()), notificationEvent)
                        .exceptionally(ex -> {
                            log.error("Failed to publish submission notification for application {}",
                                    savedJob.getId(), ex);
                            return null;
                        });
            }
        });
        return jobMapper.toJobResponse(savedJob);
    }

    public PageResponse<ApplicationResponse> getJobApplications(
            Long jobId, int pageNumber, int size) {
        Pageable pageable = (Pageable) PageRequest.of(pageNumber, size, Sort.by("lastUpdate").descending());

        Page<Application> applications = applicationRepository.getJobApplications(jobId, pageable);

        List<ApplicationResponse> applicationResponses = applications.stream()
                .map(applicationMapper::toApplicationResponse)
                .sorted(Comparator.comparing(
                        ApplicationResponse::matchingRatio,
                        Comparator.nullsLast(Double::compareTo)
                ).reversed())
                .toList();

        return new PageResponse<>(
                applicationResponses,
                applications.getNumber(),
                applications.getSize(),
                applications.getTotalElements(),
                applications.getTotalPages(),
                applications.isFirst(),
                applications.isLast());
    }

    public PageResponse<ApplicationResponse> getJobApplicationsByStatus(
            Long jobId, ApplicationStatus status, int pageNumber, int size) {
        Pageable pageable = (Pageable) PageRequest.of(pageNumber, size, Sort.by("lastUpdate").descending());

        Page<Application> applications = applicationRepository.getApplicationByStatus(jobId, status, pageable);

        List<ApplicationResponse> applicationResponses = applications.stream()
                .map(applicationMapper::toApplicationResponse)
                .toList();

        return new PageResponse<>(
                applicationResponses,
                applications.getNumber(),
                applications.getSize(),
                applications.getTotalElements(),
                applications.getTotalPages(),
                applications.isFirst(),
                applications.isLast());
    }
    // export application to excel file
    public ExportTaskResponse startExportTask(Long jobId, Authentication connectedUser) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job with ID " + jobId + " not found"));
        User user = ((User) connectedUser.getPrincipal());

        if (!Objects.equals(job.getPostedBy().getId(), user.getId())) {
            throw new OperationNotPermittedException("You are not permitted to pause this job");
        }

        List<Application> applications =
                applicationRepository.findAllByJobIdForRecruiter(jobId)
                        .stream()
                        .sorted(Comparator.comparing(
                                Application::getMatchingRatio,
                                Comparator.nullsLast(Double::compareTo)
                        ).reversed())
                        .toList();


        String taskId = UUID.randomUUID().toString();
        ExportTask task = new ExportTask(taskId, jobId,TASK_EXPIRE_TIME);
        exportTasks.put(taskId, task);

        Thread.ofVirtual().name("export-job-" + jobId).start(() -> processExport(task, applications));

        return exportTaskMapper.toExportTaskResponse(task);
    }

    private void processExport(ExportTask task, List<Application> applications) {
        try {
            byte[] data = applicationExcelExportService.generateExcel(applications);
            task.setData(data);
            task.setStatus(ExportTask.Status.COMPLETED);
        } catch (Exception e) {
            task.setStatus(ExportTask.Status.FAILED);
            task.setErrorMessage(e.getMessage());
        }
    }

    public ExportTask getExportTask(String taskId) {
        ExportTask task = exportTasks.get(taskId);
        if (task == null) {
            throw new ResourceNotFoundException("Export task with ID " + taskId + " not found");
        }
        return task;
    }

    public ExportTaskResponse getExportTaskStatus(Long jobId, String taskId, Authentication connectedUser) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job with ID " + jobId + " not found"));
        User user = ((User) connectedUser.getPrincipal());
        ExportTask task = getExportTask(taskId);
        if ((!Objects.equals(job.getPostedBy().getId(), user.getId())) && task.getJobId() == jobId) {
            throw new OperationNotPermittedException("You are not permitted to pause this job");
        }


        String downloadUrl = null;
        if (task.getStatus() == ExportTask.Status.COMPLETED) {
            downloadUrl = "/api/v1/jobs/" + jobId + "/applications/export/" + taskId + "/download";
        }

        return exportTaskMapper.toExportTaskResponse(task, downloadUrl);
    }

    public String getJobTitle(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job with ID " + jobId + " not found"));
        return job.getTitle();
    }

    public byte[] getExportedFileData(Long jobId, String taskId, Authentication connectedUser) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job with ID " + jobId + " not found"));
        User user = ((User) connectedUser.getPrincipal());

        if (!Objects.equals(job.getPostedBy().getId(), user.getId())) {
            throw new OperationNotPermittedException("You are not permitted to pause this job");
        }
        ExportTask task = getExportTask(taskId);

        if (task.getStatus() != ExportTask.Status.COMPLETED) {
            throw new BusinessLogicException("Export task is not completed yet");
        }

        return task.getData();
    }

    @Scheduled(fixedDelayString = "${app.export.cleanup-interval-ms}")
    public void cleanExpiredTasks() {
        exportTasks.entrySet().removeIf(
                entry -> entry.getValue().getExpireAt().isBefore(Instant.now())
        );
    }
}
