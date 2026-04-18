package com.preptrack.controller;

import com.preptrack.dto.request.StudyLogRequest;
import com.preptrack.dto.response.ApiResponse;
import com.preptrack.dto.response.StudyLogResponse;
import com.preptrack.service.StudyLogService;
import com.preptrack.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/study-logs")
@RequiredArgsConstructor
@Tag(name = "Study Logs", description = "Log study sessions with confidence scores")
public class StudyLogController {

    private final StudyLogService studyLogService;

    @PostMapping
    @Operation(summary = "Log a study session for a topic")
    public ResponseEntity<ApiResponse<StudyLogResponse>> log(@Valid @RequestBody StudyLogRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(studyLogService.log(request, SecurityUtil.getCurrentUserId())));
    }

    @GetMapping
    @Operation(summary = "Get all your study logs")
    public ResponseEntity<ApiResponse<List<StudyLogResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(
                studyLogService.getAllByUser(SecurityUtil.getCurrentUserId())));
    }

    @GetMapping("/topic/{topicId}")
    @Operation(summary = "Get study logs for a specific topic")
    public ResponseEntity<ApiResponse<List<StudyLogResponse>>> getByTopic(@PathVariable Long topicId) {
        return ResponseEntity.ok(ApiResponse.success(
                studyLogService.getByTopic(topicId, SecurityUtil.getCurrentUserId())));
    }

    @GetMapping("/total-minutes")
    @Operation(summary = "Get total study time in minutes")
    public ResponseEntity<ApiResponse<Integer>> getTotalMinutes() {
        return ResponseEntity.ok(ApiResponse.success(
                studyLogService.getTotalStudyMinutes(SecurityUtil.getCurrentUserId())));
    }
}
