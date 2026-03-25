package com.upply.chat.dto;

import com.upply.chat.RecruiterChatSession;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.springframework.util.StringUtils.truncate;

@Service
@RequiredArgsConstructor
public class RecruiterChatMapper {

    public SessionResponse toSessionResponse(RecruiterChatSession session) {
        return SessionResponse.builder()
                .sessionId(session.getSessionId())
                .jobId(session.getJob().getId())
                .title(session.getTitle())
                .createdAt(session.getCreatedAt())
                .build();
    }

    public RecruiterChatSession toChatSession(CreateSessionRequest request){
        return RecruiterChatSession.builder()
                .sessionId(UUID.randomUUID().toString())
                .title(truncate(request.firstPrompt(), 60))
                .build();
    }

    public List<SessionResponse> toSessionResponseList(List<RecruiterChatSession> sessions) {
        return sessions.stream()
                .map(this::toSessionResponse)
                .toList();
    }


    public ChatMessageResponse toChatMessageResponse(Message message) {
        return ChatMessageResponse.builder()
                .role(message.getMessageType())
                .content(message.getText())
                .timestamp(LocalDateTime.now())
                .build();
    }

    public List<ChatMessageResponse> toChatMessageResponseList(List<Message> messages) {
        return messages.stream()
                .filter(m -> !m.getMessageType().name().equals("SYSTEM"))
                .map(this::toChatMessageResponse)
                .toList();
    }
}
