package com.upply.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
@Profile("!test")
public class GenAiConfig {

   private ChatClient build(ChatClient.Builder builder,
                                Resource prompt,
                            double temperature,
                            int maxTokens){
        try{
            return builder
                    .defaultSystem(prompt.getContentAsString(StandardCharsets.UTF_8))
                    .defaultOptions(
                            GoogleGenAiChatOptions.builder()
                                    .model("gemini-2.5-flash-lite")
                                    .temperature(temperature)
                                    .maxOutputTokens(maxTokens)
                                    .build()
                    )
                    .build();

        }catch (IOException e){
            throw new RuntimeException("Failed to load resume analysis system prompt", e);
        }

   }

   @Bean
    public ChatClient resumeAnalysisChatClient(
            ChatClient.Builder builder,
            @Qualifier("resumeAnalysisPrompt") Resource prompt
   ){
       return build(builder, prompt, 0.2, 1024);
   }

   @Bean
    public ChatClient resumeParserChatClient(
            ChatClient.Builder builder,
            @Qualifier("resumeParserPrompt") Resource prompt
   ){
       return build(builder, prompt, 0.0, 3072);
   }

   @Bean
    public ChatClient applicationSummaryChatClient(
            ChatClient.Builder builder,
            @Qualifier("applicationSummaryPrompt") Resource prompt
   ){
       return build(builder, prompt, 0.1, 3072);
   }

}
