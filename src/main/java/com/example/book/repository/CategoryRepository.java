package com.example.book.repository;

import com.example.book.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository cho entity Category
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Tìm danh mục theo mã code
     *
     * @param code mã code của danh mục
     * @return Optional chứa danh mục nếu tìm thấy
     */
    Optional<Category> findByCode(String code);
}





