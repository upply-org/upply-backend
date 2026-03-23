package com.upply.notification.dto;

import com.upply.common.NotificationEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
    private String eventId;
    private NotificationEventType eventType;
    private Long userId;
    @Builder.Default
    private List<DispatchPayload.Channel> channels= new ArrayList<>();
    private Map<String, Object> payload;
}
