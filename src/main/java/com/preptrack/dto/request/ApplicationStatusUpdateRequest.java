package com.preptrack.dto.request;

import com.preptrack.domain.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

public record ApplicationStatusUpdateRequest(
        @NotNull(message = "New status is required")
        ApplicationStatus status,

        String notes
) {}
