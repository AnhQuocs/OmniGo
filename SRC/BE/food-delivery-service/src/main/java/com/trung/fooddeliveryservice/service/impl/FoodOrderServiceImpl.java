package com.trung.fooddeliveryservice.service.impl;

import com.trung.fooddeliveryservice.dto.request.FoodOrderCreateRequest;
import com.trung.fooddeliveryservice.dto.request.FoodOrderItemRequest;
import com.trung.fooddeliveryservice.dto.response.FoodOrderResponse;
import com.trung.fooddeliveryservice.entity.FoodOrder;
import com.trung.fooddeliveryservice.entity.FoodOrderItem;
import com.trung.fooddeliveryservice.entity.MenuItem;
import com.trung.fooddeliveryservice.entity.Restaurant;
import com.trung.fooddeliveryservice.event.FindDriverForFoodOrderEvent;
import com.trung.fooddeliveryservice.exception.BadRequestException;
import com.trung.fooddeliveryservice.exception.ResourceNotFoundException;
import com.trung.fooddeliveryservice.exception.UnauthorizedException;
import com.trung.fooddeliveryservice.mapper.FoodOrderMapper;
import com.trung.fooddeliveryservice.publisher.FoodEventPublisher;
import com.trung.fooddeliveryservice.repository.FoodOrderItemRepository;
import com.trung.fooddeliveryservice.repository.FoodOrderRepository;
import com.trung.fooddeliveryservice.repository.MenuItemRepository;
import com.trung.fooddeliveryservice.repository.RestaurantRepository;
import com.trung.fooddeliveryservice.service.FoodOrderService;
import com.trung.fooddeliveryservice.util.enums.OrderStatus;
import com.trung.fooddeliveryservice.util.enums.OrderCancelledBy;
import com.trung.fooddeliveryservice.util.enums.OrderCancelReason;
import com.trung.fooddeliveryservice.util.enums.RestaurantStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FoodOrderServiceImpl implements FoodOrderService {

    private final FoodOrderRepository foodOrderRepository;
    private final FoodOrderItemRepository foodOrderItemRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final FoodOrderMapper foodOrderMapper;
    private final FoodEventPublisher foodEventPublisher;
    private final StringRedisTemplate redisTemplate;
    private final RestTemplate directRestTemplate;

    @org.springframework.beans.factory.annotation.Value("${app.payment-service-url:http://localhost:8085}")
    private String paymentServiceBaseUrl;

    @org.springframework.beans.factory.annotation.Value("${app.driver-service-url:http://localhost:8081}")
    private String driverServiceBaseUrl;

    @Override
    @Transactional
    public FoodOrderResponse createOrder(FoodOrderCreateRequest request, Long customerId) throws ResourceNotFoundException, BadRequestException {
        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> {
                    log.warn("Không tìm thấy nhà hàng ID {}", request.getRestaurantId());
                    return new ResourceNotFoundException("Không tìm thấy nhà hàng với ID: " + request.getRestaurantId());
                });

        if (Boolean.TRUE.equals(restaurant.getIsLocked())) {
            log.warn("Nhà hàng '{}' (ID: {}) đã bị khóa bởi quản trị viên", restaurant.getName(), restaurant.getId());
            String reason = org.springframework.util.StringUtils.hasText(restaurant.getLockedReason()) ? restaurant.getLockedReason() : "Vi phạm quy định của hệ thống";
            throw new BadRequestException("Nhà hàng này hiện đang bị khóa (" + reason + "), quý khách không thể đặt món từ quán này.");
        }

        if (restaurant.getStatus() != RestaurantStatus.OPEN) {
            log.warn("Nhà hàng '{}' (ID: {}) đang ở trạng thái {} nên không nhận đơn", restaurant.getName(), restaurant.getId(), restaurant.getStatus());
            throw new BadRequestException("Nhà hàng hiện đang đóng cửa hoặc bận, không thể nhận đơn đặt");
        }

        List<Long> itemIds = request.getItems().stream()
                .map(FoodOrderItemRequest::getMenuItemId)
                .collect(Collectors.toList());

        List<MenuItem> menuItems = menuItemRepository.findAllById(itemIds);
        Map<Long, MenuItem> menuItemMap = menuItems.stream()
                .collect(Collectors.toMap(MenuItem::getId, item -> item));

        BigDecimal itemsTotal = BigDecimal.ZERO;
        List<FoodOrderItem> orderItems = new ArrayList<>();

        OrderStatus initialStatus = OrderStatus.PENDING;
        if (request.getPaymentMethod() != null && (
                "VNPAY".equalsIgnoreCase(request.getPaymentMethod().trim()) ||
                "MOMO".equalsIgnoreCase(request.getPaymentMethod().trim()) ||
                "WALLET".equalsIgnoreCase(request.getPaymentMethod().trim()))) {
            initialStatus = OrderStatus.AWAITING_PAYMENT;
        }

        FoodOrder order = FoodOrder.builder()
                .customerId(customerId)
                .customerName(request.getCustomerName())
                .customerPhone(request.getCustomerPhone())
                .restaurant(restaurant)
                .status(initialStatus)
                .deliveryAddress(request.getDeliveryAddress())
                .deliveryLatitude(request.getDeliveryLatitude())
                .deliveryLongitude(request.getDeliveryLongitude())
                .note(request.getNote())
                .paymentMethod(request.getPaymentMethod())
                .isPaid(false)
                .build();

        for (FoodOrderItemRequest itemReq : request.getItems()) {
            MenuItem menuItem = menuItemMap.get(itemReq.getMenuItemId());
            if (menuItem == null) {
                log.warn("Món ăn ID {} không tồn tại trong hệ thống", itemReq.getMenuItemId());
                throw new BadRequestException("Món ăn với ID " + itemReq.getMenuItemId() + " không tồn tại");
            }
            if (!menuItem.getRestaurant().getId().equals(restaurant.getId())) {
                log.warn("Món ăn '{}' (ID: {}) không thuộc nhà hàng '{}'", menuItem.getName(), menuItem.getId(), restaurant.getName());
                throw new BadRequestException("Món ăn '" + menuItem.getName() + "' không thuộc về nhà hàng " + restaurant.getName());
            }
            if (!Boolean.TRUE.equals(menuItem.getIsAvailable())) {
                log.warn("Món ăn '{}' (ID: {}) hiện đã hết hàng", menuItem.getName(), menuItem.getId());
                throw new BadRequestException("Món ăn '" + menuItem.getName() + "' hiện tại đã hết hàng");
            }

            BigDecimal subtotal = menuItem.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            itemsTotal = itemsTotal.add(subtotal);

            FoodOrderItem orderItem = FoodOrderItem.builder()
                    .foodOrder(order)
                    .menuItemId(menuItem.getId())
                    .itemName(menuItem.getName())
                    .price(menuItem.getPrice())
                    .quantity(itemReq.getQuantity())
                    .subtotal(subtotal)
                    .build();

            orderItems.add(orderItem);
        }

        BigDecimal deliveryFee = calculateDeliveryFee(
                restaurant.getLatitude(), restaurant.getLongitude(),
                request.getDeliveryLatitude(), request.getDeliveryLongitude()
        );

        order.setDeliveryFee(deliveryFee);
        order.setTotalPrice(itemsTotal.add(deliveryFee));
        order.setOrderItems(orderItems);

        FoodOrder savedOrder = foodOrderRepository.save(order);
        log.info("Tạo thành công đơn hàng ID {} cho khách hàng ID {}, tổng tiền: {} đ (chờ nhà hàng duyệt)", savedOrder.getId(), customerId, savedOrder.getTotalPrice());

        return foodOrderMapper.toResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public FoodOrderResponse getOrderById(Long orderId) throws ResourceNotFoundException {
        FoodOrder order = foodOrderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.warn("Không tìm thấy đơn hàng ID {}", orderId);
                    return new ResourceNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId);
                });
        return foodOrderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FoodOrderResponse> getCustomerOrders(Long customerId) {
        List<FoodOrder> orders = foodOrderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
        return foodOrderMapper.toResponseList(orders);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FoodOrderResponse> getRestaurantOrders(Long restaurantId, Long ownerId) throws ResourceNotFoundException, UnauthorizedException {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> {
                    log.warn("Không tìm thấy nhà hàng ID {}", restaurantId);
                    return new ResourceNotFoundException("Không tìm thấy nhà hàng với ID: " + restaurantId);
                });

        if (!restaurant.getOwnerId().equals(ownerId)) {
            log.warn("Chủ quán ID {} không có quyền xem đơn của nhà hàng ID {}", ownerId, restaurantId);
            throw new UnauthorizedException("Bạn không có quyền xem đơn hàng của nhà hàng này");
        }

        List<FoodOrder> orders = foodOrderRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId)
                .stream()
                .filter(o -> o.getStatus() != OrderStatus.AWAITING_PAYMENT)
                .collect(Collectors.toList());
        return foodOrderMapper.toResponseList(orders);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FoodOrderResponse> getDriverOrders(Long driverId) {
        List<FoodOrder> orders = foodOrderRepository.findByDriverIdOrderByCreatedAtDesc(driverId);
        return foodOrderMapper.toResponseList(orders);
    }

    @Override
    @Transactional
    public FoodOrderResponse updateOrderStatusByRestaurant(Long orderId, OrderStatus newStatus, Long ownerId) throws ResourceNotFoundException, UnauthorizedException, BadRequestException {
        return updateOrderStatusByRestaurant(orderId, newStatus, ownerId, null, null);
    }

    @Override
    @Transactional
    public FoodOrderResponse updateOrderStatusByRestaurant(Long orderId, OrderStatus newStatus, Long ownerId, String reason, OrderCancelReason reasonCode) throws ResourceNotFoundException, UnauthorizedException, BadRequestException {
        String lockKey = "lock:order:" + orderId;
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "locked", Duration.ofSeconds(5));
        if (Boolean.FALSE.equals(acquired)) {
            log.warn("Đơn hàng ID {} đang được thao tác bởi tiến trình khác (Lock)", orderId);
            throw new BadRequestException("Đơn hàng đang được xử lý, vui lòng thử lại sau giây lát");
        }

        try {
            FoodOrder order = foodOrderRepository.findById(orderId)
                    .orElseThrow(() -> {
                        log.warn("Không tìm thấy đơn hàng ID {} để cập nhật", orderId);
                        return new ResourceNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId);
                    });

            if (!order.getRestaurant().getOwnerId().equals(ownerId)) {
                log.warn("Chủ quán ID {} không sở hữu nhà hàng cho đơn hàng ID {}", ownerId, orderId);
                throw new UnauthorizedException("Bạn không sở hữu nhà hàng của đơn hàng này");
            }

            OrderStatus current = order.getStatus();

            if (newStatus == OrderStatus.ACCEPTED) {
                if (current != OrderStatus.PENDING) {
                    throw new BadRequestException("Chỉ có thể chấp nhận đơn khi trạng thái là Chờ xác nhận (PENDING). Hiện tại: " + current);
                }
            } else if (newStatus == OrderStatus.PREPARING) {
                if (current != OrderStatus.ACCEPTED) {
                    throw new BadRequestException("Chỉ có thể chuẩn bị món khi trạng thái là Đã nhận đơn (ACCEPTED). Hiện tại: " + current);
                }
            } else if (newStatus == OrderStatus.READY_FOR_PICKUP) {
                if (current != OrderStatus.ACCEPTED && current != OrderStatus.PREPARING && current != OrderStatus.READY_FOR_PICKUP && current != OrderStatus.NO_DRIVER_FOUND) {
                    throw new BadRequestException("Chỉ có thể báo sẵn sàng lấy hàng khi đang Chuẩn bị (PREPARING), Đã nhận đơn (ACCEPTED) hoặc Tìm lại tài xế. Hiện tại: " + current);
                }
                if (current == OrderStatus.NO_DRIVER_FOUND) {
                    int nextRetry = (order.getDriverRetryCount() != null ? order.getDriverRetryCount() : 0) + 1;
                    order.setDriverRetryCount(nextRetry);
                    log.info("Chủ quán ID {} quét tìm lại tài xế cho đơn #{} (lần thử thứ {})", ownerId, orderId, nextRetry);
                }
            } else if (newStatus == OrderStatus.REJECTED) {
                if (current != OrderStatus.PENDING) {
                    throw new BadRequestException("Chỉ có thể từ chối đơn khi đang ở trạng thái Chờ xác nhận (PENDING). Hiện tại: " + current);
                }
                order.setCancelledBy(OrderCancelledBy.RESTAURANT);
                order.setCancelReason(reason != null && !reason.trim().isEmpty() ? reason.trim() : (reasonCode != null ? reasonCode.getDefaultDescription() : "Quán từ chối tiếp nhận đơn"));
                order.setCancelReasonCode(reasonCode);
            } else if (newStatus == OrderStatus.CANCELLED) {
                if (current == OrderStatus.COMPLETED || current == OrderStatus.REJECTED || current == OrderStatus.CANCELLED) {
                    throw new BadRequestException("Không thể hủy đơn hàng đã kết thúc (" + current + ")");
                }
                order.setCancelledBy(OrderCancelledBy.RESTAURANT);
                order.setCancelReason(reason != null && !reason.trim().isEmpty() ? reason.trim() : (reasonCode != null ? reasonCode.getDefaultDescription() : "Quán hủy đơn"));
                order.setCancelReasonCode(reasonCode);
            } else {
                throw new BadRequestException("Trạng thái chuyển đổi không hợp lệ cho phía nhà hàng: " + newStatus);
            }

            order.setStatus(newStatus);
            FoodOrder saved = foodOrderRepository.save(order);
            log.info("Chủ nhà hàng ID {} đã cập nhật đơn hàng ID {} sang trạng thái {}", ownerId, orderId, newStatus);

            if (newStatus == OrderStatus.READY_FOR_PICKUP) {
                FindDriverForFoodOrderEvent event = FindDriverForFoodOrderEvent.builder()
                        .orderId(saved.getId())
                        .customerId(saved.getCustomerId())
                        .restaurantId(order.getRestaurant().getId())
                        .restaurantName(order.getRestaurant().getName())
                        .restaurantAddress(order.getRestaurant().getAddress())
                        .restaurantLatitude(order.getRestaurant().getLatitude())
                        .restaurantLongitude(order.getRestaurant().getLongitude())
                        .dropOffAddress(saved.getDeliveryAddress())
                        .dropOffLatitude(saved.getDeliveryLatitude())
                        .dropOffLongitude(saved.getDeliveryLongitude())
                        .totalPrice(saved.getTotalPrice())
                        .deliveryFee(saved.getDeliveryFee())
                        .paymentMethod(saved.getPaymentMethod())
                        .isPaid(saved.getIsPaid())
                        .build();
                foodEventPublisher.publishFindDriverDirect(event);
                log.info("Đã phát trực tiếp sự kiện tìm tài xế giao đồ ăn lên Kafka cho đơn hàng ID {}", saved.getId());

                // Timeout 60s: Kiểm tra tài xế nhận đơn
                final Long targetOrderId = saved.getId();
                CompletableFuture.runAsync(() -> {
                    try {
                        Thread.sleep(60000);
                        foodOrderRepository.findById(targetOrderId).ifPresent(o -> {
                            if (o.getStatus() == OrderStatus.READY_FOR_PICKUP) {
                                int retryCount = o.getDriverRetryCount() != null ? o.getDriverRetryCount() : 0;
                                if (retryCount < 1) {
                                    log.warn("Đơn hàng ID {} không có tài xế nhận sau lần quét đầu, chuyển sang NO_DRIVER_FOUND", targetOrderId);
                                    o.setStatus(OrderStatus.NO_DRIVER_FOUND);
                                    foodOrderRepository.save(o);
                                } else {
                                    log.warn("Đơn hàng ID {} không có tài xế nhận sau khi quét lại lần 2, tự động hủy CANCELLED", targetOrderId);
                                    o.setStatus(OrderStatus.CANCELLED);
                                    o.setCancelledBy(OrderCancelledBy.SYSTEM);
                                    o.setCancelReason("Không tìm thấy tài xế sau nhiều lần tìm kiếm");
                                    o.setCancelReasonCode(OrderCancelReason.SYSTEM_NO_DRIVER_FOUND);
                                    FoodOrder savedCancelled = foodOrderRepository.save(o);
                                    if (Boolean.TRUE.equals(savedCancelled.getIsPaid())) {
                                        triggerRefund(savedCancelled.getCustomerId(), savedCancelled.getId(), savedCancelled.getTotalPrice(), "Hệ thống hủy đơn do không tìm thấy tài xế");
                                    }
                                }
                            }
                        });
                    } catch (Exception e) {
                        log.error("Lỗi trong quá trình kiểm tra timeout tìm xế của đơn #{}: {}", targetOrderId, e.getMessage());
                    }
                });
            } else if (newStatus == OrderStatus.CANCELLED) {
                if (Boolean.TRUE.equals(saved.getIsPaid())) {
                    triggerRefund(saved.getCustomerId(), saved.getId(), saved.getTotalPrice(), "Nhà hàng hủy đơn #" + saved.getId() + (reason != null ? ": " + reason : ""));
                }
                if (saved.getDriverId() != null) {
                    updateDriverStatusOnline(saved.getDriverId());
                }
            } else if (newStatus == OrderStatus.REJECTED) {
                if (Boolean.TRUE.equals(saved.getIsPaid())) {
                    triggerRefund(saved.getCustomerId(), saved.getId(), saved.getTotalPrice(), "Nhà hàng từ chối tiếp nhận đơn #" + saved.getId() + (reason != null ? ": " + reason : ""));
                }
            }

            return foodOrderMapper.toResponse(saved);
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    @Override
    @Transactional
    public FoodOrderResponse cancelOrderByCustomer(Long orderId, Long customerId) throws ResourceNotFoundException, UnauthorizedException, BadRequestException {
        return cancelOrderByCustomer(orderId, customerId, null, null);
    }

    @Override
    @Transactional
    public FoodOrderResponse cancelOrderByCustomer(Long orderId, Long customerId, String reason, OrderCancelReason reasonCode) throws ResourceNotFoundException, UnauthorizedException, BadRequestException {
        String lockKey = "lock:order:" + orderId;
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "locked", Duration.ofSeconds(5));
        if (Boolean.FALSE.equals(acquired)) {
            log.warn("Đơn hàng ID {} đang được thao tác bởi tiến trình khác (Lock)", orderId);
            throw new BadRequestException("Đơn hàng đang được xử lý, vui lòng thử lại sau giây lát");
        }

        try {
            FoodOrder order = foodOrderRepository.findById(orderId)
                    .orElseThrow(() -> {
                        log.warn("Không tìm thấy đơn hàng ID {} để hủy", orderId);
                        return new ResourceNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId);
                    });

            if (!order.getCustomerId().equals(customerId)) {
                log.warn("Khách hàng ID {} không phải là người đặt đơn hàng ID {}", customerId, orderId);
                throw new UnauthorizedException("Bạn không phải là người tạo đơn hàng này");
            }

            if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.AWAITING_PAYMENT) {
                log.warn("Không thể hủy đơn hàng ID {} vì trạng thái hiện tại là {}", orderId, order.getStatus());
                throw new BadRequestException("Không thể hủy đơn vì nhà hàng đã tiếp nhận hoặc đang xử lý. Chỉ có thể hủy đơn khi ở trạng thái Chờ thanh toán hoặc Chờ quán xác nhận.");
            }

            order.setStatus(OrderStatus.CANCELLED);
            order.setCancelledBy(OrderCancelledBy.CUSTOMER);
            order.setCancelReason(reason != null && !reason.trim().isEmpty() ? reason.trim() : (reasonCode != null ? reasonCode.getDefaultDescription() : "Khách hàng tự hủy đơn"));
            order.setCancelReasonCode(reasonCode);
            FoodOrder saved = foodOrderRepository.save(order);
            log.info("Khách hàng ID {} đã hủy thành công đơn hàng ID {}", customerId, orderId);

            if (Boolean.TRUE.equals(saved.getIsPaid())) {
                triggerRefund(saved.getCustomerId(), saved.getId(), saved.getTotalPrice(), "Khách hàng tự hủy đơn #" + saved.getId() + (reason != null ? ": " + reason : ""));
            }

            return foodOrderMapper.toResponse(saved);
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    @Override
    @Transactional
    public FoodOrderResponse assignDriver(Long orderId, Long driverId) throws ResourceNotFoundException {
        FoodOrder order = foodOrderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.warn("Không tìm thấy đơn hàng ID {} để gán tài xế", orderId);
                    return new ResourceNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId);
                });

        order.setDriverId(driverId);
        if (order.getStatus() == OrderStatus.READY_FOR_PICKUP) {
            order.setStatus(OrderStatus.DELIVERING);
        }

        FoodOrder saved = foodOrderRepository.save(order);
        log.info("Đã gán thành công tài xế ID {} cho đơn hàng ID {}", driverId, orderId);
        return foodOrderMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public FoodOrderResponse updateOrderStatusByDriver(Long orderId, OrderStatus newStatus, Long driverId) throws ResourceNotFoundException, UnauthorizedException, BadRequestException {
        return updateOrderStatusByDriver(orderId, newStatus, driverId, null, null);
    }

    @Override
    @Transactional
    public FoodOrderResponse updateOrderStatusByDriver(Long orderId, OrderStatus newStatus, Long driverId, String reason, OrderCancelReason reasonCode) throws ResourceNotFoundException, UnauthorizedException, BadRequestException {
        FoodOrder order = foodOrderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.warn("Không tìm thấy đơn hàng ID {}", orderId);
                    return new ResourceNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId);
                });

        if (order.getDriverId() == null || !order.getDriverId().equals(driverId)) {
            log.warn("Tài xế ID {} không phải tài xế được chỉ định cho đơn hàng ID {}", driverId, orderId);
            throw new UnauthorizedException("Bạn không phải là tài xế được gán cho đơn hàng này");
        }

        if (newStatus == OrderStatus.DELIVERING) {
            order.setStatus(OrderStatus.DELIVERING);
        } else if (newStatus == OrderStatus.COMPLETED) {
            order.setStatus(OrderStatus.COMPLETED);
            order.setIsPaid(true);
        } else if (newStatus == OrderStatus.CANCELLED) {
            order.setStatus(OrderStatus.CANCELLED);
            order.setCancelledBy(OrderCancelledBy.DRIVER);
            order.setCancelReason(reason != null && !reason.trim().isEmpty() ? reason.trim() : (reasonCode != null ? reasonCode.getDefaultDescription() : "Tài xế hủy đơn"));
            order.setCancelReasonCode(reasonCode);
        } else {
            throw new BadRequestException("Trạng thái chuyển đổi không hợp lệ cho phía tài xế: " + newStatus);
        }

        FoodOrder saved = foodOrderRepository.save(order);
        log.info("Tài xế ID {} đã cập nhật đơn hàng ID {} sang trạng thái {}", driverId, orderId, newStatus);

        if (newStatus == OrderStatus.COMPLETED) {
            triggerFoodDeliveryPayout(driverId, saved.getId(), saved.getDeliveryFee(), saved.getPaymentMethod());
            updateDriverStatusOnline(driverId);
        } else if (newStatus == OrderStatus.CANCELLED) {
            if (Boolean.TRUE.equals(saved.getIsPaid())) {
                triggerRefund(saved.getCustomerId(), saved.getId(), saved.getTotalPrice(), "Tài xế hủy đơn #" + saved.getId() + (reason != null ? ": " + reason : ""));
            }
            updateDriverStatusOnline(driverId);
        }

        return foodOrderMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public FoodOrderResponse markOrderAsPaid(Long orderId) throws ResourceNotFoundException {
        FoodOrder order = foodOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng ID: " + orderId));
        order.setIsPaid(true);
        if (order.getStatus() == OrderStatus.AWAITING_PAYMENT) {
            order.setStatus(OrderStatus.PENDING);
            log.info("Đơn hàng #{} đã thanh toán thành công, tự động chuyển từ AWAITING_PAYMENT sang PENDING để quán nhận đơn", orderId);
        }
        FoodOrder saved = foodOrderRepository.save(order);
        log.info("Đã đánh dấu đơn hàng #{} là ĐÃ THANH TOÁN (isPaid = true)", orderId);
        return foodOrderMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public FoodOrderResponse switchToCashPayment(Long orderId, Long customerId) throws ResourceNotFoundException, UnauthorizedException, BadRequestException {
        FoodOrder order = foodOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng ID: " + orderId));

        if (!order.getCustomerId().equals(customerId)) {
            log.warn("Khách hàng ID {} không có quyền đổi phương thức cho đơn hàng ID {}", customerId, orderId);
            throw new UnauthorizedException("Bạn không có quyền thay đổi hình thức thanh toán cho đơn hàng này");
        }

        if (order.getStatus() != OrderStatus.AWAITING_PAYMENT && order.getStatus() != OrderStatus.PENDING) {
            log.warn("Không thể chuyển sang tiền mặt vì đơn #{} đang ở trạng thái {}", orderId, order.getStatus());
            throw new BadRequestException("Chỉ có thể chuyển sang Tiền mặt khi đơn hàng đang Chờ thanh toán hoặc Chờ quán duyệt");
        }

        order.setPaymentMethod("CASH");
        order.setIsPaid(false);
        order.setStatus(OrderStatus.PENDING);
        FoodOrder saved = foodOrderRepository.save(order);
        log.info("Khách hàng ID {} đã chuyển đơn hàng #{} sang thanh toán TIỀN MẶT (CASH), chuyển trạng thái sang PENDING để quán nhận đơn", customerId, orderId);
        return foodOrderMapper.toResponse(saved);
    }

    private void triggerFoodDeliveryPayout(Long driverId, Long orderId, BigDecimal deliveryFee, String paymentMethod) {
        try {
            Map<String, Object> payoutReq = Map.of(
                    "driverId", driverId,
                    "orderId", orderId,
                    "deliveryFee", deliveryFee != null ? deliveryFee : BigDecimal.valueOf(15000),
                    "paymentMethod", paymentMethod != null ? paymentMethod : "CASH"
            );

            List<String> targetUrls = new ArrayList<>();
            if (paymentServiceBaseUrl != null && !paymentServiceBaseUrl.isBlank()) {
                targetUrls.add(paymentServiceBaseUrl.replaceAll("/+$", "") + "/api/v1/payments/internal/food-order-payout");
            }
            targetUrls.add("http://localhost:8085/api/v1/payments/internal/food-order-payout");
            targetUrls.add("http://localhost:8080/api/v1/payments/internal/food-order-payout");

            boolean success = false;
            for (String url : targetUrls) {
                try {
                    directRestTemplate.postForEntity(url, payoutReq, Void.class);
                    log.info("Đã gửi yêu cầu quyết toán cước ship đơn đồ ăn #{} cho tài xế ID {} thành công qua URL: {}", orderId, driverId, url);
                    success = true;
                    break;
                } catch (Exception err) {
                    log.warn("Không thể gọi payment-service qua URL {}: {}", url, err.getMessage());
                }
            }
            if (!success) {
                log.error("Không thể quyết toán cước ship cho tài xế ID {} (đơn #{}) qua bất kỳ URL nào", driverId, orderId);
            }
        } catch (Exception e) {
            log.error("Lỗi khi gọi thanh toán cước phí ship cho tài xế ID {} (đơn #{}): {}", driverId, orderId, e.getMessage());
        }
    }

    private void triggerRefund(Long customerId, Long orderId, BigDecimal amount, String reason) {
        try {
            Map<String, Object> refundReq = Map.of(
                    "userId", customerId,
                    "userType", "CUSTOMER",
                    "orderId", orderId,
                    "amount", amount != null ? amount : BigDecimal.ZERO,
                    "reason", reason != null ? reason : "Hoàn tiền đơn hàng"
            );

            List<String> targetUrls = new ArrayList<>();
            if (paymentServiceBaseUrl != null && !paymentServiceBaseUrl.isBlank()) {
                targetUrls.add(paymentServiceBaseUrl.replaceAll("/+$", "") + "/api/v1/payments/internal/refund-order");
            }
            targetUrls.add("http://localhost:8085/api/v1/payments/internal/refund-order");
            targetUrls.add("http://localhost:8080/api/v1/payments/internal/refund-order");

            for (String url : targetUrls) {
                try {
                    directRestTemplate.postForEntity(url, refundReq, Void.class);
                    log.info("Đã gửi yêu cầu hoàn tiền {} VND cho khách hàng ID {} (đơn #{}) thành công qua URL: {}", amount, customerId, orderId, url);
                    break;
                } catch (Exception err) {
                    log.warn("Không thể gọi hoàn tiền qua URL {}: {}", url, err.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Lỗi khi gọi hoàn tiền cho khách hàng ID {} (đơn #{}): {}", customerId, orderId, e.getMessage());
        }
    }

    private void updateDriverStatusOnline(Long driverId) {
        try {
            String url = (driverServiceBaseUrl != null ? driverServiceBaseUrl : "http://localhost:8081") + "/api/v1/drivers/" + driverId + "/status?isActive=true";
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("X-User-Id", String.valueOf(driverId));
            org.springframework.http.HttpEntity<Void> entity = new org.springframework.http.HttpEntity<>(headers);
            directRestTemplate.exchange(url, org.springframework.http.HttpMethod.PUT, entity, Void.class);
            log.info("Đã đồng bộ trạng thái ONLINE cho tài xế ID {} sau khi giao đồ ăn xong", driverId);
        } catch (Exception e) {
            log.warn("Không thể đồng bộ trạng thái tài xế ID {} sang ONLINE: {}", driverId, e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<FoodOrderResponse> getAllOrders() {
        List<FoodOrder> list = foodOrderRepository.findAllByOrderByCreatedAtDesc();
        return foodOrderMapper.toResponseList(list);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.Map<String, Object> getAdminFoodStats() {
        List<FoodOrder> all = foodOrderRepository.findAll();
        long totalOrders = all.size();
        long completedOrders = all.stream().filter(o -> o.getStatus() == OrderStatus.COMPLETED).count();
        long deliveringOrders = all.stream().filter(o -> o.getStatus() == OrderStatus.DELIVERING).count();
        long preparingOrders = all.stream().filter(o -> o.getStatus() == OrderStatus.PREPARING || o.getStatus() == OrderStatus.READY_FOR_PICKUP).count();
        long pendingOrders = all.stream().filter(o -> o.getStatus() == OrderStatus.PENDING || o.getStatus() == OrderStatus.ACCEPTED).count();
        long cancelledOrders = all.stream().filter(o -> o.getStatus() == OrderStatus.CANCELLED || o.getStatus() == OrderStatus.REJECTED || o.getStatus() == OrderStatus.NO_DRIVER_FOUND).count();

        BigDecimal totalRevenue = all.stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED && o.getTotalPrice() != null)
                .map(FoodOrder::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDeliveryFees = all.stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED && o.getDeliveryFee() != null)
                .map(FoodOrder::getDeliveryFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalOrders", totalOrders);
        stats.put("completedOrders", completedOrders);
        stats.put("deliveringOrders", deliveringOrders);
        stats.put("preparingOrders", preparingOrders);
        stats.put("pendingOrders", pendingOrders);
        stats.put("cancelledOrders", cancelledOrders);
        stats.put("totalRevenue", totalRevenue);
        stats.put("totalDeliveryFees", totalDeliveryFees);

        return stats;
    }

    private BigDecimal calculateDeliveryFee(Double lat1, Double lon1, Double lat2, Double lon2) {
        double distanceKm = calculateDistanceInKm(lat1, lon1, lat2, lon2);
        // Phí cơ sở: 15.000đ cho 2km đầu tiên, 5.000đ cho mỗi km tiếp theo
        double fee = 15000.0;
        if (distanceKm > 2.0) {
            fee += (distanceKm - 2.0) * 5000.0;
        }
        return BigDecimal.valueOf(fee).setScale(0, RoundingMode.HALF_UP);
    }

    private double calculateDistanceInKm(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return 0.0;
        }
        double earthRadius = 6371; // Bán kính trái đất (km)
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }
}
