package com.upply.job;

import com.upply.job.enums.*;
import com.upply.organization.Organization;
import com.upply.profile.skill.Skill;
import com.upply.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "jobs")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "posted_by_user_id")
    private User postedBy;



    private String title;

    @Enumerated(EnumType.STRING)
    private JobType type;        // (full-time, part-time, internship)

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private JobSeniority seniority;   // (junior/mid/senior/lead)

    @Enumerated(EnumType.STRING)
    private JobModel model;       // (onsite, hybrid, remote)

    @Enumerated(EnumType.STRING)
    private JobStatus status;  // (open, paused, closed)

    @Enumerated(EnumType.STRING)
    private JobSource source; // (internal, external)

    private String location;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    private String description;

    @ManyToMany
    @JoinTable(
            name = "job_skills",
            joinColumns = @JoinColumn(name = "job_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private Set<Skill> skills = new HashSet<Skill>();



    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdDate;


    @ManyToOne
    @JoinColumn(name = "organization_id")
    private Organization organization;

    private String organizationName;

    private String applicationLink;
}
