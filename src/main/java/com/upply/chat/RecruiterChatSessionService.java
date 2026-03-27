package com.upply.chat;


import com.upply.chat.dto.ChatMessageResponse;
import com.upply.chat.dto.CreateSessionRequest;
import com.upply.chat.dto.RecruiterChatMapper;
import com.upply.chat.dto.SessionResponse;
import com.upply.exception.custom.OperationNotPermittedException;
import com.upply.exception.custom.ResourceNotFoundException;
import com.upply.job.Job;
import com.upply.job.JobRepository;
import com.upply.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
@Slf4j
public class RecruiterChatSessionService {
    private final VectorStore vectorStore;
    private final ChatClient geminiChatClient;
    private final ChatClient groqChatClient;
    private final ChatMemory chatMemory;
    private final RecruiterChatSessionRepository sessionRepository;
    private final RecruiterChatMapper recruiterChatMapper;
    private final JobRepository jobRepository;

    private static final int TOP_K = 50;
    private static final double SIMILARITY_THRESHOLD = 0.5;
    private static final String NO_CANDIDATES = "No relevant candidates found for this query.";

    public RecruiterChatSessionService(@Qualifier("resumeVectorStore") VectorStore vectorStore,
                                       @Qualifier("recruiterRagGeminiChatClient") ChatClient geminiChatClient,
                                       @Qualifier("recruiterRagGroqChatClient") ChatClient groqChatClient,
                                       ChatMemory chatMemory,
                                       RecruiterChatSessionRepository sessionRepository,
                                       RecruiterChatMapper recruiterChatMapper,
                                       JobRepository jobRepository) {
        this.vectorStore = vectorStore;
        this.geminiChatClient = geminiChatClient;
        this.groqChatClient = groqChatClient;
        this.chatMemory = chatMemory;
        this.sessionRepository = sessionRepository;
        this.jobRepository = jobRepository;
        this.recruiterChatMapper = recruiterChatMapper;
    }

    public SessionResponse createSession(Authentication connectedUser, CreateSessionRequest request) {
        User user = (User) connectedUser.getPrincipal();
        Job job = jobRepository.findById(request.jobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job with ID " + request.jobId() + " not found"));

        if (!Objects.equals(job.getPostedBy().getId(), user.getId())) {
            throw new OperationNotPermittedException("You are not permitted to start session for this job");
        }

        RecruiterChatSession recruiterChatSession = recruiterChatMapper.toChatSession(request);

        recruiterChatSession.setJob(job);
        sessionRepository.save(recruiterChatSession);


        return recruiterChatMapper.toSessionResponse(recruiterChatSession);
    }


    public List<SessionResponse> getSessions() {
        return sessionRepository
                .findMySessions()
                .stream()
                .map(recruiterChatMapper::toSessionResponse)
                .toList();
    }

    @Transactional
    public void deleteSession(String sessionId) {

        RecruiterChatSession session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("No Session Found with this id"));
        chatMemory.clear(sessionId);
        sessionRepository.delete(session);
    }

    public List<ChatMessageResponse> getSessionMessages(String sessionId) {

        sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("No Session Found with this id"));

        return chatMemory.get(sessionId)
                .stream()
                .map(recruiterChatMapper::toChatMessageResponse)
                .toList();
    }

    public Flux<String> streamChat(String sessionId, String prompt) {
        RecruiterChatSession session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("No Session Found with this id"));

        Long jobId = session.getJob().getId();

        String candidateCtx = buildCandidateContext(jobId, prompt);
        String jobCtx = buildJobContext(jobId);

        return callAiStream(geminiChatClient, sessionId, prompt, jobCtx, candidateCtx)
                .doOnComplete(() -> log.info(
                        "AI_STREAM_SUCCESS provider=gemini sessionId={} jobId={}",
                        sessionId, jobId
                ))
                .onErrorResume(e -> {
                        log.warn(
                                "AI_FALLBACK provider=gemini sessionId={} jobId={} errorType={} message={}",
                                sessionId,
                                jobId,
                                e.getClass().getSimpleName(),
                                e.getMessage(),
                                e
                        );

                        return callAiStream(groqChatClient, sessionId, prompt, jobCtx, candidateCtx)
                                .doOnSubscribe(s -> log.info(
                                        "AI_FALLBACK_EXEC provider=groq sessionId={} jobId={}",
                                        sessionId, jobId
                                ))
                                .doOnComplete(() -> log.info(
                                        "AI_STREAM_SUCCESS provider=groq sessionId={} jobId={}",
                                        sessionId, jobId
                                ));
                });
    }

    private Flux<String> callAiStream(ChatClient client, String sessionId, String prompt, String jobCtx, String candidateCtx) {
        return client.prompt()
                .system(s -> s
                        .param("job_context", jobCtx)
                        .param("candidate_context", candidateCtx)
                        .param("query", prompt))
                .user(prompt)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessionId))
                .stream()
                .content();
    }

    private String buildCandidateContext(Long jobId, String prompt) {
        log.info("Searching for candidates with jobId: {}", jobId);

        List<Document> docs = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(prompt)
                        .topK(TOP_K)
                        .similarityThreshold(SIMILARITY_THRESHOLD)
                        .filterExpression("jobId == '" + jobId + "'")
                        .build()
        );

        log.info("Found {} documents for jobId: {}", docs.size(), jobId);

        if (docs.isEmpty()) return NO_CANDIDATES;

        return docs.stream()
                .filter(doc -> doc.getMetadata() != null && doc.getMetadata().get("userId") != null)
                .collect(Collectors.groupingBy(
                        doc -> doc.getMetadata().get("userId").toString()
                ))
                .values().stream()
                .map(this::bestDocPerApplicant)
                .map(this::formatCandidate)
                .collect(Collectors.joining("\n---\n"));
    }

    private Document bestDocPerApplicant(List<Document> docs) {
        return docs.stream().findFirst().orElseThrow();
    }

    private String formatCandidate(Document doc) {
        Map<String, Object> meta = doc.getMetadata();
        return """
                Candidate ID: %s
                Application ID: %s
                Type: %s
                %s
                """.formatted(
                meta.get("userId"),
                meta.get("applicationId"),
                meta.get("chunkType"),
                doc.getText()
        );
    }

    private String buildJobContext(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job with ID " + jobId + " not found"));

        return String.format("""
                        Job Title: %s
                        Type: %s
                        Seniority: %s
                        Work Model: %s
                        Location: %s
                        Description: %s
                        Required Skills: %s
                        """,
                job.getTitle(),
                job.getType(),
                job.getSeniority(),
                job.getModel(),
                job.getLocation(),
                job.getDescription(),
                jobRepository.findJobSkillNames(jobId).stream()
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("None")
        );
    }

}
