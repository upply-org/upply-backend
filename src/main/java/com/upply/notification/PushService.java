package com.upply.notification;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.upply.notification.dto.DispatchPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PushService {
    public void send(DispatchPayload payload) {
        Thread.startVirtualThread(() -> {
            try {
                Message message = Message.builder()
                        .setToken(payload.getTo())
                        .setNotification(Notification.builder()
                                .setTitle(payload.getTitle())
                                .setBody(payload.getBody())
                                .build())
                        .putData("redirectTO", payload.getRedirectTo() != null ? payload.getRedirectTo() : "")
                        .putData("eventType", payload.getEventType() != null ? payload.getEventType() : "")
                        .build();
                String response = FirebaseMessaging.getInstance().send(message);
                log.info("Push sent successfully | messageId: {}", response);
            } catch (FirebaseMessagingException e) {
                log.error("Failed to send push notification to {} | reason: {}",
                        payload.getTo(), e.getMessage());
            }
        });
    }
}
