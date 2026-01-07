package com.example.book.repository;

import com.example.book.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho entity Permission
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    /**
     * Tìm permission theo tên
     *
     * @param name tên permission
     * @return Optional chứa permission nếu tìm thấy
     */
    Optional<Permission> findByName(String name);

    boolean existsByName(String name);
}

