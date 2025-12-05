package com.upply.socialLink;

import com.upply.user.User;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "social_links")
public class SocialLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String url;
    private SocialType socialType;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

}
