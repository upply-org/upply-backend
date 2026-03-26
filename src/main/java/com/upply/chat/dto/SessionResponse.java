package com.upply.chat.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record SessionResponse(String sessionId,
                              Long jobId,
                              String title,
                              LocalDateTime createdAt) {
}
