package com.upply.experience;

import lombok.Builder;

import java.util.Date;

@Builder
public record ExperienceResponse(
        Long id,
        String title,
        String organization,
        Date startDate,
        Date endDate,
        String description
) {
}
