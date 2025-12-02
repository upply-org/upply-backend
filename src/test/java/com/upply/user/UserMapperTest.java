package com.upply.user;

import com.upply.skill.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserMapper unit tests")
class UserMapperTest {

    @Mock
    private SkillMapper skillMapper;

    @InjectMocks
    private UserMapper userMapper;

    private User testUser;
    private Skill testSkill1;
    private Skill testSkill2;
    private SkillResponse testSkillResponse1;
    private SkillResponse testSkillResponse2;

    @BeforeEach
    void setUp() {
        testSkill1 = Skill.builder()
                .id(1L)
                .name("Java Programming")
                .category(SkillCategory.BACKEND_DEVELOPMENT)
                .build();

        testSkill2 = Skill.builder()
                .id(2L)
                .name("React")
                .category(SkillCategory.FRONTEND_DEVELOPMENT)
                .build();

        testSkillResponse1 = SkillResponse.builder()
                .skillId(1L)
                .skillName("Java Programming")
                .skillCategory(SkillCategory.BACKEND_DEVELOPMENT)
                .build();

        testSkillResponse2 = SkillResponse.builder()
                .skillId(2L)
                .skillName("React")
                .skillCategory(SkillCategory.FRONTEND_DEVELOPMENT)
                .build();

        Set<Skill> skills = new HashSet<>();
        skills.add(testSkill1);
        skills.add(testSkill2);

        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .university("Test University")
                .userSkills(skills)
                .build();
    }

    @Test
    @DisplayName("toUserResponse should map User to UserResponse with all fields")
    void shouldMapUserToUserResponseWithAllFields() {
        when(skillMapper.toSkillResponse(testSkill1)).thenReturn(testSkillResponse1);
        when(skillMapper.toSkillResponse(testSkill2)).thenReturn(testSkillResponse2);

        UserResponse result = userMapper.toUserResponse(testUser);

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("Test University", result.getUniversity());
        assertNotNull(result.getSkills());
        assertEquals(2, result.getSkills().size());
        assertTrue(result.getSkills().contains(testSkillResponse1));
        assertTrue(result.getSkills().contains(testSkillResponse2));
        verify(skillMapper).toSkillResponse(testSkill1);
        verify(skillMapper).toSkillResponse(testSkill2);
    }

    @Test
    @DisplayName("toUserResponse should map User with no skills to UserResponse with empty skills set")
    void shouldMapUserWithNoSkillsToUserResponse() {
        User userWithoutSkills = User.builder()
                .id(2L)
                .email("noskills@example.com")
                .firstName("Jane")
                .lastName("Smith")
                .university("Another University")
                .userSkills(new HashSet<>())
                .build();

        UserResponse result = userMapper.toUserResponse(userWithoutSkills);

        assertNotNull(result);
        assertEquals("noskills@example.com", result.getEmail());
        assertEquals("Jane", result.getFirstName());
        assertEquals("Smith", result.getLastName());
        assertEquals("Another University", result.getUniversity());
        assertNotNull(result.getSkills());
        assertTrue(result.getSkills().isEmpty());
        verify(skillMapper, never()).toSkillResponse(any());
    }

    @Test
    @DisplayName("toUserResponse should map User with null fields to UserResponse")
    void shouldMapUserWithNullFieldsToUserResponse() {
        User userWithNulls = User.builder()
                .id(3L)
                .email("nulls@example.com")
                .firstName(null)
                .lastName(null)
                .university(null)
                .userSkills(new HashSet<>())
                .build();

        UserResponse result = userMapper.toUserResponse(userWithNulls);

        assertNotNull(result);
        assertEquals("nulls@example.com", result.getEmail());
        assertNull(result.getFirstName());
        assertNull(result.getLastName());
        assertNull(result.getUniversity());
        assertNotNull(result.getSkills());
        assertTrue(result.getSkills().isEmpty());
    }

    @Test
    @DisplayName("toUserResponse should map User with single skill to UserResponse")
    void shouldMapUserWithSingleSkillToUserResponse() {
        Set<Skill> singleSkill = new HashSet<>();
        singleSkill.add(testSkill1);

        User userWithSingleSkill = User.builder()
                .id(4L)
                .email("single@example.com")
                .firstName("Single")
                .lastName("Skill")
                .university("Single University")
                .userSkills(singleSkill)
                .build();

        when(skillMapper.toSkillResponse(testSkill1)).thenReturn(testSkillResponse1);

        UserResponse result = userMapper.toUserResponse(userWithSingleSkill);

        assertNotNull(result);
        assertEquals("single@example.com", result.getEmail());
        assertEquals(1, result.getSkills().size());
        assertTrue(result.getSkills().contains(testSkillResponse1));
        verify(skillMapper).toSkillResponse(testSkill1);
        verify(skillMapper, never()).toSkillResponse(testSkill2);
    }
}

