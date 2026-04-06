package com.finance.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    /** Indicates if the operation succeeded */
    private boolean success;

    /** Human-readable message describing the result */
    private String message;

    /** The response payload (null for error responses or operations with no return data) */
    private T data;

    // ── Static Factory Methods ────────────────────────────────────────────────

    /**
     * Create a success response with data and message.
     *
     * @param message Descriptive success message
     * @param data    The response payload
     * @return Populated ApiResponse with success=true
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .build();
    }

    
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
}
