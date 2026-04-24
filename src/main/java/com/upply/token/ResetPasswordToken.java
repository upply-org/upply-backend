package com.upply.token;

import com.upply.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "reset_password_token")

public class ResetPasswordToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String token;

    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;

    private boolean used;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}