# TÀI LIỆU ĐẶC TẢ LUỒNG NGHIỆP VỤ & SƠ ĐỒ TUẦN TỰ HỆ THỐNG OMNIGO
*(OmniGo Business Flow Specification & Exact Implementation State Diagrams)*

---

## 1. TỔNG QUAN KIẾN TRÚC & CÁC TÁC NHÂN HỆ THỐNG (ACTORS & MICROSERVICES)

Hệ thống **OmniGo** vận hành trên nền tảng kiến trúc Microservices phân tán hướng sự kiện (Event-Driven Architecture), kết nối 4 nhóm tác nhân người dùng với 7 microservices chuyên biệt:

### 1.1. Các Tác Nhân Người Dùng (Actors)
| Tác nhân (Actor) | Nền tảng giao diện | Quyền hạn & Vai trò nghiệp vụ |
| :--- | :--- | :--- |
| **Khách Hàng (Customer)** | Android Mobile App | Tìm kiếm chuyến xe, chọn quán ăn, đặt đồ ăn, thanh toán không tiền mặt, theo dõi vị trí trực tiếp (Live Tracking) và đánh giá dịch vụ. |
| **Tài Xế (Driver)** | Android Mobile App | Bật/tắt trạng thái nhận cuốc (`ONLINE`/`OFFLINE`), phát tọa độ GPS liên tục, tiếp nhận cuốc xe trong 20 giây, đón trả khách hoặc đến quán lấy món đi giao. |
| **Nhà Hàng (Merchant)** | Web / Tablet Portal | Quản lý thực đơn, giờ mở cửa, nhận thông báo đơn hàng mới qua WebSocket, xác nhận chế biến và bàn giao món cho tài xế. |
| **Quản Trị Viên (Admin)** | React Web Dashboard | Thẩm định hồ sơ tài xế (`PENDING_APPROVAL` $\rightarrow$ `APPROVED`), khóa tài khoản vi phạm, cấu hình bảng giá/hệ số Surge và theo dõi thống kê toàn sàn. |

### 1.2. Các Dịch Vụ Thành Phần & Hạ Tầng Phân Tán (Microservices & Middleware)
* **API Gateway (Spring Cloud Gateway - Port 8080):** Cửa ngõ duy nhất lọc xác thực JWT, kiểm tra Blacklist/Khóa tài khoản qua Redis, đính kèm header `X-User-Id`, `X-User-Role`, `X-User-Phone` xuống các service nội bộ.
* **Discovery Service (Netflix Eureka - Port 8761):** Quản lý vòng đời và định danh tự động các instance microservices.
* **User & Driver Service (Port 8081):** Quản lý định danh, hồ sơ khách hàng, thông tin xe và trạng thái phê duyệt của tài xế (`PENDING_APPROVAL`, `APPROVED`, `REJECTED`).
* **Booking Service (Port 8082):** Quản lý toàn bộ vòng đời cuốc xe (Ride Lifecycle), thuật toán điều phối và luân chuyển tài xế (Auto-reassignment) theo `BookingStatus`.
* **Location Service (Port 8083):** Tiếp nhận GPS thời gian thực qua WebSocket/STOMP, lập chỉ mục không gian bằng **Redis Geospatial** (`GEOADD`, `GEOSEARCH`).
* **Food Delivery Service (Port 8084):** Quản lý danh mục nhà hàng, thực đơn món ăn, xử lý đơn đặt món 3 bên (Khách - Quán - Tài xế) theo `OrderStatus`.
* **Pricing Service (Port 8085):** Tính toán giá cước động (Surge Pricing) dựa trên khoảng cách, thời gian và mật độ cung-cầu phân vùng theo ô lục giác **Uber H3 Grid**.
* **Payment Service (Port 8086):** Xử lý ví điện tử, đối soát nạp/rút tiền, kết nối cổng thanh toán MoMo, VNPay qua Webhook IPN và trích thu hoa hồng tự động.
* **Apache Kafka:** Xương sống truyền thông điệp bất đồng bộ (Event-Driven Backbone), đảm bảo tính phân tách và chịu tải cao cho toàn bộ hệ thống.
* **Redis:** Bộ nhớ đệm phân tán, lưu trữ Blacklist JWT, Distributed Lock giữ chỗ tài xế (20s) và khóa chống spam đặt cuốc (5s).

---

## 2. CÁC SƠ ĐỒ TUẦN TỰ HỆ THỐNG (SEQUENCE DIAGRAMS)

---

### 2.1. Sơ Đồ Tuần Tự 1: Vòng Đời Đặt Xe & Điều Phối Chuyến Đi (OmniRide Lifecycle)
> **Trạng thái cuốc xe (`BookingStatus`):** `PENDING` $\rightarrow$ `ACCEPTED` $\rightarrow$ `ARRIVED` $\rightarrow$ `IN_PROGRESS` $\rightarrow$ `COMPLETED` (hoặc `CANCELLED`).

```mermaid
sequenceDiagram
    autonumber
    actor Customer as Khách Hàng (App)
    participant OmniGo as Hệ Thống OmniGo (Backend)
    actor Driver as Tài Xế (App)
    participant Payment as Ví & Thanh Toán

    %% 1. TÍNH CƯỚC & ĐẶT XE
    Note over Customer, OmniGo: 1. Khảo sát giá & Đặt xe
    Customer->>OmniGo: POST /api/v1/pricing/calculate (Lộ trình điểm đón / điểm trả)
    OmniGo-->>Customer: Báo giá cước động (Surge Pricing lưới H3)
    Customer->>OmniGo: POST /api/v1/bookings (Khách bấm đặt xe)
    OmniGo-->>Customer: 201 Created (Khởi tạo cuốc xe: PENDING)

    %% 2. ĐIỀU PHỐI TÀI XẾ
    Note over OmniGo, Driver: 2. Quét vị trí & Phát cuốc xe (20s)
    OmniGo->>Driver: Phát thông báo mời nhận cuốc (Giữ chỗ tài xế 20s)
    alt Tài xế nhận cuốc
        Driver->>OmniGo: PUT /api/v1/bookings/{id}/accept
        OmniGo-->>Customer: Thông báo: "Đã tìm thấy tài xế" (Tên, Xe, Biển số)
        OmniGo-->>Driver: Chuyển Status -> ACCEPTED
    else Tài xế từ chối hoặc hết 20s
        Driver-->>OmniGo: Từ chối / Timeout -> Hệ thống tự động chuyển cuốc cho tài xế kế tiếp
    end

    %% 3. ĐÓN KHÁCH & HÀNH TRÌNH
    Note over Customer, Driver: 3. Di chuyển đón khách & Chở khách
    Driver->>OmniGo: Cập nhật tọa độ GPS (STOMP /ws-location)
    OmniGo-->>Customer: Live Tracking xe di chuyển mượt mà trên bản đồ
    Driver->>OmniGo: PUT /api/v1/bookings/{id}/arrived (Khoảng cách <= 50m)
    OmniGo-->>Customer: Thông báo: "Tài xế đã đến điểm đón" (ARRIVED)
    Driver->>OmniGo: PUT /api/v1/bookings/{id}/start -> Bắt đầu chở khách (IN_PROGRESS)

    %% 4. HOÀN TẤT & QUYẾT TOÁN
    Note over Driver, Payment: 4. Hoàn thành chuyến xe & Quyết toán cước phí
    Driver->>OmniGo: PUT /api/v1/bookings/{id}/complete (Khoảng cách <= 50m điểm trả)
    OmniGo->>Payment: Quyết toán cước phí & Trừ 20% phí hoa hồng sàn
    OmniGo-->>Customer: Hiển thị hóa đơn & Mở màn hình đánh giá sao ⭐
    OmniGo-->>Driver: Chuyển Status -> COMPLETED & Cộng thu nhập ròng vào ví
```

---

### 2.2. Sơ Đồ Tuần Tự 2: Vòng Đời Đặt Món 3 Bên (OmniFood Marketplace Lifecycle)
> **Trạng thái đơn món (`OrderStatus`):** `AWAITING_PAYMENT` $\rightarrow$ `PENDING` $\rightarrow$ `ACCEPTED` $\rightarrow$ `PREPARING` $\rightarrow$ `READY_FOR_PICKUP` (hoặc `NO_DRIVER_FOUND` $\rightarrow$ Retry) $\rightarrow$ `DELIVERING` $\rightarrow$ `COMPLETED` (hoặc `CANCELLED`, `REJECTED`).

```mermaid
sequenceDiagram
    autonumber
    actor Customer as Khách Hàng (App)
    participant OmniGo as Hệ Thống OmniGo (Backend)
    actor Merchant as Quán Ăn (Merchant)
    actor Driver as Shipper (Driver App)

    %% 1. ĐẶT MÓN & THANH TOÁN
    Note over Customer, OmniGo: 1. Đặt món & Thanh toán
    Customer->>OmniGo: POST /api/v1/food-orders (Món ăn, Địa chỉ giao, Phương thức TT)
    alt Thanh toán Online (Ví OmniPay, MoMo, VNPay)
        OmniGo-->>Customer: Trạng thái AWAITING_PAYMENT -> Khách thanh toán thành công
        OmniGo->>OmniGo: Cập nhật isPaid = true, Status -> PENDING
    else Thanh toán Tiền mặt (CASH)
        OmniGo->>OmniGo: Tạo đơn thành công (Status -> PENDING)
    end
    OmniGo-->>Merchant: Chuông báo đơn mới qua WebSocket: "Có đơn đặt món mới!"

    %% 2. QUÁN DUYỆT ĐƠN & NẤU MÓN
    Note over Merchant, OmniGo: 2. Quán duyệt đơn (ACCEPTED) & Chế biến món (PREPARING)
    alt Quán từ chối (Hết món / Đóng cửa)
        Merchant->>OmniGo: PATCH /api/v1/food-orders/{id}/status (REJECTED)
        OmniGo-->>Customer: Thông báo nhà hàng từ chối & Tự động hoàn tiền ví (nếu đã thanh toán)
    else Quán tiếp nhận đơn
        Merchant->>OmniGo: PATCH /api/v1/food-orders/{id}/status (ACCEPTED -> PREPARING)
        OmniGo-->>Customer: Thông báo: "Quán đang chuẩn bị món ăn"
        OmniGo->>Driver: Quét tìm shipper quanh quán (Bán kính 3km)
        alt Chưa có tài xế nhận
            OmniGo-->>Merchant: Hiển thị trạng thái NO_DRIVER_FOUND (Có nút Quét tìm lại tối đa 3 lần)
        else Tài xế nhận đơn
            Driver->>OmniGo: Tiếp nhận đơn giao đồ ăn thành công
            OmniGo-->>Customer: Thông báo: "Đã có tài xế nhận giao đơn"
        end
    end

    %% 3. BÀN GIAO MÓN & GIAO HÀNG
    Note over Merchant, Driver: 3. Lấy món & Giao hàng (DELIVERING)
    Merchant->>OmniGo: PATCH status -> READY_FOR_PICKUP (Món đã nấu xong)
    Driver->>OmniGo: PATCH driver-status -> DELIVERING (Đã lấy món từ quán)
    OmniGo-->>Customer: Live Tracking vị trí shipper di chuyển trên bản đồ

    %% 4. HOÀN THÀNH ĐƠN
    Note over Driver, Customer: 4. Giao tận tay khách & Hoàn tất (COMPLETED)
    Driver->>OmniGo: PATCH driver-status -> COMPLETED (Khoảng cách <= 50m)
    OmniGo-->>Customer: Đơn hoàn tất! Mở màn hình Đánh giá: ⭐ Cho Quán & ⭐ Cho Tài xế
```

---

### 2.3. Sơ Đồ Tuần Tự 3: Nạp Tiền & Thanh Toán Trực Tuyến Qua Cổng MoMo / VNPay (IPN Webhook)
> **Trạng thái giao dịch (`TransactionStatus`):** `PENDING` $\rightarrow$ `SUCCESS` (hoặc `FAILED`, `CANCELLED`).

```mermaid
sequenceDiagram
    autonumber
    actor User as Khách Hàng / Tài Xế
    participant OmniGo as Hệ Thống OmniGo (Payment)
    participant Gateway as Cổng Thanh Toán (MoMo / VNPay)

    %% 1. TẠO GIAO DỊCH
    Note over User, Gateway: 1. Khởi tạo giao dịch & Mở màn hình thanh toán
    User->>OmniGo: POST /api/v1/payments/payment/create (Số tiền, Phương thức: MoMo/VNPay)
    OmniGo->>OmniGo: Khởi tạo giao dịch PENDING & Ký số HMAC-SHA256
    OmniGo->>Gateway: Gửi yêu cầu tạo thanh toán kèm IPN Webhook URL
    Gateway-->>OmniGo: Trả về PaymentUrl (Chứa mã QR thanh toán)
    OmniGo-->>User: Điều hướng mở App MoMo/VNPay hoặc quét mã QR

    %% 2. XÁC NHẬN THANH TOÁN
    Note over User, Gateway: 2. Người dùng thanh toán trên App Ngân hàng / Ví điện tử
    User->>Gateway: Xác nhận chuyển tiền trên App MoMo / VNPay
    Gateway-->>User: Redirect về ứng dụng: "Giao dịch đang chờ hệ thống xác nhận..."

    %% 3. IPN WEBHOOK XÁC THỰC NGẦM
    Note over Gateway, OmniGo: 3. Kênh IPN Webhook ngầm đối soát tự động (Quan trọng nhất)
    Gateway->>OmniGo: POST /api/v1/payments/momo/ipn (Kèm chữ ký số & resultCode)
    OmniGo->>OmniGo: Kiểm tra chữ ký HMAC-SHA256 & Kiểm tra Idempotent chống trùng
    alt Giao dịch thành công (resultCode == 0)
        OmniGo->>OmniGo: Cập nhật Transaction -> SUCCESS & Tự động cộng tiền ví / duyệt đơn
        OmniGo-->>Gateway: HTTP 204 No Content (Xác nhận xử lý thành công)
        OmniGo-->>User: Bắn thông báo: "Giao dịch nạp tiền / thanh toán thành công!"
    else Giao dịch thất bại / Chữ ký sai
        OmniGo->>OmniGo: Cập nhật Transaction -> FAILED
        OmniGo-->>Gateway: Phản hồi mã lỗi tương ứng
    end
```

---

### 2.4. Sơ Đồ Tuần Tự 4: Kiểm Soát Xác Thực, Phân Quyền & Ngăn Chặn Lỗ Hổng IDOR

```mermaid
sequenceDiagram
    autonumber
    actor Client as Client (Mobile / Web)
    participant Gateway as API Gateway (8080)
    participant Redis as Redis Cache
    participant Service as Microservices Nội Bộ

    %% 1. XÁC THỰC TOKEN TẠI CỬA NGÕ
    Note over Client, Gateway: 1. Kiểm tra xác thực & Blacklist tại API Gateway
    Client->>Gateway: Gửi Request (Header: Authorization: Bearer <JWT_Token>)
    Gateway->>Gateway: Validate cấu trúc & Hạn dùng JWT
    Gateway->>Redis: Kiểm tra token trong Blacklist (jwt_blacklist:{token})
    alt Token hết hạn hoặc nằm trong Blacklist (Đã đăng xuất)
        Gateway-->>Client: 401 Unauthorized ("Token không hợp lệ hoặc đã hết hạn")
    else Token hợp lệ & Tài khoản hoạt động
        Gateway->>Gateway: Trích xuất Identity: userId, role, phoneNumber
        Gateway->>Service: Forward Request kèm Headers nội bộ:<br>X-User-Id, X-User-Role, X-User-Phone
        
        %% 2. CHỐNG IDOR TẠI SERVICE NỘI BỘ
        Note over Service, Client: 2. Kiểm soát phân quyền & Ngăn chặn lộ dữ liệu (IDOR)
        alt Client gọi thông tin chính mình (/me)
            Service-->>Client: 200 OK (Truy vấn dữ liệu theo đúng X-User-Id của người gọi)
        else Client gọi API có ID (/users/{id})
            alt Không phải ADMIN và X-User-Id != id (Cố tình xem dữ liệu người khác)
                Service-->>Client: 403 Forbidden ("IDOR Blocked - Bạn không có quyền truy cập")
            else Là ADMIN hoặc chính chủ của ID
                Service-->>Client: 200 OK (Trả về thông tin chi tiết)
            end
        end
    end
```

---

### 2.5. Sơ Đồ Tuần Tự 5: Vòng Đời Đánh Giá Đơn Đồ Ăn & Phản Hồi Nhà Hàng (Food Order Review Lifecycle)
> **Ràng buộc thời gian & số lượng ảnh:**
> 1. **Giới hạn số lượng ảnh:** Tối đa **5 ảnh** trên mỗi đánh giá. Cả Frontend và Backend đều chặn nếu vượt quá.
> 2. **Khóa tạo mới sau 1 tuần:** Khách chỉ có thể gửi đánh giá cho đơn `COMPLETED` trong vòng **7 ngày (1 tuần)**.
> 3. **Khóa chỉnh sửa sau 48 giờ:** Khách có thể sửa đánh giá trong vòng **48 giờ** kể từ khi tạo (`createdAt + 48h`). Sau 48h tự động chuyển sang chỉ xem (`readOnly: true`).
> 4. **Phân quyền Quản trị viên (Admin):** Admin chỉ có quyền xem (`readOnly`), không có quyền chỉnh sửa.

```mermaid
sequenceDiagram
    autonumber
    actor Customer as Khách Hàng (App)
    participant OmniGo as Hệ Thống OmniGo (Backend)
    actor Merchant as Quán Ăn (Merchant)
    actor Admin as Quản Trị Viên (Admin)

    %% 1. TẢI ẢNH & GỬI ĐÁNH GIÁ (TRONG VÒNG 1 TUẦN)
    Note over Customer, OmniGo: 1. Tải ảnh & Gửi đánh giá (Tối đa 5 ảnh - Hạn chót: 1 tuần từ khi hoàn tất)
    opt Có tải ảnh món ăn thực tế
        Customer->>OmniGo: POST /api/v1/food-reviews/upload-image (Tối đa 5 ảnh, định dạng PNG/JPG/WEBP)
        OmniGo-->>Customer: Trả về danh sách URL ảnh Cloudinary
    end

    Customer->>OmniGo: POST /api/v1/food-reviews (orderId, rating quán, reviewPhotos <= 5, rating tài xế)
    alt Đơn hàng hoàn thành quá 1 tuần (now > completedAt + 7 ngày)
        OmniGo-->>Customer: 400 Bad Request ("Đơn hàng đã hoàn tất quá 1 tuần, không thể đánh giá")
    else Hợp lệ
        OmniGo->>OmniGo: Lưu đánh giá mới (Hạn chỉnh sửa: createdAt + 48h)
        OmniGo->>OmniGo: Tự động tính lại điểm sao (rating) và tổng số đánh giá (reviewCount) của quán
        OmniGo-->>Customer: 201 Created (Gửi đánh giá thành công)
        OmniGo-->>Merchant: Thông báo: "Quán có đánh giá mới từ khách hàng!"
    end

    %% 2. CHỈNH SỬA ĐÁNH GIÁ (TRONG VÒNG 48 GIỜ)
    Note over Customer, OmniGo: 2. Khách chỉnh sửa đánh giá (Chỉ cho phép trong vòng 48 giờ từ khi tạo)
    Customer->>OmniGo: PUT /api/v1/food-reviews/{id} (Cập nhật bình luận / số sao / tối đa 5 ảnh)
    alt Đã quá thời hạn 48 giờ (now > createdAt + 48h)
        OmniGo-->>Customer: 400 Bad Request ("Đã quá thời hạn 48 giờ để chỉnh sửa - Khóa chỉ xem")
    else Còn trong thời hạn 48 giờ
        OmniGo->>OmniGo: Cập nhật đánh giá & Đồng bộ lại điểm sao quán ăn
        OmniGo-->>Customer: 200 OK (Cập nhật thành công)
    end

    %% 3. QUÁN PHẢN HỒI & ADMIN GIÁM SÁT
    Note over Merchant, Admin: 3. Quán gửi phản hồi & Admin giám sát ở chế độ chỉ xem
    Merchant->>OmniGo: POST /api/v1/food-reviews/{id}/reply ("Cảm ơn quý khách đã ủng hộ quán!")
    OmniGo-->>Customer: Thông báo: "Nhà hàng đã phản hồi nhận xét của bạn!"
    Admin->>OmniGo: GET /api/v1/food-reviews/order/{orderId} -> Xem chi tiết (readOnly: true, không thể sửa)
```

---

## 3. BẢNG MÁY TRẠNG THÁI CHUẨN XÁC THEO CODEBASE (STATE MACHINES)

### 3.1. Máy Trạng Thái Cuốc Xe (`BookingStatus`)
Khớp chính xác với enum `com.trung.bookingservice.util.enums.BookingStatus`:

| Trạng thái (`BookingStatus`) | Ý nghĩa | Điều kiện kích hoạt & Mô tả hành vi hệ thống |
| :--- | :--- | :--- |
| **`PENDING`** | Chờ tài xế nhận cuốc | Khách hàng bấm "Đặt chuyến". Cuốc xe được tạo trong DB, hệ thống quét toạ độ Redis Geo và phát thông báo đếm ngược 20 giây tới các tài xế khả dụng gần nhất. |
| **`ACCEPTED`** | Tài xế đã nhận cuốc | Tài xế bấm nhận chuyến trong thời gian đếm ngược 20s (giữ bằng Redis Lock). Bắn WebSocket thông tin tài xế cho khách hàng, mở kênh Live Tracking. |
| **`ARRIVED`** | Đã đến điểm đón | Tài xế di chuyển đến điểm đón của khách. Bắt buộc kiểm tra Geofencing: $\text{Distance}(\text{Driver}, \text{Pickup}) \le 50\text{m}$. Hệ thống gửi thông báo khách ra xe. |
| **`IN_PROGRESS`** | Đang chở khách | Khách đã lên xe, tài xế bấm bắt đầu hành trình. Hệ thống tính toán thời gian di chuyển thực tế. |
| **`COMPLETED`** | Hoàn thành chuyến xe | Tài xế chở khách đến điểm trả (khoảng cách $\le 50\text{m}$) và bấm kết thúc. Xuất bản sự kiện Kafka `booking-completed-topic` để thanh toán và thu hoa hồng. |
| **`CANCELLED`** | Cuốc xe đã bị hủy | Chuyến đi bị hủy bởi Khách hàng (trước khi đón) hoặc Tài xế hủy chuyến, hoặc hệ thống hủy khi hết thời gian chờ mà không tìm được tài xế. |

---

### 3.2. Máy Trạng Thái Đơn Đồ Ăn (`OrderStatus`)
Khớp chính xác với enum `com.trung.fooddeliveryservice.util.enums.OrderStatus`:

| Trạng thái (`OrderStatus`) | Ý nghĩa | Điều kiện kích hoạt & Mô tả hành vi hệ thống |
| :--- | :--- | :--- |
| **`AWAITING_PAYMENT`** | Chờ thanh toán online | Khách hàng đặt đơn chọn phương thức thanh toán trực tuyến (`WALLET`, `MOMO`, `VNPAY`). Đơn hàng chưa gửi sang Nhà hàng để tránh rủi ro quỵt tiền / thiếu số dư. Khi thanh toán thành công chuyển sang `PENDING`. Khách có thể chủ động chuyển sang trả tiền mặt (`switch-to-cash`). |
| **`PENDING`** | Chờ nhà hàng tiếp nhận | Đơn đặt món đã hợp lệ (đã thanh toán hoặc chọn trả tiền mặt `CASH`), hệ thống gửi chuông báo WebSocket tới màn hình quản lý đơn của Nhà Hàng. |
| **`ACCEPTED`** | Nhà hàng đã tiếp nhận | Chủ quán/Bếp bấm nút tiếp nhận đơn hàng trên ứng dụng/web quản lý của quán. |
| **`PREPARING`** | Đang nấu món | Nhà hàng bắt đầu chế biến món ăn. Tại trạng thái này, khách hàng **không thể tự ý hủy đơn miễn phí**. Hệ thống quét tìm tài xế giao hàng lân cận qua Kafka. |
| **`READY_FOR_PICKUP`** | Món đã nấu xong | Bếp nấu và đóng gói xong, bấm thông báo sẵn sàng để tài xế có thể nhận đồ ăn ngay khi đến quán. |
| **`DELIVERING`** | Đang giao hàng | Tài xế có mặt tại quán (GPS $\le 50\text{m}$), nhận túi thức ăn và bắt đầu di chuyển tới địa chỉ của khách hàng (Live Tracking). |
| **`COMPLETED`** | Giao hàng thành công | Tài xế giao đồ ăn tận tay khách (GPS $\le 50\text{m}$) và bấm hoàn thành đơn. Kích hoạt quyết toán dòng tiền: quán nhận tiền món, tài xế nhận phí ship. |
| **`CANCELLED`** | Đơn hàng đã hủy | Bị hủy bởi Khách hàng, Quán ăn, Tài xế hoặc Hệ thống. Lưu vết đầy đủ `cancelledBy`, `cancelReason`, và `cancelReasonCode`. |
| **`REJECTED`** | Nhà hàng từ chối | Nhà hàng bấm từ chối nhận đơn ngay từ đầu (do hết món, quá tải hoặc sắp đến giờ đóng cửa). Tự động hoàn tiền ví nếu đã thanh toán. |
| **`NO_DRIVER_FOUND`** | Không tìm thấy tài xế | Hệ thống quét trong bán kính tối đa mà không có tài xế nào nhận giao đơn. Hệ thống hỗ trợ Quán bấm quét tìm lại (`retry-driver`, tối đa 3 lần). Nếu vẫn không có tài xế thì chuyển sang `CANCELLED`. |

---

### 3.3. Các Enum Trạng Thái Khác Được Sử Dụng Trong Hệ Thống

#### A. Trạng thái Hoạt động của Tài xế (`DriverStatus`)
*File: `com.trung.userdriverservice.util.enums.DriverStatus`*
* **`OFFLINE`**: Tài xế đang nghỉ ngơi, tắt ứng dụng hoặc ngắt kết nối (vị trí bị xóa khỏi Redis Geo).
* **`ONLINE`**: Tài xế đang trực tuyến, sẵn sàng nhận cuốc xe hoặc đơn giao đồ ăn.
* **`BUSY`**: Tài xế đang thực hiện cuốc xe chở khách (`IN_PROGRESS`) hoặc đang giao đồ ăn (`DELIVERING`), tạm thời không nhận thêm cuốc mới.

#### B. Trạng thái Phê duyệt Tài xế (`ApprovalStatus`)
*File: `com.trung.userdriverservice.util.enums.ApprovalStatus`*
* **`PENDING_APPROVAL`**: Hồ sơ tài xế mới đăng ký, đang chờ Admin kiểm tra giấy tờ xe, bằng lái. Chưa thể bật `ONLINE`.
* **`APPROVED`**: Hồ sơ đã được Admin phê duyệt, tài xế có thể kích hoạt nhận cuốc.
* **`REJECTED`**: Hồ sơ bị từ chối do giấy tờ không hợp lệ. Tài xế cần gửi lại hồ sơ qua API `/resubmit`.

#### C. Vai trò Người dùng (`Role`)
*File: `com.trung.userdriverservice.util.enums.Role`*
* **`CUSTOMER`**: Khách hàng sử dụng dịch vụ.
* **`DRIVER`**: Đối tác tài xế vận chuyển và giao hàng.
* **`RESTAURANT`**: Đối tác chủ quán ăn / nhà hàng.
* **`ADMIN`**: Quản trị viên vận hành hệ thống.

#### D. Trạng thái Giao dịch Tài chính (`TransactionStatus`)
*File: `com.trung.paymentservice.util.enums.TransactionStatus`*
* **`PENDING`**: Giao dịch đang chờ thanh toán hoặc đang chờ phản hồi IPN từ cổng MoMo/VNPay.
* **`SUCCESS`**: Giao dịch thanh toán/nạp tiền thành công, số dư ví đã được cập nhật.
* **`FAILED`**: Giao dịch thất bại do số dư không đủ hoặc lỗi ngân hàng.
* **`CANCELED`**: Giao dịch bị hủy bỏ (lưu ý: mã nguồn định nghĩa `CANCELED` với 1 chữ 'L').

#### E. Loại Giao dịch Ví (`TransactionType`)
*File: `com.trung.paymentservice.util.enums.TransactionType`*
* **`DEPOSIT`**: Nạp tiền vào ví qua cổng thanh toán trực tuyến.
* **`TRIP_PAYMENT`**: Khách hàng trừ tiền ví thanh toán cước chuyến xe.
* **`TRIP_INCOME`**: Tài xế nhận cộng tiền doanh thu chuyến xe vào ví.
* **`COMMISSION_FEE`**: Hệ thống tự động khấu trừ 20% phí hoa hồng sàn từ ví tài xế.
* **`WITHDRAWAL`**: Tài xế rút tiền từ ví doanh thu về tài khoản ngân hàng liên kết.
* **`FOOD_PAYMENT`**: Thanh toán tiền đơn hàng đồ ăn qua ví.
* **`FOOD_DELIVERY_INCOME`**: Tiền công giao hàng cộng vào ví của tài xế giao món.
* **`REFUND`**: Hoàn tiền về ví cho khách hàng khi đơn/cuốc bị hủy.

#### F. Phương thức Thanh toán (`PaymentMethod`)
*File: `com.trung.paymentservice.util.enums.PaymentMethod`*
* **`CASH`**: Thanh toán bằng tiền mặt khi hoàn tất.
* **`WALLET`**: Thanh toán trừ trực tiếp từ số dư ví điện tử OmniGo.
* **`MOMO`**: Thanh toán trực tuyến qua cổng ví điện tử MoMo.
* **`VNPAY`**: Thanh toán trực tuyến qua cổng VNPay.

#### G. Tác nhân Hủy Đơn Đồ Ăn (`OrderCancelledBy`)
*File: `com.trung.fooddeliveryservice.util.enums.OrderCancelledBy`*
* **`CUSTOMER`**: Khách hàng chủ động hủy đơn (khi đơn đang chờ duyệt `AWAITING_PAYMENT` hoặc `PENDING`).
* **`RESTAURANT`**: Quán ăn từ chối nhận đơn hoặc hủy đơn do hết nguyên liệu / quá tải.
* **`DRIVER`**: Tài xế hủy nhận đơn giao do sự cố phương tiện hoặc thời tiết.
* **`SYSTEM`**: Hệ thống tự động hủy đơn khi hết thời gian tìm tài xế hoặc quán không xác nhận kịp thời.

#### H. Mã Lý Do Hủy Đơn (`OrderCancelReason`)
*File: `com.trung.fooddeliveryservice.util.enums.OrderCancelReason`*
* **Nhóm Khách hàng (`CUSTOMER_`):**
  * `CUSTOMER_CHANGE_MIND`: Đổi ý không muốn đặt nữa.
  * `CUSTOMER_WRONG_ADDRESS`: Đặt nhầm địa chỉ nhận hàng.
  * `CUSTOMER_PAYMENT_FAILED`: Thanh toán trực tuyến thất bại hoặc không đủ số dư ví.
* **Nhóm Quán ăn (`RESTAURANT_`):**
  * `RESTAURANT_OUT_OF_STOCK`: Hết món / hết nguyên liệu chế biến.
  * `RESTAURANT_OVERLOADED`: Quán đang quá tải, không kịp phục vụ.
  * `RESTAURANT_CLOSING`: Quán sắp đến giờ đóng cửa.
  * `RESTAURANT_OTHER`: Lý do khác từ phía nhà hàng.
* **Nhóm Tài xế (`DRIVER_`):**
  * `DRIVER_ACCIDENT`: Gặp sự cố va chạm giao thông.
  * `DRIVER_VEHICLE_BROKEN`: Phương tiện bị hỏng hóc, thủng lốp.
  * `DRIVER_WEATHER`: Thời tiết mưa bão nghiêm trọng, ngập lụt.
  * `DRIVER_RESTAURANT_CLOSED`: Đến quán nhưng quán đóng cửa không hoạt động.
  * `DRIVER_OTHER`: Sự cố phát sinh khác từ tài xế.
* **Nhóm Hệ thống (`SYSTEM_`):**
  * `SYSTEM_TIMEOUT_FINDING_DRIVER`: Hết số lần quét tìm lại (tối đa 3 lần) mà không tìm thấy tài xế nhận giao.
  * `SYSTEM_TIMEOUT_RESTAURANT_CONFIRM`: Quá hạn thời gian quy định mà quán không xác nhận đơn.

---

### 3.4. Báo Cáo Doanh Thu & Bộ Chỉ Số KPI Nhà Hàng (Merchant Business Metrics)
Nhằm phục vụ phân hệ Báo cáo kinh doanh chuyên biệt cho Quán ăn (`/merchant/analytics`), hệ thống chuẩn hóa 4 chỉ số cốt lõi:
1. **Doanh Thu Hoàn Tất (Completed Food Revenue):**
   $$\text{Doanh Thu Thực Thu} = \sum_{\text{COMPLETED}} (\text{totalPrice} - \text{deliveryFee})$$
   *(Chỉ tính tiền món ăn thực tế quán nhận được, loại trừ phí giao hàng của tài xế).*
2. **Tỉ Lệ Đơn Thành Công (% Success Rate):**
   $$\text{Tỉ Lệ Thành Công} = \frac{\text{Số đơn COMPLETED}}{\text{Số đơn COMPLETED} + \text{Số đơn CANCELLED/REJECTED}} \times 100\%$$
3. **Số Món Đã Bán (Total Dishes Sold):**
   Tổng hợp số lượng từng phần ăn (`quantity`) của toàn bộ các mục món trong các đơn `COMPLETED`.
4. **Giá Trị Trung Bình Mỗi Đơn (Average Order Value - AOV):**
   $$\text{AOV} = \frac{\text{Doanh Thu Thực Thu}}{\text{Số đơn COMPLETED}}$$
   *Ý nghĩa: Cho biết mức chi tiêu trung bình của một khách hàng trên mỗi lần đặt món thành công, làm cơ sở để quán xây dựng combo món ăn và chương trình khuyến mãi upsell.*

---

## 4. QUY TẮC RÀNG BUỘC KỸ THUẬT & CHỐNG GIAN LẬN (INTEGRITY RULES)

### 4.1. Khóa Chặt Vị Trí Bằng GPS Geofencing ($\le 50\text{m}$)
* **Tại bước tài xế bấm đã đến điểm đón (`ARRIVED`):**
  $$\text{HaversineDistance}(\text{DriverGPS}, \text{PickupGPS}) \le 50\text{m}$$
* **Tại bước hoàn thành chuyến đi / giao món (`COMPLETED`):**
  $$\text{HaversineDistance}(\text{DriverGPS}, \text{DropoffGPS}) \le 50\text{m}$$
* Nếu tài xế đứng cách vị trí quy định $> 50\text{m}$ mà bấm xác nhận, Backend lập tức từ chối và trả về mã lỗi HTTP `400 Bad Request` (*"Bạn chưa có mặt tại điểm quy định, vui lòng di chuyển đến gần hơn"*).

### 4.2. Cơ Chế Chống Race Condition Bằng Redis Lock (20 giây)
* Khóa phân tán: `SET lock:driver:{driverId} {bookingId} NX EX 20`
* Đảm bảo tính nguyên tử (Atomic): Một tài xế chỉ nhận được tối đa 1 lời mời cuốc xe tại một thời điểm, triệt tiêu hoàn toàn lỗi tranh chấp cuốc (Race Condition).
* Hết thời gian 20s không phản hồi, key Redis tự động giải phóng (TTL Expired), hệ thống tự động gán tài xế vào danh sách loại trừ (Blacklist) của cuốc này và chuyển lời mời cho tài xế gần tiếp theo.

### 4.3. Quy Tắc Ràng Buộc Khóa Đánh Giá Đơn Hàng (Food Review Locking Rules)
1. **Khóa tạo mới sau 1 tuần (7 ngày):**
   * Đơn hàng bắt buộc phải ở trạng thái `COMPLETED`.
   * Thời gian hoàn thành tính theo: `order.updatedAt != null ? order.updatedAt : order.createdAt`.
   * Nếu $\text{Thời gian hiện tại} > \text{Thời gian hoàn tất} + 7\text{ ngày}$, hệ thống lập tức chặn tạo mới với lỗi `400 Bad Request` (*"Đơn hàng đã hoàn thành quá 1 tuần (7 ngày), không thể gửi đánh giá mới"*).
   * Trên giao diện khách hàng: Nút đánh giá chuyển thành nhãn xám: `🔒 Đơn hoàn thành quá 1 tuần, hết hạn đánh giá`.
2. **Khóa chỉnh sửa sau 48 giờ (Tính chuẩn xác theo `createdAt`):**
   * Khách hàng gửi đánh giá lần đầu sẽ có 48 giờ để sửa đổi nội dung hoặc số sao nếu có thay đổi cảm nhận.
   * Hạn chót thực tế được tính chuẩn xác theo: $\text{effectiveLimit} = \text{createdAt} + 48\text{ giờ}$.
   * Nếu $\text{Thời gian hiện tại} > \text{effectiveLimit}$, hệ thống từ chối cập nhật với mã lỗi `400 Bad Request` (*"Đã quá thời hạn 48 giờ kể từ khi tạo để chỉnh sửa đánh giá này"*).
   * Trên giao diện: Ẩn hoàn toàn nút *"Lưu Thay Đổi Đánh Giá"*, chỉ hiển thị nút *"Đóng"*, khóa chọn sao, ô nhập liệu và ẩn gợi ý tag.
3. **Giới hạn số lượng & định dạng tệp ảnh (Strict Max 5 Images Validation):**
   * Chỉ chấp nhận các tệp ảnh định dạng: `.png`, `.jpg`, `.jpeg`, `.webp` (MIME types: `image/png`, `image/jpeg`, `image/webp`).
   * **Chặn cứng tối đa 5 ảnh:** Cả giao diện Frontend (chọn tệp) và Backend (`FoodOrderReviewServiceImpl`) đều áp dụng chốt chặn nghiêm ngặt tối đa 5 ảnh. Mọi thao tác tải lên hoặc gửi payload quá 5 ảnh sẽ lập tức bị chặn với cảnh báo hoặc mã lỗi HTTP `400 Bad Request` (*"Chỉ được tải lên tối đa 5 ảnh cho mỗi đánh giá"*).
   * Hình ảnh được tải lên và lưu trữ an toàn trên dịch vụ đám mây Cloudinary.
4. **Phân quyền Xem & Phản hồi:**
   * **Chủ quán ăn (Merchant):** Chỉ có quyền xem đánh giá của quán mình và gửi phản hồi trả lời (`merchantReply`).
   * **Quản trị viên (Admin):** Chỉ có quyền xem chi tiết đánh giá đơn hàng (`readOnly = true`), tuyệt đối không có quyền gửi đánh giá thay hoặc chỉnh sửa đánh giá của khách hàng.
5. **Đồng bộ tự động Chỉ số Nhà hàng (Auto-sync Rating & Count):**
   * Mỗi khi có đánh giá mới hoặc đánh giá được cập nhật, hệ thống tự động tính lại điểm trung bình sao `rating` (làm tròn 1 chữ số thập phân) và tổng số lượt đánh giá `reviewCount` / `totalReviews` của nhà hàng thông qua repository `FoodOrderReviewRepository`.

---

## 5. HỢP ĐỒNG SỰ KIỆN TRUYỀN THÔNG APACHE KAFKA (KAFKA EVENT CONTRACTS)

Toàn bộ hệ thống Backend microservices của OmniGo hiện tại triển khai **chính xác 4 Kafka Topics** (đã được rà soát trực tiếp từ các Annotation `@KafkaListener` và lệnh `KafkaTemplate.send()` trong mã nguồn Java):

| Tên Topic Kafka | Event Class / Payload | Dịch vụ Gửi (Producer) | Dịch vụ Nhận (Consumer) | Mục đích & Xử lý nghiệp vụ thực tế trong mã nguồn |
| :--- | :--- | :--- | :--- | :--- |
| **`driver-registered-topic`** | `DriverRegisteredEvent`<br>`{"driverId": Long}` | `user-driver-service`<br>`DriverServiceImpl.java` (Line 79) | `payment-service`<br>`WalletServiceImpl.java` (Line 40) | Khi tài xế mới đăng ký thành công qua API `/api/v1/drivers/register`, event được bắn đi. `payment-service` lắng nghe và tự động gọi `getOrCreateWallet(driverId, UserType.DRIVER)` để khởi tạo ví tiền với số dư 0 VNĐ. |
| **`booking-completed-topic`** | `BookingCompletedEvent`<br>`{"bookingId": Long, "driverId": Long, "customerId": Long, "amount": Double}` | `booking-service`<br>`BookingServiceImpl.java` (Line 483) | `payment-service`<br>`WalletServiceImpl.java` (Line 104) | Khi cuốc xe hoàn thành (`COMPLETED`), `booking-service` phát sự kiện. `payment-service` tự động tính và trích trừ **20% phí hoa hồng sàn** (`COMMISSION_FEE = amount * 0.20`) từ ví của tài xế. Có cơ chế kiểm tra idempotent chống trừ phí 2 lần. |
| **`FIND_DRIVER_FOR_FOOD_ORDER`** | `FindDriverForFoodOrderEvent`<br>`{"orderId": Long, "customerId": Long, "restaurantId": Long, "restaurantName": String, "restaurantAddress": String, "restaurantLatitude": Double, "restaurantLongitude": Double, "dropOffAddress": String, "dropOffLatitude": Double, "dropOffLongitude": Double, "totalPrice": BigDecimal, "deliveryFee": BigDecimal, "paymentMethod": String, "isPaid": Boolean}` | `food-delivery-service`<br>`FoodEventPublisher.java` (Line 23) | `booking-service`<br>`FoodOrderKafkaConsumer.java` (Line 17) | Khi nhà hàng chuyển trạng thái đơn sang chế biến (`PREPARING`), phát sự kiện yêu cầu điều phối tài xế. `booking-service` nhận event, dùng Redis Geo quét các tài xế trực tuyến trong bán kính quanh nhà hàng để gửi lời mời giao hàng. |
| **`DRIVER_ASSIGNED_TO_FOOD_ORDER`** | `DriverAssignedToFoodOrderEvent`<br>`{"orderId": Long, "driverId": Long, "assignedAt": LocalDateTime}` | `booking-service`<br>`FoodDeliveryDispatchServiceImpl.java` (Line 208) | `food-delivery-service`<br>`FoodOrderDriverAssignedConsumer.java` (Line 17) | Khi tài xế bấm nhận đơn giao đồ ăn hoặc hệ thống ghép cuốc thành công, `booking-service` gửi sự kiện này ngược lại cho `food-delivery-service` để cập nhật `driverId` vào bảng `food_orders` và thông báo cho khách hàng. |
