package com.example.book.security.permission;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Enum định nghĩa tất cả các Role trong hệ thống
 * <p>
 * Mỗi Role chứa một tập hợp các Permission
 * <p>
 * Khi cần thêm/sửa/xóa role hoặc thay đổi permission của role,
 * chỉ cần sửa Enum này
 */
@Getter
@RequiredArgsConstructor
public enum RoleEnum {
    
    /**
     * Role ADMIN có tất cả các quyền
     */
    ADMIN("ADMIN", "Quản trị viên", PermissionEnum.values()),
    
    /**
     * Role USER chỉ có quyền đọc sách
     */
    USER("USER", "Người dùng", 
         PermissionEnum.BOOK_READ, PermissionEnum.BOOK_UPDATE);

    private final String name;
    private final String description;
    private final Set<PermissionEnum> permissions;

    /**
     * Constructor với nhiều permissions
     */
    RoleEnum(String name, String description, PermissionEnum... permissions) {
        this.name = name;
        this.description = description;
        this.permissions = Arrays.stream(permissions).collect(Collectors.toSet());
    }

    /**
     * Tìm Role theo tên
     *
     * @param name tên role
     * @return RoleEnum hoặc null nếu không tìm thấy
     */
    public static RoleEnum fromName(String name) {
        for (RoleEnum role : RoleEnum.values()) {
            if (role.getName().equalsIgnoreCase(name)) {
                return role;
            }
        }
        return null;
    }

    /**
     * Kiểm tra role có permission cụ thể không
     *
     * @param permission permission cần check
     * @return true nếu role có permission này
     */
    public boolean hasPermission(PermissionEnum permission) {
        return permissions.contains(permission);
    }
}
