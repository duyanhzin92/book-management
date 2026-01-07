# Hướng dẫn Setup Encryption (AES & RSA)

## 1. AES Key

AES key đã được cấu hình trong `application.yaml`:
```yaml
encryption:
  aes:
    key: MLEQ/ogfPk0z7ZtutxRWRodUqu48mEvorrUWagjq5Sc=
```

Key này đã được mã hóa Base64 và sẵn sàng sử dụng.

## 2. RSA Keys

### Tạo RSA Key Pair

**Cách 1: Chạy RsaKeyGenerator.java**

1. Mở file `src/main/java/com/example/book/security/util/RsaKeyGenerator.java`
2. Chạy `main()` method
3. Copy output vào `application.yaml`:

```yaml
encryption:
  rsa:
    public-key: <output_public_key>
    private-key: <output_private_key>
```

**Cách 2: Sử dụng OpenSSL**

```bash
# Generate private key
openssl genpkey -algorithm RSA -out private_key.pem -pkeyopt rsa_keygen_bits:2048

# Extract public key
openssl rsa -pubout -in private_key.pem -out public_key.pem

# Convert to Base64 (one line)
openssl base64 -in private_key.pem -out private_key_base64.txt
openssl base64 -in public_key.pem -out public_key_base64.txt
```

**Cách 3: Sử dụng Java code**

```java
import com.example.book.security.util.RsaUtil;
import java.util.Base64;

KeyPair keyPair = RsaUtil.generateKeyPair();
String publicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
String privateKey = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
```

## 3. Cấu hình trong application.yaml

```yaml
encryption:
  aes:
    key: MLEQ/ogfPk0z7ZtutxRWRodUqu48mEvorrUWagjq5Sc=
  rsa:
    public-key: <your_rsa_public_key_base64>
    private-key: <your_rsa_private_key_base64>
```

## 4. Sử dụng EncryptionService

### Mã hóa/Giải mã với AES
```java
@Autowired
private EncryptionService encryptionService;

// Mã hóa
String encrypted = encryptionService.encryptAES("plaintext");

// Giải mã
String decrypted = encryptionService.decryptAES(encrypted);
```

### Mã hóa/Giải mã với RSA
```java
// Mã hóa (chỉ dùng cho dữ liệu nhỏ như keys)
String encrypted = encryptionService.encryptRSA("small_data");

// Giải mã
String decrypted = encryptionService.decryptRSA(encrypted);
```

### Hybrid Encryption (Mã hóa AES key bằng RSA)
```java
// Client: Mã hóa AES key bằng RSA public key
String encryptedAESKey = encryptionService.encryptAESKeyWithRSA(aesKeyBase64);

// Server: Giải mã AES key bằng RSA private key
String aesKeyBase64 = encryptionService.decryptAESKeyWithRSA(encryptedAESKey);
```

## 5. API Endpoints để Test

- `POST /api/encryption/aes/encrypt` - Mã hóa AES
- `POST /api/encryption/aes/decrypt` - Giải mã AES
- `POST /api/encryption/rsa/encrypt` - Mã hóa RSA
- `POST /api/encryption/rsa/decrypt` - Giải mã RSA
- `GET /api/encryption/rsa/public-key` - Lấy RSA public key
- `POST /api/encryption/hybrid/encrypt-aes-key` - Mã hóa AES key bằng RSA

Xem Swagger UI tại: `/swagger-ui.html`

## Lưu ý

1. **AES Key**: Phải bảo vệ cẩn thận, không commit vào git (nên dùng environment variables)
2. **RSA Private Key**: Phải bảo mật tuyệt đối, chỉ server mới có
3. **RSA Public Key**: Có thể công khai cho client
4. **Key Size**: 
   - AES: 256-bit (đã đủ mạnh)
   - RSA: 2048-bit (minimum cho production)
