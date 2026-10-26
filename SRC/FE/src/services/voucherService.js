import { get, post, patch, del } from './api';

export const voucherService = {
  /**
   * Lấy toàn bộ danh sách mã khuyến mãi từ Redis
   * Endpoint BE: GET /api/v1/pricing/admin/vouchers
   */
  getAllVouchers: async () => {
    const response = await get('/api/v1/pricing/admin/vouchers');
    if (response && response.data) {
      return response.data;
    }
    return response || [];
  },

  /**
   * Tạo mới mã khuyến mãi lưu vào Redis
   * Endpoint BE: POST /api/v1/pricing/admin/vouchers
   */
  createVoucher: async (payload) => {
    return await post('/api/v1/pricing/admin/vouchers', payload);
  },

  /**
   * Cập nhật mã khuyến mãi
   * Endpoint BE: PUT /api/v1/pricing/admin/vouchers/:code
   */
  updateVoucher: async (code, payload) => {
    return await put(`/api/v1/pricing/admin/vouchers/${code}`, payload);
  },

  /**
   * Xem chi tiết voucher theo mã
   * Endpoint BE: GET /api/v1/pricing/admin/vouchers/:code
   */
  getVoucherByCode: async (code) => {
    const response = await get(`/api/v1/pricing/admin/vouchers/${code}`);
    return response?.data || response;
  },

  /**
   * Bật / Tắt trạng thái kích hoạt của voucher
   * Endpoint BE: PATCH /api/v1/pricing/admin/vouchers/:code/toggle
   */
  toggleVoucherStatus: async (code) => {
    return await patch(`/api/v1/pricing/admin/vouchers/${code}/toggle`);
  },

  /**
   * Xóa hoàn toàn voucher khỏi Redis
   * Endpoint BE: DELETE /api/v1/pricing/admin/vouchers/:code
   */
  deleteVoucher: async (code) => {
    return await del(`/api/v1/pricing/admin/vouchers/${code}`);
  },

  /**
   * Thử áp dụng voucher cho đơn hàng
   * Endpoint BE: POST /api/v1/pricing/vouchers/apply
   */
  applyVoucher: async (payload) => {
    return await post('/api/v1/pricing/vouchers/apply', payload);
  },

  /**
   * Lấy lịch sử sử dụng của một mã voucher (Audit log)
   * Endpoint BE: GET /api/v1/pricing/admin/vouchers/:code/usages
   */
  getVoucherUsages: async (code) => {
    const response = await get(`/api/v1/pricing/admin/vouchers/${code}/usages`);
    return response?.data || response || [];
  },
};

export default voucherService;
