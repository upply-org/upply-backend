package com.upply.application;

import com.upply.application.dto.ApplicationRequest;
import com.upply.application.dto.ApplicationResponse;
import com.upply.application.enums.ApplicationStatus;
import com.upply.common.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/applications")
@RequiredArgsConstructor
@Tag(name = "Application", description = "APIs for managing job applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Create job application",
            description = "Creates a new job application with a resume file (PDF) and optional cover letter. The resume is parsed to extract text content."
    )
    public ResponseEntity<ApplicationResponse> createJobApplication(
            @Valid @RequestPart("application") ApplicationRequest applicationRequest,
            @Parameter(
                    description = "Resume file in PDF format",
                    required = true
            )
            @RequestPart("file") MultipartFile resume)
            throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED).body(applicationService.createJobApplication(applicationRequest, resume));
    }

    @GetMapping("/me")
    @Operation(
            summary = "Get all applicant applications",
            description = "Retrieves all job applications submitted by the authenticated applicant. Returns a paginated list of applications."
    )
    public ResponseEntity<PageResponse<ApplicationResponse>> getAllApplicantApplications(
            @Parameter(
                    description = "Page number (0-indexed)",
                    required = false,
                    example = "0"
            )
            @RequestParam(name = "page", defaultValue = "0", required = false) int pageNumber,
            @Parameter(
                    description = "Page size",
                    required = false,
                    example = "10"
            )
            @RequestParam(name = "size", defaultValue = "10", required = false) int size) {
        return ResponseEntity.ok(applicationService.getAllApplications(pageNumber, size));
    }

    @GetMapping("/{applicationId}")
    @Operation(
            summary = "Get application by ID",
            description = "Retrieves a specific job application by its ID, including applicant details, cover letter, status, and matching ratio."
    )
    public ResponseEntity<ApplicationResponse> getApplicationById(
            @Parameter(
                    description = "The ID of the application to retrieve",
                    required = true,
                    example = "1"
            )
            @PathVariable Long applicationId
    ){
        return ResponseEntity.ok(applicationService.getApplicationById(applicationId));
    }

    @PatchMapping("/{applicationId}/status")
    @Operation(
            summary = "Update application status",
            description = "Updates the status of a job application. Valid statuses include SUBMITTED, UNDER_REVIEW, SHORTLISTED, INTERVIEW, OFFERED, HIRED, REJECTED, and WITHDRAWN."
    )
    public ResponseEntity<ApplicationResponse> updateApplicationStatus(
            @Parameter(
                    description = "The new status for the application",
                    required = true,
                    example = "UNDER_REVIEW"
            )
            @RequestParam ApplicationStatus status,
            @Parameter(
                    description = "The ID of the application to update",
                    required = true,
                    example = "1"
            )
            @PathVariable Long applicationId
    ){
        return ResponseEntity.ok(applicationService.updateApplicationStatus(applicationId, status));
    }

    // TODO : update, delete endpoints. (after support storage)
}
