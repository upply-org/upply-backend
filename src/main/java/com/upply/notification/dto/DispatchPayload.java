package com.upply.notification.dto;

import com.upply.email.EmailTemplate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispatchPayload {
    public enum Channel{
        EMAIL,
        PUSH
    }
    private Channel channel;
    private String to;

    // EMAIL fields
    private String subject;
    private EmailTemplate template;
    private Map<String, Object> templateVariables;

    // PUSH fields
    private String title;
    private String body;
    private String redirectTo;
    private String eventType;
}
