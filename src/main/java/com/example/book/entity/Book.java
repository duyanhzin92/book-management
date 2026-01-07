package com.example.book.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Entity
@Table(name = "books")
public class Book extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ISBN - mã định danh nghiệp vụ duy nhất
     */
    @NotBlank(message = "ISBN must not be blank")
    @Size(max = 20, message = "ISBN must not exceed 20 characters")
    @Column(name = "isbn", nullable = false, unique = true, length = 20)
    private String isbn;

    /**
     * Tiêu đề sách
     */
    @NotBlank(message = "Title must not be blank")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    /**
     * Tên tác giả
     */
    @NotBlank(message = "Author must not be blank")
    @Size(max = 150, message = "Author must not exceed 150 characters")
    @Column(name = "author", nullable = false, length = 150)
    private String author;

    /**
     * Giá sách
     */
    @NotNull(message = "Price must not be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 12, fraction = 2, message = "Price format is invalid")
    @Column(name = "price", nullable = false, precision = 14, scale = 2)
    private BigDecimal price;

    /**
     * Danh mục của sách
     */
    @NotNull(message = "Category must not be null")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    /**
     * Trạng thái sách (ACTIVE hoặc DELETED cho soft delete)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BookStatus status = BookStatus.ACTIVE;

    // ===== Constructors =====
    public Book() {
        // Dành cho JPA
    }

    public Book(String isbn, String title, String author, BigDecimal price, Category category) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.price = price;
        this.category = category;
        this.status = BookStatus.ACTIVE;
    }

    // ===== Getters =====
    public Long getId() {
        return id;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Category getCategory() {
        return category;
    }

    public BookStatus getStatus() {
        return status;
    }

    // ===== Setters =====
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setStatus(BookStatus status) {
        this.status = status;
    }
}

