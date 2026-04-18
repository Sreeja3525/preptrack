package com.preptrack.service;

import com.preptrack.domain.*;
import com.preptrack.dto.request.ApplicationStatusUpdateRequest;
import com.preptrack.dto.response.ApplicationResponse;
import com.preptrack.exception.InvalidStateTransitionException;
import com.preptrack.exception.ResourceNotFoundException;
import com.preptrack.repository.ApplicationRepository;
import com.preptrack.repository.CompanyRepository;
import com.preptrack.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApplicationService — State Machine Tests")
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ApplicationService applicationService;

    private User testUser;
    private Company testCompany;
    private Application testApplication;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).name("Test User").email("test@test.com").build();
        testCompany = Company.builder().id(1L).name("Swiggy").user(testUser).build();
        testApplication = Application.builder()
                .id(1L).user(testUser).company(testCompany)
                .role("Java Backend Dev").status(ApplicationStatus.APPLIED)
                .appliedDate(LocalDate.now()).lastUpdated(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("APPLIED → SCREENING should succeed")
    void appliedToScreeningShouldSucceed() {
        when(applicationRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testApplication));
        when(applicationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ApplicationStatusUpdateRequest request = new ApplicationStatusUpdateRequest(ApplicationStatus.SCREENING, null);
        ApplicationResponse result = applicationService.updateStatus(1L, request, 1L);

        assertThat(result.status()).isEqualTo(ApplicationStatus.SCREENING);
    }

    @Test
    @DisplayName("APPLIED → TECHNICAL should fail — not a valid transition")
    void appliedToTechnicalShouldThrow() {
        when(applicationRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testApplication));

        ApplicationStatusUpdateRequest request = new ApplicationStatusUpdateRequest(ApplicationStatus.TECHNICAL, null);

        assertThatThrownBy(() -> applicationService.updateStatus(1L, request, 1L))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("Cannot move from APPLIED to TECHNICAL");
    }

    @Test
    @DisplayName("Any state → REJECTED should succeed")
    void anyStateToRejectedShouldSucceed() {
        when(applicationRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testApplication));
        when(applicationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ApplicationStatusUpdateRequest request = new ApplicationStatusUpdateRequest(ApplicationStatus.REJECTED, "Not shortlisted");
        ApplicationResponse result = applicationService.updateStatus(1L, request, 1L);

        assertThat(result.status()).isEqualTo(ApplicationStatus.REJECTED);
    }

    @Test
    @DisplayName("REJECTED → anything should throw — terminal state")
    void rejectedIsTerminalState() {
        testApplication.setStatus(ApplicationStatus.REJECTED);
        when(applicationRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testApplication));

        ApplicationStatusUpdateRequest request = new ApplicationStatusUpdateRequest(ApplicationStatus.SCREENING, null);

        assertThatThrownBy(() -> applicationService.updateStatus(1L, request, 1L))
                .isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    @DisplayName("getPipeline should group applications by status")
    void pipelineShouldGroupByStatus() {
        Application app1 = Application.builder().id(1L).user(testUser).company(testCompany)
                .role("Backend Dev").status(ApplicationStatus.APPLIED)
                .appliedDate(LocalDate.now()).lastUpdated(LocalDateTime.now()).build();
        Application app2 = Application.builder().id(2L).user(testUser).company(testCompany)
                .role("Senior Dev").status(ApplicationStatus.TECHNICAL)
                .appliedDate(LocalDate.now()).lastUpdated(LocalDateTime.now()).build();

        when(applicationRepository.findByUserIdOrderByLastUpdatedDesc(1L))
                .thenReturn(java.util.List.of(app1, app2));

        Map<String, java.util.List<ApplicationResponse>> pipeline = applicationService.getPipeline(1L);

        assertThat(pipeline).containsKey("APPLIED");
        assertThat(pipeline).containsKey("TECHNICAL");
        assertThat(pipeline.get("APPLIED")).hasSize(1);
        assertThat(pipeline.get("TECHNICAL")).hasSize(1);
    }

    @Test
    @DisplayName("updateStatus should throw when application not found")
    void shouldThrowWhenApplicationNotFound() {
        when(applicationRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

        ApplicationStatusUpdateRequest request = new ApplicationStatusUpdateRequest(ApplicationStatus.SCREENING, null);

        assertThatThrownBy(() -> applicationService.updateStatus(999L, request, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
