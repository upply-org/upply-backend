package com.upply.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
