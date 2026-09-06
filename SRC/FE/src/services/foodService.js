import { get, patch, post } from './api';

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
};

export default foodService;
