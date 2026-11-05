package com.trung.bookingservice.controller;

import com.trung.bookingservice.dto.request.BookingRequest;
import com.trung.bookingservice.dto.response.ApiResponse;
import com.trung.bookingservice.dto.response.BookingDriverResponse;
import com.trung.bookingservice.dto.response.BookingResponse;
import com.trung.bookingservice.entity.Booking;
import com.trung.bookingservice.exception.BadRequestException;
import com.trung.bookingservice.exception.ResourceNotFoundException;
import com.trung.bookingservice.security.UserPrincipal;
import com.trung.bookingservice.service.BookingDriverService;
import com.trung.bookingservice.service.FoodDeliveryDispatchService;
import com.trung.bookingservice.service.OrderCreationGuard;
import com.trung.bookingservice.service.RidePrepaymentWorker;
import com.trung.bookingservice.service.impl.BookingServiceImpl;
import com.trung.bookingservice.util.enums.BookingStatus;
import com.trung.bookingservice.util.enums.ReportPeriod;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final OrderCreationGuard creationGuard;
    private final BookingServiceImpl bookingService;
    private final RidePrepaymentWorker prepayments;
    private final FoodDeliveryDispatchService foodDeliveryDispatchService;
    private final BookingDriverService bookingDriverService;

    private Long resolveCustomerId(Long customerId) {
        if (customerId != null) {
            return customerId;
        }
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            if (auth.getPrincipal() instanceof UserPrincipal p && p.getUserId() != null) {
                return p.getUserId();
            }
            if (auth.getPrincipal() != null) {
                try {
                    return Long.parseLong(auth.getPrincipal().toString());
                } catch (NumberFormatException ignored) {}
            }
        }
        return null;
    }

    @GetMapping("/{bookingId}/driver")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<BookingDriverResponse>> getBookingDriver(
            @RequestHeader(name = "X-User-Id", required = false) Long customerId,
            @PathVariable Long bookingId) {
        Long resolvedId = resolveCustomerId(customerId);
        if (resolvedId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu định danh người dùng (X-User-Id)");
        }
        return ResponseEntity.ok(ApiResponse.<BookingDriverResponse>builder()
                .success(true)
                .message("Lấy thông tin tài xế thành công")
                .data(bookingDriverService.getDriver(bookingId, resolvedId))
                .timestamp(LocalDateTime.now())
                .build());
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<BookingResponse> createBooking(
            @RequestHeader("Idempotency-Key") String key,
            @RequestHeader(name = "X-User-Id", required = false) Long customerId,
            @Valid @RequestBody BookingRequest request) throws BadRequestException {

        Long resolvedId = resolveCustomerId(customerId);
        if (resolvedId == null) {
            throw new BadRequestException("Thiếu định danh người dùng (X-User-Id)");
        }

        BookingResponse created = creationGuard.create(resolvedId, key, request, BookingResponse.class,
                BookingResponse::getBookingId, () -> bookingService.createBooking(resolvedId, request));
        prepayments.pay(created.getBookingId());
        return ResponseEntity.ok(bookingService.currentResponse(created.getBookingId()));
    }

    @PutMapping("/{bookingId}/accept")
    public ResponseEntity<BookingResponse> acceptBooking(
            @RequestHeader("X-User-Id") Long driverId,
            @PathVariable Long bookingId) throws BadRequestException, ResourceNotFoundException {

        BookingResponse response = bookingService.acceptBooking(driverId, bookingId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{bookingId}/arrived")
    public ResponseEntity<BookingResponse> arriveAtPickup(
            @RequestHeader("X-User-Id") Long driverId,
            @PathVariable Long bookingId) throws BadRequestException, ResourceNotFoundException {
        return ResponseEntity.ok(bookingService.updateBookingStatus(driverId, bookingId, BookingStatus.ARRIVED));
    }

    @PutMapping("/{bookingId}/start")
    public ResponseEntity<BookingResponse> startTrip(
            @RequestHeader("X-User-Id") Long driverId,
            @PathVariable Long bookingId) throws BadRequestException, ResourceNotFoundException {
        return ResponseEntity.ok(bookingService.updateBookingStatus(driverId, bookingId, BookingStatus.IN_PROGRESS));
    }

    @PutMapping("/{bookingId}/complete")
    public ResponseEntity<BookingResponse> completeTrip(
            @RequestHeader("X-User-Id") Long driverId,
            @PathVariable Long bookingId,
            @RequestParam(defaultValue = "CASH") String paymentMethod) throws BadRequestException, ResourceNotFoundException {
        return ResponseEntity.ok(bookingService.completeTrip(driverId, bookingId, paymentMethod));
    }

    @RequestMapping(value = "/{bookingId}/cancel", method = {RequestMethod.PUT, RequestMethod.POST})
    public ResponseEntity<BookingResponse> cancelBooking(
            @RequestHeader(name = "X-User-Id", required = false) Long customerId,
            @PathVariable Long bookingId) throws ResourceNotFoundException, BadRequestException {
        Long resolvedId = resolveCustomerId(customerId);
        if (resolvedId == null) {
            throw new BadRequestException("Thiếu định danh người dùng (X-User-Id)");
        }
        return ResponseEntity.ok(bookingService.cancelBooking(resolvedId, bookingId));
    }

    @PutMapping("/{bookingId}/driver-cancel")
    public ResponseEntity<BookingResponse> driverCancelBooking(
            @RequestHeader("X-User-Id") Long driverId,
            @PathVariable Long bookingId) throws ResourceNotFoundException, BadRequestException {

        return ResponseEntity.ok(bookingService.cancelBookingByDriver(driverId, bookingId));
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingById(
            @PathVariable Long bookingId,
            @RequestHeader(name = "X-User-Id", required = false) Long currentUserId,
            @RequestHeader(name = "X-User-Role", required = false) String currentUserRole)
            throws ResourceNotFoundException, BadRequestException {
        if (currentUserId == null) {
            throw new BadRequestException("Thiếu định danh người dùng (X-User-Id)");
        }
        return ResponseEntity.ok(bookingService.getBookingDetails(bookingId, currentUserId, currentUserRole));
    }

    @GetMapping(value = {"/driver", "/driver/history"})
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getDriverBookings(
            @RequestHeader("X-User-Id") Long driverId,
            @RequestParam(required = false) BookingStatus status) {
        return ResponseEntity.ok(bookingService.getDriverBookings(driverId, status));
    }

    @GetMapping("/customer")
    public ResponseEntity<ApiResponse<List<Booking>>> getCustomerBookings(
            @RequestHeader(name = "X-User-Id", required = false) Long customerId) {
        Long resolvedId = resolveCustomerId(customerId);
        if (resolvedId == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(bookingService.getCustomerBookings(resolvedId));
    }

    @GetMapping("/driver/daily-report")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDailyReport(
            @RequestHeader("X-User-Id") Long driverId,
            @RequestParam(defaultValue = "DAY") ReportPeriod period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(bookingService.getDriverReport(driverId, period, date));
    }

    @GetMapping("/admin/all")
    public ResponseEntity<ApiResponse<List<Booking>>> getAllBookingsAdmin() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @PostMapping("/food-orders/{orderId}/accept")
    public ResponseEntity<ApiResponse<String>> acceptFoodOrder(
            @RequestHeader("X-User-Id") Long driverId,
            @PathVariable Long orderId) throws ResourceNotFoundException, BadRequestException {
        foodDeliveryDispatchService.acceptFoodOrder(orderId, driverId);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Tài xế đã nhận đơn giao đồ ăn thành công")
                .data("OK")
                .build());
    }

    @PostMapping("/food-orders/{orderId}/reject")
    public ResponseEntity<ApiResponse<String>> rejectFoodOrder(
            @RequestHeader("X-User-Id") Long driverId,
            @PathVariable Long orderId) {
        foodDeliveryDispatchService.rejectFoodOrder(orderId, driverId);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Tài xế đã bỏ qua đơn giao đồ ăn")
                .data("OK")
                .build());
    }

    @GetMapping("/admin/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAdminBookingStats() {
        return ResponseEntity.ok(bookingService.getAdminStats());
    }
}
