package com.upply.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

@Configuration
public class AiPromptsConfig {
    @Bean
    public Resource resumeAnalysisPrompt(){
        return new ClassPathResource("prompts/resume-analysis.st");
    }

    @Bean
    public Resource resumeParserPrompt(){
        return new ClassPathResource("prompts/resume-parser.st");
    }

    @Bean
    public Resource applicationSummaryPrompt(){
        return  new ClassPathResource("prompts/application-summary.st");
    }
}
