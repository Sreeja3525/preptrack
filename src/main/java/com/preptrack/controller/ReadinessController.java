package com.preptrack.controller;

import com.preptrack.dto.response.ApiResponse;
import com.preptrack.dto.response.ReadinessScoreResponse;
import com.preptrack.service.ReadinessScoreService;
import com.preptrack.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/readiness")
@RequiredArgsConstructor
@Tag(name = "Readiness Score", description = "Calculate interview readiness for target companies")
public class ReadinessController {

    private final ReadinessScoreService readinessScoreService;

    @GetMapping("/all")
    @Operation(summary = "Get readiness score for all your target companies (sorted best to worst)")
    public ResponseEntity<ApiResponse<List<ReadinessScoreResponse>>> getAllScores() {
        return ResponseEntity.ok(ApiResponse.success(
                readinessScoreService.calculateForAllCompanies(SecurityUtil.getCurrentUserId())));
    }

    @GetMapping("/company/{companyId}")
    @Operation(summary = "Get readiness score for a specific company")
    public ResponseEntity<ApiResponse<ReadinessScoreResponse>> getScoreForCompany(
            @PathVariable Long companyId) {
        return ResponseEntity.ok(ApiResponse.success(
                readinessScoreService.calculateForCompany(companyId, SecurityUtil.getCurrentUserId())));
    }
}
