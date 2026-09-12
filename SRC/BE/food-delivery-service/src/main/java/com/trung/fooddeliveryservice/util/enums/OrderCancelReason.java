package com.trung.fooddeliveryservice.util.enums;

public enum OrderCancelReason {
    // Khách hàng hủy
    CUSTOMER_CHANGE_MIND("Khách đổi ý, không muốn đặt nữa"),
    CUSTOMER_PAYMENT_FAILED("Thanh toán trực tuyến không thành công hoặc đã hủy giao dịch"),
    CUSTOMER_WAIT_TOO_LONG("Thời gian chờ món quá lâu"),
    CUSTOMER_WRONG_ORDER("Đặt nhầm món hoặc sai địa chỉ"),
    CUSTOMER_OTHER("Lý do khác từ khách hàng"),

    // Nhà hàng hủy
    RESTAURANT_OUT_OF_STOCK("Hết món / hết nguyên liệu"),
    RESTAURANT_OVERLOADED("Quán đang quá tải, không kịp làm món"),
    RESTAURANT_CLOSING("Quán sắp đến giờ đóng cửa"),
    RESTAURANT_OTHER("Lý do khác từ quán"),

    // Tài xế hủy
    DRIVER_VEHICLE_BREAKDOWN("Sự cố phương tiện di chuyển"),
    DRIVER_WEATHER_INCLEMENT("Thời tiết xấu / mưa ngập"),
    DRIVER_CANNOT_CONTACT("Không thể liên lạc khách hàng hoặc quán"),
    DRIVER_DISTANCE_TOO_FAR("Khoảng cách giao hàng quá xa"),
    DRIVER_OTHER("Lý do khác từ tài xế"),

    // Hệ thống tự động hủy
    SYSTEM_NO_DRIVER_FOUND("Không tìm thấy tài xế sau nhiều lần quét"),
    SYSTEM_TIMEOUT("Quá thời gian xử lý đơn hàng");

    private final String defaultDescription;

    OrderCancelReason(String defaultDescription) {
        this.defaultDescription = defaultDescription;
    }

    public String getDefaultDescription() {
        return defaultDescription;
    }
}
