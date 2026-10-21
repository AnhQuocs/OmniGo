### f. Biểu đồ tuần tự UC6.1 – Nạp tiền ví qua VNPay/MoMo

**Tác nhân:** Người dùng (khách hàng hoặc tài xế).

**Thành phần:** App/Browser, PaymentController, PaymentFactory/PaymentStrategy, VnpayService hoặc MomoService, cổng thanh toán tương ứng, WalletService, các repository và cơ sở dữ liệu. Hình gộp controller, lựa chọn strategy và service của từng cổng để dễ đọc; lược API Gateway.

**Luồng chung:** Người dùng chọn cổng thanh toán và nhập số tiền. Ứng dụng gọi POST /api/v1/payments/payment/create với method, type và amount. Hệ thống lấy hoặc tạo ví theo người dùng và loại chủ ví, sau đó lưu Transaction có transactionType = DEPOSIT và status = PENDING. Hệ thống trả orderId và paymentUrl để người dùng mở trang thanh toán. Đây là luồng nạp ví, không gộp thanh toán chuyến xe hoặc đơn đồ ăn.

**VNPay:** VnpayService tự tạo URL với chữ ký HMAC-SHA512 qua VnpayEncoder; không gọi API tạo giao dịch để nhận payUrl như MoMo. Sau thanh toán, trình duyệt được chuyển đến GET /api/v1/payments/vnpay/return. Service tìm Transaction theo orderId; nếu đã SUCCESS hoặc không tìm thấy thì bỏ qua xử lý. Với responseCode = 00, service lưu SUCCESS rồi gọi WalletService cộng tiền; mã khác lưu FAILED. Controller trả trang HTML kết quả. Phương thức xử lý Return có @Transactional.

**MoMo – nhánh IPN:** MomoService ký yêu cầu bằng HMAC-SHA256 và gọi API MoMo để lấy payUrl. MoMo gửi POST /api/v1/payments/momo/ipn. Service kiểm tra chữ ký, tìm giao dịch và bỏ qua nếu đã SUCCESS. Nếu resultCode = 0, hệ thống lưu SUCCESS và gatewayTransId, rồi cộng số tiền đã lưu trong Transaction vào ví. Với kết quả thất bại, giao dịch chuyển FAILED. Controller trả 204 No Content khi xử lý kết thúc bình thường. Chữ ký sai bị từ chối; không tìm thấy giao dịch phát sinh lỗi. Mô hình nhánh chính giả định ví tồn tại.

**Cập nhật ví:** creditWallet dùng truy vấn PESSIMISTIC_WRITE theo userId và userType, cộng số dư rồi lưu. Các handler và creditWallet có @Transactional; không diễn giải đây là giao dịch phân tán ACID bao trùm cổng thanh toán bên ngoài, và không khẳng định kiểm tra SUCCESS đủ chống mọi callback đồng thời.

*Hình 12a: Biểu đồ tuần tự nạp ví qua VNPay theo mã nguồn hiện tại.*
*Hình 12b: Biểu đồ tuần tự nạp ví qua MoMo – nhánh IPN.*

**Các điểm phải phân biệt khi viết báo cáo:**
- method là VNPAY hoặc MOMO. type = CUSTOMER_DEPOSIT cho khách; DRIVER_DEPOSIT hoặc DEPOSIT cho tài xế.
- HMAC-SHA512 dùng cho VNPay, HMAC-SHA256 dùng cho MoMo.
- VNPay đang xử lý qua Return URL; PaymentController đã đọc không có endpoint VNPay IPN.
- MoMo IPN có kiểm tra chữ ký, phản hồi 204, không phải 200.
- MoMo còn có GET /api/v1/payments/momo/return gọi processMomoReturn. Luồng Return này cũng có thể cập nhật SUCCESS và cộng ví nếu chưa SUCCESS, không chỉ hiển thị kết quả. Hình MoMo giới hạn nhánh IPN; Return và IPN không được bảo đảm đến theo thứ tự cố định.
- Cả processVnpayReturn và processMomoReturn hiện chưa kiểm tra chữ ký Return. Vì vậy không thể mô tả mọi kết quả thanh toán đều được xác thực chữ ký. Đây là thiếu sót hiện hữu cần sửa ở mã nguồn nếu muốn đạt thiết kế xác thực callback; tác vụ này chỉ sửa tài liệu.
- Trang HTML kết quả dựa trên mã kết quả nhận vào, không tự chứng minh ví đã được cộng thành công trong mọi trường hợp.
- Các sơ đồ phản ánh đối chiếu tĩnh mã nguồn, chưa thử giao dịch sandbox hoặc kiểm thử đồng thời.

Nguồn: payment-service/controller/PaymentController.java; service/impl/VnpayService.java, MomoService.java, WalletServiceImpl.java; service/VnpayEncoder.java, MomoEncoder.java; repository/WalletRepository.java.
