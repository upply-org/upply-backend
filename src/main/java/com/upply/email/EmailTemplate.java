package com.upply.email;

import lombok.Getter;

@Getter
public enum EmailTemplate {

    Activation("activation"),
    ResetPassword("reset_password");


    private final String name;

    EmailTemplate(final String name) {
        this.name = name;
    }
}
