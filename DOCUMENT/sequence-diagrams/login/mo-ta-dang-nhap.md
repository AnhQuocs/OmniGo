### UC1.2 – Đăng nhập hệ thống và cấp phát token

Tác nhân: Người dùng có tài khoản (khách hàng, tài xế, quản trị viên, đối tác nhà hàng).

Người dùng nhập số điện thoại và mật khẩu. Yêu cầu đi qua API Gateway đến AuthController, sau đó được chuyển cho AuthService. Service tra cứu tài khoản và từ chối nếu tài khoản bị khóa. Với tài khoản không bị khóa, Spring Security tải thông tin người dùng, kiểm tra trạng thái tài khoản và đối chiếu mật khẩu bằng BCrypt. Khi xác thực thành công, hệ thống tạo Access Token, tạo và lưu Refresh Token vào Redis với thời hạn theo cấu hình. AuthController thiết lập cookie refresh_token có thuộc tính HttpOnly và trả thông tin người dùng cùng token cho ứng dụng. Nếu thông tin xác thực sai hoặc tài khoản bị vô hiệu hóa, hệ thống trả 401 Unauthorized.

Phạm vi bản rút gọn:
- Spring Security gộp AuthenticationManager, DaoAuthenticationProvider và CustomUserDetailsService; UserRepository đại diện truy cập dữ liệu, lược lifeline PostgreSQL.
- Các lời gọi tới JwtTokenProvider và RefreshTokenService được ghi ngay trên thông điệp, không tách thành lifeline để hình gọn.
- Phản hồi lỗi HTTP được ánh xạ bởi GlobalExceptionHandler; thành phần này được lược khỏi hình.
- Hình giả định request có đủ dữ liệu. Thiếu phoneNumber hoặc password bị validation trả 400 trước khi gọi AuthService.
- Access Token hết hạn theo cấu hình; dev hiện là 24 giờ. Refresh Token được kiểm tra với Redis, TTL dev là 7 ngày; JWT refresh hiện không chứa claim exp.
- AuthController tạo cookie HttpOnly, cấu hình hiện tại Secure=false và SameSite=Lax. Không mô tả cookie hiện tại có thuộc tính Secure.
- Body đăng nhập hiện chứa accessToken, refreshToken và user; cookie được thiết lập tại controller, không phải Gateway.
