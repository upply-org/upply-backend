package com.upply.profile.skill;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SkillRepository extends JpaRepository <Skill, Long> {

    @Query("select s from Skill s where s.id = :id")
    Optional<Skill> findById(Long id);

    @Query("select s from Skill s where s.searchName LIKE concat('%', :name, '%')")
    Optional<Skill> findSkillByName(String name);
}
