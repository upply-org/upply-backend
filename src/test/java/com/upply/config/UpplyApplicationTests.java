package com.upply.config;


import com.google.firebase.FirebaseApp;
import com.upply.application.dto.ApplicationMatchEvent;
import com.upply.job.dto.PostJobEvent;
import com.upply.notification.dto.DispatchPayload;
import com.upply.notification.dto.NotificationEvent;
import com.upply.user.dto.SkillEvent;
import org.mockito.Mockito;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.kafka.core.KafkaTemplate;

@TestConfiguration
public class UpplyApplicationTests {
    @Bean
    public KafkaTemplate<String, ApplicationMatchEvent> kafkaTemplate() {
        return Mockito.mock(KafkaTemplate.class);
    }

    @Bean
    public KafkaTemplate<String, NotificationEvent> notificationKafkaTemplate() {
        return Mockito.mock(KafkaTemplate.class);
    }

    @Bean
    public KafkaTemplate<String, DispatchPayload> dispatchKafkaTemplate() {
        return Mockito.mock(KafkaTemplate.class);
    }

    @Bean
    public KafkaTemplate<String, SkillEvent> skillEventKafkaTemplate() {
        return Mockito.mock(KafkaTemplate.class);
    }

    @Bean
    public KafkaTemplate<String, PostJobEvent> postJobEventKafkaTemplate() {
        return Mockito.mock(KafkaTemplate.class);
    }

    @Bean
    public FirebaseApp firebaseApp(){
        return Mockito.mock(FirebaseApp.class);
    }

    @Bean
    public ChatClient resumeAnalysisGroqChatClient() {
        return Mockito.mock(ChatClient.class);
    }

    @Bean
    public ChatClient resumeAnalysisGeminiChatClient() {
        return Mockito.mock(ChatClient.class);
    }

    @Bean
    public ChatClient resumeParserGroqChatClient() {
        return Mockito.mock(ChatClient.class);
    }

    @Bean
    public ChatClient resumeParserGeminiChatClient() {
        return Mockito.mock(ChatClient.class);
    }

    @Bean
    public ChatClient resumeAnalysisChatClient() {
        return Mockito.mock(ChatClient.class);
    }

    @Bean
    public ChatClient resumeParserChatClient() {
        return Mockito.mock(ChatClient.class);
    }

    @Bean
    public ChatClient jobImportGeminiChatClient() {
        return Mockito.mock(ChatClient.class);
    }

    @Bean
    public ChatClient jobImportGroqChatClient() {
        return Mockito.mock(ChatClient.class);
    }

    @Bean
    public ChatClient applicationSummaryGroqChatClient(){
        return Mockito.mock(ChatClient.class);
    }

    @Bean
    public ChatClient applicationSummaryGeminiChatClient(){
        return Mockito.mock(ChatClient.class);
    }

    @Bean
    public VectorStore resumeVectorStore() {
        return Mockito.mock(VectorStore.class);
    }

    @Bean
    public VectorStore userSkillsVectorStore() {
        return Mockito.mock(VectorStore.class);
    }

    @Bean
    public ChatClient recruiterRagGeminiChatClient() {
        return Mockito.mock(ChatClient.class);
    }

    @Bean
    public ChatClient recruiterRagGroqChatClient() {
        return Mockito.mock(ChatClient.class);
    }

    @Bean
    public ChatClient recruiterRagChatClient() {
        return Mockito.mock(ChatClient.class);
    }

    @Bean
    public ChatMemory chatMemory() {
        return Mockito.mock(ChatMemory.class);
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return Mockito.mock(RedisConnectionFactory.class);
    }
}
