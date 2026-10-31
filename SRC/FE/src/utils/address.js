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
  if (!s || s === '—' || s === 'n/a') return true;
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
 * Định dạng địa chỉ thông minh:
 * Nếu là chuỗi GPS thô (ví dụ: "Vị trí GPS hiện tại của tôi", "Vị trí GPS của bạn..."),
 * tự động phân giải tọa độ (lat, lng) sang tên đường/quận thực tế.
 */
export const formatLocation = (lat, lng, addr) => {
  if (addr && !isRawGpsPlaceholder(addr)) {
    return String(addr).trim();
  }
  if (lat === undefined || lat === null || lng === undefined || lng === null) {
    return addr && String(addr).trim() && !isRawGpsPlaceholder(addr) ? String(addr).trim() : 'Chưa có địa chỉ chi tiết';
  }
  const key = `${Number(lat).toFixed(4)},${Number(lng).toFixed(4)}`;
  if (KNOWN_LOCATIONS[key]) return KNOWN_LOCATIONS[key];
  const nLat = Number(lat);
  const nLng = Number(lng);
  if (nLat >= 20.9 && nLat <= 21.2 && nLng >= 105.7 && nLng <= 106.0) {
    if (nLat > 21.05) return 'Tây Hồ, Hà Nội';
    if (nLat > 21.03) return 'Ba Đình, Hà Nội';
    if (nLng < 105.80) return 'Cầu Giấy, Hà Nội';
    if (nLng >= 105.815 && nLng <= 105.845 && nLat >= 21.005 && nLat <= 21.030) {
      return 'Hoàng Cầu, Đống Đa, Hà Nội';
    }
    if (nLat < 21.01) return 'Hai Bà Trưng, Hà Nội';
    return 'Hoàn Kiếm, Hà Nội';
  }
  return `Tọa độ (${nLat.toFixed(4)}, ${nLng.toFixed(4)})`;
};

export const formatCoordinate = (lat, lng) => {
  if (lat === undefined || lat === null || lng === undefined || lng === null) return '—';
  return `${Number(lat).toFixed(4)}, ${Number(lng).toFixed(4)}`;
};

