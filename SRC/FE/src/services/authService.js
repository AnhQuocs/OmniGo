import { post } from './api';

export const authService = {
  /**
   * Đăng nhập Admin bằng Số Điện Thoại & Mật Khẩu
   * Endpoint BE: POST /api/v1/auth/login
   * Body: { phoneNumber, password }
   */
  login: async ({ phoneNumber, password }) => {
    try {
      const response = await post('/api/v1/auth/login', {
        phoneNumber,
        password,
      });

      // Backend returns: ApiResponse<LoginResponse> { success, message, data: { accessToken, refreshToken, user } }
      if (response && response.success && response.data) {
        // Check if user has ADMIN role
        const user = response.data.user;
        if (user.role !== 'ADMIN') {
          throw new Error('Tài khoản không có quyền Quản trị viên (ADMIN)');
        }
        return {
          user: response.data.user,
          token: response.data.accessToken,
          refreshToken: response.data.refreshToken,
        };
      }

      throw new Error(response?.message || 'Đăng nhập không thành công');
    } catch (error) {
      const message =
        error.response?.data?.message ||
        error.response?.data?.error ||
        error.message ||
        'Không thể kết nối đến máy chủ Backend (Connection Refused)';
      throw new Error(message);
    }
  },

  /**
   * Đăng xuất
   * Endpoint BE: POST /api/v1/auth/logout
   */
  logout: async () => {
    try {
      return await post('/api/v1/auth/logout', {});
    } catch (error) {
      console.warn('Backend logout failed:', error.message);
      return { success: true };
    }
  },

  /**
   * Làm mới Access Token
   * Endpoint BE: POST /api/v1/auth/refresh
   */
  refreshToken: async () => {
    return await post('/api/v1/auth/refresh', {});
  },
};

export default authService;
