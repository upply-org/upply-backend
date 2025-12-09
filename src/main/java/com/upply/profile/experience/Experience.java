package com.upply.profile.experience;

import com.upply.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "experiences")
public class Experience {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String organization;
    private Date startDate;
    private Date endDate;
    private String description;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
