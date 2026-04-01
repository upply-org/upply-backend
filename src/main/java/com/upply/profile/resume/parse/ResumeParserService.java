package com.upply.profile.resume.parse;

import com.upply.common.NormalizeSkillName;
import com.upply.exception.custom.ResourceNotFoundException;
import com.upply.profile.experience.Experience;
import com.upply.profile.experience.ExperienceRepository;
import com.upply.profile.experience.dto.ExperienceMapper;
import com.upply.profile.experience.dto.ExperienceRequest;
import com.upply.profile.project.Project;
import com.upply.profile.project.ProjectRepository;
import com.upply.profile.project.dto.ProjectMapper;
import com.upply.profile.project.dto.ProjectRequest;
import com.upply.profile.resume.AzureStorageService;
import com.upply.profile.resume.Resume;
import com.upply.profile.resume.ResumeRepository;
import com.upply.profile.resume.dto.ParseConfirmRequest;
import com.upply.profile.resume.dto.ParsedResumeResponse;
import com.upply.profile.skill.Skill;
import com.upply.profile.skill.SkillRepository;
import com.upply.profile.socialLink.SocialLink;
import com.upply.profile.socialLink.SocialLinkRepository;
import com.upply.profile.socialLink.dto.SocialLinkMapper;
import com.upply.profile.socialLink.dto.SocialLinkRequest;
import com.upply.user.User;
import com.upply.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@Slf4j
public class ResumeParserService {
    private final ChatClient geminiChatClient;
    private final ChatClient groqChatClient;
    private final UserRepository userRepository;
    private final ResumeRepository resumeRepository;
    private final AzureStorageService azureStorageService;
    private final SkillRepository skillRepository;
    private final ExperienceRepository experienceRepository;
    private final ProjectRepository projectRepository;
    private final SocialLinkRepository socialLinkRepository;

    private final ExperienceMapper experienceMapper;
    private final ProjectMapper projectMapper;
    private final SocialLinkMapper socialLinkMapper;

    public ResumeParserService(
            @Qualifier("resumeParserGroqChatClient") ChatClient groqChatClient,
            @Qualifier("resumeParserGeminiChatClient") ChatClient geminiChatClient,
            UserRepository userRepository,
            ResumeRepository resumeRepository,
            AzureStorageService azureStorageService,
            SkillRepository skillRepository,
            ExperienceRepository experienceRepository,
            ProjectRepository projectRepository,
            SocialLinkRepository socialLinkRepository,
            ExperienceMapper experienceMapper,
            ProjectMapper projectMapper,
            SocialLinkMapper socialLinkMapper) {
        this.groqChatClient = groqChatClient;
        this.geminiChatClient = geminiChatClient;
        this.userRepository = userRepository;
        this.resumeRepository = resumeRepository;
        this.azureStorageService = azureStorageService;
        this.skillRepository = skillRepository;
        this.experienceRepository = experienceRepository;
        this.projectRepository = projectRepository;
        this.socialLinkRepository = socialLinkRepository;
        this.experienceMapper = experienceMapper;
        this.projectMapper = projectMapper;
        this.socialLinkMapper = socialLinkMapper;
    }

    public ParsedResumeResponse preview(Long resumeId) {
        return callAi(extractText(resumeId));
    }

    @Transactional
    public void confirm(Long resumeId, ParseConfirmRequest request) {
        User user = userRepository.getCurrentUser()
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        //TODO Don't hit the model again use preview response
        ParsedResumeResponse parsed = callAi(extractText(resumeId));

        if (request.applyPersonal()) applyPersonal(user, parsed);
        if (request.applyExperiences()) applyExperiences(user, parsed.experiences());
        if (request.applyProjects()) applyProjects(user, parsed.projects());
        if (request.applySocialLinks()) applySocialLinks(user, parsed.socialLinks());

        if (request.selectedSkills() != null && !request.selectedSkills().isEmpty()) {
            applySkills(user, request.selectedSkills());
        }
    }


    public ParsedResumeResponse callAi(String rawText) {
        String prompt = """
                Extract all structured data from this resume and return JSON.
                
                Resume:
                \"\"\"
                %s
                \"\"\"
                """.formatted(truncate(rawText, 12000));
        try {
            log.debug("Attempting resume parse with Gemini");
            return geminiChatClient.prompt()
                    .user(prompt)
                    .call()
                    .entity(ParsedResumeResponse.class);
        } catch (Exception e) {
            return groqChatClient.prompt()
                    .user(prompt)
                    .call()
                    .entity(ParsedResumeResponse.class);
        }
    }


    private void applyPersonal(User user, ParsedResumeResponse parsed) {
        if (isBlank(user.getUniversity()) && parsed.university() != null)
            user.setUniversity(parsed.university());
        if (isBlank(user.getFirstName()) && parsed.firstName() != null)
            user.setFirstName(parsed.firstName());
        if (isBlank(user.getLastName()) && parsed.lastName() != null)
            user.setLastName(parsed.lastName());
        userRepository.save(user);
    }

    private void applySkills(User user, List<String> selectedSkills) {
        selectedSkills.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .forEach(name -> {
                    String normalized = NormalizeSkillName.normalizeSkill(name);
                    Skill skill = skillRepository.findSkillByName(normalized)
                            .orElseGet(() -> skillRepository.save(
                                    Skill.builder().name(name).build()
                            ));
                    user.getUserSkills().add(skill);
                });
        userRepository.save(user);
    }

    private void applyExperiences(User user, List<ExperienceRequest> list) {
        if (list == null || list.isEmpty()) return;
        list.forEach(req -> {
            Experience e = experienceMapper.toExperience(req);
            e.setUser(user);
            experienceRepository.save(e);
        });
    }

    private void applyProjects(User user, List<ProjectRequest> list) {
        if (list == null || list.isEmpty()) return;
        list.forEach(req -> {
            Project p = projectMapper.toProject(req);
            p.setUser(user);
            projectRepository.save(p);
        });
    }

    private void applySocialLinks(User user, List<SocialLinkRequest> list) {
        if (list == null || list.isEmpty()) return;

        Set<String> existingUrls = new HashSet<>(socialLinkRepository
                .findUserSocialLinksByUserId()
                .stream()
                .map(s -> s.getUrl().toLowerCase())
                .toList());

        list.stream()
                .filter(req -> req.url() != null)
                .filter(req -> existingUrls.add(req.url().toLowerCase()))
                .forEach(req -> {
                    SocialLink s = socialLinkMapper.toSocialLink(req);
                    s.setUser(user);
                    socialLinkRepository.save(s);
                });
    }


    private String extractText(Long resumeId) {
        Resume resume = resumeRepository.getResumeById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Resume with ID " + resumeId + " not found"));

        byte[] bytes = azureStorageService.downloadFile(resume.getBlobName());

        try (PDDocument doc = Loader.loadPDF(bytes)) {
            if (doc.isEncrypted()) return "";
            String text = new PDFTextStripper().getText(doc).strip();
            return text.isEmpty() ? "" : text;
        } catch (IOException e) {
            log.warn("PDF extraction failed for resume {}", resumeId, e);
            return "";
        }
    }

    private String truncate(String text, int max) {
        if (text == null || text.isBlank()) return "";
        return text.length() <= max ? text : text.substring(0, max) + "…";
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
