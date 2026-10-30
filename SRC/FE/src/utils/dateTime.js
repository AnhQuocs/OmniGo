/**
 * Tiện ích xử lý và định dạng ngày giờ chuẩn Việt Nam (UTC+7, Asia/Ho_Chi_Minh).
 * Tự động chuyển đổi server timestamp (UTC) sang đúng múi giờ Việt Nam.
 */

export const parseToVietnamDate = (dateVal) => {
  if (!dateVal) return null;
  if (dateVal instanceof Date) return isNaN(dateVal.getTime()) ? null : dateVal;

  if (typeof dateVal === 'number') {
    const d = new Date(dateVal > 1e11 ? dateVal : dateVal * 1000);
    return isNaN(d.getTime()) ? null : d;
  }

  let s = String(dateVal).trim();
  if (!s || s === '—' || s === 'N/A') return null;

  if (Array.isArray(dateVal)) {
    const [y, m, d = 1, h = 0, min = 0, sec = 0] = dateVal;
    return new Date(Date.UTC(y, m - 1, d, h, min, sec));
  }

  // Chuẩn hóa định dạng "YYYY-MM-DD HH:mm:ss" sang ISO
  if (/^\d{4}-\d{2}-\d{2}\s+\d{2}:\d{2}/.test(s)) {
    s = s.replace(/\s+/, 'T');
  }

  // Nếu ISO không có múi giờ, coi như UTC từ server lưu trữ
  if (s.includes('T') && !s.endsWith('Z') && !/[+-]\d{2}(:?\d{2})?$/.test(s)) {
    s = s + 'Z';
  }

  const d = new Date(s);
  return isNaN(d.getTime()) ? null : d;
};

/**
 * Định dạng ngày giờ chuẩn Việt Nam: "09:26 30/10/2026"
 */
export const formatVietnamDateTime = (dateVal) => {
  const d = parseToVietnamDate(dateVal);
  if (!d) return dateVal && String(dateVal).trim() ? String(dateVal) : '—';
  try {
    const parts = new Intl.DateTimeFormat('vi-VN', {
      timeZone: 'Asia/Ho_Chi_Minh',
      hour: '2-digit',
      minute: '2-digit',
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour12: false,
    }).formatToParts(d);

    const get = (type) => parts.find((p) => p.type === type)?.value || '';
    return `${get('hour')}:${get('minute')} ${get('day')}/${get('month')}/${get('year')}`;
  } catch {
    return d.toLocaleString('vi-VN');
  }
};

/**
 * Định dạng rút gọn: "09:26 30-10" (dùng cho bảng đơn món, cuốc xe)
 */
export const formatVietnamShortTime = (dateVal) => {
  const d = parseToVietnamDate(dateVal);
  if (!d) return '—';
  try {
    const parts = new Intl.DateTimeFormat('vi-VN', {
      timeZone: 'Asia/Ho_Chi_Minh',
      hour: '2-digit',
      minute: '2-digit',
      day: '2-digit',
      month: '2-digit',
      hour12: false,
    }).formatToParts(d);

    const get = (type) => parts.find((p) => p.type === type)?.value || '';
    return `${get('hour')}:${get('minute')} ${get('day')}-${get('month')}`;
  } catch {
    return formatVietnamDateTime(dateVal);
  }
};

/**
 * Định dạng ngày: "30/10/2026"
 */
export const formatVietnamDate = (dateVal) => {
  const d = parseToVietnamDate(dateVal);
  if (!d) return '—';
  try {
    const parts = new Intl.DateTimeFormat('vi-VN', {
      timeZone: 'Asia/Ho_Chi_Minh',
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
    }).formatToParts(d);

    const get = (type) => parts.find((p) => p.type === type)?.value || '';
    return `${get('day')}/${get('month')}/${get('year')}`;
  } catch {
    return d.toLocaleDateString('vi-VN');
  }
};

