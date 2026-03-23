package com.upply.notification.handler;

import com.upply.common.NotificationEventType;
import com.upply.email.EmailTemplate;
import com.upply.notification.dto.DispatchPayload;
import com.upply.notification.dto.NotificationEvent;
import com.upply.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class JobApplicationUpdatedHandler implements NotificationHandler {

    @Override
    public NotificationEventType support() {
        return NotificationEventType.JOB_APPLICATION_UPDATED;
    }

    @Override
    public Optional<DispatchPayload> resolve(NotificationEvent event, DispatchPayload.Channel channel, User user) {
        Map<String, Object> p = event.getPayload();

        return switch (channel) {
            case EMAIL -> Optional.of(DispatchPayload.builder()
                    .channel(DispatchPayload.Channel.EMAIL)
                    .to(user.getEmail())
                    .subject("Update on your application at " + p.get("company"))
                    .template(EmailTemplate.JOB_APPLICATION_UPDATED)
                    .templateVariables(Map.of(
                            "firstName", user.getFirstName(),
                            "jobTitle", p.get("jobTitle"),
                            "company", p.get("company"),
                            "status", p.get("status"),
                            "applicationUrl", "https://upply.com/my-applications"
                    ))
                    .build());


            case PUSH -> Optional.of(DispatchPayload.builder()
                    .channel(DispatchPayload.Channel.PUSH)
                    .to(user.getDeviceToken())
                    .title("Application Update ")
                    .body(p.get("company") + " updated your application to '" + p.get("status") + "'")
                    .redirectTo("/my-applications")
                    .build());

            default -> Optional.empty();
        };
    }
}
