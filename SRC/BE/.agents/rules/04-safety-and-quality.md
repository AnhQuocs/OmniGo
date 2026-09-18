# Quy Tắc 04: An Toàn, Xử Lý Lỗi & Chất Lượng Mã Nguồn

## 1. Xử Lý Lỗi & Ngoại Lệ (Exception Handling)

*   **Tập trung hoá (Centralized Handling)**: Sử dụng `@RestControllerAdvice` trong Spring Boot để bắt và format các exception thành JSON response đồng nhất.
*   **Custom Exceptions**: Sử dụng các Custom Business Exception (ví dụ: `ResourceNotFoundException`, `PaymentFailedException`, `InvalidBookingStateException`) thay vì ném `RuntimeException` chung chung.
*   **Không nuốt lỗi (No Silent Failures)**:
    - CẤM viết khối `catch (Exception e) {}` rỗng mà không log hoặc ném lại lỗi.
    - Trong JavaScript/React, luôn xử lý nhánh `.catch()` hoặc `try/catch` có feedback UI rõ ràng.

---

## 2. Bảo Mật & Quản Lý Bí Mật (Secrets & Security)

*   **Không Hardcode Secret**: Tuyệt đối không hardcode mật khẩu DB, JWT Secret Key, Momo Partner Code / Secret Key, API Key Google Map vào code.
*   **Tham chiếu biến môi trường**: Luôn lấy từ Environment Variables hoặc file `.env` / `application-dev.yml`.
*   **Sanitization**: Kiểm tra và validate dữ liệu đầu vào chống SQL Injection, XSS và NoSQL Injection.

---

## 3. Ghi Log Có Trách Nhiệm (Responsible Logging)

*   **Sử dụng Logger Chuẩn**: Dùng `@Slf4j` (Lombok / SLF4J trong Java) hoặc console wrapper có kiểm soát.
*   **Phân cấp Log đúng mức**:
    - `DEBUG`: Dữ liệu chi tiết cho dev (chỉ bật ở môi trường dev).
    - `INFO`: Các mốc sự kiện quan trọng (ví dụ: "User 123 tạo booking 456 thành công", "Momo callback nhận mã giao dịch XYZ").
    - `WARN`: Tình huống bất thường nhưng hệ thống vẫn tự phục hồi được.
    - `ERROR`: Lỗi nghiêm trọng cần can thiệp (kèm stacktrace có chọn lọc).
*   **Masking Dữ Liệu Nhạy Cảm**: Không bao giờ log plain-text mật khẩu, số thẻ ngân hàng, token JWT đầy đủ vào log file.
