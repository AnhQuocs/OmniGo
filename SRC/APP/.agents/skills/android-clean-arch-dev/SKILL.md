---
name: android-clean-arch-dev
description: Kỹ năng chuyên biệt để thiết kế, triển khai và chuẩn hóa các tầng Clean Architecture (Data, Domain, Presentation), Hilt Dependency Injection, Repository Pattern, Use Cases và StateFlow/Channel trong Android Kotlin.
---

# Android Clean Architecture Development Skill

Kỹ năng này hướng dẫn quy trình tạo và hoàn thiện một Feature theo chuẩn Clean Architecture + MVVM trong dự án Android.

## 1. Cấu Trúc Thư Mục Chuẩn Cho Mỗi Feature

```
features/{feature_name}/
├── data/
│   ├── local/               # Room DAO, DataStore preferences
│   ├── remote/
│   │   ├── api/             # Retrofit API interface
│   │   └── dto/             # Request & Response DTOs
│   ├── mapper/              # Extension functions: DTO <-> Domain Model
│   └── repository/          # Implementation của Domain Repository
├── domain/
│   ├── model/               # Pure Kotlin Domain Entities
│   ├── repository/          # Repository Interfaces (contracts)
│   └── usecase/             # Single-responsibility UseCases
└── presentation/
    ├── state/               # UiState (data class) & UiEvent (sealed interface)
    ├── viewmodel/           # @HiltViewModel class
    └── ui/                  # Compose screens, sections & sub-components
```

---

## 2. Quy Chuẩn Từng Tầng

### 2.1. Domain Use Case
- Mỗi Use Case đại diện cho 1 nghiệp vụ cụ thể.
- Phải dùng `operator fun invoke(...)` để gọi như function.
- Phụ thuộc duy nhất vào Domain Repository interface qua Hilt `@Inject`.

```kotlin
class GetUserProfileUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<User> {
        return authRepository.getProfile()
    }
}
```

### 2.2. Presentation ViewModel & State Management
- Kế thừa `ViewModel()` với `@HiltViewModel` và `@Inject constructor`.
- Dùng `MutableStateFlow` (private) + `StateFlow` (public asStateFlow) cho `UiState`.
- Dùng `Channel<UiEvent>(Channel.BUFFERED)` + `receiveAsFlow()` cho one-shot events (hiển thị Toast, Navigate, Dialog).

```kotlin
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<LoginUiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()
    
    // ...
}
```

### 2.3. Hilt DI Module
- Đặt tại `core/di/` hoặc `{feature}/di/`.
- Dùng `@Binds` cho Repository Implementation -> Repository Interface.
- Dùng `@Provides` cho Retrofit API service / DataStore instance.
