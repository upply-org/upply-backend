package com.upply.profile.skill.dto;

import com.upply.profile.skill.SkillCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SkillRequest {
    @NotBlank
    String skillName;

    @NotNull
    SkillCategory skillCategory;
}
