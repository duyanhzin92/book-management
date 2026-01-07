# Hướng dẫn Setup Database

## Cách 1: Tự động (Khuyến nghị)

Ứng dụng sẽ tự động tạo dữ liệu mẫu khi khởi động lần đầu thông qua `DataInitializer`.

### Chạy ứng dụng:
```bash
mvn spring-boot:run
```

### Dữ liệu sẽ được tạo tự động:
- **Users:**
  - `admin` / `admin123` (role: ADMIN)
  - `user` / `user123` (role: USER)

- **Roles:**
  - `ADMIN` - Quản trị viên (có tất cả quyền)
  - `USER` - Người dùng thông thường (chỉ đọc và tạo)

- **Permissions:**
  - `BOOK_CREATE` - POST `/api/books`
  - `BOOK_READ` - GET `/api/books/**`
  - `BOOK_UPDATE` - PUT `/api/books/{id}`
  - `BOOK_DELETE` - PUT `/api/books/{id}/delete`

## Cách 2: Chạy SQL Script thủ công

Nếu muốn chạy SQL script trực tiếp trong MySQL:

1. Mở MySQL Workbench hoặc MySQL CLI
2. Chọn database `bookmanagement`
3. Chạy file `src/main/resources/data.sql`

## Cấu trúc Database

### Bảng `users`
- `id` - Primary key
- `username` - Tên đăng nhập (unique)
- `password` - Mật khẩu đã hash (BCrypt)
- `email` - Email (unique)
- `status` - Trạng thái (ACTIVE, INACTIVE, LOCKED)
- `created_at`, `updated_at` - Timestamps

### Bảng `roles`
- `id` - Primary key
- `name` - Tên role (unique)
- `description` - Mô tả
- `created_at`, `updated_at` - Timestamps

### Bảng `permissions`
- `id` - Primary key
- `name` - Tên permission (unique)
- `url` - URL pattern
- `method` - HTTP method (GET, POST, PUT, DELETE)
- `description` - Mô tả
- `created_at`, `updated_at` - Timestamps

### Bảng `user_roles` (Many-to-Many)
- `user_id` - Foreign key to users
- `role_id` - Foreign key to roles

### Bảng `role_permissions` (Many-to-Many)
- `role_id` - Foreign key to roles
- `permission_id` - Foreign key to permissions

## Test Login

### Admin user:
```bash
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

### Normal user:
```bash
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "user",
  "password": "user123"
}
```

## Lưu ý

1. **DataInitializer** chỉ chạy khi database trống (chưa có user nào)
2. Nếu muốn reset dữ liệu, xóa các bảng và chạy lại ứng dụng
3. Password được hash bằng BCrypt, không lưu plain text
4. Có thể thêm users/roles/permissions mới qua API hoặc SQL



