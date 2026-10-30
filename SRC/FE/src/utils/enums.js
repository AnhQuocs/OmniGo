/**
 * Ánh xạ các Enum hệ thống sang ngôn ngữ Tiếng Việt thân thiện.
 */

export const TRANSACTION_TYPE_MAP = {
  COMMISSION_FEE: 'Phí sàn (20%)',
  DEPOSIT: 'Nạp tiền ví',
  TRIP_PAYMENT: 'Thanh toán cuốc xe',
  TRIP_INCOME: 'Thu nhập cuốc xe',
  FOOD_PAYMENT: 'Thanh toán đơn món',
  FOOD_DELIVERY_INCOME: 'Thu nhập giao món',
  WITHDRAWAL: 'Rút tiền ví',
  REFUND: 'Hoàn tiền',
};

export const PAYMENT_METHOD_MAP = {
  WALLET: 'Ví OmniGo',
  VNPAY: 'Cổng VNPAY',
  MOMO: 'Ví MoMo',
  CASH: 'Tiền mặt',
  DIRECT: 'Trực tiếp',
};

export const VEHICLE_TYPE_MAP = {
  BIKE: 'Xe máy',
  CAR_4: 'Ô tô 4 chỗ',
  CAR_4_SEAT: 'Ô tô 4 chỗ',
  CAR_7: 'Ô tô 7 chỗ',
  CAR_7_SEAT: 'Ô tô 7 chỗ',
  EXPRESS: 'Giao hàng Siêu tốc',
};

export const getTransactionTypeText = (type) => {
  if (!type) return 'Giao dịch';
  return TRANSACTION_TYPE_MAP[type.toUpperCase()] || type;
};

export const getPaymentMethodText = (method) => {
  if (!method) return 'Tiền mặt';
  return PAYMENT_METHOD_MAP[method.toUpperCase()] || method;
};

export const getVehicleTypeText = (vType) => {
  if (!vType) return 'Xe máy';
  return VEHICLE_TYPE_MAP[vType.toUpperCase()] || vType;
};

