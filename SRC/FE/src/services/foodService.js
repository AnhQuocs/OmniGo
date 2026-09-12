import { get, patch, post, put, del } from './api';

export const foodService = {
  /**
   * Lấy toàn bộ danh sách nhà hàng trên hệ thống
   * Endpoint: GET /api/v1/restaurants
   */
  getAllRestaurants: async (search = '') => {
    try {
      const url = search ? `/api/v1/restaurants?search=${encodeURIComponent(search)}` : '/api/v1/restaurants';
      const res = await get(url);
      if (res && res.data) {
        return Array.isArray(res.data) ? res.data : [];
      }
      return Array.isArray(res) ? res : [];
    } catch (error) {
      console.warn('Lỗi tải danh sách nhà hàng:', error.message);
      return [];
    }
  },

  /**
   * Lấy chi tiết nhà hàng theo ID
   */
  getRestaurantById: async (id) => {
    try {
      const res = await get(`/api/v1/restaurants/${id}`);
      return res?.data || res;
    } catch (error) {
      console.warn(`Lỗi tải nhà hàng #${id}:`, error.message);
      throw error;
    }
  },

  /**
   * Lấy thực đơn của nhà hàng
   * Endpoint: GET /api/v1/restaurants/{restaurantId}/items
   */
  getMenuItems: async (restaurantId) => {
    try {
      const res = await get(`/api/v1/restaurants/${restaurantId}/items`);
      if (res && res.data) {
        return Array.isArray(res.data) ? res.data : [];
      }
      return Array.isArray(res) ? res : [];
    } catch (error) {
      console.warn(`Lỗi tải thực đơn quán #${restaurantId}:`, error.message);
      return [];
    }
  },

  /**
   * Cập nhật trạng thái nhà hàng (OPEN, BUSY, CLOSED)
   */
  updateRestaurantStatus: async (restaurantId, status) => {
    try {
      const res = await patch(`/api/v1/restaurants/${restaurantId}/status`, { status });
      return res?.data || res;
    } catch (error) {
      console.warn(`Lỗi cập nhật trạng thái quán #${restaurantId}:`, error.message);
      throw error;
    }
  },

  /**
   * Khóa / Mở khóa gian hàng nhà hàng (Admin)
   * Endpoint: PATCH /api/v1/restaurants/{id}/lock
   */
  toggleLockRestaurant: async (restaurantId, payload) => {
    try {
      const res = await patch(`/api/v1/restaurants/${restaurantId}/lock`, payload);
      return res?.data || res;
    } catch (error) {
      console.warn(`Lỗi khóa/mở khóa quán #${restaurantId}:`, error.message);
      throw error;
    }
  },

  /**
   * Đăng ký đối tác nhà hàng mới (đồng thời tạo user role RESTAURANT)
   * Endpoint: POST /api/v1/restaurants/partner
   */
  registerPartnerRestaurant: async (partnerData) => {
    try {
      const res = await post('/api/v1/restaurants/partner', partnerData);
      return res?.data || res;
    } catch (error) {
      console.warn('Lỗi đăng ký đối tác nhà hàng:', error.message);
      throw error;
    }
  },

  /**
   * Lấy toàn bộ danh sách đơn đặt món đồ ăn (Admin Telemetry)
   * Endpoint: GET /api/v1/food-orders
   */
  getAllFoodOrders: async () => {
    try {
      const res = await get('/api/v1/food-orders');
      if (res && res.data) {
        return Array.isArray(res.data) ? res.data : [];
      }
      return Array.isArray(res) ? res : [];
    } catch (error) {
      console.warn('Lỗi tải danh sách đơn đặt món:', error.message);
      return [];
    }
  },

  /**
   * Lấy chi tiết đơn đặt món theo ID
   */
  getFoodOrderById: async (orderId) => {
    try {
      const res = await get(`/api/v1/food-orders/${orderId}`);
      return res?.data || res;
    } catch (error) {
      console.warn(`Lỗi tải đơn hàng #${orderId}:`, error.message);
      throw error;
    }
  },

  /**
   * Lấy thống kê tổng quan dịch vụ giao đồ ăn cho Admin
   * Endpoint: GET /api/v1/food-orders/admin/stats
   */
  getFoodStats: async () => {
    try {
      const res = await get('/api/v1/food-orders/admin/stats');
      return res?.data || res || {
        totalOrders: 0,
        completedOrders: 0,
        deliveringOrders: 0,
        preparingOrders: 0,
        pendingOrders: 0,
        cancelledOrders: 0,
        totalRevenue: 0,
        totalDeliveryFees: 0,
      };
    } catch (error) {
      console.warn('Lỗi tải thống kê đồ ăn:', error.message);
      return {
        totalOrders: 0,
        completedOrders: 0,
        deliveringOrders: 0,
        preparingOrders: 0,
        pendingOrders: 0,
        cancelledOrders: 0,
        totalRevenue: 0,
        totalDeliveryFees: 0,
      };
    }
  },

  /**
   * Xác nhận thanh toán đơn đồ ăn
   */
  markOrderPaid: async (orderId) => {
    try {
      const res = await post(`/api/v1/food-orders/${orderId}/paid`);
      return res?.data || res;
    } catch (error) {
      console.warn(`Lỗi xác nhận thanh toán đơn #${orderId}:`, error.message);
      throw error;
    }
  },

  /**
   * Lấy thông tin nhà hàng của người dùng hiện tại (Merchant Portal)
   * Endpoint: GET /api/v1/restaurants/my-restaurant
   */
  getMyRestaurant: async () => {
    try {
      const res = await get('/api/v1/restaurants/my-restaurant');
      return res?.data || res;
    } catch (error) {
      console.warn('Lỗi lấy thông tin nhà hàng cá nhân:', error.message);
      throw error;
    }
  },

  /**
   * Đăng ký nhà hàng mới cho người dùng
   * Endpoint: POST /api/v1/restaurants
   */
  createRestaurant: async (data) => {
    try {
      const res = await post('/api/v1/restaurants', data);
      return res?.data || res;
    } catch (error) {
      console.warn('Lỗi tạo nhà hàng mới:', error.message);
      throw error;
    }
  },

  /**
   * Cập nhật thông tin nhà hàng
   * Endpoint: PUT /api/v1/restaurants/{id}
   */
  updateRestaurant: async (id, data) => {
    try {
      const res = await put(`/api/v1/restaurants/${id}`, data);
      return res?.data || res;
    } catch (error) {
      console.warn(`Lỗi cập nhật nhà hàng #${id}:`, error.message);
      throw error;
    }
  },

  /**
   * Lấy danh sách đơn hàng của một nhà hàng cụ thể
   * Endpoint: GET /api/v1/food-orders/restaurant/{restaurantId}
   */
  getRestaurantOrders: async (restaurantId) => {
    try {
      const res = await get(`/api/v1/food-orders/restaurant/${restaurantId}`);
      if (res && res.data) {
        return Array.isArray(res.data) ? res.data : [];
      }
      return Array.isArray(res) ? res : [];
    } catch (error) {
      console.warn(`Lỗi lấy đơn hàng của quán #${restaurantId}:`, error.message);
      return [];
    }
  },

  /**
   * Cập nhật trạng thái đơn hàng (ACCEPTED, PREPARING, READY_FOR_PICKUP, CANCELLED, REJECTED)
   * Endpoint: PATCH /api/v1/food-orders/{orderId}/status
   */
  updateOrderStatus: async (orderId, status, reason = null, reasonCode = null) => {
    try {
      const payload = { status };
      if (reason) payload.reason = reason;
      if (reasonCode) payload.reasonCode = reasonCode;
      const res = await patch(`/api/v1/food-orders/${orderId}/status`, payload);
      return res?.data || res;
    } catch (error) {
      console.warn(`Lỗi cập nhật trạng thái đơn hàng #${orderId}:`, error.message);
      throw error;
    }
  },

  /**
   * Khách hàng hủy đơn hàng
   * Endpoint: POST /api/v1/food-orders/{orderId}/cancel
   */
  cancelOrder: async (orderId, reason = null, reasonCode = null) => {
    try {
      const payload = {};
      if (reason) payload.reason = reason;
      if (reasonCode) payload.reasonCode = reasonCode;
      const res = await post(`/api/v1/food-orders/${orderId}/cancel`, payload);
      return res?.data || res;
    } catch (error) {
      console.warn(`Lỗi hủy đơn hàng #${orderId}:`, error.message);
      throw error;
    }
  },

  /**
   * Chuyển phương thức thanh toán sang Tiền mặt (CASH)
   * Endpoint: PATCH /api/v1/food-orders/{orderId}/switch-to-cash
   */
  switchToCashPayment: async (orderId) => {
    try {
      const res = await patch(`/api/v1/food-orders/${orderId}/switch-to-cash`);
      return res?.data || res;
    } catch (error) {
      console.warn(`Lỗi chuyển đơn #${orderId} sang tiền mặt:`, error.message);
      throw error;
    }
  },

  /**
   * Thêm món ăn mới cho nhà hàng
   * Endpoint: POST /api/v1/restaurants/{restaurantId}/items
   */
  createMenuItem: async (restaurantId, data) => {
    try {
      const res = await post(`/api/v1/restaurants/${restaurantId}/items`, data);
      return res?.data || res;
    } catch (error) {
      console.warn(`Lỗi thêm món ăn cho quán #${restaurantId}:`, error.message);
      throw error;
    }
  },

  /**
   * Cập nhật món ăn
   * Endpoint: PUT /api/v1/items/{itemId}
   */
  updateMenuItem: async (itemId, data) => {
    try {
      const res = await put(`/api/v1/items/${itemId}`, data);
      return res?.data || res;
    } catch (error) {
      console.warn(`Lỗi cập nhật món ăn #${itemId}:`, error.message);
      throw error;
    }
  },

  /**
   * Xóa món ăn
   * Endpoint: DELETE /api/v1/items/{itemId}
   */
  deleteMenuItem: async (itemId) => {
    try {
      const res = await del(`/api/v1/items/${itemId}`);
      return res?.data || res;
    } catch (error) {
      console.warn(`Lỗi xóa món ăn #${itemId}:`, error.message);
      throw error;
    }
  },
};

export default foodService;
