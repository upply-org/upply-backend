package com.upply.user;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record UserRequest(
        @NotNull
        String firstName,
        @NotNull
        String lastName,

        String university
) {
}
