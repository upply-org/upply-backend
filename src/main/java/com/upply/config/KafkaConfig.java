package com.upply.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    public static final String APPLICATION_MATCH_CALC_TOPIC = "application-match-calc";
    public static final String NOTIFICATION_EVENTS = "notification-events";
    public static final String NOTIFICATION_DISPATCH = "notification-dispatch";
    public static final String UserSkillsEmbeddingTopic = "user-skills-embedding";
    public static final String JOB_POSTING_TOPIC = "job-posting-topic";

    @Bean
    public NewTopic applicationMatchTopic() {
        return TopicBuilder.name(APPLICATION_MATCH_CALC_TOPIC)
                .partitions(2)
                .replicas(2)
                .build();
    }

    @Bean
    public NewTopic notificationEventsTopic() {
        return TopicBuilder.name(NOTIFICATION_EVENTS)
                .partitions(2)
                .replicas(2)
                .build();
    }

    @Bean
    public NewTopic notificationDispatchTopic() {
        return TopicBuilder.name(NOTIFICATION_DISPATCH)
                .partitions(2)
                .replicas(2)
                .build();
    }

    @Bean
    public NewTopic userSkillsEmbeddingTopic() {
        return  TopicBuilder.name(UserSkillsEmbeddingTopic)
                .partitions(2)
                .replicas(2)
                .build();
    }

    @Bean
    public NewTopic jobPostingTopic(){
        return TopicBuilder.name(JOB_POSTING_TOPIC)
                .partitions(2)
                .replicas(2)
                .build();
    }

}
