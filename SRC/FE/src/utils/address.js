/**
 * Tiện ích xử lý và chuẩn hóa địa chỉ, tọa độ GPS và nhận diện địa danh Hà Nội.
 */

export const KNOWN_LOCATIONS = {
  '21.0245,105.8574': 'Tràng Tiền, Hoàn Kiếm, Hà Nội',
  '21.0585,105.8202': 'Xuân La, Tây Hồ, Hà Nội',
  '21.0285,105.8542': 'Đinh Tiên Hoàng, Hoàn Kiếm, Hà Nội',
  '21.0031,105.8431': 'Đại Cồ Việt, Hai Bà Trưng, Hà Nội',
  '21.0378,105.7836': 'Duy Tân, Cầu Giấy, Hà Nội',
  '21.0135,105.7988': 'Trung Hòa, Cầu Giấy, Hà Nội',
  '21.0333,105.8500': 'Điện Biên Phủ, Ba Đình, Hà Nội',
  '21.0307,105.7845': 'Trần Thái Tông, Cầu Giấy, Hà Nội',
  '21.0175,105.8540': 'Phố Huế, Hai Bà Trưng, Hà Nội',
  '21.0185,105.8266': 'Hoàng Cầu, Đống Đa, Hà Nội',
};

/**
 * Kiểm tra xem chuỗi địa chỉ có phải là chuỗi giữ chỗ GPS thô không có tên đường hay không.
 */
export const isRawGpsPlaceholder = (addr) => {
  if (!addr) return true;
  const s = String(addr).trim().toLowerCase();
  if (!s || s === '—' || s === 'n/a' || s === 'null' || s === 'undefined') return true;
  if (/^vị trí (gps|hiện tại|của bạn|của tôi)/i.test(s)) return true;
  if (/^vị trí\s*\(?\d/i.test(s)) return true;
  if (/^tọa độ/i.test(s)) return true;
  if (
    s.includes('vị trí gps') ||
    s.includes('gps của bạn') ||
    s.includes('gps hiện tại') ||
    s.includes('điểm đón gps') ||
    s.includes('hiện tại của tôi') ||
    s.includes('vị trí hiện tại')
  ) {
    return true;
  }
  return false;
};

/**
 * Kiểm tra địa chỉ có bị rút gọn (chỉ có số nhà như "131", "16") hoặc plus-code ("XP9P+578") hay không.
 */
export const isTruncatedOrPlaceholderAddress = (addr) => {
  if (!addr) return true;
  const s = String(addr).trim();
  if (isRawGpsPlaceholder(s)) return true;
  if (/^\s*(số\s*)?\d{1,5}[a-zA-Z]?\s*$/i.test(s)) return true;
  if (/^[A-Z0-9]{4,8}\+[A-Z0-9]{2,4}(\s*.*)?$/i.test(s)) return true;
  return false;
};

/**
 * Định dạng địa chỉ thông minh:
 * Nếu là chuỗi GPS thô, plus-code hoặc chỉ có số nhà ("131", "16"),
 * tự động phân giải tọa độ (lat, lng) sang tên đường/quận thực tế để hiển thị đúng và đủ.
 */
export const formatLocation = (lat, lng, addr) => {
  const s = addr ? String(addr).trim() : '';
  const isHouseNumberOnly = /^\s*(số\s*)?\d{1,5}[a-zA-Z]?\s*$/i.test(s);
  const isPlusCode = /^[A-Z0-9]{4,8}\+[A-Z0-9]{2,4}(\s*.*)?$/i.test(s);
  const isPlaceholder = isRawGpsPlaceholder(s);

  // Nếu địa chỉ đầy đủ hợp lệ (không phải số nhà lẻ, không phải plus code, không phải placeholder)
  if (s && !isHouseNumberOnly && !isPlusCode && !isPlaceholder && s.length >= 8) {
    return s;
  }

  // Phân giải tọa độ sang địa danh Hà Nội
  let areaName = '';
  if (lat !== undefined && lat !== null && lng !== undefined && lng !== null) {
    const key = `${Number(lat).toFixed(4)},${Number(lng).toFixed(4)}`;
    if (KNOWN_LOCATIONS[key]) {
      areaName = KNOWN_LOCATIONS[key];
    } else {
      const nLat = Number(lat);
      const nLng = Number(lng);
      if (nLat >= 20.8 && nLat <= 21.4 && nLng >= 105.6 && nLng <= 106.1) {
        if (nLat > 21.05) areaName = 'Tây Hồ, Hà Nội';
        else if (nLat > 21.03 && nLng >= 105.81) areaName = 'Ba Đình, Hà Nội';
        else if (nLng <= 105.78 && nLat >= 21.01) areaName = 'SVĐ QG Mỹ Đình, Quận Nam Từ Liêm, Hà Nội';
        else if (nLng < 105.805 && nLat >= 21.02) areaName = 'Cầu Giấy, Hà Nội';
        else if (nLng >= 105.815 && nLng <= 105.845 && nLat >= 21.005 && nLat <= 21.030) areaName = 'Hoàng Cầu, Đống Đa, Hà Nội';
        else if (nLat < 20.99 && nLng < 105.79) areaName = 'Hà Đông, Hà Nội';
        else if (nLat < 21.01 && nLng >= 105.84) areaName = 'Hai Bà Trưng, Hà Nội';
        else if (nLat >= 21.015 && nLat <= 21.04 && nLng >= 105.84) areaName = 'Hoàn Kiếm, Hà Nội';
        else areaName = 'Hà Nội';
      } else {
        areaName = `Tọa độ (${nLat.toFixed(4)}, ${nLng.toFixed(4)})`;
      }
    }
  }

  if (isHouseNumberOnly) {
    const num = s.replace(/^[^\d]*/, '').trim();
    return areaName ? `Số ${num}, ${areaName}` : `Số ${num}, Hà Nội`;
  }

  if (areaName) return areaName;
  return s && !isPlaceholder ? s : 'Chưa có địa chỉ chi tiết';
};

export const formatCoordinate = (lat, lng) => {
  if (lat === undefined || lat === null || lng === undefined || lng === null) return '—';
  return `${Number(lat).toFixed(4)}, ${Number(lng).toFixed(4)}`;
};
