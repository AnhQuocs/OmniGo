# OmniGo AI Agent Rules & Operational Manifesto

Chào mừng AI Assistant đến với dự án **OmniGo (Hệ thống Siêu ứng dụng Đa dịch vụ: Đặt xe, Giao đồ ăn, Thanh toán trực tuyến)**.
Tài liệu này định nghĩa các nguyên tắc cốt lõi, bắt buộc tuân thủ 100% khi thực thi bất kỳ nhiệm vụ lập trình hoặc phân tích nào trong workspace này.

> **QUY TẮC BẮT BUỘC TỐI CAO (HARNESS² MANDATE):**
> Mọi tương tác, chỉnh sửa mã nguồn và lập kế hoạch bắt buộc phải tuân theo kỹ năng **Harness²** ([harness2/SKILL.md](file:///./.agents/skills/harness2/SKILL.md)) dựa trên nghiên cứu của Google Research.
> - **Kích hoạt bắt buộc:** Khi thực thi bất kỳ tác vụ nào trong workspace, agent bắt buộc đọc và tuân thủ `.agents/skills/harness2/SKILL.md`.
> - **Trigger line:**
>   - When executing any task, planning edits, or validating changes, read .agents/skills/harness2/SKILL.md and follow it.

---

## 1. Nguyên Tắc Cốt Lõi: Khung Neo Chặn HARNESS² & Strict Scope Control

> **QUY TẮC BẤT KHẢ KHÁM PHẠM (ABSOLUTE CONSTRAINTS):**
> 1. **TUÂN THỦ HARNESS² (Google Research)**: Áp dụng chu trình Propose–Probe–Compose, tận dụng bằng chứng thực thi tương phản (behavioral contrasts) để ra quyết định, không dựa vào suy đoán.
> 2. **KHÔNG CODE NGOÀI YÊU CẦU**: Tuyệt đối không tự ý thêm các tính năng "tưởng như cần thiết", không viết thêm helper functions không được yêu cầu, không tự ý tái cấu trúc (refactor) các đoạn code lân cận không liên quan.
> 3. **HARNESS-FIRST (Khung Neo Chặn Hợp Đồng)**: Mọi thay đổi logic nghiệp vụ quan trọng phải xác định rõ "Harness": Đầu vào (Input Spec), Đầu ra (Output Contract), và Phạm vi ảnh hưởng (Blast Radius).
> 4. **SURGICAL EDITING (Phẫu thuật chính xác)**: Sửa đổi số lượng dòng code tối thiểu cần thiết để hoàn thành yêu cầu. Giữ nguyên toàn bộ comment hiện có, style code, và cấu trúc đã định hình.
> 5. **ZERO REGRESSION & PRESERVE WORKING STATE**: Tuyệt đối không làm gãy, không gây lỗi hồi quy (regression bugs) hoặc tác dụng phụ lên các đoạn code/chức năng đang hoạt động bình thường. Đảm bảo tương thích ngược 100%.
> 6. **DELIVERABLE SHAPE BELONGS TO THE REQUEST**: Hình thái sản phẩm bàn giao (Deliverable Shape: DTO, API response, cấu trúc cột, file trả về) thuộc về yêu cầu bài toán. Không chèn log nội bộ, không chèn ghi chú suy luận của agent vào output bàn giao.

---

## 2. Hệ Thống Luật & Chỉ Dẫn (Hierarchical Rules)

Tất cả các agent làm việc trong workspace này phải tự động áp dụng các quy tắc được định nghĩa chi tiết trong thư mục `.agents/rules/`:

1. [01-harness-guidelines.md](file:///./.agents/rules/01-harness-guidelines.md): Phương pháp Harness - Cách thức xác lập ranh giới hợp đồng dữ liệu và khóa chặt kỳ vọng trước khi code (không bắt buộc viết unit test).
2. [02-scope-boundary.md](file:///./.agents/rules/02-scope-boundary.md): Giới hạn phạm vi - Chống phình to scope (Scope Creep), kỹ thuật chỉnh sửa tối thiểu và chống ảo giác (Anti-Hallucination).
3. [03-architecture-standards.md](file:///./.agents/rules/03-architecture-standards.md): Chuẩn kiến trúc Microservices (Java Spring Boot, Eureka, Gateway, Kafka), React Vite Frontend, và Android App.
4. [04-safety-and-quality.md](file:///./.agents/rules/04-safety-and-quality.md): An toàn dữ liệu, xử lý Exception, bảo mật Secret/API Key, Logging có kiểm soát.

---

## 3. Quy Trình Làm Việc Tiêu Chuẩn (Harness² & Superpowers Cycle)

Hệ thống kết hợp quy trình nghiêm ngặt của OmniGo Harness² cùng phương pháp luận **Superpowers** theo chu trình khép kín:
```
[PROPOSE / PLAN] ───► [PROBE / BUILD] ───► [COMPOSE / VERIFY] ───► [REFLECT / ITERATE]
```

1. **PROPOSE / PLAN (Khảo sát & Đề xuất)**:
   - Kích hoạt `brainstorming` để đối thoại làm rõ yêu cầu, xác định các trường hợp biên và chốt hợp đồng dữ liệu.
   - Phân biệt rõ Invariant Edits (quy tắc bất biến) và Domain Edits (nghiệp vụ cụ thể).
   - Sử dụng `writing-plans` để chia nhỏ đầu việc thành các task 2–5 phút, ghi rõ đường dẫn file và mã nguồn cần sửa.
2. **PROBE / BUILD (Thực thi & Thu thập bằng chứng)**:
   - Viết code tối thiểu, chính xác để đáp ứng trọn vẹn yêu cầu người dùng mà không cần viết test tự động (dự án không yêu cầu viết test).
   - Áp dụng `code-boundary-guard` và `harness-spec-validator` để không sinh code ngoài phạm vi.
   - Kiểm tra các ràng buộc: Không vi phạm 3 quy tắc huỷ bỏ của Harness² (mọi skill phải có trigger, không dùng template braces trong always-on text, mọi script được gọi phải tồn tại).
3. **COMPOSE / VERIFY (Tổng hợp & Nghiệm thu có bằng chứng)**:
   - Đánh giá tương phản: Chỉ giữ lại các sửa đổi tối thiểu đem lại kết quả chính xác và zero-regression.
   - Kích hoạt `verification-before-completion` để biên dịch (compile), build và cung cấp bằng chứng thực tế (build output/log).
4. **REFLECT / ITERATE (Đánh giá, Đúc rút kinh nghiệm & Hoàn tất)**:
   - Ghi nhận bài học kinh nghiệm để không lặp lại lỗi ở các lần thực thi tiếp theo.
   - Thực hiện `requesting-code-review` và `receiving-code-review` kiểm tra spec compliance và code quality.
   - Đóng nhánh an toàn bằng `finishing-a-development-branch`.

---

## 4. Danh Mục Kỹ Năng AI (Skills Ecosystem & Triggers)

### Kỹ năng cốt lõi bắt buộc (Core Foundation):
- When executing any task, planning edits, or validating changes, read .agents/skills/harness2/SKILL.md and follow it.
  * [harness2](file:///./.agents/skills/harness2/SKILL.md): **(BẮT BUỘC)** Chuẩn khung neo chặn đệ quy Harness² (Google Research).
- When validating API/DTO contracts or boundary conditions, read .agents/skills/harness-spec-validator/SKILL.md and follow it.
  * [harness-spec-validator](file:///./.agents/skills/harness-spec-validator/SKILL.md): Xác thực ràng buộc trước/sau khi code.
- When inspecting file modifications or preventing scope creep, read .agents/skills/code-boundary-guard/SKILL.md and follow it.
  * [code-boundary-guard](file:///./.agents/skills/code-boundary-guard/SKILL.md): Tự động rà soát diff, ngăn chặn code rác / ngoài yêu cầu.
- When developing or extending Spring Boot microservices, read .agents/skills/microservice-dev/SKILL.md and follow it.
  * [microservice-dev](file:///./.agents/skills/microservice-dev/SKILL.md): Quy chuẩn phát triển Backend Spring Boot.
- When developing or styling the React web frontend, read .agents/skills/frontend-react-dev/SKILL.md and follow it.
  * [frontend-react-dev](file:///./.agents/skills/frontend-react-dev/SKILL.md): Quy chuẩn phát triển Frontend React + Vite.

### Kỹ năng phương pháp luận (Superpowers Framework):
- When exploring user intent and designing features, read .agents/skills/brainstorming/SKILL.md and follow it.
  * [brainstorming](file:///./.agents/skills/brainstorming/SKILL.md): Đối thoại Socratic làm rõ ý đồ thiết kế trước khi viết code.
- When organizing multi-step tasks into executable plans, read .agents/skills/writing-plans/SKILL.md and follow it.
  * [writing-plans](file:///./.agents/skills/writing-plans/SKILL.md): Phân rã công việc thành các bước chi tiết.
- When executing implementation plans with review checkpoints, read .agents/skills/executing-plans/SKILL.md and follow it.
  * [executing-plans](file:///./.agents/skills/executing-plans/SKILL.md): Thực thi plan theo từng đợt có checkpoint nghiệm thu.
- When investigating bugs or test failures, read .agents/skills/systematic-debugging/SKILL.md and follow it.
  * [systematic-debugging](file:///./.agents/skills/systematic-debugging/SKILL.md): Quy trình 4 bước điều tra và khoanh vùng root-cause.
- When claiming work is complete or fixed, read .agents/skills/verification-before-completion/SKILL.md and follow it.
  * [verification-before-completion](file:///./.agents/skills/verification-before-completion/SKILL.md): Xác minh bằng chứng thực nghiệm trước khi kết luận.
- When dispatching independent tasks to subagents, read .agents/skills/subagent-driven-development/SKILL.md and follow it.
  * [subagent-driven-development](file:///./.agents/skills/subagent-driven-development/SKILL.md): Điều phối subagent giải quyết từng task độc lập.
- When requesting review before merging or completing tasks, read .agents/skills/requesting-code-review/SKILL.md and follow it.
  * [requesting-code-review](file:///./.agents/skills/requesting-code-review/SKILL.md): Tự kiểm tra và yêu cầu review code trước khi bàn giao.
- When receiving code review feedback, read .agents/skills/receiving-code-review/SKILL.md and follow it.
  * [receiving-code-review](file:///./.agents/skills/receiving-code-review/SKILL.md): Xử lý phản hồi review một cách kỷ luật.
- When facing independent parallel tasks, read .agents/skills/dispatching-parallel-agents/SKILL.md and follow it.
  * [dispatching-parallel-agents](file:///./.agents/skills/dispatching-parallel-agents/SKILL.md): Phân phối các nhánh công việc song song.
- When isolating workspace branches, read .agents/skills/using-git-worktrees/SKILL.md and follow it.
  * [using-git-worktrees](file:///./.agents/skills/using-git-worktrees/SKILL.md): Cô lập workspace phát triển song song an toàn.
- When finalizing and integrating work, read .agents/skills/finishing-a-development-branch/SKILL.md and follow it.
  * [finishing-a-development-branch](file:///./.agents/skills/finishing-a-development-branch/SKILL.md): Nghiệm thu và dọn dẹp nhánh.
- When creating or updating skills, read .agents/skills/writing-skills/SKILL.md and follow it.
  * [writing-skills](file:///./.agents/skills/writing-skills/SKILL.md): Hướng dẫn tạo skill mới chuẩn chỉnh.
- When starting any conversation, read .agents/skills/using-superpowers/SKILL.md and follow it.
  * [using-superpowers](file:///./.agents/skills/using-superpowers/SKILL.md): Cơ chế kích hoạt và quy tắc ưu tiên skill.


