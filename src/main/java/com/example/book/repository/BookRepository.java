package com.example.book.repository;

import com.example.book.entity.Book;
import com.example.book.entity.BookStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository cho entity Book
 */
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    /**
     * Kiểm tra xem có sách đang hoạt động nào tồn tại theo ISBN không
     *
     * @param isbn ISBN cần kiểm tra
     * @return true nếu tồn tại, false nếu không
     */
    boolean existsByIsbnAndStatus(String isbn, BookStatus status);

    /**
     * Tìm sách đang hoạt động theo ISBN
     *
     * @param isbn ISBN cần tìm
     * @param status trạng thái sách
     * @return Optional chứa sách nếu tìm thấy
     */
    Optional<Book> findByIsbnAndStatus(String isbn, BookStatus status);

    /**
     * Tìm tất cả sách đang hoạt động có phân trang
     *
     * @param status trạng thái sách
     * @param pageable thông tin phân trang
     * @return Page chứa danh sách sách
     */
    Page<Book> findByStatus(BookStatus status, Pageable pageable);

    /**
     * Tìm sách đang hoạt động theo ID
     *
     * @param id mã ID của sách
     * @param status trạng thái sách
     * @return Optional chứa sách nếu tìm thấy
     */
    Optional<Book> findByIdAndStatus(Long id, BookStatus status);

    /**
     * Tìm sách theo ID (bao gồm cả sách đã xóa) - dùng cho mục đích nội bộ
     *
     * @param id mã ID của sách
     * @return Optional chứa sách nếu tìm thấy
     */
    @Override
    Optional<Book> findById(Long id);
}

