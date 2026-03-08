package com.upply.profile.resume.enums;

import java.util.Set;

public class ResumeSectionGroups {
    public static final Set<ResumeSection> GENERIC_SECTIONS = Set.of(
            ResumeSection.IMPACT,
            ResumeSection.CLARITY,
            ResumeSection.KEYWORDS,
            ResumeSection.STRUCTURE,
            ResumeSection.COMPLETENESS
    );

    public static final Set<ResumeSection> JOB_SPECIFIC_SECTIONS = Set.of(
            ResumeSection.RELEVANCE,
            ResumeSection.SKILLS_ALIGNMENT,
            ResumeSection.EXPERIENCE_LEVEL,
            ResumeSection.IMPACT,
            ResumeSection.CULTURE_FIT
    );
}
