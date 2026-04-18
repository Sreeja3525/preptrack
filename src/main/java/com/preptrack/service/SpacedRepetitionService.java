package com.preptrack.service;

import com.preptrack.domain.StudyLog;
import com.preptrack.domain.Topic;
import com.preptrack.dto.response.RevisionScheduleResponse;
import com.preptrack.repository.StudyLogRepository;
import com.preptrack.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Spaced Repetition Engine — tells you WHAT to revise TODAY and in what order.
 *
 * The algorithm is based on the Ebbinghaus Forgetting Curve:
 * The lower your confidence, the sooner you should revise.
 *
 * Intervals by confidence score:
 *   1 (Very Low)  → revisit in 1 day
 *   2 (Low)       → revisit in 3 days
 *   3 (Medium)    → revisit in 7 days
 *   4 (Good)      → revisit in 14 days
 *   5 (Excellent) → revisit in 30 days
 *
 * Java 8 / java.time features used here:
 *  - Map.of()                              — immutable constant map
 *  - LocalDate, ChronoUnit.DAYS.between()  — date arithmetic
 *  - LocalDate.plusDays()                  — calculate next revision date
 *  - Stream.filter() with predicate        — filter due topics
 *  - Stream.sorted(Comparator)             — priority ordering
 *  - Optional                              — handle never-studied topics
 */
@Service
@RequiredArgsConstructor
public class SpacedRepetitionService {

    private final TopicRepository topicRepository;
    private final StudyLogRepository studyLogRepository;

    // Immutable map: confidence score → days before next revision
    private static final Map<Integer, Integer> REVISION_INTERVALS = Map.of(
            1, 1,
            2, 3,
            3, 7,
            4, 14,
            5, 30
    );

    // Returns only the topics that are DUE today or overdue — your actual study list
    public List<RevisionScheduleResponse> getTodaysRevisionList(Long userId) {
        LocalDate today = LocalDate.now();
        return topicRepository.findByUserId(userId).stream()
                .map(topic -> buildRevisionItem(topic, userId, today))
                .filter(item -> !item.getNextRevisionDate().isAfter(today))  // due today or overdue
                .sorted(Comparator.comparingInt(RevisionScheduleResponse::getPriority))
                .collect(Collectors.toList());
    }

    // Returns the full schedule for all topics — shows upcoming revisions too
    public List<RevisionScheduleResponse> getFullSchedule(Long userId) {
        LocalDate today = LocalDate.now();
        return topicRepository.findByUserId(userId).stream()
                .map(topic -> buildRevisionItem(topic, userId, today))
                .sorted(Comparator.comparing(RevisionScheduleResponse::getNextRevisionDate))
                .collect(Collectors.toList());
    }

    private RevisionScheduleResponse buildRevisionItem(Topic topic, Long userId, LocalDate today) {
        Optional<StudyLog> latestLog =
                studyLogRepository.findTopByUserIdAndTopicIdOrderByStudiedAtDesc(userId, topic.getId());

        // Never studied → highest priority, due now
        if (latestLog.isEmpty()) {
            return RevisionScheduleResponse.builder()
                    .topicId(topic.getId())
                    .topicName(topic.getName())
                    .category(topic.getCategory().name())
                    .lastStudiedDate(null)
                    .daysSinceLastStudy(null)
                    .confidenceScore(0)
                    .nextRevisionDate(today)
                    .priority(1)  // 1 = highest priority
                    .status("NEVER_STUDIED")
                    .build();
        }

        StudyLog log = latestLog.get();
        LocalDate lastStudied = log.getStudiedAt().toLocalDate();

        // ChronoUnit.DAYS.between() = exact day count between two LocalDates
        long daysSince = ChronoUnit.DAYS.between(lastStudied, today);

        int intervalDays = REVISION_INTERVALS.getOrDefault(log.getConfidenceScore(), 7);
        LocalDate nextRevision = lastStudied.plusDays(intervalDays);

        // Priority: lower confidence + more overdue = lower number = higher urgency
        int priority = calculatePriority(log.getConfidenceScore(), daysSince, intervalDays);

        return RevisionScheduleResponse.builder()
                .topicId(topic.getId())
                .topicName(topic.getName())
                .category(topic.getCategory().name())
                .lastStudiedDate(lastStudied)
                .daysSinceLastStudy((int) daysSince)
                .confidenceScore(log.getConfidenceScore())
                .nextRevisionDate(nextRevision)
                .priority(priority)
                .status(nextRevision.isAfter(today) ? "UPCOMING" : "DUE")
                .build();
    }

    // Priority formula: (6 - confidence) × overdue_ratio × 10
    // - Low confidence (1) → (6-1)=5, high weight
    // - High confidence (5) → (6-5)=1, low weight
    // - Overdue ratio: daysSince / intervalDays (>1.0 means overdue)
    private int calculatePriority(int confidenceScore, long daysSince, int intervalDays) {
        double overdueRatio = intervalDays > 0 ? (double) daysSince / intervalDays : 1.0;
        return (int) Math.max(1, (6 - confidenceScore) * overdueRatio * 10);
    }
}
