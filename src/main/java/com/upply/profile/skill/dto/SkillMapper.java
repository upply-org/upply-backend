package com.upply.profile.skill.dto;

import com.upply.profile.skill.Skill;
import org.springframework.stereotype.Service;

@Service
public class SkillMapper {
    public Skill toSkill(SkillRequest request){
        return Skill.builder()
                .name(request.skillName)
                .category(request.skillCategory)
                .build();
    }

    public SkillResponse toSkillResponse(Skill skill){
        return SkillResponse.builder()
                .skillId(skill.getId())
                .skillName(skill.getName())
                .skillCategory(skill.getCategory())
                .build();
    }
}
