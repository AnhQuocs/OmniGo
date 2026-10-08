---
name: harness-spec-validator
description: >-
  Use this skill to validate API/DTO contracts, boundary conditions, and test harness constraints before and after modifying code. Enforces strict input/output contract locking.
---

# Skill: Harness Spec Validator

Kỹ năng này giúp AI thiết lập và kiểm thử khung neo chặn (Harness) cho các thay đổi trong hệ thống OmniGo.

## Khi Nào Sử Dụng
* Khi bắt đầu xây dựng API endpoint mới hoặc thay đổi DTO/Entity.
* Khi cần đảm bảo giao tiếp giữa Frontend và Backend, hoặc giữa các Microservice không bị gãy vỡ (Breaking Changes).

## Các Bước Thực Hiện

### 1. Phân Tích & Xác Định Hợp Đồng (Contract Extraction)
- Xác định rõ Request Body, Query Parameters, Headers và Response Body.
- Kiểm tra các ràng buộc dữ liệu:
  - `@NotNull`, `@NotBlank`, `@Size`, `@Min`, `@Max` trong Java.
  - PropTypes hoặc TypeScript types (nếu có) trong Frontend.

### 2. Định Nghĩa Harness Test Matrix
Thiết lập ma trận kiểm tra tối thiểu:
1. **Happy Path**: Dữ liệu chuẩn xác, mong đợi mã `200 OK` hoặc `201 CREATED`.
2. **Missing Required Fields**: Thiếu trường bắt buộc, mong đợi `400 BAD REQUEST` kèm thông điệp lỗi rõ ràng.
3. **Invalid Data Type**: Sai kiểu dữ liệu, mong đợi mã `400 BAD REQUEST`.
4. **Not Found**: Bản ghi không tồn tại, mong đợi `404 NOT FOUND`.

### 3. Đóng Khung Kiểm Tra (Lock & Validate)
- Sau khi viết code, so sánh cấu trúc JSON trả về với Harness Contract ban đầu.
- Tuyệt đối không tự ý thêm các trường trả về ngoài hợp đồng.
