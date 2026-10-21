### d. Biểu đồ tuần tự UC4.1 – Khám phá quán ăn lân cận và Phân trang

**Tác nhân:** Khách hàng.

**Thành phần tham gia:** Customer App, RestaurantController, RestaurantService, RestaurantRepository và PostgreSQL. Hình lược API Gateway và giả định ứng dụng đã có tọa độ tìm kiếm; chuyển địa chỉ thành tọa độ là bước trước đó, không thuộc API nearby.

**Mô tả luồng:** Khách hàng chọn địa chỉ nhận hàng hoặc vị trí muốn tìm quán. Ứng dụng gửi tọa độ cùng bán kính và thông tin phân trang tới GET /api/v1/restaurants/nearby. RestaurantController chuyển tham số cho RestaurantService để kiểm tra tính hợp lệ. Nếu hợp lệ, service tạo Pageable và gọi repository truy vấn cơ sở dữ liệu. Truy vấn tính khoảng cách Haversine, lọc nhà hàng mở cửa, không bị khóa, có tọa độ và nằm trong bán kính yêu cầu. Kết quả được sắp xếp theo khoảng cách tăng dần, nếu bằng nhau thì theo id tăng dần, rồi trả về trang dữ liệu cùng tổng số kết quả. Service chuyển dữ liệu sang DTO và controller trả ApiResponse chứa Page cho ứng dụng hiển thị. Khi đổi trang, ứng dụng gửi lại yêu cầu với page tương ứng. Nếu tham số không hợp lệ, hệ thống trả 400 Bad Request.

*Hình 10: Biểu đồ tuần tự tìm nhà hàng lân cận và phân trang.*

**Quy tắc đúng theo backend:**
- Bắt buộc latitude, longitude; giá trị hữu hạn và trong khoảng [-90, 90], [-180, 180]. Backend dùng tọa độ được truyền vào; không tự lấy GPS hoặc gọi geocoding.
- radiusKm phải hữu hạn, lớn hơn 0; mặc định 5 km.
- page là số nguyên không âm, bắt đầu từ 0; mặc định 0.
- limit là số nguyên dương; mặc định 10. API chấp nhận size thay thế khi limit không có giá trị.
- Chỉ lấy status = OPEN, is_locked = false hoặc null, có latitude và longitude; khoảng cách không vượt radiusKm.
- Không giới hạn cứng tổng kết quả ở 5 nhà hàng. Mỗi phản hồi chỉ chứa một trang theo limit, kèm totalElements và các thông tin trang.
- Khoảng cách Haversine là khoảng cách địa lý ước tính theo tọa độ trên mặt cầu, không phải quãng đường đi thực tế.
- Phân trang giảm dữ liệu trả về; không khẳng định nó loại bỏ chi phí tính khoảng cách hoặc luôn tối ưu toàn bộ truy vấn.
- Không có kết quả phù hợp vẫn trả 200 với trang rỗng.

**Sửa so với hình cũ:** thay lat/lng/radius bằng latitude/longitude/radiusKm; tách bước geocoding khỏi luồng API; bổ sung điều kiện không khóa và nhánh lỗi; phản hồi là ApiResponse<Page<RestaurantNearbyResponse>>, không chỉ Page JSON trần. Việc ứng dụng dùng Photon và chọn vị trí mặc định không được khẳng định từ riêng mã nguồn API này.

Nguồn: food-delivery-service/controller/RestaurantController.java; service/impl/RestaurantServiceImpl.java; repository/RestaurantRepository.java. Kết quả đối chiếu tĩnh mã nguồn, không phải kiểm thử hiệu năng hay kiểm thử đầu cuối.
