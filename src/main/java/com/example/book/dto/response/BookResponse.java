package com.example.book.dto.response;

import com.example.book.entity.BookStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO cho phản hồi thông tin sách
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookResponse {

    private Long id;
    private String isbn;
    private String title;
    private String author;
    private BigDecimal price;
    private CategoryResponse category;
    private BookStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}




