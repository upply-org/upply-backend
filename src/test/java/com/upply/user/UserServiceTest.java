package com.upply.user;

import com.upply.profile.experience.*;
import com.upply.profile.experience.dto.ExperienceMapper;
import com.upply.profile.experience.dto.ExperienceRequest;
import com.upply.profile.experience.dto.ExperienceResponse;
import com.upply.profile.project.*;
import com.upply.profile.project.dto.ProjectMapper;
import com.upply.profile.project.dto.ProjectRequest;
import com.upply.profile.project.dto.ProjectResponse;
import com.upply.profile.skill.*;
import com.upply.profile.skill.dto.SkillMapper;
import com.upply.profile.skill.dto.SkillRequest;
import com.upply.profile.skill.dto.SkillResponse;
import com.upply.profile.socialLink.*;
import com.upply.profile.socialLink.dto.SocialLinkMapper;
import com.upply.profile.socialLink.dto.SocialLinkRequest;
import com.upply.profile.socialLink.dto.SocialLinkResponse;
import com.upply.user.dto.UserMapper;
import com.upply.user.dto.UserRequest;
import com.upply.user.dto.UserResponse;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

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

    @Mock
    private ExperienceRepository experienceRepository;

    @Mock
    private ExperienceMapper experienceMapper;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private SocialLinkRepository socialLinkRepository;

    @Mock
    private SocialLinkMapper socialLinkMapper;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserRequest testUserRequest;
    private UserResponse testUserResponse;
    private Skill testSkill;
    private SkillResponse testSkillResponse;
    private SkillRequest testSkillRequest;
    private Experience testExperience;
    private ExperienceRequest testExperienceRequest;
    private ExperienceResponse testExperienceResponse;
    private Project testProject;
    private ProjectRequest testProjectRequest;
    private ProjectResponse testProjectResponse;
    private SocialLink testSocialLink;
    private SocialLinkRequest testSocialLinkRequest;
    private SocialLinkResponse testSocialLinkResponse;

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
                .build();

        testSkillResponse = SkillResponse.builder()
                .skillId(1L)
                .skillName("Java Programming")
                .build();

        testSkillRequest = new SkillRequest();
        testSkillRequest.setSkillName("Python");

        Date startDate = new Date(System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000); // 1 year ago
        Date endDate = new Date();

        testExperienceRequest = new ExperienceRequest(
                "Software Engineer",
                "Tech Company",
                startDate,
                endDate,
                "Developed web applications"
        );

        testExperience = Experience.builder()
                .id(1L)
                .title("Software Engineer")
                .organization("Tech Company")
                .startDate(startDate)
                .endDate(endDate)
                .description("Developed web applications")
                .user(testUser)
                .build();

        testExperienceResponse = ExperienceResponse.builder()
                .id(1L)
                .title("Software Engineer")
                .organization("Tech Company")
                .startDate(startDate)
                .endDate(endDate)
                .description("Developed web applications")
                .build();

        testProjectRequest = new ProjectRequest(
                "E-Commerce Platform",
                "A full-stack e-commerce application",
                "https://github.com/user/ecommerce",
                startDate,
                endDate,
                "Java, Spring Boot, React"
        );

        testProject = Project.builder()
                .id(1L)
                .title("E-Commerce Platform")
                .description("A full-stack e-commerce application")
                .projectUrl("https://github.com/user/ecommerce")
                .startDate(startDate)
                .endDate(endDate)
                .technologies("Java, Spring Boot, React")
                .user(testUser)
                .build();

        testProjectResponse = ProjectResponse.builder()
                .id(1L)
                .title("E-Commerce Platform")
                .description("A full-stack e-commerce application")
                .projectUrl("https://github.com/user/ecommerce")
                .startDate(startDate)
                .endDate(endDate)
                .technologies("Java, Spring Boot, React")
                .build();

        testSocialLinkRequest = new SocialLinkRequest(
                "https://github.com/user",
                SocialType.GITHUB
        );

        testSocialLink = SocialLink.builder()
                .id(1L)
                .url("https://github.com/user")
                .socialType(SocialType.GITHUB)
                .user(testUser)
                .build();

        testSocialLinkResponse = SocialLinkResponse.builder()
                .id(1L)
                .url("https://github.com/user")
                .socialType(SocialType.GITHUB)
                .build();
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

        Skill javaSkill = Skill.builder()
                .id(3L)
                .name("Java Programming")
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
                .build();

        SkillResponse anotherSkillResponse = SkillResponse.builder()
                .skillId(2L)
                .skillName("React")
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

    // Experience tests

    @Test
    @DisplayName("getUserExperience should return list of ExperienceResponse when user has experiences")
    void shouldGetUserExperienceSuccessfully() {
        List<Experience> experiences = Arrays.asList(testExperience);
        when(experienceRepository.findUserExperienceByUserId()).thenReturn(experiences);
        when(experienceMapper.toExperienceResponse(testExperience)).thenReturn(testExperienceResponse);

        List<ExperienceResponse> result = userService.getUserExperience();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testExperienceResponse, result.get(0));
        verify(experienceRepository).findUserExperienceByUserId();
        verify(experienceMapper).toExperienceResponse(testExperience);
    }

    @Test
    @DisplayName("getUserExperience should return empty list when user has no experiences")
    void shouldGetEmptyUserExperience() {
        when(experienceRepository.findUserExperienceByUserId()).thenReturn(Collections.emptyList());

        List<ExperienceResponse> result = userService.getUserExperience();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(experienceRepository).findUserExperienceByUserId();
        verify(experienceMapper, never()).toExperienceResponse(any());
    }

    @Test
    @DisplayName("getUserExperience should return multiple experiences")
    void shouldGetMultipleUserExperiences() {
        Date anotherStartDate = new Date(System.currentTimeMillis() - 730L * 24 * 60 * 60 * 1000); // 2 years ago
        Date anotherEndDate = new Date(System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000); // 1 year ago

        Experience anotherExperience = Experience.builder()
                .id(2L)
                .title("Junior Developer")
                .organization("Startup Inc")
                .startDate(anotherStartDate)
                .endDate(anotherEndDate)
                .description("Worked on mobile apps")
                .user(testUser)
                .build();

        ExperienceResponse anotherExperienceResponse = ExperienceResponse.builder()
                .id(2L)
                .title("Junior Developer")
                .organization("Startup Inc")
                .startDate(anotherStartDate)
                .endDate(anotherEndDate)
                .description("Worked on mobile apps")
                .build();

        List<Experience> experiences = Arrays.asList(testExperience, anotherExperience);
        when(experienceRepository.findUserExperienceByUserId()).thenReturn(experiences);
        when(experienceMapper.toExperienceResponse(testExperience)).thenReturn(testExperienceResponse);
        when(experienceMapper.toExperienceResponse(anotherExperience)).thenReturn(anotherExperienceResponse);

        List<ExperienceResponse> result = userService.getUserExperience();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(experienceRepository).findUserExperienceByUserId();
        verify(experienceMapper).toExperienceResponse(testExperience);
        verify(experienceMapper).toExperienceResponse(anotherExperience);
    }

    @Test
    @DisplayName("addUserExperience should add experience and return experience id")
    void shouldAddUserExperienceSuccessfully() {
        Experience newExperience = Experience.builder()
                .title("Software Engineer")
                .organization("Tech Company")
                .startDate(testExperienceRequest.startDate())
                .endDate(testExperienceRequest.endDate())
                .description("Developed web applications")
                .build();

        when(userRepository.getCurrentUser()).thenReturn(Optional.of(testUser));
        when(experienceMapper.toExperience(testExperienceRequest)).thenReturn(newExperience);
        when(experienceRepository.save(any(Experience.class))).thenAnswer(invocation -> {
            Experience exp = invocation.getArgument(0);
            exp.setId(1L);
            return exp;
        });

        Long result = userService.addUserExperience(testExperienceRequest);

        assertNotNull(result);
        assertEquals(1L, result);
        verify(userRepository).getCurrentUser();
        verify(experienceMapper).toExperience(testExperienceRequest);
        verify(experienceRepository).save(any(Experience.class));
    }

    @Test
    @DisplayName("addUserExperience should throw EntityNotFoundException when user does not exist")
    void shouldThrowExceptionWhenUserNotFoundInAddUserExperience() {
        when(userRepository.getCurrentUser()).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userService.addUserExperience(testExperienceRequest)
        );

        assertEquals("User Not found", exception.getMessage());
        verify(userRepository).getCurrentUser();
        verify(experienceMapper, never()).toExperience(any());
        verify(experienceRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateUserExperience should update experience fields successfully")
    void shouldUpdateUserExperienceSuccessfully() {
        Date newStartDate = new Date(System.currentTimeMillis() - 730L * 24 * 60 * 60 * 1000); // 2 years ago
        Date newEndDate = new Date(System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000); // 1 year ago

        ExperienceRequest updateRequest = new ExperienceRequest(
                "Senior Software Engineer",
                "New Tech Company",
                newStartDate,
                newEndDate,
                "Led development team"
        );

        when(experienceRepository.findExperienceById(1L)).thenReturn(Optional.of(testExperience));
        when(experienceRepository.save(testExperience)).thenReturn(testExperience);

        userService.updateUserExperience(1L, updateRequest);

        verify(experienceRepository).findExperienceById(1L);
        verify(experienceRepository).save(testExperience);
        assertEquals("Senior Software Engineer", testExperience.getTitle());
        assertEquals("New Tech Company", testExperience.getOrganization());
        assertEquals(newStartDate, testExperience.getStartDate());
        assertEquals(newEndDate, testExperience.getEndDate());
        assertEquals("Led development team", testExperience.getDescription());
    }

    @Test
    @DisplayName("updateUserExperience should throw EntityNotFoundException when experience does not exist")
    void shouldThrowExceptionWhenExperienceNotFoundInUpdateUserExperience() {
        when(experienceRepository.findExperienceById(999L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userService.updateUserExperience(999L, testExperienceRequest)
        );

        assertEquals("There is no experience with this id", exception.getMessage());
        verify(experienceRepository).findExperienceById(999L);
        verify(experienceRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteUserExperience should delete experience successfully")
    void shouldDeleteUserExperienceSuccessfully() {
        doNothing().when(experienceRepository).deleteExperienceById(1L);

        userService.deleteUserExperience(1L);

        verify(experienceRepository).deleteExperienceById(1L);
    }

    @Test
    @DisplayName("deleteUserExperience should handle deletion of non-existent experience")
    void shouldHandleDeleteNonExistentExperience() {
        doNothing().when(experienceRepository).deleteExperienceById(999L);

        userService.deleteUserExperience(999L);

        verify(experienceRepository).deleteExperienceById(999L);
    }

    // Project tests

    @Test
    @DisplayName("getUserProject should return list of ProjectResponse when user has projects")
    void shouldGetUserProjectSuccessfully() {
        List<Project> projects = Arrays.asList(testProject);
        when(projectRepository.findUserProjectsByUserId()).thenReturn(projects);
        when(projectMapper.toProjectResponse(testProject)).thenReturn(testProjectResponse);

        List<ProjectResponse> result = userService.getUserProject();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testProjectResponse, result.get(0));
        verify(projectRepository).findUserProjectsByUserId();
        verify(projectMapper).toProjectResponse(testProject);
    }

    @Test
    @DisplayName("getUserProject should return empty list when user has no projects")
    void shouldGetEmptyUserProject() {
        when(projectRepository.findUserProjectsByUserId()).thenReturn(Collections.emptyList());

        List<ProjectResponse> result = userService.getUserProject();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(projectRepository).findUserProjectsByUserId();
        verify(projectMapper, never()).toProjectResponse(any());
    }

    @Test
    @DisplayName("getUserProjectById should return ProjectResponse when project exists")
    void shouldGetUserProjectByIdSuccessfully() {
        when(projectRepository.findProjectById(1L)).thenReturn(Optional.of(testProject));
        when(projectMapper.toProjectResponse(testProject)).thenReturn(testProjectResponse);

        ProjectResponse result = userService.getUserProjectById(1L);

        assertNotNull(result);
        assertEquals(testProjectResponse, result);
        assertEquals(1L, result.id());
        assertEquals("E-Commerce Platform", result.title());
        verify(projectRepository).findProjectById(1L);
        verify(projectMapper).toProjectResponse(testProject);
    }

    @Test
    @DisplayName("getUserProjectById should throw EntityNotFoundException when project does not exist")
    void shouldThrowExceptionWhenProjectNotFoundInGetUserProjectById() {
        when(projectRepository.findProjectById(999L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userService.getUserProjectById(999L)
        );

        assertEquals("There is no project with this id", exception.getMessage());
        verify(projectRepository).findProjectById(999L);
        verify(projectMapper, never()).toProjectResponse(any());
    }

    @Test
    @DisplayName("addUserProject should add project and return project id")
    void shouldAddUserProjectSuccessfully() {
        Project newProject = Project.builder()
                .title("E-Commerce Platform")
                .description("A full-stack e-commerce application")
                .projectUrl("https://github.com/user/ecommerce")
                .startDate(testProjectRequest.startDate())
                .endDate(testProjectRequest.endDate())
                .technologies("Java, Spring Boot, React")
                .build();

        when(userRepository.getCurrentUser()).thenReturn(Optional.of(testUser));
        when(projectMapper.toProject(testProjectRequest)).thenReturn(newProject);
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project proj = invocation.getArgument(0);
            proj.setId(1L);
            return proj;
        });

        Long result = userService.addUserProject(testProjectRequest);

        assertNotNull(result);
        assertEquals(1L, result);
        verify(userRepository).getCurrentUser();
        verify(projectMapper).toProject(testProjectRequest);
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    @DisplayName("addUserProject should throw EntityNotFoundException when user does not exist")
    void shouldThrowExceptionWhenUserNotFoundInAddUserProject() {
        when(userRepository.getCurrentUser()).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userService.addUserProject(testProjectRequest)
        );

        assertEquals("User Not Found", exception.getMessage());
        verify(userRepository).getCurrentUser();
        verify(projectMapper, never()).toProject(any());
        verify(projectRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateUserProject should update project fields successfully")
    void shouldUpdateUserProjectSuccessfully() {
        Date newStartDate = new Date(System.currentTimeMillis() - 730L * 24 * 60 * 60 * 1000); // 2 years ago
        Date newEndDate = new Date(System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000); // 1 year ago

        ProjectRequest updateRequest = new ProjectRequest(
                "Updated E-Commerce Platform",
                "An updated full-stack e-commerce application",
                "https://github.com/user/ecommerce-v2",
                newStartDate,
                newEndDate,
                "Java, Spring Boot, React, TypeScript"
        );

        when(projectRepository.findProjectById(1L)).thenReturn(Optional.of(testProject));
        when(projectRepository.save(testProject)).thenReturn(testProject);

        userService.updateUserProject(1L, updateRequest);

        verify(projectRepository).findProjectById(1L);
        verify(projectRepository).save(testProject);
        assertEquals("Updated E-Commerce Platform", testProject.getTitle());
        assertEquals("An updated full-stack e-commerce application", testProject.getDescription());
        assertEquals("https://github.com/user/ecommerce-v2", testProject.getProjectUrl());
        assertEquals(newStartDate, testProject.getStartDate());
        assertEquals(newEndDate, testProject.getEndDate());
        assertEquals("Java, Spring Boot, React, TypeScript", testProject.getTechnologies());
    }

    @Test
    @DisplayName("updateUserProject should throw EntityNotFoundException when project does not exist")
    void shouldThrowExceptionWhenProjectNotFoundInUpdateUserProject() {
        when(projectRepository.findProjectById(999L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userService.updateUserProject(999L, testProjectRequest)
        );

        assertEquals("There is no project with this id", exception.getMessage());
        verify(projectRepository).findProjectById(999L);
        verify(projectRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteUserProject should delete project successfully")
    void shouldDeleteUserProjectSuccessfully() {
        doNothing().when(projectRepository).deleteProjectById(1L);

        userService.deleteUserProject(1L);

        verify(projectRepository).deleteProjectById(1L);
    }

    @Test
    @DisplayName("deleteUserProject should handle deletion of non-existent project")
    void shouldHandleDeleteNonExistentProject() {
        doNothing().when(projectRepository).deleteProjectById(999L);

        userService.deleteUserProject(999L);

        verify(projectRepository).deleteProjectById(999L);
    }

    // Social Link tests

    @Test
    @DisplayName("getUserSocialLinks should return list of SocialLinkResponse when user has social links")
    void shouldGetUserSocialLinksSuccessfully() {
        List<SocialLink> socialLinks = Arrays.asList(testSocialLink);
        when(socialLinkRepository.findUserSocialLinksByUserId()).thenReturn(socialLinks);
        when(socialLinkMapper.toSocialLinkResponse(testSocialLink)).thenReturn(testSocialLinkResponse);

        List<SocialLinkResponse> result = userService.getUserSocialLinks();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testSocialLinkResponse, result.get(0));
        verify(socialLinkRepository).findUserSocialLinksByUserId();
        verify(socialLinkMapper).toSocialLinkResponse(testSocialLink);
    }

    @Test
    @DisplayName("getUserSocialLinks should return empty list when user has no social links")
    void shouldGetEmptyUserSocialLinks() {
        when(socialLinkRepository.findUserSocialLinksByUserId()).thenReturn(Collections.emptyList());

        List<SocialLinkResponse> result = userService.getUserSocialLinks();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(socialLinkRepository).findUserSocialLinksByUserId();
        verify(socialLinkMapper, never()).toSocialLinkResponse(any());
    }

    @Test
    @DisplayName("getUserSocialLinkById should return SocialLinkResponse when social link exists")
    void shouldGetUserSocialLinkByIdSuccessfully() {
        when(socialLinkRepository.findSocialLinkById(1L)).thenReturn(Optional.of(testSocialLink));
        when(socialLinkMapper.toSocialLinkResponse(testSocialLink)).thenReturn(testSocialLinkResponse);

        SocialLinkResponse result = userService.getUserSocialLinkById(1L);

        assertNotNull(result);
        assertEquals(testSocialLinkResponse, result);
        assertEquals(1L, result.id());
        assertEquals("https://github.com/user", result.url());
        assertEquals(SocialType.GITHUB, result.socialType());
        verify(socialLinkRepository).findSocialLinkById(1L);
        verify(socialLinkMapper).toSocialLinkResponse(testSocialLink);
    }

    @Test
    @DisplayName("getUserSocialLinkById should throw EntityNotFoundException when social link does not exist")
    void shouldThrowExceptionWhenSocialLinkNotFoundInGetUserSocialLinkById() {
        when(socialLinkRepository.findSocialLinkById(999L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userService.getUserSocialLinkById(999L)
        );

        assertEquals("There is no social links with this id", exception.getMessage());
        verify(socialLinkRepository).findSocialLinkById(999L);
        verify(socialLinkMapper, never()).toSocialLinkResponse(any());
    }

    @Test
    @DisplayName("addUserSocialLinks should add social link and return social link id")
    void shouldAddUserSocialLinksSuccessfully() {
        SocialLink newSocialLink = SocialLink.builder()
                .url("https://github.com/user")
                .socialType(SocialType.GITHUB)
                .build();

        when(userRepository.getCurrentUser()).thenReturn(Optional.of(testUser));
        when(socialLinkMapper.toSocialLink(testSocialLinkRequest)).thenReturn(newSocialLink);
        when(socialLinkRepository.save(any(SocialLink.class))).thenAnswer(invocation -> {
            SocialLink link = invocation.getArgument(0);
            link.setId(1L);
            return link;
        });

        Long result = userService.addUserSocialLinks(testSocialLinkRequest);

        assertNotNull(result);
        assertEquals(1L, result);
        verify(userRepository).getCurrentUser();
        verify(socialLinkMapper).toSocialLink(testSocialLinkRequest);
        verify(socialLinkRepository).save(any(SocialLink.class));
    }

    @Test
    @DisplayName("addUserSocialLinks should throw EntityNotFoundException when user does not exist")
    void shouldThrowExceptionWhenUserNotFoundInAddUserSocialLinks() {
        when(userRepository.getCurrentUser()).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userService.addUserSocialLinks(testSocialLinkRequest)
        );

        assertEquals("User Not Found", exception.getMessage());
        verify(userRepository).getCurrentUser();
        verify(socialLinkMapper, never()).toSocialLink(any());
        verify(socialLinkRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateUserSocialLinks should update social link fields successfully")
    void shouldUpdateUserSocialLinksSuccessfully() {
        SocialLinkRequest updateRequest = new SocialLinkRequest(
                "https://linkedin.com/in/user",
                SocialType.LINKEDIN
        );

        when(socialLinkRepository.findSocialLinkById(1L)).thenReturn(Optional.of(testSocialLink));
        when(socialLinkRepository.save(testSocialLink)).thenReturn(testSocialLink);

        userService.updateUserSocialLinks(1L, updateRequest);

        verify(socialLinkRepository).findSocialLinkById(1L);
        verify(socialLinkRepository).save(testSocialLink);
        assertEquals("https://linkedin.com/in/user", testSocialLink.getUrl());
        assertEquals(SocialType.LINKEDIN, testSocialLink.getSocialType());
    }

    @Test
    @DisplayName("updateUserSocialLinks should throw EntityNotFoundException when social link does not exist")
    void shouldThrowExceptionWhenSocialLinkNotFoundInUpdateUserSocialLinks() {
        when(socialLinkRepository.findSocialLinkById(999L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userService.updateUserSocialLinks(999L, testSocialLinkRequest)
        );

        assertEquals("There is no social links with this id", exception.getMessage());
        verify(socialLinkRepository).findSocialLinkById(999L);
        verify(socialLinkRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteUserLinks should delete social link successfully")
    void shouldDeleteUserLinksSuccessfully() {
        doNothing().when(socialLinkRepository).deleteSocialLinkById(1L);

        userService.deleteUserLinks(1L);

        verify(socialLinkRepository).deleteSocialLinkById(1L);
    }

    @Test
    @DisplayName("deleteUserLinks should handle deletion of non-existent social link")
    void shouldHandleDeleteNonExistentSocialLink() {
        doNothing().when(socialLinkRepository).deleteSocialLinkById(999L);

        userService.deleteUserLinks(999L);

        verify(socialLinkRepository).deleteSocialLinkById(999L);
    }
}

