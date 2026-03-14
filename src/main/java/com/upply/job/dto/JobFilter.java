package com.upply.job.dto;

import com.upply.job.enums.JobModel;
import com.upply.job.enums.JobSeniority;
import com.upply.job.enums.JobType;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobFilter {

    @Size(max = 100)
    private String keyword;

    private JobType type;
    private JobSeniority seniority;
    private JobModel model;

    @Size(max = 100)
    private String location;
}
