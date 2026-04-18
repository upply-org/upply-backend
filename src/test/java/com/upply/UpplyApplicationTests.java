package com.upply;

import com.upply.application.dto.ApplicationMatchEvent;
import com.upply.notification.dto.DispatchPayload;
import com.upply.notification.dto.NotificationEvent;
import com.upply.profile.resume.AzureStorageService;
import com.upply.user.dto.SkillEvent;
import org.junit.jupiter.api.Test;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;

@SpringBootTest
@ActiveProfiles("test")
@ImportAutoConfiguration(exclude = KafkaAutoConfiguration.class)
@Import(com.upply.config.UpplyApplicationTests.class)
class UpplyApplicationTests {

	@MockBean
	@Qualifier("jobsVectorStore")
	private VectorStore vectorStore;
	@MockBean
	@Qualifier("userSkillsVectorStore")
	private VectorStore userSkillsVectorStore;
	@MockBean
	private AzureStorageService azureStorageService;
	@MockBean
	private KafkaTemplate<String, ApplicationMatchEvent> kafkaTemplate;
	@MockBean
	private KafkaTemplate<String, NotificationEvent> notificationEventKafkaTemplate;
	@MockBean
	private KafkaTemplate<String, DispatchPayload> dispatchKafkaTemplate;
	@MockBean
	private KafkaTemplate<String, SkillEvent> skillEventKafkaTemplate;
	@Test
	void contextLoads() {
	}

}
