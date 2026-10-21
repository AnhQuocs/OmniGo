### c. Nhóm lớp Người dùng, Tài xế và Xác thực

Nhóm lớp này lưu thông tin tài khoản, vai trò người dùng và hồ sơ tài xế trong hệ thống OmniGo. User chứa thông tin dùng để đăng nhập và quản lý tài khoản. DriverProfile lưu thông tin phương tiện, giấy tờ và trạng thái của tài xế. Các enum Role, DriverStatus và ApprovalStatus giúp hệ thống phân biệt quyền truy cập, tình trạng nhận việc và kết quả xét duyệt hồ sơ.

*Hình 3: Mô hình lớp Người dùng và Tài xế — dữ liệu phục vụ xác thực, phân quyền.*

| Tên lớp/enum | Vai trò |
|---|---|
| User | Lưu số điện thoại, email, mật khẩu đã băm, họ tên, ảnh đại diện, vai trò và trạng thái khóa/xóa tài khoản. |
| DriverProfile | Lưu thông tin xe, biển số, CCCD, giấy phép lái xe, trạng thái nhận việc và kết quả duyệt hồ sơ. |
| Role | Xác định vai trò: CUSTOMER (khách hàng), DRIVER (tài xế), ADMIN (quản trị viên), RESTAURANT (đối tác nhà hàng). |
| DriverStatus | Xác định trạng thái tài xế: OFFLINE (ngoại tuyến), ONLINE (trực tuyến), BUSY (đang bận). |
| ApprovalStatus | Xác định trạng thái xét duyệt: PENDING_APPROVAL (chờ duyệt), APPROVED (đã duyệt), REJECTED (bị từ chối). |

Quan hệ chính:

- Một User có thể không có hoặc có một DriverProfile; mỗi DriverProfile thuộc về đúng một User. driver_id vừa là khóa chính của driver_profiles, vừa là khóa ngoại tham chiếu users.id, được ánh xạ bằng @OneToOne và @MapsId.
- role của User sử dụng enum Role; status và approvalStatus của DriverProfile lần lượt sử dụng DriverStatus và ApprovalStatus. Các đường nét đứt thể hiện sự phụ thuộc vào kiểu enum, không phải kế thừa.
- Luồng đăng ký tài xế registerDriver tạo User có vai trò DRIVER và DriverProfile trong cùng giao dịch. Không nên khẳng định chỉ riêng giá trị role = DRIVER đã bảo đảm có hồ sơ ở mức cơ sở dữ liệu: quan hệ hiện tại không áp đặt ràng buộc theo giá trị role.
- Tài xế mới được đặt trạng thái OFFLINE. Hồ sơ mặc định chờ duyệt; luồng đăng ký hiện có nhánh autoApprove cho phép đặt APPROVED ngay.

Phạm vi sơ đồ: bản rút gọn chỉ hiển thị thuộc tính chính. User còn có lockedReason, lockedAt, createdAt và updatedAt. DriverProfile còn có tham chiếu user (đã thể hiện bằng đường nối), ảnh hai mặt CCCD/GPLX, approvedAt và updatedAt. Hai lớp này lưu dữ liệu phục vụ xác thực; việc đăng nhập, phát hành token, làm mới token và đăng xuất được xử lý trong AuthServiceImpl cùng các thành phần bảo mật, không thể hiện ở hình này.

Đối chiếu hình cũ: thay MERCHANT bằng RESTAURANT; bổ sung ảnh đại diện và dữ liệu giấy tờ trong phạm vi mô tả; diễn đạt quan hệ là 1 — 0..1; không dùng include/extend cho các quan hệ này. Dấu - trước thuộc tính phản ánh private trong mã nguồn.

Nguồn đối chiếu: entity/User.java; entity/DriverProfile.java; util/enums/Role.java, DriverStatus.java, ApprovalStatus.java; mapper/UserMapper.java; service/impl/DriverServiceImpl.java, AuthServiceImpl.java trong user-driver-service.
