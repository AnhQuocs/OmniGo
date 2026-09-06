package com.trung.bookingservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trung.bookingservice.dto.response.DriverNearbyResponse;
import com.trung.bookingservice.event.DriverAssignedToFoodOrderEvent;
import com.trung.bookingservice.event.FindDriverForFoodOrderEvent;
import com.trung.bookingservice.exception.BadRequestException;
import com.trung.bookingservice.service.FoodDeliveryDispatchService;
import com.trung.bookingservice.service.client.LocationClient;
import com.trung.bookingservice.service.client.UserDriverClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class FoodDeliveryDispatchServiceImpl implements FoodDeliveryDispatchService {

    public static final String DRIVER_ASSIGNED_TOPIC = "DRIVER_ASSIGNED_TO_FOOD_ORDER";

    private final LocationClient locationClient;
    private final UserDriverClient userDriverClient;
    private final StringRedisTemplate redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void dispatchDriverForFoodOrder(FindDriverForFoodOrderEvent event) {
        log.info("Bắt đầu tìm tài xế giao đồ ăn cho đơn hàng ID {}", event.getOrderId());

        // Lưu event vào Redis để có thể dispatch lại cho tài xế tiếp theo nếu tài xế hiện tại bỏ qua
        try {
            redisTemplate.opsForValue().set(
                    "food_order:event:" + event.getOrderId(),
                    objectMapper.writeValueAsString(event),
                    5,
                    TimeUnit.MINUTES
            );
        } catch (Exception e) {
            log.error("Lỗi khi lưu event food_order vào Redis: {}", e.getMessage());
        }

        // Lấy danh sách tài xế đã từ chối đơn này
        Set<String> rejectedDrivers = redisTemplate.opsForSet().members("food_order:rejected_drivers:" + event.getOrderId());

        Double restLng = event.getRestaurantLongitude() != null ? event.getRestaurantLongitude() : 105.8574;
        Double restLat = event.getRestaurantLatitude() != null ? event.getRestaurantLatitude() : 21.0245;

        List<DriverNearbyResponse> allNearbyDrivers;
        try {
            allNearbyDrivers = locationClient.getNearbyDeliveryDrivers(
                    restLng,
                    restLat,
                    20.0
            );
        } catch (Exception e) {
            log.error("Lỗi khi gọi location-service tìm tài xế giao đồ ăn: {}", e.getMessage());
            return;
        }

        if (allNearbyDrivers == null || allNearbyDrivers.isEmpty()) {
            log.warn("Không tìm thấy tài xế nào khả dụng quanh nhà hàng '{}'", event.getRestaurantName());
            return;
        }

        for (DriverNearbyResponse driver : allNearbyDrivers) {
            Long driverId = driver.getDriverId();

            // Nếu tài xế đã từng từ chối đơn này thì bỏ qua
            if (rejectedDrivers != null && rejectedDrivers.contains(driverId.toString())) {
                log.info("Tài xế ID {} đã từ chối đơn #{} trước đó, bỏ qua", driverId, event.getOrderId());
                continue;
            }

            try {
                ResponseEntity<Boolean> onlineResp = userDriverClient.isDriverOnlineInternal(driverId);
                if (!Boolean.TRUE.equals(onlineResp.getBody())) {
                    continue;
                }
            } catch (Exception e) {
                log.error("Không thể kiểm tra trạng thái online của tài xế {}: {}", driverId, e.getMessage());
                continue;
            }

            // Chiếm khóa phân tán (Distributed Lock) trong 30 giây để tài xế này không nhận trùng cuốc xe hoặc đơn khác
            Boolean isLockAcquired = redisTemplate.opsForValue().setIfAbsent(
                    "drivers:reserved:" + driverId,
                    "food_order:" + event.getOrderId(),
                    30,
                    TimeUnit.SECONDS
            );

            if (Boolean.TRUE.equals(isLockAcquired)) {
                log.info("Đã giữ chỗ thành công tài xế ID {} (cách quán {} km) cho đơn đồ ăn ID {}",
                        driverId, String.format("%.2f", driver.getDistanceInKm()), event.getOrderId());

                redisTemplate.opsForValue().set(
                        "food_order:driver:" + event.getOrderId(),
                        driverId.toString(),
                        25,
                        TimeUnit.SECONDS
                );

                // Bắn WebSocket thông báo mời tài xế nhận đơn giao đồ ăn
                messagingTemplate.convertAndSendToUser(
                        driverId.toString(),
                        "/queue/driver/food-match",
                        event
                );

                // Đã gửi lời mời tới 1 tài xế phù hợp nhất, tạm thời dừng lại chờ phản hồi
                return;
            }
        }

        log.warn("Đã quét tất cả tài xế lân cận nhưng chưa khóa được tài xế khả dụng cho đơn {}", event.getOrderId());
    }

    @Override
    public void acceptFoodOrder(Long orderId, Long driverId) throws BadRequestException {
        log.info("Tài xế ID {} chấp nhận đơn đồ ăn ID {}", driverId, orderId);

        // Khóa phân tán trên đơn hàng để tránh 2 tài xế bấm nhận cùng lúc
        Boolean assignedLock = redisTemplate.opsForValue().setIfAbsent(
                "food_order:assigned:" + orderId,
                driverId.toString(),
                1,
                TimeUnit.HOURS
        );

        if (Boolean.FALSE.equals(assignedLock)) {
            log.warn("Đơn hàng ID {} đã có tài xế khác nhận trước!", orderId);
            throw new BadRequestException("Đơn hàng này đã có tài xế khác nhận trước hoặc không còn khả dụng");
        }

        // Xóa key giữ chỗ tạm thời
        redisTemplate.delete("drivers:reserved:" + driverId);
        redisTemplate.delete("food_order:driver:" + orderId);
        redisTemplate.delete("food_order:event:" + orderId);

        // Bắn event Kafka thông báo tài xế đã được gán về food-delivery-service
        DriverAssignedToFoodOrderEvent assignedEvent = DriverAssignedToFoodOrderEvent.builder()
                .orderId(orderId)
                .driverId(driverId)
                .assignedAt(LocalDateTime.now())
                .build();

        log.info("Bắn event DRIVER_ASSIGNED_TO_FOOD_ORDER qua Kafka cho đơn ID {}", orderId);
        kafkaTemplate.send(DRIVER_ASSIGNED_TOPIC, String.valueOf(orderId), assignedEvent);
    }

    @Override
    public void rejectFoodOrder(Long orderId, Long driverId) {
        log.info("Tài xế ID {} bỏ qua đơn đồ ăn ID {}", driverId, orderId);

        // Xóa khóa giữ chỗ của tài xế này
        redisTemplate.delete("drivers:reserved:" + driverId);
        redisTemplate.delete("food_order:driver:" + orderId);

        // Lưu tài xế này vào tập danh sách từ chối đơn này (expire sau 5 phút)
        redisTemplate.opsForSet().add("food_order:rejected_drivers:" + orderId, driverId.toString());
        redisTemplate.expire("food_order:rejected_drivers:" + orderId, 5, TimeUnit.MINUTES);

        // Lấy lại event từ Redis và tìm tài xế tiếp theo
        String eventJson = redisTemplate.opsForValue().get("food_order:event:" + orderId);
        if (eventJson != null) {
            try {
                FindDriverForFoodOrderEvent event = objectMapper.readValue(eventJson, FindDriverForFoodOrderEvent.class);
                log.info("Tiếp tục tìm tài xế khác cho đơn đồ ăn ID {} sau khi tài xế {} từ chối", orderId, driverId);
                dispatchDriverForFoodOrder(event);
            } catch (Exception e) {
                log.error("Lỗi parse event để dispatch lại tài xế: {}", e.getMessage());
            }
        }
    }
}
