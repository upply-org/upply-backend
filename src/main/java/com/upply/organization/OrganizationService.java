package com.upply.organization;

import com.upply.common.EmailDomainValidator;
import com.upply.common.PageResponse;
import com.upply.email.EmailService;
import com.upply.email.EmailTemplate;
import com.upply.exception.custom.BusinessLogicException;
import com.upply.exception.custom.OperationNotPermittedException;
import com.upply.exception.custom.ResourceNotFoundException;
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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Validated
@Transactional(readOnly = true)
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMapper organizationMapper;
    private final JobMapper jobMapper;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final BusinessEmailVerificationTokenRepository tokenRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.business-email-verification-base-url}")
    private String verificationBaseUrl;

    public OrganizationResponse getOrganization(Long id) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization with ID " + id + " not found"));

        return organizationMapper.toResponse(organization);
    }

    @Transactional
    public OrganizationResponse updateOrganization(Long id, @Valid OrganizationUpdateRequest request, Authentication connectedUser) {

        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization with ID " + id + " not found"));

        User user = (User) connectedUser.getPrincipal();

        boolean isRecruiter = organization.getRecruiters().stream()
                .anyMatch(r -> r.getId().equals(user.getId()));

        if (!isRecruiter) {
            throw new OperationNotPermittedException("You are not permitted to update this organization");
        }

        organizationMapper.updateFromRequest(request, organization);

        Organization savedOrganization = organizationRepository.save(organization);

        return organizationMapper.toResponse(savedOrganization);
    }

    public PageResponse<JobListResponse> getOrganizationOpenJobs(Long orgId, int pageNumber, int size) {

        if (!organizationRepository.existsById(orgId)) {
            throw new ResourceNotFoundException("Organization with ID " + orgId + " not found");
        }

        Pageable pageable = PageRequest.of(pageNumber, size, Sort.by("createdDate").descending());

        var jobs = organizationRepository.findJobsByOrganizationIdAndStatus(orgId, JobStatus.OPEN, pageable);

        List<JobListResponse> jobResponses = jobs.stream()
                .map(jobMapper::toJobListResponse)
                .toList();

        return new PageResponse<>(
                jobResponses,
                jobs.getNumber(),
                jobs.getSize(),
                jobs.getTotalElements(),
                jobs.getTotalPages(),
                jobs.isFirst(),
                jobs.isLast());
    }

    public PageResponse<JobListResponse> getOrganizationJobsByStatus(Long orgId, JobStatus status, int pageNumber, int size) {

        if (!organizationRepository.existsById(orgId)) {
            throw new ResourceNotFoundException("Organization with ID " + orgId + " not found");
        }

        Pageable pageable = PageRequest.of(pageNumber, size, Sort.by("createdDate").descending());

        var jobs = organizationRepository.findJobsByOrganizationIdAndStatus(orgId, status, pageable);

        List<JobListResponse> jobResponses = jobs.stream()
                .map(jobMapper::toJobListResponse)
                .toList();

        return new PageResponse<>(
                jobResponses,
                jobs.getNumber(),
                jobs.getSize(),
                jobs.getTotalElements(),
                jobs.getTotalPages(),
                jobs.isFirst(),
                jobs.isLast());
    }

    @Transactional
    public ConnectToOrganizationResponse initiateConnection(
            @Valid ConnectToOrganizationRequest request, User user) throws JsonProcessingException {

        String businessEmail = request.getBusinessEmail().toLowerCase().trim();

        if (EmailDomainValidator.isPublicDomain(businessEmail)) {
            throw new BusinessLogicException(
                    "Public email domains are not allowed. Please use your corporate email address.");
        }

        String domain = EmailDomainValidator.extractDomain(businessEmail);

        var existingOrg = organizationRepository.findByDomain(domain);

        if (existingOrg.isPresent()) {
            Organization org = existingOrg.get();
            boolean isAlreadyRecruiter = organizationRepository.existsRecruiterInOrganization(org.getId(), user.getId());

            if (isAlreadyRecruiter) {
                throw new BusinessLogicException("You are already connected to this organization.");
            }

            sendVerificationEmail(user, businessEmail, domain, null);

            return ConnectToOrganizationResponse.builder()
                    .message("Verification email sent. Please check your inbox to connect to existing organization.")
                    .organizationCreated(false)
                    .organization(null)
                    .build();
        }

        if (request.getOrganization() == null) {
            throw new BusinessLogicException(
                    "Organization details are required since no organization exists for this domain.");
        }

        ConnectToOrganizationRequest.OrganizationDetails orgDetails = request.getOrganization();

        sendVerificationEmail(user, businessEmail, domain, orgDetails);

        return ConnectToOrganizationResponse.builder()
                .message("Verification email sent. Please check your inbox to create new organization.")
                .organizationCreated(false)
                .organization(null)
                .build();
    }

    private void sendVerificationEmail(User user, String businessEmail, String domain,
                                       ConnectToOrganizationRequest.OrganizationDetails orgDetails) throws JsonProcessingException {
        tokenRepository.markAllTokensAsUsedForUser(user.getId());

        String tokenString = generateToken();
        String encodedData = encodeOrgData(domain, orgDetails, businessEmail);

        BusinessEmailVerificationToken token = BusinessEmailVerificationToken.builder()
                .token(tokenString + ":" + encodedData)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(24))
                .used(false)
                .user(user)
                .build();

        tokenRepository.save(token);

        String fullToken = tokenString + ":" + encodedData;
        String verificationLink = verificationBaseUrl + "?token=" + fullToken;

        Map<String, Object> vars = new HashMap<>();
        vars.put("firstName", user.getFirstName());
        vars.put("verificationLink", verificationLink);

        emailService.sendEmail(
                businessEmail,
                "Verify Your Business Email",
                EmailTemplate.BUSINESS_EMAIL_VERIFICATION,
                vars);
    }

    private String generateToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String encodeOrgData(String domain, ConnectToOrganizationRequest.OrganizationDetails orgDetails, String businessEmail) throws JsonProcessingException {
        Map<String, String> data = new HashMap<>();
        data.put("domain", domain);
        data.put("businessEmail", businessEmail);
        if (orgDetails != null) {
            data.put("name", orgDetails.getName());
            data.put("industry", orgDetails.getIndustry());
            data.put("size", orgDetails.getSize());
            data.put("location", orgDetails.getLocation());
        }
        String json = objectMapper.writeValueAsString(data);
        return Base64.getEncoder().encodeToString(json.getBytes());
    }

    private Map<String, String> decodeOrgData(String encodedData) throws JsonProcessingException {
        String json = new String(Base64.getDecoder().decode(encodedData));
        return objectMapper.readValue(json, new TypeReference<Map<String, String>>() {});
    }

    @Transactional
    public ConnectToOrganizationResponse verifyAndConnect(String token) throws JsonProcessingException {
        BusinessEmailVerificationToken verificationToken = tokenRepository.findUnusedByTokenString(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid verification token"));

        if (verificationToken.isUsed()) {
            throw new BusinessLogicException("Verification token has already been used");
        }

        if (verificationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessLogicException("Verification token has expired");
        }

        String[] tokenParts = verificationToken.getToken().split(":", 2);
        if (tokenParts.length != 2) {
            throw new BusinessLogicException("Invalid verification token format");
        }

        String encodedData = tokenParts[1];
        Map<String, String> orgData = decodeOrgData(encodedData);
        String domain = orgData.get("domain");
        String businessEmail = orgData.get("businessEmail");

        boolean[] organizationCreated = {false};
        Organization organization = organizationRepository.findByDomain(domain)
                .orElseGet(() -> {
                    organizationCreated[0] = true;
                    return createNewOrganization(orgData);
                });

        User user = verificationToken.getUser();

        if (user.getOrganization() != null && user.getOrganization().getId().equals(organization.getId())) {
            throw new BusinessLogicException("You are already connected to this organization");
        }

        user.setOrganization(organization);
        user.setBusinessEmail(businessEmail);
        userRepository.save(user);

        organization.getRecruiters().add(user);
        organizationRepository.save(organization);

        verificationToken.setUsed(true);
        tokenRepository.save(verificationToken);

        return ConnectToOrganizationResponse.builder()
                .message("Successfully connected to " + organization.getName())
                .organizationCreated(organizationCreated[0])
                .organization(organizationMapper.toResponse(organization))
                .build();
    }

    private Organization createNewOrganization(Map<String, String> orgData) {
        Organization organization = Organization.builder()
                .name(orgData.get("name"))
                .domain(orgData.get("domain"))
                .industry(orgData.get("industry"))
                .size(orgData.get("size"))
                .location(orgData.get("location"))
                .isVerified(true)
                .build();

        return organizationRepository.save(organization);
    }
}
