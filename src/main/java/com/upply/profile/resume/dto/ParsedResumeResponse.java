package com.upply.profile.resume.dto;

import com.upply.profile.experience.dto.ExperienceRequest;
import com.upply.profile.project.dto.ProjectRequest;
import com.upply.profile.socialLink.dto.SocialLinkRequest;

import java.util.List;
import java.util.Objects;

public record ParsedResumeResponse(
        String university,
        String firstName,
        String lastName,

        List<ExperienceRequest> experiences,
        List<ProjectRequest> projects,
        List<String> skills,
        List<SocialLinkRequest> socialLinks,
        // true if the text appears truncated or incomplete.
        boolean partialExtraction
) {
    public ParsedResumeResponse {
        experiences = experiences == null ? List.of()
                : experiences.stream().filter(Objects::nonNull).toList();
        projects = projects == null ? List.of()
                : projects.stream().filter(Objects::nonNull).toList();
        skills = skills == null ? List.of()
                : skills.stream().filter(s -> s != null && !s.isBlank()).toList();
        socialLinks = socialLinks == null ? List.of()
                : socialLinks.stream().filter(Objects::nonNull).toList();
    }
}
