### g. Biểu đồ tuần tự UC9.1 – Phê duyệt hồ sơ đối tác tài xế phía Admin

**Tác nhân chính:** Quản trị viên. Tài xế là đối tượng có hồ sơ được xét duyệt, không trực tiếp tham gia các bước trong sơ đồ này.

**Thành phần:** Web Admin, DriverController, DriverService và DriverProfileRepository. Repository đại diện thao tác với PostgreSQL; hình lược API Gateway, Spring Security, UserMapper và các truy vấn phụ khi ánh xạ phản hồi.

**Mô tả luồng:** Quản trị viên mở màn hình quản lý tài xế. Web Admin gọi API lấy toàn bộ hồ sơ và lọc những hồ sơ có trạng thái PENDING_APPROVAL. Quản trị viên xem thông tin cá nhân, phương tiện, CCCD và giấy phép lái xe, sau đó chọn phê duyệt hoặc từ chối. Web Admin gửi yêu cầu PATCH chứa trạng thái xét duyệt và lý do nếu từ chối. Hệ thống kiểm tra quyền ADMIN, đọc hồ sơ và cập nhật kết quả. Nếu phê duyệt, approvalStatus được đặt APPROVED, approvedAt được ghi nhận bằng thời điểm hiện tại và rejectionReason được xóa. Nếu từ chối, approvalStatus chuyển REJECTED, lưu lý do và đặt trạng thái hoạt động của tài xế là OFFLINE. Hồ sơ được lưu, phản hồi trả về Web Admin để hiển thị kết quả và cập nhật danh sách.

*Hình 13: Biểu đồ tuần tự phê duyệt hoặc từ chối hồ sơ tài xế.*

**Đối chiếu hình cũ:**
- API danh sách là GET /api/v1/drivers/admin/all; controller gọi repository.findAll() trực tiếp. Không có /pending-approvals hay getPendingDrivers() trong controller/service đã kiểm tra. Giao diện Drivers.jsx lọc PENDING_APPROVAL.
- API cập nhật là PATCH /api/v1/drivers/{driverId}/approval, không phải POST /approve. Request chứa status và reason.
- Tên bảng là driver_profiles; trạng thái chờ duyệt là PENDING_APPROVAL, không phải PENDING.
- approveOrRejectDriver có @Transactional; dùng findById và save. Không có ghi audit_logs hoặc gửi Kafka/WebSocket/push trong phương thức duyệt này.
- Phê duyệt không tự chuyển trạng thái tài xế sang ONLINE, không tự mở khóa User. Không gọi kết quả này là kích hoạt toàn bộ tài khoản.
- Ngoài APPROVED/REJECTED, backend còn xử lý PENDING_APPROVAL và đặt OFFLINE. Nhánh này được lược khỏi hình chính.
- Khi bị từ chối, tài xế có thể cập nhật hồ sơ qua PUT /api/v1/drivers/{driverId}/resubmit; luồng này đặt PENDING_APPROVAL, xóa lý do từ chối và đặt OFFLINE. Đây là luồng riêng, không nằm trong hình phê duyệt.
- DriverProfile có số/ảnh hai mặt CCCD và GPLX, thông tin phương tiện; không có trường ảnh cà vẹt xe trong entity đã đối chiếu.
- Không tìm thấy hồ sơ trả lỗi 404; thiếu status bị validation từ chối. Các nhánh lỗi được lược khỏi hình chính. Không khẳng định backend chỉ cho duyệt hồ sơ đang chờ: phương thức hiện không kiểm tra trạng thái trước đó.

Nguồn: user-driver-service/controller/DriverController.java; service/impl/DriverServiceImpl.java; repository/DriverProfileRepository.java; dto/request/DriverApprovalRequest.java; entity/DriverProfile.java; SRC/FE/src/pages/Drivers.jsx.
