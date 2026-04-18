package com.preptrack.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

// Generic API response wrapper — every endpoint returns this
// success: true/false, message: optional human text, data: the actual payload
@JsonInclude(JsonInclude.Include.NON_NULL)  // omit null fields from JSON output
public record ApiResponse<T>(
        boolean success,
        String message,
        T data
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, null, data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
}
