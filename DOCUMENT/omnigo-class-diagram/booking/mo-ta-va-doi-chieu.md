### d. Nhóm lớp Đặt xe và Gọi chuyến

Nhóm lớp này quản lý thông tin chuyến xe từ khi khách hàng đặt xe đến khi chuyến đi hoàn thành hoặc bị hủy. Booking lưu khách hàng, tài xế, điểm đón, điểm trả, khoảng cách, cước phí, phương thức thanh toán và các mốc thời gian. BookingStatus xác định trạng thái hiện tại của chuyến xe. Cước phí được lấy từ pricing-service, nơi sử dụng PricingConfig làm cấu hình giá cơ sở.

*Hình 4: Mô hình lớp Đặt xe và Gọi chuyến, kèm cấu hình giá liên quan.*

*Bảng 5: Các lớp chính trong nhóm Đặt xe và Gọi chuyến.*

| Tên lớp/enum | Vai trò |
|---|---|
| Booking | Lưu dữ liệu chuyến xe: mã khách hàng, mã tài xế, tọa độ đón/trả, khoảng cách, giá, phương thức thanh toán, trạng thái và thời gian tạo/nhận/hoàn thành. |
| BookingStatus | Định nghĩa các trạng thái: PENDING (chờ nhận chuyến), ACCEPTED (đã nhận), ARRIVED (đã đến điểm đón), IN_PROGRESS (đang thực hiện chuyến), COMPLETED (hoàn thành), CANCELLED (đã hủy). |
| PricingConfig | Chứa cước mở cửa (baseFare), đơn giá mỗi km (pricePerKm) và đơn giá mỗi phút (pricePerMinute). Thuộc pricing-service; cấu hình được lưu trong Redis. |

Quan hệ chính:

- Thuộc tính status của Booking sử dụng enum BookingStatus. Nét đứt có nhãn status biểu diễn sự phụ thuộc vào kiểu enum.
- Booking tham chiếu khách hàng và tài xế bằng customerId và driverId. Đây là các mã định danh dùng để trao đổi giữa dịch vụ; không phải quan hệ JPA trực tiếp với User hoặc DriverProfile. driverId có thể chưa có khi chuyến chưa được gán tài xế.
- BookingServiceImpl gọi PricingClient để lấy giá từ pricing-service, rồi ghi giá và khoảng cách vào Booking. Booking không có thuộc tính hoặc lời gọi trực tiếp tới PricingConfig, vì vậy hình đã bỏ đường calculates fare giữa hai lớp này.
- PricingConfig được trình bày riêng trong phạm vi pricing-service. Giá thông thường còn phụ thuộc thời gian ước tính và hệ số cung–cầu, không chỉ ba mức giá cơ sở. Khi gọi dịch vụ giá thất bại, luồng tạo chuyến hiện có phương án tính giá dự phòng.
- Vòng đời thông thường: PENDING → ACCEPTED → ARRIVED → IN_PROGRESS → COMPLETED. Chuyến có thể bị hủy; luồng tài xế hủy nhận chuyến còn có thể đưa chuyến về PENDING để tìm tài xế khác. Đây là diễn giải nghiệp vụ, không phải khẳng định mọi chuyển trạng thái đều đã được chặn theo đúng chuỗi trong mã nguồn.

Đối chiếu hình cũ:

1. Giữ nguyên tên và kiểu dữ liệu của các thuộc tính được thể hiện; Booking còn có customerAvatarUrl và driverAvatarUrl, được lược bỏ để sơ đồ gọn.
2. Sáu giá trị BookingStatus trong hình cũ khớp dự án. Enum chỉ định nghĩa tập trạng thái; việc chuyển trạng thái do lớp dịch vụ xử lý. updateBookingStatus hiện kiểm tra tài xế sở hữu chuyến rồi gán trạng thái mới, không có kiểm tra đầy đủ chuỗi chuyển trạng thái tại phương thức này.
3. PricingConfig không phải entity JPA và không có quan hệ trực tiếp với Booking; nó là mô hình cấu hình của pricing-service, được PricingConfigRepository đọc/ghi Redis.
4. price là Double trong Booking; ba thuộc tính giá cơ sở là double trong PricingConfig.

Nguồn đối chiếu:
- booking-service: entity/Booking.java; util/enums/BookingStatus.java; service/client/PricingClient.java; service/impl/BookingServiceImpl.java.
- pricing-service: entity/PricingConfig.java; service/impl/PricingServiceImpl.java; repository/PricingConfigRepository.java.
