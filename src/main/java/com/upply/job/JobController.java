package com.upply.job;

import com.upply.common.PageResponse;
import com.upply.job.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @PostMapping
    public ResponseEntity<JobResponse> createJob(@Valid @RequestBody JobRequest request, Authentication connectedUser) {
        return ResponseEntity.ok(jobService.createJob(request, connectedUser));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getJob(@PathVariable Long id) {
        return ResponseEntity.ok(jobService.getJob(id));
    }

    @GetMapping
    public ResponseEntity<PageResponse<JobListResponse>> getAllOpenJobs(
            @RequestParam(name = "page", defaultValue = "0", required = false) int pageNumber,
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
    public ResponseEntity<JobResponse> updateJob(@PathVariable Long id, @Valid @RequestBody JobUpdateRequest request, Authentication connectedUser) {
        return ResponseEntity.ok(jobService.updateJob(id, request, connectedUser));
    }

    @PatchMapping("/{id}/pause")
    public ResponseEntity<JobResponse> pauseJob(@PathVariable Long id, Authentication connectedUser) {
        return ResponseEntity.ok(jobService.pauseJob(id, connectedUser));
    }

    @PatchMapping("/{id}/resume")
    public ResponseEntity<JobResponse> resumeJob(@PathVariable Long id, Authentication connectedUser) {
        return ResponseEntity.ok(jobService.resumeJob(id, connectedUser));
    }

    @PatchMapping("/{id}/close")
    public ResponseEntity<JobResponse> closeJob(@PathVariable Long id, Authentication connectedUser) {
        return ResponseEntity.ok(jobService.closeJob(id, connectedUser));
    }
}
