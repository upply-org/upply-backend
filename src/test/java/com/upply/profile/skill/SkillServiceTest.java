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
        testSkillRequest.setSkillName("Java Programming");

        testSkill = Skill.builder()
                .id(1L)
                .name("Java Programming")
                .build();

        testSkillResponse = SkillResponse.builder()
                .skillName("Java Programming")
                .build();
    }

    @Test
    @DisplayName("addSkill should map request and persist entity")
    void shouldAddSkillSuccessfully() {
        Skill persistedSkill = Skill.builder()
                .id(10L)
                .name("Java Programming")
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
                .build();

        SkillResponse anotherResponse = SkillResponse.builder()
                .skillName("React")
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
                .build();

        SkillRequest updateRequest = new SkillRequest();
        updateRequest.setSkillName("Updated Name");

        when(skillRepository.findById(5L)).thenReturn(Optional.of(existingSkill));

        skillService.updateSkill(5L, updateRequest);

        assertEquals("Updated Name", existingSkill.getName());
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