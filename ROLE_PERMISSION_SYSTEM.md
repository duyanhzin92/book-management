# Hệ Thống Role và Permission Dựa Trên Enum

## Tổng Quan

Hệ thống này sử dụng **Enum** để định nghĩa tất cả Role và Permission, không hard-code trong code. Khi cần thay đổi, chỉ cần sửa Enum.

## Cấu Trúc

### 1. PermissionEnum
Định nghĩa tất cả các Permission trong hệ thống.

**File:** `src/main/java/com/example/book/security/permission/PermissionEnum.java`

Mỗi Permission gắn với:
- **URL pattern** (ví dụ: `/api/books`, `/api/books/**`)
- **HTTP Method** (GET, POST, PUT, DELETE)
- **Mô tả**

**Ví dụ:**
```java
BOOK_CREATE("POST", "/api/books", "Quyền tạo sách"),
BOOK_READ("GET", "/api/books/**", "Quyền xem sách"),
BOOK_UPDATE("PUT", "/api/books/{id}", "Quyền cập nhật sách"),
BOOK_DELETE("PUT", "/api/books/{id}/delete", "Quyền xóa mềm sách")
```

### 2. RoleEnum
Định nghĩa tất cả các Role và các Permission mà Role đó có.

**File:** `src/main/java/com/example/book/security/permission/RoleEnum.java`

**Ví dụ:**
```java
ADMIN("ADMIN", "Quản trị viên", PermissionEnum.values()), // Có tất cả quyền
USER("USER", "Người dùng", PermissionEnum.BOOK_READ);     // Chỉ có quyền đọc
```

## Luồng Hoạt Động

1. **Đăng nhập:** User đăng nhập → Token được tạo chứa `userId` và `role`
2. **Request đến:** JWT Filter extract token → Decode lấy `userId` và `role`
3. **Check Permission:** 
   - Load Role từ `RoleEnum` theo tên role trong token
   - Kiểm tra Role có Permission cho URL + Method không
   - OK → tiếp tục, FAIL → 403

## Cách Sử Dụng

### Thêm Permission Mới

Chỉ cần thêm vào `PermissionEnum`:

```java
public enum PermissionEnum {
    // ... existing permissions
    
    BOOK_EXPORT("GET", "/api/books/export", "Quyền xuất sách");
}
```

### Thêm Role Mới

Chỉ cần thêm vào `RoleEnum`:

```java
public enum RoleEnum {
    // ... existing roles
    
    MANAGER("MANAGER", "Quản lý", 
            PermissionEnum.BOOK_READ,
            PermissionEnum.BOOK_UPDATE);
}
```

### Thay Đổi Permission Của Role

Chỉ cần sửa trong `RoleEnum`:

```java
USER("USER", "Người dùng", 
     PermissionEnum.BOOK_READ,
     PermissionEnum.BOOK_CREATE); // Thêm quyền tạo sách
```

## Token Structure

JWT Token chứa:
```json
{
  "userId": 1,
  "role": "ADMIN"
}
```

**Không chứa:** Danh sách permissions (load từ Enum khi cần)

## URL Pattern Matching

Hệ thống hỗ trợ 3 loại pattern:

1. **Exact match:** `/api/books` chỉ match `/api/books`
2. **Wildcard:** `/api/books/**` match `/api/books`, `/api/books/1`, `/api/books/1/comments`
3. **Path variable:** `/api/books/{id}` match `/api/books/1`, `/api/books/123`

## Lợi Ích

✅ **Không hard-code:** Tất cả role/permission trong Enum, không cần sửa controller  
✅ **Dễ quản lý:** Nhìn vào Enum biết được có bao nhiêu role, mỗi role có permission gì  
✅ **Dễ bảo trì:** Chỉ cần sửa Enum để thay đổi  
✅ **Type-safe:** Sử dụng Enum thay vì string  
✅ **Không cần DB:** Không cần query DB để check permission  

## Files Liên Quan

- `PermissionEnum.java` - Định nghĩa tất cả Permission
- `RoleEnum.java` - Định nghĩa tất cả Role và Permission của mỗi Role
- `PermissionService.java` - Service để check permission từ Enum
- `JwtAuthenticationFilter.java` - Filter check permission khi request đến
- `JwtUtil.java` - Utility để encode/decode JWT token
