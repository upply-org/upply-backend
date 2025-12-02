package com.upply.skill;

import jakarta.persistence.*;
import lombok.*;


@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "skills",
        indexes = {
                @Index(name = "idx_skill_search_name", columnList = "searchName"),
                @Index(name = "idx_skill_name", columnList = "name")
        }
)
public class Skill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    private SkillCategory category;

    private String searchName; // to normalize name column


    /**
     * Normalizes the skill name for search operations before inserting or updating.
     * This method creates a search-optimized version of the skill name by:
     * 1. Converting to lowercase for case-insensitive searching
     * 2. Removing all whitespace (spaces, tabs, newlines) for flexible matching
     *
     * Examples:
     * - "Java Programming" → "javaprogramming"
     * The transformation is performed automatically before database operations.
     */
    @PrePersist
    @PreUpdate
    private void normalizedSearchName() {
        this.searchName = this.name != null ?
                this.name.toLowerCase()
                        .replaceAll("\\s+", "")
                : null;
    }
}
