package com.example.book.exception;

/**
 * Central place to define application-wide error codes.
 * <p>
 * Việc chuẩn hóa {@code errorCode} giúp:
 * <ul>
 *     <li>Logging và tracing dễ dàng hơn</li>
 *     <li>Tích hợp với frontend / kênh khác (mobile, batch, ...) thống nhất</li>
 *     <li>Mapping sang message đa ngôn ngữ (i18n) theo chuẩn enterprise/banking</li>
 * </ul>
 */
public final class ErrorCode {

    /**
     * Private constructor to prevent instantiation.
     * Đây là utility class chỉ chứa hằng số.
     */
    private ErrorCode() {
        // Utility class - do not instantiate
    }

    /**
     * Sách không tồn tại trong hệ thống.
     */
    public static final String BOOK_NOT_FOUND = "BOOK_NOT_FOUND";

    /**
     * ISBN đã tồn tại cho một sách khác (vi phạm ràng buộc duy nhất theo nghiệp vụ).
     */
    public static final String BOOK_ISBN_EXISTS = "BOOK_ISBN_EXISTS";

    /**
     * Category (nhóm sách) không tồn tại.
     */
    public static final String CATEGORY_NOT_FOUND = "CATEGORY_NOT_FOUND";

    /**
     * Dữ liệu đầu vào không hợp lệ (vi phạm validation).
     */
    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";

    /**
     * Lỗi hệ thống không xác định / chưa được mapping cụ thể.
     */
    public static final String INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";

    /**
     * Lỗi xác thực (username/password sai, token không hợp lệ).
     */
    public static final String UNAUTHORIZED = "UNAUTHORIZED";

    /**
     * Không có quyền truy cập (thiếu permission).
     */
    public static final String ACCESS_DENIED = "ACCESS_DENIED";
}

