package com.upply.user;

import com.upply.experience.*;
import com.upply.project.*;
import com.upply.skill.*;
import com.upply.socialLink.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final ExperienceRepository experienceRepository;
    private final ProjectRepository projectRepository;
    private final SocialLinkRepository socialLinkRepository;
    private final UserMapper userMapper;
    private final SkillMapper skillMapper;
    private final ExperienceMapper experienceMapper;
    private final ProjectMapper projectMapper;
    private final SocialLinkMapper socialLinkMapper;

    public UserResponse getUser() {
        return userRepository.getCurrentUser()
                .map(userMapper::toUserResponse)
                .orElseThrow(() -> new EntityNotFoundException());
    }

    public void updateUser(UserRequest userRequest) {
        User user = userRepository.getCurrentUser()
                .orElseThrow(() -> new EntityNotFoundException("User Not Found"));

        user.setFirstName(userRequest.firstName());
        user.setLastName(userRequest.lastName());
        user.setUniversity(userRequest.university());

        userRepository.save(user);
    }

    //skill
    @Transactional
    public void addSkillToUser(Long skillId) {
        User user = userRepository.getCurrentUser()
                .orElseThrow(() -> new EntityNotFoundException("User Not found"));

        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new IllegalArgumentException("Skill Not found"));

        user.getUserSkills().add(skill);
        userRepository.save(user);
    }

    @Transactional
    public void addSkillByName(SkillRequest skillRequest) {
        User user = userRepository.getCurrentUser()
                .orElseThrow(() -> new EntityNotFoundException("User Not found"));

        String normalizedName =
                ((skillRequest.getSkillName() == null) ? null : skillRequest.getSkillName().toLowerCase().replaceAll("\\s+", ""));
        Skill skill = skillRepository.findSkillByName(normalizedName)
                .orElseGet(() -> {
                    Skill newSkill = new Skill();
                    newSkill.setName(skillRequest.getSkillName());
                    newSkill.setCategory(skillRequest.getSkillCategory());
                    return skillRepository.save(newSkill);
                });
        user.getUserSkills().add(skill);
        userRepository.save(user);
    }

    @Transactional
    public void removeSkillFromUser(Long skillId) {
        User user = userRepository.getCurrentUser()
                .orElseThrow(() -> new EntityNotFoundException("User Not found"));

        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new IllegalArgumentException("Skill Not found"));

        user.getUserSkills().remove(skill);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Set<SkillResponse> getUserSkills() {
        User user = userRepository.getCurrentUser()
                .orElseThrow(() -> new EntityNotFoundException("User Not found"));

        return user.getUserSkills().stream()
                .map(skillMapper::toSkillResponse)
                .collect(Collectors.toSet());
    }

    // experience

    public List<ExperienceResponse> getUserExperience() {
        return experienceRepository.findUserExperienceByUserId()
                .stream()
                .map(experienceMapper::toExperienceResponse)
                .toList();
    }

    public ExperienceResponse getUserExperienceById(Long experienceId){
        Experience experience = experienceRepository.findExperienceById(experienceId)
                .orElseThrow(() -> new EntityNotFoundException("There is no experience with this id"));
        return experienceMapper.toExperienceResponse(experience);
    }

    public Long addUserExperience(ExperienceRequest experienceRequest) {
        User user = userRepository.getCurrentUser()
                .orElseThrow(() -> new EntityNotFoundException("User Not found"));
        Experience experience = experienceMapper.toExperience(experienceRequest);
        experience.setUser(user);
        return experienceRepository.save(experience).getId();
    }

    public void updateUserExperience(Long experienceId, ExperienceRequest experienceRequest) {
        Experience experience = experienceRepository.findExperienceById(experienceId)
                .orElseThrow(() -> new EntityNotFoundException("There is no experience with this id"));

        experience.setTitle(experienceRequest.title());
        experience.setOrganization(experienceRequest.organization());
        experience.setStartDate(experienceRequest.startDate());
        experience.setEndDate(experienceRequest.endDate());
        experience.setDescription(experienceRequest.description());

        experienceRepository.save(experience);
    }

    @Transactional
    public void deleteUserExperience(Long experienceId) {
        experienceRepository.deleteExperienceById(experienceId);
    }

    //project

    public List<ProjectResponse> getUserProject() {
        return projectRepository.findUserProjectsByUserId()
                .stream()
                .map(projectMapper::toProjectResponse)
                .toList();
    }

    public ProjectResponse getUserProjectById(Long projectId){
        Project project = projectRepository.findProjectById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("There is no project with this id"));

        return projectMapper.toProjectResponse(project);
    }

    public Long addUserProject(ProjectRequest projectRequest) {
        User user = userRepository.getCurrentUser()
                .orElseThrow(() -> new EntityNotFoundException("User Not Found"));
        Project project = projectMapper.toProject(projectRequest);
        project.setUser(user);
        return projectRepository.save(project).getId();
    }

    public void updateUserProject(Long projectId, ProjectRequest projectRequest) {
        Project project = projectRepository.findProjectById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("There is no project with this id"));

        project.setTitle(projectRequest.title());
        project.setDescription(projectRequest.description());
        project.setProjectUrl(projectRequest.projectUrl());
        project.setStartDate(projectRequest.startDate());
        project.setEndDate(projectRequest.endDate());
        project.setTechnologies(projectRequest.technologies());

        projectRepository.save(project);
    }

    @Transactional
    public void deleteUserProject(Long projectId) {
        projectRepository.deleteProjectById(projectId);
    }

    //social links

    public List<SocialLinkResponse> getUserSocialLinks() {
        return socialLinkRepository.findUserSocialLinksByUserId()
                .stream()
                .map(socialLinkMapper::toSocialLinkResponse)
                .toList();
    }

    public SocialLinkResponse getUserSocialLinkById(Long socialId){
        SocialLink socialLink = socialLinkRepository.findSocialLinkById(socialId)
                .orElseThrow(() -> new EntityNotFoundException("There is no social links with this id"));
        return socialLinkMapper.toSocialLinkResponse(socialLink);
    }

    public Long addUserSocialLinks(SocialLinkRequest socialLinkRequest) {
        User user = userRepository.getCurrentUser()
                .orElseThrow(() -> new EntityNotFoundException("User Not Found"));

        SocialLink socialLink = socialLinkMapper.toSocialLink(socialLinkRequest);
        socialLink.setUser(user);

        return socialLinkRepository.save(socialLink).getId();
    }

    public void updateUserSocialLinks(Long socialId, SocialLinkRequest socialLinkRequest) {
        SocialLink socialLink = socialLinkRepository.findSocialLinkById(socialId)
                .orElseThrow(() -> new EntityNotFoundException("There is no social links with this id"));

        socialLink.setUrl(socialLinkRequest.url());
        socialLink.setSocialType(socialLinkRequest.socialType());

        socialLinkRepository.save(socialLink);
    }

    @Transactional
    public void deleteUserLinks(long socialId){
        socialLinkRepository.deleteSocialLinById(socialId);
    }

}
