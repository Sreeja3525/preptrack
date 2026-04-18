package com.preptrack.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ApplicationRequest(
        @NotNull(message = "Company ID is required")
        Long companyId,

        @NotBlank(message = "Role is required")
        String role,

        @NotNull(message = "Applied date is required")
        LocalDate appliedDate,

        LocalDate nextActionDate,

        String notes
) {}
