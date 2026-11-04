package com.example.omnigo.features.driver.common.config

/**
 * Cấu hình tỷ lệ chiết khấu sàn nền tảng OmniGo dành cho tài xế.
 * Tỷ lệ này chỉ dùng để ước tính cho cuốc xe đang chạy trước khi có xác nhận thanh toán/khấu trừ thực tế từ API.
 */
object PlatformFeeConfig {

    /**
     * Tỷ lệ chiết khấu sàn mặc định khi hệ thống/API chưa cung cấp tỷ lệ riêng.
     */
    const val DEFAULT_COMMISSION_RATE: Double = 0.20

    /**
     * Tỷ lệ chiết khấu động được cập nhật từ cấu hình backend hoặc API.
     */
    @Volatile
    var dynamicCommissionRate: Double? = null

    val effectiveRate: Double
        get() = dynamicCommissionRate ?: DEFAULT_COMMISSION_RATE

    /**
     * Cập nhật tỷ lệ chiết khấu sàn từ hệ thống hoặc API.
     */
    fun updateCommissionRate(rate: Double) {
        dynamicCommissionRate = if (rate > 1.0) rate / 100.0 else rate
    }

    /**
     * Tính phí sàn ước tính theo tỷ lệ cấu hình.
     */
    fun calculateEstimatedFee(
        grossAmount: Double,
        rate: Double = effectiveRate
    ): Double {
        return (grossAmount * rate).coerceAtLeast(0.0)
    }

    /**
     * Tính thu nhập ước tính của tài xế sau khi trừ phí sàn ước tính.
     */
    fun calculateEstimatedEarnings(
        grossAmount: Double,
        rate: Double = effectiveRate
    ): Double {
        val fee = calculateEstimatedFee(grossAmount, rate)
        return (grossAmount - fee).coerceAtLeast(0.0)
    }
}
