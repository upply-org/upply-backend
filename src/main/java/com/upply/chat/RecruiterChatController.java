package com.upply.chat;

import com.upply.chat.dto.ChatMessageResponse;
import com.upply.chat.dto.CreateSessionRequest;
import com.upply.chat.dto.SessionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/jobs/chat")
@RequiredArgsConstructor
@Tag(name = "Recruiter Chat", description = "APIs for recruiter chat with candidates using RAG")
@Validated
public class RecruiterChatController {

    private final RecruiterChatSessionService chatService;

    @PostMapping("/sessions")
    @Operation(
            summary = "Create a new chat session",
            description = "Creates a new chat session for a specific job. Requires authentication."
    )
    public ResponseEntity<SessionResponse> createSession(
            @Parameter(description = "Request containing job ID and first prompt", required = true)
            @RequestBody @Valid CreateSessionRequest request,
            Authentication auth) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(chatService.createSession(auth, request));
    }

    @GetMapping("/sessions")
    @Operation(
            summary = "Get all chat sessions",
            description = "Retrieves all chat sessions for the authenticated recruiter."
    )
    public ResponseEntity<List<SessionResponse>> getSessions() {
        return ResponseEntity.ok(chatService.getSessions());
    }

    @DeleteMapping("/sessions/{sessionId}")
    @Operation(
            summary = "Delete a chat session",
            description = "Deletes a chat session and its conversation history. Requires authentication."
    )
    public ResponseEntity<Void> deleteSession(
            @Parameter(description = "The session ID to delete", required = true, example = "abc-123")
            @PathVariable String sessionId) {
        chatService.deleteSession(sessionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sessions/{sessionId}/messages")
    @Operation(
            summary = "Get session messages",
            description = "Retrieves all messages from a specific chat session."
    )
    public ResponseEntity<List<ChatMessageResponse>> getMessages(
            @Parameter(description = "The session ID", required = true, example = "abc-123")
            @PathVariable String sessionId) {

        return ResponseEntity.ok(chatService.getSessionMessages(sessionId));
    }

    @GetMapping(
            value = "/sessions/{sessionId}/stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    @Operation(
            summary = "Stream chat response",
            description = "Sends a question and streams back the AI response using Server-Sent Events. Uses RAG to provide context from candidate resumes."
    )
    public Flux<ServerSentEvent<String>> streamChat(
            @Parameter(description = "The session ID", required = true, example = "abc-123")
            @PathVariable String sessionId,
            @Parameter(description = "The question to ask about candidates", required = true, example = "Find candidates with Python experience")
            @RequestParam @NotBlank String question) {

        return chatService.streamChat(sessionId, question)
                .map(token -> ServerSentEvent.<String>builder()
                        .event("token")
                        .data(token)
                        .build()
                )
                .concatWith(Flux.just(
                        ServerSentEvent.<String>builder()
                                .event("done")
                                .data("[DONE]")
                                .build()
                ))
                .onErrorResume(ex -> Flux.just(
                        ServerSentEvent.<String>builder()
                                .event("error")
                                .data("An error occurred while processing your request")
                                .build()
                ));
    }
}
