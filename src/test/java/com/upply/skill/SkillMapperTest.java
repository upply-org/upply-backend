package com.upply.skill;

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
        testSkillRequest.setSkillCategory(SkillCategory.BACKEND_DEVELOPMENT);

        testSkill = Skill.builder()
                .id(1L)
                .name("Java Programming")
                .category(SkillCategory.BACKEND_DEVELOPMENT)
                .build();
    }

    @Test
    @DisplayName("toSkill should map SkillRequest to Skill with all fields")
    void shouldMapSkillRequestToSkillWithAllFields() {
        Skill result = skillMapper.toSkill(testSkillRequest);

        assertNotNull(result);
        assertEquals("Java Programming", result.getName());
        assertEquals(SkillCategory.BACKEND_DEVELOPMENT, result.getCategory());
    }

    @Test
    @DisplayName("toSkill should map SkillRequest with different category to Skill")
    void shouldMapSkillRequestWithDifferentCategory() {
        SkillRequest frontendRequest = new SkillRequest();
        frontendRequest.setSkillName("React");
        frontendRequest.setSkillCategory(SkillCategory.FRONTEND_DEVELOPMENT);

        Skill result = skillMapper.toSkill(frontendRequest);

        assertNotNull(result);
        assertEquals("React", result.getName());
        assertEquals(SkillCategory.FRONTEND_DEVELOPMENT, result.getCategory());
    }

    @Test
    @DisplayName("toSkill should map SkillRequest with null name to Skill")
    void shouldMapSkillRequestWithNullName() {
        SkillRequest requestWithNullName = new SkillRequest();
        requestWithNullName.setSkillName(null);
        requestWithNullName.setSkillCategory(SkillCategory.BACKEND_DEVELOPMENT);

        Skill result = skillMapper.toSkill(requestWithNullName);

        assertNotNull(result);
        assertNull(result.getName());
        assertEquals(SkillCategory.BACKEND_DEVELOPMENT, result.getCategory());
    }

    @Test
    @DisplayName("toSkillResponse should map Skill to SkillResponse with all fields")
    void shouldMapSkillToSkillResponseWithAllFields() {
        SkillResponse result = skillMapper.toSkillResponse(testSkill);

        assertNotNull(result);
        assertEquals(1L, result.skillId());
        assertEquals("Java Programming", result.skillName());
        assertEquals(SkillCategory.BACKEND_DEVELOPMENT, result.skillCategory());
    }

    @Test
    @DisplayName("toSkillResponse should map Skill with different fields to SkillResponse")
    void shouldMapSkillWithDifferentFieldsToSkillResponse() {
        Skill differentSkill = Skill.builder()
                .id(2L)
                .name("Python")
                .category(SkillCategory.DATA_SCIENCE)
                .build();

        SkillResponse result = skillMapper.toSkillResponse(differentSkill);

        assertNotNull(result);
        assertEquals(2L, result.skillId());
        assertEquals("Python", result.skillName());
        assertEquals(SkillCategory.DATA_SCIENCE, result.skillCategory());
    }

    @Test
    @DisplayName("toSkillResponse should map Skill with null name to SkillResponse")
    void shouldMapSkillWithNullNameToSkillResponse() {
        Skill skillWithNullName = Skill.builder()
                .id(3L)
                .name(null)
                .category(SkillCategory.UI_UX_DESIGN)
                .build();

        SkillResponse result = skillMapper.toSkillResponse(skillWithNullName);

        assertNotNull(result);
        assertEquals(3L, result.skillId());
        assertNull(result.skillName());
        assertEquals(SkillCategory.UI_UX_DESIGN, result.skillCategory());
    }

    @Test
    @DisplayName("toSkillResponse should map Skill with all categories correctly")
    void shouldMapSkillWithAllCategories() {
        SkillCategory[] categories = {
                SkillCategory.BACKEND_DEVELOPMENT,
                SkillCategory.FRONTEND_DEVELOPMENT,
                SkillCategory.MOBILE_DEVELOPMENT,
                SkillCategory.DATA_SCIENCE,
                SkillCategory.PROJECT_MANAGEMENT
        };

        for (int i = 0; i < categories.length; i++) {
            Skill skill = Skill.builder()
                    .id((long) (i + 10))
                    .name("Skill " + i)
                    .category(categories[i])
                    .build();

            SkillResponse result = skillMapper.toSkillResponse(skill);

            assertNotNull(result);
            assertEquals((long) (i + 10), result.skillId());
            assertEquals("Skill " + i, result.skillName());
            assertEquals(categories[i], result.skillCategory());
        }
    }

    @Test
    @DisplayName("toSkill should handle empty string skill name")
    void shouldHandleEmptyStringSkillName() {
        SkillRequest requestWithEmptyName = new SkillRequest();
        requestWithEmptyName.setSkillName("");
        requestWithEmptyName.setSkillCategory(SkillCategory.BACKEND_DEVELOPMENT);

        Skill result = skillMapper.toSkill(requestWithEmptyName);

        assertNotNull(result);
        assertEquals("", result.getName());
        assertEquals(SkillCategory.BACKEND_DEVELOPMENT, result.getCategory());
    }
}

