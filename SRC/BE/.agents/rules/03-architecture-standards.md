# Quy Tắc 03: Chuẩn Kiến Trúc & Công Nghệ Dự Án OmniGo

Dự án **OmniGo** được chia thành 3 phân vùng chính. AI phải tuân thủ chuẩn kiến trúc của từng phân vùng:

```
SRC/
├── BE/                  # Hệ thống Microservices Java Spring Boot
│   ├── api-gateway/            # Spring Cloud Gateway (Port routing & security)
│   ├── discovery-service/      # Netflix Eureka Server (Service Registry)
│   ├── user-driver-service/    # Quản lý User, Driver, Authentication
│   ├── booking-service/        # Quản lý chuyến xe, matching, cuốc xe
│   ├── food-delivery-service/  # Quản lý nhà hàng, món ăn, đơn đặt món
│   ├── location-service/       # Xử lý toạ độ GPS, định tuyến, bản đồ
│   ├── payment-service/        # Cổng thanh toán (Momo, VNPay, ZaloPay, Tiền mặt)
│   ├── pricing-service/        # Tính giá động theo khoảng cách, giờ cao điểm
│   └── infra/                  # Docker Compose, Kafka, Redis, Prometheus, Grafana
├── FE/                  # Web Frontend (React + Vite + TailwindCSS/Vanilla)
└── APP/                 # Mobile Application (Android Native / Kotlin / Gradle)
```

---

## 1. Quy Chuẩn Backend (SRC/BE)

*   **Framework & Java Version**: Spring Boot 3.x, Java 17+.
*   **Mô hình phân tầng nghiêm ngặt (Strict Layering)**:
    - `Controller`: Chỉ nhận request, validate DTO (`@Valid`), gọi Service, trả về `ApiResponse<T>`. Tuyệt đối không viết logic nghiệp vụ trong Controller.
    - `Service / ServiceImpl`: Xử lý toàn bộ logic nghiệp vụ, transaction (`@Transactional`), gọi Repository hoặc Client.
    - `Repository`: Kế thừa `JpaRepository`, viết query qua JPA / Native query có tối ưu index.
    - `DTO / Payload`: Tách biệt hoàn toàn `RequestDTO`, `ResponseDTO` với `Entity`. Không bao giờ để `Entity` lộ ra ngoài tầng Controller API.
*   **Giao tiếp liên dịch vụ (Inter-service Communication)**:
    - Đồng bộ: Dùng `OpenFeign` hoặc `WebClient`.
    - Bất đồng bộ: Dùng Apache Kafka Producer/Consumer. Khóa chặt định dạng Event DTO trong Kafka payload.
*   **Cấu hình**: Luôn đưa biến nhạy cảm hoặc cấu hình động vào `application.yml` / `application-dev.yml` và dùng `@Value` hoặc `@ConfigurationProperties`.

---

## 2. Quy Chuẩn Frontend (SRC/FE)

*   **Framework**: React 18+ với Vite.
*   **Tổ chức Components**:
    - `src/components/`: Reusable components (UI blocks, Layout, Header, Modal...).
    - `src/pages/` hoặc `src/views/`: Trang chính theo router.
    - `src/services/` hoặc `src/api/`: Các module gọi API qua Axios / Fetch tập trung.
*   **State & Data Flow**:
    - Tách biệt rõ UI Logic và API Calls.
    - Bắt lỗi mạng (`catch`) đầy đủ và hiển thị Toast/Alert thông báo cho người dùng, không để crash ứng dụng trắng màn hình.

---

## 3. Quy Chuẩn Mobile App (SRC/APP)

*   **Môi trường**: Android Gradle build.
*   Tuân thủ cấu trúc package, tài nguyên `res/values/strings.xml`, `colors.xml`, không hardcode chuỗi hoặc kích thước trong layout.
