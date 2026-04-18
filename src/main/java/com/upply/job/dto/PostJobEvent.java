package com.upply.job.dto;

import com.upply.job.enums.JobSource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostJobEvent {
    private Long jobId;
    private JobSource jobSource;
    private Long userId;
    private ImportJobRequest importJobRequest;
}
