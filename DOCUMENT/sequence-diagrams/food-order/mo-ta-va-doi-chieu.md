### e. Biểu đồ tuần tự UC5.3 – Quy trình đặt đơn giao đồ ăn

**Tác nhân:** Khách hàng và đối tác nhà hàng.

**Thành phần:** Customer App, FoodOrderController, FoodOrderServiceImpl, các repository/cơ sở dữ liệu, Merchant Portal, Redis và Kafka Broker. Hình gộp controller/service, các repository/database; khóa Redis được thể hiện ở bước kiểm tra thay vì thêm lifeline.

**Phạm vi hình:** Luồng tiền mặt (CASH) với dữ liệu hợp lệ, từ tạo đơn đến nhà hàng nhận và chuẩn bị món. Bước READY_FOR_PICKUP là phần mở rộng để thể hiện đúng thời điểm dùng Kafka.

**Mô tả luồng:** Khách hàng chọn món, nhập địa chỉ giao và gửi yêu cầu đặt đơn. Hệ thống kiểm tra nhà hàng đang OPEN và không bị khóa; từng món phải tồn tại, thuộc nhà hàng được chọn và còn phục vụ. Hệ thống tính tiền món, phí giao hàng, rồi lưu FoodOrder và các FoodOrderItem. Với tiền mặt, đơn được tạo ở trạng thái PENDING và phản hồi 201 Created được trả cho khách. Khi bật tự làm mới, cổng nhà hàng tải lại danh sách đơn mỗi 5 giây. Nhà hàng nhận đơn bằng yêu cầu chuyển sang ACCEPTED, sau đó chuyển sang PREPARING khi bắt đầu chuẩn bị. Mỗi lần cập nhật, service dùng khóa Redis, kiểm tra chủ sở hữu và trạng thái hiện tại, lưu thay đổi rồi giải phóng khóa. Khi quán báo READY_FOR_PICKUP, hệ thống phát sự kiện FIND_DRIVER_FOR_FOOD_ORDER qua Kafka để bắt đầu tìm tài xế.

*Hình 11: Biểu đồ tuần tự đặt món và nhà hàng xử lý đơn (luồng thanh toán tiền mặt).*

**Nhánh thanh toán online:** VNPAY, MOMO và WALLET tạo đơn ở AWAITING_PAYMENT. markOrderAsPaid đặt isPaid = true và chuyển AWAITING_PAYMENT sang PENDING. Danh sách đơn phía nhà hàng loại các đơn AWAITING_PAYMENT. Có thể chuyển sang CASH qua endpoint switch-to-cash, khi đó đơn về PENDING và isPaid = false. Hình không mô tả chi tiết xử lý tại cổng thanh toán.

**Đối chiếu hình cũ:**
- POST /api/v1/food-orders, không có /create.
- PATCH /api/v1/food-orders/{orderId}/status, không phải PUT.
- Kiểm tra restaurant.status == OPEN và isLocked, không có thuộc tính Restaurant.isOpen.
- Không chuyển thẳng PENDING sang PREPARING; phải qua ACCEPTED.
- createOrder không phát thông báo đơn mới qua Kafka/WebSocket. MerchantOrders.jsx tải lại đơn mỗi 5000 ms nếu autoRefresh bật và có restaurant.id.
- Kafka được phát khi chuyển READY_FOR_PICKUP, không phải khi vừa tạo đơn hay xác nhận PREPARING.
- Không vẽ thông báo WebSocket cập nhật PREPARING tới khách vì phương thức hiện tại không gửi sự kiện này.
- totalPrice = tổng tiền các món + deliveryFee. Tên món và đơn giá được chụp lại trong FoodOrderItem.
- Khóa Redis lock:order:{orderId} có TTL 5 giây, được xóa trong finally. Các lỗi dữ liệu, quyền sở hữu và chuyển trạng thái được lược khỏi hình chính, không có nghĩa luồng bỏ kiểm tra.
- Chuỗi phản hồi tạo đơn có thông báo “đang tìm tài xế”, nhưng mã createOrder chỉ lưu đơn. Diễn giải dựa trên hành vi thực tế, không dựa riêng thông báo này.

Nguồn: food-delivery-service/controller/FoodOrderController.java; service/impl/FoodOrderServiceImpl.java; publisher/FoodEventPublisher.java; SRC/FE/src/pages/merchant/MerchantOrders.jsx. Đây là đối chiếu mã nguồn, chưa kiểm thử tích hợp.
