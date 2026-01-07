package com.example.book.service;

import com.example.book.dto.request.CreateBookRequest;
import com.example.book.dto.request.UpdateBookRequest;
import com.example.book.dto.response.BookResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Interface service cho các thao tác quản lý sách
 */
public interface BookService {

    /**
     * Tạo một cuốn sách mới
     *
     * @param request thông tin tạo sách
     * @return BookResponse chứa thông tin chi tiết sách đã tạo
     */
    BookResponse createBook(CreateBookRequest request);

    /**
     * Cập nhật thông tin một cuốn sách
     *
     * @param id mã ID của sách
     * @param request thông tin cập nhật sách
     * @return BookResponse chứa thông tin chi tiết sách đã cập nhật
     */
    BookResponse updateBook(Long id, UpdateBookRequest request);

    /**
     * Xóa mềm một cuốn sách theo ID (cập nhật trạng thái thành DELETED)
     *
     * @param id mã ID của sách
     * @return BookResponse chứa thông tin chi tiết sách đã được cập nhật với trạng thái DELETED
     */
    BookResponse deleteBook(Long id);

    /**
     * Lấy danh sách tất cả sách có phân trang
     *
     * @param pageable thông tin phân trang
     * @return Page chứa danh sách BookResponse
     */
    Page<BookResponse> getAllBooks(Pageable pageable);

    /**
     * Lấy chi tiết sách theo ID
     *
     * @param id mã ID của sách
     * @return BookResponse chứa thông tin chi tiết sách
     */
    BookResponse getBookDetail(Long id);
}

