package com.upply.profile.skill.dto;

import lombok.Builder;
@Builder
public record SkillResponse(
        Long skillId,
        String skillName
) {
}
