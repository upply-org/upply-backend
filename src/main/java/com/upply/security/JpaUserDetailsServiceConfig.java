package com.upply.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.data.repository.query.SecurityEvaluationContextExtension;
import org.springframework.stereotype.Component;

@Configuration
public class JpaUserDetailsServiceConfig {
    @Bean
    public SecurityEvaluationContextExtension securityEvaluationContextExtension() {
        return new SecurityEvaluationContextExtension();
    }
}
