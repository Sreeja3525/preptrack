package com.preptrack.dto.response;

import com.preptrack.domain.Topic;
import com.preptrack.domain.TopicCategory;

import java.time.LocalDateTime;

public record TopicResponse(
        Long id,
        String name,
        TopicCategory category,
        String description,
        LocalDateTime createdAt
) {
    // Static factory method — converts domain entity to response DTO
    // Keeps mapping logic close to the DTO, not scattered in services
    public static TopicResponse from(Topic topic) {
        return new TopicResponse(
                topic.getId(),
                topic.getName(),
                topic.getCategory(),
                topic.getDescription(),
                topic.getCreatedAt()
        );
    }
}
