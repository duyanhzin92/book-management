package com.example.book.security.service;

import com.example.book.security.permission.PermissionEnum;
import com.example.book.security.permission.RoleEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service để load permissions từ Enum và kiểm tra permission
 * <p>
 * Luồng check permission:
 * <ol>
 *     <li>Request đến</li>
 *     <li>JWT Filter decode token</li>
 *     <li>Lấy role từ token</li>
 *     <li>Load permissions từ Enum (service này)</li>
 *     <li>Match URL + METHOD (sử dụng UrlMatcherService)</li>
 *     <li>OK → tiếp tục, FAIL → 403</li>
 * </ol>
 * <p>
 * Tất cả role và permission được định nghĩa trong Enum,
 * không hard-code trong code, chỉ cần sửa Enum để thay đổi
 */
@Service
@RequiredArgsConstructor
public class PermissionService {

    private final UrlMatcherService urlMatcherService;

    /**
     * Kiểm tra xem role có permission cho URL và METHOD cụ thể không
     * <p>
     * Load từ RoleEnum, không cần query DB
     *
     * @param roleName tên role (phải match với RoleEnum)
     * @param url      URL từ request
     * @param method   HTTP method từ request
     * @return true nếu có permission
     */
    public boolean hasPermission(String roleName, String url, String method) {
        RoleEnum role = RoleEnum.fromName(roleName);
        
        if (role == null) {
            return false;
        }
        
        // Kiểm tra từng permission của role
        return role.getPermissions().stream()
                .anyMatch(permission -> urlMatcherService.matchesPermission(
                        permission.getUrl(),
                        permission.getMethod(),
                        url,
                        method
                ));
    }

    /**
     * Lấy tất cả permissions của một role từ Enum
     *
     * @param roleName tên role
     * @return Set các PermissionEnum
     */
//    public Set<PermissionEnum> getPermissionsByRole(String roleName) {
//        RoleEnum role = RoleEnum.fromName(roleName);
//
//        if (role == null) {
//            return Set.of();
//        }
//
//        return role.getPermissions();
//    }
}

