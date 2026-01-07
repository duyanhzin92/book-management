package com.example.book.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity đại diện cho quyền (permission) trong hệ thống
 * <p>
 * Permission được định nghĩa bởi:
 * <ul>
 *     <li>URL pattern (ví dụ: /api/books/**)</li>
 *     <li>HTTP METHOD (GET, POST, PUT, DELETE)</li>
 * </ul>
 * <p>
 * Luồng check permission:
 * Request → JWT Filter → decode token → get role → load permissions from DB
 * → match URL + METHOD → OK/Fail
 */
@Entity
@Table(name = "permissions")
@Getter
@Setter
public class Permission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Tên permission (ví dụ: BOOK_CREATE, BOOK_READ, BOOK_UPDATE, BOOK_DELETE)
     */
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    /**
     * URL pattern (ví dụ: /api/books, /api/books/**, /api/books/{id})
     * Có thể dùng regex pattern để match
     */
    @Column(name = "url", nullable = false, length = 255)
    private String url;

    /**
     * HTTP Method (GET, POST, PUT, DELETE, PATCH)
     */
    @Column(name = "method", nullable = false, length = 10)
    private String method;

    /**
     * Mô tả permission
     */
    @Column(name = "description", length = 255)
    private String description;

    /**
     * Constructor mặc định cho JPA
     */
    protected Permission() {
        // Dành cho JPA
    }

    public Permission(String name, String url, String method, String description) {
        this.name = name;
        this.url = url;
        this.method = method;
        this.description = description;
    }
}



