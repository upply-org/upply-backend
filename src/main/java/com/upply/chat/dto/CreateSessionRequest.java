package com.upply.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CreateSessionRequest(
        @NotNull(message = "jobId is required")
        Long jobId,

        @NotBlank(message = "firstPrompt is required")
        @Size(max = 5000)
        String firstPrompt
) {
}
