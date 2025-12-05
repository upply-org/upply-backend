package com.upply.project;

import com.upply.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProjectMapper unit tests")
class ProjectMapperTest {

    @InjectMocks
    private ProjectMapper projectMapper;

    private ProjectRequest testProjectRequest;
    private Project testProject;

    @BeforeEach
    void setUp() {
        Date startDate = new Date(System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000); // 1 year ago
        Date endDate = new Date();

        testProjectRequest = new ProjectRequest(
                "E-Commerce Platform",
                "A full-stack e-commerce application built with Java and Spring Boot",
                "https://github.com/user/ecommerce",
                startDate,
                endDate,
                "Java, Spring Boot, React, PostgreSQL"
        );

        testProject = Project.builder()
                .id(1L)
                .title("E-Commerce Platform")
                .description("A full-stack e-commerce application built with Java and Spring Boot")
                .projectUrl("https://github.com/user/ecommerce")
                .startDate(startDate)
                .endDate(endDate)
                .technologies("Java, Spring Boot, React, PostgreSQL")
                .build();
    }

    @Test
    @DisplayName("toProject should map ProjectRequest to Project with all fields")
    void shouldMapProjectRequestToProjectWithAllFields() {
        Project result = projectMapper.toProject(testProjectRequest);

        assertNotNull(result);
        assertEquals("E-Commerce Platform", result.getTitle());
        assertEquals("A full-stack e-commerce application built with Java and Spring Boot", result.getDescription());
        assertEquals("https://github.com/user/ecommerce", result.getProjectUrl());
        assertEquals(testProjectRequest.startDate(), result.getStartDate());
        assertEquals(testProjectRequest.endDate(), result.getEndDate());
        assertEquals("Java, Spring Boot, React, PostgreSQL", result.getTechnologies());
    }

    @Test
    @DisplayName("toProject should map ProjectRequest with null endDate to Project")
    void shouldMapProjectRequestWithNullEndDate() {
        ProjectRequest requestWithNullEndDate = new ProjectRequest(
                "Ongoing Project",
                "A project currently in development",
                "https://github.com/user/ongoing",
                testProjectRequest.startDate(),
                null,
                "Python, Django"
        );

        Project result = projectMapper.toProject(requestWithNullEndDate);

        assertNotNull(result);
        assertEquals("Ongoing Project", result.getTitle());
        assertEquals("A project currently in development", result.getDescription());
        assertEquals("https://github.com/user/ongoing", result.getProjectUrl());
        assertEquals(testProjectRequest.startDate(), result.getStartDate());
        assertNull(result.getEndDate());
        assertEquals("Python, Django", result.getTechnologies());
    }

    @Test
    @DisplayName("toProject should map ProjectRequest with null projectUrl to Project")
    void shouldMapProjectRequestWithNullProjectUrl() {
        ProjectRequest requestWithNullUrl = new ProjectRequest(
                "Private Project",
                "A private project without public URL",
                null,
                testProjectRequest.startDate(),
                testProjectRequest.endDate(),
                "Java, Spring Boot"
        );

        Project result = projectMapper.toProject(requestWithNullUrl);

        assertNotNull(result);
        assertEquals("Private Project", result.getTitle());
        assertEquals("A private project without public URL", result.getDescription());
        assertNull(result.getProjectUrl());
        assertEquals("Java, Spring Boot", result.getTechnologies());
    }

    @Test
    @DisplayName("toProject should map ProjectRequest with null technologies to Project")
    void shouldMapProjectRequestWithNullTechnologies() {
        ProjectRequest requestWithNullTechnologies = new ProjectRequest(
                "Project Without Tech",
                "A project without specified technologies",
                "https://github.com/user/project",
                testProjectRequest.startDate(),
                testProjectRequest.endDate(),
                null
        );

        Project result = projectMapper.toProject(requestWithNullTechnologies);

        assertNotNull(result);
        assertEquals("Project Without Tech", result.getTitle());
        assertNull(result.getTechnologies());
    }

    @Test
    @DisplayName("toProject should map ProjectRequest with different fields to Project")
    void shouldMapProjectRequestWithDifferentFields() {
        Date newStartDate = new Date(System.currentTimeMillis() - 730L * 24 * 60 * 60 * 1000); // 2 years ago
        Date newEndDate = new Date(System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000); // 1 year ago

        ProjectRequest differentRequest = new ProjectRequest(
                "Task Management App",
                "A task management application for teams",
                "https://github.com/user/taskapp",
                newStartDate,
                newEndDate,
                "Python, Django, Vue.js, PostgreSQL"
        );

        Project result = projectMapper.toProject(differentRequest);

        assertNotNull(result);
        assertEquals("Task Management App", result.getTitle());
        assertEquals("A task management application for teams", result.getDescription());
        assertEquals("https://github.com/user/taskapp", result.getProjectUrl());
        assertEquals(newStartDate, result.getStartDate());
        assertEquals(newEndDate, result.getEndDate());
        assertEquals("Python, Django, Vue.js, PostgreSQL", result.getTechnologies());
    }

    @Test
    @DisplayName("toProject should map ProjectRequest with empty strings to Project")
    void shouldMapProjectRequestWithEmptyStrings() {
        ProjectRequest requestWithEmptyStrings = new ProjectRequest(
                "",
                "",
                "",
                testProjectRequest.startDate(),
                testProjectRequest.endDate(),
                ""
        );

        Project result = projectMapper.toProject(requestWithEmptyStrings);

        assertNotNull(result);
        assertEquals("", result.getTitle());
        assertEquals("", result.getDescription());
        assertEquals("", result.getProjectUrl());
        assertEquals("", result.getTechnologies());
    }

    @Test
    @DisplayName("toProjectResponse should map Project to ProjectResponse with all fields")
    void shouldMapProjectToProjectResponseWithAllFields() {
        ProjectResponse result = projectMapper.toProjectResponse(testProject);

        assertNotNull(result);
        assertEquals(1L, result.Id());
        assertEquals("E-Commerce Platform", result.title());
        assertEquals("A full-stack e-commerce application built with Java and Spring Boot", result.description());
        assertEquals("https://github.com/user/ecommerce", result.projectUrl());
        assertEquals(testProject.getStartDate(), result.startDate());
        assertEquals(testProject.getEndDate(), result.endDate());
        assertEquals("Java, Spring Boot, React, PostgreSQL", result.technologies());
    }

    @Test
    @DisplayName("toProjectResponse should map Project with null endDate to ProjectResponse")
    void shouldMapProjectWithNullEndDateToProjectResponse() {
        Project projectWithNullEndDate = Project.builder()
                .id(2L)
                .title("Ongoing Project")
                .description("A project currently in development")
                .projectUrl("https://github.com/user/ongoing")
                .startDate(testProject.getStartDate())
                .endDate(null)
                .technologies("Python, Django")
                .build();

        ProjectResponse result = projectMapper.toProjectResponse(projectWithNullEndDate);

        assertNotNull(result);
        assertEquals(2L, result.Id());
        assertEquals("Ongoing Project", result.title());
        assertEquals("A project currently in development", result.description());
        assertEquals("https://github.com/user/ongoing", result.projectUrl());
        assertEquals(testProject.getStartDate(), result.startDate());
        assertNull(result.endDate());
        assertEquals("Python, Django", result.technologies());
    }

    @Test
    @DisplayName("toProjectResponse should map Project with null projectUrl to ProjectResponse")
    void shouldMapProjectWithNullProjectUrlToProjectResponse() {
        Project projectWithNullUrl = Project.builder()
                .id(3L)
                .title("Private Project")
                .description("A private project without public URL")
                .projectUrl(null)
                .startDate(testProject.getStartDate())
                .endDate(testProject.getEndDate())
                .technologies("Java, Spring Boot")
                .build();

        ProjectResponse result = projectMapper.toProjectResponse(projectWithNullUrl);

        assertNotNull(result);
        assertEquals(3L, result.Id());
        assertEquals("Private Project", result.title());
        assertEquals("A private project without public URL", result.description());
        assertNull(result.projectUrl());
        assertEquals("Java, Spring Boot", result.technologies());
    }

    @Test
    @DisplayName("toProjectResponse should map Project with null technologies to ProjectResponse")
    void shouldMapProjectWithNullTechnologiesToProjectResponse() {
        Project projectWithNullTechnologies = Project.builder()
                .id(4L)
                .title("Project Without Tech")
                .description("A project without specified technologies")
                .projectUrl("https://github.com/user/project")
                .startDate(testProject.getStartDate())
                .endDate(testProject.getEndDate())
                .technologies(null)
                .build();

        ProjectResponse result = projectMapper.toProjectResponse(projectWithNullTechnologies);

        assertNotNull(result);
        assertEquals(4L, result.Id());
        assertEquals("Project Without Tech", result.title());
        assertNull(result.technologies());
    }

    @Test
    @DisplayName("toProjectResponse should map Project with different fields to ProjectResponse")
    void shouldMapProjectWithDifferentFieldsToProjectResponse() {
        Date newStartDate = new Date(System.currentTimeMillis() - 1095L * 24 * 60 * 60 * 1000); // 3 years ago
        Date newEndDate = new Date(System.currentTimeMillis() - 730L * 24 * 60 * 60 * 1000); // 2 years ago

        Project differentProject = Project.builder()
                .id(5L)
                .title("Mobile App")
                .description("A mobile application for iOS and Android")
                .projectUrl("https://github.com/user/mobileapp")
                .startDate(newStartDate)
                .endDate(newEndDate)
                .technologies("React Native, TypeScript, Firebase")
                .build();

        ProjectResponse result = projectMapper.toProjectResponse(differentProject);

        assertNotNull(result);
        assertEquals(5L, result.Id());
        assertEquals("Mobile App", result.title());
        assertEquals("A mobile application for iOS and Android", result.description());
        assertEquals("https://github.com/user/mobileapp", result.projectUrl());
        assertEquals(newStartDate, result.startDate());
        assertEquals(newEndDate, result.endDate());
        assertEquals("React Native, TypeScript, Firebase", result.technologies());
    }

    @Test
    @DisplayName("toProjectResponse should map Project with empty strings to ProjectResponse")
    void shouldMapProjectWithEmptyStringsToProjectResponse() {
        Project projectWithEmptyStrings = Project.builder()
                .id(6L)
                .title("")
                .description("")
                .projectUrl("")
                .startDate(testProject.getStartDate())
                .endDate(testProject.getEndDate())
                .technologies("")
                .build();

        ProjectResponse result = projectMapper.toProjectResponse(projectWithEmptyStrings);

        assertNotNull(result);
        assertEquals(6L, result.Id());
        assertEquals("", result.title());
        assertEquals("", result.description());
        assertEquals("", result.projectUrl());
        assertEquals("", result.technologies());
    }

    @Test
    @DisplayName("toProjectResponse should map Project with null fields to ProjectResponse")
    void shouldMapProjectWithNullFieldsToProjectResponse() {
        Project projectWithNulls = Project.builder()
                .id(7L)
                .title(null)
                .description(null)
                .projectUrl(null)
                .startDate(null)
                .endDate(null)
                .technologies(null)
                .build();

        ProjectResponse result = projectMapper.toProjectResponse(projectWithNulls);

        assertNotNull(result);
        assertEquals(7L, result.Id());
        assertNull(result.title());
        assertNull(result.description());
        assertNull(result.projectUrl());
        assertNull(result.startDate());
        assertNull(result.endDate());
        assertNull(result.technologies());
    }

    @Test
    @DisplayName("toProject should handle long description text")
    void shouldHandleLongDescriptionText() {
        String longDescription = "A".repeat(1000);
        ProjectRequest requestWithLongDescription = new ProjectRequest(
                "Project with Long Description",
                longDescription,
                "https://github.com/user/longdesc",
                testProjectRequest.startDate(),
                testProjectRequest.endDate(),
                "Java"
        );

        Project result = projectMapper.toProject(requestWithLongDescription);

        assertNotNull(result);
        assertEquals(longDescription, result.getDescription());
        assertEquals(1000, result.getDescription().length());
    }

    @Test
    @DisplayName("toProject should handle long technologies string")
    void shouldHandleLongTechnologiesString() {
        String longTechnologies = "Java, Spring Boot, React, PostgreSQL, Docker, Kubernetes, AWS, " +
                "Redis, MongoDB, Elasticsearch, RabbitMQ, Kafka, GraphQL, REST API, " +
                "Microservices, CI/CD, Jenkins, GitLab, Terraform, Ansible".repeat(5);
        ProjectRequest requestWithLongTechnologies = new ProjectRequest(
                "Project with Many Technologies",
                "A project using many technologies",
                "https://github.com/user/manytech",
                testProjectRequest.startDate(),
                testProjectRequest.endDate(),
                longTechnologies
        );

        Project result = projectMapper.toProject(requestWithLongTechnologies);

        assertNotNull(result);
        assertEquals(longTechnologies, result.getTechnologies());
    }

    @Test
    @DisplayName("toProjectResponse should map Project with User relationship")
    void shouldMapProjectWithUserToProjectResponse() {
        User testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .build();

        testProject.setUser(testUser);

        ProjectResponse result = projectMapper.toProjectResponse(testProject);

        assertNotNull(result);
        assertEquals(1L, result.Id());
        assertEquals("E-Commerce Platform", result.title());
        // Note: User is not included in ProjectResponse, which is correct
    }
}
