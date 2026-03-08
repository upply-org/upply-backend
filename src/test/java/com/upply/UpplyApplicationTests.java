package com.upply;

import com.upply.application.ApplicationMatchConsumer;
import com.upply.application.dto.ApplicationMatchEvent;
import com.upply.notification.DispatchConsumer;
import com.upply.notification.NotificationOrchestrator;
import com.upply.notification.dto.DispatchPayload;
import com.upply.notification.dto.NotificationEvent;
import com.upply.profile.resume.AzureStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;

@SpringBootTest
@ActiveProfiles("test")
@ImportAutoConfiguration(exclude = KafkaAutoConfiguration.class)
@Import(com.upply.config.UpplyApplicationTests.class)
class UpplyApplicationTests {

	@MockitoBean
	private VectorStore vectorStore;
	@MockitoBean
	private AzureStorageService azureStorageService;
	@MockitoBean
	private ApplicationMatchConsumer applicationMatchConsumer;
	@MockitoBean
	private KafkaTemplate<String, ApplicationMatchEvent> kafkaTemplate;
	@MockitoBean
	private KafkaTemplate<String, NotificationEvent> notificationEventKafkaTemplate;
	@MockitoBean
	private KafkaTemplate<String, DispatchPayload> dispatchKafkaTemplate;
	@MockitoBean
	private ChatClient chatClient;
	@Test
	void contextLoads() {
	}

}
