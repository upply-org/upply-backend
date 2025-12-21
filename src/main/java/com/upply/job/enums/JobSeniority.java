package com.upply.job.enums;

public enum JobSeniority {

    JUNIOR,
    MID,
    SENIOR,
    LEAD
    ;

    // response to entity
    public static JobSeniority fromApiValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }

        return JobSeniority.valueOf(
                value.trim().toUpperCase().replace("-", "_")
        );
    }

    // entity to response
    public String toApiValue() {
        return name().toLowerCase().replace("_", "-");
    }
}
