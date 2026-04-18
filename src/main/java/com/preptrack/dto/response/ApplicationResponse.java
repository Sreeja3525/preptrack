package com.preptrack.dto.response;

import com.preptrack.domain.Application;
import com.preptrack.domain.ApplicationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ApplicationResponse(
        Long id,
        Long companyId,
        String companyName,
        String role,
        ApplicationStatus status,
        LocalDate appliedDate,
        LocalDate nextActionDate,
        LocalDateTime lastUpdated,
        String notes
) {
    public static ApplicationResponse from(Application app) {
        return new ApplicationResponse(
                app.getId(),
                app.getCompany().getId(),
                app.getCompany().getName(),
                app.getRole(),
                app.getStatus(),
                app.getAppliedDate(),
                app.getNextActionDate(),
                app.getLastUpdated(),
                app.getNotes()
        );
    }
}
