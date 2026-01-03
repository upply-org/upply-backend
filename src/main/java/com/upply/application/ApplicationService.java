package com.upply.application;

import com.upply.application.dto.ApplicationMapper;
import com.upply.application.dto.ApplicationRequest;
import com.upply.application.dto.ApplicationResponse;
import com.upply.application.enums.ApplicationStatus;
import com.upply.common.PageResponse;
import com.upply.exception.OperationNotPermittedException;
import com.upply.job.Job;
import com.upply.job.JobRepository;
import com.upply.job.dto.JobListResponse;
import com.upply.user.User;
import com.upply.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.multipart.MultipartFile;


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

    public ApplicationResponse createJobApplication(ApplicationRequest applicationRequest, MultipartFile resume) throws IOException {
        User user = userRepository.getCurrentUser()
                .orElseThrow(() -> new EntityNotFoundException("User Not found"));

        Job job = jobRepository.findById(applicationRequest.jobId())
                .orElseThrow(() -> new IllegalArgumentException("Job with ID " + applicationRequest.jobId() + " not found"));

        boolean alreadyApplied = applicationRepository.existsApplicationByApplicantAndJob(user ,job);
        if (alreadyApplied) {
            throw new OperationNotPermittedException("You have already applied for this job");
        }

        validateFile(resume);
        String resumeContent = extractTextFromPdf(resume);

        ApplicationRequest updateApplicationRequest = new ApplicationRequest(
                resumeContent,
                applicationRequest.jobId(),
                applicationRequest.coverLetter()
        );

        Application application = applicationMapper.toApplication(updateApplicationRequest);
        application.setApplicant(user);
        application.setJob(job);
        application.setStatus(ApplicationStatus.SUBMITTED);

        applicationRepository.save(application);

        return applicationMapper.toApplicationResponse(application);
    }

    public PageResponse<ApplicationResponse> getAllApplications(
            int pageNumber, int size
    ) {
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
                applications.isLast()
        );
    }

    public ApplicationResponse getApplicationById(
            Long applicationId
    ) {
        Application application = applicationRepository.getApplicationById(applicationId)
                .orElseThrow(() -> new EntityNotFoundException("Application Not Found"));

        return applicationMapper.toApplicationResponse(application);
    }

    @Transactional
    public ApplicationResponse updateApplicationStatus(Long applicationId, ApplicationStatus newStatus) {
        Application application = applicationRepository.getApplicationById(applicationId)
                .orElseThrow(() -> new EntityNotFoundException("Application Not Found"));

        validateStatusTransition(application.getStatus(), newStatus);

        application.setStatus(newStatus);

        // add email to kafka queue;
        applicationRepository.save(application);
        return applicationMapper.toApplicationResponse(application);
    }

    private void validateFile(MultipartFile file) {
        if (file == null ||file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (!"application/pdf".equals(file.getContentType())) {
            throw new IllegalArgumentException("Only PDF files are allowed");
        }
    }

    private String extractTextFromPdf(MultipartFile file) throws IOException {
        try (PDDocument document = org.apache.pdfbox.Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            stripper.setLineSeparator("\n");
            String fileContent = stripper.getText(document);
            return fileContent;
        }
    }

    private static final Map<ApplicationStatus, List<ApplicationStatus>> ALLOWED_TRANSITIONS = Map.of(
            ApplicationStatus.SUBMITTED, List.of(
                    ApplicationStatus.UNDER_REVIEW,
                    ApplicationStatus.REJECTED,
                    ApplicationStatus.WITHDRAWN
            ),

            ApplicationStatus.UNDER_REVIEW, List.of(
                    ApplicationStatus.SHORTLISTED,
                    ApplicationStatus.REJECTED,
                    ApplicationStatus.WITHDRAWN
            ),

            ApplicationStatus.SHORTLISTED, List.of(
                    ApplicationStatus.INTERVIEW,
                    ApplicationStatus.REJECTED,
                    ApplicationStatus.WITHDRAWN
            ),

            ApplicationStatus.INTERVIEW, List.of(
                    ApplicationStatus.OFFERED,
                    ApplicationStatus.REJECTED,
                    ApplicationStatus.WITHDRAWN
            ),

            ApplicationStatus.OFFERED, List.of(
                    ApplicationStatus.HIRED,
                    ApplicationStatus.REJECTED,
                    ApplicationStatus.WITHDRAWN
            ),

            // terminate states
            ApplicationStatus.HIRED, List.of(),
            ApplicationStatus.REJECTED, List.of(),
            ApplicationStatus.WITHDRAWN, List.of()
    );

    private void validateStatusTransition(ApplicationStatus currentStatus, ApplicationStatus newStatus) {
        if (currentStatus == null) {
            throw new IllegalStateException("Current application status is null and cannot be transitioned.");
        }

        if (!ALLOWED_TRANSITIONS.getOrDefault(currentStatus, List.of()).contains(newStatus)) {
            throw new IllegalStateException(
                    String.format("Cannot change status from %s to %s", currentStatus, newStatus)
            );
        }
    }

    // Application Notification Logic, let's use kafka!
}