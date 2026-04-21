package com.upply.job;

import com.upply.user.User;
import com.upply.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("JobMatchingService unit tests")
class JobMatchingServiceTest {

    @Mock
    private VectorStore jobsVectorStore;

    @Mock
    private VectorStore userSkillsVectorStore;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private UserRepository userRepository;

    private JobMatchingService jobMatchingService;

    private Job testJob;
    private User testUser;

    @BeforeEach
    void setUp() {
        jobMatchingService = new JobMatchingService(jobsVectorStore, userSkillsVectorStore, jobRepository, userRepository);
        
        testJob = mock(Job.class);
        testUser = mock(User.class);
        lenient().when(testJob.getId()).thenReturn(1L);
        lenient().when(testJob.getTitle()).thenReturn("Software Engineer");
        lenient().when(testJob.getType()).thenReturn(com.upply.job.enums.JobType.FULL_TIME);
        lenient().when(testJob.getSeniority()).thenReturn(com.upply.job.enums.JobSeniority.SENIOR);
        lenient().when(testJob.getModel()).thenReturn(com.upply.job.enums.JobModel.REMOTE);
        lenient().when(testJob.getLocation()).thenReturn("Remote");
        lenient().when(testJob.getStatus()).thenReturn(com.upply.job.enums.JobStatus.OPEN);

        lenient().when(testUser.getId()).thenReturn(1L);
    }

    @AfterEach
    void tearDown() {
        clearInvocations(jobsVectorStore, userSkillsVectorStore, jobRepository, userRepository);
    }

    @Test
    @DisplayName("storeJobEmbedding - success")
    void storeJobEmbedding_Success() {
        when(jobRepository.findJobSkillNames(1L)).thenReturn(List.of("Java", "Spring Boot"));

        jobMatchingService.storeJobEmbedding(testJob);

        verify(jobsVectorStore).add(anyList());
    }

    @Test
    @DisplayName("storeJobEmbedding - throws runtime exception on error")
    void storeJobEmbedding_ThrowsRuntimeException() {
        when(jobRepository.findJobSkillNames(1L)).thenReturn(List.of("Java", "Spring Boot"));
        doThrow(new RuntimeException("Vector store error")).when(jobsVectorStore).add(anyList());

        assertThrows(RuntimeException.class, () -> jobMatchingService.storeJobEmbedding(testJob));
    }

    @Test
    @DisplayName("deleteJobEmbedding - success")
    void deleteJobEmbedding_Success() {
        Long jobId = 1L;

        jobMatchingService.deleteJobEmbedding(jobId);

        verify(jobsVectorStore).delete(eq(List.of(String.valueOf(jobId))));
    }

    @Test
    @DisplayName("deleteJobEmbedding - handles exception gracefully")
    void deleteJobEmbedding_HandlesException() {
        Long jobId = 1L;
        doThrow(new RuntimeException("Delete error")).when(jobsVectorStore).delete(anyList());

        assertDoesNotThrow(() -> jobMatchingService.deleteJobEmbedding(jobId));
    }

    @Test
    @DisplayName("findSimilarJobs - returns matching jobs with scores")
    void findSimilarJobs_ReturnsMatchingJobs() {
        when(userRepository.findUserSkillNames(1L)).thenReturn(List.of("Java", "Spring Boot"));

        Document doc1 = new Document("1", "Job: Software Engineer", Map.of("jobId", "1"));
        Document doc2 = new Document("2", "Job: Backend Developer", Map.of("jobId", "2"));

        when(jobsVectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of(doc1, doc2));

        when(jobRepository.findAllById(anyList())).thenReturn(List.of(testJob));

        List<JobMatchingService.JobWithScore> result = jobMatchingService.findSimilarJobs(testUser, 5);

        assertNotNull(result);
        verify(jobsVectorStore).similaritySearch(any(SearchRequest.class));
    }

    @Test
    @DisplayName("findSimilarJobs - throws runtime exception on error")
    void findSimilarJobs_ThrowsRuntimeException() {
        when(userRepository.findUserSkillNames(1L)).thenReturn(List.of("Java", "Spring Boot"));
        when(jobsVectorStore.similaritySearch(any(SearchRequest.class)))
                .thenThrow(new RuntimeException("Vector store error"));

        assertThrows(RuntimeException.class, () -> jobMatchingService.findSimilarJobs(testUser, 5));
    }

    @Test
    @DisplayName("calculateMatchScore - returns score between 0 and 1")
    void calculateMatchScore_ReturnsScore() {
        when(userRepository.findUserSkillNames(1L)).thenReturn(List.of("Java", "Spring Boot"));

        Document doc = mock(Document.class);
        when(doc.getId()).thenReturn("1");
        when(doc.getScore()).thenReturn(0.85);

        when(jobsVectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of(doc));

        double score = jobMatchingService.calculateMatchScore(testUser, testJob);

        assertTrue(score >= 0.0 && score <= 1.0);
    }

    @Test
    @DisplayName("calculateMatchScore - returns 0 when no matching document found")
    void calculateMatchScore_ReturnsZeroWhenNotFound() {
        when(userRepository.findUserSkillNames(1L)).thenReturn(List.of("Java", "Spring Boot"));

        when(jobsVectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of());

        double score = jobMatchingService.calculateMatchScore(testUser, testJob);

        assertEquals(0.0, score, 0.0);
    }

    @Test
    @DisplayName("calculateMatchScore - handles exception and returns 0")
    void calculateMatchScore_HandlesException() {
        when(userRepository.findUserSkillNames(1L)).thenReturn(List.of("Java", "Spring Boot"));
        when(jobsVectorStore.similaritySearch(any(SearchRequest.class)))
                .thenThrow(new RuntimeException("Error"));

        double score = jobMatchingService.calculateMatchScore(testUser, testJob);

        assertEquals(0.0, score, 0.0);
    }

    @Test
    @DisplayName("findMatchingUsers - returns matching users for job")
    void findMatchingUsers_ReturnsMatchingUsers() {
        when(jobRepository.findJobSkillNames(1L)).thenReturn(List.of("Java", "Spring Boot"));

        Document doc = new Document("1", "User: John Doe", Map.of("userId", "1"));
        Document doc2 = new Document("2", "User: Jane Smith", Map.of("userId", "2"));

        when(userSkillsVectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of(doc, doc2));

        when(userRepository.findAllById(anyList())).thenReturn(List.of(testUser));

        List<User> result = jobMatchingService.findMatchingUsers(testJob, 5);

        assertNotNull(result);
        verify(userSkillsVectorStore).similaritySearch(any(SearchRequest.class));
    }

    @Test
    @DisplayName("findMatchingUsers - throws runtime exception on error")
    void findMatchingUsers_ThrowsRuntimeException() {
        when(jobRepository.findJobSkillNames(1L)).thenReturn(List.of("Java", "Spring Boot"));
        when(userSkillsVectorStore.similaritySearch(any(SearchRequest.class)))
                .thenThrow(new RuntimeException("Vector store error"));

        assertThrows(RuntimeException.class, () -> jobMatchingService.findMatchingUsers(testJob, 5));
    }

    @Test
    @DisplayName("JobWithScore - getter methods work correctly")
    void jobWithScore_GetterMethods() {
        JobMatchingService.JobWithScore jobWithScore = new JobMatchingService.JobWithScore(testJob, 0.85);

        assertEquals(testJob, jobWithScore.getJob());
        assertEquals(0.85, jobWithScore.getScore(), 0.0);
    }
}