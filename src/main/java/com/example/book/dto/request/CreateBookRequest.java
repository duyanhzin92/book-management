package com.example.book.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO cho việc tạo sách mới
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookRequest {

    @NotBlank(message = "ISBN must not be blank")
    @Size(max = 20, message = "ISBN must not exceed 20 characters")
    private String isbn;

    @NotBlank(message = "Title must not be blank")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @NotBlank(message = "Author must not be blank")
    @Size(max = 150, message = "Author must not exceed 150 characters")
    private String author;

    @NotNull(message = "Price must not be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 12, fraction = 2, message = "Price format is invalid")
    private BigDecimal price;

    @NotNull(message = "Category ID must not be null")
    private Long categoryId;
}





