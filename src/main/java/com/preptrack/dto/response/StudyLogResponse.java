package com.preptrack.dto.response;

import com.preptrack.domain.StudyLog;

import java.time.LocalDateTime;

public record StudyLogResponse(
        Long id,
        Long topicId,
        String topicName,
        String topicCategory,
        Integer confidenceScore,
        String confidenceLabel,
        LocalDateTime studiedAt,
        Integer durationMinutes,
        String notes
) {
    public static StudyLogResponse from(StudyLog log) {
        return new StudyLogResponse(
                log.getId(),
                log.getTopic().getId(),
                log.getTopic().getName(),
                log.getTopic().getCategory().name(),
                log.getConfidenceScore(),
                getConfidenceLabel(log.getConfidenceScore()),
                log.getStudiedAt(),
                log.getDurationMinutes(),
                log.getNotes()
        );
    }

    private static String getConfidenceLabel(int score) {
        return switch (score) {
            case 1 -> "Very Low";
            case 2 -> "Low";
            case 3 -> "Medium";
            case 4 -> "Good";
            case 5 -> "Excellent";
            default -> "Unknown";
        };
    }
}
