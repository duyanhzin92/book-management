package com.example.book.controller;

import com.example.book.dto.request.LoginRequest;
import com.example.book.dto.request.RegisterRequest;
import com.example.book.dto.response.ApiResponse;
import com.example.book.entity.Role;
import com.example.book.entity.User;
import com.example.book.entity.UserStatus;
import com.example.book.exception.BusinessException;
import com.example.book.exception.ErrorCode;
import com.example.book.repository.RoleRepository;
import com.example.book.repository.UserRepository;
import com.example.book.security.jwt.JwtTokenDto;
import com.example.book.security.jwt.JwtUtil;
import com.example.book.security.service.EncryptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * Controller cho authentication (login, register)
 * <p>
 * Endpoints này là public, không cần JWT token
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "APIs cho đăng nhập và đăng ký")
public class AuthController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final EncryptionService encryptionService;

    /**
     * Đăng nhập và nhận JWT token (Hybrid Encryption: AES + RSA)
     * <p>
     * Request body:
     * <pre>
     * {
     *   "username": "abc",
     *   "encryptedPassword": "...",  // AES(password, aesKey), Base64
     *   "encryptedAesKey": "..."     // RSA(aesKeyBase64, serverPublicKey), Base64
     * }
     * </pre>
     *
     * @param request thông tin đăng nhập (username, encryptedPassword, encryptedAesKey)
     * @return JWT token
     */
    @PostMapping("/login")
    @Operation(
            summary = "Đăng nhập (AES + RSA)",
            description = "Client encrypt password bằng AES, encrypt AES key bằng RSA public key, " +
                    "server giải mã và authenticate, sau đó trả về JWT token"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Đăng nhập thành công",
                    content = @Content(schema = @Schema(implementation = JwtTokenDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Username hoặc password không đúng"
            )
    })
    public ResponseEntity<ApiResponse<JwtTokenDto>> login(@Valid @RequestBody LoginRequest request) {
        // 1. Tìm user theo username
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.VALIDATION_ERROR, "Username hoặc password không đúng"));

        // 2. Giải mã password từ payload AES + RSA
        String rawPassword;
        try {
            rawPassword = encryptionService.decryptClientPassword(
                    request.getEncryptedPassword(),
                    request.getEncryptedAesKey()
            );
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Payload đăng nhập không hợp lệ");
        }

        // 3. So khớp password sau khi decrypt với password đã hash trong DB
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Username hoặc password không đúng");
        }

        // Lấy role đầu tiên của user (trong thực tế có thể có nhiều roles)
        String role = user.getRoles().stream()
                .findFirst()
                .map(r -> r.getName())
                .orElse("USER");

        // Generate JWT token (chỉ chứa userId và role)
        String token = jwtUtil.generateToken(user.getId(), role);

        JwtTokenDto tokenDto = JwtTokenDto.builder()
                .token(token)
                .userId(user.getId())
                .role(role)
                .build();

        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công", tokenDto));
    }

    /**
     * Đăng ký tài khoản mới
     *
     * @param request thông tin đăng ký (username, email, password, confirmPassword)
     * @return JWT token
     */
    @PostMapping("/register")
    @Operation(summary = "Đăng ký tài khoản", description = "Đăng ký tài khoản mới với username, email và password. Password phải khớp với confirmPassword. Tự động gán role USER và trả về JWT token.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Đăng ký thành công",
                    content = @Content(schema = @Schema(implementation = JwtTokenDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Dữ liệu không hợp lệ (username/email đã tồn tại, password không khớp, validation failed)"
            )
    })
    public ResponseEntity<ApiResponse<JwtTokenDto>> register(@Valid @RequestBody RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Confirm password không khớp");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Username đã tồn tại");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Email đã tồn tại");
        }

        // Default role USER
        Role userRole = roleRepository.findByName("USER")
                .orElseGet(() -> roleRepository.save(new Role("USER", "Người dùng")));

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(UserStatus.ACTIVE);
        user.setRoles(Set.of(userRole));

        User saved = userRepository.save(user);

        String role = "USER";
        String token = jwtUtil.generateToken(saved.getId(), role);

        JwtTokenDto tokenDto = JwtTokenDto.builder()
                .token(token)
                .userId(saved.getId())
                .role(role)
                .build();

        return ResponseEntity.ok(ApiResponse.success("Đăng ký thành công", tokenDto));
    }
}

