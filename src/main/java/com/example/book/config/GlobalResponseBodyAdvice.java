package com.example.book.config;

import com.example.book.dto.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
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
        if (body instanceof String) {
            try {
                response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                return objectMapper.writeValueAsString(wrapped);
            } catch (Exception e) {
                return body;
            }
        }

        return wrapped;
    }
}

