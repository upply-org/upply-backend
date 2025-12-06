package com.upply.skill;

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

    @NotBlank
    SkillCategory skillCategory;
}
