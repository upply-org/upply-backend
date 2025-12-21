package com.upply.job.enums;

public enum JobModel {

    ONSITE,
    HYBRID,
    REMOTE
    ;

    public static JobModel fromApiValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }

        return JobModel.valueOf(
                value.trim().toUpperCase().replace("-", "_")
        );
    }

    // entity to response
    public String toApiValue() {
        return name().toLowerCase().replace("_", "-");
    }

}
