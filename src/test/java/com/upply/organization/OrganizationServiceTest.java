package com.upply.organization;

import com.upply.common.PageResponse;
import com.upply.exception.custom.OperationNotPermittedException;
import com.upply.exception.custom.ResourceNotFoundException;
import com.upply.job.Job;
import com.upply.job.dto.JobListResponse;
import com.upply.job.dto.JobMapper;
import com.upply.job.enums.JobStatus;
import com.upply.organization.dto.OrganizationMapper;
import com.upply.organization.dto.OrganizationRequest;
import com.upply.organization.dto.OrganizationResponse;
import com.upply.organization.dto.OrganizationUpdateRequest;
import com.upply.user.User;
import com.upply.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrganizationService unit tests")
class OrganizationServiceTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private OrganizationMapper organizationMapper;

    @Mock
    private JobMapper jobMapper;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrganizationService organizationService;

    private User testUser;
    private Organization testOrganization;
    private OrganizationRequest testOrganizationRequest;
    private OrganizationResponse testOrganizationResponse;
    private OrganizationUpdateRequest testUpdateRequest;
    private Job testJob;
    private JobListResponse testJobResponse;
    private Authentication mockAuthentication;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("recruiter@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        testOrganization = Organization.builder()
                .id(1L)
                .name("Tech Corp")
                .domain("techcorp.com")
                .description("A tech company")
                .website("https://techcorp.com")
                .logoUrl("https://techcorp.com/logo.png")
                .industry("Technology")
                .size("100-500")
                .location("San Francisco")
                .isVerified(false)
                .recruiters(new ArrayList<>())
                .jobs(new ArrayList<>())
                .build();

        testOrganizationRequest = new OrganizationRequest(
                "Tech Corp",
                "techcorp.com",
                "A tech company",
                "https://techcorp.com",
                "https://techcorp.com/logo.png",
                "Technology",
                "100-500",
                "San Francisco"
        );

        testOrganizationResponse = OrganizationResponse.builder()
                .id(1L)
                .name("Tech Corp")
                .domain("techcorp.com")
                .description("A tech company")
                .website("https://techcorp.com")
                .logoUrl("https://techcorp.com/logo.png")
                .industry("Technology")
                .size("100-500")
                .location("San Francisco")
                .isVerified(false)
                .build();

        testUpdateRequest = new OrganizationUpdateRequest();
        testUpdateRequest.setName("Updated Tech Corp");
        testUpdateRequest.setDescription("Updated description");
        testUpdateRequest.setWebsite("https://updated.com");

        testJob = Job.builder()
                .id(1L)
                .title("Software Engineer")
                .status(JobStatus.OPEN)
                .organization(testOrganization)
                .build();

        testJobResponse = JobListResponse.builder()
                .id(1L)
                .title("Software Engineer")
                .build();

        mockAuthentication = mock(Authentication.class);
    }

    @Test
    @DisplayName("createOrganization should create and return organization with recruiter")
    void shouldCreateOrganizationSuccessfully() {
        when(organizationMapper.toOrganization(testOrganizationRequest)).thenReturn(testOrganization);
        when(mockAuthentication.getPrincipal()).thenReturn(testUser);
        when(organizationRepository.save(testOrganization)).thenReturn(testOrganization);
        when(organizationMapper.toResponse(testOrganization)).thenReturn(testOrganizationResponse);

        OrganizationResponse result = organizationService.createOrganization(testOrganizationRequest, mockAuthentication);

        assertNotNull(result);
        assertEquals("Tech Corp", result.getName());
        assertEquals(1, testOrganization.getRecruiters().size());
        verify(organizationMapper).toOrganization(testOrganizationRequest);
        verify(organizationRepository).save(testOrganization);
        verify(organizationMapper).toResponse(testOrganization);
    }

    @Test
    @DisplayName("getOrganization should return organization when found")
    void shouldGetOrganizationSuccessfully() {
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(testOrganization));
        when(organizationMapper.toResponse(testOrganization)).thenReturn(testOrganizationResponse);

        OrganizationResponse result = organizationService.getOrganization(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Tech Corp", result.getName());
        verify(organizationRepository).findById(1L);
        verify(organizationMapper).toResponse(testOrganization);
    }

    @Test
    @DisplayName("getOrganization should throw ResourceNotFoundException when not found")
    void shouldThrowExceptionWhenOrganizationNotFound() {
        when(organizationRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> organizationService.getOrganization(999L)
        );

        assertTrue(exception.getMessage().contains("not found"));
        verify(organizationRepository).findById(999L);
    }

    @Test
    @DisplayName("updateOrganization should update organization when user is recruiter")
    void shouldUpdateOrganizationSuccessfully() {
        testOrganization.getRecruiters().add(testUser);
        
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(testOrganization));
        when(mockAuthentication.getPrincipal()).thenReturn(testUser);
        when(organizationRepository.save(testOrganization)).thenReturn(testOrganization);
        when(organizationMapper.toResponse(testOrganization)).thenReturn(testOrganizationResponse);

        OrganizationResponse result = organizationService.updateOrganization(1L, testUpdateRequest, mockAuthentication);

        assertNotNull(result);
        verify(organizationMapper).updateFromRequest(testUpdateRequest, testOrganization);
        verify(organizationRepository).save(testOrganization);
    }

    @Test
    @DisplayName("updateOrganization should throw OperationNotPermittedException when user is not recruiter")
    void shouldThrowExceptionWhenUserIsNotRecruiter() {
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(testOrganization));
        when(mockAuthentication.getPrincipal()).thenReturn(testUser);

        OperationNotPermittedException exception = assertThrows(
                OperationNotPermittedException.class,
                () -> organizationService.updateOrganization(1L, testUpdateRequest, mockAuthentication)
        );

        assertTrue(exception.getMessage().contains("not permitted"));
        verify(organizationRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateOrganization should throw ResourceNotFoundException when organization not found")
    void shouldThrowExceptionWhenOrganizationNotFoundInUpdate() {
        when(organizationRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> organizationService.updateOrganization(999L, testUpdateRequest, mockAuthentication)
        );

        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    @DisplayName("getOrganizationOpenJobs should return paginated jobs")
    void shouldGetOrganizationOpenJobsSuccessfully() {
        List<Job> jobs = List.of(testJob);
        PageImpl<Job> jobPage = new PageImpl<>(jobs, PageRequest.of(0, 10), 1);
        
        when(organizationRepository.existsById(1L)).thenReturn(true);
        when(organizationRepository.findJobsByOrganizationIdAndStatus(eq(1L), eq(JobStatus.OPEN), any(PageRequest.class)))
                .thenReturn(jobPage);
        when(jobMapper.toJobListResponse(testJob)).thenReturn(testJobResponse);

        PageResponse<JobListResponse> result = organizationService.getOrganizationOpenJobs(1L, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(organizationRepository).existsById(1L);
    }

    @Test
    @DisplayName("getOrganizationOpenJobs should throw ResourceNotFoundException when organization not found")
    void shouldThrowExceptionWhenOrganizationNotFoundInGetOpenJobs() {
        when(organizationRepository.existsById(999L)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> organizationService.getOrganizationOpenJobs(999L, 0, 10)
        );

        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    @DisplayName("getOrganizationOpenJobs should return empty page when no jobs")
    void shouldReturnEmptyPageWhenNoJobs() {
        PageImpl<Job> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        
        when(organizationRepository.existsById(1L)).thenReturn(true);
        when(organizationRepository.findJobsByOrganizationIdAndStatus(eq(1L), eq(JobStatus.OPEN), any(PageRequest.class)))
                .thenReturn(emptyPage);

        PageResponse<JobListResponse> result = organizationService.getOrganizationOpenJobs(1L, 0, 10);

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
    }
}
