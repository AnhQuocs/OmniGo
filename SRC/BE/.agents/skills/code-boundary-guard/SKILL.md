---
name: code-boundary-guard
description: >-
  Use this skill to inspect file modifications, analyze git diffs, and ensure zero unrequested changes or scope creep. Protects against hallucinations and over-engineering.
---

# Skill: Code Boundary Guard

Kỹ năng tự kiểm tra ranh giới, ngăn chặn việc sinh mã thừa thãi và phát hiện các chỉnh sửa nằm ngoài phạm vi yêu cầu.

## Khi Nào Sử Dụng
* Trước khi AI kết thúc phản hồi hoặc nộp kết quả công việc cho người dùng.
* Khi chuẩn bị thực hiện refactoring hoặc sửa đổi nhiều file đồng thời.

## Danh Sách Kiểm Tra Ranh Giới (Boundary Inspection)

### 1. Phân Tích Phạm Vi Tác Động (Blast Radius Analysis)
- Đếm số lượng file bị thêm mới, sửa đổi hoặc xóa bỏ.
- Đặt câu hỏi: *"Mỗi file bị sửa có nằm trong danh sách cần thiết để đáp ứng yêu cầu người dùng không?"*
- Nếu có file bị sửa ngoài ý muốn (ví dụ: tự ý sửa file config chung), lập tức hoàn tác (`revert`).

### 2. Phát Hiện Code Thừa & Over-Engineering (Dead Code & Bloat Detection)
- Tìm các hàm helper, interface, enum được tạo mới nhưng chỉ được gọi 0 hoặc 1 lần không cần thiết.
- Tìm các class dự phòng cho tương lai (ví dụ: tạo sẵn 5 Payment Provider trong khi chỉ cần 1).
- Loại bỏ toàn bộ phần code thừa này trước khi hoàn thành.

### 3. Bảo Vệ Bình Luận & Định Dạng (Preserve Comments & Format)
- Kiểm tra lại các file đã sửa: đảm bảo không làm mất comment, Javadoc, TODO hoặc license header của dự án gốc.

### 4. Kiểm Soát Lỗi Hồi Quy (Zero Regression & Side-Effect Check)
- Đảm bảo các hàm dùng chung, signature API cũ không bị phá vỡ.
- Kiểm tra lại để chắc chắn các chức năng lân cận đang chạy tốt không bị ảnh hưởng tiêu cực sau khi sửa code.

