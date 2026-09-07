---
trigger: always_on
---

# Rule 02: Quy Ước UI/UX & Design System Tokens

## 1. Đa Ngôn Ngữ Bắt Buộc (i18n - 3 Files strings.xml)
- **Quy tắc tuyệt đối**: KHÔNG hardcode chuỗi text hiển thị trực tiếp trong bất kỳ file Composable nào.
- **Bắt buộc khai báo đồng thời ở cả 3 file**:
  1. `app/src/main/res/values/strings.xml` (Default / Tiếng Anh)
  2. `app/src/main/res/values-en/strings.xml` (Tiếng Anh)
  3. `app/src/main/res/values-vi/strings.xml` (Tiếng Việt)
- **Cách sử dụng trong Compose**:
  ```kotlin
  Text(text = stringResource(id = R.string.auth_login_title))
  ```
- **Quy ước đặt tên key resource**: `{feature}_{screen/section}_{action/label}` (ví dụ: `auth_login_btn_submit`, `home_service_ride`).

---

## 2. Tokenized Design System (Tuyệt Đối Không Magic Numbers)

Mọi thông số kích thước, màu sắc, khoảng cách, bo góc và typography phải lấy từ hệ thống Design System chuẩn:

### 2.1. Spacing & Spacer (`AppSpacing.kt`)
- Đường dẫn: `com.example.omnigo.ui.dimens.AppSpacing`
- Sử dụng các hằng số spacing đã được định nghĩa sẵn trong `AppSpacing.kt`. Không hardcode giá trị `dp` trực tiếp trong UI.
- Các hằng số spacing hiện có:
  - `AppSpacing.XXS` = 2.dp
  - `AppSpacing.XS` = 4.dp
  - `AppSpacing.XSPlus` = 6.dp
  - `AppSpacing.S` = 8.dp
  - `AppSpacing.SPlus` = 10.dp
  - `AppSpacing.M` = 12.dp
  - `AppSpacing.MPlus` = 14.dp
  - `AppSpacing.MediumLarge` = 16.dp
  - `AppSpacing.L` = 24.dp
  - `AppSpacing.LPlus` = 28.dp
  - `AppSpacing.XL` = 32.dp
  - `AppSpacing.XLPlus` = 36.dp
  - `AppSpacing.XXL` = 48.dp
  - `AppSpacing.XXLPlus` = 50.dp
  - `AppSpacing.Ultra` = 68.dp
- Khi cần spacing, hãy lựa chọn hằng số có giá trị phù hợp nhất thay vì tạo thêm giá trị mới hoặc hardcode `dp`.
- **Ví dụ:**
  ```kotlin
  Spacer(modifier = Modifier.height(AppSpacing.M))
  ```

### 2.2. Kích Thước, Padding & Size (`Dimen.kt`)
- **Đường dẫn:** `com.example.omnigo.ui.dimens.Dimen`
- Tất cả các giá trị liên quan đến `padding`, `margin`, `width`, `height`, `size`, `icon size`, `stroke width` và các kích thước UI khác phải sử dụng token đã được định nghĩa trong `Dimen.kt`.
- Các token hiện có được phân nhóm như sau:
  - **Padding:** `Dimen.PaddingXXS`, `Dimen.PaddingXS`, `Dimen.PaddingXSPlus`, `Dimen.PaddingS`, `Dimen.PaddingSM`, `Dimen.PaddingM`, `Dimen.PaddingML`, `Dimen.PaddingL`, `Dimen.PaddingXL`, `Dimen.PaddingXXL`, `Dimen.PaddingUltra`.
  - **Size:** `Dimen.SizeS`, `Dimen.SizeSM`, `Dimen.SizeM`, `Dimen.SizeML`, `Dimen.SizeL`, `Dimen.SizeXL`, `Dimen.SizeXLPlus`, `Dimen.SizeXXL`, `Dimen.SizeXXLPlus`, `Dimen.SizeMega`, `Dimen.SizeUltra`.
  - **Height:** `Dimen.HeightDefault`.
- Không hardcode các giá trị `dp` trực tiếp trong UI, ví dụ `16.dp`, `24.dp`, `48.dp`, nếu đã có token tương ứng trong `Dimen.kt`.
- Nếu cần một kích thước chưa có token phù hợp, ưu tiên bổ sung token vào `Dimen.kt` thay vì hardcode trực tiếp trong UI.
- Không tạo thêm file hoặc hệ thống dimension khác nếu `Dimen.kt` đã đáp ứng nhu cầu.

### 2.3. Bo Góc & Shape (`AppShape.kt`)
- **Đường dẫn:** `com.example.omnigo.ui.dimens.AppShape`
- Các giá trị bo góc của UI phải sử dụng token được định nghĩa trong `AppShape.kt`.
- Các token shape hiện có:
  - `AppShape.ShapeXXS`
  - `AppShape.ShapeXS`
  - `AppShape.ShapeS`
  - `AppShape.ShapeM`
  - `AppShape.ShapeL`
  - `AppShape.ShapeXL`
  - `AppShape.ShapeXL2`
  - `AppShape.ShapeXXL`
- Không hardcode các giá trị `RoundedCornerShape(x.dp)` hoặc giá trị `dp` tương đương trực tiếp trong UI nếu đã có token phù hợp trong `AppShape.kt`.
- Nếu cần một giá trị bo góc chưa tồn tại, ưu tiên bổ sung token vào `AppShape.kt` thay vì hardcode trong UI.
- **Ví dụ:**
  ```kotlin
  Surface(
      shape = RoundedCornerShape(AppShape.ShapeM)
  ) {
      // ...
  }
  ```

### 2.4. Typography (`TypographyExt` / `Type.kt`)
- **Đường dẫn:**
  - `com.example.omnigo.ui.theme.Type` đối với `Type.kt` / Material Typography.
  - `com.example.omnigo.utils.TypographyExt` đối với các extension của `TypographyExt`.
- Ưu tiên sử dụng `MaterialTheme.typography` hoặc các extension typography đã được định nghĩa sẵn trong `TypographyExt`.
- Các typography size hiện có:
  - `MaterialTheme.typography.s10`
  - `MaterialTheme.typography.s12`
  - `MaterialTheme.typography.s13`
  - `MaterialTheme.typography.s14`
  - `MaterialTheme.typography.s15`
  - `MaterialTheme.typography.s16`
  - `MaterialTheme.typography.s18`
  - `MaterialTheme.typography.s20`
  - `MaterialTheme.typography.s22`
  - `MaterialTheme.typography.s24`
  - `MaterialTheme.typography.s28`
  - `MaterialTheme.typography.s32`
- Các extension font weight hiện có:
  - `.bold()`
  - `.semiBold()`
  - `.medium()`
  - `.normal()`
  - `.light()`
- Các extension style/decoration hiện có:
  - `.italic()`
  - `.underline()`
  - `.lineThrough()`
- Có thể kết hợp các extension để tạo style phù hợp. Ví dụ:
  `style = MaterialTheme.typography.s16.semiBold().italic()`
- **Tuyệt đối không** hardcode `fontSize`, `lineHeight`, `fontWeight`, `fontStyle` hoặc `textDecoration` trực tiếp trong các UI component nếu đã có typography token hoặc extension tương ứng.
- Không tự tạo `TextStyle` riêng trong từng screen/component nếu typography hiện tại đã đáp ứng nhu cầu.
- Nếu cần một typography chưa tồn tại, ưu tiên bổ sung hoặc mở rộng `TypographyExt` theo convention hiện tại thay vì hardcode trực tiếp trong UI.

### 2.5. Màu Sắc & Palette (`Color.kt`)
- Đường dẫn: `com.example.omnigo.ui.theme.Color`
- Tất cả màu sắc trong UI phải được định danh trong `Color.kt` và map qua `MaterialTheme.colorScheme` hoặc truy xuất từ theme tokens.
- **Tuyệt đối không**: Hardcode `Color(0xFF...)` hoặc hex string trong các file UI.

---

## 3. Responsive UI & Thiết Bị Đa Kích Thước
- Không hardcode các giá trị pixel cố định phụ thuộc vào 1 kích thước màn hình nhất định (gây vỡ layout trên tablet hoặc màn hình nhỏ).
- Sử dụng các cơ chế linh hoạt của Compose: `weight()`, `fillMaxWidth()`, `Arrangement.SpaceBetween`, `BoxWithConstraints`, `Modifier.aspectRatio()`.
