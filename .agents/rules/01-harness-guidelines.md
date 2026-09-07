# Quy Tắc 01: Phương Pháp Harness & Test-First Constraint

## 1. Bản Chất Của Phương Pháp Harness Trong AI Coding

Phương pháp **Harness (Khung Neo Chặn)** là kỹ thuật xây dựng một hệ thống khung kiểm soát chặt chẽ bao quanh đoạn mã hoặc chức năng mà AI chuẩn bị sửa đổi hay viết mới. 

Mục tiêu tối thượng: **AI chỉ được phép hoạt động bên trong vùng giới hạn đã được neo chặn, không vượt qua lằn ranh đỏ (boundary), và mọi kết quả phải thỏa mãn bộ kiểm thử định sẵn.**

```
+-------------------------------------------------------------+
|                      HARNESS BOUNDARY                       |
|  +---------------------+           +---------------------+  |
|  | Input Contracts     |           | Output Contracts    |  |
|  | (DTO / Params / Ty) |  ======>  | (Response / Events) |  |
|  +---------------------+           +---------------------+  |
|                 |                         ^                 |
|                 v                         |                 |
|            +-----------------------------------+            |
|            |    AI EXECUTION / CODE LOGIC      |            |
|            |  (Strictly Limited to Scope Only) |            |
|            +-----------------------------------+            |
+-------------------------------------------------------------+
```

---

## 2. Các Bước Thực Thi Theo Harness

### Bước 1: Khóa Chặt Hợp Đồng Dữ Liệu (Contract Locking)
- Trước khi chỉnh sửa hoặc thêm logic, xác định rõ:
  - Input Schema: Các trường bắt buộc, kiểu dữ liệu, validation constraint.
  - Output Schema: DTO trả về, mã HTTP status code, format lỗi chuẩn (`ApiResponse<T>`).
- Tuyệt đối **không tự ý thay đổi tên trường, thêm trường tùy tiện** vào DTO hiện có trừ khi người dùng yêu cầu rõ ràng.

### Bước 2: Xác Định Harness Verification (Kiểm thử Neo Chặn)
- Xác định trước tiêu chí pass/fail cụ thể:
  - Unit Test / Component Test có sẵn nào phải PASS?
  - Payload test mẫu (Mock payload) là gì?
  - Dòng log nào hoặc kết quả trả về nào chứng minh tính năng hoạt động đúng?

### Bước 3: Triển Khai Logic Tối Giản
- Viết mã vừa đủ để thỏa mãn Harness Constraints.
- Không viết code "dự phòng cho tương lai" (No premature generalization).
- Không import thêm thư viện thứ 3 khi hệ thống đã có sẵn thư viện tương đương (VD: đã có Jackson thì không tự thêm Gson; đã có Axios thì không tự thêm fetch bọc ngoài).

### Bước 4: Kiểm Chứng Khép Kín (Closed-loop Verification)
- Chạy lệnh build / test / lint của module bị ảnh hưởng.
- So sánh kết quả thực tế với Harness Contract ban đầu.
- Nếu có sai lệch: chỉ sửa logic bên trong, không nới lỏng bài test hay sửa contract để che giấu lỗi.
