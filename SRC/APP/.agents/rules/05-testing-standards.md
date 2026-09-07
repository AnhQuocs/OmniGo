# Rule 05: Tiêu Chuẩn Kiểm Thử Unit Test (Testing Standards)

## 1. Nguyên Tắc Thiết Kế Dễ Kiểm Thử (Testability by Design)
- Mọi business logic phải được thiết kế để có thể unit test độc lập mà không cần khởi động Android Emulator hay Context thực tế.
- Tách biệt rõ ràng side-effects, dependencies qua Constructor Injection để dễ dàng Mock/Fake bằng MockK hoặc Fake implementations.

---

## 2. Kiểm Thử Hành Vi Thực Tế (Behavior-Driven, Zero Vanity Tests)
- **CẤM VIẾT TEST HÌNH THỨC CHỈ ĐỂ LẤY COVERAGE**: Tuyệt đối không viết test vô nghĩa (như test getter/setter rỗng, test không có `assert`, `assertTrue(true)`, hoặc mock toàn bộ mà không kiểm tra kết quả trả về).
- **Tiêu chuẩn Mutation / Behavior Check**: Mỗi bài test **BẮT BUỘC PHẢI FAIL** nếu logic nghiệp vụ bên trong code bị sửa sai, bị đảo ngược hoặc bị xóa bỏ.
- **Test Behavior thay vì Implementation Details**: Test tập trung vào Input $\rightarrow$ Output Contract. Khi refactor code bên trong mà không làm đổi behavior bên ngoài, test không được phép bị gãy một cách không cần thiết.

---

## 3. Quy Chuẩn Kiểm Thử Tầng Data Layer (Data Layer Testing)

Data Layer **không bắt buộc phải test 100% mọi class**. Chỉ viết test cho các class chứa logic hoặc hành vi có khả năng phát sinh lỗi:

### 3.1. Các Thành Phần BẮT BUỘC Test trong Data Layer
1. **Repository Implementations (`*RepositoryImpl.kt`)**:
   - Test trường hợp thành công (dữ liệu từ Remote API / Local DB trả về hợp lệ).
   - Test các trường hợp lỗi từ Data Source (Network error, HTTP 4xx/5xx, Database error).
   - Test logic Cache, Refresh Token, Fallback và các nhánh xử lý lỗi (Error Handling).
2. **Mappers (`*Mapper.kt`)**:
   - Chuyển đổi qua lại giữa DTO $\leftrightarrow$ Domain Model.
   - Bắt buộc kiểm tra: các trường dữ liệu quan trọng, `null` values, default fallback values và các trường hợp enum/type mapping đặc biệt.
3. **Data Transformations & Token/Cache Managers**:
   - Các class xử lý mã hóa, parse header, session storage, format dữ liệu thô.

### 3.2. Các Thành Phần KHÔNG CẦN Viết Unit Test
- **Pure DTOs / Request / Response data classes** không có custom logic.
- **Retrofit API Service Interfaces** (chỉ chứa annotations `@GET`, `@POST`).
- **Room DAO Interfaces** (chỉ chứa annotations `@Query`, `@Insert`).
- Các class boilerplate không chứa logic tính toán hay phân nhánh.

---

## 4. Quy Chuẩn Cho Tầng Domain & Presentation

### 4.1. Unit Test Cho Use Case
- Test với input hợp lệ $\rightarrow$ Trả về `Result.Success` hoặc Domain Entity mong đợi.
- Test với input không hợp lệ $\rightarrow$ Trả về lỗi validation hoặc `Result.Failure`.
- Test khi Repository quăng Exception hoặc trả về lỗi $\rightarrow$ Xử lý mapping lỗi đúng chuẩn.

### 4.2. Unit Test Cho ViewModel
- Sử dụng `MainDispatcherRule` (với `StandardTestDispatcher` hoặc `UnconfinedTestDispatcher`) để test Coroutines.
- Sử dụng thư viện `Turbine` để lắng nghe và verify các emission của `StateFlow<UiState>` và `Channel/SharedFlow<UiEvent>`.
- Test State Transition:
  - Trạng thái ban đầu (`Initial / Idle`).
  - Trạng thái đang tải (`Loading`).
  - Trạng thái thành công (`Success`) kèm dữ liệu cập nhật.
  - Trạng thái thất bại (`Error`) kèm message lỗi.
- Test One-shot Events: Bắn event điều hướng (Navigate) hoặc thông báo (Toast/Snackbar) đúng thời điểm.

---

## 5. Tính Độc Lập & Xác Định (Determinism & Isolation)
- Mỗi test case (`@Test`) phải hoàn toàn độc lập, có hàm `@Before` để reset mocks/states.
- Thứ tự thực thi của các test cases không được ảnh hưởng đến kết quả của nhau.
- Không sử dụng `Thread.sleep()` trong Unit Test; sử dụng `runTest` và `advanceUntilIdle()` của `kotlinx-coroutines-test`.

---

## 6. Tiêu Chí Nghiệm Thu (Definition of Done)
- **Code biên dịch (compile) thành công KHÔNG CÓ NGHĨA là xong việc**.
- Một tính năng/thay đổi chỉ được xem là hoàn tất khi:
  1. Đã có đầy đủ Unit Test kiểm tra đúng behavior thực tế của code.
  2. Toàn bộ các test cases chạy thành công (PASSED).
  3. Không làm gãy bất kỳ Unit Test hiện có nào trong project.
