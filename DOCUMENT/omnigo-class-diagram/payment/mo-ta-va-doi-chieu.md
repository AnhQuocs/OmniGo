### f. Nhóm lớp Ví điện tử và Thanh toán

Nhóm lớp này quản lý số dư ví và thông tin các giao dịch thanh toán trong hệ thống. Wallet lưu chủ ví, loại chủ ví và số dư hiện tại. Transaction ghi nhận số tiền, loại nghiệp vụ, phương thức thanh toán và trạng thái xử lý của từng giao dịch. Các enum giúp phân biệt giao dịch nạp tiền, rút tiền, thanh toán, thu nhập, phí hoa hồng và hoàn tiền.

*Hình 6: Mô hình lớp Ví điện tử và Thanh toán — bản rút gọn.*

| Tên lớp/enum | Vai trò |
|---|---|
| Wallet | Lưu userId, userType và số dư balance. Số dư dùng BigDecimal, được ánh xạ với precision = 19, scale = 2. |
| Transaction | Lưu số tiền, mã ví, mã nghiệp vụ liên quan, mã tham chiếu thanh toán, loại giao dịch, phương thức thanh toán, trạng thái và mã giao dịch từ cổng thanh toán. |
| UserType | Phân loại chủ ví: DRIVER (tài xế), CUSTOMER (khách hàng). |
| TransactionType | Phân loại nghiệp vụ: DEPOSIT (nạp tiền), TRIP_PAYMENT (thanh toán chuyến xe), TRIP_INCOME (thu nhập chuyến xe), COMMISSION_FEE (phí hoa hồng), WITHDRAWAL (rút tiền), FOOD_PAYMENT (thanh toán đồ ăn), FOOD_DELIVERY_INCOME (thu nhập giao đồ ăn), REFUND (hoàn tiền). |
| PaymentMethod | Định nghĩa phương thức: WALLET, MOMO, VNPAY, CASH. |
| TransactionStatus | Định nghĩa trạng thái: PENDING (chờ xử lý), SUCCESS (thành công), FAILED (thất bại), CANCELED (đã hủy). |

*Bảng 7: Các lớp chính trong nhóm Ví điện tử và Thanh toán.*

Quan hệ chính:

- Dịch vụ tìm hoặc tạo ví theo cặp userId và userType. Không nên mô tả userId một mình luôn tương ứng duy nhất một ví: entity hiện không khai báo unique cho userId hoặc ràng buộc unique tổng hợp cho cặp này.
- Một Wallet có thể được nhiều Transaction tham chiếu qua walletId. Đây là trường Long, không phải quan hệ @ManyToOne/@OneToMany. walletId không được khai báo nullable = false nên hình thể hiện phía Wallet là 0..1, phía Transaction là 0..*. Nét đứt giữa hai lớp biểu diễn tham chiếu logic bằng ID, không khẳng định có khóa ngoại JPA.
- Wallet dùng enum UserType. Transaction dùng TransactionType, PaymentMethod và TransactionStatus; các mũi tên nét đứt tới enum biểu diễn phụ thuộc kiểu dữ liệu.
- orderId là chuỗi tham chiếu thanh toán duy nhất và bắt buộc, được tạo theo từng nghiệp vụ, chẳng hạn DEP_..., WITHDRAW_..., FOOD_INC_..., REFUND_.... Không mặc định đồng nhất trường này với FoodOrder.id.
- bookingId là Long dùng để lưu mã nghiệp vụ liên quan. Ngoài mã chuyến xe, một số luồng giao đồ ăn/hoàn tiền hiện truyền mã đơn đồ ăn vào trường này.
- gatewayTransId lưu mã giao dịch từ cổng thanh toán, không chỉ dành riêng cho VNPay.
- Transaction hỗ trợ theo dõi và tra cứu giao dịch. Không khẳng định mọi thay đổi số dư đều luôn tự tạo Transaction: phương thức creditWallet hiện chỉ cập nhật số dư, việc tạo bản ghi giao dịch có thể nằm ở luồng gọi bên ngoài.

Đối chiếu hình cũ:

1. Thay toàn bộ TransactionType bằng danh sách hiện tại; TOPUP, WITHDRAW, PAYMENT, DRIVER_TRIP_PAYOUT và DRIVER_TIP không tồn tại trong enum này.
2. Bổ sung MOMO vào PaymentMethod và CANCELED vào TransactionStatus. Giữ đúng cách viết CANCELED của mã nguồn.
3. Sửa cách diễn giải orderId, bookingId và tính duy nhất của ví.
4. Đổi liên kết Wallet–Transaction thành tham chiếu walletId, không mô tả như quan hệ JPA trực tiếp.
5. Lược createdAt, updatedAt khỏi cả hai lớp trên hình; các thuộc tính này vẫn có trong mã nguồn.

Nguồn đối chiếu trong payment-service:
- entity/Wallet.java, entity/Transaction.java.
- util/enums/UserType.java, TransactionType.java, PaymentMethod.java, TransactionStatus.java.
- repository/WalletRepository.java.
- service/impl/WalletServiceImpl.java, MomoService.java, VnpayService.java.
