package com.preptrack.dto.request;

import com.preptrack.domain.CompanyType;
import com.preptrack.domain.InterviewDifficulty;
import jakarta.validation.constraints.NotBlank;

public record CompanyRequest(
        @NotBlank(message = "Company name is required")
        String name,

        CompanyType type,

        InterviewDifficulty difficulty,

        String jobPortalUrl
) {}
