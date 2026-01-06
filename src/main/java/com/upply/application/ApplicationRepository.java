package com.upply.application;

import com.upply.application.enums.ApplicationStatus;
import com.upply.job.Job;
import com.upply.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    @Query("select a from Application a where a.applicant.id =?#{principal.getId()}")
    Page<Application> getUserApplications(Pageable pageable);

    @Query("select a from Application a where a.applicant.id = ?#{principal.getId()} and a.id = :applicationId")
    Optional<Application> getApplicationById(Long applicationId);

    @Query("select  a from Application a where a.job.postedBy.id = ?#{principal.getId()} and a.job.id = :jobId")
    //add @hasAuthority
    Page<Application> getJobApplications(Long jobId,Pageable pageable);

    @Query("select a from Application a where a.job.postedBy.id = ?#{principal.getId()} and a.status = :status and a.job.id = :jobId")
    // add @hasauthority
    Page<Application> getApplicationByStatus(Long jobId,ApplicationStatus status ,Pageable pageable);

    @Query("select a from Application a where a.job.postedBy.id = ?#{principal.getId()} and a.id = :applicationId")
    Optional<Application> getApplicationByIdForRecruiter(Long applicationId);

    boolean existsApplicationByApplicantAndJob(User applicant, Job job);
}
