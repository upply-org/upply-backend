package com.upply.application.dto;

import com.upply.application.Application;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.upply.profile.socialLink.dto.SocialLinkMapper;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class ApplicationMapper {
    private final SocialLinkMapper socialLinkMapper;
    public Application toApplication(ApplicationRequest applicationRequest){
        return Application.builder()
                .coverLetter(applicationRequest.coverLetter())
                .build();
    }

    public ApplicationResponse toApplicationResponse(Application application){
        return ApplicationResponse.builder()
                .id(application.getId())
                .applicantFullName(application.getApplicant().getFullName())
                .applicantEmail(application.getApplicant().getEmail())
                .resumeId(application.getResume().getId())
                .applicantSocialLinks(application.getApplicant().getSocialLinks() != null ? application.getApplicant().getSocialLinks()
                        .stream()
                        .map(socialLinkMapper::toSocialLinkResponse)
                        .toList() : Collections.emptyList())

                .university(application.getApplicant().getUniversity())
                .jobId(application.getJob().getId())
                .jobTitle(application.getJob().getTitle())
                    // TO-DO get a resume file.
                .coverLetter(application.getCoverLetter())
                .status(application.getStatus())
                .matchingRatio(application.getMatchingRatio())
                .build();
    }
}
