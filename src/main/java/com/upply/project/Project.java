package com.upply.project;

import com.upply.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Table(name = "projects")
@Entity
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String description;
    private String projectUrl;
    private Date startDate;
    private Date endDate;
    private String technologies;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
