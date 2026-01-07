package com.example.book.exception;

import com.example.book.dto.response.ApiResponse;
import com.example.book.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Global exception handler cho toàn bộ REST API layer.
 * <p>
 * Nhiệm vụ:
 * <ul>
 *     <li>Chuẩn hóa response lỗi trả về client</li>
 *     <li>Ẩn chi tiết kỹ thuật nội bộ, chỉ expose {@code errorCode} và message cần thiết</li>
 *     <li>Map các exception domain sang HTTP status tương ứng (404, 400, 500, ...)</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Xử lý {@link ResourceNotFoundException} và trả về HTTP 404.
     *
     * @param ex exception chứa thông tin resource không tìm thấy
     * @return {@link ResponseEntity} với body {@link ErrorResponse} và status 404
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getErrorCode(),
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.<ErrorResponse>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .data(errorResponse)
                        .timestamp(java.time.LocalDateTime.now())
                        .build());
    }

    /**
     * Xử lý {@link BusinessException} và trả về HTTP 400 (Bad Request).
     * Thường dùng cho các lỗi nghiệp vụ như: trùng ISBN, trạng thái không hợp lệ,...
     *
     * @param ex exception nghiệp vụ
     * @return {@link ResponseEntity} với body {@link ErrorResponse} và status 400
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleBusinessException(BusinessException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getErrorCode(),
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<ErrorResponse>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .data(errorResponse)
                        .timestamp(java.time.LocalDateTime.now())
                        .build());
    }

    /**
     * Xử lý lỗi validate từ annotation {@code @Valid} (DTO request).
     * <p>
     * Trả về danh sách message lỗi cụ thể cho từng field.
     *
     * @param ex exception do Spring tạo ra khi validate thất bại
     * @return {@link ResponseEntity} với body {@link ErrorResponse} chứa danh sách lỗi và status 400
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleValidationException(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ErrorCode.VALIDATION_ERROR)
                .message("Validation failed")
                .details(errors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<ErrorResponse>builder()
                        .success(false)
                        .message("Validation failed")
                        .data(errorResponse)
                        .timestamp(java.time.LocalDateTime.now())
                        .build());
    }

    /**
     * Fallback cho các exception khác chưa được handle riêng.
     * <p>
     * Trong môi trường production, nên log chi tiết tại đây (stacktrace, context)
     * và trả về message chung để tránh lộ thông tin nội bộ.
     *
     * @param ex exception bất kỳ
     * @return {@link ResponseEntity} với body {@link ErrorResponse} và status 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleGenericException(Exception ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                ErrorCode.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred: " + ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.<ErrorResponse>builder()
                        .success(false)
                        .message("An unexpected error occurred")
                        .data(errorResponse)
                        .timestamp(java.time.LocalDateTime.now())
                        .build());
    }
}
