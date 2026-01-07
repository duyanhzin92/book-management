package com.example.book.security.permission;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum định nghĩa tất cả các Permission trong hệ thống
 * <p>
 * Mỗi Permission gắn với:
 * <ul>
 *     <li>URL pattern (ví dụ: /api/books, /api/books/**)</li>
 *     <li>HTTP Method (GET, POST, PUT, DELETE)</li>
 *     <li>Mô tả</li>
 * </ul>
 * <p>
 * Khi cần thêm/sửa/xóa permission, chỉ cần sửa Enum này
 */
@Getter
@RequiredArgsConstructor
public enum PermissionEnum {
    
    // Book Permissions
    BOOK_CREATE("POST", "/api/books", "Quyền tạo sách"),
    BOOK_READ("GET", "/api/books/**", "Quyền xem sách"),
    BOOK_UPDATE("PUT", "/api/books/{id}", "Quyền cập nhật sách"),
    BOOK_DELETE("PUT", "/api/books/{id}/delete", "Quyền xóa mềm sách"),
    BOOK_DELETETT("PUT", "/api/books/{id}/delete", "Quyền xóa mềm sách");

    private final String method;
    private final String url;
    private final String description;
}
