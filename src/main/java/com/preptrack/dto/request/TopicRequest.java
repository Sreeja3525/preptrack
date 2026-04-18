package com.preptrack.dto.request;

import com.preptrack.domain.TopicCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TopicRequest(
        @NotBlank(message = "Topic name is required")
        String name,

        @NotNull(message = "Category is required")
        TopicCategory category,

        String description
) {}
