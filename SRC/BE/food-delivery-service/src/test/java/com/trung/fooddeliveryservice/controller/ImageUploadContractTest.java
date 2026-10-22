package com.trung.fooddeliveryservice.controller;

import com.trung.fooddeliveryservice.service.CloudinaryService;
import com.trung.fooddeliveryservice.service.RestaurantService;
import com.trung.fooddeliveryservice.exception.GlobalExceptionHandler;
import com.trung.fooddeliveryservice.exception.ImageUploadException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ImageUploadContractTest {
    private final CloudinaryService cloud = mock(CloudinaryService.class);
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(
            new RestaurantController(mock(RestaurantService.class), cloud))
            .setControllerAdvice(new GlobalExceptionHandler()).build();

    @Test
    void multipartFileReturnsUrlInData() throws Exception {
        when(cloud.uploadImage(any())).thenReturn("https://res.cloudinary.com/demo/image/upload/a.png");
        mvc.perform(multipart("/api/v1/restaurants/upload-image").file(
                new MockMultipartFile("file", "a.png", "image/png", new byte[]{1})))
                .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.url").value("https://res.cloudinary.com/demo/image/upload/a.png"));
    }

    @Test
    void uploadFailureIsReturnedToClient() throws Exception {
        when(cloud.uploadImage(any())).thenThrow(new ImageUploadException("Không thể tải ảnh", HttpStatus.BAD_GATEWAY));
        mvc.perform(multipart("/api/v1/restaurants/upload-image").file(
                new MockMultipartFile("file", "a.png", "image/png", new byte[]{1})))
                .andExpect(status().isBadGateway()).andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void missingFileAndJsonUploadAreRejected() throws Exception {
        mvc.perform(multipart("/api/v1/restaurants/upload-image")).andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/restaurants/upload-image").contentType("application/json")
                .content("{\"file\":\"data:image/png;base64,AA==\"}"))
                .andExpect(status().isUnsupportedMediaType());
        verifyNoInteractions(cloud);
    }
}
