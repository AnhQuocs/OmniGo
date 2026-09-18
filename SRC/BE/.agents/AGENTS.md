# OmniGo AI Agent Rules & Operational Manifesto

Chào mừng AI Assistant đến với dự án **OmniGo (Hệ thống Siêu ứng dụng Đa dịch vụ: Đặt xe, Giao đồ ăn, Thanh toán trực tuyến)**.
Tài liệu này định nghĩa các nguyên tắc cốt lõi, bắt buộc tuân thủ 100% khi thực thi bất kỳ nhiệm vụ lập trình hoặc phân tích nào trong workspace này.

---

## 1. Nguyên Tắc Cốt Lõi: Phương Pháp HARNESS & Strict Scope Control

> **QUY TẮC BẤT KHẢ KHÁM PHẠM (ABSOLUTE CONSTRAINT):**
> 1. **KHÔNG CODE NGOÀI YÊU CẦU**: Tuyệt đối không tự ý thêm các tính năng "tưởng như cần thiết", không viết thêm helper functions không được yêu cầu, không tự ý tái cấu trúc (refactor) các đoạn code lân cận không liên quan.
> 2. **HARNESS-FIRST (Khung Neo Chặn)**: Mọi thay đổi logic nghiệp vụ quan trọng phải xác định rõ "Harness": Đầu vào (Input Spec), Đầu ra (Output Contract), và Phạm vi ảnh hưởng (Blast Radius).
> 3. **SURGICAL EDITING (Phẫu thuật chính xác)**: Sửa đổi số lượng dòng code tối thiểu cần thiết để hoàn thành yêu cầu. Giữ nguyên toàn bộ comment hiện có, style code, và cấu trúc đã định hình.
> 4. **ZERO REGRESSION & PRESERVE WORKING STATE (Bảo toàn mã đang chạy & Cấm gây lỗi gián tiếp)**: Tuyệt đối không làm gãy, không gây lỗi hồi quy (regression bugs) hoặc tác dụng phụ (side-effects) lên các đoạn code/chức năng không liên quan vốn đang hoạt động bình thường. Sửa ở đâu thì chỉ ảnh hưởng đúng vùng đó, bảo đảm tương thích ngược (backward compatibility).

---

## 2. Hệ Thống Luật & Chỉ Dẫn (Hierarchical Rules)

Tất cả các agent làm việc trong workspace này phải tự động áp dụng các quy tắc được định nghĩa chi tiết trong thư mục `.agents/rules/`:

1. [01-harness-guidelines.md](file:///./.agents/rules/01-harness-guidelines.md): Phương pháp Harness - Cách thức xác lập ranh giới hợp đồng dữ liệu và khóa chặt kỳ vọng trước khi code (không bắt buộc viết unit test).
2. [02-scope-boundary.md](file:///./.agents/rules/02-scope-boundary.md): Giới hạn phạm vi - Chống phình to scope (Scope Creep), kỹ thuật chỉnh sửa tối thiểu và chống ảo giác (Anti-Hallucination).
3. [03-architecture-standards.md](file:///./.agents/rules/03-architecture-standards.md): Chuẩn kiến trúc Microservices (Java Spring Boot, Eureka, Gateway, Kafka), React Vite Frontend, và Android App.
4. [04-safety-and-quality.md](file:///./.agents/rules/04-safety-and-quality.md): An toàn dữ liệu, xử lý Exception, bảo mật Secret/API Key, Logging có kiểm soát.

---

## 3. Quy Trình Làm Việc Tiêu Chuẩn (Workflows & Superpowers Cycle)

Hệ thống kết hợp quy trình nghiêm ngặt của OmniGo Harness cùng phương pháp luận **Superpowers** theo chu trình khép kín:
```
[PLAN] ───► [BUILD] ───► [VERIFY] ───► [ITERATE]
```

1. **PLAN (Khảo sát & Lập kế hoạch)**:
   - Trước khi code bất kỳ tính năng hay refactor nào, bắt buộc kích hoạt `brainstorming` để đối thoại làm rõ yêu cầu, xác định các trường hợp biên và chốt spec.
   - Sử dụng `writing-plans` để chia nhỏ đầu việc thành các task 2–5 phút, ghi rõ đường dẫn file, mã nguồn cần sửa.
2. **BUILD (Xây dựng & Kiểm soát viền)**:
   - Viết code tối thiểu, chính xác để đáp ứng trọn vẹn yêu cầu người dùng mà không cần viết test tự động (dự án không yêu cầu viết test).
   - Áp dụng `code-boundary-guard` và `harness-spec-validator` để không sinh code ngoài phạm vi.
   - Có thể điều phối qua `subagent-driven-development` hoặc `executing-plans`.
3. **VERIFY (Điều tra & Nghiệm thu có bằng chứng)**:
   - Khi fix bug, kích hoạt `systematic-debugging` theo quy trình điều tra khoanh vùng nguyên nhân gốc rễ.
   - Trước khi thông báo hoàn thành nhiệm vụ, kích hoạt `verification-before-completion` để biên dịch (compile), build và cung cấp bằng chứng thực tế (build output/log).
4. **ITERATE (Đánh giá & Hoàn tất)**:
   - Thực hiện `requesting-code-review` và `receiving-code-review` kiểm tra spec compliance và code quality.
   - Đóng nhánh an toàn bằng `finishing-a-development-branch`.

---

## 4. Danh Mục Kỹ Năng AI (Skills Ecosystem)

### Kỹ năng cốt lõi dự án (OmniGo Specialized):
*   `harness-spec-validator`: Xác thực ràng buộc trước/sau khi code ([SKILL.md](file:///./.agents/skills/harness-spec-validator/SKILL.md)).
*   `code-boundary-guard`: Tự động rà soát diff, ngăn chặn code rác / ngoài yêu cầu ([SKILL.md](file:///./.agents/skills/code-boundary-guard/SKILL.md)).
*   `microservice-dev`: Quy chuẩn phát triển Backend Spring Boot ([SKILL.md](file:///./.agents/skills/microservice-dev/SKILL.md)).
*   `frontend-react-dev`: Quy chuẩn phát triển Frontend React + Vite ([SKILL.md](file:///./.agents/skills/frontend-react-dev/SKILL.md)).

### Kỹ năng phương pháp luận (Superpowers Framework):
*   `brainstorming`: Đối thoại Socratic làm rõ ý đồ thiết kế trước khi viết code ([SKILL.md](file:///./.agents/skills/brainstorming/SKILL.md)).
*   `writing-plans`: Phân rã công việc thành các bước siêu chi tiết ([SKILL.md](file:///./.agents/skills/writing-plans/SKILL.md)).
*   `executing-plans`: Thực thi plan theo từng đợt có checkpoint nghiệm thu ([SKILL.md](file:///./.agents/skills/executing-plans/SKILL.md)).
*   `test-driven-development`: (Tùy chọn, dự án không yêu cầu test tự động) ([SKILL.md](file:///./.agents/skills/test-driven-development/SKILL.md)).
*   `systematic-debugging`: Quy trình 4 bước điều tra và khoanh vùng root-cause ([SKILL.md](file:///./.agents/skills/systematic-debugging/SKILL.md)).
*   `verification-before-completion`: Xác minh bằng chứng thực nghiệm trước khi kết luận ([SKILL.md](file:///./.agents/skills/verification-before-completion/SKILL.md)).
*   `subagent-driven-development`: Điều phối subagent giải quyết từng task độc lập ([SKILL.md](file:///./.agents/skills/subagent-driven-development/SKILL.md)).
*   `requesting-code-review`: Tự kiểm tra và yêu cầu review code trước khi bàn giao ([SKILL.md](file:///./.agents/skills/requesting-code-review/SKILL.md)).
*   `receiving-code-review`: Xử lý phản hồi review một cách kỷ luật ([SKILL.md](file:///./.agents/skills/receiving-code-review/SKILL.md)).
*   `dispatching-parallel-agents`: Phân phối các nhánh công việc song song ([SKILL.md](file:///./.agents/skills/dispatching-parallel-agents/SKILL.md)).
*   `using-git-worktrees`: Cô lập workspace phát triển song song an toàn ([SKILL.md](file:///./.agents/skills/using-git-worktrees/SKILL.md)).
*   `finishing-a-development-branch`: Nghiệm thu và dọn dẹp nhánh/worktree ([SKILL.md](file:///./.agents/skills/finishing-a-development-branch/SKILL.md)).
*   `writing-skills`: Hướng dẫn tạo skill mới chuẩn chỉnh ([SKILL.md](file:///./.agents/skills/writing-skills/SKILL.md)).
*   `using-superpowers`: Cơ chế kích hoạt và quy tắc ưu tiên skill ([SKILL.md](file:///./.agents/skills/using-superpowers/SKILL.md)).

