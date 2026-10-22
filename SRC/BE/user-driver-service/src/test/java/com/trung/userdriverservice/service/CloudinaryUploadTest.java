package com.trung.userdriverservice.service;

import com.cloudinary.Cloudinary;
import com.trung.userdriverservice.service.impl.CloudinaryServiceImpl;
import com.trung.userdriverservice.exception.BadRequestException;
import com.trung.userdriverservice.exception.ImageUploadException;
import com.trung.userdriverservice.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import java.io.IOException;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CloudinaryUploadTest {
    private static final String URL = "https://res.cloudinary.com/demo/image/upload/v1/photo.png";
    private final Cloudinary cloud = mock(Cloudinary.class, RETURNS_DEEP_STUBS);
    private final CloudinaryServiceImpl service = new CloudinaryServiceImpl(cloud);
    private MockMultipartFile image() {
        return new MockMultipartFile("file", "a.png", "image/png", new byte[]{1,2,3});
    }

    @Test
    void businessRequestRejectsBase64AndExternalUrlsWithoutUpload() {
        for (String input : new String[]{"data:image/png;base64,aGVsbG8=", "aGVsbG8=",
                "https://example.com/photo.png", "https://res.cloudinary.com.evil.example/demo/image/upload/a.png", ""}) {
            ImageUploadException error = assertThrows(ImageUploadException.class, () -> service.validateImageUrl(input));
            assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
        }
        verifyNoInteractions(cloud);
    }

    @Test
    void businessRequestUsesUploadedUrlAndAllowsAbsentOptionalImage() {
        assertEquals(URL, service.validateImageUrl(URL));
        assertNull(service.validateImageUrl(null));
        verifyNoInteractions(cloud);
    }

    @Test
    void multipartFailureReturns502ToUser() throws Exception {
        when(cloud.uploader().upload(any(), anyMap())).thenThrow(new IOException("provider secret detail"));
        ImageUploadException error = assertThrows(ImageUploadException.class, () -> service.uploadImage(image()));
        var response = new GlobalExceptionHandler().handleImageUpload(error);
        assertEquals(502, response.getStatusCode().value());
        assertFalse(response.getBody().isSuccess());
        assertEquals("IMAGE_UPLOAD_FAILED", response.getBody().getError());
        assertFalse(response.getBody().getMessage().contains("provider secret"));
    }

    @Test
    void invalidCloudinaryResponseFailsInsteadOfSavingInvalidUrl() throws Exception {
        when(cloud.uploader().upload(any(), anyMap())).thenReturn(Map.of());
        assertThrows(ImageUploadException.class, () -> service.uploadImage(image()));
        when(cloud.uploader().upload(any(), anyMap())).thenReturn(Map.of("secure_url", "https://example.com/a.png"));
        assertThrows(ImageUploadException.class, () -> service.uploadImage(image()));
    }

    @Test
    void emptyOrUnsupportedMultipartIsRejected() {
        assertThrows(BadRequestException.class, () -> service.uploadImage(
                new MockMultipartFile("file", "a.png", "image/png", new byte[0])));
        assertThrows(BadRequestException.class, () -> service.uploadImage(
                new MockMultipartFile("file", "a.pdf", "application/pdf", new byte[]{1})));
        verifyNoInteractions(cloud);
    }

    @Test
    void multipartSuccessUploadsBytesAndReturnsUrl() throws Exception {
        when(cloud.uploader().upload(any(), anyMap())).thenReturn(Map.of("secure_url", URL));
        assertEquals(URL, service.uploadImage(image()));
        verify(cloud.uploader()).upload(org.mockito.AdditionalMatchers.aryEq(new byte[]{1,2,3}), argThat(options -> "image".equals(options.get("resource_type"))));
    }
}