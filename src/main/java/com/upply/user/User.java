package com.upply.user;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

@EntityListeners(AuditingEntityListener.class)

@Entity
@Table(name = "users")

public class User implements UserDetails, Principal {

    @Id
    @GeneratedValue  // generation type ??
    private Integer id;

    private String firstName;
    private String lastName;

    @Column(unique = true)
    private String email;

    private String password;
    private String university;
    private String cv;

    private boolean accountLocked;
    private boolean accountActivated;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(insertable = false)
    private LocalDateTime lastModifiedDate;




    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(); // TODO
    }

    @Override
    public String getUsername() {
        return email; //unique identifier of the user
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !accountLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return accountActivated;
    }

    @Override
    public String getName() {
        return email;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
