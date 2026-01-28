package com.upply.job.dto;

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

    private String location;

    private Instant createdDate;

    private Double matchScore;
    private Integer matchPercentage;

}
