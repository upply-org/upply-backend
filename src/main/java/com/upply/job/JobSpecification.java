package com.upply.job;

import com.upply.job.dto.JobFilter;
import com.upply.job.enums.JobModel;
import com.upply.job.enums.JobSeniority;
import com.upply.job.enums.JobStatus;
import com.upply.job.enums.JobType;
import org.springframework.data.jpa.domain.Specification;

public class JobSpecification {

    public static Specification<Job> withFilters(JobFilter filter) {
        return hasStatus(JobStatus.OPEN)
                .and(hasKeyword(filter.getKeyword()))
                .and(hasType(filter.getType()))
                .and(hasSeniority(filter.getSeniority()))
                .and(hasModel(filter.getModel()))
                .and(hasLocation(filter.getLocation()));
    }

    public static Specification<Job> hasStatus(JobStatus status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Job> hasKeyword(String keyword) {
        return (root, query, cb) -> {

            if(keyword == null || keyword.isBlank()) {
                return null;
            }

            String pattern = "%" + keyword.toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }

    public static Specification<Job> hasLocation(String location) {
        return (root, query, cb) ->
                location == null || location.isBlank() ? null : cb.like(cb.lower(root.get("location")), "%" + location.toLowerCase() + "%");
    }

    public static Specification<Job> hasType(JobType type) {
        return (root, query, cb) ->
                type == null ? null : cb.equal(root.get("type"), type);
    }

    public static Specification<Job> hasSeniority(JobSeniority seniority) {
        return (root, query, cb) ->
                seniority == null ? null : cb.equal(root.get("seniority"), seniority);
    }

    public static Specification<Job> hasModel(JobModel model) {
        return (root, query, cb) ->
                model == null ? null : cb.equal(root.get("model"), model);
    }
}
