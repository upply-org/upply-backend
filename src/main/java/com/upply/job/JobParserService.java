package com.upply.job;

import com.upply.common.NormalizeSkillName;
import com.upply.exception.custom.BusinessLogicException;
import com.upply.job.dto.ParsedJobResponse;
import com.upply.job.enums.JobModel;
import com.upply.job.enums.JobSeniority;
import com.upply.job.enums.JobType;
import com.upply.profile.skill.Skill;
import com.upply.profile.skill.SkillRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class JobParserService {

    private final ChatClient geminiChatClient;
    private final ChatClient groqChatClient;
    private final SkillRepository skillRepository;

    public JobParserService(
            @Qualifier("jobImportGeminiChatClient") ChatClient geminiChatClient,
            @Qualifier("jobImportGroqChatClient")   ChatClient groqChatClient,
            SkillRepository skillRepository) {
        this.geminiChatClient = geminiChatClient;
        this.groqChatClient   = groqChatClient;
        this.skillRepository  = skillRepository;
    }

    public ParsedJobResponse parse(String rawText) {
        String prompt = buildPrompt(rawText);
        try {
            log.debug("Attempting job parse with Gemini");
            return geminiChatClient.prompt()
                    .user(prompt)
                    .call()
                    .entity(ParsedJobResponse.class);
        } catch (Exception geminiEx) {
            log.warn("Gemini job parse failed, falling back to Groq: {}", geminiEx.getMessage());
            try {
                return groqChatClient.prompt()
                        .user(prompt)
                        .call()
                        .entity(ParsedJobResponse.class);
            } catch (Exception groqEx) {
                log.error("Groq job parse also failed: {}", groqEx.getMessage());
                throw new BusinessLogicException(
                        "Failed to parse job description — both AI providers unavailable: "
                        + groqEx.getMessage());
            }
        }
    }

    public JobType resolveType(String rawValue) {
        return resolveEnum(JobType::fromApiValue, rawValue, "type");
    }

    public JobSeniority resolveSeniority(String rawValue) {
        return resolveEnum(JobSeniority::fromApiValue, rawValue, "seniority");
    }

    public JobModel resolveModel(String rawValue) {
        return resolveEnum(JobModel::fromApiValue, rawValue, "model");
    }

    public Set<Skill> resolveSkills(List<String> rawSkills) {
        Set<Skill> skills = new HashSet<>();
        if (rawSkills == null || rawSkills.isEmpty()) return skills;

        rawSkills.stream()
                .filter(name -> name != null && !name.isBlank())
                .forEach(name -> {
                    String normalized = NormalizeSkillName.normalizeSkill(name);
                    Skill skill = skillRepository.findSkillByName(normalized)
                            .orElseGet(() -> {
                                log.debug("Creating new skill: '{}'", name);
                                return skillRepository.save(
                                        Skill.builder()
                                                .name(name)
                                                .searchName(normalized)
                                                .build()
                                );
                            });
                    skills.add(skill);
                });

        return skills;
    }

    private String buildPrompt(String rawText) {
        return """
                Parse this job description and extract all structured fields.

                Job description:
                \"\"\"
                %s
                \"\"\"
                """.formatted(truncate(rawText, 12_000));
    }

    private <T> T resolveEnum(java.util.function.Function<String, T> fromApiValue,
                              String rawValue,
                              String fieldName) {
        if (rawValue == null || rawValue.isBlank()) return null;
        try {
            return fromApiValue.apply(rawValue);
        } catch (IllegalArgumentException e) {
            log.warn("AI returned unrecognised {} value '{}', treating as null", fieldName, rawValue);
            return null;
        }
    }

    private String truncate(String text, int max) {
        if (text == null || text.isBlank()) return "";
        return text.length() <= max ? text : text.substring(0, max) + "…";
    }
}
