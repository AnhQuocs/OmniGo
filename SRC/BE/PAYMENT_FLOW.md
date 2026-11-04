# Luồng thanh toán và triển khai

## Cấu hình bắt buộc

- Đặt cùng một `INTERNAL_API_TOKEN` ngẫu nhiên, đủ dài (ít nhất 32 byte ngẫu nhiên) cho `api-gateway`, `payment-service`, `booking-service`, `food-delivery-service`. Không đưa token này vào app/web hoặc Git. Khi thiếu token, luồng được bảo vệ sẽ từ chối yêu cầu.
- Giữ các dịch vụ backend trong mạng riêng; app gọi qua Gateway. Gateway gắn `X-Gateway-Token` sau khi xác thực JWT. Các cuộc gọi tài chính giữa dịch vụ dùng `X-Internal-Token`; Gateway chặn truy cập bên ngoài tới các đường nội bộ và đường `/food-orders/{id}/paid`.
- `payment-service`: `APP_BOOKING_URL`, `APP_FOOD_DELIVERY_URL`; hai dịch vụ đơn hàng: `APP_PAYMENT_SERVICE_URL`. Docker Compose đã đặt URL nội bộ.
- VNPay: đăng ký URL IPN công khai `https://<gateway>/api/v1/payments/vnpay/ipn` với VNPay; URL return là `/api/v1/payments/vnpay/return`. Chỉ cấu hình return sẽ không xác nhận thanh toán.
- MoMo: URL IPN là `/api/v1/payments/momo/ipn`, URL return là `/api/v1/payments/momo/return`. Giữ đúng mã merchant và secret tương ứng môi trường thử nghiệm/thật.
- Chạy các migration mới: payment `V3__payment_integrity.sql`; booking và food `V20__financial_outbox.sql`. Cập nhật đồng bộ Gateway và cả ba dịch vụ, tránh để bản cũ/bản mới cùng nhận yêu cầu tài chính.
- Tỷ lệ sàn vẫn lấy từ Redis DB `PAYMENT_REDIS_DB` (mặc định 5), key `system:config:platform-fee:percentage`, không hết hạn. PostgreSQL lưu tiền, giao dịch và công việc quyết toán để có thể khôi phục khi Redis/mạng lỗi.

## Hành vi của API

- `POST /api/v1/payments/payment/create`: backend kiểm tra chủ đơn, giá và trạng thái từ dịch vụ đơn hàng; không dùng `driverId` do app gửi để xác định người nhận tiền. Chỉ chấp nhận số tiền nguyên VND dương, tối đa 100 triệu VND/yêu cầu.
- Đồ ăn online thanh toán khi `AWAITING_PAYMENT`; cuốc xe online thanh toán khi `COMPLETED`. Tiền mặt không tạo giao dịch thanh toán online.
- Yêu cầu tạo lại cho cùng đơn trả giao dịch đang chờ nếu cùng cổng, hoặc báo xung đột nếu khác cổng. Đơn đã trả tiền trả `paymentUrl: ALREADY_PAID`; app kiểm tra trạng thái thay vì mở giá trị này như URL. `WALLET_SUCCESS` cũng không phải URL.
- MoMo IPN kiểm tra chữ ký, merchant, requestId, cổng và số tiền. VNPay IPN kiểm tra chữ ký, merchant, cổng, số tiền và hai mã kết quả. Callback lặp/đồng thời chỉ cập nhật tiền một lần.
- Yêu cầu MoMo được lưu `PENDING` trước khi gọi cổng. Nếu phản hồi bị mất, callback vẫn tìm được mã giao dịch; app kiểm tra trạng thái thay vì tự tạo ngay lần thanh toán khác.
- Return URL chỉ hiển thị trạng thái trong cơ sở dữ liệu; không cộng tiền hay tin mã thành công trên trình duyệt. App cần kiểm tra trạng thái, có thể chờ vài giây để nhận IPN và đồng bộ đơn.
- `/wallet/deposit` cũ bị chặn: không còn tự cộng tiền khi chưa qua cổng thanh toán. Dùng `CUSTOMER_DEPOSIT` hoặc `DRIVER_DEPOSIT` với MoMo/VNPay.
- Nút đồng bộ thanh toán của admin chỉ đồng bộ một giao dịch đã được xác thực, không tự tạo thanh toán thành công.

## Quyết toán và hoàn tiền

- Cuốc xe CASH: khách trả trực tiếp tài xế, ví tài xế chỉ bị trừ phí sàn.
- Giao đồ ăn CASH: tài xế đã giữ phần tiền ship thu từ khách, chỉ trừ phí ship của sàn; quán đã nhận tiền món từ tài xế, ví quán chỉ bị trừ phí món của sàn.
- Đơn online: tài xế và quán nhận phần tương ứng sau phí vào ví. Tổng phí không được tính trên toàn bộ tiền đơn hai lần.
- Hoàn tiền dựa trên giao dịch thành công gốc và trạng thái đơn được backend xác nhận, dùng tham chiếu cố định cho mỗi giao dịch gốc. Hoàn vào ví khách hàng theo cơ chế hiện có, không phải chuyển trả qua API hoàn tiền MoMo/VNPay.
- Thanh toán đến muộn sau hủy/đổi CASH, hoặc hai lần thanh toán thành công cho cùng đơn: khoản cần trả lại được hoàn vào ví một lần; không trả thêm cho tài xế/quán.
- Trạng thái kết thúc không cho mở lại/hủy lại bằng thao tác của tài xế. Luồng đặt xe phải đi qua ACCEPTED → ARRIVED → IN_PROGRESS → COMPLETED.
- Chỉ cập nhật tỷ lệ Redis không tính lại giao dịch đã quyết toán. Tỷ lệ được đọc tại lần quyết toán thành công.

## Khôi phục khi lỗi

- Bảng `financial_outbox` của booking/food lưu mã đơn cần đối soát cùng giao dịch thay đổi trạng thái. Worker gửi lại sau lỗi; các cột `attempts`, `next_attempt_at`, `last_error` hỗ trợ theo dõi.
- Payment đã nhận IPN thành công được lưu `SUCCESS` trước khi đồng bộ đơn/quyết toán. Các cột `fulfillment_done`, `fulfillment_attempts`, `fulfillment_next_attempt_at` trong `transactions` lưu tiến độ. Lỗi mạng/Redis giữ công việc để thử lại.
- Không xóa bản ghi chờ để giải quyết lỗi. Sửa kết nối/token/dữ liệu rồi để worker chạy lại. Không sửa số dư ví bằng tay.
- Migration đánh dấu các giao dịch lịch sử đã tồn tại là đã xử lý để tránh tự chi trả lần nữa; các giao dịch cũ từng bị mất quyết toán cần đối soát riêng trước khi đánh dấu cần chạy lại.

## Kiểm thử

- Kiểm thử đơn vị: xác thực thông báo cổng, đối chiếu đơn, bảo vệ API nội bộ và tính phí.
- `PaymentConcurrencyTest` dùng PostgreSQL riêng qua `TEST_POSTGRES_URL`, user `codex_test`, mật khẩu rỗng dành riêng cho dữ liệu thử nghiệm. Nó tạo/xóa bảng nên tuyệt đối không dùng URL cơ sở dữ liệu thật.
- `FinancialOutboxIntegrationTest` ở booking/food dùng PostgreSQL thử nghiệm và HTTP server cục bộ để kiểm tra rollback, giao lặp, lỗi mạng và khởi động lại worker.
- Cần chạy thêm giao dịch thử nghiệm với tài khoản merchant của dự án sau khi đăng ký IPN; kiểm thử cục bộ không xác minh được cấu hình merchant trên MoMo/VNPay.

Tài liệu cổng tham chiếu: [MoMo IPN](https://developers.momo.vn/v3/vi/docs/payment/api/result-handling/notification/), [VNPay 2.1.0](https://sandbox.vnpayment.vn/apis/files/VNPAY%20Payment%20Gateway_Techspec%202.1.0-VN.pdf).


## Idempotency và chặn đơn mới

- Bắt buộc `Idempotency-Key` (16–128 ký tự chữ, số, `_`, `-`) cho `POST /api/v1/bookings`, `POST /api/v1/food-orders` và `POST /api/v1/payments/payment/create`. Client cũ phải cập nhật trước khi triển khai backend này. Hai trang demo đã gửi key và giữ lại qua tải lại trang/lỗi mạng.
- Một ý định tạo đơn hoặc thanh toán dùng một key. Gửi lại cùng key và cùng nội dung trả kết quả đã lưu; đổi nội dung với key cũ trả 409. Không tạo key mới chỉ vì timeout. Key tạo đơn được tách theo khách hàng và dịch vụ; key thanh toán tách theo vai trò và tài khoản.
- `order_idempotency` ở booking/food lưu kết quả cùng transaction tạo đơn. Lỗi nghiệp vụ rollback đơn rồi ghi FAILED riêng. `customer_order_admission` ở payment giữ chỗ theo khách, khóa PostgreSQL chung cho cả đặt xe và đồ ăn. Yêu cầu thứ hai với key khác bị chặn khi yêu cầu trước chưa rõ kết quả. Không dùng TTL để tự mở khóa một yêu cầu chưa được xác minh.
- Trước khi cấp chỗ mới, backend kiểm tra cả đơn lịch sử xe và đồ ăn: đơn bị hủy/từ chối không chặn; tiền mặt chỉ hết chặn khi hoàn tất; online chỉ hết chặn khi payment có SUCCESS chưa hoàn tiền. Mất kết nối kiểm tra đơn sẽ chặn đặt mới. Các điểm kiểm tra này ở backend, không phụ thuộc khóa nút, tab hoặc thiết bị.
- `payment_idempotency` lưu hash nội dung, kết quả và mã giao dịch cổng. Với MoMo, mã cổng được gắn bền vững vào key trong transaction độc lập **trước** khi gửi HTTP. Timeout sau khi gửi không được biến thành một yêu cầu thu tiền mới. Wallet/VNPay lưu kết quả cùng transaction nghiệp vụ.
- Cổng xác nhận FAILED: trả 422 để client kết thúc lần cũ; lần bấm tiếp theo dùng key mới. CANCELED do hết thời gian chờ cục bộ **không** chứng minh cổng đã hủy: giữ chặn và cần đối soát/callback đã xác thực. Không có tác vụ tự hỏi trạng thái merchant trong thay đổi này; không đánh dấu đã trả hoặc mở khóa bằng tay khi chưa kiểm tra cổng.
- Không cho chuyển đơn online sang CASH trong lúc kết quả chưa chắc chắn, vì callback và cập nhật isPaid bất đồng bộ có thể đến sau. Chọn CASH ngay khi tạo đơn vẫn được hỗ trợ.
- Demo có nút gửi lại yêu cầu đặt đơn chưa rõ kết quả, dùng đúng key và nội dung đã lưu. Không xóa localStorage/key trước khi khôi phục yêu cầu đó. Nạp ví mới sau khi nhận được phản hồi khởi tạo được coi là một ý định mới; retry do mất phản hồi vẫn dùng key cũ.
- Triển khai migration payment `V4__request_idempotency.sql`, booking/food `V21__order_idempotency.sql`, cùng backend và các client đã cập nhật. Các bảng idempotency không tự hết hạn; chỉ xây dựng chính sách lưu trữ sau khi xác định thời gian retry và yêu cầu đối soát. Tỷ lệ phí sàn vẫn lưu Redis như trước; sổ chống trùng dùng PostgreSQL để cùng commit/rollback với tiền và đơn.
- Kiểm thử mới: `IdempotencyIntegrationTest` (payment) và `OrderCreationGuardIntegrationTest` (cả booking/food) cần `TEST_POSTGRES_URL` trỏ database thử nghiệm. Kiểm tra gửi đồng thời, khác key giữa xe/đồ ăn, nội dung khác cùng key, rollback, lỗi cổng và khôi phục. Demo: `node --test SRC/BE/frontend/payment-flow.test.cjs SRC/BE/frontend/request-idempotency.test.cjs`.


### Sửa nhánh lỗi thanh toán
- Demo chỉ kết thúc idempotency intent sau khi đọc trọn JSON và kiểm tra phản hồi thành công có mã đơn/giao dịch. HTTP 200 nhưng mất body hoặc JSON sai vẫn giữ key để gửi lại.
- Food giữ nguyên HTTP status của ResponseStatusException, gồm 409 (đang xử lý/xung đột), 422 (thất bại chắc chắn), 503 (dịch vụ chưa sẵn sàng).
- MoMo: các lỗi khởi tạo 20/21/22 và thất bại 1001/1006, có đúng mã đơn và merchant, được lưu FAILED trong transaction độc lập rồi trả 422. Mã trùng 40/41, đang xử lý, không rõ kết quả, lỗi HTTP không có dữ liệu xác định hoặc timeout vẫn giữ pending để đối soát. Không tự đánh dấu thất bại chỉ vì HTTP lỗi. Cập nhật FAILED có điều kiện, không ghi đè SUCCESS của IPN.
- Tham chiếu mã kết quả: https://developers.momo.vn/v3/docs/payment/api/result-handling/resultcode/
