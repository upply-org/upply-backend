package com.upply.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
public class GenAiConfig {
    @Bean
    public Resource resumeSystemPrompt(){
        return new ClassPathResource("prompts/resume-analysis.st");
    }

   @Bean
   public ChatClient chatClient(ChatClient.Builder builder,
                                @Qualifier("resumeSystemPrompt") Resource systemPrompt){
        try{
            return builder
                    .defaultSystem(systemPrompt.getContentAsString(StandardCharsets.UTF_8))
                    .defaultOptions(
                            GoogleGenAiChatOptions.builder()
                                    .model("gemini-2.5-flash-lite")
                                    .temperature(0.2)
                                    .maxOutputTokens(1024)
                                    .build()
                    )
                    .build();

        }catch (IOException e){
            throw new RuntimeException("Failed to load resume analysis system prompt", e);
        }

   }

}
