package com.upply.config;


import com.google.firebase.FirebaseApp;
import com.upply.application.dto.ApplicationMatchEvent;
import com.upply.notification.dto.DispatchPayload;
import com.upply.notification.dto.NotificationEvent;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
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
    public FirebaseApp firebaseApp(){
        return Mockito.mock(FirebaseApp.class);
    }
}
