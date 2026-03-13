package com.upply.profile.resume.analysis;

import com.upply.profile.resume.dto.ResumeFeedbackResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user/me/resume/feedback")
@Tag(name = "Resume Analysis", description = "Generative AI Resume Analysis")
public class ResumeAnalysisController {
    private final ResumeAnalysisService resumeAnalysisService;

    @GetMapping("/{resumeId}")
    @Operation(
            summary = "Get resume feedback",
            description = "Analyzes the specified resume of the authenticated user and returns AI-generated feedback and recommendations."
    )
    public ResponseEntity<ResumeFeedbackResponse> getResumeFeedback(
            @Parameter(
                    description = "The ID of the resume to analyze",
                    required = true,
                    example = "1"
            )
            @PathVariable Long resumeId){
        return ResponseEntity.ok(resumeAnalysisService.analysisProfile(resumeId));
    }

    @GetMapping("/{resumeId}/job/{jobId}")
    @Operation(
            summary = "Get resume feedback for specific job",
            description = "Analyzes how well the specified resume matches a given job posting and returns targeted feedback and match insights."
    )
    public ResponseEntity<ResumeFeedbackResponse> getResumeFeedbackForJob(
            @Parameter(
                    description = "The ID of the resume to analyze",
                    required = true,
                    example = "1"
            )
            @PathVariable Long resumeId,
            @Parameter(
                    description = "The ID of the job to compare against",
                    required = true,
                    example = "10"
            )
            @PathVariable Long jobId
    ){
        return ResponseEntity.ok(resumeAnalysisService.analysisProfileForJob(jobId,resumeId));
    }
}
