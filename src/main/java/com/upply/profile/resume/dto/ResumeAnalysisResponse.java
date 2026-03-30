package com.upply.profile.resume.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.upply.profile.resume.enums.ResumeSection;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ResumeAnalysisResponse(
        String summary,
        Map<ResumeSection, Integer> score,
        Map<ResumeSection, String> feedback,
        Map<ResumeSection, String> fixes,
        List<String> topStrengths,
        List<String> quickWins,
        Integer jobMatchScore,
        List<String> matchedSkills,
        List<String> missingSkills,
        String topTip,
        String extractionQuality,
        String extractionWarning
) {
}
