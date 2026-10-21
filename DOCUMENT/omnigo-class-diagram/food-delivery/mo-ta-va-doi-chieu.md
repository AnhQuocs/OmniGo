### e. Nhóm lớp Giao đồ ăn và Thực đơn

Nhóm lớp này quản lý nhà hàng, món ăn và quá trình đặt giao đồ ăn. Restaurant lưu thông tin nhà hàng, còn MenuItem lưu các món ăn trong thực đơn. Khi khách đặt món, FoodOrder lưu thông tin chung của đơn và FoodOrderItem ghi lại từng món, số lượng, đơn giá tại thời điểm đặt. Sau khi đơn hoàn thành, FoodOrderReview lưu đánh giá của khách đối với nhà hàng và tài xế, cùng phản hồi từ nhà hàng.

*Hình 5: Mô hình lớp Giao đồ ăn và Thực đơn — bản rút gọn.*

| Tên lớp | Vai trò |
|---|---|
| Restaurant | Lưu thông tin nhà hàng, chủ sở hữu, địa chỉ, tọa độ, giờ hoạt động, trạng thái và điểm đánh giá. |
| MenuItem | Quản lý món ăn: tên, mô tả, giá, ảnh, danh mục và trạng thái sẵn sàng phục vụ. |
| FoodOrder | Lưu thông tin đơn hàng, khách hàng, tài xế, địa chỉ giao, tổng tiền đã gồm phí giao hàng, phương thức và trạng thái thanh toán. Trường version hỗ trợ kiểm soát cập nhật đồng thời bằng khóa lạc quan. |
| FoodOrderItem | Lưu từng món trong đơn: mã món, tên món và giá được sao chép tại thời điểm đặt, số lượng và thành tiền. |
| FoodOrderReview | Lưu đánh giá nhà hàng, đánh giá tài xế, nhận xét, ảnh đánh giá nhà hàng và phản hồi của nhà hàng. |

*Bảng 6: Các lớp chính trong nhóm Giao đồ ăn và Thực đơn.*

Quan hệ chính:

- Restaurant — MenuItem: một nhà hàng có 0 hoặc nhiều món; mỗi món thuộc đúng một nhà hàng. Quan hệ được ánh xạ bằng @OneToMany/@ManyToOne.
- Restaurant — FoodOrder: một nhà hàng nhận 0 hoặc nhiều đơn; mỗi đơn thuộc đúng một nhà hàng, thông qua restaurant_id.
- FoodOrder — FoodOrderItem: mỗi đơn hợp lệ có ít nhất một chi tiết món; mỗi chi tiết thuộc đúng một đơn. Ánh xạ sử dụng @OneToMany/@ManyToOne; yêu cầu ít nhất một món được kiểm tra qua @NotEmpty trên items của FoodOrderCreateRequest, không phải riêng ánh xạ JPA.
- FoodOrder — FoodOrderReview: mỗi đơn có tối đa một đánh giá. Đây là liên kết logic qua orderId; cột order_id có unique = true. Lớp đánh giá không khai báo @OneToOne hay thuộc tính FoodOrder. Nét đứt trong hình biểu diễn tham chiếu bằng ID.
- FoodOrderItem lưu menuItemId dưới dạng Long, không ánh xạ @ManyToOne tới MenuItem. Tên món và đơn giá được lưu riêng để giữ thông tin tại thời điểm đặt.
- FoodOrder tham chiếu khách hàng và tài xế qua customerId, driverId; đây là mã định danh giữa dịch vụ, không phải quan hệ JPA tới User/DriverProfile.

Trạng thái đúng theo mã nguồn:

| Enum | Giá trị |
|---|---|
| RestaurantStatus | OPEN (mở cửa), CLOSED (đóng cửa), BUSY (đang bận). |
| OrderStatus | AWAITING_PAYMENT (chờ thanh toán), PENDING (chờ xử lý), ACCEPTED (đã chấp nhận), PREPARING (đang chuẩn bị), READY_FOR_PICKUP (sẵn sàng lấy món), DELIVERING (đang giao), COMPLETED (hoàn thành), CANCELLED (đã hủy), REJECTED (bị từ chối), NO_DRIVER_FOUND (không tìm được tài xế). |

Các điểm sửa so với hình cũ:

1. Loại CONFIRMED, PICKED_UP, DELIVERED vì không có trong OrderStatus hiện tại; bổ sung các giá trị đang dùng như bảng trên.
2. Bổ sung BUSY cho RestaurantStatus.
3. Sửa mô tả totalPrice: tổng tiền đơn = tổng subtotal của các món + deliveryFee. Không cộng deliveryFee thêm lần nữa khi diễn giải totalPrice.
4. Sửa quan hệ đánh giá: liên kết qua ID duy nhất, không phải @OneToOne.
5. Sơ đồ lược bỏ BaseEntity và các enum để dễ đọc. Restaurant, MenuItem, FoodOrder và FoodOrderReview kế thừa BaseEntity với createdAt, updatedAt; FoodOrderItem không kế thừa lớp này. BaseEntity là @MappedSuperclass, không phải bảng nghiệp vụ riêng.
6. Các trường chi tiết về tọa độ, giấy phép nhà hàng (licenseImageUrl), ảnh, khóa nhà hàng, thông tin hủy đơn, thử tìm tài xế lại và thời hạn sửa đánh giá được lược bỏ khỏi hình. Các thuộc tính liên kết restaurant, foodOrder, menuItems, orderItems được thể hiện bằng đường nối.

Nguồn đối chiếu trong food-delivery-service:
- entity: Restaurant, MenuItem, FoodOrder, FoodOrderItem, FoodOrderReview, BaseEntity.
- util/enums: OrderStatus, RestaurantStatus.
- dto/request/FoodOrderCreateRequest.java.
- service/impl/FoodOrderServiceImpl.java và FoodOrderReviewServiceImpl.java.
