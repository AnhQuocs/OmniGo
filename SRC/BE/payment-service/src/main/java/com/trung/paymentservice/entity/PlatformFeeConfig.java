package com.trung.paymentservice.entity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
@Entity
@Table(name = "platform_fee_config")
@Getter @Setter @NoArgsConstructor
public class PlatformFeeConfig {
    @Id private Long id = 1L;
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal percentage = new BigDecimal("20.00");
}
