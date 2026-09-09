import { get, put } from './api';

export const dispatchConfigService = {
  /**
   * Lấy cấu hình bán kính điều phối hiện tại từ Redis qua booking-service
   * Endpoint BE: GET /api/v1/bookings/admin/dispatch-config
   */
  getDispatchConfig: async () => {
    const response = await get('/api/v1/bookings/admin/dispatch-config');
    if (response && response.data) {
      return response.data;
    }
    return response;
  },

  /**
   * Cập nhật cấu hình bán kính điều phối
   * Endpoint BE: PUT /api/v1/bookings/admin/dispatch-config
   */
  updateDispatchConfig: async (configPayload) => {
    return await put('/api/v1/bookings/admin/dispatch-config', configPayload);
  },
};

export default dispatchConfigService;
