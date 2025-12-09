package com.upply.profile.socialLink.dto;

import com.upply.profile.socialLink.SocialType;
import lombok.Builder;

@Builder
public record SocialLinkResponse(
        Long id,
        String url,
        SocialType socialType
) {
}
