# OmniGo AI Agent Rules & Operational Manifesto

Chào mừng AI Assistant đến với dự án **OmniGo (Hệ thống Siêu ứng dụng Đa dịch vụ: Đặt xe, Giao đồ ăn, Thanh toán trực tuyến)**.
Tài liệu này định nghĩa các nguyên tắc cốt lõi, bắt buộc tuân thủ 100% khi thực thi bất kỳ nhiệm vụ lập trình hoặc phân tích nào trong workspace này.

---

## 1. Nguyên Tắc Cốt Lõi: Phương Pháp HARNESS & Strict Scope Control

> **QUY TẮC BẤT KHẢ KHÁM PHẠM (ABSOLUTE CONSTRAINT):**
> 1. **KHÔNG CODE NGOÀI YÊU CẦU**: Tuyệt đối không tự ý thêm các tính năng "tưởng như cần thiết", không viết thêm helper functions không được yêu cầu, không tự ý tái cấu trúc (refactor) các đoạn code lân cận không liên quan.
> 2. **HARNESS-FIRST (Khung Neo Chặn)**: Mọi thay đổi logic nghiệp vụ quan trọng phải xác định rõ "Harness": Đầu vào (Input Spec), Đầu ra (Output Contract), Phạm vi ảnh hưởng (Blast Radius), và Tiêu chí Kiểm thử (Acceptance Tests).
> 3. **SURGICAL EDITING (Phẫu thuật chính xác)**: Sửa đổi số lượng dòng code tối thiểu cần thiết để hoàn thành yêu cầu. Giữ nguyên toàn bộ comment hiện có, style code, và cấu trúc đã định hình.
> 4. **ZERO REGRESSION & PRESERVE WORKING STATE (Bảo toàn mã đang chạy & Cấm gây lỗi gián tiếp)**: Tuyệt đối không làm gãy, không gây lỗi hồi quy (regression bugs) hoặc tác dụng phụ (side-effects) lên các đoạn code/chức năng không liên quan vốn đang hoạt động bình thường. Sửa ở đâu thì chỉ ảnh hưởng đúng vùng đó, bảo đảm tương thích ngược (backward compatibility).

---

## 2. Hệ Thống Luật & Chỉ Dẫn (Hierarchical Rules)

Tất cả các agent làm việc trong workspace này phải tự động áp dụng các quy tắc được định nghĩa chi tiết trong thư mục `.agents/rules/`:

1. [01-harness-guidelines.md](file:///./.agents/rules/01-harness-guidelines.md): Phương pháp Harness - Cách thức xác lập ranh giới, thiết kế bài test harness và khóa chặt kỳ vọng trước khi code.
2. [02-scope-boundary.md](file:///./.agents/rules/02-scope-boundary.md): Giới hạn phạm vi - Chống phình to scope (Scope Creep), kỹ thuật chỉnh sửa tối thiểu và chống ảo giác (Anti-Hallucination).
3. [03-architecture-standards.md](file:///./.agents/rules/03-architecture-standards.md): Chuẩn kiến trúc Microservices (Java Spring Boot, Eureka, Gateway, Kafka), React Vite Frontend, và Android App.
4. [04-safety-and-quality.md](file:///./.agents/rules/04-safety-and-quality.md): An toàn dữ liệu, xử lý Exception, bảo mật Secret/API Key, Logging có kiểm soát.

---

## 3. Quy Trình Làm Việc Tiêu Chuẩn (Workflows)

Khi thực hiện các tác vụ, hãy vận hành theo các workflow chuẩn trong `.agents/workflows/`:

*   **Phát triển tính năng mới**: Theo quy trình 4 bước Harness tại [feature-development.md](file:///./.agents/workflows/feature-development.md).
*   **Điều tra & Fix Bug**: Quy trình tái hiện, khoanh vùng và sửa chữa an toàn tại [bugfix-investigation.md](file:///./.agents/workflows/bugfix-investigation.md).
*   **Review & Nghiệm thu Code**: Tiêu chuẩn kiểm tra diff và verify tại [code-review-verification.md](file:///./.agents/workflows/code-review-verification.md).

---

## 4. Kích Hoạt Kỹ Năng AI (Skills)

Kích hoạt các kỹ năng chuyên biệt khi xử lý các phần việc tương ứng:
*   `harness-spec-validator`: Xác thực ràng buộc trước/sau khi code ([SKILL.md](file:///./.agents/skills/harness-spec-validator/SKILL.md)).
*   `code-boundary-guard`: Tự động rà soát diff, ngăn chặn code rác / ngoài yêu cầu ([SKILL.md](file:///./.agents/skills/code-boundary-guard/SKILL.md)).
*   `microservice-dev`: Quy chuẩn phát triển Backend Spring Boot ([SKILL.md](file:///./.agents/skills/microservice-dev/SKILL.md)).
*   `frontend-react-dev`: Quy chuẩn phát triển Frontend React + Vite ([SKILL.md](file:///./.agents/skills/frontend-react-dev/SKILL.md)).
