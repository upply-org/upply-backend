package com.upply.job;

import com.upply.job.enums.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    Page<Job> findByStatus(JobStatus status, Pageable pageable);

    @Query("""
        SELECT js.name
        FROM Job j
        JOIN j.skills js
        WHERE j.id = :jobId
    """)
    List<String> findJobSkillNames(@Param("jobId") Long jobId);
}
