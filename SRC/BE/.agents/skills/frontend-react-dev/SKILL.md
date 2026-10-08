---
name: frontend-react-dev
description: >-
  Use this skill when developing, debugging, or styling the React + Vite web frontend in SRC/FE, including state management, API integration, and responsive layouts.
---

# Skill: Frontend React Developer

Kỹ năng chuyên biệt cho ứng dụng Web Frontend (React, Vite, JavaScript/JSX, CSS/Tailwind, Axios).

## Khi Nào Sử Dụng
* Khi xây dựng giao diện người dùng mới hoặc cập nhật giao diện hiện có trong `SRC/FE`.
* Khi tích hợp API từ Backend Gateway vào các component React.

## Hướng Dẫn Kỹ Thuật

### 1. Phân Tách Trách Nhiệm (Separation of Concerns)
* **Components (`src/components/`)**: Chỉ chứa logic hiển thị và props, giữ cho component ngắn gọn, dễ tái sử dụng.
* **Services / API (`src/services/` hoặc `src/api/`)**: Đóng gói các hàm gọi API bằng Axios/Fetch tập trung, không viết trực tiếp URL backend rải rác trong component.

### 2. Xử Lý Trạng Thái & Bắt Lỗi Mạng
* Luôn khai báo đầy đủ 3 trạng thái khi gọi API:
  - `loading`: Hiển thị Skeleton hoặc Spinner.
  - `data`: Render dữ liệu khi thành công.
  - `error`: Bắt lỗi và hiển thị thông báo thân thiện với người dùng (Toast message).

### 3. Thiết Kế UI & Trải Nghiệm Người Dùng
* Giao diện responsive trên cả Desktop và Mobile.
* Hiệu ứng hover, transition mượt mà, màu sắc hài hòa và nhất quán.
