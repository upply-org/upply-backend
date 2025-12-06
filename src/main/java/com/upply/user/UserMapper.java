package com.upply.user;

import com.upply.experience.ExperienceMapper;
import com.upply.project.ProjectMapper;
import com.upply.skill.SkillMapper;
import com.upply.socialLink.SocialLinkMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserMapper {

    private final SkillMapper skillMapper;
    private final ExperienceMapper experienceMapper;
    private final ProjectMapper projectMapper;
    private final SocialLinkMapper socialLinkMapper;

    public UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .university(user.getUniversity())
                .skills(user.getUserSkills().stream()
                        .map(skillMapper::toSkillResponse)
                        .collect(Collectors.toSet())
                )
                .experiences(user.getExperiences() != null ? user.getExperiences()
                        .stream()
                        .map(experienceMapper::toExperienceResponse)
                        .toList() : Collections.emptyList()
                )
                .projects(user.getProjects() != null ? user.getProjects()
                        .stream()
                        .map(projectMapper::toProjectResponse)
                        .toList() : Collections.emptyList()
                )
                .socialLinks(user.getSocialLinks() != null ? user.getSocialLinks()
                        .stream()
                        .map(socialLinkMapper::toSocialLinkResponse)
                        .toList() : Collections.emptyList()
                )
                .build();
    }
}
