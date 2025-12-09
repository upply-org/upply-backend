package com.upply.config;

import com.upply.user.User;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;


// The AuditorAware<T> interface is used when you want to automatically fill audit fields
// (like createdBy, lastModifiedBy) in your database entities.

// The generic type <Integer> means the auditor (the user who performed the action)
// is identified by an Integer ID — typically the user’s primary key.

public class AuditAware implements AuditorAware<Long> {


    @Override
    public Optional<Long> getCurrentAuditor() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }

        User userPrincipal = (User) authentication.getPrincipal();
        return Optional.ofNullable(userPrincipal.getId());
    }
}
