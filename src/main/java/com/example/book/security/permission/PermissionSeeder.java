package com.example.book.security.permission;

import com.example.book.entity.Permission;
import com.example.book.entity.Role;
import com.example.book.repository.PermissionRepository;
import com.example.book.repository.RoleRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Seeder để đồng bộ permissions và roles từ enum vào database
 * <p>
 * Khi khởi động ứng dụng, seeder này sẽ:
 * 1. Xóa các permissions không còn trong PermissionEnum
 * 2. Thêm/cập nhật permissions từ PermissionEnum
 * 3. Cập nhật role_permissions theo RoleEnum
 * <p>
 * Mục đích: Đảm bảo database luôn đồng bộ với enum definitions
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PermissionSeeder implements ApplicationListener<ContextRefreshedEvent> {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private boolean alreadySeeded = false;

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // Chỉ seed một lần để tránh seed nhiều lần khi context refresh
        if (alreadySeeded) {
            return;
        }
        alreadySeeded = true;
        seed();
    }

    @Transactional
    public void seed() {
        log.info("Bắt đầu cập nhật permissions và roles...");

        // 1. Lấy danh sách tên permissions từ enum
        Set<String> enumPermissionNames = java.util.Arrays.stream(PermissionEnum.values())
                .map(Enum::name)
                .collect(Collectors.toSet());

        // 2. Xóa các permissions không còn trong enum
        List<Permission> allPermissions = permissionRepository.findAll();
        List<Permission> permissionsToDelete = allPermissions.stream()
                .filter(p -> !enumPermissionNames.contains(p.getName()))
                .collect(Collectors.toList());
        
        if (!permissionsToDelete.isEmpty()) {
            log.info("Xóa {} permissions không còn trong enum: {}", 
                    permissionsToDelete.size(), 
                    permissionsToDelete.stream().map(Permission::getName).collect(Collectors.toList()));
            // Xóa role_permissions trước
            for (Permission permission : permissionsToDelete) {
                deletePermissionAndRolePermissions(permission.getName());
            }
        }

        // 3. Thêm/cập nhật permissions từ PermissionEnum
        log.info("Cập nhật permissions từ PermissionEnum...");
        Set<Permission> permissionMap = new HashSet<>();
        for (PermissionEnum permissionEnum : PermissionEnum.values()) {
            Permission permission = permissionRepository.findByName(permissionEnum.name())
                    .orElse(new Permission(
                            permissionEnum.name(),
                            permissionEnum.getUrl(),
                            permissionEnum.getMethod(),
                            permissionEnum.getDescription()
                    ));

            // Cập nhật thông tin nếu đã tồn tại
            permission.setUrl(permissionEnum.getUrl());
            permission.setMethod(permissionEnum.getMethod());
            permission.setDescription(permissionEnum.getDescription());

            Permission savedPermission = permissionRepository.save(permission);
            permissionMap.add(savedPermission);
            log.info("Đã cập nhật permission: {}", permissionEnum.name());
        }

        // 4. Cập nhật role_permissions theo RoleEnum
        log.info("Cập nhật role_permissions theo RoleEnum...");
        for (RoleEnum roleEnum : RoleEnum.values()) {
            Role role = roleRepository.findByName(roleEnum.getName())
                    .orElse(new Role(roleEnum.getName(), roleEnum.getDescription()));

            // Cập nhật description nếu role đã tồn tại
            role.setDescription(roleEnum.getDescription());

            // Gán permissions cho role
            Set<Permission> rolePermissions = new HashSet<>();
            for (PermissionEnum permissionEnum : roleEnum.getPermissions()) {
                Permission permission = permissionMap.stream()
                        .filter(p -> p.getName().equals(permissionEnum.name()))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException(
                                "Không tìm thấy permission: " + permissionEnum.name()));
                rolePermissions.add(permission);
            }
            role.setPermissions(rolePermissions);

            Role savedRole = roleRepository.save(role);
            log.info("Đã cập nhật role: {} với {} permissions", 
                    savedRole.getName(), savedRole.getPermissions().size());
        }

        log.info("Hoàn thành cập nhật permissions và roles!");
    }

    /**
     * Xóa permission và tất cả role_permissions liên quan
     *
     * @param permissionName tên permission cần xóa
     */
    @Transactional
    public void deletePermissionAndRolePermissions(String permissionName) {
        log.info("Xóa permission và role_permissions: {}", permissionName);
        
        Permission permission = permissionRepository.findByName(permissionName)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy permission: " + permissionName));

        // Xóa tất cả role_permissions liên quan
        entityManager.createNativeQuery(
                "DELETE FROM role_permissions WHERE permission_id = :permissionId")
                .setParameter("permissionId", permission.getId())
                .executeUpdate();

        // Xóa permission
        permissionRepository.delete(permission);
        log.info("Đã xóa permission: {}", permissionName);
    }

    /**
     * Xóa tất cả role_permissions của một role
     *
     * @param roleName tên role
     */
    @Transactional
    public void deleteRolePermissions(String roleName) {
        log.info("Xóa tất cả role_permissions của role: {}", roleName);
        
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy role: " + roleName));

        // Xóa tất cả role_permissions của role bằng native query
        entityManager.createNativeQuery(
                "DELETE FROM role_permissions WHERE role_id = :roleId")
                .setParameter("roleId", role.getId())
                .executeUpdate();

        // Refresh entity để đồng bộ với database
        entityManager.refresh(role);
        log.info("Đã xóa tất cả role_permissions của role: {}", roleName);
    }
}
