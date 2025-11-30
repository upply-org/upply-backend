package com.upply.user;

import com.upply.skill.SkillResponse;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private String email;
    private String firstName;
    private String lastName;
    private String university;
    private Set<SkillResponse> skills;
}
