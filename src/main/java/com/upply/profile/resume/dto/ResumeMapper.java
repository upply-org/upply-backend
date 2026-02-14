package com.upply.profile.resume.dto;

import com.upply.profile.resume.Resume;
import org.springframework.stereotype.Service;

@Service
public class ResumeMapper {
    public ResumeResponse toResumeResponse(Resume resume){
        return ResumeResponse.builder()
                .id(resume.getId())
                .fileName(resume.getFileName())
                .blobName(resume.getBlobName())
                .createdAt(resume.getCreatedAt())
                .build();
    }
}
