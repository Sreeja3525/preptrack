package com.preptrack.service;

import com.preptrack.domain.Application;
import com.preptrack.domain.ApplicationStatus;
import com.preptrack.domain.Company;
import com.preptrack.domain.User;
import com.preptrack.dto.request.ApplicationRequest;
import com.preptrack.dto.request.ApplicationStatusUpdateRequest;
import com.preptrack.dto.response.ApplicationResponse;
import com.preptrack.exception.InvalidStateTransitionException;
import com.preptrack.exception.ResourceNotFoundException;
import com.preptrack.repository.ApplicationRepository;
import com.preptrack.repository.CompanyRepository;
import com.preptrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Application lifecycle management using a STATE MACHINE pattern.
 *
 * The state machine is represented as an immutable Map<Status, Set<Status>>
 * where the key is the CURRENT state and the value is the SET OF ALLOWED NEXT states.
 *
 * Valid transitions:
 *   APPLIED    → SCREENING, REJECTED, WITHDRAWN
 *   SCREENING  → TECHNICAL, REJECTED, WITHDRAWN
 *   TECHNICAL  → HR, REJECTED, WITHDRAWN
 *   HR         → OFFERED, REJECTED, WITHDRAWN
 *   OFFERED    → WITHDRAWN
 *   REJECTED   → (terminal — no transitions allowed)
 *   WITHDRAWN  → (terminal — no transitions allowed)
 *
 * Interview talking point: "Why a Map instead of switch-case?"
 * Answer: The Map is data — it can be loaded from DB or config later.
 *         Switch-case is code — every new transition requires a code change.
 *         Open/Closed Principle: open for extension, closed for modification.
 */
@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    // State machine transition table — immutable, defined once
    private static final Map<ApplicationStatus, Set<ApplicationStatus>> VALID_TRANSITIONS = Map.of(
            ApplicationStatus.APPLIED,   Set.of(ApplicationStatus.SCREENING, ApplicationStatus.REJECTED, ApplicationStatus.WITHDRAWN),
            ApplicationStatus.SCREENING, Set.of(ApplicationStatus.TECHNICAL, ApplicationStatus.REJECTED, ApplicationStatus.WITHDRAWN),
            ApplicationStatus.TECHNICAL, Set.of(ApplicationStatus.HR,        ApplicationStatus.REJECTED, ApplicationStatus.WITHDRAWN),
            ApplicationStatus.HR,        Set.of(ApplicationStatus.OFFERED,   ApplicationStatus.REJECTED, ApplicationStatus.WITHDRAWN),
            ApplicationStatus.OFFERED,   Set.of(ApplicationStatus.WITHDRAWN),
            ApplicationStatus.REJECTED,  Set.of(),
            ApplicationStatus.WITHDRAWN, Set.of()
    );

    @Transactional
    public ApplicationResponse create(ApplicationRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Company company = companyRepository.findByIdAndUserId(request.companyId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + request.companyId()));

        Application application = Application.builder()
                .user(user)
                .company(company)
                .role(request.role())
                .status(ApplicationStatus.APPLIED)  // always starts as APPLIED
                .appliedDate(request.appliedDate())
                .nextActionDate(request.nextActionDate())
                .notes(request.notes())
                .lastUpdated(LocalDateTime.now())
                .build();

        return ApplicationResponse.from(applicationRepository.save(application));
    }

    public List<ApplicationResponse> getAllByUser(Long userId) {
        return applicationRepository.findByUserIdOrderByLastUpdatedDesc(userId)
                .stream()
                .map(ApplicationResponse::from)
                .collect(Collectors.toList());
    }

    // Kanban board view: group applications by their current status
    // Uses Collectors.groupingBy — a key Java 8 interview topic
    public Map<String, List<ApplicationResponse>> getPipeline(Long userId) {
        return applicationRepository.findByUserIdOrderByLastUpdatedDesc(userId)
                .stream()
                .map(ApplicationResponse::from)
                .collect(Collectors.groupingBy(app -> app.status().name()));
    }

    @Transactional
    public ApplicationResponse updateStatus(Long applicationId,
                                            ApplicationStatusUpdateRequest request,
                                            Long userId) {
        Application application = applicationRepository.findByIdAndUserId(applicationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));

        ApplicationStatus current = application.getStatus();
        ApplicationStatus next = request.status();

        // Look up allowed transitions for current state
        Set<ApplicationStatus> allowed = VALID_TRANSITIONS.getOrDefault(current, Set.of());

        if (!allowed.contains(next)) {
            throw new InvalidStateTransitionException(
                    String.format("Cannot move from %s to %s. Allowed: %s", current, next, allowed)
            );
        }

        application.setStatus(next);
        application.setLastUpdated(LocalDateTime.now());
        if (request.notes() != null) {
            application.setNotes(request.notes());
        }

        return ApplicationResponse.from(applicationRepository.save(application));
    }
}
