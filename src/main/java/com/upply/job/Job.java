package com.upply.job;

import com.upply.profile.skill.Skill;
import com.upply.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "jobs")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "posted_by_user_id")
    private User postedBy;

    String title;
    String type;        // (full-time, part-time, internship)
    String seniority;   // (junior/mid/senior/lead)
    String model;       // (onsite, hybrid, remote)
    String location;
    String description;

    @ManyToMany
    @JoinTable(
            name = "job_skills",
            joinColumns = @JoinColumn(name = "job_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private Set<Skill> skills = new HashSet<Skill>();


    // TODO: Organization Relationship


    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;
}
