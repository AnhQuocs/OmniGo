package com.trung.userdriverservice.service;

import com.trung.userdriverservice.entity.User;
import com.trung.userdriverservice.entity.DriverProfile;
import com.trung.userdriverservice.exception.BadRequestException;
import com.trung.userdriverservice.exception.ResourceNotFoundException;
import com.trung.userdriverservice.mapper.UserMapper;
import com.trung.userdriverservice.repository.DriverProfileRepository;
import com.trung.userdriverservice.repository.UserRepository;
import com.trung.userdriverservice.util.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserAvatarServiceTest {
    private final UserRepository users = mock(UserRepository.class);
    private final DriverProfileRepository drivers = mock(DriverProfileRepository.class);
    private final CloudinaryService cloudinary = mock(CloudinaryService.class);
    private final UserMapper mapper = new UserMapper(mock(PasswordEncoder.class), drivers);
    private final UserAvatarService service = new UserAvatarService(users, mapper, cloudinary);
    private final MockMultipartFile image = new MockMultipartFile("file", "avatar.png", "image/png", new byte[]{1, 2});

    private User user(Role role) {
        User user = new User();
        user.setId(1L);
        user.setRole(role);
        user.setAvatarUrl("https://example.com/old.png");
        when(users.findById(1L)).thenReturn(Optional.of(user));
        when(users.save(user)).thenReturn(user);
        return user;
    }

    @Test
    void savesCustomerAvatarAndReturnsItInProfile() throws Exception {
        User user = user(Role.CUSTOMER);
        when(cloudinary.uploadImage(image)).thenReturn("https://example.com/new.png");
        assertEquals("https://example.com/new.png", service.updateAvatar(1L, image).getAvatarUrl());
        assertEquals("https://example.com/new.png", user.getAvatarUrl());
        verify(users).save(user);
    }

    @Test
    void driverAvatarAppearsInPublicAndInternalProfiles() throws Exception {
        User user = user(Role.DRIVER);
        DriverProfile profile = new DriverProfile();
        profile.setUser(user);
        when(drivers.findById(1L)).thenReturn(Optional.of(profile));
        when(cloudinary.uploadImage(image)).thenReturn("https://example.com/driver.png");
        assertEquals("https://example.com/driver.png", service.updateAvatar(1L, image).getAvatarUrl());
        assertEquals("https://example.com/driver.png", mapper.toDriverInternalResponse(profile).getAvatarUrl());
    }

    @Test
    void legacyAccountHasNullAvatar() {
        User user = user(Role.CUSTOMER);
        user.setAvatarUrl(null);
        assertNull(mapper.toUserResponse(user).getAvatarUrl());
    }

    @Test
    void rejectsEmptyFile() {
        user(Role.CUSTOMER);
        assertThrows(BadRequestException.class, () -> service.updateAvatar(1L,
                new MockMultipartFile("file", new byte[0])));
        verifyNoInteractions(cloudinary);
    }

    @Test
    void rejectsNonImageEvenWithImageExtension() {
        user(Role.CUSTOMER);
        assertThrows(BadRequestException.class, () -> service.updateAvatar(1L,
                new MockMultipartFile("file", "fake.png", "text/plain", new byte[]{1})));
        verifyNoInteractions(cloudinary);
    }

    @Test
    void rejectsOversizedImage() {
        user(Role.CUSTOMER);
        assertThrows(BadRequestException.class, () -> service.updateAvatar(1L,
                new MockMultipartFile("file", "big.png", "image/png", new byte[1024 * 1024 + 1])));
        verifyNoInteractions(cloudinary);
    }

    @Test
    void uploadFailureDoesNotReplaceExistingAvatar() throws Exception {
        User user = user(Role.CUSTOMER);
        when(cloudinary.uploadImage(image)).thenThrow(new RuntimeException("Upload failed"));
        assertEquals(502, assertThrows(ResponseStatusException.class,
                () -> service.updateAvatar(1L, image)).getStatusCode().value());
        assertEquals("https://example.com/old.png", user.getAvatarUrl());
        verify(users, never()).save(any());
    }

    @Test
    void invalidUploadResultDoesNotReplaceExistingAvatar() throws Exception {
        User user = user(Role.CUSTOMER);
        when(cloudinary.uploadImage(image)).thenReturn(null);
        assertThrows(ResponseStatusException.class, () -> service.updateAvatar(1L, image));
        assertEquals("https://example.com/old.png", user.getAvatarUrl());
        verify(users, never()).save(any());
    }

    @Test
    void missingUserDoesNotUploadFile() {
        assertThrows(ResourceNotFoundException.class, () -> service.updateAvatar(99L, image));
        verifyNoInteractions(cloudinary);
    }
}
