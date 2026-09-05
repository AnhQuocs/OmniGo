import { get } from './api';

export const bookingService = {
  /**
   * Lấy toàn bộ danh sách chuyến xe trong hệ thống từ booking-service
   * Endpoint BE: GET /api/v1/bookings/admin/all
   */
  getAllBookings: async () => {
    try {
      const res = await get('/api/v1/bookings/admin/all');
      if (res && res.data) {
        return Array.isArray(res.data) ? res.data : (res.data.content || []);
      }
      if (Array.isArray(res)) {
        return res;
      }
      if (res && res.content && Array.isArray(res.content)) {
        return res.content;
      }
      return [];
    } catch (error) {
      console.warn('Backend bookings endpoint warning:', error.message);
      throw error;
    }
  },

  /**
   * Lấy số liệu thống kê chuyến xe thời gian thực từ booking-service
   * Endpoint BE: GET /api/v1/bookings/admin/stats
   */
  getBookingStats: async () => {
    try {
      const res = await get('/api/v1/bookings/admin/stats');
      if (res && res.data) {
        return res.data;
      }
      return res || { totalBookings: 0, completedBookings: 0, inProgressBookings: 0, cancelledBookings: 0, pendingBookings: 0, totalGmv: 0 };
    } catch (error) {
      console.warn('Backend booking stats warning:', error.message);
      return { totalBookings: 0, completedBookings: 0, inProgressBookings: 0, cancelledBookings: 0, pendingBookings: 0, totalGmv: 0 };
    }
  },
};

export default bookingService;
