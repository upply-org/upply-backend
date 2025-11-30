package com.upply.skill;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SkillService unit tests")
class SkillServiceTest {

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private SkillMapper skillMapper;

    @InjectMocks
    private SkillService skillService;

    private SkillRequest testSkillRequest;
    private Skill testSkill;
    private SkillResponse testSkillResponse;

    @BeforeEach
    void setUp() {
        testSkillRequest = new SkillRequest();
        testSkillRequest.skillName = "Java Programming";
        testSkillRequest.skillCategory = SkillCategory.BACKEND_DEVELOPMENT;

        testSkill = Skill.builder()
                .id(1L)
                .name("Java Programming")
                .category(SkillCategory.BACKEND_DEVELOPMENT)
                .build();

        testSkillResponse = SkillResponse.builder()
                .skillName("Java Programming")
                .skillCategory(SkillCategory.BACKEND_DEVELOPMENT)
                .build();
    }

    @Test
    @DisplayName("addSkill should map request and persist entity")
    void shouldAddSkillSuccessfully() {
        Skill persistedSkill = Skill.builder()
                .id(10L)
                .name("Java Programming")
                .category(SkillCategory.BACKEND_DEVELOPMENT)
                .build();

        when(skillMapper.toSkill(testSkillRequest)).thenReturn(testSkill);
        when(skillRepository.save(testSkill)).thenReturn(persistedSkill);

        Long skillId = skillService.addSkill(testSkillRequest);

        assertNotNull(skillId);
        assertEquals(10L, skillId);
        verify(skillMapper).toSkill(testSkillRequest);
        verify(skillRepository).save(testSkill);
    }

    @Test
    @DisplayName("getAllSkills should return mapped responses")
    void shouldReturnAllSkills() {
        Skill anotherSkill = Skill.builder()
                .id(2L)
                .name("React")
                .category(SkillCategory.FRONTEND_DEVELOPMENT)
                .build();

        SkillResponse anotherResponse = SkillResponse.builder()
                .skillName("React")
                .skillCategory(SkillCategory.FRONTEND_DEVELOPMENT)
                .build();

        when(skillRepository.findAll()).thenReturn(List.of(testSkill, anotherSkill));
        when(skillMapper.toSkillResponse(testSkill)).thenReturn(testSkillResponse);
        when(skillMapper.toSkillResponse(anotherSkill)).thenReturn(anotherResponse);

        List<SkillResponse> result = skillService.getAllSkills();

        assertEquals(2, result.size());
        assertEquals(testSkillResponse, result.get(0));
        assertEquals(anotherResponse, result.get(1));
        verify(skillRepository).findAll();
        verify(skillMapper).toSkillResponse(testSkill);
        verify(skillMapper).toSkillResponse(anotherSkill);
    }

    @Test
    @DisplayName("getSkillsByCategory should filter by repository method")
    void shouldReturnSkillsByCategory() {
        when(skillRepository.findSkillsByCategory(SkillCategory.BACKEND_DEVELOPMENT))
                .thenReturn(List.of(testSkill));
        when(skillMapper.toSkillResponse(testSkill)).thenReturn(testSkillResponse);

        List<SkillResponse> result = skillService.getSkillsByCategory(SkillCategory.BACKEND_DEVELOPMENT);

        assertEquals(1, result.size());
        assertEquals(testSkillResponse, result.get(0));
        verify(skillRepository).findSkillsByCategory(SkillCategory.BACKEND_DEVELOPMENT);
        verify(skillMapper).toSkillResponse(testSkill);
    }

    @Test
    @DisplayName("getSkillsByName should delegate to repository and mapper")
    void shouldReturnSkillsByName() {
        // SkillService normalizes the name to lowercase and removes spaces
        when(skillRepository.findSkillByName("javaprogramming")).thenReturn(Optional.of(testSkill));
        when(skillMapper.toSkillResponse(testSkill)).thenReturn(testSkillResponse);

        SkillResponse result = skillService.getSkillsByName("Java Programming");

        assertNotNull(result);
        assertEquals(testSkillResponse, result);
        verify(skillRepository).findSkillByName("javaprogramming");
        verify(skillMapper).toSkillResponse(testSkill);
    }

    @Test
    @DisplayName("updateSkill should update persisted entity fields")
    void shouldUpdateSkillSuccessfully() {
        Skill existingSkill = Skill.builder()
                .id(5L)
                .name("Old Name")
                .category(SkillCategory.UI_UX_DESIGN)
                .build();

        SkillRequest updateRequest = new SkillRequest();
        updateRequest.skillName = "Updated Name";
        updateRequest.skillCategory = SkillCategory.FRONTEND_DEVELOPMENT;

        when(skillRepository.findById(5L)).thenReturn(Optional.of(existingSkill));

        skillService.updateSkill(5L, updateRequest);

        assertEquals("Updated Name", existingSkill.getName());
        assertEquals(SkillCategory.FRONTEND_DEVELOPMENT, existingSkill.getCategory());
        verify(skillRepository).findById(5L);
        verify(skillRepository).save(existingSkill);
    }

    @Test
    @DisplayName("updateSkill should throw when skill does not exist")
    void shouldThrowWhenSkillNotFoundDuringUpdate() {
        when(skillRepository.findById(42L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> skillService.updateSkill(42L, testSkillRequest)
        );

        assertEquals("There is no skill with this skill ID", exception.getMessage());
        verify(skillRepository).findById(42L);
        verify(skillRepository, never()).save(any());
    }
}