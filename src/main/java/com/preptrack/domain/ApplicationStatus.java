package com.preptrack.domain;

// Represents a job application's lifecycle
// The state machine in ApplicationService enforces valid transitions
public enum ApplicationStatus {
    APPLIED,    // Just submitted the application
    SCREENING,  // HR reached out / online assessment
    TECHNICAL,  // Technical round(s) ongoing
    HR,         // HR round / culture fit
    OFFERED,    // Got an offer!
    REJECTED,   // Rejected at any stage (terminal state)
    WITHDRAWN   // You withdrew the application (terminal state)
}
