package com.preptrack.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevisionScheduleResponse {
    private Long topicId;
    private String topicName;
    private String category;
    private LocalDate lastStudiedDate;    // null if never studied
    private Integer daysSinceLastStudy;   // null if never studied
    private Integer confidenceScore;      // 0 if never studied
    private LocalDate nextRevisionDate;
    private int priority;                 // lower = more urgent (1 is highest)
    private String status;                // DUE / UPCOMING / NEVER_STUDIED
}
