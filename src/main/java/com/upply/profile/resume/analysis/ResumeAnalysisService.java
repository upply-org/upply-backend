package com.upply.profile.resume.analysis;

import com.upply.exception.custom.ResourceNotFoundException;
import com.upply.job.Job;
import com.upply.job.JobRepository;
import com.upply.profile.resume.AzureStorageService;
import com.upply.profile.resume.ResumeRepository;
import com.upply.profile.resume.dto.ResumeAnalysisResponse;
import com.upply.profile.resume.enums.ResumeSection;
import com.upply.profile.resume.enums.ResumeSectionGroups;
import com.upply.profile.skill.Skill;
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
    private final ChatClient groqChatClient;
    private final ChatClient geminiChatClient;
    private final UserRepository userRepository;
    private final ResumeRepository resumeRepository;
    private final JobRepository jobRepository;
    private final AzureStorageService azureStorageService;

    public ResumeAnalysisService(
            @Qualifier("resumeAnalysisGroqChatClient") ChatClient groqchatClient,
            @Qualifier("resumeAnalysisGeminiChatClient") ChatClient geminiChatClient,
            UserRepository userRepository,
            ResumeRepository resumeRepository,
            JobRepository jobRepository,
            AzureStorageService azureStorageService) {
        this.groqChatClient = groqchatClient;
        this.geminiChatClient = geminiChatClient;
        this.userRepository = userRepository;
        this.resumeRepository = resumeRepository;
        this.jobRepository = jobRepository;
        this.azureStorageService = azureStorageService;
    }

    /**
     * Job-Specific analysis - resume compared against a specific job.
     * Sections:RELEVANCE, SKILLS_ALIGNMENT, EXPERIENCE_LEVEL, IMPACT, CULTURE_FIT
     * */
    public ResumeAnalysisResponse analysisResumeForJob(Long jobId, Long resumeId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job with ID " + jobId + " not found"));
        UserProfileContext ctx = buildUserContext(resumeId);
        try {
            return geminiChatClient.prompt()
                    .user(buildJobPrompt(ctx, job))
                    .call()
                    .entity(ResumeAnalysisResponse.class);
        } catch (Exception e) {
            return groqChatClient.prompt()
                    .user(buildJobPrompt(ctx, job))
                    .call()
                    .entity(ResumeAnalysisResponse.class);
        }
    }

    private UserProfileContext buildUserContext(Long resumeId) {
        String resumeText = resumeRepository.getResumeById(resumeId)
                .map(resume -> extractTextFromPdf(
                        azureStorageService.downloadFile(resume.getBlobName())
                ))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No Resume Found"
                ));
        return new UserProfileContext(resumeText);
    }

    private String buildJobPrompt(UserProfileContext ctx, Job job) {
        return """
                Analyze this candidate's resume against the job posting below.
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
        return """
                == CANDIDATE PROFILE == 
                Resume Text:
                \"\"\"
                %s
                \"\"\"
                """.formatted(
                ctx.resumeText()
        );
    }

    private String extractTextFromPdf(byte[] pdfFile) {
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            if (document.isEncrypted()) {
                return "Resume is encrypted and can't be processed - Start analysis based on profile data only";
            }
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document).strip();

            return text.isEmpty() ? "Resume text could not be extracted- Start analysis based on profile data only" : text;
        } catch (IOException e) {
            // I don't want fail whole request
            return "Resume text unavailable, Start analysis based on profile data only.";
        }
    }

    private String sectionNames(Set<ResumeSection> sections) {
        return sections.stream()
                .map(ResumeSection::name)
                .collect(Collectors.joining(", "));
    }

    private record UserProfileContext(String resumeText) {
    }
}
