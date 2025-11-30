package com.upply.skill;

import lombok.Builder;
@Builder
public record SkillResponse(
        Long skillId,
        String skillName,
        SkillCategory skillCategory
) {
}
