package com.upply.profile.experience;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExperienceRepository extends JpaRepository<Experience, Long> {


    @Query("select e from Experience e where e.id =:id and e.user.id = ?#{principal.getId()}")
    Optional<Experience> findExperienceById(Long id);

    @Query("select e from Experience e where e.user.id = ?#{principal.getId()}")
    List<Experience> findUserExperienceByUserId();

    @Transactional
    @Modifying
    @Query("delete from Experience  e where e.id  = :id and e.user.id = ?#{principal.getId()}")
    void deleteExperienceById(Long id);
}
