package com.upply.profile.skill;

import com.upply.profile.skill.dto.SkillRequest;
import com.upply.profile.skill.dto.SkillResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/skills")
@Tag(name = "Skills", description = "APIs for managing skills in the system")
@RequiredArgsConstructor
public class SkillController {
    private final SkillService skillService;

    @PostMapping
    @Operation(
            summary = "Create a new skill",
            description = "Adds a new skill to the system. The skill name and category are required."
    )
    public ResponseEntity<Long> addSkill(@Valid @RequestBody SkillRequest skillRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(skillService.addSkill(skillRequest));
    }

    @GetMapping
    @Operation(
            summary = "Get all skills",
            description = "Retrieves a list of all skills available in the system."
    )
    public ResponseEntity<List<SkillResponse>> getAllSkills() {
        return ResponseEntity.ok(skillService.getAllSkills());
    }

    @GetMapping("/name")
    @Operation(
            summary = "Get skill by name",
            description = "Retrieves a skill by its name. The search is case-insensitive and handles spaces."
    )
    public ResponseEntity<SkillResponse> getSkillsByName(
            @Parameter(
                    description = "The name of the skill to search for",
                    required = true,
                    example = "Java Programming"
            )
            @Valid @RequestParam String name) {
        return ResponseEntity.ok(skillService.getSkillsByName(name));
    }

    @PutMapping("/update/{skillId}")
    @Operation(
            summary = "Update an existing skill",
            description = "Updates the name and/or category of an existing skill identified by its ID."
    )
    public ResponseEntity<Void> updateSkill(
            @Parameter(
                    description = "The ID of the skill to update",
                    required = true,
                    example = "1"
            )
            @PathVariable Long skillId,
            @Valid @RequestBody SkillRequest skillRequest) {
        skillService.updateSkill(skillId, skillRequest);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
