import { get } from './api';

export const paymentService = {
  /**
   * Lấy toàn bộ danh sách giao dịch & thanh toán từ payment-service
   * Endpoint BE: GET /api/v1/payments/admin/transactions
   */
  getAllTransactions: async () => {
    try {
      const res = await get('/api/v1/payments/admin/transactions');
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
      console.warn('Backend payment transactions warning:', error.message);
      throw error;
    }
  },

  /**
   * Lấy thống kê thanh toán & tổng volume dòng tiền từ payment-service
   * Endpoint BE: GET /api/v1/payments/admin/stats
   */
  getPaymentStats: async () => {
    try {
      const res = await get('/api/v1/payments/admin/stats');
      if (res && res.data) {
        return res.data;
      }
      return res || { totalTransactions: 0, successTransactions: 0, totalVolume: 0 };
    } catch (error) {
      console.warn('Backend payment stats warning:', error.message);
      return { totalTransactions: 0, successTransactions: 0, totalVolume: 0 };
    }
  },
};

export default paymentService;
