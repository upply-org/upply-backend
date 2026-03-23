package com.upply.notification;

import com.upply.common.NotificationEventType;
import com.upply.exception.custom.ResourceNotFoundException;
import com.upply.notification.dto.DispatchPayload;
import com.upply.notification.dto.NotificationEvent;
import com.upply.notification.handler.NotificationHandler;
import com.upply.user.User;
import com.upply.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.upply.config.KafkaConfig.NOTIFICATION_DISPATCH;
import static com.upply.config.KafkaConfig.NOTIFICATION_EVENTS;
import static com.upply.notification.dto.DispatchPayload.Channel.PUSH;

@Service
@Slf4j
public class NotificationOrchestrator {
    private final KafkaTemplate<String, DispatchPayload> kafkaTemplate;
    private final UserRepository userRepository;
    private final Map<NotificationEventType, NotificationHandler> handlers;

    public NotificationOrchestrator(KafkaTemplate<String, DispatchPayload> kafkaTemplate,
                                    UserRepository userRepository,
                                    List<NotificationHandler> handlerList) {
        this.kafkaTemplate = kafkaTemplate;
        this.userRepository = userRepository;
        this.handlers = handlerList.stream()
                .collect(Collectors.toMap(NotificationHandler::support, Function.identity()));

    }

    @KafkaListener(topics = NOTIFICATION_EVENTS, groupId = "orchestrator-group")
    public void handle(NotificationEvent event) {

        User user = userRepository.findById(event.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("user not found: " + event.getUserId()));

        NotificationHandler handler = handlers.get(event.getEventType());

        if (handler == null) {
            log.warn("No handler for eventType: {}", event.getEventType());
            return;
        }

        for (DispatchPayload.Channel channel : event.getChannels()) {
            if (channel == PUSH && user.getDeviceToken() == null) {
                log.warn("Skipping PUSH for userId: {} — no device token", event.getUserId());
                continue;
            }

            handler.resolve(event, channel, user)
                    .ifPresentOrElse(
                            payload -> {
                                kafkaTemplate.send(NOTIFICATION_DISPATCH,
                                        String.valueOf(event.getUserId()), payload);
                                log.info("Dispatched {} for userId: {}", channel, event.getUserId());
                            },
                            () -> log.warn("No payload resolved for {} / {}",
                                    event.getEventType(), channel)
                    );
        }
    }
}
