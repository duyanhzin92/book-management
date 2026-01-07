package com.example.book.repository;

import com.example.book.entity.Permission;
import com.example.book.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho entity Role
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Tìm role theo tên
     *
     * @param name tên role
     * @return Optional chứa role nếu tìm thấy
     */
    Optional<Role> findByName(String name);

    /**
     * Lấy tất cả permissions của một role
     * <p>
     * Query này load permissions từ DB dựa trên role name
     * Đây là cách đúng để check permission (không hard-code)
     *
     * @param roleName tên role
     * @return danh sách permissions
     */
    @Query("SELECT p FROM Role r " +
           "JOIN r.permissions p " +
           "WHERE r.name = :roleName")
    List<Permission> findPermissionsByRoleName(@Param("roleName") String roleName);

    /**
     * Load tất cả roles cùng với permissions (JOIN FETCH)
     * Sử dụng để tránh LazyInitializationException
     */
    @Query("SELECT DISTINCT r FROM Role r LEFT JOIN FETCH r.permissions")
    List<Role> findAllWithPermissions();
}

