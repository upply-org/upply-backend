package com.upply.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

@Builder
public record ApplicationRequest(

        @JsonIgnore
        @Schema(hidden = true)
        String resume,

        @NotNull(message = "Job ID is required")
        @Min(value = 1, message = "Job ID must be greater than 0")
        Long jobId,

        @Size(max = 5000, message = "Cover letter must not exceed 5000 characters")
        String coverLetter
) {
}
