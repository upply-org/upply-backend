package com.upply.application;

import com.upply.job.Job;
import com.upply.profile.skill.Skill;
import com.upply.user.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ApplicationSummaryService {
    private final ChatClient groqChatClient;
    private final ChatClient geminiChatClient;


    public ApplicationSummaryService(
            @Qualifier("applicationSummaryGroqChatClient") ChatClient groqChatClient,
            @Qualifier("applicationSummaryGeminiChatClient") ChatClient geminiChatclient
    ) {
        this.groqChatClient = groqChatClient;
        this.geminiChatClient = geminiChatclient;
    }


    public String callAi(double score, Job job, String resumeTxt) {
        try {
            return geminiChatClient.prompt()
                    .user(buildPrompt(score, job, resumeTxt))
                    .call()
                    .content();
        } catch (Exception e) {
            return groqChatClient.prompt()
                    .user(buildPrompt(score, job, resumeTxt))
                    .call()
                    .content();
        }
    }

    private String buildPrompt(double score, Job job, String resumeTxt) {


        String jobSkills = job.getSkills().isEmpty()
                ? "None listed"
                : job.getSkills().stream()
                  .map(Skill::getName)
                  .collect(Collectors.joining(", "));
        return """
                == JOB ==
                Title          : %s
                Seniority      : %s
                Required Skills: %s
                
                == CANDIDATE ==
                Resume Text:
                \"\"\"
                %s
                \"\"\"
                """.formatted(
                job.getTitle(),
                job.getSeniority(),
                jobSkills,
                truncate(resumeTxt, 12000)
        );
    }

    public String extractText(byte[] bytes) {
        try (PDDocument doc = Loader.loadPDF(bytes)) {
            if (doc.isEncrypted()) return "Resume is encrypted.";
            String text = new PDFTextStripper().getText(doc).strip();
            return text.isEmpty() ? "Resume text could not be extracted." : text;
        } catch (IOException e) {
            log.warn("PDF extraction failed", e);
            return "Resume text unavailable.";
        }
    }
    private String truncate(String text, int max) {
        if (text == null || text.isBlank()) return "";
        return text.length() <= max ? text : text.substring(0, max) + "…";
    }
}
