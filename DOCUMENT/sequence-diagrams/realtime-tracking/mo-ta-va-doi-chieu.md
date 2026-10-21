### c. Biểu đồ tuần tự UC3.1 – Định vị và Cập nhật vị trí GPS

**Tác nhân:** Tài xế.

**Thành phần tham gia:** Driver App, LocationController, LocationService và Redis Geo. Hình lược API Gateway và các lớp trung gian trong ứng dụng Android.

**Mô tả luồng:** Khi dịch vụ theo dõi vị trí đang hoạt động và ứng dụng được cấp quyền định vị, ứng dụng tài xế nhận tọa độ từ thiết bị. Android yêu cầu chu kỳ cập nhật 5 giây, khoảng thời gian tối thiểu 3 giây và khoảng cách di chuyển tối thiểu 5 m; đây là cấu hình yêu cầu, không bảo đảm nhận dữ liệu đúng mỗi 3 giây. Mỗi khi có callback vị trí, ứng dụng gửi HTTP POST tới /api/v1/locations/drivers/me. LocationController lấy mã tài xế từ header X-User-Id và chuyển dữ liệu cho LocationService. Service cập nhật vị trí vào Redis Geo với khóa drivers:online:locations, thành viên là driverId và tọa độ theo thứ tự longitude, latitude. Backend trả HTTP 200 cùng thông báo văn bản. Quá trình lặp lại khi có dữ liệu vị trí mới và dịch vụ theo dõi vẫn hoạt động.

*Hình 9: Biểu đồ tuần tự cập nhật vị trí GPS của tài xế theo mã nguồn hiện tại.*

**Các điểm sửa so với hình cũ:**
- Android hiện dùng HTTP POST qua DriverApi, không gửi WebSocket trong luồng DriverLocationForegroundService → SendDriverLocationUseCase → DriverRepositoryImpl.
- Backend cũng có đầu vào WebSocket tại /app/driver/location qua LocationWebSocketController, không phải /app/driver-location. Đầu vào này chỉ gọi updateDriverLocation để lưu Redis.
- Không có thao tác đặt TTL 30 giây trong updateDriverLocation. Không mô tả từng vị trí tự hết hạn sau 30 giây.
- Không có thao tác phát tọa độ tới /topic/booking/{bookingId} hoặc /topic/tracking/{bookingId} trong controller/service cập nhật vị trí đã kiểm tra. Không đưa Customer App, chuyển động mượt hoặc tính ETA vào luồng đã triển khai.
- Android có khai báo topic /topic/driver-location/{driverId} cho phần giao đồ ăn; việc khai báo ở phía nhận không chứng minh backend đã phát dữ liệu trên topic này.
- Có điểm chưa đồng bộ: DriverApi.sendDriverLocation mong ApiResponse<Any>, trong khi LocationController trả chuỗi văn bản. Sơ đồ ghi đúng phản hồi backend, không khẳng định ứng dụng đã xử lý thành công đầu cuối. Đây là đối chiếu tĩnh mã nguồn, chưa chạy kiểm thử tích hợp.

**Nguồn:** location-service/controller/LocationController.java, LocationWebSocketController.java; service/impl/LocationServiceImpl.java; config/WebSocketConfig.java. Android: DriverLocationForegroundService.kt, DriverRepositoryImpl.kt, DriverApi.kt, ApiEndpoints.kt.
