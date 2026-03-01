package com.upply.user;

import com.upply.exception.custom.OperationNotPermittedException;
import com.upply.profile.experience.*;
import com.upply.profile.experience.dto.ExperienceMapper;
import com.upply.profile.experience.dto.ExperienceRequest;
import com.upply.profile.experience.dto.ExperienceResponse;
import com.upply.profile.project.*;
import com.upply.profile.project.dto.ProjectMapper;
import com.upply.profile.project.dto.ProjectRequest;
import com.upply.profile.project.dto.ProjectResponse;
import com.upply.profile.resume.AzureStorageService;
import com.upply.profile.resume.Resume;
import com.upply.profile.resume.ResumeRepository;
import com.upply.profile.resume.dto.ResumeMapper;
import com.upply.profile.resume.dto.ResumeResponse;
import com.upply.profile.skill.*;
import com.upply.profile.skill.dto.SkillMapper;
import com.upply.profile.skill.dto.SkillRequest;
import com.upply.profile.skill.dto.SkillResponse;
import com.upply.profile.socialLink.*;
import com.upply.profile.socialLink.dto.SocialLinkMapper;
import com.upply.profile.socialLink.dto.SocialLinkRequest;
import com.upply.profile.socialLink.dto.SocialLinkResponse;
import com.upply.user.dto.UserMapper;
import com.upply.user.dto.UserRequest;
import com.upply.user.dto.UserResponse;
import com.upply.exception.custom.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    private final AzureStorageService azureStorageService;
    private final ResumeRepository resumeRepository;
    private final ResumeMapper resumeMapper;

    public UserResponse getUser() {
        return userRepository.getCurrentUser()
                .map(userMapper::toUserResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));
    }

    public void updateUser(UserRequest userRequest) {
        User user = userRepository.getCurrentUser()
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));

        user.setFirstName(userRequest.firstName());
        user.setLastName(userRequest.lastName());
        user.setUniversity(userRequest.university());

        userRepository.save(user);
    }

    //skill
    @Transactional
    public void addSkillToUser(Long skillId) {
        User user = userRepository.getCurrentUser()
                .orElseThrow(() -> new ResourceNotFoundException("User Not found"));

        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill Not found"));

        user.getUserSkills().add(skill);
        userRepository.save(user);
    }

    @Transactional
    public void addSkillByName(SkillRequest skillRequest) {
        User user = userRepository.getCurrentUser()
                .orElseThrow(() -> new ResourceNotFoundException("User Not found"));

        String normalizedName =
                ((skillRequest.getSkillName() == null) ? null : skillRequest.getSkillName().toLowerCase().replaceAll("\\s+", ""));
        Skill skill = skillRepository.findSkillByName(normalizedName)
                .orElseGet(() -> {
                    Skill newSkill = new Skill();
                    newSkill.setName(skillRequest.getSkillName());
                    return skillRepository.save(newSkill);
                });
        user.getUserSkills().add(skill);
        userRepository.save(user);
    }

    @Transactional
    public void removeSkillFromUser(Long skillId) {
        User user = userRepository.getCurrentUser()
                .orElseThrow(() -> new ResourceNotFoundException("User Not found"));

        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill Not found"));

        user.getUserSkills().remove(skill);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Set<SkillResponse> getUserSkills() {
        User user = userRepository.getCurrentUser()
                .orElseThrow(() -> new ResourceNotFoundException("User Not found"));

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

    public ExperienceResponse getUserExperienceById(Long experienceId) {
        Experience experience = experienceRepository.findExperienceById(experienceId)
                .orElseThrow(() -> new ResourceNotFoundException("There is no experience with this id"));
        return experienceMapper.toExperienceResponse(experience);
    }

    public Long addUserExperience(ExperienceRequest experienceRequest) {
        User user = userRepository.getCurrentUser()
                .orElseThrow(() -> new ResourceNotFoundException("User Not found"));
        Experience experience = experienceMapper.toExperience(experienceRequest);
        experience.setUser(user);
        return experienceRepository.save(experience).getId();
    }

    public void updateUserExperience(Long experienceId, ExperienceRequest experienceRequest) {
        Experience experience = experienceRepository.findExperienceById(experienceId)
                .orElseThrow(() -> new ResourceNotFoundException("There is no experience with this id"));

        experience.setTitle(experienceRequest.title());
        experience.setOrganization(experienceRequest.organization());
        experience.setStartDate(experienceRequest.startDate());
        experience.setEndDate(experienceRequest.endDate());
        experience.setDescription(experienceRequest.description());

        experienceRepository.save(experience);
    }

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

    public ProjectResponse getUserProjectById(Long projectId) {
        Project project = projectRepository.findProjectById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("There is no project with this id"));

        return projectMapper.toProjectResponse(project);
    }

    public Long addUserProject(ProjectRequest projectRequest) {
        User user = userRepository.getCurrentUser()
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));
        Project project = projectMapper.toProject(projectRequest);
        project.setUser(user);
        return projectRepository.save(project).getId();
    }

    public void updateUserProject(Long projectId, ProjectRequest projectRequest) {
        Project project = projectRepository.findProjectById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("There is no project with this id"));

        project.setTitle(projectRequest.title());
        project.setDescription(projectRequest.description());
        project.setProjectUrl(projectRequest.projectUrl());
        project.setStartDate(projectRequest.startDate());
        project.setEndDate(projectRequest.endDate());
        project.setTechnologies(projectRequest.technologies());

        projectRepository.save(project);
    }

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

    public SocialLinkResponse getUserSocialLinkById(Long socialId) {
        SocialLink socialLink = socialLinkRepository.findSocialLinkById(socialId)
                .orElseThrow(() -> new ResourceNotFoundException("There is no social links with this id"));
        return socialLinkMapper.toSocialLinkResponse(socialLink);
    }

    public Long addUserSocialLinks(SocialLinkRequest socialLinkRequest) {
        User user = userRepository.getCurrentUser()
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));

        SocialLink socialLink = socialLinkMapper.toSocialLink(socialLinkRequest);
        socialLink.setUser(user);

        return socialLinkRepository.save(socialLink).getId();
    }

    public void updateUserSocialLinks(Long socialId, SocialLinkRequest socialLinkRequest) {
        SocialLink socialLink = socialLinkRepository.findSocialLinkById(socialId)
                .orElseThrow(() -> new ResourceNotFoundException("There is no social links with this id"));

        socialLink.setUrl(socialLinkRequest.url());
        socialLink.setSocialType(socialLinkRequest.socialType());

        socialLinkRepository.save(socialLink);
    }

    public void deleteUserLinks(long socialId) {
        socialLinkRepository.deleteSocialLinkById(socialId);
    }

    //resume

    @Transactional
    public ResumeResponse addUserResume(MultipartFile resumeFile) throws IOException {
        validateFile(resumeFile);
        User user = userRepository.getCurrentUser()
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));
        String blobName = azureStorageService.uploadFile(user.getId(), resumeFile.getBytes());
        String fileName = resumeFile.getOriginalFilename();

        Resume resume = new Resume();

        resume.setBlobName(blobName);
        resume.setFileName(fileName);
        resume.setUser(user);

        resumeRepository.save(resume);
        return resumeMapper.toResumeResponse(resume);
    }

    public byte[] getResumeFileById(Long resumeId) {
        Resume resume = resumeRepository.getResumeById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("There is no resume with this id"));
        return azureStorageService.downloadFile(resume.getBlobName());
    }

    public List<ResumeResponse> getAllUserResumes() {
        return resumeRepository.getAllUserResumes()
                .stream()
                .map(resumeMapper::toResumeResponse)
                .toList();
    }

    public ResumeResponse getLastSubmittedResume() {
        Resume resume = resumeRepository.getLastSubmittedResume()
                .orElseThrow(() -> new ResourceNotFoundException("Failed to get last submitted resume"));
        return resumeMapper.toResumeResponse(resume);
    }

    public void deleteUserResume(Long resumeId) {
        Resume resume = resumeRepository.getResumeById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("There is no resume with this id"));

        if (Boolean.TRUE.equals(resume.getIsDeleted())) {
            throw new IllegalStateException("Resume is already deleted");
        }

        resume.setIsDeleted(true);
        resumeRepository.save(resume);
    }

    public String getFileName(Long resumeId) {
        Resume resume = resumeRepository.getResumeById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("There is no resume with this id"));
        return resume.getFileName();
    }

    private void validateFile(MultipartFile file) {
        long MAX_FILE_SIZE = 5 * 1024 * 1024;

        if (file == null || file.isEmpty()) {
            throw new OperationNotPermittedException("File is empty");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new OperationNotPermittedException("Only PDF files are allowed");
        }

        if (!"application/pdf".equals(file.getContentType())) {
            throw new OperationNotPermittedException("Only PDF files are allowed");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new OperationNotPermittedException("File size exceeds the 5MB limit");
        }
    }

    // mobile device token

    public void saveDeviceToken(String deviceToken) {
        User user = userRepository.getCurrentUser()
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));
        user.setDeviceToken(deviceToken);
        userRepository.save(user);
    }
}
