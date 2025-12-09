package com.upply.profile.socialLink.dto;

import com.upply.profile.socialLink.SocialLink;
import org.springframework.stereotype.Service;

@Service
public class SocialLinkMapper {
    public SocialLink toSocialLink(SocialLinkRequest socialLinkRequest){
        return SocialLink.builder()
                .url(socialLinkRequest.url())
                .socialType(socialLinkRequest.socialType())
                .build();
    }

    public SocialLinkResponse toSocialLinkResponse(SocialLink socialLink){
        return SocialLinkResponse.builder()
                .id(socialLink.getId())
                .url(socialLink.getUrl())
                .socialType(socialLink.getSocialType())
                .build();
    }
}
