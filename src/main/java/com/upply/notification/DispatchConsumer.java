package com.upply.notification;

import com.upply.email.EmailService;
import com.upply.notification.dto.DispatchPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import static com.upply.config.KafkaConfig.NOTIFICATION_DISPATCH;

@Slf4j
@Service
@RequiredArgsConstructor
public class DispatchConsumer {

    private final EmailService emailService;
    private final PushService pushService;

    @KafkaListener(topics = NOTIFICATION_DISPATCH, groupId = "dispatch-group")
    public void handle(DispatchPayload payload) {
        try {
            switch (payload.getChannel()) {
                case EMAIL -> {
                    log.info("Sending EMAIL to {}", payload.getTo());
                    emailService.sendEmail(
                            payload.getTo(),
                            payload.getSubject(),
                            payload.getTemplate(),
                            payload.getTemplateVariables()
                    );
                }
                case PUSH -> {
                    log.info("Sending PUSH to device: {}", payload.getTo());
                    pushService.send(payload);
                }
            }
        } catch (Exception e) {
            log.error("Failed to dispatch {} notification to {} | reason: {}",
                    payload.getChannel(), payload.getTo(), e.getMessage());
        }
    }
}