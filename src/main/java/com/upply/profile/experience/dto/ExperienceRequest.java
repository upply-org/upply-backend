package com.upply.profile.experience.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Date;

public record ExperienceRequest(
        @NotBlank
        String title,
        @NotBlank
        String organization,
        @NotNull
        Date startDate,

        Date endDate,
        @NotBlank
        String description
) {
}
