---
name: android-unit-test-guide
description: Kỹ năng chuyên sâu để thiết kế, triển khai và bảo trì Unit Test cho Android Kotlin (MockK, Turbine, kotlinx-coroutines-test) bao phủ Use Case, ViewModel StateFlow/Channel, Repository và Mappers.
---

# Android Unit Testing Guide & Patterns

Kỹ năng này cung cấp các templates và best practices chuẩn để viết Unit Test thực chất cho dự án Kotlin Clean Architecture.

---

## 1. Nguyên Tắc Cốt Lõi: Chống "Test Hình Thức" (Anti-Vanity Testing)
- **Không viết test chỉ để lấy số % coverage**: Mọi test case phải kiểm tra **hành vi thực tế (Actual Behavior)** của mã nguồn.
- **Tiêu chuẩn Fail-on-Mutation**: Test phải viết sao cho nếu logic nghiệp vụ bên trong code bị đổi sai (ví dụ: đổi dấu `+` thành `-`, bỏ qua điều kiện `if`, không cập nhật state), bài test **BẮT BUỘC PHẢI FAIL**.
- **Test Behavior thay vì Implementation Details**: Không gò bó vào từng private method hay bước phụ; tập trung kiểm chứng contract Input $\rightarrow$ Output để khi refactor code không bị sửa test thừa thãi.
- **Data Layer Phân Loại Rõ Ràng**:
  - **Cần Test**: `RepositoryImpl`, `Mapper`, Caching, Error Handlers.
  - **Không Test**: DTOs thuần, Retrofit Interfaces, Room DAO interfaces (boilerplate).

---

## 2. Test Rule Cho Coroutines (`MainDispatcherRule`)

Dùng để thay thế `Dispatchers.Main` trong môi trường JVM Unit Test.

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
```

---

## 3. Template Test Cho Mapper (Data Layer)

```kotlin
class UserMapperTest {

    @Test
    fun `toDomain with complete DTO maps all fields accurately`() {
        // Given
        val dto = UserDto(
            id = "user_123",
            email = "test@example.com",
            fullName = "Nguyen Van A",
            role = "CUSTOMER",
            phoneNumber = "0987654321",
            status = "ACTIVE"
        )

        // When
        val domain = dto.toDomain()

        // Then - Kiểm tra chính xác từng field quan trọng
        assertEquals("user_123", domain.id)
        assertEquals("test@example.com", domain.email)
        assertEquals("Nguyen Van A", domain.fullName)
        assertEquals(UserRole.CUSTOMER, domain.role)
        assertEquals(UserStatus.ACTIVE, domain.status)
    }

    @Test
    fun `toDomain with null or missing optional fields uses sensible defaults`() {
        // Given
        val dto = UserDto(
            id = "user_456",
            email = "test@example.com",
            fullName = null,
            role = null,
            phoneNumber = null,
            status = null
        )

        // When
        val domain = dto.toDomain()

        // Then - Đảm bảo fallback default values an toàn, không crash
        assertEquals("user_456", domain.id)
        assertEquals("", domain.fullName)
        assertEquals(UserRole.CUSTOMER, domain.role)
        assertEquals(UserStatus.PENDING, domain.status)
    }
}
```

---

## 4. Template Test Cho Repository Implementation (Data Layer)

```kotlin
class AuthRepositoryImplTest {

    private lateinit var authApi: AuthApi
    private lateinit var tokenManager: TokenManager
    private lateinit var authRepository: AuthRepository

    @Before
    fun setUp() {
        authApi = mockk()
        tokenManager = mockk(relaxed = true)
        authRepository = AuthRepositoryImpl(authApi, tokenManager)
    }

    @Test
    fun `login with successful response saves token and returns domain user`() = runTest {
        // Given
        val request = LoginRequest("test@example.com", "Password123!")
        val apiResponse = ApiResponse(
            code = 200,
            message = "Success",
            data = LoginResponse(
                token = "jwt_token_xyz",
                user = UserResponse(id = "1", email = "test@example.com", name = "Test User")
            )
        )
        coEvery { authApi.login(request) } returns Response.success(apiResponse)

        // When
        val result = authRepository.login("test@example.com", "Password123!")

        // Then
        assertTrue(result.isSuccess)
        assertEquals("1", result.getOrNull()?.id)
        coVerify(exactly = 1) { tokenManager.saveAccessToken("jwt_token_xyz") }
    }

    @Test
    fun `login with HTTP 401 error returns failure with mapped message`() = runTest {
        // Given
        val errorResponseBody = "{\"message\":\"Invalid credentials\"}"
            .toResponseBody("application/json".toMediaTypeOrNull())
        coEvery { authApi.login(any()) } returns Response.error(401, errorResponseBody)

        // When
        val result = authRepository.login("test@example.com", "WrongPassword")

        // Then
        assertTrue(result.isFailure)
        coVerify(exactly = 0) { tokenManager.saveAccessToken(any()) }
    }
}
```

---

## 5. Template Test Cho Use Case (Domain Layer)

```kotlin
class LoginUseCaseTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var loginUseCase: LoginUseCase

    @Before
    fun setUp() {
        authRepository = mockk()
        loginUseCase = LoginUseCase(authRepository)
    }

    @Test
    fun `invoke with valid credentials returns success`() = runTest {
        val email = "test@example.com"
        val password = "Password123!"
        val expectedUser = AuthUser(id = "1", email = email, name = "Test User")
        coEvery { authRepository.login(email, password) } returns Result.success(expectedUser)

        val result = loginUseCase(email, password)

        assertTrue(result.isSuccess)
        assertEquals(expectedUser, result.getOrNull())
        coVerify(exactly = 1) { authRepository.login(email, password) }
    }
}
```

---

## 6. Template Test Cho ViewModel (Presentation Layer - Turbine)

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var loginUseCase: LoginUseCase
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        loginUseCase = mockk()
        viewModel = LoginViewModel(loginUseCase)
    }

    @Test
    fun `onLoginClicked updates loading state and navigates to home on success`() = runTest {
        val user = AuthUser(id = "1", email = "test@example.com", name = "Test User")
        coEvery { loginUseCase(any(), any()) } returns Result.success(user)

        viewModel.uiEvent.test {
            viewModel.onEmailChanged("test@example.com")
            viewModel.onPasswordChanged("Password123!")
            viewModel.onLoginClicked()

            val finalState = viewModel.uiState.value
            assertFalse(finalState.isLoading)
            assertNull(finalState.errorMessage)

            val event = awaitItem()
            assertTrue(event is LoginUiEvent.NavigateToHome)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
```
