package com.upply.profile.project;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query("select p from Project p where p.id = :id and p.user.id = ?#{principal.getId()}")
    Optional<Project> findProjectById(Long id);

    @Query("select p from Project p where p.user.id = ?#{principal.getId()}")
    List<Project> findUserProjectsByUserId();

    @Transactional
    @Modifying
    @Query("delete from Project p where p.id = :id  and p.user.id = ?#{principal.getId()}")
    void deleteProjectById(Long id);



}
