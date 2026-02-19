package com.upply.application;

import com.upply.application.enums.ApplicationStatus;
import com.upply.job.Job;
import com.upply.profile.resume.Resume;
import com.upply.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
@Table(name = "applications")
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "applicant_user_id")
    private User applicant;

    @ManyToOne
    @JoinColumn(name = "job_id")
    private Job job;

    @ManyToOne
    @JoinColumn(name = "resume_id")
    private Resume resume;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime applyTime;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime lastUpdate;
    @Column(length = 5000)
    private String coverLetter;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    private double matchingRatio;

}

