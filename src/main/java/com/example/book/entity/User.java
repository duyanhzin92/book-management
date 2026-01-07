package com.example.book.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Entity đại diện cho người dùng trong hệ thống
 */
@Entity
@Table(name = "users")
@Getter
@Setter
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Tên đăng nhập (username)
     */
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    /**
     * Mật khẩu (đã được hash)
     */
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    /**
     * Email người dùng
     */
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    /**
     * Trạng thái tài khoản (ACTIVE, INACTIVE, LOCKED)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    /**
     * Danh sách roles của user
     * Một user có thể có nhiều roles
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    /**
     * Constructor mặc định cho JPA
     */
    public User() {
        // Dành cho JPA
    }

    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.status = UserStatus.ACTIVE;
    }
}



