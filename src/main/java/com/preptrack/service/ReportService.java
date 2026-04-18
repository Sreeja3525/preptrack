package com.preptrack.service;

import com.preptrack.domain.Application;
import com.preptrack.domain.StudyLog;
import com.preptrack.dto.response.WeeklyReportResponse;
import com.preptrack.repository.ApplicationRepository;
import com.preptrack.repository.StudyLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Async weekly report using CompletableFuture.
 *
 * Why @Async + CompletableFuture?
 * Generating a report means querying multiple tables. If we do them sequentially,
 * we wait for each DB call to finish before starting the next. With CompletableFuture,
 * we kick off all queries in parallel on separate threads, then combine when all are done.
 *
 * CompletableFuture.allOf() — waits for ALL supplied futures to complete
 * .thenApply()              — runs after allOf(), has access to all results
 * .join()                   — blocks to get the result of a completed future
 *
 * Java 8 features in this service:
 *  - CompletableFuture.supplyAsync()    — async task submission
 *  - CompletableFuture.allOf()          — fan-in: wait for all parallel tasks
 *  - Collectors.groupingBy + counting() — breakdown maps
 *  - OptionalDouble from mapToInt.average() — null-safe average
 *  - Stream.mapToInt().sum()            — total study minutes
 */
@Service
@RequiredArgsConstructor
public class ReportService {

    private final StudyLogRepository studyLogRepository;
    private final ApplicationRepository applicationRepository;

    @Async("taskExecutor")  // runs on the thread pool configured in AsyncConfig
    public CompletableFuture<WeeklyReportResponse> generateWeeklyReport(Long userId) {
        LocalDate weekStart = LocalDate.now().minusDays(7);
        LocalDateTime since = weekStart.atStartOfDay();

        // Kick off both DB queries in parallel on separate threads
        CompletableFuture<List<StudyLog>> studyFuture = CompletableFuture.supplyAsync(
                () -> studyLogRepository.findByUserIdAndStudiedAtAfter(userId, since)
        );

        CompletableFuture<List<Application>> appFuture = CompletableFuture.supplyAsync(
                () -> applicationRepository.findByUserIdAndLastUpdatedAfter(userId, since)
        );

        // Wait for both, then build the report
        return CompletableFuture.allOf(studyFuture, appFuture)
                .thenApply(v -> {
                    List<StudyLog> studyLogs = studyFuture.join();
                    List<Application> applications = appFuture.join();

                    // topic name → how many times studied this week
                    Map<String, Long> topicBreakdown = studyLogs.stream()
                            .collect(Collectors.groupingBy(
                                    log -> log.getTopic().getName(),
                                    Collectors.counting()
                            ));

                    // OptionalDouble handles the case where there are no study logs
                    OptionalDouble avgConfidence = studyLogs.stream()
                            .mapToInt(StudyLog::getConfidenceScore)
                            .average();

                    // application status → count of apps in that status
                    Map<String, Long> statusBreakdown = applications.stream()
                            .collect(Collectors.groupingBy(
                                    app -> app.getStatus().name(),
                                    Collectors.counting()
                            ));

                    int totalMinutes = studyLogs.stream()
                            .mapToInt(log -> log.getDurationMinutes() != null ? log.getDurationMinutes() : 0)
                            .sum();

                    return WeeklyReportResponse.builder()
                            .weekStartDate(weekStart)
                            .weekEndDate(LocalDate.now())
                            .totalStudySessions(studyLogs.size())
                            .totalStudyMinutes(totalMinutes)
                            .uniqueTopicsStudied(topicBreakdown.size())
                            .averageConfidenceScore(
                                    Math.round(avgConfidence.orElse(0.0) * 10.0) / 10.0
                            )
                            .topicStudyBreakdown(topicBreakdown)
                            .applicationStatusBreakdown(statusBreakdown)
                            .totalApplicationsMoved(applications.size())
                            .readinessChange("See GET /api/readiness for current company scores")
                            .build();
                });
    }
}
