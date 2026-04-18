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

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Async weekly progress reports")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/weekly")
    @Operation(summary = "Generate weekly report (async — fetches study logs and applications in parallel)")
    public CompletableFuture<ResponseEntity<ApiResponse<WeeklyReportResponse>>> getWeeklyReport() {
        Long userId = SecurityUtil.getCurrentUserId();
        return reportService.generateWeeklyReport(userId)
                .thenApply(report -> ResponseEntity.ok(ApiResponse.success(report)));
    }
}
