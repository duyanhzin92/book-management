package com.example.book.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "categories")
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Mã danh mục, duy nhất để định danh nghiệp vụ
     */
    @NotBlank(message = "Category code must not be blank")
    @Size(max = 50, message = "Category code must not exceed 50 characters")
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    /**
     * Tên hiển thị của danh mục
     */
    @NotBlank(message = "Category name must not be blank")
    @Size(max = 100, message = "Category name must not exceed 100 characters")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    // ===== Constructors =====
    protected Category() {
        // Dành cho JPA
    }

    public Category(String code, String name) {
        this.code = code;
        this.name = name;
    }

    // ===== Getters =====
    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}

