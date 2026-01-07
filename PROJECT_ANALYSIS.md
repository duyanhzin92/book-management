# 📚 PHÂN TÍCH TOÀN BỘ DỰ ÁN BOOK MANAGEMENT

## 🎯 TỔNG QUAN DỰ ÁN

Đây là một **RESTful API** quản lý sách (Book Management) được xây dựng theo kiến trúc **3-layer** chuẩn enterprise với:
- **Security**: JWT Authentication + Permission-based Authorization
- **Encryption**: AES + RSA utilities
- **Soft Delete**: Không xóa hẳn, chỉ đổi trạng thái
- **Audit Fields**: Tự động track createdAt, updatedAt

---

## 📁 CẤU TRÚC THƯ MỤC

```
com.example.book/
├── 📂 entity/          # Database entities (JPA)
├── 📂 repository/      # Data access layer (Spring Data JPA)
├── 📂 service/         # Business logic layer
├── 📂 controller/      # Presentation layer (REST API)
├── 📂 dto/             # Data Transfer Objects
├── 📂 mapper/          # Entity ↔ DTO converter
├── 📂 exception/       # Custom exceptions & error handling
├── 📂 security/        # Security components (JWT, Encryption)
└── 📂 config/          # Configuration classes
```

---

## 🔄 LUỒNG HOẠT ĐỘNG TỔNG QUAN

### **Luồng 1: Request đến Protected Endpoint**

```
1. Client Request
   ↓
2. SecurityConfig (Spring Security)
   ↓
3. JwtAuthenticationFilter
   ├── Extract JWT token từ header
   ├── Validate token (JwtUtil)
   ├── Decode token → lấy userId, role
   ├── Load permissions từ DB (PermissionService)
   ├── Match URL + METHOD với permissions
   └── OK → Set Authentication Context
   ↓
4. BookController
   ↓
5. BookService (Business Logic)
   ↓
6. BookRepository (Data Access)
   ↓
7. Database
   ↓
8. Response trả về Client
```

### **Luồng 2: Login (Public Endpoint)**

```
1. POST /api/auth/login
   ↓
2. AuthController
   ├── Validate username/password
   ├── Hash password check (PasswordEncoder)
   └── Generate JWT token (JwtUtil)
   ↓
3. Response: JWT Token
```

---

## 📦 CHI TIẾT TỪNG LỚP

### **1. ENTITY LAYER** (`entity/`)

**Mục đích**: Định nghĩa cấu trúc database, mapping JPA

#### **BaseEntity.java**
- **Tác dụng**: Lớp cơ sở cho tất cả entities
- **Chứa**: `createdAt`, `updatedAt` (tự động fill bởi JPA Auditing)
- **Kế thừa**: `Book`, `Category`, `User`, `Role`, `Permission`

#### **Book.java**
- **Tác dụng**: Entity đại diện cho sách
- **Fields**: id, isbn, title, author, price, category, status
- **Quan hệ**: ManyToOne với `Category`
- **Đặc biệt**: Có `status` (ACTIVE/DELETED) cho soft delete

#### **Category.java**
- **Tác dụng**: Entity đại diện cho danh mục sách
- **Fields**: id, code, name

#### **User.java**
- **Tác dụng**: Entity đại diện cho người dùng
- **Fields**: id, username, password (hashed), email, status
- **Quan hệ**: ManyToMany với `Role`

#### **Role.java**
- **Tác dụng**: Entity đại diện cho vai trò
- **Fields**: id, name, description
- **Quan hệ**: ManyToMany với `Permission`

#### **Permission.java**
- **Tác dụng**: Entity đại diện cho quyền
- **Fields**: id, name, url, method, description
- **Đặc biệt**: Định nghĩa quyền bằng URL pattern + HTTP METHOD

---

### **2. REPOSITORY LAYER** (`repository/`)

**Mục đích**: Data Access Layer, giao tiếp với database

#### **BookRepository.java**
- **Tác dụng**: CRUD operations cho Book
- **Methods**:
  - `existsByIsbnAndStatus()` - Check ISBN tồn tại (chỉ ACTIVE)
  - `findByStatus()` - Lấy books theo status (chỉ ACTIVE)
  - `findByIdAndStatus()` - Tìm book theo ID và status

#### **CategoryRepository.java**
- **Tác dụng**: CRUD operations cho Category
- **Methods**: `findByCode()`

#### **UserRepository.java**
- **Tác dụng**: CRUD operations cho User
- **Methods**: `findByUsername()`, `existsByUsername()`

#### **RoleRepository.java**
- **Tác dụng**: CRUD operations cho Role
- **Methods**: 
  - `findByName()`
  - `findPermissionsByRoleName()` - Query permissions của role (JPQL)

#### **PermissionRepository.java**
- **Tác dụng**: CRUD operations cho Permission
- **Methods**: `findByName()`

**Lưu ý**: Repository sử dụng Spring Data JPA, tự động generate query từ method name.

---

### **3. SERVICE LAYER** (`service/`)

**Mục đích**: Business Logic Layer, xử lý nghiệp vụ

#### **BookService.java** (Interface)
- **Tác dụng**: Định nghĩa contract cho Book operations
- **Methods**: createBook, updateBook, deleteBook, getAllBooks, getBookDetail

#### **BookServiceImpl.java** (Implementation)
- **Tác dụng**: Triển khai business logic
- **Luồng xử lý**:
  1. Validate input (ISBN duplicate, category exists)
  2. Convert DTO → Entity (BookMapper)
  3. Save to database
  4. Convert Entity → DTO (BookMapper)
  5. Return response
- **Đặc biệt**:
  - Soft delete: Chỉ đổi status thành DELETED
  - Filter: Chỉ lấy books có status ACTIVE
  - Transaction: `@Transactional` cho write, `@Transactional(readOnly=true)` cho read

#### **PermissionService.java** (`security/service/`)
- **Tác dụng**: Load và check permissions từ database
- **Methods**:
  - `getPermissionsByRole()` - Load permissions của role (có cache)
  - `hasPermission()` - Check role có permission cho URL + METHOD không
- **Đặc biệt**: Không hard-code, load từ DB

---

### **4. CONTROLLER LAYER** (`controller/`)

**Mục đích**: Presentation Layer, nhận HTTP requests và trả về responses

#### **BookController.java**
- **Tác dụng**: REST endpoints cho Book operations
- **Endpoints**:
  - `POST /api/books` - Tạo sách mới
  - `GET /api/books` - Lấy danh sách (có phân trang)
  - `GET /api/books/{id}` - Lấy chi tiết sách
  - `PUT /api/books/{id}` - Cập nhật sách
  - `PUT /api/books/{id}/delete` - Xóa mềm sách
- **Đặc biệt**:
  - Không hard-code HTTP status codes
  - Sử dụng `@Valid` để validate request
  - Swagger documentation

#### **AuthController.java**
- **Tác dụng**: Authentication endpoints
- **Endpoints**:
  - `POST /api/auth/login` - Đăng nhập, nhận JWT token
- **Public**: Không cần JWT token

---

### **5. DTO LAYER** (`dto/`)

**Mục đích**: Data Transfer Objects, tách biệt API contract với Entity

#### **Request DTOs** (`dto/request/`)
- **CreateBookRequest.java**: DTO cho tạo sách mới
- **UpdateBookRequest.java**: DTO cho cập nhật sách
- **LoginRequest.java**: DTO cho đăng nhập

#### **Response DTOs** (`dto/response/`)
- **BookResponse.java**: DTO trả về thông tin sách (bao gồm createdAt, updatedAt, status)
- **CategoryResponse.java**: DTO trả về thông tin category
- **ErrorResponse.java**: DTO trả về lỗi
- **ApiResponse.java**: Wrapper cho API response (success, message, data)

**Lý do dùng DTO**: 
- Tách biệt Entity (database) với API contract
- Bảo mật: Không expose toàn bộ Entity
- Linh hoạt: Có thể thay đổi API mà không ảnh hưởng Entity

---

### **6. MAPPER LAYER** (`mapper/`)

**Mục đích**: Convert giữa Entity và DTO

#### **BookMapper.java**
- **Tác dụng**: Chuyển đổi Book Entity ↔ DTO
- **Methods**:
  - `toEntity()` - CreateBookRequest → Book Entity
  - `updateEntity()` - UpdateBookRequest → Update Book Entity
  - `toResponse()` - Book Entity → BookResponse
  - `toCategoryResponse()` - Category Entity → CategoryResponse

**Lý do dùng Mapper**: 
- Tách biệt logic convert
- Dễ maintain, test
- Có thể dùng MapStruct (nhưng ở đây dùng manual)

---

### **7. EXCEPTION LAYER** (`exception/`)

**Mục đích**: Xử lý lỗi tập trung, chuẩn hóa error response

#### **ErrorCode.java**
- **Tác dụng**: Constants cho error codes
- **Error codes**: BOOK_NOT_FOUND, BOOK_ISBN_EXISTS, CATEGORY_NOT_FOUND, VALIDATION_ERROR, etc.

#### **BusinessException.java**
- **Tác dụng**: Exception cho lỗi nghiệp vụ (400 Bad Request)
- **Ví dụ**: ISBN đã tồn tại, validation failed

#### **ResourceNotFoundException.java**
- **Tác dụng**: Exception cho resource không tìm thấy (404 Not Found)
- **Ví dụ**: Book không tồn tại, Category không tồn tại

#### **GlobalExceptionHandler.java**
- **Tác dụng**: Xử lý tất cả exceptions, trả về ErrorResponse chuẩn
- **Handlers**:
  - `ResourceNotFoundException` → 404
  - `BusinessException` → 400
  - `MethodArgumentNotValidException` → 400 (validation errors)
  - `Exception` → 500 (generic)

**Luồng xử lý lỗi**:
```
Exception xảy ra
   ↓
GlobalExceptionHandler catch
   ↓
Map exception → HTTP status code
   ↓
Trả về ErrorResponse chuẩn
```

---

### **8. SECURITY LAYER** (`security/`)

**Mục đích**: Authentication, Authorization, Encryption

#### **JWT Components** (`security/jwt/`)

**JwtUtil.java**
- **Tác dụng**: Generate và validate JWT tokens
- **Methods**:
  - `generateToken(userId, role)` - Tạo token
  - `validateToken()` - Validate token
  - `getUserIdFromToken()` - Lấy userId từ token
  - `getRoleFromToken()` - Lấy role từ token
- **Payload**: Chỉ chứa `userId` và `role` (không chứa permissions)

**JwtTokenDto.java**
- **Tác dụng**: DTO trả về JWT token response

#### **Filter** (`security/filter/`)

**JwtAuthenticationFilter.java**
- **Tác dụng**: Filter mọi request, check JWT và permission
- **Luồng**:
  1. Extract token từ header `Authorization: Bearer <token>`
  2. Validate token (JwtUtil)
  3. Decode token → lấy userId, role
  4. Load permissions từ DB (PermissionService)
  5. Match request URL + METHOD với permissions
  6. OK → Set Authentication Context, FAIL → 403
- **Đặc biệt**: Không hard-code role, load từ DB

#### **Service** (`security/service/`)

**PermissionService.java**
- **Tác dụng**: Load và check permissions
- **Methods**:
  - `getPermissionsByRole()` - Load permissions (có cache)
  - `hasPermission()` - Check permission cho URL + METHOD
- **URL Matching**: Hỗ trợ exact match, wildcard (`/**`), path variable (`{id}`)

#### **Utilities** (`security/util/`)

**AesUtil.java**
- **Tác dụng**: Mã hóa AES (Symmetric Encryption)
- **Dùng cho**: Mã hóa dữ liệu thật (CIF, số thẻ, thông tin nhạy cảm)
- **Methods**: encrypt, decrypt, generateKey

**RsaUtil.java**
- **Tác dụng**: Mã hóa RSA (Asymmetric Encryption)
- **Dùng cho**: Trao đổi key, ký số (KHÔNG mã hóa dữ liệu lớn)
- **Methods**: encrypt, decrypt, generateKeyPair

---

### **9. CONFIG LAYER** (`config/`)

**Mục đích**: Cấu hình ứng dụng

#### **SecurityConfig.java**
- **Tác dụng**: Cấu hình Spring Security
- **Config**:
  - Public endpoints: `/api/auth/**`, `/swagger-ui/**`, `/api-docs/**`
  - Protected endpoints: Tất cả các endpoint khác
  - JWT Filter: Thêm vào filter chain
  - CORS: Cấu hình cross-origin
  - Password Encoder: BCrypt

#### **SwaggerConfig.java**
- **Tác dụng**: Cấu hình Swagger/OpenAPI documentation
- **URL**: `/swagger-ui.html`

#### **DataInitializer.java**
- **Tác dụng**: Khởi tạo dữ liệu mẫu khi ứng dụng start
- **Tạo**:
  - Users: admin/admin123, user/user123
  - Roles: ADMIN, USER
  - Permissions: BOOK_CREATE, BOOK_READ, BOOK_UPDATE, BOOK_DELETE
  - Mappings: User-Role, Role-Permission

---

## 🔗 CÁCH CÁC LỚP LIÊN KẾT VỚI NHAU

### **Dependency Flow**

```
Controller
   ↓ depends on
Service (Interface)
   ↓ implemented by
ServiceImpl
   ↓ depends on
Repository + Mapper
   ↓ depends on
Entity
   ↓ mapped to
Database
```

### **Security Flow**

```
Request
   ↓
SecurityConfig (Spring Security)
   ↓
JwtAuthenticationFilter
   ├── JwtUtil (validate token)
   └── PermissionService
       └── RoleRepository (load permissions)
   ↓
Controller (nếu có permission)
```

### **Exception Flow**

```
Any Layer throws Exception
   ↓
GlobalExceptionHandler catch
   ↓
ErrorCode (map error code)
   ↓
ErrorResponse (standard format)
   ↓
Client receives error
```

---

## 📊 VÍ DỤ LUỒNG HOẠT ĐỘNG CỤ THỂ

### **Ví dụ 1: Tạo sách mới (POST /api/books)**

```
1. Client gửi request:
   POST /api/books
   Authorization: Bearer <JWT_TOKEN>
   Body: { "isbn": "123", "title": "Java", ... }

2. SecurityConfig kiểm tra: Endpoint này cần authentication

3. JwtAuthenticationFilter:
   - Extract token từ header
   - Validate token → OK
   - Decode → userId=1, role="ADMIN"
   - Load permissions của ADMIN từ DB
   - Check: ADMIN có permission BOOK_CREATE cho POST /api/books?
   - → YES → Set Authentication Context

4. BookController.createBook():
   - Nhận CreateBookRequest
   - Validate (@Valid)
   - Gọi bookService.createBook()

5. BookServiceImpl.createBook():
   - Check ISBN duplicate (BookRepository.existsByIsbnAndStatus)
   - Load Category (CategoryRepository.findById)
   - Convert DTO → Entity (BookMapper.toEntity)
   - Set status = ACTIVE
   - Save (BookRepository.save)
   - Convert Entity → DTO (BookMapper.toResponse)
   - Return BookResponse

6. BookController trả về:
   - Status: 201 Created
   - Body: BookResponse
   - Header: Location: /api/books/1

7. Client nhận response
```

### **Ví dụ 2: Đăng nhập (POST /api/auth/login)**

```
1. Client gửi request:
   POST /api/auth/login
   Body: { "username": "admin", "password": "admin123" }

2. SecurityConfig: Endpoint này là public → Bỏ qua filter

3. AuthController.login():
   - Nhận LoginRequest
   - Validate (@Valid)
   - Tìm User (UserRepository.findByUsername)
   - Check password (PasswordEncoder.matches)
   - Lấy role đầu tiên của user
   - Generate JWT token (JwtUtil.generateToken)
   - Return JwtTokenDto

4. Client nhận JWT token
```

### **Ví dụ 3: Xóa sách (PUT /api/books/1/delete)**

```
1. Client gửi request:
   PUT /api/books/1/delete
   Authorization: Bearer <JWT_TOKEN>

2. JwtAuthenticationFilter:
   - Validate token → OK
   - role = "USER"
   - Load permissions của USER
   - Check: USER có permission BOOK_DELETE cho PUT /api/books/1/delete?
   - → NO → Return 403 Forbidden

3. Nếu có permission:
   - BookController.deleteBook()
   - BookServiceImpl.deleteBook()
   - Tìm book (BookRepository.findByIdAndStatus với ACTIVE)
   - Set status = DELETED (soft delete)
   - Save
   - Return BookResponse với status = DELETED
```

---

## 🎯 CÁC ĐIỂM QUAN TRỌNG

### **1. Soft Delete**
- Không xóa hẳn, chỉ đổi status thành DELETED
- Repository chỉ query books có status ACTIVE
- Đảm bảo data integrity, có thể recover

### **2. Permission-based Authorization**
- Không hard-code role trong code
- Load permissions từ database
- Check permission bằng URL + METHOD matching
- Dễ scale, thêm role/permission mới không cần sửa code

### **3. JWT Token**
- Payload chỉ chứa userId và role
- Không chứa permissions (load từ DB khi cần)
- Stateless authentication

### **4. Separation of Concerns**
- Entity: Database structure
- DTO: API contract
- Mapper: Convert giữa Entity và DTO
- Service: Business logic
- Controller: HTTP handling

### **5. Error Handling**
- Custom exceptions cho từng loại lỗi
- GlobalExceptionHandler xử lý tập trung
- ErrorResponse chuẩn hóa

### **6. Security Best Practices**
- Password hash bằng BCrypt
- JWT token với expiration
- Permission check ở filter level
- CORS configuration

---

## 🔧 CẤU HÌNH QUAN TRỌNG

### **application.yaml**
- Database connection
- JPA/Hibernate settings
- JWT secret và expiration

### **SecurityConfig**
- Public/Protected endpoints
- JWT Filter chain
- CORS settings
- Password encoder

---

## 📝 KẾT LUẬN

Dự án được thiết kế theo **kiến trúc 3-layer** chuẩn enterprise với:
- ✅ Separation of Concerns rõ ràng
- ✅ Security: JWT + Permission-based
- ✅ Soft Delete
- ✅ Audit Fields
- ✅ Error Handling tập trung
- ✅ Không hard-code role/permission
- ✅ Encryption utilities (AES, RSA)

Code sẵn sàng cho production với các best practices của ngân hàng.



