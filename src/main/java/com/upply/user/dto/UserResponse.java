package com.upply.user.dto;

import com.upply.profile.experience.dto.ExperienceResponse;
import com.upply.profile.project.dto.ProjectResponse;
import com.upply.profile.skill.dto.SkillResponse;
import com.upply.profile.socialLink.dto.SocialLinkResponse;
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
