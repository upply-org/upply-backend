package com.upply.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;

@Configuration
@RequiredArgsConstructor
public class Beans {

    @Bean
    public AuditorAware<Long> auditorProvider() {
        return new AuditAware();
    }

}
