package com.example.book.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Entity đại diện cho vai trò (role) trong hệ thống
 * <p>
 * Mô hình phân quyền chuẩn:
 * USER → ROLE → ROLE_PERMISSION → PERMISSION (URL + METHOD)
 * <p>
 * Không hard-code role trong code, mà lưu trong database
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
public class Role extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Tên role (ví dụ: ADMIN, USER, MANAGER)
     * Không hard-code, lưu trong DB
     */
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    /**
     * Mô tả role
     */
    @Column(name = "description", length = 255)
    private String description;

    /**
     * Danh sách permissions của role
     * Một role có thể có nhiều permissions
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();

    /**
     * Constructor mặc định cho JPA
     */
    protected Role() {
        // Dành cho JPA
    }

    public Role(String name, String description) {
        this.name = name;
        this.description = description;
    }
}



