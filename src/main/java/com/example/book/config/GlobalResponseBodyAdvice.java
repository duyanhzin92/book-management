package com.example.book.config;

import com.example.book.dto.response.ApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Tự động wrap toàn bộ response thành ApiResponse để đảm bảo format đồng nhất.
 * Chỉ áp dụng cho controllers trong package com.example.book.controller
 */
@RestControllerAdvice(basePackages = "com.example.book.controller")
@RequiredArgsConstructor
@Slf4j
public class GlobalResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {
        // Nếu đã là ApiResponse rồi thì không wrap lại
        if (body instanceof ApiResponse) {
            return body;
        }

        // Wrap body thành ApiResponse với message mặc định
        String message = "Success";
        ApiResponse<Object> wrapped = ApiResponse.success(message, body);

        // Special case: String response must be converted to JSON string
        // Nếu body là String, cần serialize ApiResponse thành JSON string
        if (body instanceof String) {
            try {
                response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                String jsonString = objectMapper.writeValueAsString(wrapped);
                log.debug("Converted String response to JSON string");
                return jsonString;
            } catch (JsonProcessingException e) {
                // Lỗi serialize JSON (objectMapper.writeValueAsString failed)
                log.error("Failed to serialize ApiResponse to JSON string: {}", e.getMessage(), e);
                // Fallback: return original String body (không wrap)
                log.warn("Returning original String body without ApiResponse wrapper");
                return body;
            } catch (Exception e) {
                /**
                 * Fallback cuối cùng cho các exception chưa được handle ở trên.
                 * <p>
                 * Các exception đã được handle:
                 * <ul>
                 *     <li>JsonProcessingException - JSON serialize failed</li>
                 * </ul>
                 * <p>
                 * Nếu exception rơi vào đây, có thể là:
                 * <ul>
                 *     <li>NullPointerException - objectMapper null</li>
                 *     <li>RuntimeException khác - logic error</li>
                 *     <li>Unexpected checked exception - cần thêm handler cụ thể</li>
                 * </ul>
                 * <p>
                 * ⚠️ Phải log đầy đủ để debug!
                 */
                log.error("Unexpected error converting String response to JSON: {}", e.getMessage(), e);
                log.error("Exception type: {}", e.getClass().getName());
                log.debug("Exception stacktrace:", e);
                // Fallback: return original String body without wrapper
                return body;
            }
        }

        return wrapped;
    }
}

