package com.upply.application.dto;

import com.upply.application.enums.ApplicationStatus;
import com.upply.profile.socialLink.dto.SocialLinkResponse;
import lombok.Builder;

import java.util.List;

@Builder
public record ApplicationResponse(
        Long id,

        String applicantFullName,

        String applicantEmail,

        String university,

        List<SocialLinkResponse> applicantSocialLinks,

        Long jobId,

        String jobTitle,

        String coverLetter,

        Long resumeId,

        ApplicationStatus status,

        double matchingRatio
) {
}
