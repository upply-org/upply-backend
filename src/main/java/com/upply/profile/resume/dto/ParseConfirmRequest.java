package com.upply.profile.resume.dto;

import java.util.List;

public record ParseConfirmRequest(
        boolean applyPersonal,
        boolean applyExperiences,
        boolean applyProjects,
        boolean applySocialLinks,
        List<String> selectedSkills
) {
    public ParseConfirmRequest {
        selectedSkills = selectedSkills == null ? List.of() : List.copyOf(selectedSkills);
    }
}
