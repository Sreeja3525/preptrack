package com.preptrack.controller;

import com.preptrack.dto.response.ApiResponse;
import com.preptrack.dto.response.WeeklyReportResponse;
import com.preptrack.service.ReportService;
import com.preptrack.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Weekly progress reports with parallel CompletableFuture queries")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/weekly")
    @Operation(summary = "Generate weekly report — runs study log and application queries in parallel via CompletableFuture")
    public ResponseEntity<ApiResponse<WeeklyReportResponse>> getWeeklyReport() {
        Long userId = SecurityUtil.getCurrentUserId();
        WeeklyReportResponse report = reportService.generateWeeklyReport(userId);
        return ResponseEntity.ok(ApiResponse.success(report));
    }
}
