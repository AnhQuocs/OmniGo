---
name: microservice-dev
description: >-
  Use this skill when developing, debugging, or extending Java Spring Boot microservices in SRC/BE, including Eureka, Gateway, Kafka, JPA, and payment integrations.
---

# Skill: Backend Microservice Developer

Kỹ năng chuyên biệt cho hệ sinh thái Backend Microservices (Spring Boot, Java 17+, Eureka, Cloud Gateway, Kafka, MySQL/PostgreSQL, Redis).

## Khi Nào Sử Dụng
* Khi tạo mới hoặc cập nhật API trong các service: `user-driver-service`, `booking-service`, `food-delivery-service`, `payment-service`, `pricing-service`, `location-service`, `api-gateway`, `discovery-service`.
* Khi cấu hình Kafka producer/consumer hoặc Eureka client.

## Hướng Dẫn Kỹ Thuật

### 1. Chuẩn Hóa Kiến Trúc 3 Tầng (Controller - Service - Repository)
```
[Client/Gateway] 
       │ (JSON Request)
       ▼
[Controller] ──> (@Valid RequestDTO)
       │
       ▼
[Service / ServiceImpl] ──> (Business Logic, @Transactional, DTO-to-Entity)
       │
       ▼
[Repository] ──> (Spring Data JPA / DB)
```

### 2. Định Dạng Phản Hồi Chuẩn (`ApiResponse<T>`)
Luôn bọc kết quả trả về trong đối tượng response chuẩn của dự án:
```java
public class ApiResponse<T> {
    private int status;
    private String message;
    private T data;
    private LocalDateTime timestamp;
}
```

### 3. Tích Hợp Cổng Thanh Toán (Momo / VNPay / ZaloPay)
- Luôn kiểm tra chữ ký số (Signature / HMAC-SHA256) khi nhận webhook/IPN callback từ cổng thanh toán.
- Đảm bảo tính bất biến (Idempotency): Một giao dịch callback không được cộng tiền hoặc cập nhật trạng thái hai lần.
