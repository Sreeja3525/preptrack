package com.preptrack.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record StudyLogRequest(
        @NotNull(message = "Topic ID is required")
        Long topicId,

        // 1=Very Low, 2=Low, 3=Medium, 4=Good, 5=Excellent
        // This drives both spaced repetition intervals and readiness score weighting
        @NotNull(message = "Confidence score is required")
        @Min(value = 1, message = "Confidence score must be between 1 and 5")
        @Max(value = 5, message = "Confidence score must be between 1 and 5")
        Integer confidenceScore,

        @NotNull(message = "Study time is required")
        LocalDateTime studiedAt,

        @Min(value = 1, message = "Duration must be at least 1 minute")
        Integer durationMinutes,

        String notes
) {}
