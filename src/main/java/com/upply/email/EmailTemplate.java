package com.upply.email;

import lombok.Getter;

@Getter
public enum EmailTemplate {

    Activation("activation"),
    ResetPassword("reset_password"),
    JOB_APPLICATION_UPDATED("job_application_update"),
    JOB_APPLICATION_SUBMITTED("job_application_submitted"),
    JOB_POSTED_SUCCESSFULLY("job_posted_successfully"),
    NEW_MATCHED_JOBS("new_matched_jobs");

    private final String name;

    EmailTemplate(final String name) {
        this.name = name;
    }
}
