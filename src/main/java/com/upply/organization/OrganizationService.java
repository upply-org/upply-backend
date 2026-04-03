package com.upply.organization;

import com.upply.common.PageResponse;
import com.upply.exception.custom.OperationNotPermittedException;
import com.upply.exception.custom.ResourceNotFoundException;
import com.upply.job.dto.JobListResponse;
import com.upply.job.dto.JobMapper;
import com.upply.job.enums.JobStatus;
import com.upply.organization.dto.OrganizationMapper;
import com.upply.organization.dto.OrganizationRequest;
import com.upply.organization.dto.OrganizationResponse;
import com.upply.organization.dto.OrganizationUpdateRequest;
import com.upply.user.User;
import com.upply.user.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@RequiredArgsConstructor
@Validated
@Transactional(readOnly = true)
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMapper organizationMapper;
    private final JobMapper jobMapper;
    private final UserRepository userRepository;

    @Transactional
    public OrganizationResponse createOrganization(@Valid OrganizationRequest request, Authentication connectedUser) {

        Organization organization = organizationMapper.toOrganization(request);

        User user = (User) connectedUser.getPrincipal();
        user.setOrganization(organization);
        organization.getRecruiters().add(user);

        Organization savedOrganization = organizationRepository.save(organization);
        userRepository.save(user);

        return organizationMapper.toResponse(savedOrganization);
    }

    public OrganizationResponse getOrganization(Long id) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization with ID " + id + " not found"));

        return organizationMapper.toResponse(organization);
    }

    @Transactional
    public OrganizationResponse updateOrganization(Long id, @Valid OrganizationUpdateRequest request, Authentication connectedUser) {

        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization with ID " + id + " not found"));

        User user = (User) connectedUser.getPrincipal();

        boolean isRecruiter = organization.getRecruiters().stream()
                .anyMatch(r -> r.getId().equals(user.getId()));

        if (!isRecruiter) {
            throw new OperationNotPermittedException("You are not permitted to update this organization");
        }

        organizationMapper.updateFromRequest(request, organization);

        Organization savedOrganization = organizationRepository.save(organization);

        return organizationMapper.toResponse(savedOrganization);
    }

    public PageResponse<JobListResponse> getOrganizationOpenJobs(Long orgId, int pageNumber, int size) {

        if (!organizationRepository.existsById(orgId)) {
            throw new ResourceNotFoundException("Organization with ID " + orgId + " not found");
        }

        Pageable pageable = PageRequest.of(pageNumber, size, Sort.by("createdDate").descending());

        var jobs = organizationRepository.findJobsByOrganizationIdAndStatus(orgId, JobStatus.OPEN, pageable);

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
}
