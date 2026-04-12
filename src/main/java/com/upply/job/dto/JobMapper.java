package com.upply.job.dto;

import com.upply.job.Job;
import com.upply.job.enums.JobModel;
import com.upply.job.enums.JobSeniority;
import com.upply.job.enums.JobSource;
import com.upply.job.enums.JobStatus;
import com.upply.job.enums.JobType;
import com.upply.profile.skill.Skill;
import com.upply.profile.skill.dto.SkillMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JobMapper {

    private final SkillMapper skillMapper;

    public Job toJob(JobRequest request, Set<Skill> skills) {

        return Job.builder()
                .title(request.getTitle())
                .source(JobSource.INTERNAL)
                .type(JobType.fromApiValue(request.getType()))
                .seniority(JobSeniority.fromApiValue(request.getSeniority()))
                .model(JobModel.fromApiValue(request.getModel()))
                .status(JobStatus.OPEN)

                .location(request.getLocation())
                .description(request.getDescription())
                .skills(skills)
                .build();

    }

    public JobResponse toJobResponse(Job job) {

        return JobResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .organizationName(job.getOrganizationName())

                .type(job.getType() != null ? job.getType().toApiValue() : null)
                .seniority(job.getSeniority() != null ? job.getSeniority().toApiValue() : null)
                .model(job.getModel() != null ? job.getModel().toApiValue() : null)
                .status(job.getStatus() != null ? job.getStatus().toApiValue() : null)
                .jobSource(job.getSource() != null ? job.getSource().toApiValue() : null)

                .location(job.getLocation())
                .description(job.getDescription())
                .createdDate(job.getCreatedDate())
                .skills(
                        job.getSkills()
                                .stream()
                                .map(skillMapper::toSkillResponse)
                                .collect(Collectors.toSet())
                )
                .build();
    }

    public JobListResponse toJobListResponse(Job job) {

        return JobListResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .organizationName(job.getOrganizationName())

                .type(job.getType() != null ? job.getType().toApiValue() : null)
                .seniority(job.getSeniority() != null ? job.getSeniority().toApiValue() : null)
                .model(job.getModel() != null ? job.getModel().toApiValue() : null)
                .status(job.getStatus() != null ? job.getStatus().toApiValue() : null)
                .jobSource(job.getSource() != null ? job.getSource().toApiValue() : null)

                .location(job.getLocation())
                .createdDate(job.getCreatedDate())
                .build();
    }

    public MatchedJobListResponse toMatchedJobListResponse(Job job, Double matchScore) {

        return MatchedJobListResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .organizationName(job.getOrganizationName())

                .type(job.getType() != null ? job.getType().toApiValue() : null)
                .seniority(job.getSeniority() != null ? job.getSeniority().toApiValue() : null)
                .model(job.getModel() != null ? job.getModel().toApiValue() : null)
                .status(job.getStatus() != null ? job.getStatus().toApiValue() : null)
                .jobSource(job.getSource() != null ? job.getSource().toApiValue() : null)

                .location(job.getLocation())
                .createdDate(job.getCreatedDate())

                .matchScore(matchScore)
                .matchPercentage((int) Math.round(matchScore * 100))
                .build();
    }
}
