# Workflow: Phát Triển Tính Năng Mới Theo Phương Pháp Harness

Tài liệu này hướng dẫn quy trình 4 giai đoạn bắt buộc khi AI tiếp nhận yêu cầu phát triển hoặc mở rộng tính năng mới trong dự án OmniGo.

---

## Giai Đoạn 1: Phân Tích & Xác Lập Khung Harness (Spec & Harness Setup)

1. **Hiểu rõ yêu cầu (Scope Identification)**:
   - Xác định chính xác mục tiêu tối thiểu cần đạt được.
   - Liệt kê các file dự kiến cần thêm mới hoặc chỉnh sửa.
   - Lập danh sách những gì **KHÔNG LÀM** (Out of scope) để tránh lan man.
2. **Khóa Hợp Đồng (Contract Definition)**:
   - Định nghĩa DTO request / response hoặc schema API.
   - Thống nhất URL endpoint, HTTP method và mã phản hồi.
3. **Lập Kế Hoạch Kiểm Thử (Verification Plan)**:
   - Chuẩn bị payload kiểm thử mẫu.
   - Xác định lệnh build / test sẽ chạy để kiểm chứng sau khi hoàn thành.

---

## Giai Đoạn 2: Thực Thi Tối Giản (Surgical Implementation)

1. **Thêm / Cập nhật DTO & Interface trước**:
   - Khai báo các field cần thiết theo đúng contract đã định.
2. **Triển khai Logic Tầng Dưới lên Tầng Trên (Bottom-up)**:
   - Database / Repository (nếu có schema mới).
   - Service Layer: Cài đặt logic nghiệp vụ cốt lõi, bắt exception.
   - Controller / Route Layer: Map endpoint và validate DTO.
   - Frontend UI: Tạo component hoặc tích hợp API service.
3. **Tuân thủ quy tắc Không Code Thừa**:
   - Không tự động sinh code generic thừa.
   - Không tự ý thêm helper utility nếu chỉ dùng 1 lần và logic dưới 3 dòng.

---

## Giai Đoạn 3: Kiểm Chứng Khép Kín (Harness Verification)

1. **Thực thi Test Suite**:
   - Chạy Gradle build / Maven build hoặc NPM build để kiểm tra lỗi biên dịch (compile error).
   ```bash
   # Ví dụ kiểm tra BE module:
   ./gradlew :SRC:BE:<service-name>:build -x test
   # Ví dụ kiểm tra FE:
   cd SRC/FE && npm run build
   ```
2. **Kiểm tra Lint & Formatting**:
   - Đảm bảo không vi phạm linter rules và syntax.
3. **Thực hiện kiểm thử hợp đồng (Contract Assertion)**:
   - Xác nhận output trả về khớp 100% với DTO đã cam kết ở Giai đoạn 1.

---

## Giai Đoạn 4: Bàn Giao & Kiểm Tra Diff (Diff Review & Handoff)

1. **Rà soát `git status` / `git diff`**:
   - Đảm bảo chỉ có các file trong kế hoạch bị thay đổi.
   - Đảm bảo không còn file rác, log thừa, hay comment bị xóa mất tích.
2. **Báo cáo kết quả**:
   - Tóm tắt ngắn gọn các file đã can thiệp.
   - Cung cấp hướng dẫn cho người dùng kiểm thử nhanh (cURL, payload mẫu hoặc thao tác UI).
