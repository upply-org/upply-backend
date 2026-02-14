package com.upply.profile.resume.dto;

import lombok.Builder;

import java.time.Instant;

@Builder
public record ResumeResponse(Long id,
                             String fileName,
                             String blobName,
                             Instant createdAt
                             ) {
}
