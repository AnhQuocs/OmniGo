# Quy Tắc 02: Kiểm Soát Ranh Giới & Cấm Code Ngoài Yêu Cầu

## 1. Nguyên Tắc "Zero Unrequested Code" (Không Code Thừa)

AI tuyệt đối **KHÔNG ĐƯỢC**:
1. **Tự ý thêm tính năng phụ**: Ví dụ: người dùng yêu cầu làm API thanh toán Momo, AI không được tự ý viết thêm ZaloPay, VNPay nếu chưa được yêu cầu.
2. **Tự ý Refactor code không liên quan**: Khi mở một file để sửa một hàm 5 dòng, không được tự ý "làm đẹp" 10 hàm khác trong cùng file, không đổi tên biến/hàm của người khác.
3. **Tự ý thay đổi cấu trúc thư mục hoặc cấu hình**: Không tự sửa `pom.xml`, `build.gradle`, `package.json`, `application.yml` nếu tác vụ không liên quan đến việc cấu hình dependencies hay môi trường.
4. **Tự ý xoá comments/annotations**: Giữ nguyên 100% comment, Javadoc, TODO và annotation có sẵn trong code gốc.

---

## 2. Kỹ Thuật Chỉnh Sửa "Phẫu Thuật" (Surgical Edits)

Khi thay đổi code, AI phải tuân thủ nguyên lý can thiệp tối thiểu (Minimal Blast Radius):

*   **Chỉ nhắm trúng mục tiêu (Targeted Changes)**: Chỉ chạm vào những dòng code thực sự cần thay đổi để phục vụ yêu cầu.
*   **Không tạo code rác (No Dead Code)**: Không để lại comment code cũ bị bỏ (trừ khi người dùng bảo giữ), không tạo file tạm hay mock data dư thừa trong production code.
*   **Bảo toàn Style hiện có**: Nếu dự án dùng 4 spaces, không được tự ý format thành 2 spaces hoặc ngược lại. Tuân thủ conventions của từng module.

---

## 3. Biện Pháp Chống Ảo Giác & Suy Đoán (Anti-Hallucination Guardrails)

*   **Không bịa đặt API / Library**: Luôn kiểm tra xem module, method, class có thực sự tồn tại trong project hay không trước khi gọi.
*   **Khi thiếu thông tin / Mơ hồ**: Dừng lại và hỏi người dùng hoặc kiểm tra lại file config/spec hiện có. Không tự đưa ra giả định ngầm có rủi ro cao.
*   **Giới hạn tác động (Blast Radius Check)**: Trước khi hoàn tất, AI phải tự kiểm tra `git status` / `git diff` để đảm bảo không có file lạ nào bị sửa ngoài ý muốn.

---

## 4. Chống Gây Lỗi Hồi Quy & Bảo Toàn Mã Đang Hoạt Động (Zero Regression)

*   **Không làm gãy code đang chạy (Do No Harm)**: Mọi sửa đổi cho tính năng mới hoặc fix bug A tuyệt đối KHÔNG được làm hỏng tính năng B, C đang chạy ổn định.
*   **Không sửa đổi Signature/Contract chung khi chưa đánh giá**: Không được tự ý thay đổi tham số hàm (parameter list), kiểu trả về (return type) của các hàm utility, interface hoặc DTO dùng chung nếu có nhiều service/component khác đang phụ thuộc.
*   **Kiểm thử biên & Không gây Side-effects**: Khi sửa một logic, phải đảm bảo các luồng dữ liệu cũ vẫn trả về đúng kết quả như trước, không phát sinh NullPointer, lỗi cú pháp, hay sai lệch trạng thái ở nơi khác.

