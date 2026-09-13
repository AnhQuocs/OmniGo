package com.trung.fooddeliveryservice.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.trung.fooddeliveryservice.exception.BadRequestException;
import com.trung.fooddeliveryservice.service.CloudinaryService;
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
            throw new BadRequestException("Chỉ cho phép tải lên ảnh định dạng PNG, JPG/JPEG hoặc WEBP.");
        }

        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", "omnigofood/reviews",
                    "resource_type", "auto"
            ));
            String url = (String) uploadResult.get("secure_url");
            log.info("Uploaded file to Cloudinary successfully: {}", url);
            return url;
        } catch (IOException e) {
            log.error("Failed to upload image file to Cloudinary: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể tải ảnh lên Cloudinary: " + e.getMessage());
        }
    }

    @Override
    public String uploadBase64(String base64Data) {
        if (base64Data == null || base64Data.trim().isEmpty()) {
            return null;
        }
        // If already a remote URL, return as is
        if (base64Data.startsWith("http://") || base64Data.startsWith("https://")) {
            return base64Data;
        }

        // Validate base64 data url mime type
        if (base64Data.startsWith("data:")) {
            int semiIdx = base64Data.indexOf(';');
            if (semiIdx > 5) {
                String mime = base64Data.substring(5, semiIdx).toLowerCase();
                if (!isAllowedMime(mime)) {
                    log.warn("Bỏ qua ảnh base64 không đúng định dạng cho phép: {}", mime);
                    return null;
                }
            }
        }

        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(base64Data, ObjectUtils.asMap(
                    "folder", "omnigofood/reviews",
                    "resource_type", "auto"
            ));
            String url = (String) uploadResult.get("secure_url");
            log.info("Uploaded base64 image to Cloudinary successfully: {}", url);
            return url;
        } catch (Exception e) {
            log.error("Failed to upload base64 image to Cloudinary: {}", e.getMessage(), e);
            // If upload fails, fallback to raw string rather than throwing error
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
