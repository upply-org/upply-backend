package com.upply.job.enums;

public enum JobSource {

    INTERNAL, // posted by recruiter in the platform
    EXTERNAL  // imported by admins from job description text
    ;

    public static JobSource fromApiValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }

        return JobSource.valueOf(
                value.trim().toUpperCase().replace("-", "_")
        );
    }

    public String toApiValue() {
        return name().toLowerCase().replace("_", "-");
    }
}
