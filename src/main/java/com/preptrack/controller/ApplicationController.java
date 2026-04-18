package com.preptrack.controller;

import com.preptrack.dto.request.ApplicationRequest;
import com.preptrack.dto.request.ApplicationStatusUpdateRequest;
import com.preptrack.dto.response.ApiResponse;
import com.preptrack.dto.response.ApplicationResponse;
import com.preptrack.service.ApplicationService;
import com.preptrack.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@Tag(name = "Applications", description = "Track job applications through their lifecycle")
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping
    @Operation(summary = "Add a new job application")
    public ResponseEntity<ApiResponse<ApplicationResponse>> create(@Valid @RequestBody ApplicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(applicationService.create(request, SecurityUtil.getCurrentUserId())));
    }

    @GetMapping
    @Operation(summary = "Get all your job applications")
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(
                applicationService.getAllByUser(SecurityUtil.getCurrentUserId())));
    }

    @GetMapping("/pipeline")
    @Operation(summary = "Get applications grouped by status (Kanban board view)")
    public ResponseEntity<ApiResponse<Map<String, List<ApplicationResponse>>>> getPipeline() {
        return ResponseEntity.ok(ApiResponse.success(
                applicationService.getPipeline(SecurityUtil.getCurrentUserId())));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update application status (follows state machine — invalid transitions rejected)")
    public ResponseEntity<ApiResponse<ApplicationResponse>> updateStatus(
            @PathVariable Long id, @Valid @RequestBody ApplicationStatusUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                applicationService.updateStatus(id, request, SecurityUtil.getCurrentUserId())));
    }
}
