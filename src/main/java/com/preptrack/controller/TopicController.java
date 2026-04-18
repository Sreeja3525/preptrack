package com.preptrack.controller;

import com.preptrack.domain.TopicCategory;
import com.preptrack.dto.request.TopicRequest;
import com.preptrack.dto.response.ApiResponse;
import com.preptrack.dto.response.TopicResponse;
import com.preptrack.service.TopicService;
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
@RequestMapping("/api/topics")
@RequiredArgsConstructor
@Tag(name = "Topics", description = "Manage study topics")
public class TopicController {

    private final TopicService topicService;

    @PostMapping
    @Operation(summary = "Add a new study topic")
    public ResponseEntity<ApiResponse<TopicResponse>> create(@Valid @RequestBody TopicRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(topicService.create(request, SecurityUtil.getCurrentUserId())));
    }

    @GetMapping
    @Operation(summary = "Get all your topics")
    public ResponseEntity<ApiResponse<List<TopicResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(
                topicService.getAllByUser(SecurityUtil.getCurrentUserId())));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get topics filtered by category")
    public ResponseEntity<ApiResponse<List<TopicResponse>>> getByCategory(@PathVariable TopicCategory category) {
        return ResponseEntity.ok(ApiResponse.success(
                topicService.getByCategory(SecurityUtil.getCurrentUserId(), category)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a topic")
    public ResponseEntity<ApiResponse<TopicResponse>> update(
            @PathVariable Long id, @Valid @RequestBody TopicRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                topicService.update(id, request, SecurityUtil.getCurrentUserId())));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a topic")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        topicService.delete(id, SecurityUtil.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success("Topic deleted", null));
    }
}
