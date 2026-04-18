package com.preptrack.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyReportResponse {
    private LocalDate weekStartDate;
    private LocalDate weekEndDate;
    private int totalStudySessions;
    private int totalStudyMinutes;
    private int uniqueTopicsStudied;
    private double averageConfidenceScore;
    private Map<String, Long> topicStudyBreakdown;       // topic name → session count
    private Map<String, Long> applicationStatusBreakdown; // status → app count
    private int totalApplicationsMoved;
    private String readinessChange;
}
