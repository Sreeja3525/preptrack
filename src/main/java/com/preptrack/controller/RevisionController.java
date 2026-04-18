package com.preptrack.controller;

import com.preptrack.dto.response.ApiResponse;
import com.preptrack.dto.response.RevisionScheduleResponse;
import com.preptrack.service.SpacedRepetitionService;
import com.preptrack.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/revision")
@RequiredArgsConstructor
@Tag(name = "Revision Schedule", description = "Spaced repetition — what to study today")
public class RevisionController {

    private final SpacedRepetitionService spacedRepetitionService;

    @GetMapping("/today")
    @Operation(summary = "Get topics due for revision today (sorted by priority)")
    public ResponseEntity<ApiResponse<List<RevisionScheduleResponse>>> getTodaysList() {
        return ResponseEntity.ok(ApiResponse.success(
                spacedRepetitionService.getTodaysRevisionList(SecurityUtil.getCurrentUserId())));
    }

    @GetMapping("/schedule")
    @Operation(summary = "Get full revision schedule for all topics")
    public ResponseEntity<ApiResponse<List<RevisionScheduleResponse>>> getFullSchedule() {
        return ResponseEntity.ok(ApiResponse.success(
                spacedRepetitionService.getFullSchedule(SecurityUtil.getCurrentUserId())));
    }
}
