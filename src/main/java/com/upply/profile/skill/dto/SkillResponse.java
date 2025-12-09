package com.upply.profile.skill.dto;

import com.upply.profile.skill.SkillCategory;
import lombok.Builder;
@Builder
public record SkillResponse(
        Long skillId,
        String skillName,
        SkillCategory skillCategory
) {
}
