package com.trung.fooddeliveryservice.service;

import com.trung.fooddeliveryservice.dto.request.MenuItemRequest;
import com.trung.fooddeliveryservice.entity.MenuItem;
import com.trung.fooddeliveryservice.entity.Restaurant;
import com.trung.fooddeliveryservice.exception.ImageUploadException;
import com.trung.fooddeliveryservice.mapper.MenuItemMapper;
import com.trung.fooddeliveryservice.repository.MenuItemRepository;
import com.trung.fooddeliveryservice.repository.RestaurantRepository;
import com.trung.fooddeliveryservice.service.impl.MenuItemServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class MenuItemImageUploadTest {
    private final MenuItemRepository items = mock(MenuItemRepository.class);
    private final RestaurantRepository restaurants = mock(RestaurantRepository.class);
    private final CloudinaryService cloud = mock(CloudinaryService.class);
    private final MenuItemServiceImpl service = new MenuItemServiceImpl(items, restaurants, new MenuItemMapper(), cloud);

    @Test
    void createPersistsOnlyValidatedUrl() throws Exception {
        when(restaurants.findById(1L)).thenReturn(Optional.of(Restaurant.builder().id(1L).ownerId(2L).build()));
        var request = new MenuItemRequest();
        request.setImageUrl("https://res.cloudinary.com/demo/image/upload/a.png");
        when(cloud.validateImageUrl(request.getImageUrl())).thenReturn("https://res.cloudinary.com/demo/image/upload/a.png");
        when(items.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var response = service.addMenuItem(1L, request, 2L);
        assertEquals("https://res.cloudinary.com/demo/image/upload/a.png", response.getImageUrl());
        verify(items).save(argThat(item -> item.getImageUrl().startsWith("https://res.cloudinary.com/")));
    }

    @Test
    void createDoesNotSaveWhenUrlValidationFails() {
        when(restaurants.findById(1L)).thenReturn(Optional.of(Restaurant.builder().ownerId(2L).build()));
        var request = new MenuItemRequest(); request.setImageUrl("base64");
        when(cloud.validateImageUrl("base64")).thenThrow(new ImageUploadException("Invalid image URL", HttpStatus.BAD_REQUEST));
        assertThrows(ImageUploadException.class, () -> service.addMenuItem(1L, request, 2L));
        verify(items, never()).save(any());
    }

    @Test
    void updateDoesNotSaveWhenUrlValidationFails() {
        var item = MenuItem.builder().restaurant(Restaurant.builder().ownerId(2L).build()).build();
        when(items.findById(1L)).thenReturn(Optional.of(item));
        var request = new MenuItemRequest(); request.setImageUrl("base64");
        when(cloud.validateImageUrl("base64")).thenThrow(new ImageUploadException("Invalid image URL", HttpStatus.BAD_REQUEST));
        assertThrows(ImageUploadException.class, () -> service.updateMenuItem(1L, request, 2L));
        verify(items, never()).save(any());
    }

    @Test
    void updateKeepsExistingImageWithoutRevalidating() throws Exception {
        var existingUrl = "https://images.unsplash.com/photo-123";
        var item = MenuItem.builder()
                .id(1L)
                .restaurant(Restaurant.builder().id(1L).ownerId(2L).build())
                .name("Old Name")
                .price(new BigDecimal("50000"))
                .imageUrl(existingUrl)
                .isAvailable(true)
                .build();
        when(items.findById(1L)).thenReturn(Optional.of(item));
        when(items.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var request = new MenuItemRequest();
        request.setName("New Name");
        request.setPrice(new BigDecimal("60000"));
        request.setImageUrl(existingUrl);
        request.setIsAvailable(false);

        var response = service.updateMenuItem(1L, request, 2L);
        assertEquals(existingUrl, response.getImageUrl());
        assertEquals(false, response.getIsAvailable());
        verify(cloud, never()).validateImageUrl(any());
        verify(items).save(argThat(saved -> !saved.getIsAvailable() && existingUrl.equals(saved.getImageUrl())));
    }

    @Test
    void updateItemAvailabilityTogglesWithoutTouchingImageOrDetails() throws Exception {
        var existingUrl = "https://example.com/external.jpg";
        var item = MenuItem.builder()
                .id(1L)
                .restaurant(Restaurant.builder().id(1L).ownerId(2L).build())
                .name("Lobster")
                .price(new BigDecimal("350000"))
                .imageUrl(existingUrl)
                .isAvailable(true)
                .build();
        when(items.findById(1L)).thenReturn(Optional.of(item));
        when(items.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.updateItemAvailability(1L, false, 2L);
        assertEquals(false, response.getIsAvailable());
        verify(cloud, never()).validateImageUrl(any());
        verify(items).save(argThat(saved -> !saved.getIsAvailable() && existingUrl.equals(saved.getImageUrl())));
    }
}
