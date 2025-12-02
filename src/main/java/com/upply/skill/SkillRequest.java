package com.upply.skill;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SkillRequest {
    @NotNull
    String skillName;

    @NotNull
    SkillCategory skillCategory;
}
