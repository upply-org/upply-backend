package com.upply.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record UserRequest(
        @NotBlank
        String firstName,
        @NotBlank
        String lastName,

        String university
) {
}
