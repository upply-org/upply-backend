package com.upply.notification.handler;

import com.upply.common.NotificationEventType;
import com.upply.email.EmailTemplate;
import com.upply.notification.dto.DispatchPayload;
import com.upply.notification.dto.NotificationEvent;
import com.upply.user.User;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class JobPostedSuccessfullyHandler implements NotificationHandler {
    @Override
    public NotificationEventType support() {
        return NotificationEventType.JOB_POSTED_SUCCESSFULLY;
    }

    @Override
    public Optional<DispatchPayload> resolve(NotificationEvent event, DispatchPayload.Channel channel, User user) {
        Map<String, Object> p = event.getPayload();

        return switch (channel) {
            case EMAIL -> Optional.of(DispatchPayload.builder()
                    .channel(DispatchPayload.Channel.EMAIL)
                    .to(user.getEmail())
                    .subject("Your job '" + p.get("jobTitle") + "' is now live! 🚀")
                    .template(EmailTemplate.JOB_POSTED_SUCCESSFULLY)
                    .templateVariables(Map.of(
                            "firstName", user.getFirstName(),
                            "jobTitle", p.get("jobTitle"),
                            "jobType", p.get("jobType"),
                            "seniority", p.get("seniority"),
                            "location", p.get("location"),
                            "jobUrl", p.get("jobUrl")
                    ))
                    .build());
            default -> Optional.empty();
        };
    }
}
