package com.upply.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record StreamChatRequest(
        @NotBlank(message = "prompt is required")
        @Size(max = 5000)
        String prompt
)
{}
