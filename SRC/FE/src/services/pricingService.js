import { get, put } from './api';

export const pricingService = {
  /**
   * Lấy cấu hình giá cước hiện tại từ Redis qua BE
   * Endpoint BE: GET /api/v1/pricing/admin/config
   */
  getPricingConfig: async () => {
    const response = await get('/api/v1/pricing/admin/config');
    if (response && response.data) {
      return response.data;
    }
    return response;
  },

  /**
   * Cập nhật cấu hình giá cước
   * Endpoint BE: PUT /api/v1/pricing/admin/config
   */
  updatePricingConfig: async (configPayload) => {
    return await put('/api/v1/pricing/admin/config', configPayload);
  },
};

export default pricingService;
