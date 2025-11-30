package com.upply.user;

import com.upply.skill.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService unit tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private SkillMapper skillMapper;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserRequest testUserRequest;
    private UserResponse testUserResponse;
    private Skill testSkill;
    private SkillResponse testSkillResponse;
    private SkillRequest testSkillRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .university("Test University")
                .userSkills(new HashSet<>())
                .build();

        testUserRequest = new UserRequest(
                "Jane",
                "Smith",
                "New University"
        );

        testUserResponse = UserResponse.builder()
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .university("Test University")
                .skills(new HashSet<>())
                .build();

        testSkill = Skill.builder()
                .id(1L)
                .name("Java Programming")
                .category(SkillCategory.BACKEND_DEVELOPMENT)
                .build();

        testSkillResponse = SkillResponse.builder()
                .skillId(1L)
                .skillName("Java Programming")
                .skillCategory(SkillCategory.BACKEND_DEVELOPMENT)
                .build();

        testSkillRequest = new SkillRequest();
        testSkillRequest.setSkillName("Python");
        testSkillRequest.setSkillCategory(SkillCategory.BACKEND_DEVELOPMENT);
    }

    @Test
    @DisplayName("getUser should return UserResponse when user exists")
    void shouldGetUserSuccessfully() {
        when(userRepository.getCurrentUser()).thenReturn(Optional.of(testUser));
        when(userMapper.toUserResponse(testUser)).thenReturn(testUserResponse);

        UserResponse result = userService.getUser();

        assertNotNull(result);
        assertEquals(testUserResponse, result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("John", result.getFirstName());
        verify(userRepository).getCurrentUser();
        verify(userMapper).toUserResponse(testUser);
    }

    @Test
    @DisplayName("getUser should throw EntityNotFoundException when user does not exist")
    void shouldThrowExceptionWhenUserNotFoundInGetUser() {
        when(userRepository.getCurrentUser()).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userService.getUser()
        );

        assertNotNull(exception);
        verify(userRepository).getCurrentUser();
        verify(userMapper, never()).toUserResponse(any());
    }

    @Test
    @DisplayName("updateUser should update user fields successfully")
    void shouldUpdateUserSuccessfully() {
        when(userRepository.getCurrentUser()).thenReturn(Optional.of(testUser));
        when(userRepository.save(testUser)).thenReturn(testUser);

        userService.updateUser(testUserRequest);

        assertEquals("Jane", testUser.getFirstName());
        assertEquals("Smith", testUser.getLastName());
        assertEquals("New University", testUser.getUniversity());
        verify(userRepository).getCurrentUser();
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("updateUser should throw EntityNotFoundException when user does not exist")
    void shouldThrowExceptionWhenUserNotFoundInUpdateUser() {
        when(userRepository.getCurrentUser()).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userService.updateUser(testUserRequest)
        );

        assertEquals("User Not Found", exception.getMessage());
        verify(userRepository).getCurrentUser();
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("addSkillToUser should add skill to user successfully")
    void shouldAddSkillToUserSuccessfully() {
        when(userRepository.getCurrentUser()).thenReturn(Optional.of(testUser));
        when(skillRepository.findById(1L)).thenReturn(Optional.of(testSkill));
        when(userRepository.save(testUser)).thenReturn(testUser);

        userService.addSkillToUser(1L);

        assertTrue(testUser.getUserSkills().contains(testSkill));
        verify(userRepository).getCurrentUser();
        verify(skillRepository).findById(1L);
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("addSkillToUser should throw EntityNotFoundException when user does not exist")
    void shouldThrowExceptionWhenUserNotFoundInAddSkillToUser() {
        when(userRepository.getCurrentUser()).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userService.addSkillToUser(1L)
        );

        assertEquals("User Not found", exception.getMessage());
        verify(userRepository).getCurrentUser();
        verify(skillRepository, never()).findById(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("addSkillToUser should throw IllegalArgumentException when skill does not exist")
    void shouldThrowExceptionWhenSkillNotFoundInAddSkillToUser() {
        when(userRepository.getCurrentUser()).thenReturn(Optional.of(testUser));
        when(skillRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.addSkillToUser(999L)
        );

        assertEquals("Skill Not found", exception.getMessage());
        verify(userRepository).getCurrentUser();
        verify(skillRepository).findById(999L);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("addSkillByName should add existing skill to user")
    void shouldAddExistingSkillByNameToUser() {
        when(userRepository.getCurrentUser()).thenReturn(Optional.of(testUser));
        // The method normalizes the name: "Python" -> "python"
        when(skillRepository.findSkillByName("python")).thenReturn(Optional.of(testSkill));
        when(userRepository.save(testUser)).thenReturn(testUser);

        userService.addSkillByName(testSkillRequest);

        assertTrue(testUser.getUserSkills().contains(testSkill));
        verify(userRepository).getCurrentUser();
        verify(skillRepository).findSkillByName("python");
        verify(skillRepository, never()).save(any());
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("addSkillByName should create and add new skill to user when skill does not exist")
    void shouldCreateAndAddNewSkillByNameToUser() {
        Skill newSkill = Skill.builder()
                .id(2L)
                .name("Python")
                .category(SkillCategory.BACKEND_DEVELOPMENT)
                .build();

        when(userRepository.getCurrentUser()).thenReturn(Optional.of(testUser));
        // The method normalizes the name: "Python" -> "python"
        when(skillRepository.findSkillByName("python")).thenReturn(Optional.empty());
        when(skillRepository.save(any(Skill.class))).thenAnswer(invocation -> {
            Skill skill = invocation.getArgument(0);
            skill.setId(2L);
            return skill;
        });
        when(userRepository.save(testUser)).thenReturn(testUser);

        userService.addSkillByName(testSkillRequest);

        assertTrue(testUser.getUserSkills().size() > 0);
        verify(userRepository).getCurrentUser();
        verify(skillRepository).findSkillByName("python");
        verify(skillRepository).save(any(Skill.class));
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("addSkillByName should throw EntityNotFoundException when user does not exist")
    void shouldThrowExceptionWhenUserNotFoundInAddSkillByName() {
        when(userRepository.getCurrentUser()).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userService.addSkillByName(testSkillRequest)
        );

        assertEquals("User Not found", exception.getMessage());
        verify(userRepository).getCurrentUser();
        verify(skillRepository, never()).findSkillByName(any());
        verify(skillRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("addSkillByName should normalize skill name with spaces and mixed case")
    void shouldNormalizeSkillNameWithSpacesAndMixedCase() {
        SkillRequest skillRequestWithSpaces = new SkillRequest();
        skillRequestWithSpaces.setSkillName("Java Programming");
        skillRequestWithSpaces.setSkillCategory(SkillCategory.BACKEND_DEVELOPMENT);

        Skill javaSkill = Skill.builder()
                .id(3L)
                .name("Java Programming")
                .category(SkillCategory.BACKEND_DEVELOPMENT)
                .build();

        when(userRepository.getCurrentUser()).thenReturn(Optional.of(testUser));
        // Normalized: "Java Programming" -> "javaprogramming"
        when(skillRepository.findSkillByName("javaprogramming")).thenReturn(Optional.of(javaSkill));
        when(userRepository.save(testUser)).thenReturn(testUser);

        userService.addSkillByName(skillRequestWithSpaces);

        assertTrue(testUser.getUserSkills().contains(javaSkill));
        verify(skillRepository).findSkillByName("javaprogramming");
    }

    @Test
    @DisplayName("addSkillByName should handle null skill name gracefully")
    void shouldHandleNullSkillName() {
        SkillRequest skillRequestWithNull = new SkillRequest();
        skillRequestWithNull.setSkillName(null);
        skillRequestWithNull.setSkillCategory(SkillCategory.BACKEND_DEVELOPMENT);

        when(userRepository.getCurrentUser()).thenReturn(Optional.of(testUser));
        // Normalized: null -> null
        when(skillRepository.findSkillByName(null)).thenReturn(Optional.empty());
        when(skillRepository.save(any(Skill.class))).thenAnswer(invocation -> {
            Skill skill = invocation.getArgument(0);
            skill.setId(4L);
            return skill;
        });
        when(userRepository.save(testUser)).thenReturn(testUser);

        userService.addSkillByName(skillRequestWithNull);

        assertTrue(testUser.getUserSkills().size() > 0);
        verify(skillRepository).findSkillByName(null);
        verify(skillRepository).save(any(Skill.class));
    }

    @Test
    @DisplayName("removeSkillFromUser should remove skill from user successfully")
    void shouldRemoveSkillFromUserSuccessfully() {
        testUser.getUserSkills().add(testSkill);
        when(userRepository.getCurrentUser()).thenReturn(Optional.of(testUser));
        when(skillRepository.findById(1L)).thenReturn(Optional.of(testSkill));
        when(userRepository.save(testUser)).thenReturn(testUser);

        userService.removeSkillFromUser(1L);

        assertFalse(testUser.getUserSkills().contains(testSkill));
        verify(userRepository).getCurrentUser();
        verify(skillRepository).findById(1L);
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("removeSkillFromUser should throw EntityNotFoundException when user does not exist")
    void shouldThrowExceptionWhenUserNotFoundInRemoveSkillFromUser() {
        when(userRepository.getCurrentUser()).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userService.removeSkillFromUser(1L)
        );

        assertEquals("User Not found", exception.getMessage());
        verify(userRepository).getCurrentUser();
        verify(skillRepository, never()).findById(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("removeSkillFromUser should throw IllegalArgumentException when skill does not exist")
    void shouldThrowExceptionWhenSkillNotFoundInRemoveSkillFromUser() {
        when(userRepository.getCurrentUser()).thenReturn(Optional.of(testUser));
        when(skillRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.removeSkillFromUser(999L)
        );

        assertEquals("Skill Not found", exception.getMessage());
        verify(userRepository).getCurrentUser();
        verify(skillRepository).findById(999L);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("getUserSkills should return set of SkillResponse when user has skills")
    void shouldGetUserSkillsSuccessfully() {
        Skill anotherSkill = Skill.builder()
                .id(2L)
                .name("React")
                .category(SkillCategory.FRONTEND_DEVELOPMENT)
                .build();

        SkillResponse anotherSkillResponse = SkillResponse.builder()
                .skillId(2L)
                .skillName("React")
                .skillCategory(SkillCategory.FRONTEND_DEVELOPMENT)
                .build();

        testUser.getUserSkills().add(testSkill);
        testUser.getUserSkills().add(anotherSkill);

        when(userRepository.getCurrentUser()).thenReturn(Optional.of(testUser));
        when(skillMapper.toSkillResponse(testSkill)).thenReturn(testSkillResponse);
        when(skillMapper.toSkillResponse(anotherSkill)).thenReturn(anotherSkillResponse);

        Set<SkillResponse> result = userService.getUserSkills();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(testSkillResponse));
        assertTrue(result.contains(anotherSkillResponse));
        verify(userRepository).getCurrentUser();
        verify(skillMapper).toSkillResponse(testSkill);
        verify(skillMapper).toSkillResponse(anotherSkill);
    }

    @Test
    @DisplayName("getUserSkills should return empty set when user has no skills")
    void shouldGetEmptyUserSkills() {
        when(userRepository.getCurrentUser()).thenReturn(Optional.of(testUser));

        Set<SkillResponse> result = userService.getUserSkills();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository).getCurrentUser();
        verify(skillMapper, never()).toSkillResponse(any());
    }

    @Test
    @DisplayName("getUserSkills should throw EntityNotFoundException when user does not exist")
    void shouldThrowExceptionWhenUserNotFoundInGetUserSkills() {
        when(userRepository.getCurrentUser()).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userService.getUserSkills()
        );

        assertEquals("User Not found", exception.getMessage());
        verify(userRepository).getCurrentUser();
        verify(skillMapper, never()).toSkillResponse(any());
    }
}

