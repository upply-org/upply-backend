package com.upply.organization;

import com.upply.job.Job;
import com.upply.job.enums.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    Optional<Organization> findByDomain(String domain);

    @Query("SELECT j FROM Job j WHERE j.organization.id = :orgId AND j.status = :status")
    Page<Job> findJobsByOrganizationIdAndStatus(@Param("orgId") Long orgId, @Param("status") JobStatus status, Pageable pageable);

    @Query("SELECT COUNT(r) > 0 FROM Organization o JOIN o.recruiters r WHERE o.id = :orgId AND r.id = :userId")
    boolean existsRecruiterInOrganization(@Param("orgId") Long orgId, @Param("userId") Long userId);
}
