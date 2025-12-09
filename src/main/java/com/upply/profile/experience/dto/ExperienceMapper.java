package com.upply.profile.experience.dto;

import com.upply.profile.experience.Experience;
import org.springframework.stereotype.Service;

@Service

public class ExperienceMapper {
    public Experience toExperience(ExperienceRequest experienceRequest) {
        return Experience.builder()
                .title(experienceRequest.title())
                .organization(experienceRequest.organization())
                .startDate(experienceRequest.startDate())
                .endDate(experienceRequest.endDate())
                .description(experienceRequest.description())
                .build();
    }

    public ExperienceResponse toExperienceResponse(Experience experience) {
        return ExperienceResponse.builder()
                .id(experience.getId())
                .title(experience.getTitle())
                .organization(experience.getOrganization())
                .startDate(experience.getStartDate())
                .endDate(experience.getEndDate())
                .description(experience.getDescription())
                .build();
    }
}
