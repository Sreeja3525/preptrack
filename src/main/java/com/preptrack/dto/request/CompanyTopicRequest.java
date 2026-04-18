package com.preptrack.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CompanyTopicRequest(
        @NotNull(message = "Topic ID is required")
        Long topicId,

        // How critical is this topic for this company's interview?
        // 1 = Nice to know, 5 = Must know (used as weight in readiness score)
        @NotNull(message = "Importance level is required")
        @Min(value = 1, message = "Importance level must be between 1 and 5")
        @Max(value = 5, message = "Importance level must be between 1 and 5")
        Integer importanceLevel
) {}
