package com.upply.application;

import com.upply.application.dto.ApplicationMapper;
import com.upply.application.dto.ApplicationRequest;
import com.upply.application.dto.ApplicationResponse;
import com.upply.application.enums.ApplicationStatus;
import com.upply.common.PageResponse;
import com.upply.exception.custom.BusinessLogicException;
import com.upply.exception.custom.OperationNotPermittedException;
import com.upply.exception.custom.ResourceNotFoundException;
import com.upply.job.Job;
import com.upply.job.JobRepository;
import com.upply.profile.resume.AzureStorageService;
import com.upply.profile.resume.Resume;
import com.upply.profile.resume.ResumeRepository;
import com.upply.user.User;
import com.upply.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ApplicationService {
    private final ApplicationMapper applicationMapper;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final ResumeRepository resumeRepository;
    private final AzureStorageService azureStorageService;

    @Transactional
    public ApplicationResponse createJobApplication(ApplicationRequest applicationRequest)
            throws IOException {
        User user = userRepository.getCurrentUser()
                .orElseThrow(() -> new ResourceNotFoundException("User Not found"));

        Job job = jobRepository.findById(applicationRequest.jobId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Job with ID " + applicationRequest.jobId() + " not found"));

        boolean alreadyApplied = applicationRepository.existsApplicationByApplicantAndJob(user, job);
        if (alreadyApplied) {
            throw new OperationNotPermittedException("You have already applied for this job");
        }

        Resume resume = resumeRepository.getResumeById(applicationRequest.resumeId())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid resume id"));

        Application application = applicationMapper.toApplication(applicationRequest);
        application.setApplicant(user);
        application.setJob(job);
        application.setResume(resume);
        application.setStatus(ApplicationStatus.SUBMITTED);

        applicationRepository.save(application);

        return applicationMapper.toApplicationResponse(application);
    }

    public PageResponse<ApplicationResponse> getAllApplications(
            int pageNumber, int size) {
        Pageable pageable = (Pageable) PageRequest.of(pageNumber, size, Sort.by("lastUpdate").descending());

        Page<Application> applications = applicationRepository.getUserApplications(pageable);

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

    public ApplicationResponse getApplicationById(
            Long applicationId) {
        Application application = applicationRepository.getApplicationById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application Not Found"));

        return applicationMapper.toApplicationResponse(application);
    }

    @Transactional
    public ApplicationResponse updateApplicationStatus(Long applicationId, ApplicationStatus newStatus) {
        Application application = applicationRepository.getApplicationById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application Not Found"));

        validateStatusTransition(application.getStatus(), newStatus);

        application.setStatus(newStatus);

        // add email to kafka queue;
        applicationRepository.save(application);
        return applicationMapper.toApplicationResponse(application);
    }

    public byte[] getApplicationResume(Long applicationId) {
        Application application = applicationRepository.getApplicationByIdForRecruiter(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application Not Found"));
        return azureStorageService.downloadFile(application.getResume().getBlobName());
    }

    private static final Map<ApplicationStatus, List<ApplicationStatus>> ALLOWED_TRANSITIONS = Map.of(
            ApplicationStatus.SUBMITTED, List.of(
                    ApplicationStatus.UNDER_REVIEW,
                    ApplicationStatus.REJECTED,
                    ApplicationStatus.WITHDRAWN),

            ApplicationStatus.UNDER_REVIEW, List.of(
                    ApplicationStatus.SHORTLISTED,
                    ApplicationStatus.REJECTED,
                    ApplicationStatus.WITHDRAWN),

            ApplicationStatus.SHORTLISTED, List.of(
                    ApplicationStatus.INTERVIEW,
                    ApplicationStatus.REJECTED,
                    ApplicationStatus.WITHDRAWN),

            ApplicationStatus.INTERVIEW, List.of(
                    ApplicationStatus.OFFERED,
                    ApplicationStatus.REJECTED,
                    ApplicationStatus.WITHDRAWN),

            ApplicationStatus.OFFERED, List.of(
                    ApplicationStatus.HIRED,
                    ApplicationStatus.REJECTED,
                    ApplicationStatus.WITHDRAWN),

            // terminate states
            ApplicationStatus.HIRED, List.of(),
            ApplicationStatus.REJECTED, List.of(),
            ApplicationStatus.WITHDRAWN, List.of());

    private void validateStatusTransition(ApplicationStatus currentStatus, ApplicationStatus newStatus) {
        if (currentStatus == null) {
            throw new BusinessLogicException(
                    "Current application status is null and cannot be transitioned.");
        }

        if (!ALLOWED_TRANSITIONS.getOrDefault(currentStatus, List.of()).contains(newStatus)) {
            throw new BusinessLogicException(
                    String.format("Cannot change status from %s to %s", currentStatus, newStatus));
        }
    }

    public String getResumeName(Long applicationId) {
        Application application = applicationRepository.getApplicationByIdForRecruiter(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application Not Found"));
        return application.getResume().getFileName();
    }

    // Application Notification Logic, let's use kafka!
}