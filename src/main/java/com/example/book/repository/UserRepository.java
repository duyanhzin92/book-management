package com.example.book.repository;

import com.example.book.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository cho entity User
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Tìm user theo username
     *
     * @param username tên đăng nhập
     * @return Optional chứa user nếu tìm thấy
     */
    Optional<User> findByUsername(String username);

    /**
     * Kiểm tra username đã tồn tại chưa
     *
     * @param username tên đăng nhập
     * @return true nếu đã tồn tại
     */
    boolean existsByUsername(String username);

    /**
     * Tìm user theo email
     *
     * @param email email
     * @return Optional chứa user nếu tìm thấy
     */
    Optional<User> findByEmail(String email);

    /**
     * Kiểm tra email đã tồn tại chưa
     *
     * @param email email
     * @return true nếu đã tồn tại
     */
    boolean existsByEmail(String email);
}



