package com.upply.job;

import com.upply.exception.custom.BusinessLogicException;
import com.upply.job.dto.ParsedJobResponse;
import com.upply.job.enums.JobModel;
import com.upply.job.enums.JobSeniority;
import com.upply.job.enums.JobType;
import com.upply.profile.skill.Skill;
import com.upply.profile.skill.SkillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JobParserService unit tests")
class JobParserServiceTest {

    @Mock
    private ChatClient geminiChatClient;

    @Mock
    private ChatClient groqChatClient;

    @Mock
    private SkillRepository skillRepository;

    @InjectMocks
    private JobParserService jobParserService;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("resolveType should return JobType for valid value")
    void shouldResolveTypeForValidValue() {
        JobType result = jobParserService.resolveType("full-time");
        assertEquals(JobType.FULL_TIME, result);
    }

    @Test
    @DisplayName("resolveType should return JobType for PART_TIME value")
    void shouldResolveTypeForPartTime() {
        JobType result = jobParserService.resolveType("part-time");
        assertEquals(JobType.PART_TIME, result);
    }

    @Test
    @DisplayName("resolveType should return JobType for INTERNSHIP value")
    void shouldResolveTypeForInternship() {
        JobType result = jobParserService.resolveType("internship");
        assertEquals(JobType.INTERNSHIP, result);
    }

    @Test
    @DisplayName("resolveType should return null for invalid value")
    void shouldReturnNullForInvalidTypeValue() {
        JobType result = jobParserService.resolveType("invalid-type");
        assertNull(result);
    }

    @Test
    @DisplayName("resolveType should return null for null value")
    void shouldReturnNullForNullTypeValue() {
        JobType result = jobParserService.resolveType(null);
        assertNull(result);
    }

    @Test
    @DisplayName("resolveType should return null for blank value")
    void shouldReturnNullForBlankTypeValue() {
        JobType result = jobParserService.resolveType("   ");
        assertNull(result);
    }

    @Test
    @DisplayName("resolveSeniority should return JobSeniority for valid value")
    void shouldResolveSeniorityForValidValue() {
        JobSeniority result = jobParserService.resolveSeniority("senior");
        assertEquals(JobSeniority.SENIOR, result);
    }

    @Test
    @DisplayName("resolveSeniority should return JobSeniority for JUNIOR value")
    void shouldResolveSeniorityForJunior() {
        JobSeniority result = jobParserService.resolveSeniority("junior");
        assertEquals(JobSeniority.JUNIOR, result);
    }

    @Test
    @DisplayName("resolveSeniority should return JobSeniority for MID value")
    void shouldResolveSeniorityForMid() {
        JobSeniority result = jobParserService.resolveSeniority("mid");
        assertEquals(JobSeniority.MID, result);
    }

    @Test
    @DisplayName("resolveSeniority should return JobSeniority for LEAD value")
    void shouldResolveSeniorityForLead() {
        JobSeniority result = jobParserService.resolveSeniority("lead");
        assertEquals(JobSeniority.LEAD, result);
    }

    @Test
    @DisplayName("resolveSeniority should return JobSeniority for MANAGER value")
    void shouldResolveSeniorityForManager() {
        JobSeniority result = jobParserService.resolveSeniority("manager");
        assertEquals(JobSeniority.MANAGER, result);
    }

    @Test
    @DisplayName("resolveSeniority should return null for invalid value")
    void shouldReturnNullForInvalidSeniorityValue() {
        JobSeniority result = jobParserService.resolveSeniority("invalid-seniority");
        assertNull(result);
    }

    @Test
    @DisplayName("resolveSeniority should return null for null value")
    void shouldReturnNullForNullSeniorityValue() {
        JobSeniority result = jobParserService.resolveSeniority(null);
        assertNull(result);
    }

    @Test
    @DisplayName("resolveSeniority should return null for blank value")
    void shouldReturnNullForBlankSeniorityValue() {
        JobSeniority result = jobParserService.resolveSeniority("   ");
        assertNull(result);
    }

    @Test
    @DisplayName("resolveModel should return JobModel for valid value")
    void shouldResolveModelForValidValue() {
        JobModel result = jobParserService.resolveModel("remote");
        assertEquals(JobModel.REMOTE, result);
    }

    @Test
    @DisplayName("resolveModel should return JobModel for ONSITE value")
    void shouldResolveModelForOnsite() {
        JobModel result = jobParserService.resolveModel("onsite");
        assertEquals(JobModel.ONSITE, result);
    }

    @Test
    @DisplayName("resolveModel should return JobModel for HYBRID value")
    void shouldResolveModelForHybrid() {
        JobModel result = jobParserService.resolveModel("hybrid");
        assertEquals(JobModel.HYBRID, result);
    }

    @Test
    @DisplayName("resolveModel should return null for invalid value")
    void shouldReturnNullForInvalidModelValue() {
        JobModel result = jobParserService.resolveModel("invalid-model");
        assertNull(result);
    }

    @Test
    @DisplayName("resolveModel should return null for null value")
    void shouldReturnNullForNullModelValue() {
        JobModel result = jobParserService.resolveModel(null);
        assertNull(result);
    }

    @Test
    @DisplayName("resolveModel should return null for blank value")
    void shouldReturnNullForBlankModelValue() {
        JobModel result = jobParserService.resolveModel("   ");
        assertNull(result);
    }

    @Test
    @DisplayName("resolveSkills should return set of skills from list when found in repository")
    void shouldResolveSkillsFromList() {
        Skill skill = Skill.builder().id(1L).name("Java").searchName("java").build();
        when(skillRepository.findSkillByName("java")).thenReturn(Optional.of(skill));
        when(skillRepository.findSkillByName("python")).thenReturn(Optional.empty());
        when(skillRepository.save(any(Skill.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Set<Skill> result = jobParserService.resolveSkills(List.of("Java", "Python"));

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("resolveSkills should return empty set for null input")
    void shouldReturnEmptySetForNullInput() {
        Set<Skill> result = jobParserService.resolveSkills(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("resolveSkills should return empty set for empty list")
    void shouldReturnEmptySetForEmptyList() {
        Set<Skill> result = jobParserService.resolveSkills(List.of());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("resolveSkills should create new skill when not found in repository")
    void shouldCreateNewSkillWhenNotFound() {
        when(skillRepository.findSkillByName(anyString())).thenReturn(Optional.empty());
        when(skillRepository.save(any(Skill.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Set<Skill> result = jobParserService.resolveSkills(List.of("NewSkill"));

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(skillRepository).save(any(Skill.class));
    }

    @Test
    @DisplayName("resolveSkills should filter null and blank strings")
    void shouldFilterNullAndBlankStrings() {
        Skill skill = Skill.builder().id(1L).name("Java").searchName("java").build();
        when(skillRepository.findSkillByName("java")).thenReturn(Optional.of(skill));
        when(skillRepository.findSkillByName("python")).thenReturn(Optional.empty());
        when(skillRepository.save(any(Skill.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Set<Skill> result = jobParserService.resolveSkills(Arrays.asList("Java", null, "  ", "Python"));

        assertNotNull(result);
        assertTrue(result.size() >= 1);
    }

    @Test
    @DisplayName("resolveSkills should filter blank strings")
    void shouldFilterBlankStrings() {
        Skill skill = Skill.builder().id(1L).name("Java").searchName("java").build();
        when(skillRepository.findSkillByName("java")).thenReturn(Optional.of(skill));

        Set<Skill> result = jobParserService.resolveSkills(List.of("  Java  "));

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}