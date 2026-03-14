package com.upply.job;

import com.upply.job.enums.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {

    Page<Job> findByStatus(JobStatus status, Pageable pageable);

    @Query("""
        SELECT js.name
        FROM Job j
        JOIN j.skills js
        WHERE j.id = :jobId
    """)
    List<String> findJobSkillNames(@Param("jobId") Long jobId);

    @Query("select distinct j from User u join u.userBookmarkedJobs j where u.id = ?#{principal.getId()}")
    Set<Job> findUserBookMarks();
}
