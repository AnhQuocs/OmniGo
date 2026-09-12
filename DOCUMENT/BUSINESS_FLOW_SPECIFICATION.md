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

## 2. CÁC SƠ ĐỒ TUẦN TỰ CHI TIẾT NHẤT (SEQUENCE DIAGRAMS)

---

### 2.1. Sơ Đồ Tuần Tự 1: Vòng Đời Đặt Xe & Điều Phối Chuyến Đi (OmniRide Lifecycle)
> **Trạng thái thực tế trong mã nguồn (`BookingStatus`):** `PENDING` $\rightarrow$ `ACCEPTED` $\rightarrow$ `ARRIVED` $\rightarrow$ `IN_PROGRESS` $\rightarrow$ `COMPLETED` (hoặc `CANCELLED`).

```mermaid
sequenceDiagram
    autonumber
    actor Customer as Khách Hàng (App)
    participant Gateway as API Gateway (8080)
    participant Booking as Booking Service (8082)
    participant Pricing as Pricing Service (8085)
    participant Location as Location Service (8083)
    participant Redis as Redis (Geo & Lock)
    actor Driver as Tài Xế (App)
    participant Kafka as Apache Kafka
    participant Payment as Payment Service (8086)

    %% BƯỚC 1: TÍNH GIÁ ĐỘNG & ĐẶT XE
    Note over Customer, Pricing: 1. Khảo sát lộ trình & Định giá động (Surge Pricing)
    Customer->>Gateway: POST /api/v1/pricing/calculate (Pickup, Dropoff)
    Gateway->>Pricing: Forward Request
    Pricing->>Location: Lấy số lượng tài xế khả dụng quanh điểm đón (Redis Geo)
    Location-->>Pricing: Trả về mật độ tài xế (nearbyDriversCount)
    Pricing->>Pricing: Tính cước = (BasePrice + Km * PricePerKm) * SurgeMultiplier (Lưới H3)
    Pricing-->>Gateway: Trả về báo giá cước & Mức Surge (NORMAL, LIGHT, MODERATE, SEVERE)
    Gateway-->>Customer: Hiển thị bảng giá các loại xe (OmniBike, OmniCar)

    %% BƯỚC 2: TẠO BOOKING
    Note over Customer, Booking: 2. Khách hàng bấm "Đặt chuyến"
    Customer->>Gateway: POST /api/v1/bookings (startLat/Lng, endLat/Lng) [Token JWT]
    Gateway->>Booking: Forward + Header X-User-Id
    Booking->>Redis: Kiểm tra Spam Check (Key: spam:booking:{userId}, TTL: 5s)
    alt Bị Spam (Trong vòng 5 giây gọi liên tục)
        Redis-->>Booking: Key tồn tại
        Booking-->>Customer: 429 Too Many Requests ("Vui lòng chờ giây lát")
    else Hợp lệ
        Booking->>Booking: Khởi tạo cuốc xe (Status: PENDING)
        Booking-->>Customer: 201 Created (bookingId, status: PENDING)
    end

    %% BƯỚC 3: QUÉT REDIS GEO & ĐIỀU PHỐI (MATCHING)
    Note over Booking, Driver: 3. Quét toạ độ & Giữ chỗ tài xế bằng Redis Lock (20 giây)
    Booking->>Location: GET /api/v1/internal/locations/drivers/nearby?radius=3km
    Location->>Redis: GEOSEARCH drivers_geo FROMLONLAT (lat, lng) BYRADIUS 3km
    Redis-->>Location: Danh sách Driver ID gần nhất
    Location-->>Booking: Trả về danh sách ứng viên tài xế

    loop Cho từng ứng viên tài xế (ưu tiên khoảng cách gần nhất)
        Booking->>Redis: SET lock:driver:{driverId} bookingId NX EX 20
        alt Khóa Lock thành công (Tài xế chưa bị giữ chỗ)
            Booking->>Driver: Bắn WebSocket/FCM Popup: "Có cuốc xe mới!" (Đếm ngược 20s)
            alt Tài xế bấm "Nhận Cuốc" (Trong vòng 20s)
                Driver->>Gateway: PUT /api/v1/bookings/{id}/accept
                Gateway->>Booking: Forward + Header X-User-Id (DriverId)
                Booking->>Booking: Chuyển Status -> ACCEPTED
                Booking->>Redis: Xóa khóa tạm, lưu cuốc chính thức
                Booking-->>Customer: WebSocket thông báo: "Đã tìm thấy tài xế" (Tên, Xe, Biển số)
            else Tài xế bấm "Từ chối" hoặc Hết 20s (Timeout)
                Driver->>Booking: PUT /api/v1/bookings/{id}/driver-cancel (hoặc Timeout)
                Booking->>Redis: DEL lock:driver:{driverId}
                Booking->>Booking: Thêm driverId vào Blacklist cuốc này, luân chuyển cho tài xế kế tiếp
            end
        end
    end

    %% BƯỚC 4: LIVE TRACKING & HÀNH TRÌNH CHỞ KHÁCH
    Note over Driver, Customer: 4. Di chuyển đón khách & Geofencing Validation
    loop Cập nhật tọa độ di chuyển (Mỗi 2-3 giây)
        Driver->>Location: STOMP /ws-location (lat, lng, bearing)
        Location->>Redis: GEOADD drivers_geo lng lat driverId
        Location-->>Customer: WebSocket Broadcast vị trí xe di chuyển mượt mà trên bản đồ
    end

    Driver->>Gateway: PUT /api/v1/bookings/{id}/arrived
    Gateway->>Booking: Forward (Kiểm tra GPS: Distance <= 50m điểm đón)
    Booking->>Booking: Chuyển Status -> ARRIVED
    Booking-->>Customer: Thông báo: "Tài xế đã đến điểm đón"

    Driver->>Gateway: PUT /api/v1/bookings/{id}/start
    Gateway->>Booking: Forward
    Booking->>Booking: Chuyển Status -> IN_PROGRESS
    Booking-->>Customer: Thông báo: "Chuyến đi đang diễn ra"

    %% BƯỚC 5: HOÀN THÀNH & QUYẾT TOÁN TỰ ĐỘNG
    Note over Driver, Payment: 5. Hoàn thành chuyến xe & Thu chiết khấu qua Kafka
    Driver->>Gateway: PUT /api/v1/bookings/{id}/complete (paymentMethod)
    Gateway->>Booking: Forward (Kiểm tra GPS: Distance <= 50m điểm trả)
    Booking->>Booking: Chuyển Status -> COMPLETED
    Booking->>Kafka: Publish Event "booking-completed-topic" (bookingId, driverId, fare, paymentMethod)
    Booking-->>Customer: Mở màn hình Hóa đơn & Đánh giá sao ⭐

    Kafka->>Payment: Consume "booking-completed-topic"
    alt Thanh toán Ví / Thẻ Online
        Payment->>Payment: Trừ ví Khách hàng, Cộng ví Tài xế (trừ 15% hoa hồng sàn)
    else Thanh toán Tiền mặt (CASH)
        Payment->>Payment: Trừ 15% hoa hồng trực tiếp từ số dư ví ký quỹ của Tài xế
        alt Số dư ví tài xế âm > 50.000 VNĐ
            Payment->>Kafka: Publish "driver-low-balance-topic"
            Kafka->>Booking: Tự động khóa trạng thái nhận cuốc, ép tài xế OFFLINE
        end
    end
```

---

### 2.2. Sơ Đồ Tuần Tự 2: Vòng Đời Đặt Món 3 Bên (OmniFood Marketplace Lifecycle)
> **Trạng thái thực tế trong mã nguồn (`OrderStatus`):** `PENDING` $\rightarrow$ `ACCEPTED` $\rightarrow$ `PREPARING` $\rightarrow$ `READY_FOR_PICKUP` $\rightarrow$ `DELIVERING` $\rightarrow$ `COMPLETED` (hoặc `CANCELLED`, `REJECTED`, `NO_DRIVER_FOUND`).

```mermaid
sequenceDiagram
    autonumber
    actor Customer as Khách Hàng (App)
    participant Gateway as API Gateway (8080)
    participant FoodSvc as Food Delivery Service (8084)
    actor Merchant as Nhà Hàng (Web/POS)
    participant Location as Location Service (8083)
    actor Driver as Tài Xế Giao Hàng (App)
    participant Kafka as Apache Kafka
    participant Payment as Payment Service (8086)

    %% BƯỚC 1: ĐẶT MÓN
    Note over Customer, FoodSvc: 1. Khách chọn món & Tạo đơn giao
    Customer->>Gateway: POST /api/v1/food-orders (restaurantId, items, deliveryAddress, lat, lng)
    Gateway->>FoodSvc: Forward + Header X-User-Id
    FoodSvc->>FoodSvc: Tính Tổng tiền = Tiền món + Phí ship theo km - Voucher
    FoodSvc->>FoodSvc: Khởi tạo đơn (Status: PENDING)
    FoodSvc-->>Merchant: WebSocket Chuông báo: "Có đơn đặt món mới!" (#FD-XXXX)
    FoodSvc-->>Customer: 201 Created (orderId, status: PENDING)

    %% BƯỚC 2: QUÁN TIẾP NHẬN & CHẾ BIẾN
    Note over Merchant, Driver: 2. Quán duyệt đơn (ACCEPTED) & Chế biến (PREPARING)
    Merchant->>Gateway: PATCH /api/v1/food-orders/{id}/status {"status": "ACCEPTED"}
    Gateway->>FoodSvc: Forward
    FoodSvc->>FoodSvc: Chuyển Status -> ACCEPTED (Nhà hàng tiếp nhận đơn)
    FoodSvc-->>Customer: Thông báo: "Nhà hàng đã tiếp nhận đơn"

    Merchant->>Gateway: PATCH /api/v1/food-orders/{id}/status {"status": "PREPARING"}
    Gateway->>FoodSvc: Forward
    FoodSvc->>FoodSvc: Chuyển Status -> PREPARING (Bếp đang nấu món, khóa quyền tự hủy)
    FoodSvc-->>Customer: Thông báo: "Nhà hàng đang chuẩn bị món ăn"

    %% TÌM TÀI XẾ GIAO MÓN QUA KAFKA & BOOKING DISPATCH
    Note over FoodSvc, Kafka: Phát sự kiện tìm tài xế giao món qua Kafka
    FoodSvc->>Kafka: Publish Event "FIND_DRIVER_FOR_FOOD_ORDER" (FindDriverForFoodOrderEvent)
    Kafka->>Booking: Consume "FIND_DRIVER_FOR_FOOD_ORDER" (FoodOrderKafkaConsumer)
    Booking->>Location: Quét tài xế giao hàng quanh nhà hàng (radius = 3km từ Redis Geo)
    Location-->>Booking: Danh sách tài xế khả dụng
    Booking->>Driver: Phát thông tin đơn giao hàng tới Driver gần nhất
    Driver->>Booking: Tài xế chấp nhận nhận giao đơn món
    Booking->>Kafka: Publish Event "DRIVER_ASSIGNED_TO_FOOD_ORDER" (DriverAssignedToFoodOrderEvent)
    Kafka->>FoodSvc: Consume "DRIVER_ASSIGNED_TO_FOOD_ORDER" (FoodOrderDriverAssignedConsumer)
    FoodSvc->>FoodSvc: Gán driverId vào đơn hàng
    FoodSvc-->>Customer: Thông báo: "Đã có tài xế nhận giao đơn của bạn"

    %% BƯỚC 3: QUÁN BÁO NẤU XONG & TÀI XẾ LẤY MÓN
    Note over Driver, Merchant: 3. Món đã sẵn sàng (READY_FOR_PICKUP) & Giao hàng
    Merchant->>Gateway: PATCH /api/v1/food-orders/{id}/status {"status": "READY_FOR_PICKUP"}
    Gateway->>FoodSvc: Forward
    FoodSvc->>FoodSvc: Chuyển Status -> READY_FOR_PICKUP (Chờ tài xế lấy món)
    
    Driver->>Gateway: PATCH /api/v1/food-orders/{id}/driver-status {"status": "DELIVERING"}
    Gateway->>FoodSvc: Forward (Validate tọa độ Driver cách Restaurant <= 50m)
    FoodSvc->>FoodSvc: Chuyển Status -> DELIVERING (Đã lấy món, đang trên đường giao)
    FoodSvc-->>Customer: Thông báo: "Tài xế đã lấy món và đang di chuyển tới bạn"

    %% BƯỚC 4: GIAO HÀNG & HOÀN TẤT
    Note over Driver, Customer: 4. Giao tận tay khách & Hoàn tất (COMPLETED)
    loop Live Tracking giao đồ ăn
        Driver->>Location: Gửi tọa độ GPS
        Location-->>Customer: Cập nhật đường đi của shipper trên bản đồ
    end

    Driver->>Gateway: PATCH /api/v1/food-orders/{id}/driver-status {"status": "COMPLETED"}
    Gateway->>FoodSvc: Forward (Validate tọa độ Driver cách Khách <= 50m)
    FoodSvc->>FoodSvc: Chuyển Status -> COMPLETED
    FoodSvc-->>Customer: Mở màn hình Đánh giá riêng biệt: ⭐ Cho Quán & ⭐ Cho Tài xế
```

---

### 2.3. Sơ Đồ Tuần Tự 3: Nạp Tiền & Thanh Toán Trực Tuyến Qua Cổng MoMo / VNPay (IPN Webhook)
> **Trạng thái thực tế trong mã nguồn (`TransactionStatus`):** `PENDING` $\rightarrow$ `SUCCESS` (hoặc `FAILED`, `CANCELLED`).

```mermaid
sequenceDiagram
    autonumber
    actor Customer as Khách Hàng / Tài Xế
    participant Gateway as API Gateway (8080)
    participant Payment as Payment Service (8086)
    participant ExtGW as Cổng MoMo / VNPay
    participant DB as PostgreSQL (payment_db)
    participant Kafka as Apache Kafka

    Customer->>Gateway: POST /api/v1/payments/payment/create (method: MOMO/VNPAY, type: DEPOSIT/TRIP_PAYMENT, amount)
    Gateway->>Payment: Forward + Header X-User-Id
    Payment->>DB: Lưu bản ghi giao dịch (Status: PENDING, Mã: TXN_XXXX)
    Payment->>Payment: Ký số dữ liệu bằng HMAC-SHA256 (Khóa bí mật SecretKey)
    Payment->>ExtGW: Gửi API tạo giao dịch (partnerCode, orderId, amount, returnUrl, ipnUrl, signature)
    ExtGW-->>Payment: Trả về URL thanh toán / PayUrl (Chứa mã QR)
    Payment-->>Gateway: Trả về paymentUrl
    Gateway-->>Customer: Điều hướng mở App MoMo / VNPay hoặc hiển thị mã QR

    Customer->>ExtGW: Quét mã & Xác nhận chuyển tiền trên App MoMo / VNPay
    
    %% KÊNH ĐỒNG BỘ: RETURN URL (REDIRECT BROWSER)
    ExtGW-->>Customer: Điều hướng người dùng về Frontend: GET /api/v1/payments/momo/return
    Customer->>Gateway: Redirect hiển thị màn hình: "Giao dịch đang chờ xác nhận..."

    %% KÊNH BẤT ĐỒNG BỘ NỀN: IPN WEBHOOK (QUAN TRỌNG NHẤT)
    Note over ExtGW, Payment: Kênh IPN ngầm giữa máy chủ Cổng thanh toán và Payment Service
    ExtGW->>Gateway: POST /api/v1/payments/momo/ipn (Body: transId, resultCode, signature, extraData)
    Gateway->>Payment: Bypass Auth (Public Webhook URL) -> Forward tới Payment Service
    Payment->>Payment: Tái tạo chữ ký HMAC-SHA256 & So sánh với signature từ cổng
    alt Chữ ký không hợp lệ (Giả mạo)
        Payment-->>ExtGW: HTTP 400 Bad Request ("Chữ ký bảo mật không khớp")
    else Chữ ký hợp lệ & resultCode == 0 (Thành công)
        Payment->>DB: Kiểm tra trạng thái giao dịch hiện tại trong DB
        alt Giao dịch đã xử lý trước đó (Idempotent)
            Payment-->>ExtGW: HTTP 204 No Content
        else Giao dịch đang PENDING
            Payment->>DB: Cập nhật Transaction Status -> SUCCESS
            alt Giao dịch NẠP TIỀN (DEPOSIT)
                Payment->>DB: Cộng tiền vào Wallet của người dùng
            else Giao dịch THANH TOÁN ĐƠN MÓN (FOOD_PAYMENT)
                Payment->>Gateway: REST POST /api/v1/food-orders/{orderId}/paid
                Gateway->>FoodSvc: Cập nhật isPaid = true cho đơn hàng
            else Giao dịch THANH TOÁN CHUYẾN ĐI (TRIP_PAYMENT)
                Payment->>DB: Cộng tiền TRIP_INCOME cho ví tài xế & Trừ 20% COMMISSION_FEE
            end
            Payment-->>ExtGW: HTTP 204 No Content (Xác nhận đã xử lý IPN thành công)
        end
    end
```

---

### 2.4. Sơ Đồ Tuần Tự 4: Kiểm Soát Xác Thực, Phân Quyền & Ngăn Chặn Lỗ Hổng IDOR

```mermaid
sequenceDiagram
    autonumber
    actor Client as Client (Mobile / Web)
    participant Gateway as API Gateway (GlobalFilter)
    participant Redis as Redis Cache
    participant UserSvc as User-Driver Service (8081)
    participant DB as PostgreSQL (user_db)

    Client->>Gateway: Gửi Request (Header: Authorization: Bearer <JWT_Token>)
    Gateway->>Gateway: Kiểm tra định dạng Header & Cấu trúc JWT Token
    alt Token sai định dạng / Hết hạn (Expired)
        Gateway-->>Client: 401 Unauthorized ("Access Token không hợp lệ hoặc đã hết hạn")
    else Token hợp lệ
        Gateway->>Redis: Kiểm tra token trong Blacklist: EXISTS jwt_blacklist:{token}
        alt Token nằm trong Blacklist (Đã đăng xuất)
            Redis-->>Gateway: true
            Gateway-->>Client: 401 Unauthorized ("Token đã bị vô hiệu hóa")
        else Token chưa bị Blacklist
            Gateway->>Gateway: Giải mã Claims lấy userId, role, phoneNumber
            Gateway->>Redis: Kiểm tra tài khoản bị khóa: EXISTS user_locked:{userId}
            alt Tài khoản đang bị Admin khóa
                Redis-->>Gateway: true
                Gateway-->>Client: 401 Unauthorized ("Tài khoản của bạn đã bị khóa bởi Quản trị viên")
            else Tài khoản bình thường
                Gateway->>Gateway: Đính kèm Header mới vào Request:<br>X-User-Id = userId<br>X-User-Role = role<br>X-User-Phone = phoneNumber
                
                %% KIỂM SOÁT TẠI SERVICE NỘI BỘ
                alt Khách gọi API xem thông tin chính mình: GET /api/v1/users/me
                    Gateway->>UserSvc: Forward kèm Header X-User-Id
                    UserSvc->>DB: Truy vấn user theo X-User-Id
                    DB-->>UserSvc: Trả về thông tin chính chủ
                    UserSvc-->>Client: 200 OK (UserResponse)
                else Khách gọi API có Path ID: GET /api/v1/users/{id}
                    Gateway->>UserSvc: Forward kèm Path {id} và Header X-User-Id
                    UserSvc->>UserSvc: Đánh giá SpEL: hasRole('ADMIN') or #currentUserId == #id
                    alt Không phải Admin VÀ currentUserId != id (Cố tình đọc trộm dữ liệu người khác)
                        UserSvc-->>Client: 403 Forbidden ("Bạn không có quyền truy cập tài nguyên này - IDOR Blocked")
                    else Là Admin HOẶC Là chính chủ của ID đó
                        UserSvc->>DB: Truy vấn chi tiết theo {id}
                        DB-->>UserSvc: Dữ liệu hợp lệ
                        UserSvc-->>Client: 200 OK (UserResponse)
                    end
                end
            end
        end
    end
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
| **`PENDING`** | Chờ nhà hàng tiếp nhận | Đơn đặt món được tạo thành công, hệ thống gửi chuông báo WebSocket tới màn hình quản lý đơn của Nhà Hàng. |
| **`ACCEPTED`** | Nhà hàng đã tiếp nhận | Chủ quán/Bếp bấm nút tiếp nhận đơn hàng trên ứng dụng/web quản lý của quán. |
| **`PREPARING`** | Đang nấu món | Nhà hàng bắt đầu chế biến món ăn. Tại trạng thái này, khách hàng **không thể tự ý hủy đơn miễn phí**. Hệ thống quét tìm tài xế giao hàng lân cận. |
| **`READY_FOR_PICKUP`** | Món đã nấu xong | Bếp nấu và đóng gói xong, bấm thông báo sẵn sàng để tài xế có thể nhận đồ ăn ngay khi đến quán. |
| **`DELIVERING`** | Đang giao hàng | Tài xế có mặt tại quán (GPS $\le 50\text{m}$), nhận túi thức ăn và bắt đầu di chuyển tới địa chỉ của khách hàng (Live Tracking). |
| **`COMPLETED`** | Giao hàng thành công | Tài xế giao đồ ăn tận tay khách (GPS $\le 50\text{m}$) và bấm hoàn thành đơn. Kích hoạt sự kiện Kafka quyết toán dòng tiền 3 bên (Quán - Tài xế - Sàn). |
| **`CANCELLED`** | Đơn hàng đã hủy | Khách hủy đơn khi quán chưa nấu, hoặc tài xế/quán hủy đơn do sự cố. |
| **`REJECTED`** | Nhà hàng từ chối | Nhà hàng bấm từ chối nhận đơn (do hết món, quá tải hoặc sắp đến giờ đóng cửa). Tự động hoàn tiền ví cho khách. |
| **`NO_DRIVER_FOUND`** | Không tìm thấy tài xế | Hệ thống quét trong bán kính tối đa mà không có tài xế nào nhận giao đơn. Hệ thống tự động hủy đơn và hoàn tiền $100\%$ cho khách hàng. |

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

---

## 4. QUY TẮC RÀNG BUỘC KỸ THUẬT & CHỐNG GIAN LẬN (INTEGRITY RULES)

### 4.1. Khóa Chặt Vị Trí Bằng GPS Geofencing ($\le 50\text{m}$)
* **Tại bước tài xế bấm đã đến điểm đón (`ARRIVED`):**
  $$\text{HaversineDistance}(\text{Driver\_GPS}, \text{Pickup\_GPS}) \le 50\text{ mét}$$
* **Tại bước hoàn thành chuyến đi / giao món (`COMPLETED`):**
  $$\text{HaversineDistance}(\text{Driver\_GPS}, \text{Dropoff\_GPS}) \le 50\text{ mét}$$
* Nếu tài xế đứng cách vị trí quy định $> 50\text{m}$ mà bấm xác nhận, Backend lập tức từ chối và trả về mã lỗi HTTP `400 Bad Request` (*"Bạn chưa có mặt tại điểm quy định, vui lòng di chuyển đến gần hơn"*).

### 4.2. Cơ Chế Chống Race Condition Bằng Redis Lock (20 giây)
* Khóa phân tán: `SET lock:driver:{driverId} {bookingId} NX EX 20`
* Đảm bảo tính nguyên tử (Atomic): Một tài xế chỉ nhận được tối đa 1 lời mời cuốc xe tại một thời điểm, triệt tiêu hoàn toàn lỗi tranh chấp cuốc (Race Condition).
* Hết thời gian 20s không phản hồi, key Redis tự động giải phóng (TTL Expired), hệ thống tự động gán tài xế vào danh sách loại trừ (Blacklist) của cuốc này và chuyển lời mời cho tài xế gần tiếp theo.

---

## 5. HỢP ĐỒNG SỰ KIỆN TRUYỀN THÔNG APACHE KAFKA (KAFKA EVENT CONTRACTS)

Toàn bộ hệ thống Backend microservices của OmniGo hiện tại triển khai **chính xác 4 Kafka Topics** (đã được rà soát trực tiếp từ các Annotation `@KafkaListener` và lệnh `KafkaTemplate.send()` trong mã nguồn Java):

| Tên Topic Kafka | Event Class / Payload | Dịch vụ Gửi (Producer) | Dịch vụ Nhận (Consumer) | Mục đích & Xử lý nghiệp vụ thực tế trong mã nguồn |
| :--- | :--- | :--- | :--- | :--- |
| **`driver-registered-topic`** | `DriverRegisteredEvent`<br>`{"driverId": Long}` | `user-driver-service`<br>`DriverServiceImpl.java` (Line 79) | `payment-service`<br>`WalletServiceImpl.java` (Line 40) | Khi tài xế mới đăng ký thành công qua API `/api/v1/drivers/register`, event được bắn đi. `payment-service` lắng nghe và tự động gọi `getOrCreateWallet(driverId, UserType.DRIVER)` để khởi tạo ví tiền với số dư 0 VNĐ. |
| **`booking-completed-topic`** | `BookingCompletedEvent`<br>`{"bookingId": Long, "driverId": Long, "customerId": Long, "amount": Double}` | `booking-service`<br>`BookingServiceImpl.java` (Line 483) | `payment-service`<br>`WalletServiceImpl.java` (Line 104) | Khi cuốc xe hoàn thành (`COMPLETED`), `booking-service` phát sự kiện. `payment-service` tự động tính và trích trừ **20% phí hoa hồng sàn** (`COMMISSION_FEE = amount * 0.20`) từ ví của tài xế. Có cơ chế kiểm tra idempotent chống trừ phí 2 lần. |
| **`FIND_DRIVER_FOR_FOOD_ORDER`** | `FindDriverForFoodOrderEvent`<br>`{"orderId": Long, "customerId": Long, "restaurantId": Long, "restaurantName": String, "restaurantAddress": String, "restaurantLatitude": Double, "restaurantLongitude": Double, "dropOffAddress": String, "dropOffLatitude": Double, "dropOffLongitude": Double, "totalPrice": BigDecimal, "deliveryFee": BigDecimal, "paymentMethod": String, "isPaid": Boolean}` | `food-delivery-service`<br>`FoodEventPublisher.java` (Line 23) | `booking-service`<br>`FoodOrderKafkaConsumer.java` (Line 17) | Khi nhà hàng chuyển trạng thái đơn sang chế biến (`PREPARING`), phát sự kiện yêu cầu điều phối tài xế. `booking-service` nhận event, dùng Redis Geo quét các tài xế trực tuyến trong bán kính quanh nhà hàng để gửi lời mời giao hàng. |
| **`DRIVER_ASSIGNED_TO_FOOD_ORDER`** | `DriverAssignedToFoodOrderEvent`<br>`{"orderId": Long, "driverId": Long, "assignedAt": LocalDateTime}` | `booking-service`<br>`FoodDeliveryDispatchServiceImpl.java` (Line 208) | `food-delivery-service`<br>`FoodOrderDriverAssignedConsumer.java` (Line 17) | Khi tài xế bấm nhận đơn giao đồ ăn hoặc hệ thống ghép cuốc thành công, `booking-service` gửi sự kiện này ngược lại cho `food-delivery-service` để cập nhật `driverId` vào bảng `food_orders` và thông báo cho khách hàng. |
