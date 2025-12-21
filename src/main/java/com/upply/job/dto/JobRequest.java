package com.upply.job.dto;

import com.upply.job.enums.JobModel;
import com.upply.job.enums.JobSeniority;
import com.upply.job.enums.JobType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;


import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    private String title;


    @NotBlank(message = "Type is required")
    private String type;

    @NotBlank(message = "Seniority is required")
    private String seniority;

    @NotBlank(message = "Model is required")
    private String model;


    @NotBlank(message = "Location is required")
    @Size(max = 150, message = "Location must not exceed 150 characters")
    private String location;

    @NotBlank(message = "Description is required")
    @Size(min = 20, max = 5000, message = "Description must be between 20 and 5000 characters")
    private String description;

    @NotEmpty(message = "At least one skill is required")
    private Set<Long> skillIds;
}