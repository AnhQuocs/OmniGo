package com.trung.userdriverservice.service;

import com.trung.userdriverservice.dto.response.UserResponse;
import com.trung.userdriverservice.exception.BadRequestException;
import com.trung.userdriverservice.exception.ResourceNotFoundException;
import com.trung.userdriverservice.mapper.UserMapper;
import com.trung.userdriverservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserAvatarService {
    private static final long MAX_AVATAR_BYTES = 1024 * 1024;
    private static final Set<String> IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final CloudinaryService cloudinaryService;

    @Transactional
    public UserResponse updateAvatar(Long userId, MultipartFile file)
            throws BadRequestException, ResourceNotFoundException {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Vui lòng chọn ảnh đại diện");
        }
        if (file.getSize() > MAX_AVATAR_BYTES) {
            throw new BadRequestException("Ảnh đại diện không được vượt quá 1 MB");
        }
        if (file.getContentType() == null || !IMAGE_TYPES.contains(file.getContentType())) {
            throw new BadRequestException("Ảnh đại diện phải có định dạng JPEG, PNG hoặc WEBP");
        }
        String url;
        try {
            url = cloudinaryService.uploadImage(file);
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Không thể tải ảnh đại diện", ex);
        }
        if (url == null || !url.startsWith("https://") || url.length() > 2048) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Dịch vụ ảnh trả về URL không hợp lệ");
        }
        user.setAvatarUrl(url);
        return userMapper.toUserResponse(userRepository.save(user));
    }
}
