package com.preptrack.dto.response;

import com.preptrack.domain.MockInterview;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record MockInterviewResponse(
        Long id,
        Long companyId,
        String companyName,
        LocalDateTime interviewedAt,
        Integer overallScore,
        String scoreLabel,
        String interviewer,
        List<String> topicsAsked,
        String weakAreas,
        String notes
) {
    public static MockInterviewResponse from(MockInterview interview) {
        return new MockInterviewResponse(
                interview.getId(),
                interview.getCompany() != null ? interview.getCompany().getId() : null,
                interview.getCompany() != null ? interview.getCompany().getName() : "General Mock",
                interview.getInterviewedAt(),
                interview.getOverallScore(),
                getScoreLabel(interview.getOverallScore()),
                interview.getInterviewer(),
                interview.getTopicsAsked().stream()
                        .map(t -> t.getName())
                        .collect(Collectors.toList()),
                interview.getWeakAreas(),
                interview.getNotes()
        );
    }

    private static String getScoreLabel(int score) {
        if (score >= 9) return "Outstanding";
        if (score >= 7) return "Good";
        if (score >= 5) return "Average";
        if (score >= 3) return "Below Average";
        return "Poor";
    }
}
