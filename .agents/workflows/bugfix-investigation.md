# Workflow: Điều Tra & Sửa Lỗi (Bugfix & Investigation)

Quy trình chuẩn giúp AI điều tra nguyên nhân gốc rễ (Root Cause) và khắc phục sự cố mà không tạo ra lỗi hồi quy (Regression Bug).

---

## 1. Tái Hiện & Thu Thập Bối Cảnh (Reproduce & Context Gathering)

*   **Đọc kỹ Log & Stack Trace**:
    - Xác định chính xác class, file và dòng mã ném ra ngoại lệ (Exception).
    - Tìm hiểu giá trị biến đầu vào gây ra crash (Null Pointer, Index Out of Bounds, Type Mismatch...).
*   **Khoanh vùng phạm vi (Isolate Scope)**:
    - Lỗi xảy ra ở tầng nào: Frontend (UI/Network), Gateway, hay Backend Microservice cụ thể?
    - Không vội vàng sửa code khi chưa hiểu rõ dòng chảy dữ liệu (Data Flow).

---

## 2. Tìm Nguyên Nhân Gốc Rễ (Root Cause Analysis)

*   Phân biệt giữa **Triệu Chứng (Symptom)** và **Nguyên Nhân Gốc (Root Cause)**.
    - *Ví dụ xấu*: Bọc `try { ... } catch (Exception e) {}` để che giấu lỗi `NullPointerException` (chỉ chữa triệu chứng).
    - *Ví dụ tốt*: Kiểm tra logic khởi tạo object hoặc query database trả về `Optional.empty()` và xử lý fallback hợp lý.
*   Kiểm tra các trường hợp biên (Edge Cases):
    - Dữ liệu rỗng (`null`, `empty string`, `empty array`).
    - Lỗi mạng hoặc Service phụ thuộc (Eureka/Kafka/Database timeout).

---

## 3. Sửa Lỗi Tối Thiểu (Minimal & Precise Fix)

*   Chỉ can thiệp đúng vị trí gây ra lỗi.
*   Không tái cấu trúc lại toàn bộ module xung quanh khi chỉ cần sửa một điều kiện `if/else` hoặc một câu query.
*   Bổ sung null-check hoặc validation phòng ngừa ở điểm đón dữ liệu.

---

## 4. Kiểm Thử Lại & Chống Lỗi Hồi Quy (Verification)

*   Chạy lại kịch bản gây lỗi trước đó để đảm bảo lỗi đã được giải quyết.
*   Chạy lại các kịch bản bình thường (Happy Path) để chắc chắn không làm gãy các tính năng đang chạy tốt.
