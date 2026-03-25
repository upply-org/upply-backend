package com.upply.chat.dto;

import lombok.Builder;
import org.springframework.ai.chat.messages.MessageType;

import java.time.LocalDateTime;

@Builder
public record ChatMessageResponse(MessageType role,
                                  String content,
                                  LocalDateTime timestamp)
{ }
