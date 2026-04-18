package com.preptrack.service;

import com.preptrack.domain.*;
import com.preptrack.dto.response.ReadinessScoreResponse;
import com.preptrack.exception.ResourceNotFoundException;
import com.preptrack.repository.CompanyRepository;
import com.preptrack.repository.CompanyTopicRepository;
import com.preptrack.repository.StudyLogRepository;
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
@DisplayName("ReadinessScoreService Tests")
class ReadinessScoreServiceTest {

    @Mock private CompanyTopicRepository companyTopicRepository;
    @Mock private CompanyRepository companyRepository;
    @Mock private StudyLogRepository studyLogRepository;

    @InjectMocks
    private ReadinessScoreService readinessScoreService;

    private User testUser;
    private Company testCompany;
    private Topic streamTopic;
    private Topic optionalTopic;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).name("Test User").email("test@test.com").build();
        testCompany = Company.builder().id(1L).name("Swiggy").user(testUser).build();
        streamTopic = Topic.builder().id(1L).name("Stream API").category(TopicCategory.JAVA_8).build();
        optionalTopic = Topic.builder().id(2L).name("Optional").category(TopicCategory.JAVA_8).build();
    }

    @Test
    @DisplayName("Should return 100% readiness when all topics have confidence 5")
    void shouldReturnFullReadinessWhenAllTopicsPerfect() {
        CompanyTopic ct1 = CompanyTopic.builder().id(1L).company(testCompany).topic(streamTopic).importanceLevel(5).build();
        CompanyTopic ct2 = CompanyTopic.builder().id(2L).company(testCompany).topic(optionalTopic).importanceLevel(3).build();

        StudyLog log1 = StudyLog.builder().topic(streamTopic).confidenceScore(5).studiedAt(LocalDateTime.now()).build();
        StudyLog log2 = StudyLog.builder().topic(optionalTopic).confidenceScore(5).studiedAt(LocalDateTime.now()).build();

        when(companyRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testCompany));
        when(companyTopicRepository.findByCompanyId(1L)).thenReturn(List.of(ct1, ct2));
        when(studyLogRepository.findTopByUserIdAndTopicIdOrderByStudiedAtDesc(1L, 1L)).thenReturn(Optional.of(log1));
        when(studyLogRepository.findTopByUserIdAndTopicIdOrderByStudiedAtDesc(1L, 2L)).thenReturn(Optional.of(log2));

        ReadinessScoreResponse result = readinessScoreService.calculateForCompany(1L, 1L);

        assertThat(result.getReadinessPercentage()).isEqualTo(100.0);
        assertThat(result.getReadinessLabel()).isEqualTo("READY");
        assertThat(result.getStrongTopics()).containsExactlyInAnyOrder("Stream API", "Optional");
        assertThat(result.getWeakTopics()).isEmpty();
        assertThat(result.getUnstudiedTopics()).isEmpty();
    }

    @Test
    @DisplayName("Should return 0% for unstudied topics and mark them as unstudied")
    void shouldReturnZeroForUnstudiedTopics() {
        CompanyTopic ct1 = CompanyTopic.builder().id(1L).company(testCompany).topic(streamTopic).importanceLevel(5).build();

        when(companyRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testCompany));
        when(companyTopicRepository.findByCompanyId(1L)).thenReturn(List.of(ct1));
        when(studyLogRepository.findTopByUserIdAndTopicIdOrderByStudiedAtDesc(1L, 1L)).thenReturn(Optional.empty());

        ReadinessScoreResponse result = readinessScoreService.calculateForCompany(1L, 1L);

        assertThat(result.getReadinessPercentage()).isEqualTo(0.0);
        assertThat(result.getReadinessLabel()).isEqualTo("NOT_READY");
        assertThat(result.getUnstudiedTopics()).contains("Stream API");
        assertThat(result.getTopicsStudied()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should calculate weighted score correctly — high importance topics count more")
    void shouldCalculateWeightedScoreCorrectly() {
        // Stream API (importance=5, confidence=4), Optional (importance=1, confidence=2)
        // Score = (4×5 + 2×1) / (5×5 + 5×1) × 100 = 22/30 × 100 = 73.3%
        CompanyTopic ct1 = CompanyTopic.builder().id(1L).company(testCompany).topic(streamTopic).importanceLevel(5).build();
        CompanyTopic ct2 = CompanyTopic.builder().id(2L).company(testCompany).topic(optionalTopic).importanceLevel(1).build();

        StudyLog log1 = StudyLog.builder().topic(streamTopic).confidenceScore(4).studiedAt(LocalDateTime.now()).build();
        StudyLog log2 = StudyLog.builder().topic(optionalTopic).confidenceScore(2).studiedAt(LocalDateTime.now()).build();

        when(companyRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testCompany));
        when(companyTopicRepository.findByCompanyId(1L)).thenReturn(List.of(ct1, ct2));
        when(studyLogRepository.findTopByUserIdAndTopicIdOrderByStudiedAtDesc(1L, 1L)).thenReturn(Optional.of(log1));
        when(studyLogRepository.findTopByUserIdAndTopicIdOrderByStudiedAtDesc(1L, 2L)).thenReturn(Optional.of(log2));

        ReadinessScoreResponse result = readinessScoreService.calculateForCompany(1L, 1L);

        assertThat(result.getReadinessPercentage()).isEqualTo(73.3);
        assertThat(result.getReadinessLabel()).isEqualTo("ALMOST_READY");
        assertThat(result.getStrongTopics()).contains("Stream API");
        assertThat(result.getWeakTopics()).contains("Optional");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when company does not belong to user")
    void shouldThrowWhenCompanyNotFound() {
        when(companyRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> readinessScoreService.calculateForCompany(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Company not found");
    }
}
