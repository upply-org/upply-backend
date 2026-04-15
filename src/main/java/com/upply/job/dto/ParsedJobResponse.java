package com.upply.job.dto;

import java.util.List;
import java.util.Objects;

public record ParsedJobResponse(


        String title,
        String organization,
        String type,
        String seniority,
        String model,
        String location,
        String description,
        String applicationLink,
        List<String> skills

) {
    public ParsedJobResponse {
        skills = skills == null
                ? List.of()
                : skills.stream()
                        .filter(s -> s != null && !s.isBlank())
                        .toList();
    }
}
