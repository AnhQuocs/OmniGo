# Rule 04: Hành Vi Phát Triển & An Toàn Mã Nguồn

## 1. Kiểm Tra Trước Khi Code (Check-Before-Code)
- **Tái sử dụng tài nguyên**: Trước khi viết một component, utility, helper hay định nghĩa thêm resource mới, Agent BẮT BUỘC phải tìm kiếm trong project xem đã có sẵn component/token tương tự hay chưa (`Dimen.kt`, `AppSpacing.kt`, `AppShape.kt`, `Color.kt`, `Type.kt`, `core/ui/components/`).
- Nếu đã có -> Bắt buộc tái sử dụng, không viết lại cái thứ hai.

---

## 2. Nhất Quán Convention (Convention Consistency)
- Khi có nhiều cách triển khai một bài toán (ví dụ: cách inject Hilt, cách định nghĩa `UiState`, cách xử lý Event qua Channel, cách viết Mapper):
  👉 **Luôn ưu tiên cách viết đồng nhất với các file hiện hữu trong codebase**.
- Không tự ý tạo ra style code mới hoặc áp dụng convention lạ nếu project đã có convention chuẩn.

---

## 3. Phẫu Thuật Code Chính Xác & Refactor Cơ Hội (Surgical & Safe Refactoring)
1. **Surgical Editing**: Khi sửa lỗi hoặc thêm tính năng, chỉ chỉnh sửa số lượng dòng code tối thiểu cần thiết. Giữ nguyên formatting, comments và cấu trúc code xung quanh.
2. **Opportunistic Safe Refactor**:
   - Khi phát hiện code trong phạm vi đang chỉnh sửa vi phạm các rule (như hardcode string, hardcode dp, hardcode Color, file vượt quá 400 dòng), Agent chủ động đề xuất hoặc thực hiện refactor nhẹ nhàng.
   - **Điều kiện tiên quyết**: Refactor tuyệt đối không được làm thay đổi behavior/chức năng đang hoạt động bình thường của ứng dụng.
