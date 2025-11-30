package com.upply.skill;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SkillService {

    private final SkillRepository skillRepository;
    private final SkillMapper skillMapper;

    public Long addSkill(SkillRequest skillRequest) {
        Skill skill = skillMapper.toSkill(skillRequest);
        return skillRepository.save(skill).getId();
    }

    public List<SkillResponse> getAllSkills() {
        return skillRepository.findAll().stream()
                .map(skillMapper::toSkillResponse)
                .toList();
    }

    public List<SkillResponse> getSkillsByCategory(SkillCategory skillCategory) {
        return skillRepository.findSkillsByCategory(skillCategory)
                .stream()
                .map(skillMapper::toSkillResponse)
                .toList();
    }

    public SkillResponse getSkillsByName(String name) {
        String normalizedName =
                ((name == null) ? null : name.toLowerCase().replaceAll("\\s+", ""));
        Skill skill =  skillRepository.findSkillByName(normalizedName)
                .orElseThrow(() -> new EntityNotFoundException("No Skill found with this name"));
        return skillMapper.toSkillResponse(skill);
    }

    public void updateSkill(Long skillId, SkillRequest skillRequest) {
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new IllegalArgumentException("There is no skill with this skill ID"));

        skill.setName(skillRequest.skillName);
        skill.setCategory(skillRequest.skillCategory);

        skillRepository.save(skill);
    }
}
