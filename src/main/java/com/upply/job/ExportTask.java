package com.upply.job;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;

@Getter
@Setter
public class ExportTask {

    public enum Status {
        PROCESSING, COMPLETED, FAILED
    }

    private final String taskId;
    private final Long jobId;
    private volatile Status status;
    private volatile byte[] data;
    private String errorMessage;
    private final Instant createdAt;
    private final Instant expireAt;

    public ExportTask(String taskId, Long jobId, int taskExportTime) {
        this.taskId = taskId;
        this.jobId = jobId;
        this.status = Status.PROCESSING;
        this.createdAt = Instant.now();
        this.expireAt = Instant.now().plusSeconds(taskExportTime);
    }
}
