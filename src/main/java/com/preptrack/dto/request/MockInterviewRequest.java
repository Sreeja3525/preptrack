package com.preptrack.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record MockInterviewRequest(
        Long companyId,  // nullable — general mock not tied to a company

        @NotNull(message = "Interview time is required")
        LocalDateTime interviewedAt,

        @NotNull(message = "Overall score is required")
        @Min(value = 1, message = "Score must be between 1 and 10")
        @Max(value = 10, message = "Score must be between 1 and 10")
        Integer overallScore,

        String interviewer,  // "Priya from Swiggy", "Pramp", "Peer mock"

        List<Long> topicIds,

        String weakAreas,

        String notes
) {}
