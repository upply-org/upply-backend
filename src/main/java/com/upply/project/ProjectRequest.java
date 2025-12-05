package com.upply.project;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.Date;

@Builder
public record ProjectRequest(
        @NotNull
        String title,
        @NotNull
        String description,
        String projectUrl,
        @NotNull
        Date startDate,
        Date endDate,
        String technologies
) {
}
