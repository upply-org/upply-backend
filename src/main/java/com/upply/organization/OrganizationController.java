package com.upply.organization;

import com.upply.common.PageResponse;
import com.upply.job.dto.JobListResponse;
import com.upply.job.enums.JobStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.upply.organization.dto.ConnectToOrganizationRequest;
import com.upply.organization.dto.ConnectToOrganizationResponse;
import com.upply.organization.dto.OrganizationResponse;
import com.upply.organization.dto.OrganizationUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/organizations")
@RequiredArgsConstructor
@Tag(name = "Organization", description = "APIs for managing organizations")
@Validated
public class OrganizationController {

    private final OrganizationService organizationService;

    @GetMapping("/{id}")
    @Operation(
            summary = "Get organization by ID",
            description = "Retrieves the details of an organization by its ID."
    )
    public ResponseEntity<OrganizationResponse> getOrganization(
            @Parameter(description = "The ID of the organization", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(organizationService.getOrganization(id));
    }

    @PatchMapping("/{id}")
    @Operation(
            summary = "Update organization",
            description = "Partially updates an existing organization. Only non-null fields are applied. Requires the authenticated user to be a recruiter of the organization."
    )
    public ResponseEntity<OrganizationResponse> updateOrganization(
            @Parameter(description = "The ID of the organization", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody OrganizationUpdateRequest request,
            Authentication connectedUser) {
        return ResponseEntity.ok(organizationService.updateOrganization(id, request, connectedUser));
    }

    @GetMapping("/{id}/jobs")
    @Operation(
            summary = "List open jobs for an organization (paginated)",
            description = "Retrieves a paginated list of jobs with status OPEN that belong to the specified organization."
    )
    public ResponseEntity<PageResponse<JobListResponse>> getOrganizationOpenJobs(
            @Parameter(description = "The ID of the organization", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Page number (0-indexed)", required = false, example = "0")
            @RequestParam(name = "page", defaultValue = "0", required = false)
            @Min(value = 0, message = "Page index must not be less than zero") int pageNumber,
            @Parameter(description = "Page size", required = false, example = "10")
            @RequestParam(name = "size", defaultValue = "10", required = false)
            @Min(value = 1, message = "Page size must be at least 1")
            @Max(value = 50, message = "Page size must not exceed 50") int size) {
        return ResponseEntity.ok(organizationService.getOrganizationOpenJobs(id, pageNumber, size));
    }

    @GetMapping("/{id}/jobs/{status}")
    @Operation(
            summary = "List jobs for an organization by status (paginated)",
            description = "Retrieves a paginated list of jobs for the specified organization filtered by status. Valid statuses: open, paused, closed."
    )
    public ResponseEntity<PageResponse<JobListResponse>> getOrganizationJobsByStatus(
            @Parameter(description = "The ID of the organization", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Job status to filter by", required = true, example = "open")
            @PathVariable JobStatus status,
            @Parameter(description = "Page number (0-indexed)", required = false, example = "0")
            @RequestParam(name = "page", defaultValue = "0", required = false)
            @Min(value = 0, message = "Page index must not be less than zero") int pageNumber,
            @Parameter(description = "Page size", required = false, example = "10")
            @RequestParam(name = "size", defaultValue = "10", required = false)
            @Min(value = 1, message = "Page size must be at least 1")
            @Max(value = 50, message = "Page size must not exceed 50") int size) {
        return ResponseEntity.ok(organizationService.getOrganizationJobsByStatus(id, status, pageNumber, size));
    }

    @PostMapping("/connect")
    @Operation(
            summary = "Initiate connection to an organization",
            description = "Validates business email domain and sends verification email to connect to an existing organization or create a new one."
    )
    public ResponseEntity<ConnectToOrganizationResponse> connectToOrganization(
            @Valid @RequestBody ConnectToOrganizationRequest request,
            Authentication connectedUser) throws JsonProcessingException {
        com.upply.user.User user = (com.upply.user.User) connectedUser.getPrincipal();
        return ResponseEntity.ok(organizationService.initiateConnection(request, user));
    }

    @GetMapping("/connect/verify")
    @Operation(
            summary = "Verify business email and complete connection",
            description = "Verifies the token sent to the user's business email and completes the connection to the organization."
    )
    public ResponseEntity<ConnectToOrganizationResponse> verifyAndConnect(
            @Parameter(description = "Verification token", required = true, example = "abc123xyz")
            @RequestParam("token") String token) throws JsonProcessingException {
        return ResponseEntity.ok(organizationService.verifyAndConnect(token));
    }
}
