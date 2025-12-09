package com.upply.user;


import com.upply.profile.experience.Experience;
import com.upply.job.Job;
import com.upply.profile.project.Project;
import com.upply.profile.skill.Skill;
import com.upply.profile.socialLink.SocialLink;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;


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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;

    @Column(unique = true)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    private String university;
    private String cv;

    private boolean accountLocked;
    private boolean accountActivated;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime lastModifiedDate;

    @ManyToMany
    @JoinTable(
            name = "users_skills",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private Set<Skill> userSkills = new HashSet<>();

    @OneToMany(mappedBy = "postedBy", cascade = CascadeType.ALL)
    private List<Job> postedJobs = new ArrayList<>();


    @OneToMany(mappedBy = "user")
    private List<Experience> experiences = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Project> projects = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<SocialLink> socialLinks = new ArrayList<>();

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

        if (firstName == null && lastName == null) {
            return "";
        }

        if (lastName == null) {
            return firstName;
        }

        if (firstName == null) {
            return lastName;
        }

        return firstName + " " + lastName;
    }
}
