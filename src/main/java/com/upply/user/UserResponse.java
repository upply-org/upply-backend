package com.upply.user;

import com.upply.experience.ExperienceResponse;
import com.upply.project.ProjectResponse;
import com.upply.skill.SkillResponse;
import com.upply.socialLink.SocialLinkResponse;
import lombok.*;

import java.util.List;
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
    private List<ExperienceResponse> experiences;
    private List<ProjectResponse> projects;
    private List<SocialLinkResponse> socialLinks;
}
