# Tài liệu triển khai Security (Encryption + Authorization)

## PHẦN I – ENCRYPTION (MÃ HÓA) 🔐

### 1. Tư duy Senior về Encryption

✅ **Đúng:**
- **AES** dùng để mã hóa dữ liệu thật (CIF, số thẻ, thông tin nhạy cảm trong DB)
- **RSA** dùng để trao đổi key và ký số, KHÔNG dùng mã hóa dữ liệu lớn

❌ **Sai (Junior hay mắc):**
- "RSA mạnh hơn AES → dùng RSA hết" → **FAIL ngay vòng security**

### 2. Phân biệt AES & RSA

#### 2.1 AES – Symmetric Encryption
- **Key**: 1 key duy nhất
- **Tốc độ**: Rất nhanh
- **Dùng cho**: Mã hóa dữ liệu thật
- **Nhược điểm**: Phải bảo vệ key

#### 2.2 RSA – Asymmetric Encryption
- **Key**: Public + Private
- **Tốc độ**: Rất chậm
- **Dùng cho**: Trao đổi key, ký số
- **Key size**: ≥ 2048 (bắt buộc)

### 3. Luồng mã hóa chuẩn (Hybrid Encryption)

```
[Client]
   |
   |-- RSA Public Key
   |
Encrypt AES Key bằng RSA
   |
   v
[Server]
Decrypt AES Key bằng RSA Private Key
   |
Dùng AES encrypt/decrypt data
```

### 4. Code Implementation

#### AES Utility
- File: `src/main/java/com/example/book/security/util/AesUtil.java`
- Methods:
  - `encrypt(String plainText, SecretKey key)` - Mã hóa
  - `decrypt(String cipherText, SecretKey key)` - Giải mã
  - `generateKey()` - Sinh AES key 256-bit

#### RSA Utility
- File: `src/main/java/com/example/book/security/util/RsaUtil.java`
- Methods:
  - `encrypt(String data, PublicKey publicKey)` - Mã hóa (chỉ cho key nhỏ)
  - `decrypt(String data, PrivateKey privateKey)` - Giải mã
  - `generateKeyPair()` - Sinh RSA keypair 2048-bit

## PHẦN II – AUTHORIZATION (JWT + PERMISSION) 🔑

### 1. Vì sao KHÔNG hard-code role?

❌ **Sai:**
```java
if (role.equals("ADMIN")) { ... }
@PreAuthorize("hasRole('ADMIN')")
```

✅ **Đúng:**
- Lưu role và permission trong database
- Load permission từ DB khi cần check
- Thêm role mới không cần sửa code

### 2. Mô hình phân quyền chuẩn

```
USER
 └── ROLE
      └── ROLE_PERMISSION
            └── PERMISSION
                  ├── URL
                  └── METHOD
```

### 3. Entity Model

- **User**: Người dùng
- **Role**: Vai trò (ADMIN, USER, MANAGER...)
- **Permission**: Quyền (URL + METHOD)

### 4. JWT Payload (CHỈ chứa cái cần)

```json
{
  "userId": 123,
  "role": "ADMIN"
}
```

❌ **Không chứa:**
- Permission list (load từ DB)
- Thông tin nhạy cảm

### 5. Luồng check permission

```
Request
 → JWT Filter
   → decode token
   → get role
   → load permissions from DB / cache
   → match URL + METHOD
       → OK → tiếp tục
       → FAIL → 403
```

### 6. Implementation

#### JWT Utility
- File: `src/main/java/com/example/book/security/jwt/JwtUtil.java`
- Generate token với userId và role
- Validate token

#### Permission Service
- File: `src/main/java/com/example/book/security/service/PermissionService.java`
- Load permissions từ DB theo role
- Check permission: match URL + METHOD

#### JWT Filter
- File: `src/main/java/com/example/book/security/filter/JwtAuthenticationFilter.java`
- Extract token từ header
- Validate token
- Check permission
- Set authentication context

### 7. Cấu hình

#### SecurityConfig
- File: `src/main/java/com/example/book/config/SecurityConfig.java`
- Public endpoints: `/api/auth/**`, `/swagger-ui/**`, `/api-docs/**`
- Protected endpoints: Tất cả các endpoint khác cần JWT + permission

#### Application Properties
```yaml
jwt:
  secret: your-256-bit-secret-key-for-hmac-sha256-algorithm-minimum-32-characters
  expiration: 86400000 # 24 hours
```

## Cách sử dụng

### 1. Đăng nhập để lấy JWT token

```bash
POST /api/auth/login
{
  "username": "admin",
  "password": "password"
}
```

Response:
```json
{
  "success": true,
  "message": "Đăng nhập thành công",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "userId": 1,
    "role": "ADMIN"
  }
}
```

### 2. Gọi API với JWT token

```bash
GET /api/books
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 3. Setup Permission trong Database

1. Tạo Role: `ADMIN`, `USER`, etc.
2. Tạo Permission với URL và METHOD:
   - `BOOK_CREATE`: `/api/books`, `POST`
   - `BOOK_READ`: `/api/books/**`, `GET`
   - `BOOK_UPDATE`: `/api/books/{id}`, `PUT`
   - `BOOK_DELETE`: `/api/books/{id}/delete`, `PUT`
3. Gán Permission cho Role qua bảng `role_permissions`

## Lưu ý quan trọng

1. **AES** dùng mã hóa dữ liệu, **RSA** dùng trao đổi key
2. **JWT** chỉ chứa userId và role, không chứa permission list
3. **Permission** load từ DB, không hard-code
4. **Role** lưu trong DB, không hard-code trong code
5. **SHA256withRSA** dùng để ký, không phải mã hóa



