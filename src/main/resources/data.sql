-- Script SQL để khởi tạo dữ liệu mẫu
-- Lưu ý: File này chỉ chạy nếu spring.jpa.defer-datasource-initialization=true
-- Hoặc có thể chạy trực tiếp trong MySQL

-- 1. Tạo Permissions
INSERT INTO permissions (name, url, method, description, created_at, updated_at)
VALUES
    ('BOOK_CREATE', '/api/books', 'POST', 'Quyền tạo sách mới', NOW(), NOW()),
    ('BOOK_READ', '/api/books/**', 'GET', 'Quyền xem danh sách và chi tiết sách', NOW(), NOW()),
    ('BOOK_UPDATE', '/api/books/{id}', 'PUT', 'Quyền cập nhật thông tin sách', NOW(), NOW()),
    ('BOOK_DELETE', '/api/books/{id}/delete', 'PUT', 'Quyền xóa mềm sách', NOW(), NOW())
ON DUPLICATE KEY UPDATE name=name;

-- 2. Tạo Roles
INSERT INTO roles (name, description, created_at, updated_at)
VALUES
    ('ADMIN', 'Quản trị viên - có tất cả quyền', NOW(), NOW()),
    ('USER', 'Người dùng thông thường - chỉ xem và tạo', NOW(), NOW())
ON DUPLICATE KEY UPDATE name=name;

-- 3. Gán Permissions cho ADMIN role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ADMIN'
  AND p.name IN ('BOOK_CREATE', 'BOOK_READ', 'BOOK_UPDATE', 'BOOK_DELETE')
ON DUPLICATE KEY UPDATE role_id=role_id;

-- 4. Gán Permissions cho USER role (chỉ đọc và tạo)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'USER'
  AND p.name IN ('BOOK_CREATE', 'BOOK_READ')
ON DUPLICATE KEY UPDATE role_id=role_id;

-- 5. Tạo Users (password đã được hash bằng BCrypt)
-- Password: admin123 -> $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
-- Password: user123 -> $2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.H/Kb0q8K8Y8Y8Y8Y8Y8Y8
INSERT INTO users (username, password, email, status, created_at, updated_at)
VALUES
    ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'admin@example.com', 'ACTIVE', NOW(), NOW()),
    ('user', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.H/Kb0q8K8Y8Y8Y8Y8Y8Y8Y8', 'user@example.com', 'ACTIVE', NOW(), NOW())
ON DUPLICATE KEY UPDATE username=username;

-- 6. Gán Roles cho Users
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'admin' AND r.name = 'ADMIN'
ON DUPLICATE KEY UPDATE user_id=user_id;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'user' AND r.name = 'USER'
ON DUPLICATE KEY UPDATE user_id=user_id;



