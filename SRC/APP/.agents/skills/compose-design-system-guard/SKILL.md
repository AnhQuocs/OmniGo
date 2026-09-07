---
name: compose-design-system-guard
description: Kỹ năng rà soát, kiểm tra và cưỡng chế tuân thủ 100% Tokenized Design System (Dimen.kt, AppSpacing.kt, AppShape.kt, Color.kt, TypographyExt), đa ngôn ngữ i18n trong cả 3 file strings.xml, và giới hạn độ dài file < 400 dòng.
---

# Compose Design System Guard Skill

Kỹ năng này chịu trách nhiệm kiểm tra toàn diện chất lượng mã nguồn Jetpack Compose trước khi commit hoặc hoàn thành bất kỳ task UI nào.

## 1. Danh Sách Kiểm Tra Bắt Buộc (Audit Checklist)

### 1.1. Internationalization (i18n) Verification
Mọi string resource sử dụng trong Composable phải xuất hiện đồng thời trong 3 file:
- [ ] `app/src/main/res/values/strings.xml` (Default)
- [ ] `app/src/main/res/values-en/strings.xml` (English)
- [ ] `app/src/main/res/values-vi/strings.xml` (Vietnamese)
- [ ] Không chứa raw text dạng `Text("Hello")` hay `Text(text = "Đăng nhập")`.

### 1.2. Design System Tokens Verification
- [ ] **Spacers**: Không dùng `Modifier.height(16.dp)` hay `Modifier.width(8.dp)`. Phải dùng `AppSpacing.*` (ví dụ: `AppSpacing.MediumLarge`, `AppSpacing.S`).
- [ ] **Padding / Size / Height**: Phải dùng `Dimen.*` (ví dụ: `Dimen.PaddingXXS`, `Dimen.PaddingM`).
- [ ] **Shapes**: Phải dùng `AppShape.*` (ví dụ: `AppShape.ShapeS`, `AppShape.ShapeL`).
- [ ] **Colors**: Không dùng `Color(0xFF...)` hoặc mã hex. Phải map qua `MaterialTheme.colorScheme` hoặc hằng số trong `Color.kt`.
- [ ] **Typography**: Dùng `MaterialTheme.typography.*` hoặc extensions trong `TypographyExt`.

### 1.3. File Size & Separation Verification
- [ ] Không có file Kotlin nào vượt quá **400 dòng**.
- [ ] Các màn hình phức tạp phải được bóc tách thành `*Screen.kt`, `*Sections.kt`, `*Components.kt`.
- [ ] Không có business logic hay repository/API call trực tiếp trong Composable.

---

## 2. Quy Trình Refactor Khi Phát Hiện Vi Phạm

1. **Nếu phát hiện Hardcoded String**:
   - Trích xuất key resource theo format: `{feature}_{screen}_{element}`.
   - Thêm bản dịch tương ứng vào cả 3 file `strings.xml`.
   - Thay thế bằng `stringResource(id = R.string.{key})`.

2. **Nếu phát hiện Magic Dp / Color**:
   - Kiểm tra xem token đã có trong `Dimen.kt` / `AppSpacing.kt` / `Color.kt` chưa.
   - Nếu chưa có và là giá trị chuẩn của Design System -> Bổ sung vào file token tương ứng.
   - Thay thế magic number bằng token name.

3. **Nếu phát hiện File > 400 lines**:
   - Xác định các cụm Composable độc lập (Header, Form, List, BottomAction).
   - Tách thành các file `*Sections.kt` hoặc sub-components cùng package.
   - Giữ nguyên toàn bộ parameters, state binding và callbacks.
