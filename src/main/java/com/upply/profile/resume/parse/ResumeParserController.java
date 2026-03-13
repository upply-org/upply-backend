package com.upply.profile.resume.parse;

import com.upply.profile.resume.dto.ParseConfirmRequest;
import com.upply.profile.resume.dto.ParsedResumeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/me/resume/parse")
@RequiredArgsConstructor
@Tag(name = "Profile Auto Fill", description = "resume parser to auto-fill user profile data")
public class ResumeParserController {
    private final ResumeParserService resumeParserService;

    @GetMapping("/{resumeId}/preview")
    @Operation(
            summary = "Preview parsed resume data",
            description = "Extracts structured data from the specified resume using AI and returns a preview of the parsed fields. No changes are applied to the user's profile."
    )
    public ResponseEntity<ParsedResumeResponse> parsePreview(
            @Parameter(
                    description = "The ID of the resume to parse",
                    required = true,
                    example = "1"
            )
            @PathVariable Long resumeId
    ){
        return ResponseEntity.ok(resumeParserService.preview(resumeId));
    }

    @PostMapping("/{resumeId}/confirm")
    @Operation(
            summary = "Confirm and apply parsed resume data",
            description = "Applies the AI-parsed resume data to the authenticated user's profile. The request body controls which sections to apply (personal info, experiences, projects, social links) and which skills to add."
    )
    public ResponseEntity<Void> parseConfirm(
            @Parameter(
                    description = "The ID of the resume to apply parsed data from",
                    required = true,
                    example = "1"
            )
            @PathVariable Long resumeId,
            @Valid @RequestBody ParseConfirmRequest request
            ){
        resumeParserService.confirm(resumeId, request);
        return ResponseEntity.ok().build();
    }
}
