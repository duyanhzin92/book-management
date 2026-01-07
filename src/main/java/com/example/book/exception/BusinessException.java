package com.example.book.exception;

/**
 * Checked-like runtime exception được sử dụng khi vi phạm rule nghiệp vụ
 * nhưng không phải lỗi kỹ thuật (ví dụ: trùng ISBN, trạng thái không hợp lệ).
 * <p>
 * Lưu ý:
 * <ul>
 *     <li>Không log stacktrace ở mức ERROR cho các lỗi nghiệp vụ bình thường</li>
 *     <li>{@link #errorCode} phải là một trong các hằng số được định nghĩa trong {@link ErrorCode}</li>
 * </ul>
 */
public class BusinessException extends RuntimeException {

    /**
     * Mã lỗi nghiệp vụ chuẩn hóa, dùng cho logging và trả về client.
     */
    private final String errorCode;

    /**
     * Khởi tạo {@link BusinessException} chỉ với {@code errorCode}.
     * <p>
     * {@code errorCode} đồng thời được sử dụng làm message mặc định.
     *
     * @param errorCode mã lỗi chuẩn hóa (xem thêm {@link ErrorCode})
     */
    public BusinessException(String errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }

    /**
     * Khởi tạo {@link BusinessException} với {@code errorCode} và message chi tiết.
     *
     * @param errorCode mã lỗi chuẩn hóa (xem thêm {@link ErrorCode})
     * @param message   mô tả chi tiết lỗi (phục vụ debug / hiển thị cho client)
     */
    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Trả về mã lỗi nghiệp vụ chuẩn hóa.
     *
     * @return errorCode tương ứng với lỗi
     */
    public String getErrorCode() {
        return errorCode;
    }
}

