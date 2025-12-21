package com.upply.job.dto;


import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobListResponse {

    private String title;
    private String organizationName;

    private String type;
    private String seniority;
    private String model;
    private String status;

    private String location;

    private Instant createdDate;

}
