import { get, post } from './api';

export const userService = {
  /**
   * Lấy danh sách người dùng (Customer / User)
   * Endpoint BE: GET /api/v1/users?page=0&size=10
   */
  getAllUsers: async (params = { page: 0, size: 10 }) => {
    return await get('/api/v1/users', params);
  },

  /**
   * Chi tiết người dùng theo ID
   * Endpoint BE: GET /api/v1/users/{id}
   */
  getUserById: async (id) => {
    return await get(`/api/v1/users/${id}`);
  },

  /**
   * Đăng ký tài khoản khách hàng mới
   * Endpoint BE: POST /api/v1/users/register/customer
   */
  registerCustomer: async (payload) => {
    return await post('/api/v1/users/register/customer', payload);
  },
};

export default userService;
