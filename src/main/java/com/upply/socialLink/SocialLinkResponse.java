package com.upply.socialLink;

import lombok.Builder;

@Builder
public record SocialLinkResponse(
        Long id,
        String url,
        SocialType socialType
) {
}
