# Workflow: Phát Triển Tính Năng Mới (Feature Implementation)

Quy trình chuẩn hóa 5 bước để phát triển một tính năng mới trong dự án Android Kotlin Clean Architecture.

---

## Bước 1: Khảo Sát Tài Nguyên & Convention Hiện Có (Check-Before-Code)
1. Kiểm tra các component dùng chung hiện có trong `core/ui/components/` (ví dụ: `AppButton`, `AppTextField`, `AppDialog`).
2. Kiểm tra các Design Tokens trong `ui/dimens/` (`Dimen.kt`, `AppSpacing.kt`, `AppShape.kt`) và `ui/theme/` (`Color.kt`, `Type.kt`).
3. Xác định các keys string cần dùng cho cả 2 ngôn ngữ (En/Vi).

---

## Bước 2: Thiết Kế Domain Layer (Core Business)
1. Định nghĩa các Domain Models / Entities trong `features/{feature}/domain/model/`.
2. Định nghĩa Repository Interface trong `features/{feature}/domain/repository/`.
3. Tạo các UseCase đơn nhiệm vụ trong `features/{feature}/domain/usecase/` với `operator fun invoke(...)`.

---

## Bước 3: Triển Khai Data Layer (Remote / Local)
1. Khai báo endpoint trong `ApiEndpoints.kt` và interface Retrofit trong `features/{feature}/data/remote/api/`.
2. Tạo DTOs (Request / Response) trong `features/{feature}/data/remote/dto/`.
3. Viết Mapper chuyển đổi DTO sang Domain Model.
4. Triển khai Repository Implementation trong `features/{feature}/data/repository/` và bind qua Hilt DI Module.

---

## Bước 4: Xây Dựng Presentation Layer (MVVM & Compose)
1. Định nghĩa `UiState` và `UiEvent` trong `features/{feature}/presentation/state/`.
2. Tạo `@HiltViewModel` xử lý tương tác qua UseCases, cập nhật `StateFlow<UiState>` và bắn `Channel<UiEvent>`.
3. Dựng giao diện Jetpack Compose:
   - File chính `*Screen.kt` (quản lý Scaffold, State, Events).
   - Tách các sections / sub-components thành các file riêng để đảm bảo mỗi file **dưới 400 dòng**.
   - **Bắt buộc**: 100% text gọi qua `stringResource(R.string.*)` đã khai báo ở cả 3 file `strings.xml`.
   - **Bắt buộc**: 100% Spacers dùng `AppSpacing.*`, padding/sizes dùng `Dimen.*`, shapes dùng `AppShape.*`, colors dùng `Color.kt`.

---

## Bước 5: Viết Unit Test & Nghiệm Thu (Testing & Verification)
1. **Viết Unit Test Bắt Buộc**:
   - Viết Unit Test cho tất cả các UseCases mới (Happy Path, Error, Exceptions).
   - Viết Unit Test cho ViewModel (State transitions, Events qua Turbine).
   - Viết Unit Test cho Mappers & Repository Logic.
2. **Chạy & Kiểm Tra Kết Quả Test**:
   - Chạy lệnh test: `./gradlew testDebugUnitTest`.
   - Đảm bảo 100% unit test mới và cũ đều **PASSED**.
3. **Rà Soát Hoàn Tất (Audit)**:
   - Đảm bảo không có file nào vượt quá 400 dòng.
   - Đảm bảo đầy đủ 3 file `strings.xml`.
   - Compile code và verify không có lỗi runtime/warnings.
