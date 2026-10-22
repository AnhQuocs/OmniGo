package com.trung.fooddeliveryservice.service;

import com.trung.fooddeliveryservice.exception.BadRequestException;
import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {
    String uploadImage(MultipartFile file) throws BadRequestException;
    String validateImageUrl(String imageUrl);
}
