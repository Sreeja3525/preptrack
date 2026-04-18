package com.preptrack.repository;

import com.preptrack.domain.StudyLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudyLogRepository extends JpaRepository<StudyLog, Long> {

    List<StudyLog> findByUserIdOrderByStudiedAtDesc(Long userId);

    List<StudyLog> findByUserIdAndTopicIdOrderByStudiedAtDesc(Long userId, Long topicId);

    // Used by ReadinessScoreService and SpacedRepetitionService to get the latest confidence score
    Optional<StudyLog> findTopByUserIdAndTopicIdOrderByStudiedAtDesc(Long userId, Long topicId);

    // Used by ReportService for weekly report
    List<StudyLog> findByUserIdAndStudiedAtAfter(Long userId, LocalDateTime since);

    @Query("SELECT COALESCE(SUM(sl.durationMinutes), 0) FROM StudyLog sl WHERE sl.user.id = :userId")
    Integer getTotalStudyMinutesByUserId(Long userId);
}
