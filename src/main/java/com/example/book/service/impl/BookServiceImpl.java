package com.example.book.service.impl;

import com.example.book.dto.request.CreateBookRequest;
import com.example.book.dto.request.UpdateBookRequest;
import com.example.book.dto.response.BookResponse;
import com.example.book.entity.Book;
import com.example.book.entity.BookStatus;
import com.example.book.entity.Category;
import com.example.book.exception.BusinessException;
import com.example.book.exception.ErrorCode;
import com.example.book.exception.ResourceNotFoundException;
import com.example.book.mapper.BookMapper;
import com.example.book.repository.BookRepository;
import com.example.book.repository.CategoryRepository;
import com.example.book.service.BookService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Triển khai của BookService
 */
@Service
@RequiredArgsConstructor
@Transactional
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final BookMapper bookMapper;

    /**
     * Tạo một cuốn sách mới
     *
     * @param request thông tin tạo sách
     * @return BookResponse chứa thông tin chi tiết sách đã tạo
     * @throws BusinessException nếu ISBN đã tồn tại
     * @throws ResourceNotFoundException nếu không tìm thấy danh mục
     */
    @Override
    public BookResponse createBook(CreateBookRequest request) {
        if (bookRepository.existsByIsbnAndStatus(request.getIsbn(), BookStatus.ACTIVE)) {
            throw new BusinessException(ErrorCode.BOOK_ISBN_EXISTS, "Book with ISBN " + request.getIsbn() + " already exists");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CATEGORY_NOT_FOUND, "Category with ID " + request.getCategoryId() + " not found"));

        Book book = bookMapper.toEntity(request, category);
        book.setStatus(BookStatus.ACTIVE);
        Book savedBook = bookRepository.save(book);

        return bookMapper.toResponse(savedBook);
    }

    /**
     * Cập nhật thông tin một cuốn sách
     *
     * @param id mã ID của sách
     * @param request thông tin cập nhật sách
     * @return BookResponse chứa thông tin chi tiết sách đã cập nhật
     * @throws ResourceNotFoundException nếu không tìm thấy sách hoặc danh mục
     * @throws BusinessException nếu ISBN đã tồn tại cho sách khác
     */
    @Override
    public BookResponse updateBook(Long id, UpdateBookRequest request) {
        Book book = bookRepository.findByIdAndStatus(id, BookStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.BOOK_NOT_FOUND, "Book with ID " + id + " not found"));

        // Kiểm tra nếu ISBN đang được thay đổi và ISBN mới đã tồn tại cho các sách đang hoạt động
        if (!book.getIsbn().equals(request.getIsbn()) && bookRepository.existsByIsbnAndStatus(request.getIsbn(), BookStatus.ACTIVE)) {
            throw new BusinessException(ErrorCode.BOOK_ISBN_EXISTS, "Book with ISBN " + request.getIsbn() + " already exists");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CATEGORY_NOT_FOUND, "Category with ID " + request.getCategoryId() + " not found"));

        bookMapper.updateEntity(book, request, category);
        Book updatedBook = bookRepository.save(book);

        return bookMapper.toResponse(updatedBook);
    }

    /**
     * Xóa mềm một cuốn sách theo ID (cập nhật trạng thái thành DELETED)
     *
     * @param id mã ID của sách
     * @return BookResponse chứa thông tin chi tiết sách đã được cập nhật với trạng thái DELETED
     * @throws ResourceNotFoundException nếu không tìm thấy sách
     */
    @Override
    public BookResponse deleteBook(Long id) {
        Book book = bookRepository.findByIdAndStatus(id, BookStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.BOOK_NOT_FOUND, "Book with ID " + id + " not found"));

        book.setStatus(BookStatus.DELETED);
        Book updatedBook = bookRepository.save(book);

        return bookMapper.toResponse(updatedBook);
    }

    /**
     * Lấy danh sách tất cả sách có phân trang
     *
     * @param pageable thông tin phân trang
     * @return Page chứa danh sách BookResponse
     */
    @Override
    @Transactional(readOnly = true)
    public Page<BookResponse> getAllBooks(Pageable pageable) {
        return bookRepository.findByStatus(BookStatus.ACTIVE, pageable)
                .map(bookMapper::toResponse);
    }

    /**
     * Lấy chi tiết sách theo ID
     *
     * @param id mã ID của sách
     * @return BookResponse chứa thông tin chi tiết sách
     * @throws ResourceNotFoundException nếu không tìm thấy sách
     */
    @Override
    @Transactional(readOnly = true)
    public BookResponse getBookDetail(Long id) {
        Book book = bookRepository.findByIdAndStatus(id, BookStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.BOOK_NOT_FOUND, "Book with ID " + id + " not found"));

        return bookMapper.toResponse(book);
    }
}
