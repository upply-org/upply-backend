package com.upply.experience;

import jakarta.validation.constraints.NotNull;

import java.util.Date;

public record ExperienceRequest(
        @NotNull
        String title,
        @NotNull
        String organization,
        @NotNull
        Date startDate,

        Date endDate,
        @NotNull
        String description
) {
}
