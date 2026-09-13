package com.trung.fooddeliveryservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "food_order_reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodOrderReview extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false, unique = true)
    private Long orderId;

    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "customer_name", length = 100)
    private String customerName;

    @Column(name = "driver_id")
    private Long driverId;

    @Column(name = "restaurant_rating", nullable = false)
    private Integer restaurantRating;

    @Column(name = "restaurant_comment", columnDefinition = "TEXT")
    private String restaurantComment;

    @Column(name = "restaurant_images", columnDefinition = "TEXT")
    private String restaurantImages;

    @Column(name = "driver_rating")
    private Integer driverRating;

    @Column(name = "driver_comment", columnDefinition = "TEXT")
    private String driverComment;

    @Column(name = "merchant_reply", columnDefinition = "TEXT")
    private String merchantReply;

    @Column(name = "merchant_replied_at")
    private LocalDateTime merchantRepliedAt;

    @Column(name = "editable_until", nullable = false)
    private LocalDateTime editableUntil;

    @Column(name = "is_edited")
    @Builder.Default
    private Boolean isEdited = false;
}
