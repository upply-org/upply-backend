package com.upply.job.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobUpdateRequest {

    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    private String title;


    private String type;
    private String seniority;
    private String model;

    @Size(max = 150, message = "Location must not exceed 150 characters")
    private String location;

    @Size(min = 20, max = 5000, message = "Description must be between 20 and 2000 characters")
    private String description;

    @Size(min = 1, message = "At least one skill is required")
    private Set<Long> skillIds;
}
