package com.upply.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
@Profile("!test")
public class GenAiConfig {
    private final ChatClient.Builder geminiBuilder;
    private final ChatClient.Builder groqBuilder;

    @Value("${app.model.google-lite-model}")
    private String googleLiteModel;

    @Value("${app.model.google-full-model}")
    private String googleFullModel;

    @Value("${app.model.groq-lite-model}")
    private String groqLiteModel;

    @Value("${app.model.groq-full-model}")
    private String groqFullModel;

    public GenAiConfig(
            @Qualifier("googleGenAiChatModel") ChatModel geminiModel,
            @Qualifier("openAiChatModel") ChatModel groqModel) {
        this.geminiBuilder = ChatClient.builder(geminiModel);
        this.groqBuilder   = ChatClient.builder(groqModel);
    }

    private ChatClient geminiBuild(Resource prompt, double temperature, int maxTokens) {
        try {
            return geminiBuilder.clone()
                    .defaultSystem(prompt.getContentAsString(StandardCharsets.UTF_8))
                    .defaultOptions(GoogleGenAiChatOptions.builder()
                            .model(googleLiteModel)
                            .temperature(temperature)
                            .maxOutputTokens(maxTokens)
                            .build())
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load gemini model", e);
        }
    }

    private ChatClient groqBuild(Resource prompt, double temperature, int maxTokens) {
        try {
            return groqBuilder.clone()
                    .defaultSystem(prompt.getContentAsString(StandardCharsets.UTF_8))
                    .defaultOptions(OpenAiChatOptions.builder()
                            .model(groqLiteModel)
                            .temperature(temperature)
                            .maxTokens(maxTokens)
                            .build())
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load groq model", e);
        }
    }

    @Bean
    public ChatClient resumeAnalysisGeminiChatClient(@Qualifier("resumeAnalysisPrompt") Resource prompt) {
        return geminiBuild(prompt, 0.2, 1024);
    }

    @Bean
    public ChatClient resumeAnalysisGroqChatClient(@Qualifier("resumeAnalysisPrompt") Resource prompt) {
        return groqBuild(prompt, 0.2, 1024);
    }

    @Bean
    public ChatClient resumeParserGeminiChatClient(@Qualifier("resumeParserPrompt") Resource prompt) {
        return geminiBuild(prompt, 0.0, 3072);
    }

    @Bean
    public ChatClient resumeParserGroqChatClient(@Qualifier("resumeParserPrompt") Resource prompt) {
        return groqBuild(prompt, 0.0, 3072);
    }

    @Bean
    public ChatClient applicationSummaryGeminiChatClient(@Qualifier("applicationSummaryPrompt") Resource prompt) {
        return geminiBuild(prompt, 0.1, 3072);
    }

    @Bean
    public ChatClient applicationSummaryGroqChatClient(@Qualifier("applicationSummaryPrompt") Resource prompt) {
        return groqBuild(prompt, 0.1, 3072);
    }

    @Bean
    public ChatClient jobImportGeminiChatClient(@Qualifier("jobImportPrompt") Resource prompt) {
        return geminiBuild(prompt, 0.0, 2048);
    }

    @Bean
    public ChatClient jobImportGroqChatClient(@Qualifier("jobImportPrompt") Resource prompt) {
        return groqBuild(prompt, 0.0, 2048);
    }

    @Bean
    public ChatClient recruiterRagGeminiChatClient(@Qualifier("recruiterRag") Resource prompt) {
        try {
            return geminiBuilder.clone()
                    .defaultSystem(prompt.getContentAsString(StandardCharsets.UTF_8))
                    .defaultOptions(GoogleGenAiChatOptions.builder()
                            .model(googleFullModel)
                            .temperature(0.3)
                            .maxOutputTokens(65536)
                            .thinkingBudget(4096)
                            .build())
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load recruiter RAG prompt", e);
        }
    }

    @Bean
    public ChatClient recruiterRagGroqChatClient(@Qualifier("recruiterRag") Resource prompt) {
        try {
            return groqBuilder.clone()
                    .defaultSystem(prompt.getContentAsString(StandardCharsets.UTF_8))
                    .defaultOptions(OpenAiChatOptions.builder()
                            .model(groqFullModel)
                            .temperature(0.3)
                            .maxTokens(8192)
                            .build())
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load recruiter RAG prompt", e);
        }
    }

    @Bean
    public ChatMemory chatMemory(JdbcChatMemoryRepository jdbcChatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(jdbcChatMemoryRepository)
                .maxMessages(10)
                .build();
    }
}
