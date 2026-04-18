package com.preptrack.controller;

import com.preptrack.dto.request.MockInterviewRequest;
import com.preptrack.dto.response.ApiResponse;
import com.preptrack.dto.response.MockInterviewResponse;
import com.preptrack.service.MockInterviewService;
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
@RequestMapping("/api/mock-interviews")
@RequiredArgsConstructor
@Tag(name = "Mock Interviews", description = "Log and review mock interview sessions")
public class MockInterviewController {

    private final MockInterviewService mockInterviewService;

    @PostMapping
    @Operation(summary = "Log a mock interview session")
    public ResponseEntity<ApiResponse<MockInterviewResponse>> log(@Valid @RequestBody MockInterviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(mockInterviewService.log(request, SecurityUtil.getCurrentUserId())));
    }

    @GetMapping
    @Operation(summary = "Get all your mock interview logs")
    public ResponseEntity<ApiResponse<List<MockInterviewResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(
                mockInterviewService.getAllByUser(SecurityUtil.getCurrentUserId())));
    }
}
