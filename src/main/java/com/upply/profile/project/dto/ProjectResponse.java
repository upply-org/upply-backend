package com.upply.profile.project.dto;

import lombok.Builder;

import java.util.Date;

@Builder
public record ProjectResponse(
        Long id,
        String title,
        String description,
        String projectUrl,
        Date startDate,
        Date endDate,
        String technologies
) {
}
