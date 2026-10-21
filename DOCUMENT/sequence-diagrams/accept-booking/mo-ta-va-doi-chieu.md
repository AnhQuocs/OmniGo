### b. Biểu đồ tuần tự UC2.1 & UC2.2 – Đặt chuyến xe và Ghép cuốc thời gian thực

**Tác nhân:** Khách hàng và tài xế.

**Thành phần tham gia:** Customer App, BookingController, BookingServiceImpl, Location Service, Pricing Service, BookingRepository, Redis, User-Driver Service và Driver App. BookingTimeoutService và BookingReassignService xử lý hết thời gian và tìm tài xế thay thế.

**Mô tả luồng:** Khách hàng chọn điểm đón, điểm đến và gửi yêu cầu đặt xe. Hệ thống kiểm tra tần suất yêu cầu và bảo đảm khách hàng chưa có chuyến đang hoạt động. BookingService lấy danh sách tài xế lân cận qua Location Service, gọi Pricing Service để lấy cước phí và lưu chuyến ở trạng thái PENDING. Hệ thống xét các tài xế theo từng mức bán kính, kiểm tra trạng thái trực tuyến và giữ chỗ tài xế bằng khóa Redis. Khi giữ chỗ thành công, lời mời nhận chuyến được gửi đến ứng dụng tài xế qua WebSocket, đồng thời kích hoạt bộ đếm chờ 20 giây. Khi tài xế nhận chuyến, hệ thống kiểm tra mã giữ chỗ và trạng thái PENDING, sau đó lưu trạng thái ACCEPTED, gán tài xế và thời gian nhận. Hệ thống xóa khóa giữ chỗ, cập nhật tài xế sang BUSY, gửi trạng thái mới cho khách qua WebSocket và trả kết quả cho tài xế.

**Luồng thay thế:**
- Nếu mã giữ chỗ không hợp lệ hoặc chuyến không còn PENDING, yêu cầu nhận chuyến bị từ chối. Không tìm thấy chuyến là trường hợp 404, được lược khỏi hình.
- Nếu hết 20 giây mà chuyến vẫn PENDING và phiên giữ chỗ còn khớp, hệ thống giải phóng tài xế, đóng lời mời cũ và kích hoạt tìm tài xế khác. Bộ xử lý tìm lại hiện xét các mức 3, 5 và 8 km; nếu không tìm được tài xế thay thế và chuyến vẫn PENDING thì chuyển CANCELLED và thông báo khách.
- Nếu lần tìm ban đầu không giữ chỗ được tài xế nào, chuyến chuyển CANCELLED và được trả về cho khách.
- Nếu Pricing Service lỗi, mã nguồn có phương án tính giá dự phòng.

*Hình 8: Biểu đồ tuần tự nghiệp vụ Đặt chuyến xe và Ghép cuốc.*

**Đối chiếu hình cũ:**
1. API tạo chuyến là POST /api/v1/bookings, không có /create.
2. API nhận chuyến là PUT /api/v1/bookings/{bookingId}/accept, không phải POST.
3. Lời mời được gửi bằng SimpMessagingTemplate qua /queue/driver/match; không dùng Kafka hoặc ride-request-topic trong luồng này.
4. Location Service tìm vị trí tài xế; BookingService không trực tiếp truy vấn Redis Geospatial. Redis trong hình là nơi giữ chỗ, không đại diện kho vị trí.
5. Tìm ban đầu lấy danh sách theo bán kính tối đa được cấu hình rồi lọc theo từng nấc trong bộ nhớ; không cố định chỉ 3 km.
6. Chờ phản hồi là 20 giây; TTL khóa drivers:reserved là 30 giây. Hai thời hạn này khác nhau. Nhận chuyến kiểm tra khóa còn khớp và trạng thái PENDING, không so sánh trực tiếp thời gian 20 giây.
7. Không mô tả bước nhận chuyến như giành một distributed lock mới: SET NX giữ chỗ được thực hiện trước khi gửi lời mời; khi nhận chỉ đọc/đối chiếu khóa rồi xóa sau khi lưu.
8. Giá được lấy sau khi lấy danh sách tài xế lân cận để có dữ liệu cung phục vụ tính cước.

**Phạm vi rút gọn:** Hình gộp BookingController và BookingServiceImpl thành Booking API / Service; lược API Gateway, các Feign client, truy vấn ảnh đại diện, phản hồi lưu dữ liệu và chi tiết vòng lặp thử ứng viên. BookingRepository đại diện truy cập cơ sở dữ liệu. Các bước timeout là xử lý bất đồng bộ, chỉ thực hiện khi các điều kiện nêu trên còn đúng; không chạy tiếp như một bước bắt buộc sau nhận chuyến thành công.

Nguồn: booking-service/controller/BookingController.java; service/impl/BookingServiceImpl.java, BookingTimeoutService.java, BookingReassignService.java; repository/BookingRepository.java.
