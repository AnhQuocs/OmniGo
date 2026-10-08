# Workflow: Review Mã Nguồn & Nghiệm Thu (Code Review & Verification)

Quy trình tự kiểm tra (Self-Check) mà AI phải thực hiện trước khi công bố hoàn thành nhiệm vụ.

---

## 1. Danh Sách Kiểm Tra Ranh Giới (Boundary Checklist)

Trước khi kết thúc lượt phản hồi, AI phải tự trả lời các câu hỏi sau:

- [ ] **Đúng phạm vi?**: Toàn bộ các dòng code mới hoặc sửa đổi có trực tiếp phục vụ yêu cầu của người dùng không?
- [ ] **Không code thừa?**: Có bất kỳ file/hàm/biến nào được tạo ra mà không được sử dụng ở đâu không?
- [ ] **Bảo toàn hiện trạng?**: Các comment cũ, format code gốc, và các hàm không liên quan có được giữ nguyên 100% không?
- [ ] **Không gây lỗi hồi quy (Zero Regression)?**: Các tính năng cũ, hàm dùng chung hoặc logic lân cận có tiếp tục hoạt động bình thường, không phát sinh lỗi mới ở nơi khác không?
- [ ] **Không lộ Secret?**: Có token, API key, credential nào bị dán cứng vào mã nguồn không?

---

## 2. Kiểm Tra Biên Dịch & Build (Compile & Build Integrity)

*   **Với Backend (Spring Boot)**:
    - Kiểm tra imports: Không để sót unused imports hoặc import sai package.
    - Đảm bảo JPA Entities và DTO mapping chính xác.
*   **Với Frontend (Vite / React)**:
    - Kiểm tra JSX tags đóng mở hợp lệ.
    - Đảm bảo imports path (`import ... from '...'`) trỏ đúng file.

---

## 3. Báo Cáo & Hướng Dẫn Nghiệm Thu (Handoff Report)

Sau khi hoàn thành tác vụ, báo cáo cho người dùng theo cấu trúc ngắn gọn:
1. **Các file đã tác động**: Liệt kê kèm liên kết Markdown.
2. **Tóm tắt thay đổi**: Giải thích ngắn gọn tại sao thay đổi như vậy.
3. **Cách thức kiểm thử (Test instructions)**: Cung cấp lệnh curl hoặc các bước click trên giao diện để người dùng tự xác nhận.
