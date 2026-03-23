package com.upply.notification.handler;

import com.upply.common.NotificationEventType;
import com.upply.notification.dto.DispatchPayload;
import com.upply.notification.dto.NotificationEvent;
import com.upply.user.User;

import java.util.Optional;

public interface NotificationHandler {
    NotificationEventType  support();
    Optional<DispatchPayload> resolve(NotificationEvent event,
                                      DispatchPayload.Channel channel,
                                      User user);
}
