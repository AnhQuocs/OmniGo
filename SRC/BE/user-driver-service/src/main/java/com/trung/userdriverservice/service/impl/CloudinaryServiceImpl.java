package com.trung.userdriverservice.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.trung.userdriverservice.exception.BadRequestException;
import com.trung.userdriverservice.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    @Override
    public String uploadImage(MultipartFile file) throws BadRequestException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String filename = file.getOriginalFilename();
        String contentType = file.getContentType();
        if (!isAllowedImageType(contentType, filename)) {
            throw new BadRequestException("Chỉ cho phép tải lên hình ảnh định dạng PNG, JPG/JPEG hoặc WEBP.");
        }

        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", "omnigo/driver_documents",
                    "resource_type", "auto"
            ));
            String url = (String) uploadResult.get("secure_url");
            log.info("Đã tải ảnh lên Cloudinary thành công: {}", url);
            return url;
        } catch (IOException e) {
            log.error("Lỗi khi tải ảnh lên Cloudinary: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể tải ảnh lên Cloudinary: " + e.getMessage());
        }
    }

    @Override
    public String uploadBase64(String base64Data) {
        if (base64Data == null || base64Data.trim().isEmpty()) {
            return null;
        }
        // Nếu đã là link URL http/https thì giữ nguyên
        if (base64Data.startsWith("http://") || base64Data.startsWith("https://")) {
            return base64Data;
        }

        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(base64Data, ObjectUtils.asMap(
                    "folder", "omnigo/driver_documents",
                    "resource_type", "auto"
            ));
            String url = (String) uploadResult.get("secure_url");
            log.info("Đã tải base64 lên Cloudinary thành công: {}", url);
            return url;
        } catch (Exception e) {
            log.warn("Không thể tải base64 lên Cloudinary (sử dụng chuỗi dữ liệu gốc): {}", e.getMessage());
            // Trả về dữ liệu gốc để không chặn luồng đăng ký nếu lỗi Cloudinary
            return base64Data;
        }
    }

    private boolean isAllowedImageType(String contentType, String filename) {
        if (contentType != null && isAllowedMime(contentType.toLowerCase())) {
            return true;
        }
        if (filename != null) {
            String fn = filename.toLowerCase();
            return fn.endsWith(".png") || fn.endsWith(".jpg") || fn.endsWith(".jpeg") || fn.endsWith(".webp");
        }
        return false;
    }

    private boolean isAllowedMime(String mime) {
        return "image/png".equals(mime)
                || "image/jpeg".equals(mime)
                || "image/jpg".equals(mime)
                || "image/webp".equals(mime);
    }
}
