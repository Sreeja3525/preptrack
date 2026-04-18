package com.preptrack.service;

import com.preptrack.domain.*;
import com.preptrack.dto.response.RevisionScheduleResponse;
import com.preptrack.repository.StudyLogRepository;
import com.preptrack.repository.TopicRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SpacedRepetitionService Tests")
class SpacedRepetitionServiceTest {

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private StudyLogRepository studyLogRepository;

    @InjectMocks
    private SpacedRepetitionService spacedRepetitionService;

    private User testUser;
    private Topic streamTopic;
    private Topic optionalTopic;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).name("Test User").build();
        streamTopic = Topic.builder().id(1L).name("Stream API").category(TopicCategory.JAVA_8).user(testUser).build();
        optionalTopic = Topic.builder().id(2L).name("Optional").category(TopicCategory.JAVA_8).user(testUser).build();
    }

    @Test
    @DisplayName("Never-studied topic should have status NEVER_STUDIED and highest priority")
    void neverStudiedTopicShouldBeHighestPriority() {
        when(topicRepository.findByUserId(1L)).thenReturn(List.of(streamTopic));
        when(studyLogRepository.findTopByUserIdAndTopicIdOrderByStudiedAtDesc(1L, 1L))
                .thenReturn(Optional.empty());

        List<RevisionScheduleResponse> schedule = spacedRepetitionService.getTodaysRevisionList(1L);

        assertThat(schedule).hasSize(1);
        assertThat(schedule.get(0).getStatus()).isEqualTo("NEVER_STUDIED");
        assertThat(schedule.get(0).getPriority()).isEqualTo(1);
        assertThat(schedule.get(0).getConfidenceScore()).isEqualTo(0);
    }

    @Test
    @DisplayName("Topic with confidence 5 studied today should not appear in today's list")
    void excellentTopicStudiedTodayShouldNotBeDue() {
        StudyLog recentLog = StudyLog.builder()
                .topic(streamTopic)
                .confidenceScore(5)
                .studiedAt(LocalDateTime.now())  // studied TODAY
                .build();

        when(topicRepository.findByUserId(1L)).thenReturn(List.of(streamTopic));
        when(studyLogRepository.findTopByUserIdAndTopicIdOrderByStudiedAtDesc(1L, 1L))
                .thenReturn(Optional.of(recentLog));

        List<RevisionScheduleResponse> todaysList = spacedRepetitionService.getTodaysRevisionList(1L);

        // Confidence 5 → interval is 30 days → NOT due today
        assertThat(todaysList).isEmpty();
    }

    @Test
    @DisplayName("Topic with confidence 1 studied yesterday should appear in today's list")
    void lowConfidenceTopicStudiedYesterdayShouldBeDueToday() {
        StudyLog oldLog = StudyLog.builder()
                .topic(streamTopic)
                .confidenceScore(1)
                .studiedAt(LocalDateTime.now().minusDays(1))  // studied YESTERDAY
                .build();

        when(topicRepository.findByUserId(1L)).thenReturn(List.of(streamTopic));
        when(studyLogRepository.findTopByUserIdAndTopicIdOrderByStudiedAtDesc(1L, 1L))
                .thenReturn(Optional.of(oldLog));

        List<RevisionScheduleResponse> todaysList = spacedRepetitionService.getTodaysRevisionList(1L);

        // Confidence 1 → interval 1 day → studied 1 day ago → DUE TODAY
        assertThat(todaysList).hasSize(1);
        assertThat(todaysList.get(0).getStatus()).isEqualTo("DUE");
        assertThat(todaysList.get(0).getTopicName()).isEqualTo("Stream API");
    }

    @Test
    @DisplayName("Full schedule should return all topics sorted by next revision date")
    void fullScheduleShouldReturnAllTopicsSorted() {
        StudyLog log1 = StudyLog.builder().topic(streamTopic).confidenceScore(5)
                .studiedAt(LocalDateTime.now().minusDays(5)).build();
        StudyLog log2 = StudyLog.builder().topic(optionalTopic).confidenceScore(1)
                .studiedAt(LocalDateTime.now().minusDays(2)).build();

        when(topicRepository.findByUserId(1L)).thenReturn(List.of(streamTopic, optionalTopic));
        when(studyLogRepository.findTopByUserIdAndTopicIdOrderByStudiedAtDesc(1L, 1L))
                .thenReturn(Optional.of(log1));
        when(studyLogRepository.findTopByUserIdAndTopicIdOrderByStudiedAtDesc(1L, 2L))
                .thenReturn(Optional.of(log2));

        List<RevisionScheduleResponse> schedule = spacedRepetitionService.getFullSchedule(1L);

        // Optional (confidence 1, studied 2 days ago, interval 1 day) → overdue
        // Stream API (confidence 5, studied 5 days ago, interval 30 days) → upcoming
        assertThat(schedule).hasSize(2);
        assertThat(schedule.get(0).getTopicName()).isEqualTo("Optional"); // earliest next revision date
    }
}
