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
public class NewMatchedJobsHandler implements NotificationHandler {
    @Override
    public NotificationEventType support() {
        return NotificationEventType.NEW_MATCHED_JOBS;
    }

    @Override
    public Optional<DispatchPayload> resolve(NotificationEvent event, DispatchPayload.Channel channel, User user) {
        Map<String, Object> p = event.getPayload();

        return switch (channel) {
            case EMAIL -> Optional.of(DispatchPayload.builder()
                    .channel(EMAIL)
                    .to(user.getEmail())
                    .subject("New job matches your skills - " + p.get("jobTitle"))
                    .template(EmailTemplate.NEW_MATCHED_JOBS)
                    .templateVariables(Map.of(
                            "firstName", user.getFirstName(),
                            "jobTitle", p.get("jobTitle"),
                            "jobCompany", p.get("jobCompany"),
                            "jobUrl", p.get("jobUrl")
                    ))
                    .build());

            case PUSH -> {
                String jobTitle = p.get("jobTitle").toString();
                String company = p.get("jobCompany").toString();
                yield Optional.of(DispatchPayload.builder()
                        .channel(PUSH)
                        .to(user.getDeviceToken())
                        .title("New Job Alert: " + jobTitle)
                        .body(company + " is hiring for " + jobTitle + ". Check it out!")
                        .redirectTo("/jobs?id=" + p.get("jobId"))
                        .build());
            }

            default -> Optional.empty();
        };
    }
}