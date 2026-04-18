package com.preptrack.controller;

import com.preptrack.dto.request.CompanyRequest;
import com.preptrack.dto.request.CompanyTopicRequest;
import com.preptrack.dto.response.ApiResponse;
import com.preptrack.dto.response.CompanyResponse;
import com.preptrack.service.CompanyService;
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
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@Tag(name = "Companies", description = "Manage target companies and their required topics")
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping
    @Operation(summary = "Add a target company")
    public ResponseEntity<ApiResponse<CompanyResponse>> create(@Valid @RequestBody CompanyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(companyService.create(request, SecurityUtil.getCurrentUserId())));
    }

    @GetMapping
    @Operation(summary = "Get all your target companies")
    public ResponseEntity<ApiResponse<List<CompanyResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(
                companyService.getAllByUser(SecurityUtil.getCurrentUserId())));
    }

    @PostMapping("/{companyId}/topics")
    @Operation(summary = "Add a required topic to a company (with importance level 1-5)")
    public ResponseEntity<ApiResponse<Void>> addTopic(
            @PathVariable Long companyId, @Valid @RequestBody CompanyTopicRequest request) {
        companyService.addRequiredTopic(companyId, request, SecurityUtil.getCurrentUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Topic added to company", null));
    }

    @DeleteMapping("/{companyId}/topics/{topicId}")
    @Operation(summary = "Remove a required topic from a company")
    public ResponseEntity<ApiResponse<Void>> removeTopic(
            @PathVariable Long companyId, @PathVariable Long topicId) {
        companyService.removeRequiredTopic(companyId, topicId, SecurityUtil.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success("Topic removed from company", null));
    }
}
