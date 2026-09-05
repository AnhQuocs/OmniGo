import { get, post, put } from './api';

export const driverService = {
  /**
   * Lấy danh sách tài xế
   * Endpoint BE: GET /api/v1/users (filter role DRIVER)
   */
  getAllDrivers: async (params = { page: 0, size: 100 }) => {
    return await get('/api/v1/users', { ...params, role: 'DRIVER' });
  },

  /**
   * Lấy danh sách toàn bộ hồ sơ tài xế từ driver-service
   * Endpoint BE: GET /api/v1/drivers/admin/all
   */
  getAllDriverProfiles: async () => {
    return await get('/api/v1/drivers/admin/all');
  },

  /**
   * Cập nhật toàn diện thông tin cá nhân và phương tiện của tài xế (Admin)
   * Endpoint BE: PUT /api/v1/drivers/{driverId}/admin
   */
  updateDriverAdmin: async (driverId, driverData) => {
    return await put(`/api/v1/drivers/${driverId}/admin`, driverData);
  },

  /**
   * Cập nhật thông tin phương tiện xe của tài xế
   * Endpoint BE: PUT /api/v1/drivers/{driverId}/vehicle
   */
  updateDriverVehicle: async (driverId, vehicleData) => {
    return await put(`/api/v1/drivers/${driverId}/vehicle`, vehicleData);
  },

  /**
   * Đăng ký thêm mới tài khoản tài xế
   * Endpoint BE: POST /api/v1/drivers/register
   */
  createDriver: async (driverPayload) => {
    return await post('/api/v1/drivers/register', driverPayload);
  },

  /**
   * Cập nhật trạng thái bật/tắt hoạt động của tài xế
   * Endpoint BE: PUT /api/v1/drivers/{driverId}/status?isActive={isActive}
   */
  toggleDriverStatus: async (driverId, isActive) => {
    return await put(`/api/v1/drivers/${driverId}/status?isActive=${isActive}`);
  },
};

export default driverService;

