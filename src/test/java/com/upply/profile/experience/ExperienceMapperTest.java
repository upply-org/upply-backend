package com.upply.experience;

import com.upply.profile.experience.Experience;
import com.upply.profile.experience.dto.ExperienceMapper;
import com.upply.profile.experience.dto.ExperienceRequest;
import com.upply.profile.experience.dto.ExperienceResponse;
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
@DisplayName("ExperienceMapper unit tests")
class ExperienceMapperTest {

    @InjectMocks
    private ExperienceMapper experienceMapper;

    private ExperienceRequest testExperienceRequest;
    private Experience testExperience;

    @BeforeEach
    void setUp() {
        Date startDate = new Date(System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000); // 1 year ago
        Date endDate = new Date();

        testExperienceRequest = new ExperienceRequest(
                "Software Engineer",
                "Tech Company",
                startDate,
                endDate,
                "Developed web applications using Java and Spring Boot"
        );

        testExperience = Experience.builder()
                .id(1L)
                .title("Software Engineer")
                .organization("Tech Company")
                .startDate(startDate)
                .endDate(endDate)
                .description("Developed web applications using Java and Spring Boot")
                .build();
    }

    @Test
    @DisplayName("toExperience should map ExperienceRequest to Experience with all fields")
    void shouldMapExperienceRequestToExperienceWithAllFields() {
        Experience result = experienceMapper.toExperience(testExperienceRequest);

        assertNotNull(result);
        assertEquals("Software Engineer", result.getTitle());
        assertEquals("Tech Company", result.getOrganization());
        assertEquals(testExperienceRequest.startDate(), result.getStartDate());
        assertEquals(testExperienceRequest.endDate(), result.getEndDate());
        assertEquals("Developed web applications using Java and Spring Boot", result.getDescription());
    }

    @Test
    @DisplayName("toExperience should map ExperienceRequest with null endDate to Experience")
    void shouldMapExperienceRequestWithNullEndDate() {
        ExperienceRequest requestWithNullEndDate = new ExperienceRequest(
                "Software Engineer",
                "Tech Company",
                testExperienceRequest.startDate(),
                null,
                "Current position"
        );

        Experience result = experienceMapper.toExperience(requestWithNullEndDate);

        assertNotNull(result);
        assertEquals("Software Engineer", result.getTitle());
        assertEquals("Tech Company", result.getOrganization());
        assertEquals(testExperienceRequest.startDate(), result.getStartDate());
        assertNull(result.getEndDate());
        assertEquals("Current position", result.getDescription());
    }

    @Test
    @DisplayName("toExperience should map ExperienceRequest with different fields to Experience")
    void shouldMapExperienceRequestWithDifferentFields() {
        Date newStartDate = new Date(System.currentTimeMillis() - 730L * 24 * 60 * 60 * 1000); // 2 years ago
        Date newEndDate = new Date(System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000); // 1 year ago

        ExperienceRequest differentRequest = new ExperienceRequest(
                "Junior Developer",
                "Startup Inc",
                newStartDate,
                newEndDate,
                "Worked on mobile applications"
        );

        Experience result = experienceMapper.toExperience(differentRequest);

        assertNotNull(result);
        assertEquals("Junior Developer", result.getTitle());
        assertEquals("Startup Inc", result.getOrganization());
        assertEquals(newStartDate, result.getStartDate());
        assertEquals(newEndDate, result.getEndDate());
        assertEquals("Worked on mobile applications", result.getDescription());
    }

    @Test
    @DisplayName("toExperience should map ExperienceRequest with empty strings to Experience")
    void shouldMapExperienceRequestWithEmptyStrings() {
        ExperienceRequest requestWithEmptyStrings = new ExperienceRequest(
                "",
                "",
                testExperienceRequest.startDate(),
                testExperienceRequest.endDate(),
                ""
        );

        Experience result = experienceMapper.toExperience(requestWithEmptyStrings);

        assertNotNull(result);
        assertEquals("", result.getTitle());
        assertEquals("", result.getOrganization());
        assertEquals("", result.getDescription());
    }

    @Test
    @DisplayName("toExperienceResponse should map Experience to ExperienceResponse with all fields")
    void shouldMapExperienceToExperienceResponseWithAllFields() {
        ExperienceResponse result = experienceMapper.toExperienceResponse(testExperience);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Software Engineer", result.title());
        assertEquals("Tech Company", result.organization());
        assertEquals(testExperience.getStartDate(), result.startDate());
        assertEquals(testExperience.getEndDate(), result.endDate());
        assertEquals("Developed web applications using Java and Spring Boot", result.description());
    }

    @Test
    @DisplayName("toExperienceResponse should map Experience with null endDate to ExperienceResponse")
    void shouldMapExperienceWithNullEndDateToExperienceResponse() {
        Experience experienceWithNullEndDate = Experience.builder()
                .id(2L)
                .title("Current Position")
                .organization("Current Company")
                .startDate(testExperience.getStartDate())
                .endDate(null)
                .description("Ongoing position")
                .build();

        ExperienceResponse result = experienceMapper.toExperienceResponse(experienceWithNullEndDate);

        assertNotNull(result);
        assertEquals(2L, result.id());
        assertEquals("Current Position", result.title());
        assertEquals("Current Company", result.organization());
        assertEquals(testExperience.getStartDate(), result.startDate());
        assertNull(result.endDate());
        assertEquals("Ongoing position", result.description());
    }

    @Test
    @DisplayName("toExperienceResponse should map Experience with different fields to ExperienceResponse")
    void shouldMapExperienceWithDifferentFieldsToExperienceResponse() {
        Date newStartDate = new Date(System.currentTimeMillis() - 1095L * 24 * 60 * 60 * 1000); // 3 years ago
        Date newEndDate = new Date(System.currentTimeMillis() - 730L * 24 * 60 * 60 * 1000); // 2 years ago

        Experience differentExperience = Experience.builder()
                .id(3L)
                .title("Intern")
                .organization("Big Corp")
                .startDate(newStartDate)
                .endDate(newEndDate)
                .description("Summer internship program")
                .build();

        ExperienceResponse result = experienceMapper.toExperienceResponse(differentExperience);

        assertNotNull(result);
        assertEquals(3L, result.id());
        assertEquals("Intern", result.title());
        assertEquals("Big Corp", result.organization());
        assertEquals(newStartDate, result.startDate());
        assertEquals(newEndDate, result.endDate());
        assertEquals("Summer internship program", result.description());
    }

    @Test
    @DisplayName("toExperienceResponse should map Experience with empty strings to ExperienceResponse")
    void shouldMapExperienceWithEmptyStringsToExperienceResponse() {
        Experience experienceWithEmptyStrings = Experience.builder()
                .id(4L)
                .title("")
                .organization("")
                .startDate(testExperience.getStartDate())
                .endDate(testExperience.getEndDate())
                .description("")
                .build();

        ExperienceResponse result = experienceMapper.toExperienceResponse(experienceWithEmptyStrings);

        assertNotNull(result);
        assertEquals(4L, result.id());
        assertEquals("", result.title());
        assertEquals("", result.organization());
        assertEquals("", result.description());
    }

    @Test
    @DisplayName("toExperienceResponse should map Experience with null fields to ExperienceResponse")
    void shouldMapExperienceWithNullFieldsToExperienceResponse() {
        Experience experienceWithNulls = Experience.builder()
                .id(5L)
                .title(null)
                .organization(null)
                .startDate(null)
                .endDate(null)
                .description(null)
                .build();

        ExperienceResponse result = experienceMapper.toExperienceResponse(experienceWithNulls);

        assertNotNull(result);
        assertEquals(5L, result.id());
        assertNull(result.title());
        assertNull(result.organization());
        assertNull(result.startDate());
        assertNull(result.endDate());
        assertNull(result.description());
    }

    @Test
    @DisplayName("toExperience should handle long description text")
    void shouldHandleLongDescriptionText() {
        String longDescription = "A".repeat(1000);
        ExperienceRequest requestWithLongDescription = new ExperienceRequest(
                "Software Engineer",
                "Tech Company",
                testExperienceRequest.startDate(),
                testExperienceRequest.endDate(),
                longDescription
        );

        Experience result = experienceMapper.toExperience(requestWithLongDescription);

        assertNotNull(result);
        assertEquals(longDescription, result.getDescription());
        assertEquals(1000, result.getDescription().length());
    }

    @Test
    @DisplayName("toExperienceResponse should handle Experience with User relationship")
    void shouldMapExperienceWithUserToExperienceResponse() {
        User testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .build();

        testExperience.setUser(testUser);

        ExperienceResponse result = experienceMapper.toExperienceResponse(testExperience);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Software Engineer", result.title());
        // Note: User is not included in ExperienceResponse, which is correct
    }
}

