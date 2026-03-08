package com.upply.profile.resume.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.upply.profile.resume.enums.ResumeSection;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ResumeFeedbackResponse(
        String summary,
        Map<ResumeSection, Integer> score,
        Map<ResumeSection, String> feedback,
        Map<ResumeSection, String> fixes,
        List<String> topStrengths,
        List<String> quickWins,
        List<String> matchedSkills,
        List<String> missingSkills,
        String topTip
) {
    /**
     * Job-specific fields are all null.
     */
    public static ResumeFeedbackResponse generic(
            String summary,
            Map<ResumeSection, Integer> scores,
            Map<ResumeSection, String> feedback,
            Map<ResumeSection, String> fixes,
            List<String> topStrengths,
            List<String> quickWins) {

        return new ResumeFeedbackResponse(
                summary, scores, feedback, fixes,
                topStrengths, quickWins,
                null,  null, null   // job-specific fields → null
        );
    }

    /**
     * All fields populated.
     */
    public static ResumeFeedbackResponse forJob(
            String summary,
            Map<ResumeSection, Integer> scores,
            Map<ResumeSection, String> feedback,
            Map<ResumeSection, String> fixes,
            List<String> topStrengths,
            List<String> quickWins,
            List<String> matchedSkills,
            List<String> missingSkills,
            String topTip) {

        return new ResumeFeedbackResponse(
                summary, scores, feedback, fixes,
                topStrengths, quickWins,
                matchedSkills, missingSkills, topTip
        );
    }


}
