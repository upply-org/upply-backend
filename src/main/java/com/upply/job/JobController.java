package com.upply.job;

import com.upply.application.dto.ApplicationResponse;
import com.upply.application.enums.ApplicationStatus;
import com.upply.common.PageResponse;
import com.upply.job.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
@Tag(name = "Job", description = "APIs for managing jobs")
public class JobController {

    private final JobService jobService;

    @PostMapping
    @Operation(
            summary = "Create a new job",
            description = "Creates a new job posting. Requires authentication."
    )
    public ResponseEntity<JobResponse> createJob(@Valid @RequestBody JobRequest request, Authentication connectedUser) {
        return ResponseEntity.ok(jobService.createJob(request, connectedUser));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get job by ID",
            description = "Retrieves the details of a job by its ID."
    )
    public ResponseEntity<JobResponse> getJob(@PathVariable Long id) {
        return ResponseEntity.ok(jobService.getJob(id));
    }

    @GetMapping
    @Operation(
            summary = "List open jobs (paginated)",
            description = "Retrieves a paginated list of jobs with status OPEN."
    )
    public ResponseEntity<PageResponse<JobListResponse>> getAllOpenJobs(
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
            @RequestParam(name = "size", defaultValue = "10", required = false) int size,
            Authentication connectedUser
    ) {
        return ResponseEntity.ok(jobService.getAllOpenJobs(pageNumber, size, connectedUser));
    }

    @GetMapping("/matched")
    @Operation(
            summary = "Get matched jobs for current user",
            description = "Returns jobs that match the user's profile using AI-powered similarity search. Jobs are ranked by match score."
    )
    public ResponseEntity<List<MatchedJobListResponse>> getMatchedJobs(Authentication connectedUser) {
        return ResponseEntity.ok(jobService.getMatchedJobs(connectedUser));
    }

    @PatchMapping("/{id}")
    @Operation(
            summary = "Update job",
            description = "Updates an existing job posting by ID. Requires authentication."
    )
    public ResponseEntity<JobResponse> updateJob(@PathVariable Long id, @Valid @RequestBody JobUpdateRequest request, Authentication connectedUser) {
        return ResponseEntity.ok(jobService.updateJob(id, request, connectedUser));
    }

    @PatchMapping("/{id}/pause")
    @Operation(
            summary = "Pause job",
            description = "Pauses a job posting by ID. Requires authentication."
    )
    public ResponseEntity<JobResponse> pauseJob(@PathVariable Long id, Authentication connectedUser) {
        return ResponseEntity.ok(jobService.pauseJob(id, connectedUser));
    }

    @PatchMapping("/{id}/resume")
    @Operation(
            summary = "Resume job",
            description = "Resumes a paused job posting by ID. Requires authentication."
    )
    public ResponseEntity<JobResponse> resumeJob(@PathVariable Long id, Authentication connectedUser) {
        return ResponseEntity.ok(jobService.resumeJob(id, connectedUser));
    }

    @PatchMapping("/{id}/close")
    @Operation(
            summary = "Close job",
            description = "Closes a job posting by ID. Requires authentication."
    )
    public ResponseEntity<JobResponse> closeJob(@PathVariable Long id, Authentication connectedUser) {
        return ResponseEntity.ok(jobService.closeJob(id, connectedUser));
    }

    @GetMapping("/{id}/applications")
    @Operation(
            summary = "List job applications (paginated)",
            description = "Retrieves a paginated list of applications submitted to a specific job."
    )
    public ResponseEntity<PageResponse<ApplicationResponse>> getJobApplications(
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
            @RequestParam(name = "size", defaultValue = "10", required = false) int size,
            @Parameter(
                    description = "The ID of the job",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(jobService.getJobApplications(id, pageNumber, size));
    }

    @GetMapping("/{id}/applications/{status}")
    @Operation(
        summary = "List job applications by status (paginated)",
        description = "Retrieves a paginated list of applications for a job filtered by status. Valid statuses include SUBMITTED, UNDER_REVIEW, SHORTLISTED, INTERVIEW, OFFERED, HIRED, REJECTED, and WITHDRAWN."
    )
    public ResponseEntity<PageResponse<ApplicationResponse>> getApplicationByStatus(
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
            @RequestParam(name = "size", defaultValue = "10", required = false) int size,
            @Parameter(
                    description = "The ID of the job",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id,
            @Parameter(
                    description = "Application status to filter by",
                    required = true,
                    example = "UNDER_REVIEW"
            )
            @PathVariable ApplicationStatus status
    ) {
        return ResponseEntity.ok(jobService.getJobApplicationsByStatus(id, status, pageNumber, size));
    }
}
