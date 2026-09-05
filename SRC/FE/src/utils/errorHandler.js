/**
 * Utility helper to parse Backend API error responses (400, 409, 422, etc.)
 * Extracts exact error message and maps errors to specific form fields.
 */
export const parseApiError = (error, defaultFallback = 'Đã có lỗi xảy ra. Vui lòng kiểm tra lại thông tin.') => {
  const data = error.response?.data;
  const fieldErrors = {};
  let message = '';

  // 1. If backend returns error field as an object (e.g. validation errors map { field: message })
  if (data && typeof data.error === 'object' && data.error !== null && !Array.isArray(data.error)) {
    Object.entries(data.error).forEach(([field, msg]) => {
      fieldErrors[field] = msg;
    });
    // Build combined message if not already descriptive
    const msgs = Object.values(data.error).filter(Boolean);
    if (msgs.length > 0) {
      message = msgs.join('; ');
    }
  }

  // 2. If data.message or data.error is a descriptive string
  if (!message) {
    if (typeof data?.error === 'string' && data.error.trim() && data.error !== 'Xung đột dữ liệu' && data.error !== 'Yêu cầu không hợp lệ') {
      message = data.error;
    } else if (typeof data?.message === 'string' && data.message.trim() && data.message !== 'Xung đột dữ liệu' && data.message !== 'Yêu cầu không hợp lệ') {
      message = data.message;
    } else if (typeof data?.error === 'string' && data.error.trim()) {
      message = data.error;
    } else if (typeof data?.message === 'string' && data.message.trim()) {
      message = data.message;
    } else if (error.message) {
      message = error.message;
    } else {
      message = defaultFallback;
    }
  }

  // 3. Heuristic matching if fieldErrors is still empty
  const lowerMsg = (message || '').toLowerCase();

  if (!fieldErrors.phoneNumber && (lowerMsg.includes('số điện thoại') || lowerMsg.includes('sđt') || lowerMsg.includes('phone'))) {
    fieldErrors.phoneNumber = message;
  }
  if (!fieldErrors.licensePlate && (lowerMsg.includes('biển số') || lowerMsg.includes('license') || lowerMsg.includes('plate'))) {
    fieldErrors.licensePlate = message;
  }
  if (!fieldErrors.email && lowerMsg.includes('email')) {
    fieldErrors.email = message;
  }
  if (!fieldErrors.password && (lowerMsg.includes('mật khẩu') || lowerMsg.includes('password'))) {
    fieldErrors.password = message;
  }
  if (!fieldErrors.fullName && (lowerMsg.includes('họ và tên') || lowerMsg.includes('họ tên') || lowerMsg.includes('fullname'))) {
    fieldErrors.fullName = message;
  }
  if (!fieldErrors.vehicleType && (lowerMsg.includes('loại phương tiện') || lowerMsg.includes('loại xe') || lowerMsg.includes('vehicletype'))) {
    fieldErrors.vehicleType = message;
  }
  if (!fieldErrors.vehicleModel && (lowerMsg.includes('dòng xe') || lowerMsg.includes('model'))) {
    fieldErrors.vehicleModel = message;
  }

  return { message, fieldErrors };
};

export default parseApiError;
