package com.preptrack.domain;

import jakarta.persistence.*;
import lombok.*;

// This is a join entity between Company and Topic with an extra column: importanceLevel
// We CANNOT use a simple @ManyToMany when we need extra columns on the join table
// So we model it as a separate entity with two @ManyToOne relationships
@Entity
@Table(name = "company_topics")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CompanyTopic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    // How important is this topic for this company's interview? 1=Nice to know, 5=Must know
    // Used as a weight in the readiness score formula:
    //   score = Σ(confidence × importance) / Σ(5 × importance) × 100
    @Column(nullable = false)
    private Integer importanceLevel;
}
