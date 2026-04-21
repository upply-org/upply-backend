package com.upply.job;

import com.upply.application.ApplicationRepository;
import com.upply.application.dto.ApplicationMapper;
import com.upply.exception.custom.BusinessLogicException;
import com.upply.exception.custom.OperationNotPermittedException;
import com.upply.exception.custom.ResourceNotFoundException;
import com.upply.job.dto.JobFilter;
import com.upply.job.dto.JobMapper;
import com.upply.job.dto.JobRequest;
import com.upply.job.dto.JobResponse;
import com.upply.job.dto.JobUpdateRequest;
import com.upply.job.dto.PostJobEvent;
import com.upply.job.enums.JobSeniority;
import com.upply.job.enums.JobSource;
import com.upply.job.enums.JobStatus;
import com.upply.job.enums.JobType;
import com.upply.job.enums.JobModel;
import com.upply.job.ApplicationExcelExportService;
import com.upply.job.dto.ExportTaskMapper;
import com.upply.job.dto.MatchedJobListResponse;
import com.upply.job.dto.JobListResponse;
import com.upply.notification.dto.NotificationEvent;
import com.upply.profile.skill.Skill;
import com.upply.profile.skill.SkillRepository;
import com.upply.user.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("JobService unit tests")
class JobServiceTest {

    @Mock
    private JobMapper jobMapper;

    @Mock
    private ApplicationMapper applicationMapper;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private JobMatchingService jobMatchingService;

    @Mock
    private ApplicationExcelExportService applicationExcelExportService;

    @Mock
    private ExportTaskMapper exportTaskMapper;

    @Mock
    private KafkaTemplate<String, NotificationEvent> notificationKafkaTemplate;

    @Mock
    private JobParserService jobParserService;

    @Mock
    private KafkaTemplate<String, PostJobEvent> postJobEventKafkaTemplate;

    @InjectMocks
    private JobService jobService;

    @Mock
    private User testUser;

    @Mock
    private Job testJob;

    @Mock
    private Skill testSkill;

    private Authentication mockAuthentication;

    @BeforeEach
    void setUp() {
        TransactionSynchronizationManager.initSynchronization();

        when(testUser.getId()).thenReturn(1L);
        when(testSkill.getId()).thenReturn(1L);
        when(testJob.getId()).thenReturn(1L);
        when(testJob.getTitle()).thenReturn("Software Engineer");
        when(testJob.getStatus()).thenReturn(JobStatus.OPEN);
        when(testJob.getPostedBy()).thenReturn(testUser);
        
        mockAuthentication = mock(Authentication.class);
        when(mockAuthentication.getPrincipal()).thenReturn(testUser);
    }

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    @DisplayName("createJob should create job successfully")
    void shouldCreateJobSuccessfully() {
        JobRequest request = mock(JobRequest.class);
        when(request.getSkillIds()).thenReturn(Set.of(1L));
        
        List<Skill> skills = List.of(testSkill);
        JobResponse jobResponse = mock(JobResponse.class);
        
        when(skillRepository.findAllById(Set.of(1L))).thenReturn(new ArrayList<>(skills));
        when(jobMapper.toJob(any(JobRequest.class), anySet())).thenReturn(testJob);
        when(jobRepository.save(any(Job.class))).thenReturn(testJob);
        when(jobMapper.toJobResponse(any(Job.class))).thenReturn(jobResponse);

        JobResponse result = jobService.createJob(request, mockAuthentication);

        assertNotNull(result);
        verify(skillRepository).findAllById(Set.of(1L));
        verify(jobMapper).toJob(any(JobRequest.class), anySet());
    }

    @Test
    @DisplayName("createJob should throw ResourceNotFoundException when skill not found")
    void shouldThrowExceptionWhenSkillNotFoundInCreateJob() {
        JobRequest request = mock(JobRequest.class);
        when(request.getSkillIds()).thenReturn(Set.of(1L));
        
        doReturn(Collections.emptyList()).when(skillRepository).findAllById(anySet());

        assertThrows(ResourceNotFoundException.class, 
            () -> jobService.createJob(request, mockAuthentication));
    }

    @Test
    @DisplayName("getJob should return JobResponse when job exists")
    void shouldGetJobSuccessfully() {
        JobResponse jobResponse = mock(JobResponse.class);
        
        when(jobRepository.findById(1L)).thenReturn(Optional.of(testJob));
        when(jobMapper.toJobResponse(testJob)).thenReturn(jobResponse);

        JobResponse result = jobService.getJob(1L);

        assertNotNull(result);
        verify(jobRepository).findById(1L);
    }

    @Test
    @DisplayName("getJob should throw ResourceNotFoundException when job not found")
    void shouldThrowExceptionWhenJobNotFoundInGetJob() {
        when(jobRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> jobService.getJob(999L));

        assertTrue(exception.getMessage().contains("999"));
    }

    @Test
    @DisplayName("getAllOpenJobs should return paginated results")
    void shouldGetAllOpenJobsSuccessfully() {
        Page<Job> jobPage = new PageImpl<>(List.of(testJob));
        
        when(jobRepository.findByStatus(eq(JobStatus.OPEN), any(Pageable.class)))
                .thenReturn(jobPage);
        when(jobMapper.toJobListResponse(any(Job.class))).thenReturn(mock(JobListResponse.class));

        var result = jobService.getAllOpenJobs(0, 10, mockAuthentication);

        assertNotNull(result);
    }

    @Test
    @DisplayName("searchJobs should return jobs with filters")
    void shouldSearchJobsSuccessfully() {
        JobFilter filter = new JobFilter();
        Page<Job> jobPage = new PageImpl<>(List.of(testJob));
        
        when(jobRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
                .thenReturn(jobPage);
        when(jobMapper.toJobListResponse(any(Job.class))).thenReturn(mock(JobListResponse.class));

        var result = jobService.searchJobs(0, 10, filter);

        assertNotNull(result);
    }

    @Test
    @DisplayName("getMatchedJobs should return matched jobs")
    void shouldGetMatchedJobsSuccessfully() {
        JobMatchingService.JobWithScore jobWithScore = new JobMatchingService.JobWithScore(testJob, 0.85);
        
        when(jobMatchingService.findSimilarJobs(any(User.class), anyInt())).thenReturn(List.of(jobWithScore));
        when(jobMapper.toMatchedJobListResponse(any(Job.class), anyDouble())).thenReturn(mock(MatchedJobListResponse.class));

        var result = jobService.getMatchedJobs(mockAuthentication);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("closeJob should close job successfully")
    void shouldCloseJobSuccessfully() {
        when(testJob.getStatus()).thenReturn(JobStatus.OPEN);
        when(testJob.getPostedBy()).thenReturn(testUser);
        
        JobResponse jobResponse = mock(JobResponse.class);
        
        when(jobRepository.findById(1L)).thenReturn(Optional.of(testJob));
        when(jobRepository.save(any(Job.class))).thenReturn(testJob);
        when(jobMapper.toJobResponse(any(Job.class))).thenReturn(jobResponse);

        JobResponse result = jobService.closeJob(1L, mockAuthentication);

        assertNotNull(result);
        verify(jobMatchingService).deleteJobEmbedding(1L);
    }

    @Test
    @DisplayName("pauseJob should pause job successfully")
    void shouldPauseJobSuccessfully() {
        when(testJob.getStatus()).thenReturn(JobStatus.OPEN);
        when(testJob.getPostedBy()).thenReturn(testUser);
        
        JobResponse jobResponse = mock(JobResponse.class);
        
        when(jobRepository.findById(1L)).thenReturn(Optional.of(testJob));
        when(jobRepository.save(any(Job.class))).thenReturn(testJob);
        when(jobMapper.toJobResponse(any(Job.class))).thenReturn(jobResponse);

        JobResponse result = jobService.pauseJob(1L, mockAuthentication);

        assertNotNull(result);
    }

    @Test
    @DisplayName("resumeJob should resume job successfully")
    void shouldResumeJobSuccessfully() {
        when(testJob.getStatus()).thenReturn(JobStatus.PAUSED);
        when(testJob.getPostedBy()).thenReturn(testUser);
        when(testJob.getType()).thenReturn(JobType.FULL_TIME);
        when(testJob.getSeniority()).thenReturn(JobSeniority.MID);
        when(testJob.getTitle()).thenReturn("Software Engineer");
        
        JobResponse jobResponse = mock(JobResponse.class);
        
        when(jobRepository.findById(1L)).thenReturn(Optional.of(testJob));
        when(jobRepository.save(any(Job.class))).thenReturn(testJob);
        when(jobMapper.toJobResponse(any(Job.class))).thenReturn(jobResponse);

        JobResponse result = jobService.resumeJob(1L, mockAuthentication);

        assertNotNull(result);
    }

    @Test
    @DisplayName("updateJob should update job successfully")
    void shouldUpdateJobSuccessfully() {
        JobUpdateRequest updateRequest = mock(JobUpdateRequest.class);
        when(updateRequest.getTitle()).thenReturn("Senior Engineer");
        when(updateRequest.getSkillIds()).thenReturn(Set.of(1L));
        
        when(testJob.getPostedBy()).thenReturn(testUser);
        
        JobResponse jobResponse = mock(JobResponse.class);
        
        when(jobRepository.findById(1L)).thenReturn(Optional.of(testJob));
        when(skillRepository.findAllById(Set.of(1L))).thenReturn(new ArrayList<>(List.of(testSkill)));
        when(jobRepository.save(any(Job.class))).thenReturn(testJob);
        when(jobMapper.toJobResponse(any(Job.class))).thenReturn(jobResponse);

        JobResponse result = jobService.updateJob(1L, updateRequest, mockAuthentication);

        assertNotNull(result);
    }

    @Test
    @DisplayName("updateJob should handle partial update")
    void shouldHandlePartialUpdateInUpdateJob() {
        JobUpdateRequest partialRequest = mock(JobUpdateRequest.class);
        when(partialRequest.getTitle()).thenReturn("Updated Title");
        when(partialRequest.getType()).thenReturn(null);
        when(partialRequest.getSeniority()).thenReturn(null);
        when(partialRequest.getModel()).thenReturn(null);
        when(partialRequest.getLocation()).thenReturn(null);
        when(partialRequest.getDescription()).thenReturn(null);
        when(partialRequest.getSkillIds()).thenReturn(null);
        
        when(testJob.getPostedBy()).thenReturn(testUser);
        
        JobResponse jobResponse = mock(JobResponse.class);
        
        when(jobRepository.findById(1L)).thenReturn(Optional.of(testJob));
        when(jobRepository.save(any(Job.class))).thenReturn(testJob);
        when(jobMapper.toJobResponse(any(Job.class))).thenReturn(jobResponse);

        JobResponse result = jobService.updateJob(1L, partialRequest, mockAuthentication);

        assertNotNull(result);
    }

    @Test
    @DisplayName("updateJob should throw ResourceNotFoundException when job not found")
    void shouldThrowExceptionWhenJobNotFoundInUpdateJob() {
        JobUpdateRequest updateRequest = mock(JobUpdateRequest.class);
        
        when(jobRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> jobService.updateJob(999L, updateRequest, mockAuthentication));
    }

    @Test
    @DisplayName("updateJob should throw OperationNotPermittedException when user is not owner")
    void shouldThrowExceptionWhenUnauthorizedInUpdateJob() {
        User differentUser = mock(User.class);
        when(differentUser.getId()).thenReturn(2L);
        
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(differentUser);
        
        when(jobRepository.findById(1L)).thenReturn(Optional.of(testJob));
        
        JobUpdateRequest updateRequest = mock(JobUpdateRequest.class);

        assertThrows(OperationNotPermittedException.class,
                () -> jobService.updateJob(1L, updateRequest, auth));
    }

    @Test
    @DisplayName("closeJob should throw BusinessLogicException when job already closed")
    void shouldThrowExceptionWhenJobAlreadyClosedInCloseJob() {
        when(testJob.getStatus()).thenReturn(JobStatus.CLOSED);
        when(jobRepository.findById(1L)).thenReturn(Optional.of(testJob));

        BusinessLogicException exception = assertThrows(
                BusinessLogicException.class,
                () -> jobService.closeJob(1L, mockAuthentication));

        assertTrue(exception.getMessage().contains("already closed"));
    }

    @Test
    @DisplayName("closeJob should throw OperationNotPermittedException when unauthorized")
    void shouldThrowExceptionWhenUnauthorizedInCloseJob() {
        User differentUser = mock(User.class);
        when(differentUser.getId()).thenReturn(2L);
        
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(differentUser);
        
        when(jobRepository.findById(1L)).thenReturn(Optional.of(testJob));

        assertThrows(OperationNotPermittedException.class,
                () -> jobService.closeJob(1L, auth));
    }

    @Test
    @DisplayName("pauseJob should throw BusinessLogicException when job already paused")
    void shouldThrowExceptionWhenJobAlreadyPausedInPauseJob() {
        when(testJob.getStatus()).thenReturn(JobStatus.PAUSED);
        when(jobRepository.findById(1L)).thenReturn(Optional.of(testJob));

        BusinessLogicException exception = assertThrows(
                BusinessLogicException.class,
                () -> jobService.pauseJob(1L, mockAuthentication));

        assertTrue(exception.getMessage().contains("already paused"));
    }

    @Test
    @DisplayName("pauseJob should throw BusinessLogicException when job is closed")
    void shouldThrowExceptionWhenJobIsClosedInPauseJob() {
        when(testJob.getStatus()).thenReturn(JobStatus.CLOSED);
        when(jobRepository.findById(1L)).thenReturn(Optional.of(testJob));

        BusinessLogicException exception = assertThrows(
                BusinessLogicException.class,
                () -> jobService.pauseJob(1L, mockAuthentication));

        assertTrue(exception.getMessage().contains("closed"));
    }

    @Test
    @DisplayName("pauseJob should throw OperationNotPermittedException when unauthorized")
    void shouldThrowExceptionWhenUnauthorizedInPauseJob() {
        User differentUser = mock(User.class);
        when(differentUser.getId()).thenReturn(2L);
        
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(differentUser);
        
        when(jobRepository.findById(1L)).thenReturn(Optional.of(testJob));

        assertThrows(OperationNotPermittedException.class,
                () -> jobService.pauseJob(1L, auth));
    }

    @Test
    @DisplayName("resumeJob should throw BusinessLogicException when job is already open")
    void shouldThrowExceptionWhenJobAlreadyOpenInResumeJob() {
        when(testJob.getStatus()).thenReturn(JobStatus.OPEN);
        when(testJob.getPostedBy()).thenReturn(testUser);
        
        when(jobRepository.findById(1L)).thenReturn(Optional.of(testJob));

        BusinessLogicException exception = assertThrows(
                BusinessLogicException.class,
                () -> jobService.resumeJob(1L, mockAuthentication));

        assertTrue(exception.getMessage().contains("already OPEN"));
    }

    @Test
    @DisplayName("resumeJob should throw BusinessLogicException when job is closed")
    void shouldThrowExceptionWhenJobIsClosedInResumeJob() {
        when(testJob.getStatus()).thenReturn(JobStatus.CLOSED);
        when(testJob.getPostedBy()).thenReturn(testUser);
        
        when(jobRepository.findById(1L)).thenReturn(Optional.of(testJob));

        BusinessLogicException exception = assertThrows(
                BusinessLogicException.class,
                () -> jobService.resumeJob(1L, mockAuthentication));

        assertTrue(exception.getMessage().contains("closed"));
    }

    @Test
    @DisplayName("resumeJob should throw OperationNotPermittedException when unauthorized")
    void shouldThrowExceptionWhenUnauthorizedInResumeJob() {
        User differentUser = mock(User.class);
        when(differentUser.getId()).thenReturn(2L);
        
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(differentUser);
        
        when(jobRepository.findById(1L)).thenReturn(Optional.of(testJob));

        assertThrows(OperationNotPermittedException.class,
                () -> jobService.resumeJob(1L, auth));
    }
}