import { get, post, put } from './api';

export const driverService = {
  /**
   * Lấy danh sách tài xế
   * Endpoint BE: GET /api/v1/users (filter role DRIVER)
   */
  getAllDrivers: async (params = { page: 0, size: 10 }) => {
    return await get('/api/v1/users', { ...params, role: 'DRIVER' });
  },

  /**
   * Cập nhật trạng thái bật/tắt hoạt động của tài xế
   * Endpoint BE: PUT /api/v1/drivers/{driverId}/status?isActive={isActive}
   */
  toggleDriverStatus: async (driverId, isActive) => {
    return await put(`/api/v1/drivers/${driverId}/status?isActive=${isActive}`);
  },

  /**
   * Cập nhật thông tin phương tiện xe của tài xế
   * Endpoint BE: PUT /api/v1/drivers/{driverId}/vehicle
   */
  updateDriverVehicle: async (driverId, vehicleData) => {
    return await put(`/api/v1/drivers/${driverId}/vehicle`, vehicleData);
  },

  /**
   * Đăng ký tài xế mới
   * Endpoint BE: POST /api/v1/drivers/register
   */
  registerDriver: async (driverPayload) => {
    return await post('/api/v1/drivers/register', driverPayload);
  },
};

export default driverService;
