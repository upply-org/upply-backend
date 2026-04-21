package com.upply.job;

import com.upply.job.dto.JobMapper;
import com.upply.job.dto.JobListResponse;
import com.upply.job.dto.JobRequest;
import com.upply.job.dto.JobResponse;
import com.upply.job.dto.MatchedJobListResponse;
import com.upply.job.enums.*;
import com.upply.profile.skill.Skill;
import com.upply.profile.skill.dto.SkillMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JobMapper unit tests")
class JobMapperTest {

    @Mock
    private SkillMapper skillMapper;

    @InjectMocks
    private JobMapper jobMapper;

    private Job testJob;

    @BeforeEach
    void setUp() {
        testJob = new Job();
        testJob.setId(1L);
        testJob.setTitle("Senior Java Developer");
        testJob.setOrganizationName("Tech Corp");
        testJob.setType(JobType.FULL_TIME);
        testJob.setSeniority(JobSeniority.SENIOR);
        testJob.setModel(JobModel.REMOTE);
        testJob.setStatus(JobStatus.OPEN);
        testJob.setSource(JobSource.INTERNAL);
        testJob.setLocation("New York, NY");
        testJob.setDescription("We are looking for a senior Java developer");
        testJob.setCreatedDate(Instant.now());
        testJob.setApplicationLink("https://example.com/apply");
    }

    @Test
    @DisplayName("toJobResponse should map Job to JobResponse")
    void shouldMapJobToJobResponse() {
        com.upply.profile.skill.Skill testSkill = new com.upply.profile.skill.Skill();
        testSkill.setId(1L);
        testSkill.setName("Java Programming");
        testJob.setSkills(new HashSet<>(Set.of(testSkill)));

        com.upply.profile.skill.dto.SkillResponse skillResponse = com.upply.profile.skill.dto.SkillResponse.builder()
                .skillId(1L)
                .skillName("Java Programming")
                .build();
        when(skillMapper.toSkillResponse(any())).thenReturn(skillResponse);

        JobResponse result = jobMapper.toJobResponse(testJob);

        assertNotNull(result);
        assertEquals(testJob.getId(), result.getId());
        assertEquals(testJob.getTitle(), result.getTitle());
        assertEquals(testJob.getOrganizationName(), result.getOrganizationName());
        assertEquals(testJob.getType().toApiValue(), result.getType());
        assertEquals(testJob.getSeniority().toApiValue(), result.getSeniority());
        assertEquals(testJob.getModel().toApiValue(), result.getModel());
        assertEquals(testJob.getStatus().toApiValue(), result.getStatus());
        assertEquals(testJob.getSource().toApiValue(), result.getJobSource());
        assertEquals(testJob.getLocation(), result.getLocation());
        assertEquals(testJob.getDescription(), result.getDescription());
        assertEquals(testJob.getCreatedDate(), result.getCreatedDate());
        assertEquals(testJob.getApplicationLink(), result.getApplicationLink());

        verify(skillMapper).toSkillResponse(any());
    }

    @Test
    @DisplayName("toJobResponse should handle null fields gracefully")
    void shouldHandleNullFieldsInJobResponse() {
        Job jobWithNulls = new Job();
        jobWithNulls.setId(1L);
        jobWithNulls.setTitle("Test Job");
        jobWithNulls.setOrganizationName("Test Org");

        JobResponse result = jobMapper.toJobResponse(jobWithNulls);

        assertNotNull(result);
        assertEquals("Test Job", result.getTitle());
        assertNull(result.getType());
        assertNull(result.getSeniority());
        assertNull(result.getModel());
        assertNull(result.getStatus());
        assertNull(result.getJobSource());
    }

    @Test
    @DisplayName("toJobListResponse should map Job to JobListResponse")
    void shouldMapJobToJobListResponse() {
        JobListResponse result = jobMapper.toJobListResponse(testJob);

        assertNotNull(result);
        assertEquals(testJob.getId(), result.getId());
        assertEquals(testJob.getTitle(), result.getTitle());
        assertEquals(testJob.getOrganizationName(), result.getOrganizationName());
        assertEquals(testJob.getType().toApiValue(), result.getType());
        assertEquals(testJob.getSeniority().toApiValue(), result.getSeniority());
        assertEquals(testJob.getModel().toApiValue(), result.getModel());
        assertEquals(testJob.getStatus().toApiValue(), result.getStatus());
        assertEquals(testJob.getSource().toApiValue(), result.getJobSource());
        assertEquals(testJob.getLocation(), result.getLocation());
        assertEquals(testJob.getCreatedDate(), result.getCreatedDate());
    }

    @Test
    @DisplayName("toMatchedJobListResponse should map Job to MatchedJobListResponse with match score")
    void shouldMapJobToMatchedJobListResponse() {
        Double matchScore = 0.85;

        MatchedJobListResponse result = jobMapper.toMatchedJobListResponse(testJob, matchScore);

        assertNotNull(result);
        assertEquals(testJob.getId(), result.getId());
        assertEquals(testJob.getTitle(), result.getTitle());
        assertEquals(matchScore, result.getMatchScore());
        assertEquals(85, result.getMatchPercentage());
    }

    @Test
    @DisplayName("toJob should map JobRequest and skills to Job")
    void shouldMapJobRequestToJob() {
        JobRequest request = JobRequest.builder()
                .title("Software Engineer")
                .type("full-time")
                .seniority("senior")
                .model("remote")
                .location("New York")
                .description("Great job opportunity")
                .build();

        Skill skill = Skill.builder()
                .id(1L)
                .name("Java")
                .build();
        Set<Skill> skills = Set.of(skill);

        Job result = jobMapper.toJob(request, skills);

        assertNotNull(result);
        assertEquals(request.getTitle(), result.getTitle());
        assertEquals(JobType.FULL_TIME, result.getType());
        assertEquals(JobSeniority.SENIOR, result.getSeniority());
        assertEquals(JobModel.REMOTE, result.getModel());
        assertEquals(JobStatus.OPEN, result.getStatus());
        assertEquals(request.getLocation(), result.getLocation());
        assertEquals(request.getDescription(), result.getDescription());
        assertEquals(JobSource.INTERNAL, result.getSource());
        assertEquals(skills, result.getSkills());
    }

    @Test
    @DisplayName("toJob should map JobRequest with different type values")
    void shouldMapJobRequestWithDifferentTypes() {
        JobRequest request = JobRequest.builder()
                .title("Part Time Job")
                .type("part-time")
                .seniority("junior")
                .model("onsite")
                .location("London")
                .description("Part time position")
                .build();

        Job result = jobMapper.toJob(request, new HashSet<>());

        assertNotNull(result);
        assertEquals(JobType.PART_TIME, result.getType());
        assertEquals(JobSeniority.JUNIOR, result.getSeniority());
        assertEquals(JobModel.ONSITE, result.getModel());
    }

    @Test
    @DisplayName("toJob should map JobRequest with internship type")
    void shouldMapJobRequestWithInternship() {
        JobRequest request = JobRequest.builder()
                .title("Internship")
                .type("internship")
                .seniority("intern")
                .model("hybrid")
                .location("Berlin")
                .description("Internship program")
                .build();

        Job result = jobMapper.toJob(request, new HashSet<>());

        assertNotNull(result);
        assertEquals(JobType.INTERNSHIP, result.getType());
        assertEquals(JobSeniority.INTERN, result.getSeniority());
        assertEquals(JobModel.HYBRID, result.getModel());
    }
}