package com.upply.job.dto;

import com.upply.job.enums.JobModel;
import com.upply.job.enums.JobSeniority;
import com.upply.job.enums.JobStatus;
import com.upply.job.enums.JobType;
import com.upply.profile.skill.dto.SkillResponse;
import lombok.*;

import java.time.Instant;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobResponse {

    private String title;
    private String organizationName;

    private String type;
    private String seniority;
    private String model;
    private String status;

    private String location;
    private String description;
    private Instant createdDate;
    private Set<SkillResponse> skills;
}

