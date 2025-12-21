package com.upply.job.enums;

public enum JobStatus {

    OPEN,
    PAUSED,
    CLOSED
    ;

    // response to entity
    public static JobStatus fromApiValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }

        return JobStatus.valueOf(
                value.trim().toUpperCase().replace("-", "_")
        );
    }

    // entity to response
    public String toApiValue() {
        return name().toLowerCase().replace("_", "-");
    }
}
