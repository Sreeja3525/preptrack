package com.preptrack.service;

import com.preptrack.domain.CompanyTopic;
import com.preptrack.domain.StudyLog;
import com.preptrack.domain.Topic;
import com.preptrack.dto.response.ReadinessScoreResponse;
import com.preptrack.exception.ResourceNotFoundException;
import com.preptrack.repository.CompanyRepository;
import com.preptrack.repository.CompanyTopicRepository;
import com.preptrack.repository.StudyLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * THE CORE ENGINE — This is what makes PrepTrack unique.
 *
 * Formula:  readiness % = Σ(confidence × importance) / Σ(5 × importance) × 100
 *
 * Why weighted? A topic with importance=5 (must-know) has more impact on your
 * score than a topic with importance=1 (nice-to-know). This mirrors real interviews.
 *
 * Java 8 features used here:
 *  - Stream.collect(Collectors.toMap(...))   — build Topic→StudyLog map
 *  - Stream.mapToDouble() + .sum()           — weighted score calculation
 *  - Optional.map().orElse()                 — null-safe confidence extraction
 *  - Stream.filter() with Predicate          — strong/weak/unstudied categorization
 *  - Collectors.groupingBy() variant         — weak topics sorted by importance
 *  - Map.Entry comparator                    — sort by importance descending
 */
@Service
@RequiredArgsConstructor
public class ReadinessScoreService {

    private final CompanyTopicRepository companyTopicRepository;
    private final CompanyRepository companyRepository;
    private final StudyLogRepository studyLogRepository;

    public ReadinessScoreResponse calculateForCompany(Long companyId, Long userId) {
        var company = companyRepository.findByIdAndUserId(companyId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + companyId));

        List<CompanyTopic> companyTopics = companyTopicRepository.findByCompanyId(companyId);

        if (companyTopics.isEmpty()) {
            return ReadinessScoreResponse.builder()
                    .companyId(companyId)
                    .companyName(company.getName())
                    .readinessPercentage(0.0)
                    .readinessLabel("NO_TOPICS_DEFINED")
                    .totalTopicsRequired(0)
                    .topicsStudied(0)
                    .strongTopics(List.of())
                    .weakTopics(List.of())
                    .unstudiedTopics(List.of())
                    .build();
        }

        // Step 1: For each required topic, find the latest study log
        // Collectors.toMap builds a Map<Topic, Optional<StudyLog>>
        Map<Topic, Optional<StudyLog>> topicToLatestLog = companyTopics.stream()
                .map(CompanyTopic::getTopic)
                .collect(Collectors.toMap(
                        topic -> topic,
                        topic -> studyLogRepository.findTopByUserIdAndTopicIdOrderByStudiedAtDesc(userId, topic.getId())
                ));

        // Step 2: Calculate weighted denominator — what a perfect score would be
        double totalPossible = companyTopics.stream()
                .mapToDouble(ct -> 5.0 * ct.getImportanceLevel())
                .sum();

        // Step 3: Calculate actual weighted score
        // Optional.map(...).orElse(0) handles topics never studied — confidence defaults to 0
        double actualScore = companyTopics.stream()
                .mapToDouble(ct -> {
                    Optional<StudyLog> log = topicToLatestLog.get(ct.getTopic());
                    int confidence = log.map(StudyLog::getConfidenceScore).orElse(0);
                    return (double) confidence * ct.getImportanceLevel();
                })
                .sum();

        double percentage = Math.round((actualScore / totalPossible) * 1000.0) / 10.0;

        // Step 4: Categorize topics
        // Strong: latest confidence >= 4
        List<String> strongTopics = companyTopics.stream()
                .filter(ct -> topicToLatestLog.get(ct.getTopic())
                        .map(log -> log.getConfidenceScore() >= 4)
                        .orElse(false))
                .map(ct -> ct.getTopic().getName())
                .collect(Collectors.toList());

        // Weak: latest confidence <= 2, sorted by importance DESC (most critical first)
        List<String> weakTopics = companyTopics.stream()
                .filter(ct -> topicToLatestLog.get(ct.getTopic())
                        .map(log -> log.getConfidenceScore() <= 2)
                        .orElse(false))
                .sorted(Comparator.comparingInt(CompanyTopic::getImportanceLevel).reversed())
                .map(ct -> ct.getTopic().getName())
                .collect(Collectors.toList());

        // Unstudied: Optional is empty — no study log at all
        List<String> unstudiedTopics = companyTopics.stream()
                .filter(ct -> topicToLatestLog.get(ct.getTopic()).isEmpty())
                .map(ct -> ct.getTopic().getName())
                .collect(Collectors.toList());

        long topicsStudied = topicToLatestLog.values().stream()
                .filter(Optional::isPresent)
                .count();

        return ReadinessScoreResponse.builder()
                .companyId(companyId)
                .companyName(company.getName())
                .readinessPercentage(percentage)
                .readinessLabel(getReadinessLabel(percentage))
                .totalTopicsRequired(companyTopics.size())
                .topicsStudied((int) topicsStudied)
                .strongTopics(strongTopics)
                .weakTopics(weakTopics)
                .unstudiedTopics(unstudiedTopics)
                .build();
    }

    // Calculate readiness for all companies, sorted best → worst
    public List<ReadinessScoreResponse> calculateForAllCompanies(Long userId) {
        return companyTopicRepository.findDistinctCompanyIdsByUserId(userId)
                .stream()
                .map(companyId -> calculateForCompany(companyId, userId))
                .sorted(Comparator.comparingDouble(ReadinessScoreResponse::getReadinessPercentage).reversed())
                .collect(Collectors.toList());
    }

    private String getReadinessLabel(double score) {
        if (score >= 80) return "READY";
        if (score >= 60) return "ALMOST_READY";
        if (score >= 40) return "IN_PROGRESS";
        return "NOT_READY";
    }
}
