# Rule 03: Tổ Chức Code, Giới Hạn Độ Dài & Phân Rã Component

## 1. Giới Hạn Độ Dài File (< 400 Dòng)
- **Quy tắc cứng**: Mỗi file Kotlin tuyệt đối **không được vượt quá 400 dòng**.
- Nếu một file có xu hướng vượt quá 350 dòng, phải chủ động phân tách trước khi chạm trần 400 dòng.

---

## 2. Chiến Lược Phân Rã Composable (Decomposition Strategy)
Khi một màn hình (Screen) có nhiều sections hoặc components độc lập:
1. **Screen Level (`*Screen.kt`)**: 
   - Chỉ giữ vai trò Scaffold, TopAppBar, BottomBar, lắng nghe `UiState` và điều phối luồng tổng thể.
   - Giữ độ dài ngắn gọn (thường dưới 150-200 dòng).
2. **Section Level (`*Sections.kt` hoặc `*Components.kt`)**: 
   - Tách các khối logic/UI lớn thành file riêng trong cùng package presentation.
   - Ví dụ: `LoginHeaderSection.kt`, `LoginFormSection.kt`, `RegisterDriverDocumentsSection.kt`.
3. **Sub-components Level**:
   - Nếu component chỉ dùng nội bộ cho 1 screen -> đặt trong package `presentation/ui/{screen_name}/components/`.
   - Nếu component tái sử dụng trên nhiều features (như `AppButton`, `AppTextField`, `AppDialog`) -> chuyển vào `core/ui/components/` hoặc `common/presentation/components/`.

---

## 3. Nguyên Tắc Tránh Over-Engineering
- **Tránh Trùng Lặp (DRY)**: Các component lặp lại từ 2 nơi trở lên phải được trừu tượng hóa và tái sử dụng.
- **Không Tạo Abstraction Thừa**: Nếu một component chỉ dùng đúng 1 lần và logic không quá phức tạp, KHÔNG cố tình tạo abstraction chung nếu việc đó làm tăng độ phức tạp không cần thiết (KISS principle).
- Giữ tên gọi component rõ ràng, tường minh, phản ánh đúng chức năng hiển thị.
