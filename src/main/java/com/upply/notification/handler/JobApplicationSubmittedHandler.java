package com.upply.notification.handler;

import com.upply.common.NotificationEventType;
import com.upply.email.EmailTemplate;
import com.upply.notification.dto.DispatchPayload;
import com.upply.notification.dto.NotificationEvent;
import com.upply.user.User;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

import static com.upply.notification.dto.DispatchPayload.Channel.EMAIL;
import static com.upply.notification.dto.DispatchPayload.Channel.PUSH;

@Component
public class JobApplicationSubmittedHandler implements NotificationHandler {
    @Override
    public NotificationEventType support() {
        return NotificationEventType.JOB_APPLICATION_SUBMITTED;
    }

    @Override
    public Optional<DispatchPayload> resolve(NotificationEvent event, DispatchPayload.Channel channel, User user) {
        Map<String, Object> p = event.getPayload();

        return switch (channel) {
            case EMAIL -> Optional.of(DispatchPayload.builder()
                    .channel(EMAIL)
                    .to(user.getEmail())
                    .subject("Your application to " + p.get("company") + " has been submitted!")
                    .template(EmailTemplate.JOB_APPLICATION_SUBMITTED)
                    .templateVariables(Map.of(
                            "firstName", user.getFirstName(),
                            "jobTitle", p.get("jobTitle"),
                            "company", p.get("company"),
                            "status", p.get("status")
                    ))
                    .build());

            case PUSH -> Optional.of(DispatchPayload.builder()
                    .channel(PUSH)
                    .to(user.getDeviceToken())
                    .title("Application Submitted")
                    .body("Your application for " + p.get("jobTitle") + " at " + p.get("company") + " has been submitted!")
                    .redirectTo("/my-applications")
                    .build());

            default -> Optional.empty();
        };
    }
}
