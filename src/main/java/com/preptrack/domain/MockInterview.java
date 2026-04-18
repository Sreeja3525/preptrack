package com.preptrack.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mock_interviews")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MockInterview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Nullable — could be a general mock (e.g., Pramp) not tied to a specific company
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "company_id")
    private Company company;

    @Column(nullable = false)
    private LocalDateTime interviewedAt;

    // Self-rated overall performance: 1-10
    @Column(nullable = false)
    private Integer overallScore;

    // Who conducted it: "Priya from Swiggy", "Pramp", "Peer mock"
    private String interviewer;

    // Topics that were actually asked about in this interview
    @ManyToMany
    @JoinTable(
        name = "mock_interview_topics",
        joinColumns = @JoinColumn(name = "mock_interview_id"),
        inverseJoinColumns = @JoinColumn(name = "topic_id")
    )
    @Builder.Default
    private List<Topic> topicsAsked = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String weakAreas;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
