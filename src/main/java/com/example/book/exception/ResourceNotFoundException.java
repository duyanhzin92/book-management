package com.example.book.exception;

/**
 * Runtime exception được sử dụng khi không tìm thấy resource theo yêu cầu
 * (ví dụ: Book, Category, ... không tồn tại trong DB).
 * <p>
 * Đây là dạng lỗi nghiệp vụ phổ biến, thường được map sang HTTP status 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Mã lỗi chuẩn hóa, thường là {@link ErrorCode#BOOK_NOT_FOUND} hoặc {@link ErrorCode#CATEGORY_NOT_FOUND}.
     */
    private final String errorCode;

    /**
     * Khởi tạo {@link ResourceNotFoundException} chỉ với {@code errorCode}.
     * <p>
     * {@code errorCode} đồng thời được sử dụng làm message mặc định.
     *
     * @param errorCode mã lỗi chuẩn hóa (xem thêm {@link ErrorCode})
     */
    public ResourceNotFoundException(String errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }

    /**
     * Khởi tạo {@link ResourceNotFoundException} với {@code errorCode} và message chi tiết.
     *
     * @param errorCode mã lỗi chuẩn hóa (xem thêm {@link ErrorCode})
     * @param message   mô tả chi tiết lỗi (ví dụ: \"Book with ID 10 not found\")
     */
    public ResourceNotFoundException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Trả về mã lỗi chuẩn hóa tương ứng với resource không tìm thấy.
     *
     * @return errorCode tương ứng với lỗi
     */
    public String getErrorCode() {
        return errorCode;
    }
}
