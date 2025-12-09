package com.upply.profile.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.Date;

@Builder
public record ProjectRequest(
        @NotBlank
        String title,
        @NotBlank
        String description,
        String projectUrl,
        @NotNull
        Date startDate,
        Date endDate,
        String technologies
) {
}
