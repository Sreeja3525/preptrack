package com.preptrack.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "study_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StudyLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    // Self-reported confidence: 1=Very Low, 2=Low, 3=Medium, 4=Good, 5=Excellent
    // Drives spaced repetition intervals and readiness score calculations
    @Column(nullable = false)
    private Integer confidenceScore;

    @Column(nullable = false)
    private LocalDateTime studiedAt;

    private Integer durationMinutes;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
