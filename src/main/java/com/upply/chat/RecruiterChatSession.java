package com.upply.chat;

import com.upply.job.Job;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "recruiter_chat_session")
public class RecruiterChatSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String sessionId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "job_id")
    private Job job;

    private String title;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}