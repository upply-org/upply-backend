package com.upply.profile.resume;

import com.upply.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.Instant;
import java.time.LocalDateTime;

import static java.lang.Boolean.FALSE;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Table(name = "resumes",
        indexes = {
        @Index(name = "idx_user_created_at", columnList = "user_id, is_deleted, created_at DESC")
        }
)
@Entity
public class Resume {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String blobName;
    private String fileName;
    @CreatedDate
    private Instant createdAt;
    Boolean isDeleted = false;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

}
