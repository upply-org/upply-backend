package com.upply.profile.resume;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, Long> {

    @Query("select r from Resume r where r.id = :resumeId and r.user.id = ?#{principal.getId()}")
    Optional<Resume> getResumeById(Long resumeId);

    @Query("select r from Resume r where r.user.id = ?#{principal.getId()} and r.isDeleted = false ")
    List<Resume> getAllUserResumes();

    @Query("select r from Resume r where r.user.id = ?#{principal.getId()} and r.isDeleted = false  order by r.createdAt desc  fetch first 1 ROWS only")
    Optional<Resume> getLastSubmittedResume();
}
