package com.upply.organization;

import com.upply.email.EmailService;
import com.upply.email.EmailTemplate;
import com.upply.exception.custom.BusinessLogicException;
import com.upply.exception.custom.OperationNotPermittedException;
import com.upply.exception.custom.ResourceNotFoundException;
import com.upply.job.Job;
import com.upply.job.dto.JobListResponse;
import com.upply.job.dto.JobMapper;
import com.upply.job.enums.JobStatus;
import com.upply.organization.dto.ConnectToOrganizationRequest;
import com.upply.organization.dto.ConnectToOrganizationResponse;
import com.upply.organization.dto.OrganizationMapper;
import com.upply.organization.dto.OrganizationResponse;
import com.upply.organization.dto.OrganizationUpdateRequest;
import com.upply.token.BusinessEmailVerificationToken;
import com.upply.token.BusinessEmailVerificationTokenRepository;
import com.upply.user.User;
import com.upply.user.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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

    @Mock
    private EmailService emailService;

    @Mock
    private BusinessEmailVerificationTokenRepository tokenRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private OrganizationService organizationService;

    private User testUser;
    private Organization testOrganization;
    private OrganizationUpdateRequest testUpdateRequest;
    private Job testJob;
    private JobListResponse testJobResponse;
    private Authentication mockAuthentication;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(organizationService, "verificationBaseUrl", "https://api.upply.tech/api/v1/organizations/connect/verify");
        ReflectionTestUtils.setField(organizationService, "objectMapper", new ObjectMapper());

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
    @DisplayName("getOrganization should return organization when found")
    void shouldGetOrganizationSuccessfully() {
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(testOrganization));
        when(organizationMapper.toResponse(testOrganization)).thenReturn(OrganizationResponse.builder()
                .id(1L)
                .name("Tech Corp")
                .domain("techcorp.com")
                .build());

        OrganizationResponse result = organizationService.getOrganization(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Tech Corp", result.getName());
        verify(organizationRepository).findById(1L);
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
    }

    @Test
    @DisplayName("updateOrganization should update organization when user is recruiter")
    void shouldUpdateOrganizationSuccessfully() {
        testOrganization.getRecruiters().add(testUser);

        when(organizationRepository.findById(1L)).thenReturn(Optional.of(testOrganization));
        when(mockAuthentication.getPrincipal()).thenReturn(testUser);
        when(organizationRepository.save(testOrganization)).thenReturn(testOrganization);
        when(organizationMapper.toResponse(testOrganization)).thenReturn(OrganizationResponse.builder()
                .id(1L)
                .name("Updated Tech Corp")
                .build());

        OrganizationResponse result = organizationService.updateOrganization(1L, testUpdateRequest, mockAuthentication);

        assertNotNull(result);
        verify(organizationMapper).updateFromRequest(testUpdateRequest, testOrganization);
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

        var result = organizationService.getOrganizationOpenJobs(1L, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    // ==================== initiateConnection tests ====================

    @Test
    @DisplayName("initiateConnection should throw exception for public email domain")
    void shouldThrowExceptionForPublicEmailDomain() {
        ConnectToOrganizationRequest request = ConnectToOrganizationRequest.builder()
                .businessEmail("user@gmail.com")
                .build();

        BusinessLogicException exception = assertThrows(
                BusinessLogicException.class,
                () -> organizationService.initiateConnection(request, testUser)
        );

        assertTrue(exception.getMessage().contains("Public email domains are not allowed"));
        verify(organizationRepository, never()).findByDomain(any());
        verify(emailService, never()).sendEmail(any(), any(), any(), any());
    }

    @Test
    @DisplayName("initiateConnection should throw exception for public email domain (outlook)")
    void shouldThrowExceptionForOutlookEmailDomain() {
        ConnectToOrganizationRequest request = ConnectToOrganizationRequest.builder()
                .businessEmail("user@outlook.com")
                .build();

        BusinessLogicException exception = assertThrows(
                BusinessLogicException.class,
                () -> organizationService.initiateConnection(request, testUser)
        );

        assertTrue(exception.getMessage().contains("Public email domains are not allowed"));
    }

    @Test
    @DisplayName("initiateConnection should send verification email when org exists")
    void shouldSendVerificationEmailWhenOrgExists() throws JsonProcessingException {
        ConnectToOrganizationRequest request = ConnectToOrganizationRequest.builder()
                .businessEmail("user@techcorp.com")
                .build();

        when(organizationRepository.findByDomain("techcorp.com")).thenReturn(Optional.of(testOrganization));
        when(tokenRepository.save(any(BusinessEmailVerificationToken.class))).thenReturn(null);

        ConnectToOrganizationResponse result = organizationService.initiateConnection(request, testUser);

        assertNotNull(result);
        assertTrue(result.getMessage().contains("Verification email sent"));
        assertFalse(result.isOrganizationCreated());
        assertNull(result.getOrganization());

        verify(emailService).sendEmail(
                eq("user@techcorp.com"),
                eq("Verify Your Business Email"),
                eq(EmailTemplate.BUSINESS_EMAIL_VERIFICATION),
                anyMap()
        );
    }

    @Test
    @DisplayName("initiateConnection should throw when user already connected to existing org")
    void shouldThrowWhenUserAlreadyConnectedToOrg() {
        ConnectToOrganizationRequest request = ConnectToOrganizationRequest.builder()
                .businessEmail("user@techcorp.com")
                .build();

        when(organizationRepository.findByDomain("techcorp.com")).thenReturn(Optional.of(testOrganization));
        when(organizationRepository.existsRecruiterInOrganization(1L, 1L)).thenReturn(true);

        BusinessLogicException exception = assertThrows(
                BusinessLogicException.class,
                () -> organizationService.initiateConnection(request, testUser)
        );

        assertTrue(exception.getMessage().contains("already connected"));
    }

    @Test
    @DisplayName("initiateConnection should require org details when org doesn't exist")
    void shouldRequireOrgDetailsWhenOrgDoesNotExist() {
        ConnectToOrganizationRequest request = ConnectToOrganizationRequest.builder()
                .businessEmail("user@newcompany.com")
                .build();

        when(organizationRepository.findByDomain("newcompany.com")).thenReturn(Optional.empty());

        BusinessLogicException exception = assertThrows(
                BusinessLogicException.class,
                () -> organizationService.initiateConnection(request, testUser)
        );

        assertTrue(exception.getMessage().contains("Organization details are required"));
    }

    @Test
    @DisplayName("initiateConnection should send verification email when creating new org")
    void shouldSendVerificationEmailWhenCreatingNewOrg() throws JsonProcessingException {
        ConnectToOrganizationRequest.OrganizationDetails orgDetails = ConnectToOrganizationRequest.OrganizationDetails.builder()
                .name("New Company")
                .industry("Technology")
                .size("50-100")
                .location("New York")
                .build();

        ConnectToOrganizationRequest request = ConnectToOrganizationRequest.builder()
                .businessEmail("user@newcompany.com")
                .organization(orgDetails)
                .build();

        when(organizationRepository.findByDomain("newcompany.com")).thenReturn(Optional.empty());
        when(tokenRepository.save(any(BusinessEmailVerificationToken.class))).thenReturn(null);

        ConnectToOrganizationResponse result = organizationService.initiateConnection(request, testUser);

        assertNotNull(result);
        assertTrue(result.getMessage().contains("Verification email sent"));
        assertFalse(result.isOrganizationCreated());

        verify(emailService).sendEmail(
                eq("user@newcompany.com"),
                eq("Verify Your Business Email"),
                eq(EmailTemplate.BUSINESS_EMAIL_VERIFICATION),
                anyMap()
        );
    }

    @Test
    @DisplayName("initiateConnection should not update user organization in database")
    void shouldNotUpdateUserOrganizationOnInitiateConnection() throws JsonProcessingException {
        ConnectToOrganizationRequest request = ConnectToOrganizationRequest.builder()
                .businessEmail("user@techcorp.com")
                .build();

        when(organizationRepository.findByDomain("techcorp.com")).thenReturn(Optional.of(testOrganization));
        when(tokenRepository.save(any(BusinessEmailVerificationToken.class))).thenReturn(null);

        organizationService.initiateConnection(request, testUser);

        verify(userRepository, never()).save(any(User.class));
        assertNull(testUser.getOrganization());
    }

    // ==================== verifyAndConnect tests ====================

    @Test
    @DisplayName("verifyAndConnect should throw ResourceNotFoundException for invalid token")
    void shouldThrowExceptionForInvalidToken() {
        when(tokenRepository.findUnusedByTokenString("invalidtoken"))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> organizationService.verifyAndConnect("invalidtoken")
        );

        assertTrue(exception.getMessage().contains("Invalid verification token"));
    }

    @Test
    @DisplayName("verifyAndConnect should throw BusinessLogicException when token already used")
    void shouldThrowExceptionWhenTokenAlreadyUsed() {
        BusinessEmailVerificationToken usedToken = BusinessEmailVerificationToken.builder()
                .id(1)
                .token("validtoken:encodeddata")
                .used(true)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();

        when(tokenRepository.findUnusedByTokenString("validtoken"))
                .thenReturn(Optional.of(usedToken));

        BusinessLogicException exception = assertThrows(
                BusinessLogicException.class,
                () -> organizationService.verifyAndConnect("validtoken")
        );

        assertTrue(exception.getMessage().contains("already been used"));
    }

    @Test
    @DisplayName("verifyAndConnect should throw BusinessLogicException when token expired")
    void shouldThrowExceptionWhenTokenExpired() {
        BusinessEmailVerificationToken expiredToken = BusinessEmailVerificationToken.builder()
                .id(1)
                .token("validtoken:encodeddata")
                .used(false)
                .expiresAt(LocalDateTime.now().minusHours(1))
                .user(testUser)
                .build();

        when(tokenRepository.findUnusedByTokenString("validtoken"))
                .thenReturn(Optional.of(expiredToken));

        BusinessLogicException exception = assertThrows(
                BusinessLogicException.class,
                () -> organizationService.verifyAndConnect("validtoken")
        );

        assertTrue(exception.getMessage().contains("expired"));
    }

    @Test
    @DisplayName("verifyAndConnect should connect to existing organization successfully")
    void shouldConnectToExistingOrganization() throws JsonProcessingException {
        String encodedData = java.util.Base64.getEncoder()
                .encodeToString("{\"domain\":\"techcorp.com\",\"businessEmail\":\"user@techcorp.com\"}".getBytes());
        String tokenWithData = "validtoken:" + encodedData;

        BusinessEmailVerificationToken validToken = BusinessEmailVerificationToken.builder()
                .id(1)
                .token(tokenWithData)
                .used(false)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .user(testUser)
                .build();

        OrganizationResponse orgResponse = OrganizationResponse.builder()
                .id(1L)
                .name("Tech Corp")
                .domain("techcorp.com")
                .build();

        when(tokenRepository.findUnusedByTokenString("validtoken"))
                .thenReturn(Optional.of(validToken));
        when(organizationRepository.findByDomain("techcorp.com"))
                .thenReturn(Optional.of(testOrganization));
        when(organizationMapper.toResponse(testOrganization)).thenReturn(orgResponse);

        ConnectToOrganizationResponse result = organizationService.verifyAndConnect("validtoken");

        assertNotNull(result);
        assertTrue(result.getMessage().contains("Successfully connected"));
        assertEquals("Tech Corp", result.getOrganization().getName());
        assertEquals("user@techcorp.com", testUser.getBusinessEmail());
        assertEquals(testOrganization, testUser.getOrganization());

        verify(userRepository).save(testUser);
        verify(organizationRepository).save(testOrganization);
    }

    @Test
    @DisplayName("verifyAndConnect should create and connect to new organization when not exists")
    void shouldCreateAndConnectToNewOrganization() throws JsonProcessingException {
        String encodedData = java.util.Base64.getEncoder()
                .encodeToString("{\"domain\":\"newcompany.com\",\"businessEmail\":\"user@newcompany.com\",\"name\":\"New Company\",\"industry\":\"Technology\",\"size\":\"50-100\",\"location\":\"New York\"}".getBytes());
        String tokenWithData = "validtoken:" + encodedData;

        BusinessEmailVerificationToken validToken = BusinessEmailVerificationToken.builder()
                .id(1)
                .token(tokenWithData)
                .used(false)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .user(testUser)
                .build();

        Organization newOrg = Organization.builder()
                .id(2L)
                .name("New Company")
                .domain("newcompany.com")
                .industry("Technology")
                .size("50-100")
                .location("New York")
                .isVerified(true)
                .recruiters(new ArrayList<>())
                .build();

        OrganizationResponse orgResponse = OrganizationResponse.builder()
                .id(2L)
                .name("New Company")
                .domain("newcompany.com")
                .build();

        when(tokenRepository.findUnusedByTokenString("validtoken"))
                .thenReturn(Optional.of(validToken));
        when(organizationRepository.findByDomain("newcompany.com"))
                .thenReturn(Optional.empty());
        when(organizationRepository.save(any(Organization.class)))
                .thenReturn(newOrg);
        when(organizationMapper.toResponse(newOrg)).thenReturn(orgResponse);

        ConnectToOrganizationResponse result = organizationService.verifyAndConnect("validtoken");

        assertNotNull(result);
        assertTrue(result.getMessage().contains("Successfully connected"));
        assertTrue(result.isOrganizationCreated());
        assertEquals("New Company", result.getOrganization().getName());

        verify(organizationRepository, atLeast(1)).save(any(Organization.class));
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("verifyAndConnect should throw when user already connected to the same org")
    void shouldThrowWhenUserAlreadyConnectedToSameOrg() throws JsonProcessingException {
        testUser.setOrganization(testOrganization);

        String encodedData = java.util.Base64.getEncoder()
                .encodeToString("{\"domain\":\"techcorp.com\",\"businessEmail\":\"user@techcorp.com\"}".getBytes());
        String tokenWithData = "validtoken:" + encodedData;

        BusinessEmailVerificationToken validToken = BusinessEmailVerificationToken.builder()
                .id(1)
                .token(tokenWithData)
                .used(false)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .user(testUser)
                .build();

        when(tokenRepository.findUnusedByTokenString("validtoken"))
                .thenReturn(Optional.of(validToken));
        when(organizationRepository.findByDomain("techcorp.com"))
                .thenReturn(Optional.of(testOrganization));

        BusinessLogicException exception = assertThrows(
                BusinessLogicException.class,
                () -> organizationService.verifyAndConnect("validtoken")
        );

        assertTrue(exception.getMessage().contains("already connected to this organization"));
    }
}