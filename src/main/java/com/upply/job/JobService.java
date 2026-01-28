package com.upply.job;

import com.upply.application.Application;
import com.upply.application.ApplicationRepository;
import com.upply.application.dto.ApplicationMapper;
import com.upply.application.dto.ApplicationResponse;
import com.upply.application.enums.ApplicationStatus;
import com.upply.common.PageResponse;
import com.upply.exception.OperationNotPermittedException;
import com.upply.job.dto.*;
import com.upply.job.enums.JobModel;
import com.upply.job.enums.JobSeniority;
import com.upply.job.enums.JobStatus;
import com.upply.job.enums.JobType;
import com.upply.profile.skill.Skill;
import com.upply.profile.skill.SkillRepository;
import com.upply.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobMapper jobMapper;
    private final ApplicationMapper applicationMapper;
    private final JobRepository jobRepository;
    private final SkillRepository skillRepository;
    private final ApplicationRepository applicationRepository;
    private final JobMatchingService jobMatchingService;

    @Transactional
    public JobResponse createJob(@Valid JobRequest request, Authentication connectedUser) {

        Set<Skill> skills = new HashSet<>(
                skillRepository.findAllById(request.getSkillIds())
        );

        if (skills.size() != request.getSkillIds().size()) {
            throw new IllegalArgumentException("One or more skills do not exist");
        }

        Job job = jobMapper.toJob(request, skills);

        User user = (User) connectedUser.getPrincipal();
        job.setPostedBy(user);

        Job savedJob = jobRepository.save(job);

        // Store job embedding
        jobMatchingService.storeJobEmbedding(savedJob);

        return jobMapper.toJobResponse(savedJob);
    }


    public JobResponse getJob(Long id) {

        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Job with ID " + id + " not found"));

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
                jobs.isLast()
        );
    }

    public List<MatchedJobListResponse> getMatchedJobs(Authentication connectedUser) {

        User user = (User) connectedUser.getPrincipal();

        List<JobMatchingService.JobWithScore> matchedJobsWithScores = jobMatchingService.findSimilarJobs(user, 50);

        return matchedJobsWithScores.stream()
                .map(jobWithScore -> jobMapper.toMatchedJobListResponse(
                        jobWithScore.getJob(),
                        jobWithScore.getScore()
                ))
                .toList();
    }


    @Transactional
    public JobResponse updateJob(Long id, JobUpdateRequest request, Authentication connectedUser) {

        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Job with ID " + id + " not found"));

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
                throw new IllegalArgumentException("One or more skills do not exist");
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
                .orElseThrow(() -> new IllegalArgumentException("Job with ID " + id + " not found"));

        User user = ((User) connectedUser.getPrincipal());

        if (!Objects.equals(job.getPostedBy().getId(), user.getId())) {
            throw new OperationNotPermittedException("You are not permitted to close this job");
        }

        if (job.getStatus() == JobStatus.CLOSED) {
            throw new IllegalArgumentException("Job with ID " + id + " is already closed");
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
                .orElseThrow(() -> new IllegalArgumentException("Job with ID " + id + " not found"));

        User user = ((User) connectedUser.getPrincipal());

        if (!Objects.equals(job.getPostedBy().getId(), user.getId())) {
            throw new OperationNotPermittedException("You are not permitted to pause this job");
        }

        if (job.getStatus() == JobStatus.CLOSED) {
            throw new IllegalArgumentException("Job with ID " + id + " is closed and cannot be paused");
        }

        if (job.getStatus() == JobStatus.PAUSED) {
            throw new IllegalArgumentException("Job with ID " + id + " is already paused");
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
                .orElseThrow(() -> new IllegalArgumentException("Job with ID " + id + " not found"));

        User user = ((User) connectedUser.getPrincipal());

        if (!Objects.equals(job.getPostedBy().getId(), user.getId())) {
            throw new OperationNotPermittedException("You are not permitted to resume this job");
        }

        if (job.getStatus() == JobStatus.CLOSED) {
            throw new IllegalArgumentException("Job with ID " + id + " is closed and cannot be resumed");
        }

        if (job.getStatus() == JobStatus.OPEN) {
            throw new IllegalArgumentException("Job with ID " + id + " is already OPEN");
        }

        job.setStatus(JobStatus.OPEN);

        Job savedJob = jobRepository.save(job);

        // Update embedding with OPEN status (for filtering)
        jobMatchingService.storeJobEmbedding(savedJob);

        return jobMapper.toJobResponse(savedJob);
    }

    public PageResponse<ApplicationResponse> getJobApplications(
            Long jobId, int pageNumber, int size
    ) {
        Pageable pageable = (Pageable) PageRequest.of(pageNumber, size, Sort.by("lastUpdate").descending());

        Page<Application> applications = applicationRepository.getJobApplications(jobId ,pageable);

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
                applications.isLast()
        );
    }

    public PageResponse<ApplicationResponse> getJobApplicationsByStatus(
            Long jobId ,ApplicationStatus status ,int pageNumber, int size
    ) {
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
                applications.isLast()
        );
    }

}
