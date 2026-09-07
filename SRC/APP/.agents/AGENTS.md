# Android Agent Rules & Architecture Manifesto

Tài liệu này định nghĩa các nguyên tắc cốt lõi, chuẩn kiến trúc, quy ước lập trình và tiêu chuẩn kiểm thử (Unit Test) bắt buộc 100% khi phát triển ứng dụng Android trong workspace này.

---

## 1. Nguyên Tắc Cốt Lõi (Core Principles)

1. **CLEAN ARCHITECTURE & MVVM**:
   - Tách biệt rõ ràng 3 tầng: `data` -> `domain` -> `presentation`.
   - Data phụ thuộc Domain, Presentation phụ thuộc Domain. Domain độc lập hoàn toàn, không phụ thuộc vào Android Framework hay Data Layer.
   - Mọi tương tác dữ liệu/nghiệp vụ từ ViewModel phải thông qua **Use Case** và **Repository Pattern**.

2. **100% TOKENIZED DESIGN SYSTEM & ZERO MAGIC NUMBERS**:
   - Không hardcode màu sắc (`Color(...)`, `#HEX`), kích thước (dp/sp), khoảng cách (Spacer), bo góc (Shape) hoặc chuỗi văn bản (Strings) trực tiếp trong file UI.
   - Mọi chuỗi hiển thị phải khai báo đầy đủ ở **cả 3 file** `strings.xml` (`values/`, `values-en/`, `values-vi/`).

3. **FILE SIZE & SINGLE RESPONSIBILITY (< 400 LINES)**:
   - Mỗi file Kotlin tuyệt đối **không vượt quá 400 dòng**.
   - Chủ động phân rã màn hình phức tạp thành các section/sub-components độc lập.
   - Tránh duplicate code cho các UI components tái sử dụng; không over-engineer/tạo abstraction thừa thãi cho các component chỉ dùng 1 lần.

4. **CHECK BEFORE CODE & CONVENTION CONSISTENCY**:
   - Luôn kiểm tra các components, utilities, resources sẵn có trong project trước khi tạo mới.
   - Giữ tính nhất quán với cấu trúc và style hiện tại. Không tự ý tạo convention mới khi đã có convention tương ứng.

5. **TEST-DRIVEN, COMPREHENSIVE & BEHAVIOR-FOCUSED TESTING**:
   - Mọi business logic và logic xử lý dữ liệu có khả năng phát sinh lỗi phải có Unit Test. Ưu tiên kiểm thử Use Cases, ViewModels, Repository implementations, Mappers, Validators, error handling và data transformation. Các DTO, API interfaces, DAO interfaces và class chỉ chứa boilerplate không bắt buộc có Unit Test nếu không có behavior riêng cần kiểm chứng.
   - **Không viết test chỉ để lấy số % coverage**: Test phải kiểm tra hành vi thực tế (actual behavior) và **bắt buộc phải FAIL** khi behavior đó bị thay đổi sai.
   - Test đầy đủ: Happy Path, Error/Exception, Boundary cases và Branch logic.
   - Quy trình bắt buộc: `Implement → Viết/Update Unit Test → Chạy các Unit Test liên quan → Tất cả test liên quan phải PASS mới coi task hoàn thành`.
   - Nếu test hiện có của project fail do nguyên nhân không liên quan đến thay đổi hiện tại, phải phân biệt rõ lỗi pre-existing với regression mới và báo cáo lại, không tự ý sửa các phần không liên quan.

---

## 2. Hệ Thống Luật & Quy Ước Chi Tiết (Hierarchical Rules)

Các agent hoạt động trong workspace bắt buộc tuân thủ 5 bộ quy chuẩn tại `.agents/rules/`:

1. [01-architecture-standards.md](file:///./.agents/rules/01-architecture-standards.md): Tiêu chuẩn Clean Architecture, MVVM, Hilt DI, Repository Pattern, Use Case & StateFlow.
2. [02-ui-design-system-conventions.md](file:///./.agents/rules/02-ui-design-system-conventions.md): Quy ước UI/UX, Design Tokens (Dimen, AppSpacing, AppShape, Color, TypographyExt) và i18n 3 ngôn ngữ.
3. [03-code-organization-and-modularity.md](file:///./.agents/rules/03-code-organization-and-modularity.md): Giới hạn độ dài file (< 400 lines), phân rã component, tái sử dụng và nguyên tắc chống over-engineering.
4. [04-development-behavior-and-safety.md](file:///./.agents/rules/04-development-behavior-and-safety.md): Quy trình kiểm tra tài nguyên, giữ tính nhất quán, phẫu thuật code chính xác và refactor an toàn.
5. [05-testing-standards.md](file:///./.agents/rules/05-testing-standards.md): Chuẩn kiểm thử Unit Test cho Use Case, ViewModel State transitions, Repository, Mapper và MockK/Turbine (Chống test hình thức).

---

## 3. Quy Trình Làm Việc Tiêu Chuẩn (Workflows)

Khi thực hiện nhiệm vụ, hãy vận hành theo các workflow chuẩn trong `.agents/workflows/`:

*   **Phát triển tính năng mới**: [feature-implementation.md](file:///./.agents/workflows/feature-implementation.md).
*   **Refactor & Dọn dẹp Code**: [code-refactoring-cleanup.md](file:///./.agents/workflows/code-refactoring-cleanup.md).

---

## 4. Kỹ Năng Chuyên Biệt (Skills)

Kích hoạt các kỹ năng chuyên biệt khi thực thi:
*   `android-clean-arch-dev`: Dựng và chuẩn hóa tầng Data - Domain - Presentation với Hilt & Use Cases ([SKILL.md](file:///./.agents/skills/android-clean-arch-dev/SKILL.md)).
*   `compose-design-system-guard`: Rà soát Design Tokens, i18n 3 file strings.xml, check file size < 400 dòng ([SKILL.md](file:///./.agents/skills/compose-design-system-guard/SKILL.md)).
*   `android-unit-test-guide`: Hướng dẫn mẫu và thực thi Unit Test thực chất cho UseCase, ViewModel, Repository (MockK, Turbine, Coroutines Test) ([SKILL.md](file:///./.agents/skills/android-unit-test-guide/SKILL.md)).
