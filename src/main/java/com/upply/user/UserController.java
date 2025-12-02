package com.upply.user;

import com.upply.skill.SkillRequest;
import com.upply.skill.SkillResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user/me")
@Tag(name = "User", description = "APIs for managing authenticated user profile and skills")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(
            summary = "Get authenticated user profile",
            description = "Retrieves the complete profile information of the currently authenticated user, including personal details and associated skills."
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
}
