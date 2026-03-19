package com.upply.job;

import com.upply.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JobController.class)
@AutoConfigureMockMvc(addFilters = false)
class JobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JobService jobService;

    // --- Security Layer Mocks ---
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private UserDetailsService userDetailsService;
    @MockitoBean
    private org.springframework.security.data.repository.query.SecurityEvaluationContextExtension securityExtension;

    // --- Persistence Layer Mocks ---
    @MockitoBean
    private org.springframework.data.jpa.mapping.JpaMetamodelMappingContext jpaMappingContext;

    // We use @TestConfiguration for AuditorAware because ProxyFactory needs a concrete bean during context start
    @TestConfiguration
    static class TestConfig {
        @Bean
        public org.springframework.data.domain.AuditorAware<Long> auditorAware() {
            return () -> Optional.of(1L);
        }
    }

    @Test
    @WithMockUser
    void getAllOpenJobs_ShouldReturn200_WhenParametersAreValid() throws Exception {
        mockMvc.perform(get("/jobs")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getAllOpenJobs_ShouldReturn400_WhenPageIsNegative() throws Exception {
        mockMvc.perform(get("/jobs")
                        .param("page", "-1"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(jobService);
    }

    @Test
    @WithMockUser
    void getJobApplications_ShouldReturn400_WhenSizeIsTooLarge() throws Exception {
        mockMvc.perform(get("/jobs/1/applications")
                        .param("size", "100"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void getApplicationByStatus_ShouldReturn400_WhenStatusIsInvalid() throws Exception {
        mockMvc.perform(get("/jobs/1/applications/INVALID_STATUS"))
                .andExpect(status().isBadRequest());
    }
}