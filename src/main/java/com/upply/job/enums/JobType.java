package com.upply.job.enums;

public enum JobType {

    FULL_TIME,
    PART_TIME,
    INTERNSHIP
    ;

    // response to entity
    public static JobType fromApiValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }

        return JobType.valueOf(
                value.trim().toUpperCase().replace("-", "_")
        );
    }

    // entity to response
    public String toApiValue() {
        return name().toLowerCase().replace("_", "-");
    }
}
