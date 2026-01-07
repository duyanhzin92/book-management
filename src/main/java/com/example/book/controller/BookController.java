package com.example.book.controller;

import com.example.book.dto.request.CreateBookRequest;
import com.example.book.dto.request.UpdateBookRequest;
import com.example.book.dto.response.BookResponse;
import com.example.book.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * REST controller cho các thao tác quản lý sách
 */
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Tag(name = "Books", description = "APIs for managing books")
public class BookController {

    private final BookService bookService;

    /**
     * Tạo một cuốn sách mới
     *
     * @param request thông tin tạo sách
     * @return ResponseEntity chứa thông tin sách đã tạo
     */
    @PostMapping
    @Operation(summary = "Create a new book", description = "Creates a new book with the provided information")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Book created successfully",
                    content = @Content(schema = @Schema(implementation = BookResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid input or business rule violation"
            )
    })
    public ResponseEntity<BookResponse> createBook(@Valid @RequestBody CreateBookRequest request) {
        BookResponse response = bookService.createBook(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    /**
     * Cập nhật thông tin một cuốn sách
     *
     * @param id mã ID của sách
     * @param request thông tin cập nhật sách
     * @return ResponseEntity chứa thông tin sách đã cập nhật
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update a book", description = "Updates an existing book with the provided information")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Book updated successfully",
                    content = @Content(schema = @Schema(implementation = BookResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid input or business rule violation"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Book not found"
            )
    })
    public ResponseEntity<BookResponse> updateBook(
            @Parameter(description = "Book ID", required = true) @PathVariable Long id,
            @Valid @RequestBody UpdateBookRequest request) {
        BookResponse response = bookService.updateBook(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Xóa mềm một cuốn sách theo ID (cập nhật trạng thái thành DELETED)
     *
     * @param id mã ID của sách
     * @return ResponseEntity chứa thông tin sách đã được cập nhật với trạng thái DELETED
     */
    @PutMapping("/{id}/delete")
    @Operation(summary = "Soft delete a book", description = "Updates book status to DELETED (soft delete)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Book status updated to DELETED successfully",
                    content = @Content(schema = @Schema(implementation = BookResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Book not found"
            )
    })
    public ResponseEntity<BookResponse> deleteBook(
            @Parameter(description = "Book ID", required = true) @PathVariable Long id) {
        BookResponse response = bookService.deleteBook(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy danh sách tất cả sách có phân trang
     *
     * @param pageable thông tin phân trang
     * @return ResponseEntity chứa một trang danh sách sách
     */
    @GetMapping
    @Operation(summary = "Get all books", description = "Retrieves all books with pagination support")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Books retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))
            )
    })
    public ResponseEntity<Page<BookResponse>> getAllBooks(
            @Parameter(description = "Pagination parameters") @PageableDefault(size = 10) Pageable pageable) {
        Page<BookResponse> books = bookService.getAllBooks(pageable);
        return ResponseEntity.ok(books);
    }

    /**
     * Lấy chi tiết sách theo ID
     *
     * @param id mã ID của sách
     * @return ResponseEntity chứa thông tin chi tiết sách
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get book by ID", description = "Retrieves book details by its ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Book retrieved successfully",
                    content = @Content(schema = @Schema(implementation = BookResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Book not found"
            )
    })
    public ResponseEntity<BookResponse> getBookDetail(
            @Parameter(description = "Book ID", required = true) @PathVariable Long id) {
        BookResponse response = bookService.getBookDetail(id);
        return ResponseEntity.ok(response);
    }
}
