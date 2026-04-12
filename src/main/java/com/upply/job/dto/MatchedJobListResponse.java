package com.upply.job.dto;

import com.upply.job.enums.JobSource;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchedJobListResponse {

    private Long id;
    private String title;
    private String organizationName;

    private String type;
    private String seniority;
    private String model;
    private String status;
    private String jobSource;

    private String location;

    private Instant createdDate;

    private Double matchScore;
    private Integer matchPercentage;

}
