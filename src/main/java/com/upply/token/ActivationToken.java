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
@Table(name = "activation_token")

public class ActivationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    String token;

    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;

    private boolean used;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
