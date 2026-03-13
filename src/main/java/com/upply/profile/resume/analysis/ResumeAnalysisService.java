package com.upply.profile.resume.analysis;

import com.upply.exception.custom.ResourceNotFoundException;
import com.upply.job.Job;
import com.upply.job.JobRepository;
import com.upply.profile.resume.AzureStorageService;
import com.upply.profile.resume.ResumeRepository;
import com.upply.profile.resume.dto.ResumeFeedbackResponse;
import com.upply.profile.resume.enums.ResumeSection;
import com.upply.profile.resume.enums.ResumeSectionGroups;
import com.upply.profile.skill.Skill;
import com.upply.user.User;
import com.upply.user.UserRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.util.StringUtils.truncate;

@Service
@Transactional
public class ResumeAnalysisService {
    private final ChatClient chatClient;
    private final UserRepository userRepository;
    private final ResumeRepository resumeRepository;
    private final JobRepository jobRepository;
    private final AzureStorageService azureStorageService;

    public ResumeAnalysisService(
            @Qualifier("resumeAnalysisChatClient") ChatClient chatClient,
            UserRepository      userRepository,
            ResumeRepository    resumeRepository,
            JobRepository       jobRepository,
            AzureStorageService azureStorageService) {
        this.chatClient          = chatClient;
        this.userRepository      = userRepository;
        this.resumeRepository    = resumeRepository;
        this.jobRepository       = jobRepository;
        this.azureStorageService = azureStorageService;
    }

    /**
     * Generic profile analysis, without job context
     * Response sections: impact, clarity, structure, completeness
     * */

    public ResumeFeedbackResponse analysisProfile(Long resumeId){
        UserProfileContext ctx = buildUserContext(resumeId);
        return chatClient.prompt()
                .user(buildGenericPrompt(ctx))
                .call()
                .entity(ResumeFeedbackResponse.class);
    }

    /**
     * Job-Specific analysis - profile compared against a specific job.
     * Sections:RELEVANCE, SKILLS_ALIGNMENT, EXPERIENCE_LEVEL, IMPACT, CULTURE_FIT
     * */
    public ResumeFeedbackResponse analysisProfileForJob(Long jobId, Long resumeId){
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job with ID " + jobId + " not found"));
        UserProfileContext ctx = buildUserContext(resumeId);
        return chatClient.prompt()
                .user(buildJobPrompt(ctx, job))
                .call()
                .entity(ResumeFeedbackResponse.class);
    }

    private UserProfileContext buildUserContext(Long resumeId){
        User user = userRepository.getCurrentUser()
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));
        String resumeText = resumeRepository.getResumeById(resumeId)
                .map(resume -> extractTextFromPdf(
                        azureStorageService.downloadFile(resume.getBlobName())
                ))
                .orElseThrow(()-> new ResourceNotFoundException(
                        "No Resume Found"
                ));
        return new UserProfileContext(user, resumeText);
    }

    private String buildGenericPrompt(UserProfileContext ctx) {
        return """
                Analyze this candidate's complete profile and return the feedback JSON.

                Sections to score: %s

                %s
                """.formatted(
                sectionNames(ResumeSectionGroups.GENERIC_SECTIONS),
                formatProfile(ctx)
        );
    }

    private String buildJobPrompt(UserProfileContext ctx, Job job) {
        return """
                Analyze this candidate's complete profile against the job posting below.
                Return the full job-specific feedback JSON including:
                jobMatchScore, matchedSkills, missingSkills, and topTip.

                Sections to score: %s

                %s

                == JOB POSTING ==
                Title          : %s
                Company        : %s
                Seniority      : %s
                Work Model     : %s
                Location       : %s
                Required Skills: %s

                Description:
                \"\"\"
                %s
                \"\"\"
                """.formatted(
                sectionNames(ResumeSectionGroups.JOB_SPECIFIC_SECTIONS),
                formatProfile(ctx),
                job.getTitle(),
                "our org name is : ", //hardcoded string
                //job.getOrganization().getName(), TODO: add ORG and delete the hard coded string
                job.getSeniority(),
                job.getModel(),
                job.getLocation() != null ? job.getLocation() : "N/A",
                job.getSkills().stream()
                        .map(Skill::getName)
                        .collect(Collectors.joining(", ")),
                truncate(job.getDescription() != null ? job.getDescription() : "", 1500)
        );
    }


    private String formatProfile(UserProfileContext ctx) {
        User user = ctx.user();

        String skills = user.getUserSkills().isEmpty()
                ? "None listed"
                : user.getUserSkills().stream()
                .map(Skill::getName)
                .collect(Collectors.joining(", "));

        String experiences = user.getExperiences().isEmpty()
                ? "  No experiences added yet."
                : user.getExperiences().stream()
                .map(e -> "  - %s at %s (%s → %s)\n    %s".formatted(
                        e.getTitle(),
                        e.getOrganization(),
                        e.getStartDate(),
                        e.getEndDate() != null
                                ? e.getEndDate()
                                : "Present",
                        e.getDescription()))
                .collect(Collectors.joining("\n"));

        String projects = user.getProjects().isEmpty()
                ? "  No projects added yet."
                : user.getProjects().stream()
                .map(p -> "  - %s (%s)\n    %s\n    Tech: %s".formatted(
                        p.getTitle(),
                        p.getStartDate(),
                        p.getDescription(),
                        p.getTechnologies() != null
                                ? p.getTechnologies()
                                : "N/A"))
                .collect(Collectors.joining("\n"));

        return """
                == CANDIDATE PROFILE ==
                Name      : %s %s
                University: %s
                Skills    : %s

                Experiences:
                %s

                Projects:
                %s

                Resume Text:
                \"\"\"
                %s
                \"\"\"
                """.formatted(
                user.getFirstName(),
                user.getLastName(),
                user.getUniversity() != null ? user.getUniversity() : "N/A",
                skills,
                experiences,
                projects,
                truncate(ctx.resumeText(), 2000)
        );
    }
    private String extractTextFromPdf(byte[] pdfFile){
        try(PDDocument document = Loader.loadPDF(pdfFile)){
            if(document.isEncrypted()){
                return "Resume is encrypted and can't be processed - Start analysis based on profile data only";
            }
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document).strip();

            return text.isEmpty()?"Resume text could not be extracted- Start analysis based on profile data only" : text;
        }catch (IOException e){
            // I don't want fail whole request
            return "Resume text unavailable, Start analysis based on profile data only.";
        }
    }

    private String sectionNames(Set<ResumeSection> sections){
        return sections.stream()
                .map(ResumeSection::name)
                .collect(Collectors.joining(", "));
    }

    private record UserProfileContext(User user, String resumeText){}
}
