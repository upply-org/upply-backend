package com.upply.project;

import lombok.Builder;

import java.util.Date;

@Builder
public record ProjectResponse(
        Long Id,
        String title,
        String description,
        String projectUrl,
        Date startDate,
        Date endDate,
        String technologies
) {
}
