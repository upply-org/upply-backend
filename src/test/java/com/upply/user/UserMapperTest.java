package com.upply.user;

import com.upply.experience.*;
import com.upply.project.*;
import com.upply.skill.*;
import com.upply.socialLink.*;
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
@DisplayName("UserMapper unit tests")
class UserMapperTest {

    @Mock
    private SkillMapper skillMapper;

    @Mock
    private ExperienceMapper experienceMapper;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private SocialLinkMapper socialLinkMapper;

    @InjectMocks
    private UserMapper userMapper;

    private User testUser;
    private Skill testSkill1;
    private Skill testSkill2;
    private SkillResponse testSkillResponse1;
    private SkillResponse testSkillResponse2;
    private Experience testExperience1;
    private Experience testExperience2;
    private ExperienceResponse testExperienceResponse1;
    private ExperienceResponse testExperienceResponse2;
    private Project testProject1;
    private Project testProject2;
    private ProjectResponse testProjectResponse1;
    private ProjectResponse testProjectResponse2;
    private SocialLink testSocialLink1;
    private SocialLink testSocialLink2;
    private SocialLinkResponse testSocialLinkResponse1;
    private SocialLinkResponse testSocialLinkResponse2;

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

        Date startDate1 = new Date(System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000); // 1 year ago
        Date endDate1 = new Date();
        Date startDate2 = new Date(System.currentTimeMillis() - 730L * 24 * 60 * 60 * 1000); // 2 years ago
        Date endDate2 = new Date(System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000); // 1 year ago

        testExperience1 = Experience.builder()
                .id(1L)
                .title("Software Engineer")
                .organization("Tech Company")
                .startDate(startDate1)
                .endDate(endDate1)
                .description("Developed web applications")
                .build();

        testExperience2 = Experience.builder()
                .id(2L)
                .title("Junior Developer")
                .organization("Startup Inc")
                .startDate(startDate2)
                .endDate(endDate2)
                .description("Worked on mobile apps")
                .build();

        testExperienceResponse1 = ExperienceResponse.builder()
                .id(1L)
                .title("Software Engineer")
                .organization("Tech Company")
                .startDate(startDate1)
                .endDate(endDate1)
                .description("Developed web applications")
                .build();

        testExperienceResponse2 = ExperienceResponse.builder()
                .id(2L)
                .title("Junior Developer")
                .organization("Startup Inc")
                .startDate(startDate2)
                .endDate(endDate2)
                .description("Worked on mobile apps")
                .build();

        testProject1 = Project.builder()
                .id(1L)
                .title("E-Commerce Platform")
                .description("A full-stack e-commerce application")
                .projectUrl("https://github.com/user/ecommerce")
                .startDate(startDate1)
                .endDate(endDate1)
                .technologies("Java, Spring Boot, React")
                .build();

        testProject2 = Project.builder()
                .id(2L)
                .title("Task Management App")
                .description("A task management application")
                .projectUrl("https://github.com/user/taskapp")
                .startDate(startDate2)
                .endDate(endDate2)
                .technologies("Python, Django, Vue.js")
                .build();

        testProjectResponse1 = ProjectResponse.builder()
                .Id(1L)
                .title("E-Commerce Platform")
                .description("A full-stack e-commerce application")
                .projectUrl("https://github.com/user/ecommerce")
                .startDate(startDate1)
                .endDate(endDate1)
                .technologies("Java, Spring Boot, React")
                .build();

        testProjectResponse2 = ProjectResponse.builder()
                .Id(2L)
                .title("Task Management App")
                .description("A task management application")
                .projectUrl("https://github.com/user/taskapp")
                .startDate(startDate2)
                .endDate(endDate2)
                .technologies("Python, Django, Vue.js")
                .build();

        testSocialLink1 = SocialLink.builder()
                .id(1L)
                .url("https://github.com/user")
                .socialType(SocialType.GITHUB)
                .build();

        testSocialLink2 = SocialLink.builder()
                .id(2L)
                .url("https://linkedin.com/in/user")
                .socialType(SocialType.LINKEDIN)
                .build();

        testSocialLinkResponse1 = SocialLinkResponse.builder()
                .id(1L)
                .url("https://github.com/user")
                .socialType(SocialType.GITHUB)
                .build();

        testSocialLinkResponse2 = SocialLinkResponse.builder()
                .id(2L)
                .url("https://linkedin.com/in/user")
                .socialType(SocialType.LINKEDIN)
                .build();

        Set<Skill> skills = new HashSet<>();
        skills.add(testSkill1);
        skills.add(testSkill2);

        List<Experience> experiences = new ArrayList<>();
        experiences.add(testExperience1);
        experiences.add(testExperience2);

        List<Project> projects = new ArrayList<>();
        projects.add(testProject1);
        projects.add(testProject2);

        List<SocialLink> socialLinks = new ArrayList<>();
        socialLinks.add(testSocialLink1);
        socialLinks.add(testSocialLink2);

        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .university("Test University")
                .userSkills(skills)
                .experiences(experiences)
                .projects(projects)
                .socialLinks(socialLinks)
                .build();
    }

    @Test
    @DisplayName("toUserResponse should map User to UserResponse with all fields")
    void shouldMapUserToUserResponseWithAllFields() {
        when(skillMapper.toSkillResponse(testSkill1)).thenReturn(testSkillResponse1);
        when(skillMapper.toSkillResponse(testSkill2)).thenReturn(testSkillResponse2);
        when(experienceMapper.toExperienceResponse(testExperience1)).thenReturn(testExperienceResponse1);
        when(experienceMapper.toExperienceResponse(testExperience2)).thenReturn(testExperienceResponse2);
        when(projectMapper.toProjectResponse(testProject1)).thenReturn(testProjectResponse1);
        when(projectMapper.toProjectResponse(testProject2)).thenReturn(testProjectResponse2);
        when(socialLinkMapper.toSocialLinkResponse(testSocialLink1)).thenReturn(testSocialLinkResponse1);
        when(socialLinkMapper.toSocialLinkResponse(testSocialLink2)).thenReturn(testSocialLinkResponse2);

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
        assertNotNull(result.getExperiences());
        assertEquals(2, result.getExperiences().size());
        assertTrue(result.getExperiences().contains(testExperienceResponse1));
        assertTrue(result.getExperiences().contains(testExperienceResponse2));
        assertNotNull(result.getProjects());
        assertEquals(2, result.getProjects().size());
        assertTrue(result.getProjects().contains(testProjectResponse1));
        assertTrue(result.getProjects().contains(testProjectResponse2));
        assertNotNull(result.getSocialLinks());
        assertEquals(2, result.getSocialLinks().size());
        assertTrue(result.getSocialLinks().contains(testSocialLinkResponse1));
        assertTrue(result.getSocialLinks().contains(testSocialLinkResponse2));
        verify(skillMapper).toSkillResponse(testSkill1);
        verify(skillMapper).toSkillResponse(testSkill2);
        verify(experienceMapper).toExperienceResponse(testExperience1);
        verify(experienceMapper).toExperienceResponse(testExperience2);
        verify(projectMapper).toProjectResponse(testProject1);
        verify(projectMapper).toProjectResponse(testProject2);
        verify(socialLinkMapper).toSocialLinkResponse(testSocialLink1);
        verify(socialLinkMapper).toSocialLinkResponse(testSocialLink2);
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
                .experiences(new ArrayList<>())
                .build();

        UserResponse result = userMapper.toUserResponse(userWithoutSkills);

        assertNotNull(result);
        assertEquals("noskills@example.com", result.getEmail());
        assertEquals("Jane", result.getFirstName());
        assertEquals("Smith", result.getLastName());
        assertEquals("Another University", result.getUniversity());
        assertNotNull(result.getSkills());
        assertTrue(result.getSkills().isEmpty());
        assertNotNull(result.getExperiences());
        assertTrue(result.getExperiences().isEmpty());
        verify(skillMapper, never()).toSkillResponse(any());
        verify(experienceMapper, never()).toExperienceResponse(any());
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
                .experiences(new ArrayList<>())
                .build();

        UserResponse result = userMapper.toUserResponse(userWithNulls);

        assertNotNull(result);
        assertEquals("nulls@example.com", result.getEmail());
        assertNull(result.getFirstName());
        assertNull(result.getLastName());
        assertNull(result.getUniversity());
        assertNotNull(result.getSkills());
        assertTrue(result.getSkills().isEmpty());
        assertNotNull(result.getExperiences());
        assertTrue(result.getExperiences().isEmpty());
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
                .experiences(new ArrayList<>())
                .build();

        when(skillMapper.toSkillResponse(testSkill1)).thenReturn(testSkillResponse1);

        UserResponse result = userMapper.toUserResponse(userWithSingleSkill);

        assertNotNull(result);
        assertEquals("single@example.com", result.getEmail());
        assertEquals(1, result.getSkills().size());
        assertTrue(result.getSkills().contains(testSkillResponse1));
        assertNotNull(result.getExperiences());
        assertTrue(result.getExperiences().isEmpty());
        verify(skillMapper).toSkillResponse(testSkill1);
        verify(skillMapper, never()).toSkillResponse(testSkill2);
        verify(experienceMapper, never()).toExperienceResponse(any());
    }

    @Test
    @DisplayName("toUserResponse should map User with experiences but no skills")
    void shouldMapUserWithExperiencesButNoSkills() {
        List<Experience> experiences = new ArrayList<>();
        experiences.add(testExperience1);

        User userWithExperiences = User.builder()
                .id(5L)
                .email("experiences@example.com")
                .firstName("Experience")
                .lastName("User")
                .university("Experience University")
                .userSkills(new HashSet<>())
                .experiences(experiences)
                .build();

        when(experienceMapper.toExperienceResponse(testExperience1)).thenReturn(testExperienceResponse1);

        UserResponse result = userMapper.toUserResponse(userWithExperiences);

        assertNotNull(result);
        assertEquals("experiences@example.com", result.getEmail());
        assertEquals("Experience", result.getFirstName());
        assertEquals("User", result.getLastName());
        assertEquals("Experience University", result.getUniversity());
        assertNotNull(result.getSkills());
        assertTrue(result.getSkills().isEmpty());
        assertNotNull(result.getExperiences());
        assertEquals(1, result.getExperiences().size());
        assertTrue(result.getExperiences().contains(testExperienceResponse1));
        verify(skillMapper, never()).toSkillResponse(any());
        verify(experienceMapper).toExperienceResponse(testExperience1);
    }

    @Test
    @DisplayName("toUserResponse should map User with single experience to UserResponse")
    void shouldMapUserWithSingleExperienceToUserResponse() {
        List<Experience> singleExperience = new ArrayList<>();
        singleExperience.add(testExperience1);

        User userWithSingleExperience = User.builder()
                .id(6L)
                .email("singleexp@example.com")
                .firstName("Single")
                .lastName("Experience")
                .university("Single Experience University")
                .userSkills(new HashSet<>())
                .experiences(singleExperience)
                .build();

        when(experienceMapper.toExperienceResponse(testExperience1)).thenReturn(testExperienceResponse1);

        UserResponse result = userMapper.toUserResponse(userWithSingleExperience);

        assertNotNull(result);
        assertEquals("singleexp@example.com", result.getEmail());
        assertNotNull(result.getExperiences());
        assertEquals(1, result.getExperiences().size());
        assertTrue(result.getExperiences().contains(testExperienceResponse1));
        verify(experienceMapper).toExperienceResponse(testExperience1);
        verify(experienceMapper, never()).toExperienceResponse(testExperience2);
    }

    @Test
    @DisplayName("toUserResponse should map User with multiple experiences to UserResponse")
    void shouldMapUserWithMultipleExperiencesToUserResponse() {
        List<Experience> multipleExperiences = new ArrayList<>();
        multipleExperiences.add(testExperience1);
        multipleExperiences.add(testExperience2);

        User userWithMultipleExperiences = User.builder()
                .id(7L)
                .email("multipleexp@example.com")
                .firstName("Multiple")
                .lastName("Experiences")
                .university("Multiple Experience University")
                .userSkills(new HashSet<>())
                .experiences(multipleExperiences)
                .build();

        when(experienceMapper.toExperienceResponse(testExperience1)).thenReturn(testExperienceResponse1);
        when(experienceMapper.toExperienceResponse(testExperience2)).thenReturn(testExperienceResponse2);

        UserResponse result = userMapper.toUserResponse(userWithMultipleExperiences);

        assertNotNull(result);
        assertEquals("multipleexp@example.com", result.getEmail());
        assertNotNull(result.getExperiences());
        assertEquals(2, result.getExperiences().size());
        assertTrue(result.getExperiences().contains(testExperienceResponse1));
        assertTrue(result.getExperiences().contains(testExperienceResponse2));
        verify(experienceMapper).toExperienceResponse(testExperience1);
        verify(experienceMapper).toExperienceResponse(testExperience2);
    }

    @Test
    @DisplayName("toUserResponse should map User with both skills and experiences")
    void shouldMapUserWithBothSkillsAndExperiences() {
        Set<Skill> skills = new HashSet<>();
        skills.add(testSkill1);

        List<Experience> experiences = new ArrayList<>();
        experiences.add(testExperience1);

        User userWithBoth = User.builder()
                .id(8L)
                .email("both@example.com")
                .firstName("Both")
                .lastName("User")
                .university("Both University")
                .userSkills(skills)
                .experiences(experiences)
                .build();

        when(skillMapper.toSkillResponse(testSkill1)).thenReturn(testSkillResponse1);
        when(experienceMapper.toExperienceResponse(testExperience1)).thenReturn(testExperienceResponse1);

        UserResponse result = userMapper.toUserResponse(userWithBoth);

        assertNotNull(result);
        assertEquals("both@example.com", result.getEmail());
        assertEquals("Both", result.getFirstName());
        assertEquals("User", result.getLastName());
        assertEquals("Both University", result.getUniversity());
        assertNotNull(result.getSkills());
        assertEquals(1, result.getSkills().size());
        assertTrue(result.getSkills().contains(testSkillResponse1));
        assertNotNull(result.getExperiences());
        assertEquals(1, result.getExperiences().size());
        assertTrue(result.getExperiences().contains(testExperienceResponse1));
        verify(skillMapper).toSkillResponse(testSkill1);
        verify(experienceMapper).toExperienceResponse(testExperience1);
    }

    @Test
    @DisplayName("toUserResponse should handle User with null experiences list")
    void shouldHandleUserWithNullExperiences() {
        User userWithNullExperiences = User.builder()
                .id(9L)
                .email("nullexp@example.com")
                .firstName("Null")
                .lastName("Experiences")
                .university("Null Experience University")
                .userSkills(new HashSet<>())
                .experiences(null)
                .projects(null)
                .socialLinks(null)
                .build();

        UserResponse result = userMapper.toUserResponse(userWithNullExperiences);

        assertNotNull(result);
        assertEquals("nullexp@example.com", result.getEmail());
        assertEquals("Null", result.getFirstName());
        assertEquals("Experiences", result.getLastName());
        assertEquals("Null Experience University", result.getUniversity());
        assertNotNull(result.getSkills());
        assertTrue(result.getSkills().isEmpty());
        assertNotNull(result.getExperiences());
        assertTrue(result.getExperiences().isEmpty());
        assertNotNull(result.getProjects());
        assertTrue(result.getProjects().isEmpty());
        assertNotNull(result.getSocialLinks());
        assertTrue(result.getSocialLinks().isEmpty());
        verify(experienceMapper, never()).toExperienceResponse(any());
        verify(projectMapper, never()).toProjectResponse(any());
        verify(socialLinkMapper, never()).toSocialLinkResponse(any());
    }

    @Test
    @DisplayName("toUserResponse should map User with projects but no experiences")
    void shouldMapUserWithProjectsButNoExperiences() {
        List<Project> projects = new ArrayList<>();
        projects.add(testProject1);

        User userWithProjects = User.builder()
                .id(10L)
                .email("projects@example.com")
                .firstName("Project")
                .lastName("User")
                .university("Project University")
                .userSkills(new HashSet<>())
                .experiences(new ArrayList<>())
                .projects(projects)
                .socialLinks(new ArrayList<>())
                .build();

        when(projectMapper.toProjectResponse(testProject1)).thenReturn(testProjectResponse1);

        UserResponse result = userMapper.toUserResponse(userWithProjects);

        assertNotNull(result);
        assertEquals("projects@example.com", result.getEmail());
        assertEquals("Project", result.getFirstName());
        assertEquals("User", result.getLastName());
        assertEquals("Project University", result.getUniversity());
        assertNotNull(result.getProjects());
        assertEquals(1, result.getProjects().size());
        assertTrue(result.getProjects().contains(testProjectResponse1));
        assertNotNull(result.getExperiences());
        assertTrue(result.getExperiences().isEmpty());
        verify(projectMapper).toProjectResponse(testProject1);
        verify(experienceMapper, never()).toExperienceResponse(any());
    }

    @Test
    @DisplayName("toUserResponse should map User with social links but no projects")
    void shouldMapUserWithSocialLinksButNoProjects() {
        List<SocialLink> socialLinks = new ArrayList<>();
        socialLinks.add(testSocialLink1);

        User userWithSocialLinks = User.builder()
                .id(11L)
                .email("social@example.com")
                .firstName("Social")
                .lastName("User")
                .university("Social University")
                .userSkills(new HashSet<>())
                .experiences(new ArrayList<>())
                .projects(new ArrayList<>())
                .socialLinks(socialLinks)
                .build();

        when(socialLinkMapper.toSocialLinkResponse(testSocialLink1)).thenReturn(testSocialLinkResponse1);

        UserResponse result = userMapper.toUserResponse(userWithSocialLinks);

        assertNotNull(result);
        assertEquals("social@example.com", result.getEmail());
        assertEquals("Social", result.getFirstName());
        assertEquals("User", result.getLastName());
        assertEquals("Social University", result.getUniversity());
        assertNotNull(result.getSocialLinks());
        assertEquals(1, result.getSocialLinks().size());
        assertTrue(result.getSocialLinks().contains(testSocialLinkResponse1));
        assertNotNull(result.getProjects());
        assertTrue(result.getProjects().isEmpty());
        verify(socialLinkMapper).toSocialLinkResponse(testSocialLink1);
        verify(projectMapper, never()).toProjectResponse(any());
    }

    @Test
    @DisplayName("toUserResponse should handle User with null projects list")
    void shouldHandleUserWithNullProjects() {
        User userWithNullProjects = User.builder()
                .id(12L)
                .email("nullproj@example.com")
                .firstName("Null")
                .lastName("Projects")
                .university("Null Project University")
                .userSkills(new HashSet<>())
                .experiences(new ArrayList<>())
                .projects(null)
                .socialLinks(new ArrayList<>())
                .build();

        UserResponse result = userMapper.toUserResponse(userWithNullProjects);

        assertNotNull(result);
        assertEquals("nullproj@example.com", result.getEmail());
        assertNotNull(result.getProjects());
        assertTrue(result.getProjects().isEmpty());
        verify(projectMapper, never()).toProjectResponse(any());
    }

    @Test
    @DisplayName("toUserResponse should handle User with null social links list")
    void shouldHandleUserWithNullSocialLinks() {
        User userWithNullSocialLinks = User.builder()
                .id(13L)
                .email("nullsocial@example.com")
                .firstName("Null")
                .lastName("Social")
                .university("Null Social University")
                .userSkills(new HashSet<>())
                .experiences(new ArrayList<>())
                .projects(new ArrayList<>())
                .socialLinks(null)
                .build();

        UserResponse result = userMapper.toUserResponse(userWithNullSocialLinks);

        assertNotNull(result);
        assertEquals("nullsocial@example.com", result.getEmail());
        assertNotNull(result.getSocialLinks());
        assertTrue(result.getSocialLinks().isEmpty());
        verify(socialLinkMapper, never()).toSocialLinkResponse(any());
    }

    @Test
    @DisplayName("toUserResponse should map User with all collections populated")
    void shouldMapUserWithAllCollectionsPopulated() {
        Set<Skill> skills = new HashSet<>();
        skills.add(testSkill1);

        List<Experience> experiences = new ArrayList<>();
        experiences.add(testExperience1);

        List<Project> projects = new ArrayList<>();
        projects.add(testProject1);

        List<SocialLink> socialLinks = new ArrayList<>();
        socialLinks.add(testSocialLink1);

        User userWithAll = User.builder()
                .id(14L)
                .email("all@example.com")
                .firstName("All")
                .lastName("Collections")
                .university("All University")
                .userSkills(skills)
                .experiences(experiences)
                .projects(projects)
                .socialLinks(socialLinks)
                .build();

        when(skillMapper.toSkillResponse(testSkill1)).thenReturn(testSkillResponse1);
        when(experienceMapper.toExperienceResponse(testExperience1)).thenReturn(testExperienceResponse1);
        when(projectMapper.toProjectResponse(testProject1)).thenReturn(testProjectResponse1);
        when(socialLinkMapper.toSocialLinkResponse(testSocialLink1)).thenReturn(testSocialLinkResponse1);

        UserResponse result = userMapper.toUserResponse(userWithAll);

        assertNotNull(result);
        assertEquals("all@example.com", result.getEmail());
        assertEquals(1, result.getSkills().size());
        assertEquals(1, result.getExperiences().size());
        assertEquals(1, result.getProjects().size());
        assertEquals(1, result.getSocialLinks().size());
        verify(skillMapper).toSkillResponse(testSkill1);
        verify(experienceMapper).toExperienceResponse(testExperience1);
        verify(projectMapper).toProjectResponse(testProject1);
        verify(socialLinkMapper).toSocialLinkResponse(testSocialLink1);
    }
}

