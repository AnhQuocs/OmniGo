**TỔNG HỢP API DÀNH CHO MOBILE APP (OMNIGO)**

*Tài liệu Tách Riêng Phân Hệ Ứng Dụng Khách Hàng (Customer) & Tài Xế (Driver)*

*Ghi chú phạm vi: Tài liệu này được biên soạn độc lập cho đội ngũ phát triển Mobile App (Android / iOS). Tất cả các API quản trị hệ thống (Admin Portal) và nghiệp vụ quản lý món ăn / xác nhận bếp của Nhà hàng (Restaurant Web) đã được lược bỏ để tinh gọn, tập trung 100% vào nghiệp vụ người dùng cuối trên thiết bị di động.*

**PHẦN I: CÁC API DÙNG CHUNG TRÊN MOBILE APP (COMMON / AUTH & VÍ)**

*Các API trong phần này được sử dụng chung cho cả ứng dụng Khách hàng (Customer App) và ứng dụng Đối tác Tài xế (Driver App) để thực hiện xác thực, quản lý phiên và giao dịch tài chính.*

**1. Xác thực, Đăng nhập & Quản lý Phiên (/api/v1/auth & /api/v1/users)**

**\[POST\] /api/v1/auth/login**

- **Mô tả:** Đăng nhập tài khoản trên thiết bị di động bằng số điện thoại và mật khẩu. Trả về Access Token (JWT) và lưu Refresh Token vào Cookie an toàn.
- **Phân quyền / Header:** Public (Không yêu cầu đăng nhập trước)
- **Danh sách trường Request Body (JSON):**
  - **phoneNumber:** *(String)* \[Bắt buộc\] Số điện thoại đăng ký tài khoản (định dạng 10 chữ số)
  - **password:** *(String)* \[Bắt buộc\] Mật khẩu người dùng (tối thiểu 6 ký tự)
- **Kết quả trả về (Response):** LoginResponse (accessToken, refreshToken, user: {id, fullName, phoneNumber, role, email})

**\[POST\] /api/v1/auth/refresh**

- **Mô tả:** Cấp lại Access Token mới khi token hiện tại trên điện thoại hết hạn mà không cần bắt người dùng đăng nhập lại.
- **Phân quyền / Header:** Yêu cầu Refresh Token từ Cookie hoặc Header
- **Kết quả trả về (Response):** LoginResponse (accessToken mới, refreshToken mới)

**\[POST\] /api/v1/auth/logout**

- **Mô tả:** Đăng xuất tài khoản khỏi ứng dụng di động, vô hiệu hóa Access Token (đưa vào Redis Blacklist) và xóa sạch Cookie.
- **Phân quyền / Header:** Bearer Token (Header Authorization)
- **Kết quả trả về (Response):** String: 'Đăng xuất thành công'

**\[GET\] /api/v1/users/me**

- **Mô tả:** Lấy thông tin chi tiết của chính người dùng đang đăng nhập trên ứng dụng (Được Gateway inject Header X-User-Id, bảo mật tuyệt đối chống lỗi IDOR).
- **Phân quyền / Header:** Bearer Token (Yêu cầu đã đăng nhập)
- **Kết quả trả về (Response):** UserResponse (id, fullName, phoneNumber, email, role, status, createdAt)

**2. Quản lý Số dư Ví Điện Tử & Nạp Tiền (/api/v1/payments)**

**\[GET\] /api/v1/payments/wallet**

- **Mô tả:** Xem thông tin ví điện tử cá nhân và số dư khả dụng hiện tại (VNĐ) trên màn hình ví của App.
- **Phân quyền / Header:** Bearer Token (Header X-User-Id, X-User-Role do Gateway chuyển tiếp)
- **Kết quả trả về (Response):** WalletResponse (id, userId, userType: CUSTOMER/DRIVER, balance, status: ACTIVE)

**\[GET\] /api/v1/payments/transactions**

- **Mô tả:** Lấy danh sách lịch sử biến động số dư và các giao dịch nạp / trừ tiền / cộng thu nhập của ví cá nhân.
- **Phân quyền / Header:** Bearer Token (Header X-User-Id, X-User-Role)
- **Kết quả trả về (Response):** List\<Transaction\> (id, orderId, amount, transactionType: DEPOSIT/TRIP_PAYMENT/TRIP_INCOME/COMMISSION_FEE/FOOD_PAYMENT, status: SUCCESS/PENDING/FAILED/CANCELED, createdAt)

**\[POST\] /api/v1/payments/payment/create**

- **Mô tả:** Khởi tạo yêu cầu nạp tiền vào ví hoặc thanh toán chuyến đi/đơn món qua cổng MoMo hoặc VNPay. Trả về paymentUrl để mở App MoMo / VNPay hoặc hiển thị mã QR.
- **Phân quyền / Header:** Bearer Token (Header X-User-Id)
- **Tham số (Path / Query Params):** method (String: MOMO / VNPAY), type (String: DEPOSIT / TRIP_PAYMENT), amount (BigDecimal - Số tiền), bookingId (Long, optional), driverId (Long, optional)
- **Kết quả trả về (Response):** PaymentUrlResponse (paymentUrl: URL điều hướng cổng thanh toán, orderId: Mã giao dịch)

**\[POST\] /api/v1/payments/wallet/deposit**

- **Mô tả:** Nạp tiền trực tiếp vào ví cá nhân (dùng cho môi trường thử nghiệm Sandbox hoặc giao dịch tiền mặt tại quầy).
- **Phân quyền / Header:** Bearer Token (Header X-User-Id, X-User-Role)
- **Tham số (Path / Query Params):** amount (Double - Số tiền nạp), note (String, tùy chọn - Ghi chú nạp tiền)
- **Kết quả trả về (Response):** WalletResponse (Ví sau khi cộng tiền, số dư balance mới)

**\[POST\] /api/v1/payments/wallet/withdraw**

- **Mô tả:** Khách hàng tạo yêu cầu rút tiền từ số dư ví OmniGo về tài khoản ngân hàng liên kết.
- **Phân quyền / Header:** Bearer Token (Header X-User-Id, X-User-Role=CUSTOMER)
- **Tham số (Path / Query Params):** amount (Double - Số tiền muốn rút)
- **Kết quả trả về (Response):** HTTP 200 OK (Rút tiền thành công, trừ số dư ví)

**PHẦN II: TẬP LỆNH API DÀNH RIÊNG CHO KHÁCH HÀNG (CUSTOMER APP)**

*Toàn bộ các chức năng mà người dùng Khách hàng tương tác trên ứng dụng di động: Đăng ký tài khoản, Quét xe xung quanh trên bản đồ, Tính trước giá cước chuyến, Đặt xe, Hủy xe, Tìm kiếm quán ăn, Đặt món ăn giao tận nơi và Theo dõi đơn hàng theo thời gian thực.*

**1. Đăng ký Tài khoản Khách hàng Mới**

**\[POST\] /api/v1/users/register/customer**

- **Mô tả:** Khách hàng đăng ký tài khoản mới trên ứng dụng OmniGo. Tự động mã hóa mật khẩu bằng BCrypt và cấp quyền ROLE_CUSTOMER.
- **Phân quyền / Header:** Public (Không yêu cầu đăng nhập)
- **Danh sách trường Request Body (JSON):**
  - **fullName:** *(String)* \[Bắt buộc\] Họ và tên đầy đủ của khách hàng
  - **phoneNumber:** *(String)* \[Bắt buộc\] Số điện thoại (duy nhất, không trùng lặp)
  - **password:** *(String)* \[Bắt buộc\] Mật khẩu đăng nhập (tối thiểu 6 ký tự)
  - **email:** *(String)* \[Bắt buộc\] Địa chỉ email hợp lệ để nhận thông báo và hóa đơn điện tử
- **Kết quả trả về (Response):** LoginResponse (Tự động đăng nhập ngay sau khi đăng ký, trả về accessToken và thông tin User)

**2. Khảo sát Tài xế Xung Quanh (Bản đồ Khách hàng)**

**\[GET\] /api/v1/locations/nearby**

- **Mô tả:** Lấy danh sách các tài xế chở khách (Xe ôm / Taxi) đang trực tuyến (ONLINE) xung quanh vị trí khách hàng để vẽ biểu tượng xe di chuyển trên bản đồ.
- **Phân quyền / Header:** Public / Authenticated
- **Tham số (Path / Query Params):** longitude (Double - Kinh độ GPS khách), latitude (Double - Vĩ độ GPS khách), radius (Double, tùy chọn - Bán kính quét tính theo km, mặc định 5.0)
- **Kết quả trả về (Response):** List\<DriverLocationResponse\> (driverId, latitude, longitude, bearing, speed, status, distanceKm)

**\[GET\] /api/v1/locations/nearby-delivery-drivers**

- **Mô tả:** Lấy danh sách các tài xế giao đồ ăn (Shipper) đang hoạt động trong bán kính xung quanh khách hàng.
- **Phân quyền / Header:** Public / Authenticated
- **Tham số (Path / Query Params):** longitude (Double - Kinh độ), latitude (Double - Vĩ độ), radius (Double, mặc định 5.0 km)
- **Kết quả trả về (Response):** List\<DriverLocationResponse\> (Danh sách vị trí tài xế giao hàng lân cận)

**3. Ước tính Giá Cước Chuyến Xe (Dynamic Pricing)**

**\[POST\] /api/v1/pricing/calculate**

- **Mô tả:** Tính toán giá cước chuyến xe ước tính trước khi đặt. Áp dụng công thức định giá động đa biến: Quãng đường Haversine, Phí mở cửa, Hệ số nhu cầu giờ cao điểm, Thời tiết và Mật độ tài xế xung quanh.
- **Phân quyền / Header:** Public / Authenticated
- **Tham số (Path / Query Params):** nearbyDriversCount (Integer, tùy chọn - Số lượng tài xế quét được từ API nearby, mặc định 0)
- **Danh sách trường Request Body (JSON):**
  - **pickupLatitude:** *(Double)* \[Bắt buộc\] Vĩ độ điểm đón khách
  - **pickupLongitude:** *(Double)* \[Bắt buộc\] Kinh độ điểm đón khách
  - **dropoffLatitude:** *(Double)* \[Bắt buộc\] Vĩ độ điểm trả khách
  - **dropoffLongitude:** *(Double)* \[Bắt buộc\] Kinh độ điểm trả khách
  - **vehicleType:** *(String)* \[Bắt buộc\] Loại phương tiện khách chọn: 'BIKE' (Xe máy) hoặc 'CAR' (Ô tô)
  - **weatherCondition:** *(String)* \[Tùy chọn\] Điều kiện thời tiết: 'NORMAL' (Bình thường), 'RAIN' (Mưa), 'STORM' (Bão)
- **Kết quả trả về (Response):** PricingResponse (distanceKm: Quãng đường km, baseFare: Cước cơ sở, dynamicMultiplier: Hệ số giờ cao điểm, totalFare: Tổng giá cước cuối cùng VNĐ, estimatedMinutes: Thời gian di chuyển dự kiến)

**4. Nghiệp vụ Đặt Chuyến Xe Chở Khách (Ride-Hailing)**

**\[POST\] /api/v1/bookings**

- **Mô tả:** Khách hàng bấm 'Đặt Chuyến Xe'. Hệ thống khởi tạo cuốc xe ở trạng thái PENDING, khóa tài xế lân cận bằng Redis Lock và phát thông báo đếm ngược 20s tới tài xế gần nhất.
- **Phân quyền / Header:** Bearer Token (Header X-User-Id đóng vai trò customerId)
- **Danh sách trường Request Body (JSON):**
  - **pickupAddress:** *(String)* \[Bắt buộc\] Địa chỉ đón chi tiết (ví dụ: '123 Cầu Giấy, Hà Nội')
  - **dropoffAddress:** *(String)* \[Bắt buộc\] Địa chỉ trả khách chi tiết
  - **pickupLatitude:** *(Double)* \[Bắt buộc\] Tọa độ vĩ độ điểm đón
  - **pickupLongitude:** *(Double)* \[Bắt buộc\] Tọa độ kinh độ điểm đón
  - **dropoffLatitude:** *(Double)* \[Bắt buộc\] Tọa độ vĩ độ điểm trả
  - **dropoffLongitude:** *(Double)* \[Bắt buộc\] Tọa độ kinh độ điểm trả
  - **fare:** *(Double)* \[Bắt buộc\] Số tiền cước chuyến đi (đã chốt từ API tính giá trước đó)
  - **paymentMethod:** *(String)* \[Bắt buộc\] Phương thức thanh toán: 'CASH', 'WALLET', 'MOMO', 'VNPAY'
  - **vehicleType:** *(String)* \[Bắt buộc\] Loại xe: 'BIKE' hoặc 'CAR'
- **Kết quả trả về (Response):** BookingResponse (id: bookingId, customerId, status: 'PENDING', fare, pickupAddress, dropoffAddress, createdAt)

**\[PUT\] /api/v1/bookings/{bookingId}/cancel**

- **Mô tả:** Khách hàng hủy chuyến đi đã đặt (khi đang ở trạng thái PENDING hoặc ACCEPTED trước khi tài xế đón). Hệ thống giải phóng Redis Lock tài xế.
- **Phân quyền / Header:** Bearer Token (Header X-User-Id)
- **Tham số (Path / Query Params):** bookingId (Long - Mã cuốc xe trên Path)
- **Kết quả trả về (Response):** BookingResponse (status: 'CANCELLED')

**\[GET\] /api/v1/bookings/customer**

- **Mô tả:** Lấy toàn bộ danh sách lịch sử các chuyến xe đã đi (hoặc đã hủy) của khách hàng đang đăng nhập.
- **Phân quyền / Header:** Bearer Token (Header X-User-Id)
- **Kết quả trả về (Response):** ApiResponse\<List\<Booking\>\> (Danh sách đầy đủ các cuốc xe theo thời gian mới nhất)

**\[GET\] /api/v1/payments/booking/{bookingId}/payment-status**

- **Mô tả:** Khách hàng kiểm tra tình trạng thanh toán trực tuyến của cuốc xe (đã thanh toán thành công hay đang chờ xử lý).
- **Phân quyền / Header:** Public / Authenticated
- **Tham số (Path / Query Params):** bookingId (Long - Mã cuốc xe)
- **Kết quả trả về (Response):** String: 'SUCCESS' (nếu đã thanh toán) hoặc 'PENDING' (chưa thanh toán/chờ MoMo/VNPay)

**5. Nghiệp vụ Khám Phá Quán Ăn & Đặt Món Ăn Giao Tận Nơi (Food Delivery)**

**\[GET\] /api/v1/restaurants**

- **Mô tả:** Tìm kiếm quán ăn theo từ khóa hoặc xem danh sách tất cả các nhà hàng đang mở cửa hoạt động.
- **Phân quyền / Header:** Public / Authenticated
- **Tham số (Path / Query Params):** search (String, tùy chọn - Tìm kiếm theo tên quán, địa chỉ hoặc món ăn đặc trưng)
- **Kết quả trả về (Response):** ApiResponse\<List\<RestaurantResponse\>\> (Danh sách quán ăn: id, name, address, phone, imageUrl, rating: điểm sao TB, reviewCount / totalReviews: tổng lượt đánh giá, openHours, isOpen)

**\[GET\] /api/v1/restaurants/{id}**

- **Mô tả:** Xem chi tiết thông tin một nhà hàng cụ thể trước khi chọn món (thời gian mở cửa, địa chỉ chính xác, đánh giá).
- **Phân quyền / Header:** Public / Authenticated
- **Tham số (Path / Query Params):** id (Long - Mã định danh nhà hàng)
- **Kết quả trả về (Response):** ApiResponse\<RestaurantResponse\> (Chi tiết nhà hàng)

**\[GET\] /api/v1/restaurants/{restaurantId}/items**

- **Mô tả:** Lấy danh mục thực đơn món ăn của nhà hàng kèm giá tiền, hình ảnh minh họa món và trạng thái còn món.
- **Phân quyền / Header:** Public / Authenticated
- **Tham số (Path / Query Params):** restaurantId (Long - Mã nhà hàng), availableOnly (Boolean, mặc định false - true nếu chỉ muốn lấy các món đang sẵn sàng phục vụ)
- **Kết quả trả về (Response):** ApiResponse\<List\<MenuItemResponse\>\> (id, name, description, price, imageUrl, isAvailable, category)

**\[GET\] /api/v1/items/{itemId}**

- **Mô tả:** Lấy thông tin chi tiết một món ăn bằng ID món (dùng khi khách bấm vào món từ banner trang chủ, thông báo khuyến mãi hoặc xem lại từ giỏ hàng mà không cần ID quán).
- **Phân quyền / Header:** Public / Authenticated
- **Tham số (Path / Query Params):** itemId (Long - ID món ăn)
- **Kết quả trả về (Response):** ApiResponse\<MenuItemResponse\> (id, restaurantId, restaurantName, name, description, price, imageUrl, category, isAvailable, createdAt, updatedAt)

**\[GET\] /api/v1/restaurants/{restaurantId}/items/{itemId}**

- **Mô tả:** Xem thông tin chi tiết món ăn khi đang duyệt thực đơn của một nhà hàng cụ thể (hệ thống kiểm tra chặt chẽ món thuộc đúng nhà hàng, trả về 404 nếu không khớp quán).
- **Phân quyền / Header:** Public / Authenticated
- **Tham số (Path / Query Params):** restaurantId (Long - ID nhà hàng), itemId (Long - ID món ăn)
- **Kết quả trả về (Response):** ApiResponse\<MenuItemResponse\> (id, restaurantId, restaurantName, name, description, price, imageUrl, category, isAvailable, createdAt, updatedAt)

**\[POST\] /api/v1/food-orders**

- **Mô tả:** Khách hàng gửi đơn đặt món ăn. Nếu chọn CASH (tiền mặt), hệ thống tạo đơn PENDING và bắn chuông báo tới nhà hàng. Nếu chọn WALLET (Ví OmniPay) hoặc MOMO/VNPAY, hệ thống tạo đơn AWAITING_PAYMENT (chờ thanh toán thành công trước khi gửi đơn cho nhà hàng tiếp nhận).
- **Phân quyền / Header:** Bearer Token (Header X-User-Id)
- **Danh sách trường Request Body (JSON):**
  - **restaurantId:** *(Long)* \[Bắt buộc\] ID của nhà hàng cung cấp món
  - **dropOffAddress:** *(String)* \[Bắt buộc\] Địa chỉ giao đồ ăn tận tay khách hàng
  - **dropOffLatitude:** *(Double)* \[Bắt buộc\] Tọa độ vĩ độ nơi giao hàng
  - **dropOffLongitude:** *(Double)* \[Bắt buộc\] Tọa độ kinh độ nơi giao hàng
  - **items:** *(List\<FoodOrderItemRequest\>)* \[Bắt buộc\] Danh sách các món ăn khách chọn đặt
  - **items\[\].menuItemId:** *(Long)* \[Bắt buộc\] ID của món ăn trong thực đơn
  - **items\[\].quantity:** *(Integer)* \[Bắt buộc\] Số lượng phần ăn đặt
  - **items\[\].note:** *(String)* \[Tùy chọn\] Ghi chú riêng cho món (ví dụ: 'Ít đường', 'Không lấy tương cà')
  - **note:** *(String)* \[Tùy chọn\] Ghi chú chung cho đơn hàng / Lời dặn tài xế shipper (ví dụ: 'Gọi trước khi đến 5 phút')
  - **paymentMethod:** *(String)* \[Bắt buộc\] Phương thức thanh toán: 'CASH', 'WALLET', 'MOMO', 'VNPAY'
- **Kết quả trả về (Response):** ApiResponse\<FoodOrderResponse\> (orderId, status: 'PENDING' hoặc 'AWAITING_PAYMENT', isPaid, totalPrice, deliveryFee, items, estimatedDeliveryMinutes)

**\[PATCH\] /api/v1/food-orders/{orderId}/switch-to-cash**

**• Mô tả:** Khách hàng đổi phương thức thanh toán sang Tiền mặt (CASH) khi giao dịch thanh toán online (Ví/MoMo/VNPay) thất bại, không đủ số dư hoặc khách đổi ý. Đơn hàng chuyển ngay từ AWAITING_PAYMENT sang PENDING để quán nhận và làm món.

**• Phân quyền / Header:** Bearer Token (Header X-User-Id)

**• Tham số (Path / Query Params):** orderId (Long - Mã đơn hàng đồ ăn)

**• Kết quả trả về (Response):** ApiResponse\<FoodOrderResponse\> (status: 'PENDING', paymentMethod: 'CASH', isPaid: false)

**\[GET\] /api/v1/food-orders/{orderId}**

- **Mô tả:** Xem chi tiết đơn đặt món ăn, trạng thái xử lý đơn (Bếp nhận -\> Đang nấu -\> Tìm shipper -\> Đang giao), vị trí và thông tin tài xế.
- **Phân quyền / Header:** Bearer Token (Header X-User-Id)
- **Tham số (Path / Query Params):** orderId (Long - Mã đơn hàng đồ ăn)
- **Kết quả trả về (Response):** ApiResponse\<FoodOrderResponse\> (Chi tiết đơn, tiến độ trạng thái, driverId, thông tin shipper nếu đã có)

**\[GET\] /api/v1/food-orders/my-orders**

- **Mô tả:** Xem toàn bộ lịch sử các đơn hàng đặt đồ ăn của khách hàng đang đăng nhập.
- **Phân quyền / Header:** Bearer Token (Header X-User-Id)
- **Kết quả trả về (Response):** ApiResponse\<List\<FoodOrderResponse\>\> (Danh sách lịch sử đơn món)

**\[POST\] /api/v1/food-orders/{orderId}/cancel**

- **Mô tả:** Khách hàng hủy đơn đặt món. Được phép hủy khi đơn ở trạng thái AWAITING_PAYMENT, PENDING hoặc NO_DRIVER_FOUND (trước khi quán bắt đầu nấu PREPARING). Nếu đơn đã trừ tiền ví OmniPay, hệ thống tự động hoàn tiền 100% về ví ngay lập tức.
- **Phân quyền / Header:** Bearer Token (Header X-User-Id)
- **Tham số (Path / Query Params):** orderId (Long - Mã đơn hàng cần hủy)

**• Danh sách trường Request Body (JSON):**

**- reason:** (String) \[Tùy chọn\] Lý do khách hủy đơn

**- reasonCode:** (String) \[Tùy chọn\] Mã lý do: 'CHANGED_MIND', 'FOUND_BETTER_PRICE', 'LONG_WAIT_TIME', 'PAYMENT_FAILED', 'NO_DRIVER_FOUND', 'OTHER'

- **Kết quả trả về (Response):** ApiResponse\<FoodOrderResponse\> (status: 'CANCELLED', cancelledBy: 'CUSTOMER', cancelReason, cancelReasonCode, tự động hoàn tiền ví)

**\[POST\] /api/v1/food-orders/{orderId}/retry-driver**

**• Mô tả:** Khách hàng bấm tìm lại tài xế khi đơn rơi vào trạng thái NO_DRIVER_FOUND (hệ thống chưa tìm được tài xế sau các đợt quét). Cho phép retry tối đa 3 lần.

**• Phân quyền / Header:** Bearer Token (Header X-User-Id)

**• Tham số (Path / Query Params):** orderId (Long - Mã đơn hàng đồ ăn)

**• Kết quả trả về (Response):** ApiResponse\<FoodOrderResponse\> (driverRetryCount tăng thêm 1, tiếp tục quét tài xế)

**\[GET\] /api/v1/payments/food-order/{orderId}/payment-status**

- **Mô tả:** Khách hàng kiểm tra trạng thái thanh toán của đơn đặt món đồ ăn.
- **Phân quyền / Header:** Public / Authenticated
- **Tham số (Path / Query Params):** orderId (Long - Mã đơn hàng đồ ăn)
- **Kết quả trả về (Response):** String: 'SUCCESS' hoặc 'PENDING'

**6. Đánh Giá Đơn Đồ Ăn & Xem Nhận Xét Quán Ăn (/api/v1/food-reviews, /api/v1/restaurants/{id}/reviews)**

**\[POST\] /api/v1/food-reviews/upload-image**

- **Mô tả:** Tải ảnh chụp món ăn/hóa đơn lên hệ thống lưu trữ Cloudinary trước khi gửi đánh giá đơn hàng. Mobile app chỉ cho phép người dùng đính kèm các tệp hình ảnh định dạng PNG, JPG, JPEG hoặc WEBP.
- **Phân quyền / Header:** Bearer Token (Header Authorization)
- **Tham số (Form-Data):** file (MultipartFile \[Bắt buộc\] - Tệp hình ảnh ảnh chụp món ăn/hóa đơn)
- **Kết quả trả về (Response):** ApiResponse\<Map\<String, String\>\> (url: Đường dẫn ảnh CDN Cloudinary HTTPS, publicId: Mã định danh Cloudinary)

**\[POST\] /api/v1/food-reviews**

- **Mô tả:** Khách hàng gửi đánh giá mới hoặc cập nhật đánh giá cho đơn hàng đồ ăn đã giao thành công (DELIVERED). Ràng buộc 1 tuần: Đơn hàng hoàn thành quá 7 ngày sẽ bị khóa hoàn toàn, không thể gửi đánh giá mới (HTTP 400). Ràng buộc 48 giờ: Cho phép chỉnh sửa nội dung đánh giá trong vòng 48h kể từ lúc tạo (createdAt + 48h); sau 48h app tự động khóa tính năng chỉnh sửa và chuyển sang chế độ chỉ xem. Tự động cập nhật điểm sao trung bình (rating) và tổng số lượt đánh giá (reviewCount / totalReviews) của quán ăn.
- **Phân quyền / Header:** Bearer Token (Header Authorization, X-User-Id)
- **Danh sách trường Request Body (JSON):** Chi tiết các trường dữ liệu:
  - **orderId:** (Long) \[Bắt buộc\] Mã định danh đơn hàng đồ ăn đã hoàn thành
  - **restaurantRating:** (Integer) \[Bắt buộc\] Điểm đánh giá quán ăn từ 1 đến 5 sao
  - **restaurantComment:** (String) \[Tùy chọn\] Nhận xét chi tiết về món ăn và chất lượng dịch vụ
  - **driverRating:** (Integer) \[Bắt buộc\] Điểm đánh giá tài xế giao hàng từ 1 đến 5 sao
  - **driverTags:** (List\<String\>) \[Tùy chọn\] Danh sách tag nhận xét tài xế (VD: 'Giao hàng nhanh', 'Thân thiện', 'Bảo quản cẩn thận')
  - **reviewPhotos:** (List\<String\>) \[Tùy chọn\] Danh sách URL ảnh đính kèm (tối đa 5 ảnh tải lên từ Cloudinary)
- **Kết quả trả về (Response):** ApiResponse\<FoodOrderReviewResponse\> (id, orderId, restaurantRating, restaurantComment, driverRating, driverTags, reviewPhotos, merchantReply, merchantReplyAt, createdAt, readOnly: boolean)

**\[GET\] /api/v1/food-reviews/order/{orderId}**

- **Mô tả:** Khách hàng kiểm tra xem đơn hàng đã từng đánh giá hay chưa và kiểm tra cờ readOnly (true nếu đã quá 48h để khóa nút sửa trên giao diện mobile app).
- **Phân quyền / Header:** Bearer Token (Header Authorization)
- **Tham số (Path / Query Params):** orderId (Long - Mã đơn hàng đồ ăn)
- **Kết quả trả về (Response):** ApiResponse\<FoodOrderReviewResponse\> (Chi tiết đánh giá của đơn, kèm readOnly: true/false)

**\[GET\] /api/v1/restaurants/{restaurantId}/reviews**

- **Mô tả:** Khách hàng xem danh sách các đánh giá, ảnh chụp món ăn thực tế và phản hồi của nhà hàng trước khi quyết định chọn món. Hỗ trợ lọc theo số sao (1-5 sao) và phân trang.
- **Phân quyền / Header:** Public / Authenticated
- **Tham số (Path / Query Params):** restaurantId (Long - Mã nhà hàng), page (int - Số trang từ 0), size (int - Số lượng đánh giá mỗi trang, mặc định 10), rating (Integer - Tùy chọn lọc 1-5 sao)
- **Kết quả trả về (Response):** ApiResponse\<Page\<FoodOrderReviewResponse\>\> (Danh sách đánh giá phân trang kèm ảnh, bình luận và phản hồi từ quán)

**PHẦN III: TẬP LỆNH API DÀNH RIÊNG CHO TÀI XẾ (DRIVER APP)**

*Toàn bộ các chức năng dành riêng cho Đối tác Tài xế trên ứng dụng Driver App: Đăng ký hồ sơ, Bật/Tắt chế độ trực tuyến nhận cuốc, Bắn tọa độ GPS định kỳ, Nhận/Hủy cuốc xe, Xác nhận đến điểm đón (Geofencing 50m), Bắt đầu chở khách, Hoàn thành cuốc xe, Tiếp nhận đơn giao đồ ăn, Cập nhật tiến độ giao món, Xem báo cáo doanh thu ngày và Rút tiền ví tài xế.*

**1. Đăng ký Đối Tác Tài Xế & Quản lý Phương Tiện**

**\[POST\] /api/v1/drivers/register**

- **Mô tả:** Tài xế đăng ký tài khoản đối tác mới. Hồ sơ được lưu ở trạng thái chờ duyệt (PENDING_APPROVAL). Bắn sự kiện Kafka 'driver-registered-topic' sang Payment Service để tự động mở ví tài xế 0 VNĐ.
- **Phân quyền / Header:** Public (Không yêu cầu đăng nhập)
- **Danh sách trường Request Body (JSON):**
  - **fullName:** *(String)* \[Bắt buộc\] Họ và tên đầy đủ của tài xế
  - **phoneNumber:** *(String)* \[Bắt buộc\] Số điện thoại đăng ký (duy nhất)
  - **password:** *(String)* \[Bắt buộc\] Mật khẩu tài khoản (tối thiểu 6 ký tự)
  - **email:** *(String)* \[Bắt buộc\] Địa chỉ email cá nhân
  - **licensePlate:** *(String)* \[Bắt buộc\] Biển kiểm soát phương tiện (ví dụ: '29B1-999.88')
  - **vehicleType:** *(String)* \[Bắt buộc\] Loại phương tiện hoạt động: 'BIKE' (Xe máy) hoặc 'CAR' (Ô tô)
  - **driverLicenseNumber:** *(String)* \[Bắt buộc\] Số giấy phép lái xe (GPLX)
  - **idCardNumber:** *(String)* \[Bắt buộc\] Số thẻ Căn cước công dân / CMND
- **Kết quả trả về (Response):** ApiResponse\<LoginResponse\> (Thông báo đăng ký thành công, trạng thái duyệt PENDING_APPROVAL)

**\[GET\] /api/v1/drivers/{driverId}/profile**

- **Mô tả:** Tài xế xem thông tin hồ sơ của chính mình, kiểm tra tình trạng phê duyệt hồ sơ (PENDING_APPROVAL: Chờ duyệt, APPROVED: Đã duyệt, REJECTED: Bị từ chối).
- **Phân quyền / Header:** Bearer Token (Chính chủ tài xế)
- **Tham số (Path / Query Params):** driverId (Long - ID tài xế trên Path)
- **Kết quả trả về (Response):** ApiResponse\<UserResponse\> (Hồ sơ tài xế, thông tin xe, giấy phép, trạng thái duyệt)

**\[PUT\] /api/v1/drivers/{driverId}/vehicle**

- **Mô tả:** Tài xế cập nhật thông tin phương tiện khi đổi xe mới hoặc sửa đổi biển số xe.
- **Phân quyền / Header:** Bearer Token (Header X-User-Id trùng khớp driverId)
- **Tham số (Path / Query Params):** driverId (Long - ID tài xế)
- **Danh sách trường Request Body (JSON):**
  - **vehicleType:** *(String)* \[Bắt buộc\] Loại xe: 'BIKE' hoặc 'CAR'
  - **licensePlate:** *(String)* \[Bắt buộc\] Biển số xe mới
  - **driverLicenseNumber:** *(String)* \[Bắt buộc\] Số GPLX tương ứng
- **Kết quả trả về (Response):** ApiResponse\<UserResponse\> (Hồ sơ tài xế sau khi cập nhật phương tiện)

**\[PUT\] /api/v1/drivers/{driverId}/resubmit**

- **Mô tả:** Tài xế nộp lại hồ sơ sau khi bị Admin từ chối phê duyệt (bổ sung thông tin chính xác để xét duyệt lại).
- **Phân quyền / Header:** Bearer Token (Header X-User-Id)
- **Tham số (Path / Query Params):** driverId (Long - ID tài xế)
- **Danh sách trường Request Body (JSON):**
  - **fullName:** *(String)* \[Bắt buộc\] Họ tên tài xế
  - **email:** *(String)* \[Bắt buộc\] Email liên lạc
  - **vehicleType:** *(String)* \[Bắt buộc\] Loại phương tiện
  - **licensePlate:** *(String)* \[Bắt buộc\] Biển kiểm soát
  - **driverLicenseNumber:** *(String)* \[Bắt buộc\] Số bằng lái xe
  - **idCardNumber:** *(String)* \[Bắt buộc\] Số CCCD/CMND
- **Kết quả trả về (Response):** ApiResponse\<UserResponse\> (Trạng thái chuyển về PENDING_APPROVAL để Admin duyệt lại)

**2. Trạng Thái Trực Tuyến & Cập Nhật Tọa Độ GPS Thời Gian Thực**

**\[PUT\] /api/v1/drivers/{driverId}/status**

- **Mô tả:** Tài xế gạt công tắc BẬT / TẮT nhận cuốc. Khi Bật (ONLINE), vị trí được đưa vào Redis Geo để sẵn sàng nhận cuốc xe và đơn đồ ăn. Khi Tắt (OFFLINE), tài xế bị xóa khỏi bộ quét tìm kiếm.
- **Phân quyền / Header:** Bearer Token (Header X-User-Id trùng khớp driverId)
- **Tham số (Path / Query Params):** driverId (Long - ID tài xế trên Path), isActive (Boolean - true: Bật ONLINE nhận chuyến, false: Tắt OFFLINE nghỉ ngơi)
- **Kết quả trả về (Response):** ApiResponse\<String\>: 'Cập nhật trạng thái tài xế thành công'

**\[POST\] /api/v1/locations/drivers/me**

- **Mô tả:** Ứng dụng Driver App chạy tiến trình nền (Background Service) định kỳ gửi tọa độ GPS hiện tại lên Location Service (chu kỳ 5 - 10 giây/lần). Dữ liệu được ghi tức thì vào Redis Geospatial và phát Realtime qua WebSocket.
- **Phân quyền / Header:** Bearer Token (Header X-User-Id, Role DRIVER)
- **Danh sách trường Request Body (JSON):**
  - **latitude:** *(Double)* \[Bắt buộc\] Tọa độ vĩ độ hiện tại của thiết bị tài xế
  - **longitude:** *(Double)* \[Bắt buộc\] Tọa độ kinh độ hiện tại của thiết bị tài xế
  - **bearing:** *(Double)* \[Tùy chọn\] Góc xoay hướng di chuyển của xe (0.0 đến 360.0 độ) để xoay mũi tên icon xe trên bản đồ
  - **speed:** *(Double)* \[Tùy chọn\] Vận tốc di chuyển thực tế (km/h)
- **Kết quả trả về (Response):** String: 'Đã cập nhật vị trí'

**3. Nghiệp vụ Nhận & Thực Hiện Cuốc Xe Chở Khách (Ride-Hailing)**

**\[PUT\] /api/v1/bookings/{bookingId}/accept**

- **Mô tả:** Tài xế bấm 'Chấp Nhận Cuốc Xe' trên màn hình đếm ngược 20 giây. Hệ thống sử dụng Redis Lock kiểm tra tính nguyên tử (Atomic). Nếu thành công, trạng thái chuyển sang ACCEPTED và phát thông báo WebSocket cho khách hàng.
- **Phân quyền / Header:** Bearer Token (Header X-User-Id đóng vai trò driverId)
- **Tham số (Path / Query Params):** bookingId (Long - Mã cuốc xe được mời)
- **Kết quả trả về (Response):** BookingResponse (status: 'ACCEPTED', driverId, customerId, fare, pickupAddress, dropoffAddress)

**\[PUT\] /api/v1/bookings/{bookingId}/arrived**

- **Mô tả:** Tài xế bấm 'Đã Đến Điểm Đón'. RÀNG BUỘC KỸ THUẬT: Hệ thống tính khoảng cách Haversine giữa GPS tài xế và điểm đón của khách. Nếu khoảng cách \> 50 mét, hệ thống từ chối (HTTP 400). Khi \<= 50m, chuyển trạng thái sang ARRIVED và báo khách ra xe.
- **Phân quyền / Header:** Bearer Token (Header X-User-Id)
- **Tham số (Path / Query Params):** bookingId (Long - Mã cuốc xe)
- **Kết quả trả về (Response):** BookingResponse (status: 'ARRIVED')

**\[PUT\] /api/v1/bookings/{bookingId}/start**

- **Mô tả:** Tài xế bấm 'Bắt Đầu Chuyến Đi' sau khi khách hàng đã lên xe an toàn. Trạng thái chuyến xe chuyển sang IN_PROGRESS. Bắt đầu tính toán quãng đường và thời gian di chuyển thực tế.
- **Phân quyền / Header:** Bearer Token (Header X-User-Id)
- **Tham số (Path / Query Params):** bookingId (Long - Mã cuốc xe)
- **Kết quả trả về (Response):** BookingResponse (status: 'IN_PROGRESS')

**\[PUT\] /api/v1/bookings/{bookingId}/complete**

- **Mô tả:** Tài xế chở khách đến điểm trả và bấm 'Hoàn Thành Chuyến Đi' (ràng buộc GPS điểm trả \<= 50m). Trạng thái chuyển sang COMPLETED. Hệ thống xuất bản sự kiện Kafka 'booking-completed-topic' để Payment Service tự động thu 20% phí sàn từ ví tài xế.
- **Phân quyền / Header:** Bearer Token (Header X-User-Id)
- **Tham số (Path / Query Params):** bookingId (Long - Mã cuốc xe), paymentMethod (String, mặc định 'CASH' - Phương thức khách thanh toán)
- **Kết quả trả về (Response):** BookingResponse (status: 'COMPLETED', completedAt, fare)

**\[PUT\] /api/v1/bookings/{bookingId}/driver-cancel**

- **Mô tả:** Tài xế hủy chuyến xe đã nhận vì lý do bất khả kháng (khách không ra điểm đón, xe gặp sự cố kỹ thuật...). Cuốc xe bị hủy và ghi nhận lịch sử xử lý.
- **Phân quyền / Header:** Bearer Token (Header X-User-Id)
- **Tham số (Path / Query Params):** bookingId (Long - Mã cuốc xe)
- **Kết quả trả về (Response):** BookingResponse (status: 'CANCELLED')

**\[GET\] /api/v1/bookings/driver/daily-report**

- **Mô tả:** Tài xế xem báo cáo tóm tắt hoạt động trong ngày: Tổng số cuốc đã chạy, tổng doanh thu kiếm được, tổng phí sàn đã trừ và thu nhập thực nhận.
- **Phân quyền / Header:** Bearer Token (Header X-User-Id)
- **Kết quả trả về (Response):** ApiResponse\<Map\<String, Object\>\> (date, totalTrips, totalFare, commissionDeducted, netIncome)

**4. Nghiệp vụ Tiếp Nhận & Giao Đơn Đồ Ăn (Food Delivery)**

**\[POST\] /api/v1/bookings/food-orders/{orderId}/accept**

- **Mô tả:** Tài xế bấm nhận đơn giao đồ ăn từ nhà hàng đến khách. Hệ thống gán tài xế vào đơn và xuất bản sự kiện Kafka 'DRIVER_ASSIGNED_TO_FOOD_ORDER' để cập nhật bảng food_orders.
- **Phân quyền / Header:** Bearer Token (Header X-User-Id)
- **Tham số (Path / Query Params):** orderId (Long - Mã đơn hàng đồ ăn)
- **Kết quả trả về (Response):** ApiResponse\<String\>: 'Tài xế đã nhận đơn giao đồ ăn thành công'

**\[POST\] /api/v1/bookings/food-orders/{orderId}/reject**

- **Mô tả:** Tài xế từ chối / bỏ qua lời mời giao đơn đồ ăn. Hệ thống chuyển lời mời cho tài xế giao hàng tiếp theo trong danh sách quét.
- **Phân quyền / Header:** Bearer Token (Header X-User-Id)
- **Tham số (Path / Query Params):** orderId (Long - Mã đơn hàng đồ ăn)
- **Kết quả trả về (Response):** ApiResponse\<String\>: 'Tài xế đã bỏ qua đơn giao đồ ăn'

**\[GET\] /api/v1/food-orders/driver**

- **Mô tả:** Tài xế xem danh sách các đơn giao đồ ăn đang thực hiện hoặc đã hoàn thành của mình.
- **Phân quyền / Header:** Bearer Token (Header X-User-Id)
- **Kết quả trả về (Response):** ApiResponse\<List\<FoodOrderResponse\>\> (Danh sách đơn giao hàng kèm thông tin nhà hàng và khách hàng)

**\[PATCH\] /api/v1/food-orders/{orderId}/driver-status**

- **Mô tả:** Tài xế cập nhật tiến trình giao món ăn: Chuyển sang 'DELIVERING' khi đã lấy thức ăn tại quán (GPS \<= 50m); Chuyển sang 'COMPLETED' khi đã giao tận tay khách (GPS \<= 50m); Hoặc chuyển sang 'CANCELLED' khi gặp sự cố khẩn cấp. LƯU Ý THU TIỀN: Nếu đơn là tiền mặt CASH (isPaid = false), tài xế thu đủ tiền mặt từ khách; Nếu đơn đã thanh toán ví/online (isPaid = true), màn hình tài xế hiển thị rõ tiền mặt cần thu = 0đ (KHÔNG thu tiền mặt của khách).
- **Phân quyền / Header:** Bearer Token (Header X-User-Id)
- **Tham số (Path / Query Params):** orderId (Long - Mã đơn đồ ăn trên Path)
- **Danh sách trường Request Body (JSON):**
  - **status:** (String) \[Bắt buộc\] Trạng thái tài xế cập nhật: 'DELIVERING' (Đang giao), 'COMPLETED' (Đã giao thành công), hoặc 'CANCELLED' (Sự cố hủy đơn)

**- reason:** (String) \[Tùy chọn\] Lý do tài xế hủy đơn nếu status là CANCELLED

**- reasonCode:** (String) \[Tùy chọn\] Mã lý do tài xế hủy: 'DRIVER_VEHICLE_BREAKDOWN', 'CUSTOMER_UNREACHABLE', 'OTHER'

- **Kết quả trả về (Response):** ApiResponse\<FoodOrderResponse\> (status, isPaid, cancelledBy, cancelReason, cancelReasonCode)

**5. Rút Tiền Thu Nhập Từ Ví Tài Xế Về Ngân Hàng**

**\[POST\] /api/v1/payments/driver/withdraw**

- **Mô tả:** Tài xế tạo lệnh rút tiền thu nhập từ ví tài xế về tài khoản ngân hàng cá nhân liên kết.
- **Phân quyền / Header:** Bearer Token (Header X-User-Id đóng vai trò driverId)
- **Tham số (Path / Query Params):** amount (Double - Số tiền tài xế muốn rút khỏi ví)
- **Kết quả trả về (Response):** HTTP 200 OK (Khấu trừ số dư ví tài xế và tạo bản ghi Transaction WITHDRAWAL)

**PHẦN IV: HƯỚNG DẪN KẾT NỐI REALTIME WEBSOCKET (STOMP) TRÊN MOBILE APP**

*Giao thức WebSocket STOMP (over SockJS) được triển khai trên cổng API Gateway để truyền nhận dữ liệu thời gian thực giữa Mobile App và máy chủ Backend.*

**1. Cấu hình Kết Nối Endpoint WebSocket**

- **Endpoint Điều Phối Cuốc Xe & Đơn Món:** ws://\<gateway-host\>:8080/ws-booking (hoặc wss://\<domain\>/ws-booking)
- **Endpoint Live Tracking Tọa Độ GPS:** ws://\<gateway-host\>:8080/ws-location (hoặc wss://\<domain\>/ws-location)
- **Header khi gửi lệnh CONNECT STOMP:** userId: \<currentUserId\> (Bắt buộc để định danh phiên User/Driver)

**2. Danh sách Kênh Lắng Nghe (Topic / Queue Subscriptions)**

**\[STOMP\] /user/queue/ride-offers**

- **Mô tả:** \[DÀNH CHO TÀI XẾ\] Lắng nghe lời mời cuốc xe chở khách mới gần vị trí của tài xế (thời gian đếm ngược nhận cuốc 20 giây).
- **Phân quyền / Header:** Khách hàng kết nối gửi Header userId: \<driverId\>
- **Kết quả trả về (Response):** RideOfferMessage (bookingId, pickupAddress, dropoffAddress, pickupLat, pickupLng, dropoffLat, dropoffLng, fare, distanceKm, countdownSeconds: 20)

**\[STOMP\] /user/queue/food-offers**

- **Mô tả:** \[DÀNH CHO TÀI XẾ\] Lắng nghe thông báo mời nhận giao đơn đồ ăn từ nhà hàng gần vị trí của tài xế.
- **Phân quyền / Header:** Khách hàng kết nối gửi Header userId: \<driverId\>
- **Kết quả trả về (Response):** FoodOfferMessage (orderId, restaurantName, restaurantAddress, dropOffAddress, deliveryFee, totalPrice)

**\[STOMP\] /topic/booking/{bookingId}**

- **Mô tả:** \[DÀNH CHO KHÁCH HÀNG & TÀI XẾ\] Kênh công khai theo dõi tiến trình của một cuốc xe cụ thể (nhận cuốc, tài xế đến nơi, đang đi, hoàn tất, hủy).
- **Phân quyền / Header:** Public / Dynamic theo bookingId
- **Tham số (Path / Query Params):** bookingId (Long - ID cuốc xe đang theo dõi)
- **Kết quả trả về (Response):** BookingEventMessage (bookingId, status: ACCEPTED/ARRIVED/IN_PROGRESS/COMPLETED/CANCELLED, driverInfo: {id, name, phone, licensePlate})

**\[STOMP\] /topic/driver-location/{driverId}**

- **Mô tả:** \[DÀNH CHO KHÁCH HÀNG\] Nhận luồng tọa độ GPS thời gian thực của tài xế đang đón hoặc đang chở mình để vẽ vị trí và góc xoay icon xe mượt mà trên bản đồ.
- **Phân quyền / Header:** Public / Dynamic theo driverId
- **Tham số (Path / Query Params):** driverId (Long - ID tài xế đang nhận chuyến)
- **Kết quả trả về (Response):** LocationLiveMessage (driverId, latitude, longitude, bearing, speed, timestamp)

**\[STOMP\] /user/queue/food-order**

- **Mô tả:** \[DÀNH CHO KHÁCH HÀNG\] Nhận thông báo khi đơn đặt món ăn đã được tài xế shipper tiếp nhận thành công.
- **Phân quyền / Header:** Gửi Header userId: \<customerId\>
- **Kết quả trả về (Response):** FoodOrderAssignedMessage (orderId, driverId, driverName, driverPhone, vehiclePlate)
