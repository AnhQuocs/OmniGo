/**
 * Tiện ích xử lý và định dạng ngày giờ chuẩn Việt Nam (UTC+7, Asia/Ho_Chi_Minh).
 * Tự động chuyển đổi server timestamp (UTC hoặc LocalDateTime) sang đúng múi giờ Việt Nam.
 */

export const parseToVietnamDate = (dateVal) => {
  if (!dateVal) return null;
  if (dateVal instanceof Date) return isNaN(dateVal.getTime()) ? null : dateVal;

  if (typeof dateVal === 'number') {
    const d = new Date(dateVal > 1e11 ? dateVal : dateVal * 1000);
    return isNaN(d.getTime()) ? null : d;
  }

  let s = String(dateVal).trim();
  if (!s || s === '—' || s === '-' || s === 'N/A' || s === 'null' || s === 'undefined') return null;

  if (Array.isArray(dateVal)) {
    const [y, m, d = 1, h = 0, min = 0, sec = 0] = dateVal;
    return new Date(Date.UTC(y, m - 1, d, h - 7, min, sec));
  }

  // Chuẩn hóa định dạng "YYYY-MM-DD HH:mm:ss" sang ISO
  if (/^\d{4}-\d{2}-\d{2}\s+\d{2}:\d{2}/.test(s)) {
    s = s.replace(/\s+/, 'T');
  }

  // Nếu ISO có múi giờ cụ thể (Z hoặc +/-HH:mm)
  if (s.endsWith('Z') || /[+-]\d{2}(:?\d{2})?$/.test(s)) {
    const d = new Date(s);
    return isNaN(d.getTime()) ? null : d;
  }

  // Nếu chuỗi ISO không có múi giờ (ví dụ "2026-11-04T10:07:21.546575"),
  // chuỗi này vốn đã là giờ địa phương máy chủ Việt Nam (Asia/Ho_Chi_Minh, UTC+7) sinh ra từ LocalDateTime.
  // Khi format qua Intl với timeZone: 'Asia/Ho_Chi_Minh', Date UTC cần được lùi 7 tiếng
  // để khi bù múi giờ UTC+7 sẽ hiển thị đúng chính xác ngày giờ địa phương ban đầu.
  const localMatch = s.match(/^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})(?::(\d{2}))?(?:\.(\d+))?/);
  if (localMatch) {
    const [, y, m, d, h, min, sec = 0, ms = 0] = localMatch;
    const msNum = Number(String(ms).padEnd(3, '0').slice(0, 3));
    return new Date(Date.UTC(Number(y), Number(m) - 1, Number(d), Number(h) - 7, Number(min), Number(sec), msNum));
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
