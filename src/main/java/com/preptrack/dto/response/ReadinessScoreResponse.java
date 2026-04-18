package com.preptrack.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadinessScoreResponse {
    private Long companyId;
    private String companyName;
    private double readinessPercentage;   // 0.0 to 100.0
    private String readinessLabel;        // READY / ALMOST_READY / IN_PROGRESS / NOT_READY
    private int totalTopicsRequired;
    private int topicsStudied;
    private List<String> strongTopics;    // confidence >= 4
    private List<String> weakTopics;      // confidence <= 2, sorted by importance desc
    private List<String> unstudiedTopics; // never logged a study session
}
