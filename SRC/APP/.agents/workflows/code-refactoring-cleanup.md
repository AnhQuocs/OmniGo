# Workflow: Refactor & Dọn Dẹp Chuẩn Hóa Code (Code Refactoring & Cleanup)

Quy trình chuẩn hóa và dọn dẹp các vi phạm convention trong mã nguồn Android Jetpack Compose mà không làm ảnh hưởng tới behavior hiện tại.

---

## 1. Mục Tiêu Refactor
- Phân tách các file Kotlin vượt quá ngưỡng 400 dòng thành các file nhỏ gọn, đơn trách nhiệm.
- Loại bỏ triệt để hardcoded text, magic numbers (dp/sp), hardcoded colors (`Color(...)`), và raw shapes.
- Bổ sung đồng bộ resource string vào cả 3 file `strings.xml`.

---

## 2. Quy Trình 4 Bước Thực Hiện

### Bước 1: Quét & Khoanh Vùng Vi Phạm (Scan & Identify)
1. Kiểm tra số dòng của file mục tiêu. Nếu `totalLines > 350`, chuẩn bị kế hoạch phân tách.
2. Quét tìm:
   - Các chuỗi raw text dạng `Text("...")` hoặc chuỗi nối trong Composable.
   - Các giá trị `.dp`, `.sp` viết trực tiếp (không qua `Dimen.*` hay `AppSpacing.*`).
   - Các khai báo `Color(0xFF...)` hoặc `RoundedCornerShape(...)` rải rác.

### Bước 2: Chuẩn Hóa Resources & Design Tokens
1. Thêm key string còn thiếu vào đồng thời 3 file:
   - `app/src/main/res/values/strings.xml`
   - `app/src/main/res/values-en/strings.xml`
   - `app/src/main/res/values-vi/strings.xml`
2. Nếu kích thước/màu sắc chưa có trong Design System, bổ sung vào `Dimen.kt`, `AppSpacing.kt` hoặc `Color.kt` theo đúng quy ước đặt tên.

### Bước 3: Phân Tách Composable & Phẫu Thuật Code (Surgical Decomposition)
1. Tách các cụm giao diện thành các file độc lập (`*Sections.kt`, `*Components.kt`).
2. Giữ nguyên toàn bộ State, Parameters và Callbacks của Composable gốc để đảm bảo tương thích ngược 100%.
3. Đảm bảo sau khi phân tách, mỗi file mới đều có độ dài `< 300 dòng`.

### Bước 4: Kiểm Thử & Xác Nhận (Verification)
1. Đảm bảo toàn bộ import được resolve chính xác.
2. Kiểm tra preview hoặc build app không phát sinh lỗi biên dịch.
3. Xác nhận giao diện và hành vi tương tác hoạt động hoàn toàn giống như trước khi refactor.
