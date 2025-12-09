package com.upply.profile.socialLink.dto;

import com.upply.profile.socialLink.SocialType;
import jakarta.validation.constraints.NotBlank;

public record SocialLinkRequest(
        @NotBlank
        String url,
        SocialType socialType
) {
}
