# Tinh chỉnh bảo mật Token theo phản hồi: Xử lý lỗi Decrypt & Result API

Kế hoạch này tập trung vào việc cải thiện khả năng xử lý lỗi của `CryptoManager` bằng cách sử dụng `Result<String>` thay vì trả về chuỗi rỗng khi gặp sự cố, giúp `SessionManager` phân biệt rõ ràng giữa "token trống" và "lỗi giải mã".

## User Review Required

> [!NOTE]
> - Thay đổi này không làm thay đổi cách lưu trữ dữ liệu hiện tại, nhưng giúp ứng dụng xử lý các tình huống lỗi (như KeyStore bị hỏng) một cách tin cậy hơn.

## Proposed Changes

### Core Security Component

#### [MODIFY] [CryptoManager.kt](file:///D:/Android%20Studio/Jetpack%20Compose/OmniGo/SRC/APP/app/src/main/java/com/example/omnigo/core/security/CryptoManager.kt)
- Thay đổi kiểu trả về của `encrypt` và `decrypt` từ `String` sang `Result<String>`.
- Sử dụng `runCatching` để bao bọc logic mã hóa/giải mã.
- Loại bỏ `e.printStackTrace()` để tuân thủ bảo mật log.
- Cải thiện việc quản lý `KeyStore` instance (khởi tạo trong `getSecretKey` để tăng độ bền bỉ).

### Core DataStore Component

#### [MODIFY] [SessionManager.kt](file:///D:/Android%20Studio/Jetpack%20Compose/OmniGo/SRC/APP/app/src/main/java/com/example/omnigo/core/datastore/SessionManager.kt)
- Cập nhật hàm `decryptToken`: Sử dụng `.getOrElse` để kiểm tra lỗi. Nếu `decrypt` trả về thất bại, lập tức kích hoạt `clearSession()`.
- Cập nhật hàm `saveSession`: Xử lý giá trị trả về `Result` từ `encrypt`.

---

## Verification Plan

### Automated Tests
- **CryptoManagerTest**: Cập nhật assertion để kiểm tra `isSuccess` và `isFailure`.
- **SessionManagerTest**: Kiểm tra hành vi `clearSession()` khi `CryptoManager.decrypt` trả về `Result.failure`.

### Manual Verification
1. Đăng nhập và sử dụng App bình thường.
2. Giả lập lỗi giải mã (bằng cách sửa code hoặc can thiệp data) để xác nhận App tự động logout an toàn.
