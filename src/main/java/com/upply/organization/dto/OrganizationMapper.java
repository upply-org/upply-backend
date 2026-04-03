package com.upply.organization.dto;

import com.upply.organization.Organization;
import com.upply.organization.dto.OrganizationUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrganizationMapper {

    public Organization toOrganization(OrganizationRequest request) {
        return Organization.builder()
                .name(request.getName())
                .domain(request.getDomain())
                .description(request.getDescription())
                .website(request.getWebsite())
                .logoUrl(request.getLogoUrl())
                .industry(request.getIndustry())
                .size(request.getSize())
                .location(request.getLocation())
                .build();
    }

    public void updateFromRequest(OrganizationUpdateRequest request, Organization organization) {
        if (request.getName() != null) {
            organization.setName(request.getName());
        }
        if (request.getDescription() != null) {
            organization.setDescription(request.getDescription());
        }
        if (request.getWebsite() != null) {
            organization.setWebsite(request.getWebsite());
        }
        if (request.getLogoUrl() != null) {
            organization.setLogoUrl(request.getLogoUrl());
        }
        if (request.getIndustry() != null) {
            organization.setIndustry(request.getIndustry());
        }
        if (request.getSize() != null) {
            organization.setSize(request.getSize());
        }
        if (request.getLocation() != null) {
            organization.setLocation(request.getLocation());
        }
    }

    public OrganizationResponse toResponse(Organization organization) {
        return OrganizationResponse.builder()
                .id(organization.getId())
                .name(organization.getName())
                .domain(organization.getDomain())
                .description(organization.getDescription())
                .website(organization.getWebsite())
                .logoUrl(organization.getLogoUrl())
                .industry(organization.getIndustry())
                .size(organization.getSize())
                .location(organization.getLocation())
                .isVerified(organization.isVerified())
                .createdDate(organization.getCreatedDate())
                .lastModifiedDate(organization.getLastModifiedDate())
                .build();
    }
}
