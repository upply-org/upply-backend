package com.upply.user;

import com.upply.profile.experience.dto.ExperienceRequest;
import com.upply.profile.experience.dto.ExperienceResponse;
import com.upply.profile.project.dto.ProjectRequest;
import com.upply.profile.project.dto.ProjectResponse;
import com.upply.profile.resume.dto.ResumeResponse;
import com.upply.profile.skill.dto.SkillRequest;
import com.upply.profile.skill.dto.SkillResponse;
import com.upply.profile.socialLink.dto.SocialLinkRequest;
import com.upply.profile.socialLink.dto.SocialLinkResponse;
import com.upply.user.dto.UserRequest;
import com.upply.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user/me")
@Tag(name = "User", description = "APIs for managing authenticated user profile, skills, and experiences")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(
            summary = "Get authenticated user profile",
            description = "Retrieves the complete profile information of the currently authenticated user, including personal details, associated skills, and experiences."
    )
    public ResponseEntity<UserResponse> getUser() {
        return ResponseEntity.ok(userService.getUser());
    }

    @PutMapping
    @Operation(
            summary = "Update user profile",
            description = "Updates the authenticated user's profile information including first name, last name, and university."
    )
    public ResponseEntity<Void> updateUser(@Valid @RequestBody UserRequest userRequest) {
        userService.updateUser(userRequest);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    //skills

    @GetMapping("/skills")
    @Operation(
            summary = "Get user's skills",
            description = "Retrieves all skills associated with the authenticated user's profile."
    )
    public ResponseEntity<Set<SkillResponse>> getUserSkills() {
        return ResponseEntity.ok(userService.getUserSkills());
    }

    @PostMapping("/skills/{skillId}")
    @Operation(
            summary = "Add skill to user by ID",
            description = "Adds an existing skill to the authenticated user's profile by skill ID."
    )
    public ResponseEntity<Void> addSkillsToUser(
            @Parameter(
                    description = "The ID of the skill to add to the user",
                    required = true,
                    example = "1"
            )
            @PathVariable Long skillId) {
        userService.addSkillToUser(skillId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/skills")
    @Operation(
            summary = "Add skill to user by name",
            description = "Adds a skill to the authenticated user's profile by skill name. If the skill doesn't exist, it will be created automatically."
    )
    public ResponseEntity<Void> addSkillToUserByName(@Valid @RequestBody SkillRequest skillRequest) {
        userService.addSkillByName(skillRequest);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/skills/{skillId}")
    @Operation(
            summary = "Remove skill from user",
            description = "Removes a skill from the authenticated user's profile by skill ID."
    )
    public ResponseEntity<Void> removeSkillFromUser(
            @Parameter(
                    description = "The ID of the skill to remove from the user",
                    required = true,
                    example = "1"
            )
            @PathVariable Long skillId) {
        userService.removeSkillFromUser(skillId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    //experience

    @GetMapping("/experiences")
    @Operation(
            summary = "Get user's experiences",
            description = "Retrieves all work experiences associated with the authenticated user's profile."
    )
    public ResponseEntity<List<ExperienceResponse>> getUserExperiences() {
        return ResponseEntity.ok(userService.getUserExperience());
    }

    @GetMapping("/experiences/{experienceId}")
    @Operation(
            summary = "Get user experience by ID",
            description = "Retrieves a specific work experience for the authenticated user by experience ID."
    )
    public ResponseEntity<ExperienceResponse> getUserExperienceById(
            @Parameter(
                    description = "The ID of the experience to retrieve",
                    required = true,
                    example = "1"
            )
            @PathVariable Long experienceId) {
        return ResponseEntity.ok(userService.getUserExperienceById(experienceId));
    }

    @PostMapping("/experiences")
    @Operation(
            summary = "Add experience to user",
            description = "Adds a new work experience to the authenticated user's profile. Returns the ID of the created experience."
    )
    public ResponseEntity<Long> addUserExperience(@Valid @RequestBody ExperienceRequest experienceRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.addUserExperience(experienceRequest));
    }

    @PutMapping("/experiences/{experienceId}")
    @Operation(
            summary = "Update user experience",
            description = "Updates an existing work experience for the authenticated user by experience ID."
    )
    public ResponseEntity<Void> updateUserExperience(
            @Parameter(
                    description = "The ID of the experience to update",
                    required = true,
                    example = "1"
            )
            @PathVariable Long experienceId,
            @Valid @RequestBody ExperienceRequest experienceRequest) {
        userService.updateUserExperience(experienceId, experienceRequest);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/experiences/{experienceId}")
    @Operation(
            summary = "Delete user experience",
            description = "Removes a work experience from the authenticated user's profile by experience ID."
    )
    public ResponseEntity<Void> deleteUserExperience(
            @Parameter(
                    description = "The ID of the experience to delete",
                    required = true,
                    example = "1"
            )
            @PathVariable Long experienceId) {
        userService.deleteUserExperience(experienceId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // project

    @GetMapping("/projects")
    @Operation(
            summary = "Get user's projects",
            description = "Retrieves all projects associated with the authenticated user's profile."
    )
    public ResponseEntity<List<ProjectResponse>> getAllUserProjects() {
        return ResponseEntity.ok(userService.getUserProject());
    }

    @GetMapping("/projects/{projectId}")
    @Operation(
            summary = "Get user project by ID",
            description = "Retrieves a specific project for the authenticated user by project ID."
    )
    public ResponseEntity<ProjectResponse> getUserProjectById(
            @Parameter(
                    description = "The ID of the project to retrieve",
                    required = true,
                    example = "1"
            )
            @PathVariable Long projectId) {
        return ResponseEntity.ok(userService.getUserProjectById(projectId));
    }

    @PostMapping("/projects")
    @Operation(
            summary = "Add project to user",
            description = "Adds a new project to the authenticated user's profile. Returns the ID of the created project."
    )
    public ResponseEntity<Long> addUserProjects(@Valid @RequestBody ProjectRequest projectRequest) {

        return ResponseEntity.status(HttpStatus.CREATED).body(userService.addUserProject(projectRequest));
    }

    @PutMapping("/projects/{projectId}")
    @Operation(
            summary = "Update user project",
            description = "Updates an existing project for the authenticated user by project ID."
    )
    public ResponseEntity<Void> updateUserProject(
            @Parameter(
                    description = "The ID of the project to update",
                    required = true,
                    example = "1"
            )
            @PathVariable Long projectId, @Valid @RequestBody ProjectRequest projectRequest) {
        userService.updateUserProject(projectId, projectRequest);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/projects/{projectId}")
    @Operation(
            summary = "Delete user project",
            description = "Removes a project from the authenticated user's profile by project ID."
    )
    public ResponseEntity<Void> deleteUserProject(
            @Parameter(
                    description = "The ID of the project to delete",
                    required = true,
                    example = "1"
            )
            @PathVariable Long projectId) {
        userService.deleteUserProject(projectId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    //social Links

    @GetMapping("/social-links")
    @Operation(
            summary = "Get user's social links",
            description = "Retrieves all social links associated with the authenticated user's profile."
    )
    public ResponseEntity<List<SocialLinkResponse>> getAllUserSocial() {
        return ResponseEntity.ok(userService.getUserSocialLinks());
    }

    @GetMapping("/social-links/{socialId}")
    @Operation(
            summary = "Get user social link by ID",
            description = "Retrieves a specific social link for the authenticated user by social link ID."
    )
    public ResponseEntity<SocialLinkResponse> getUserSocialById(
            @Parameter(
                    description = "The ID of the social link to retrieve",
                    required = true,
                    example = "1"
            )
            @PathVariable Long socialId) {
        return ResponseEntity.ok(userService.getUserSocialLinkById(socialId));
    }

    @PostMapping("/social-links")
    @Operation(
            summary = "Add social link to user",
            description = "Adds a new social link to the authenticated user's profile. Returns the ID of the created social link."
    )
    public ResponseEntity<Long> addUserSocial(@Valid @RequestBody SocialLinkRequest socialLinkRequest) {

        return ResponseEntity.status(HttpStatus.CREATED).body(userService.addUserSocialLinks(socialLinkRequest));
    }

    @PutMapping("/social-links/{socialId}")
    @Operation(
            summary = "Update user social link",
            description = "Updates an existing social link for the authenticated user by social link ID."
    )
    public ResponseEntity<Void> updateUserSocial(
            @Parameter(
                    description = "The ID of the social link to update",
                    required = true,
                    example = "1"
            )
            @PathVariable Long socialId, @Valid @RequestBody SocialLinkRequest socialLinkRequest) {
        userService.updateUserSocialLinks(socialId, socialLinkRequest);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/social-links/{socialId}")
    @Operation(
            summary = "Delete user social link",
            description = "Removes a social link from the authenticated user's profile by social link ID."
    )
    public ResponseEntity<Void> deleteUserSocial(
            @Parameter(
                    description = "The ID of the social link to delete",
                    required = true,
                    example = "1"
            )
            @PathVariable Long socialId) {
        userService.deleteUserLinks(socialId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    //resume

    @PostMapping(value = "/resume", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload user resume",
            description = "Uploads a new resume (PDF) for the authenticated user. The resume file is stored and parsed for text content."
    )
    public ResponseEntity<ResumeResponse> addUserResume(
            @Parameter(
                    description = "The resume file to upload (PDF format)",
                    required = true
            )
            @RequestParam("file") MultipartFile resume) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.addUserResume(resume));
    }

    @GetMapping("/resume")
    @Operation(
            summary = "Get all user resumes",
            description = "Retrieves all resumes associated with the authenticated user's profile."
    )
    public ResponseEntity<List<ResumeResponse>> getAllUserResumes() {
        return ResponseEntity.ok(userService.getAllUserResumes());
    }

    @GetMapping("/resume/last")
    @Operation(
            summary = "Get last submitted resume",
            description = "Retrieves the most recently submitted resume for the authenticated user."
    )
    public ResponseEntity<ResumeResponse> getLastSubmittedResume() {
        return ResponseEntity.ok(userService.getLastSubmittedResume());
    }

    @GetMapping("/resume/view/{resumeId}")
    @Operation(
            summary = "View user resume",
            description = "Returns the resume PDF file for inline viewing in the browser by resume ID."
    )
    public ResponseEntity<byte[]> viewUserResume(
            @Parameter(
                    description = "The ID of the resume to view",
                    required = true,
                    example = "1"
            )
            @PathVariable Long resumeId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.builder("inline")
                        .build()
        );
        return ResponseEntity.ok()
                .headers(headers)
                .body(userService.getResumeFileById(resumeId));
    }

    @GetMapping("/resume/download/{resumeId}")
    @Operation(
            summary = "Download user resume",
            description = "Downloads the resume PDF file as an attachment by resume ID."
    )
    public ResponseEntity<byte[]> downloadUSerResume(
            @Parameter(
                    description = "The ID of the resume to download",
                    required = true,
                    example = "1"
            )
            @PathVariable Long resumeId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.builder("attachment")
                        .filename(userService.getFileName(resumeId))
                        .build()
        );
        return ResponseEntity.ok()
                .headers(headers)
                .body(userService.getResumeFileById(resumeId));
    }

    @DeleteMapping("/resume/{resumeId}")
    @Operation(
            summary = "Delete user resume",
            description = "Removes a resume from the authenticated user's profile by resume ID."
    )
    public ResponseEntity<Void> deleteUserResume(
            @Parameter(
                    description = "The ID of the resume to delete",
                    required = true,
                    example = "1"
            )
            @PathVariable Long resumeId) {
        userService.deleteUserResume(resumeId);
        return ResponseEntity.noContent().build();
    }

    // device token

    @PostMapping("/device-token/{token}")
    public ResponseEntity<Void> saveDeviceToken(@PathVariable String token){
        userService.saveDeviceToken(token);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
