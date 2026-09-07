---
trigger: always_on
---

# Rule 01: Chuẩn Kiến Trúc Clean Architecture & MVVM

## 1. Tech Stack Bắt Buộc
- **Language**: Kotlin 100%
- **UI Framework**: Jetpack Compose + Material 3
- **Architecture Pattern**: MVVM + Clean Architecture + Repository Pattern + Use Cases
- **Dependency Injection**: Hilt (`@HiltViewModel`, `@Inject`, `@Module`, `@InstallIn`)
- **Async & Concurrency**: Kotlin Coroutines + Flow (`StateFlow`, `SharedFlow`, `Channel`)

---

## 2. Phân Tầng Clean Architecture

### 2.1. Domain Layer (`features/{feature_name}/domain/`)
- **Mục tiêu**: Chứa toàn bộ core business logic của feature, độc lập 100% với Android Framework và Data Layer.
- **Thành phần**:
  - `model/`: Các Domain Entities thuần Kotlin (data class, enum).
  - `repository/`: Repository Interfaces định nghĩa contracts (ví dụ `AuthRepository.kt`).
  - `usecase/`: Các Use Case thực hiện 1 nghiệp vụ duy nhất (Single Responsibility), có method `operator fun invoke(...)`.

### 2.2. Data Layer (`features/{feature_name}/data/`)
- **Mục tiêu**: Xử lý việc lấy, lưu và chuyển đổi dữ liệu từ Remote (Retrofit API) hoặc Local (DataStore / Room).
- **Thành phần**:
  - `remote/api/`: Retrofit API Interfaces.
  - `remote/dto/`: DTO request/response có annotations serialization (`@SerializedName`).
  - `repository/`: Implementation của Repository Interface từ Domain Layer.
  - `mapper/`: Mappers chuyển đổi qua lại giữa DTO và Domain Entities.

### 2.3. Presentation Layer (`features/{feature_name}/presentation/`)
- **Mục tiêu**: Quản lý UI state, user interactions và render giao diện Jetpack Compose.
- **Thành phần**:
  - `viewmodel/`: ViewModel kế thừa `ViewModel()` với `@HiltViewModel`.
  - `state/`: `UiState` (data class bất biến) và `UiEvent` (one-shot events qua `Channel`/`SharedFlow`).
  - `ui/`: Các Composable screens và sub-components.

---

## 3. Quy Tắc Bất Biến Về Luồng Dữ Liệu
1. **Không đưa Business Logic vào Composable**: Composable chỉ nhận State và bắn Callbacks/Events lên ViewModel. Mọi tính toán logic, validate, gọi API phải nằm ở ViewModel / UseCase.
2. **Không đưa Data/Domain Models chưa qua xử lý vào UI nếu gây leak boundary**: Presentation chỉ giao tiếp với Domain qua UseCase, không gọi trực tiếp Data Sources / API / DTOs.
3. **Quản lý State một chiều (UDF - Unidirectional Data Flow)**:
   - ViewModel expose `StateFlow<UiState>`.
   - UI chỉ đọc `state.collectAsStateWithLifecycle()` và gọi hàm ViewModel hoặc bắn Event.
