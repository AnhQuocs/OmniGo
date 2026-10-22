package com.trung.userdriverservice.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.trung.userdriverservice.exception.BadRequestException;
import com.trung.userdriverservice.exception.ImageUploadException;
import com.trung.userdriverservice.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.net.URI;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryServiceImpl implements CloudinaryService {
    private final Cloudinary cloudinary;

    @Override
    public String uploadImage(MultipartFile file) throws BadRequestException {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Vui lòng chọn ảnh để tải lên.");
        }
        if (!isAllowedImageType(file.getContentType(), file.getOriginalFilename())) {
            throw new BadRequestException("Chỉ cho phép ảnh PNG, JPG/JPEG hoặc WEBP.");
        }
        try {
            return upload(file.getBytes());
        } catch (java.io.IOException error) {
            log.error("Cannot read image for upload", error);
            throw new ImageUploadException("Không thể đọc ảnh tải lên. Vui lòng chọn lại ảnh.", HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public String validateImageUrl(String input) {
        // Business requests only store URLs returned by the multipart upload endpoint.
        if (input == null) return null;
        String url = input.trim();
        if (!isCloudinaryImageUrl(url)) {
            throw new ImageUploadException(
                    "Vui lòng upload ảnh bằng multipart/form-data trước, sau đó gửi URL Cloudinary được trả về.",
                    HttpStatus.BAD_REQUEST);
        }
        return url;
    }
    private String upload(byte[] image) {
        try {
            Map<?, ?> result = cloudinary.uploader().upload(image, ObjectUtils.asMap(
                    "folder", "omnigo/driver_documents", "resource_type", "image"));
            Object url = result == null ? null : result.get("secure_url");
            if (!(url instanceof String) || !isCloudinaryImageUrl((String) url)) {
                throw new IllegalStateException("Cloudinary returned no valid image URL");
            }
            return (String) url;
        } catch (Exception error) {
            log.error("Cloudinary image upload failed", error);
            throw new ImageUploadException("Không thể tải ảnh lên Cloudinary. Vui lòng thử lại sau.", HttpStatus.BAD_GATEWAY);
        }
    }

    private boolean isCloudinaryImageUrl(String value) {
        try {
            URI uri = URI.create(value);
            return "https".equalsIgnoreCase(uri.getScheme())
                    && "res.cloudinary.com".equalsIgnoreCase(uri.getHost())
                    && uri.getUserInfo() == null && uri.getPort() == -1
                    && uri.getPath() != null && uri.getPath().matches("/[^/]+/image/upload/.+");
        } catch (IllegalArgumentException error) {
            return false;
        }
    }

    private boolean isAllowedImageType(String contentType, String filename) {
        if (contentType != null && isAllowedMime(contentType.toLowerCase(Locale.ROOT))) return true;
        if (filename == null) return false;
        String name = filename.toLowerCase(Locale.ROOT);
        return name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".webp");
    }

    private boolean isAllowedMime(String mime) {
        return "image/png".equals(mime) || "image/jpeg".equals(mime)
                || "image/jpg".equals(mime) || "image/webp".equals(mime);
    }
}