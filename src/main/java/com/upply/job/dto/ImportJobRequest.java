package com.upply.job.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportJobRequest {

    @NotBlank(message = "Description text is required")
    @Size(max = 15000, message = "Description text must not exceed 15000 characters")
    private String descriptionText;
}
