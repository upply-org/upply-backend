package com.upply.skill;

import com.upply.profile.skill.*;
import com.upply.profile.skill.dto.SkillMapper;
import com.upply.profile.skill.dto.SkillRequest;
import com.upply.profile.skill.dto.SkillResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SkillMapper unit tests")
class SkillMapperTest {

    @InjectMocks
    private SkillMapper skillMapper;

    private SkillRequest testSkillRequest;
    private Skill testSkill;

    @BeforeEach
    void setUp() {
        testSkillRequest = new SkillRequest();
        testSkillRequest.setSkillName("Java Programming");

        testSkill = Skill.builder()
                .id(1L)
                .name("Java Programming")
                .build();
    }

    @Test
    @DisplayName("toSkill should map SkillRequest to Skill with all fields")
    void shouldMapSkillRequestToSkillWithAllFields() {
        Skill result = skillMapper.toSkill(testSkillRequest);

        assertNotNull(result);
        assertEquals("Java Programming", result.getName());
    }

    @Test
    @DisplayName("toSkill should map SkillRequest with null name to Skill")
    void shouldMapSkillRequestWithNullName() {
        SkillRequest requestWithNullName = new SkillRequest();
        requestWithNullName.setSkillName(null);

        Skill result = skillMapper.toSkill(requestWithNullName);

        assertNotNull(result);
        assertNull(result.getName());
    }

    @Test
    @DisplayName("toSkillResponse should map Skill to SkillResponse with all fields")
    void shouldMapSkillToSkillResponseWithAllFields() {
        SkillResponse result = skillMapper.toSkillResponse(testSkill);

        assertNotNull(result);
        assertEquals(1L, result.skillId());
        assertEquals("Java Programming", result.skillName());
    }

    @Test
    @DisplayName("toSkillResponse should map Skill with different fields to SkillResponse")
    void shouldMapSkillWithDifferentFieldsToSkillResponse() {
        Skill differentSkill = Skill.builder()
                .id(2L)
                .name("Python")
                .build();

        SkillResponse result = skillMapper.toSkillResponse(differentSkill);

        assertNotNull(result);
        assertEquals(2L, result.skillId());
        assertEquals("Python", result.skillName());
    }

    @Test
    @DisplayName("toSkillResponse should map Skill with null name to SkillResponse")
    void shouldMapSkillWithNullNameToSkillResponse() {
        Skill skillWithNullName = Skill.builder()
                .id(3L)
                .name(null)
                .build();

        SkillResponse result = skillMapper.toSkillResponse(skillWithNullName);

        assertNotNull(result);
        assertEquals(3L, result.skillId());
        assertNull(result.skillName());
    }

    @Test
    @DisplayName("toSkill should handle empty string skill name")
    void shouldHandleEmptyStringSkillName() {
        SkillRequest requestWithEmptyName = new SkillRequest();
        requestWithEmptyName.setSkillName("");

        Skill result = skillMapper.toSkill(requestWithEmptyName);

        assertNotNull(result);
        assertEquals("", result.getName());
    }
}

