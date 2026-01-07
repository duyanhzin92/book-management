package com.example.book.mapper;

import com.example.book.dto.request.CreateBookRequest;
import com.example.book.dto.request.UpdateBookRequest;
import com.example.book.dto.response.BookResponse;
import com.example.book.dto.response.CategoryResponse;
import com.example.book.entity.Book;
import com.example.book.entity.Category;
import org.springframework.stereotype.Component;

/**
 * Mapper để chuyển đổi giữa Book entity và DTOs
 */
@Component
public class BookMapper {

    /**
     * Chuyển đổi CreateBookRequest thành Book entity
     *
     * @param request thông tin tạo sách
     * @param category entity danh mục
     * @return Book entity
     */
    public Book toEntity(CreateBookRequest request, Category category) {
        Book book = new Book();
        book.setIsbn(request.getIsbn());
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setPrice(request.getPrice());
        book.setCategory(category);
        return book;
    }

    /**
     * Cập nhật Book entity từ UpdateBookRequest
     *
     * @param book entity sách cần cập nhật
     * @param request thông tin cập nhật sách
     * @param category entity danh mục
     */
    public void updateEntity(Book book, UpdateBookRequest request, Category category) {
        book.setIsbn(request.getIsbn());
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setPrice(request.getPrice());
        book.setCategory(category);
    }

    /**
     * Chuyển đổi Book entity thành BookResponse
     *
     * @param book entity sách
     * @return BookResponse
     */
    public BookResponse toResponse(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .author(book.getAuthor())
                .price(book.getPrice())
                .category(toCategoryResponse(book.getCategory()))
                .status(book.getStatus())
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .build();
    }

    /**
     * Chuyển đổi Category entity thành CategoryResponse
     *
     * @param category entity danh mục
     * @return CategoryResponse
     */
    private CategoryResponse toCategoryResponse(Category category) {
        if (category == null) {
            return null;
        }
        return CategoryResponse.builder()
                .id(category.getId())
                .code(category.getCode())
                .name(category.getName())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}




