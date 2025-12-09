package com.upply.profile.project.dto;

import com.upply.profile.project.Project;
import org.springframework.stereotype.Service;

@Service
public class ProjectMapper {
    public Project toProject(ProjectRequest projectRequest){
        return Project.builder()
                .title(projectRequest.title())
                .description(projectRequest.description())
                .projectUrl(projectRequest.projectUrl())
                .startDate(projectRequest.startDate())
                .endDate(projectRequest.endDate())
                .technologies(projectRequest.technologies())
                .build();
    }

    public ProjectResponse toProjectResponse(Project project){
        return ProjectResponse.builder()
                .id(project.getId())
                .title(project.getTitle())
                .description(project.getDescription())
                .projectUrl(project.getProjectUrl())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .technologies(project.getTechnologies())
                .build();
    }
}
