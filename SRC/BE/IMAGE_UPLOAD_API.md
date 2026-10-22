# Luồng upload ảnh qua multipart

Client upload file lên BE bằng `multipart/form-data`, với part tên chính xác `file`.
BE upload bytes lên Cloudinary và trả URL ở `data.url`. Client truyền URL này vào
API tạo/sửa dữ liệu. API nghiệp vụ chỉ kiểm tra URL và lưu URL vào database, không
upload lại ảnh, không nhận base64 và không tải ảnh từ URL ngoài Cloudinary.

| Ảnh | Endpoint upload | Xác thực | Trường dùng URL khi lưu nghiệp vụ |
| --- | --- | --- | --- |
| Giấy tờ tài xế | `POST /api/v1/drivers/upload-document` | Public để upload trước đăng ký | `cccdFrontImage`, `cccdBackImage`, `gplxFrontImage`, `gplxBackImage` |
| Nhà hàng, giấy phép, món ăn | `POST /api/v1/restaurants/upload-image` | Public để upload trước đăng ký đối tác | `imageUrl`, `licenseImageUrl`; món ăn dùng `imageUrl` |
| Ảnh đánh giá | `POST /api/v1/food-reviews/upload-image` | Bearer token | Các URL trong `restaurantImages` hoặc alias `images` |

Ví dụ response upload thành công, URL chỉ minh họa:

```json
{
  "success": true,
  "message": "Tải ảnh lên thành công",
  "data": {
    "url": "https://res.cloudinary.com/demo/image/upload/v1/photo.png"
  }
}
```

Response thực tế có thể thêm `timestamp`; message tùy endpoint. Client lấy
`data.url` làm giá trị trường ảnh trong JSON nghiệp vụ. Upload nhiều ảnh thì gửi
từng file và tập hợp các URL trả về. Không tự đặt Content-Type thiếu boundary;
để thư viện HTTP tạo multipart boundary.

Avatar giữ endpoint tiện ích hiện tại `PUT /api/v1/users/me/avatar`: nhận multipart
`file`, upload Cloudinary rồi lưu URL vào `users.avatar_url` trong cùng request;
response là `ApiResponse<UserResponse>` với `data.avatarUrl`. Endpoint này cần
Bearer token và không nhận JSON/base64.

API nghiệp vụ chấp nhận URL HTTPS có host `res.cloudinary.com` và đường dẫn
`/{cloud}/image/upload/...`; trường ảnh tùy chọn cho phép null. Base64, chuỗi rỗng
và URL ngoài Cloudinary bị trả HTTP 400. Đây là kiểm tra cấu trúc URL, không phải
truy vấn xác minh asset tồn tại trên Cloudinary.

Lỗi upload: file thiếu/rỗng/sai định dạng trả 400; Content-Type không hỗ trợ trả
415; quá dung lượng cấu hình trả 413; Cloudinary lỗi hoặc không trả URL hợp lệ
trả 502. Không trả thành công kèm URL rỗng và không fallback lưu base64.
Các response lỗi có `success=false` và `message`; lỗi từ ImageUploadException
có `error=IMAGE_UPLOAD_FAILED`. Avatar có handler lỗi riêng.

Triển khai đồng bộ api-gateway, food-delivery-service và user-driver-service.
Client cũ gửi base64 phải chuyển sang upload multipart trước. Dữ liệu ảnh cũ trong
database chưa được di chuyển; các API chỉ áp dụng quy tắc này cho dữ liệu ghi mới.
