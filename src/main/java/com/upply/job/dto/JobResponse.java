package com.upply.job.dto;

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
    private Long id;
    private String title;
    private String organizationName;

    private String type;
    private String seniority;
    private String model;
    private String status;
    private String jobSource;

    private String location;
    private String description;
    private Instant createdDate;
    private Set<SkillResponse> skills;
    private String applicationLink;
}
