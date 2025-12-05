package com.upply.user;

import com.upply.experience.ExperienceRequest;
import com.upply.experience.ExperienceResponse;
import com.upply.project.ProjectRequest;
import com.upply.project.ProjectResponse;
import com.upply.skill.SkillRequest;
import com.upply.skill.SkillResponse;
import com.upply.socialLink.SocialLinkRequest;
import com.upply.socialLink.SocialLinkResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @PathVariable Long projectId){
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
            @PathVariable Long projectId, @Valid @RequestBody ProjectRequest projectRequest){
        userService.updateUserProject(projectId,projectRequest);
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
            @PathVariable Long projectId){
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
            @PathVariable Long socialId){
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
            @PathVariable Long socialId, @Valid @RequestBody SocialLinkRequest socialLinkRequest){
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
            @PathVariable Long socialId){
        userService.deleteUserLinks(socialId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
