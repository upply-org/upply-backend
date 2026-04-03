package com.upply.organization.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationResponse {
    private Long id;
    private String name;
    private String domain;
    private String description;
    private String website;
    private String logoUrl;
    private String industry;
    private String size;
    private String location;
    private boolean isVerified;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
}
