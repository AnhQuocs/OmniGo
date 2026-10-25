
const CANCEL_REASON_MAP = {
  CUSTOMER_CHANGE_MIND: 'Khách đổi ý, không muốn đặt nữa',
  CUSTOMER_PAYMENT_FAILED: 'Thanh toán trực tuyến không thành công hoặc đã hủy giao dịch',
  CUSTOMER_WAIT_TOO_LONG: 'Thời gian chờ món quá lâu',
  CUSTOMER_WRONG_ORDER: 'Đặt nhầm món hoặc sai địa chỉ nhận',
  CUSTOMER_OTHER: 'Lý do khác từ khách hàng',
  RESTAURANT_OUT_OF_STOCK: 'Hết món / hết nguyên liệu',
  RESTAURANT_OVERLOADED: 'Quán đang quá tải, không kịp làm món',
  RESTAURANT_CLOSING: 'Quán sắp đến giờ đóng cửa',
  RESTAURANT_OTHER: 'Lý do khác từ quán',
  DRIVER_VEHICLE_BREAKDOWN: 'Sự cố phương tiện di chuyển',
  DRIVER_WEATHER_INCLEMENT: 'Thời tiết xấu / mưa ngập',
  DRIVER_CANNOT_CONTACT: 'Không thể liên lạc khách hàng hoặc quán',
  DRIVER_DISTANCE_TOO_FAR: 'Khoảng cách giao hàng quá xa',
  DRIVER_OTHER: 'Lý do khác từ tài xế',
  SYSTEM_NO_DRIVER_FOUND: 'Không tìm thấy tài xế sau nhiều lần quét',
  SYSTEM_TIMEOUT: 'Quá thời gian xử lý đơn hàng',
};

const formatCancelReason = (reason, reasonCode) => {
  if (reasonCode && CANCEL_REASON_MAP[reasonCode]) {
    return CANCEL_REASON_MAP[reasonCode];
  }
  if (reason && CANCEL_REASON_MAP[reason]) {
    return CANCEL_REASON_MAP[reason];
  }
  if (reason && typeof reason === 'string' && reason.trim()) {
    const trimmed = reason.trim();
    for (const [key, label] of Object.entries(CANCEL_REASON_MAP)) {
      if (trimmed === key) return label;
      if (trimmed.includes(key)) {
        return trimmed.replace(key, label);
      }
    }
    return trimmed;
  }
  return 'Không có lý do cụ thể';
};

import { useState, useEffect, useRef } from 'react';
import { useOutletContext, useNavigate } from 'react-router-dom';
import {
  Box,
  Typography,
  Card,
  Chip,
  Button,
  IconButton,
  Dialog,
  DialogContent,
  Avatar,
  CircularProgress,
  Divider,
  Switch,
  FormControlLabel,
  TextField,
  DialogTitle,
  DialogActions,
} from '@mui/material';
import {
  Close as CloseIcon,
  Phone as PhoneIcon,
  AccessTime as TimeIcon,
  Person as PersonIcon,
  ChevronRight as ChevronRightIcon,
  LocalShipping as DriverIcon,
  AccountBalanceWallet as WalletIcon,
  Restaurant as DishIcon,
  Refresh as RefreshIcon,
  ReceiptLong as ReceiptIcon,
  LocationOn as LocationIcon,
  BarChart as BarChartIcon,
} from '@mui/icons-material';
import foodService from '../../services/foodService';
import driverService from '../../services/driverService';
import toast from 'react-hot-toast';
import AddressAutocomplete from '../../components/common/AddressAutocomplete';

// Beep sound function using Web Audio API
const playOrderBeep = () => {
  try {
    const AudioContext = window.AudioContext || window.webkitAudioContext;
    if (!AudioContext) return;
    const ctx = new AudioContext();
    const osc = ctx.createOscillator();
    const gain = ctx.createGain();
    osc.connect(gain);
    gain.connect(ctx.destination);
    osc.type = 'sine';
    osc.frequency.setValueAtTime(880, ctx.currentTime);
    gain.gain.setValueAtTime(0.15, ctx.currentTime);
    osc.start();
    osc.stop(ctx.currentTime + 0.35);
  } catch {
    // Audio context may be restricted by browser policy before first interaction
  }
};

const getPaymentMethodInfo = (method, isPaid) => {
  const m = (method || 'CASH').toUpperCase();
  let label = 'Tiền mặt (COD)';
  let color = '#0284C7';
  let bgcolor = '#F0F9FF';
  let borderColor = '#BAE6FD';

  if (m === 'WALLET') {
    label = 'Ví OmniPay';
    color = '#7C3AED';
    bgcolor = '#F5F3FF';
    borderColor = '#DDD6FE';
  } else if (m === 'MOMO') {
    label = 'Ví MoMo';
    color = '#C026D3';
    bgcolor = '#FDF4FF';
    borderColor = '#F5D0FE';
  } else if (m === 'VNPAY') {
    label = 'Cổng VNPay';
    color = '#2563EB';
    bgcolor = '#EFF6FF';
    borderColor = '#BFDBFE';
  } else if (m === 'E-WALLET' || m === 'EWALLET') {
    label = 'Ví điện tử';
    color = '#EA580C';
    bgcolor = '#FFF7ED';
    borderColor = '#FED7AA';
  } else if (m !== 'CASH') {
    label = m;
  }

  return {
    label: `Thanh toán: ${label}`,
    rawMethod: label,
    color,
    bgcolor,
    borderColor,
    isPaid: Boolean(isPaid),
  };
};

const getStatusLabel = (status) => {
  switch (status) {
    case 'AWAITING_PAYMENT': return 'Chờ thanh toán';
    case 'PENDING': return 'Đơn mới';
    case 'ACCEPTED': return 'Đã nhận đơn';
    case 'PREPARING': return 'Đang nấu';
    case 'READY_FOR_PICKUP': return 'Đang tìm tài xế';
    case 'DELIVERING': return 'Đang giao hàng';
    case 'COMPLETED': return 'Hoàn tất';
    case 'CANCELLED': return 'Đã hủy';
    case 'REJECTED': return 'Từ chối';
    case 'NO_DRIVER_FOUND': return 'Chưa có tài xế';
    default: return status || 'Đơn hàng';
  }
};

const getCancelledInfo = (order) => {
  if (!order) return null;
  const by = order.cancelledBy;
  const reason = formatCancelReason(order.cancelReason, order.cancelReasonCode);
  let title = 'Đơn đã bị hủy';
  let badgeLabel = 'Đã hủy';
  let color = '#EF4444';
  let bgcolor = '#FEF2F2';
  let borderColor = '#FECACA';

  if (by === 'DRIVER') {
    title = 'Tài xế đã hủy đơn';
    badgeLabel = 'Tài xế hủy';
    color = '#DC2626';
    bgcolor = '#FFF1F2';
    borderColor = '#FECDD3';
  } else if (by === 'CUSTOMER') {
    title = 'Khách hàng đã hủy đơn';
    badgeLabel = 'Khách hủy';
    color = '#EA580C';
    bgcolor = '#FFF7ED';
    borderColor = '#FED7AA';
  } else if (by === 'RESTAURANT') {
    title = 'Quán đã hủy đơn';
    badgeLabel = 'Quán hủy';
    color = '#D97706';
    bgcolor = '#FFFBEB';
    borderColor = '#FDE68A';
  } else if (by === 'SYSTEM' || order.cancelReasonCode === 'SYSTEM_NO_DRIVER_FOUND') {
    title = 'Hệ thống tự động hủy';
    badgeLabel = 'Không tìm thấy xế (Đã hủy)';
    color = '#9333EA';
    bgcolor = '#FAF5FF';
    borderColor = '#E9D5FF';
  }

  return {
    title,
    badgeLabel,
    reason: reason || 'Không có lý do cụ thể',
    color,
    bgcolor,
    borderColor,
  };
};

export const MerchantOrders = () => {
  const { restaurant, loadingRes, refreshRestaurant } = useOutletContext();
  const navigate = useNavigate();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [autoRefresh, setAutoRefresh] = useState(() => {
    try {
      const saved = localStorage.getItem('merchant_auto_refresh');
      return saved !== null ? JSON.parse(saved) : true;
    } catch {
      return true;
    }
  });

  const handleToggleAutoRefresh = (checked) => {
    setAutoRefresh(checked);
    localStorage.setItem('merchant_auto_refresh', JSON.stringify(checked));
  };

  const [selectedOrder, setSelectedOrder] = useState(null);
  const [driverProfilesMap, setDriverProfilesMap] = useState({});

  useEffect(() => {
    let active = true;
    async function loadDrivers() {
      try {
        const res = await driverService.getAllDriverProfiles();
        const list = res?.data || (Array.isArray(res) ? res : []);
        const map = {};
        list.forEach((d) => {
          if (d.driverId) {
            map[d.driverId] = d;
          }
        });
        if (active) setDriverProfilesMap(map);
      } catch {
        // silent
      }
    }
    loadDrivers();
    return () => { active = false; };
  }, []);
  const [actionLoading, setActionLoading] = useState(false);

  // Cancellation modal state
  const [cancelDialog, setCancelDialog] = useState({
    open: false,
    orderId: null,
    targetStatus: null,
    reason: '',
    reasonCode: 'RESTAURANT_OUT_OF_STOCK',
  });

  // Registration modal state (when user has no restaurant yet)
  const [openRegister, setOpenRegister] = useState(false);
  const [registering, setRegistering] = useState(false);
  const [regForm, setRegForm] = useState({
    name: '',
    phone: '',
    address: '',
    latitude: '21.033333',
    longitude: '105.789123',
    openTime: '08:00',
    closeTime: '22:00',
    imageUrl: 'https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=500',
  });

  const handleRegister = async (e) => {
    e.preventDefault();
    if (!regForm.name || !regForm.address) {
      toast.error('Vui lòng nhập tên quán và địa chỉ');
      return;
    }
    try {
      setRegistering(true);
      await foodService.createRestaurant({
        ...regForm,
        latitude: parseFloat(regForm.latitude) || 21.033333,
        longitude: parseFloat(regForm.longitude) || 105.789123,
      });
      toast.success('Đăng ký quán ăn thành công!');
      setOpenRegister(false);
      if (refreshRestaurant) await refreshRestaurant();
    } catch (err) {
      toast.error(err.response?.data?.message || err.message || 'Đăng ký thất bại');
    } finally {
      setRegistering(false);
    }
  };

  const prevPendingCountRef = useRef(0);

  const fetchOrders = async (isManual = false) => {
    if (!restaurant?.id) return;
    try {
      if (isManual) setLoading(true);
      const res = await foodService.getRestaurantOrders(restaurant.id);
      const orderList = Array.isArray(res) ? res : res?.data || [];

      // Check if new pending orders arrived to beep
      const pendingCount = orderList.filter((o) => o.status === 'PENDING').length;
      if (pendingCount > prevPendingCountRef.current && prevPendingCountRef.current !== 0) {
        playOrderBeep();
        toast('Có đơn hàng mới!', {
          style: {
            borderRadius: '12px',
            background: '#F97316',
            color: '#fff',
            fontWeight: 700,
          },
        });
      }
      prevPendingCountRef.current = pendingCount;
      setOrders(orderList);
    } catch (err) {
      console.warn('Lỗi tải đơn hàng:', err.message);
    } finally {
      if (isManual) setLoading(false);
    }
  };

  useEffect(() => {
    let active = true;
    async function init() {
      if (!restaurant?.id) return;
      try {
        const orderList = await foodService.getMerchantOrders(restaurant.id);
        if (active) {
          setOrders(orderList);
        }
      } catch (err) {
        console.warn('Lỗi tải đơn hàng:', err.message);
      } finally {
        if (active) setLoading(false);
      }
    }
    init();
    return () => { active = false; };
  }, [restaurant?.id]);

  // Polling interval
  useEffect(() => {
    if (!autoRefresh || !restaurant?.id) return;
    const interval = setInterval(() => {
      fetchOrders(false);
    }, 5000);
    return () => clearInterval(interval);
  }, [autoRefresh, restaurant?.id]);

  // Handle order status update
  const handleUpdateStatus = async (orderId, newStatus, reason = null, reasonCode = null) => {
    try {
      setActionLoading(true);
      await foodService.updateOrderStatus(orderId, newStatus, reason, reasonCode);
      toast.success(
        newStatus === 'ACCEPTED'
          ? 'Đã nhận đơn và chuyển sang Đang nấu!'
          : newStatus === 'PREPARING'
          ? 'Bắt đầu nấu món!'
          : newStatus === 'READY_FOR_PICKUP'
          ? 'Đang tìm tài xế giao món!'
          : newStatus === 'CANCELLED' || newStatus === 'REJECTED'
          ? 'Đã từ chối / hủy đơn hàng.'
          : 'Đã cập nhật trạng thái đơn!'
      );
      setSelectedOrder(null);
      setCancelDialog({ open: false, orderId: null, targetStatus: null, reason: '', reasonCode: 'RESTAURANT_OUT_OF_STOCK' });
      await fetchOrders(false);
    } catch (err) {
      toast.error(err.response?.data?.message || err.message || 'Lỗi cập nhật đơn hàng');
    } finally {
      setActionLoading(false);
    }
  };

  // Group orders into 4 categories
  const newOrders = orders.filter((o) => o.status === 'PENDING');
  const cookingOrders = orders.filter((o) => o.status === 'ACCEPTED' || o.status === 'PREPARING');
  const readyOrders = orders.filter(
    (o) => o.status === 'READY_FOR_PICKUP' || o.status === 'DELIVERING' || o.status === 'NO_DRIVER_FOUND'
  );

  const historyOrders = orders.filter(
    (o) => o.status === 'COMPLETED' || o.status === 'CANCELLED' || o.status === 'REJECTED'
  );

  // Business Performance Metrics
  // (Metrics computed on demand)

  const formatPrice = (price) => {
    return (price || 0).toLocaleString('vi-VN') + 'đ';
  };

  const formatTime = (dateStr) => {
    if (!dateStr) return 'Vừa xong';
    const d = new Date(dateStr);
    return d.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
  };

  const formatFullDateTime = (dateStr) => {
    if (!dateStr) return 'Vừa xong';
    try {
      const d = new Date(dateStr);
      return `${d.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })} • ${d.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' })}`;
    } catch {
      return dateStr;
    }
  };

  // 1. Loading State
  if (loadingRes) {
    return (
      <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', py: 14 }}>
        <CircularProgress sx={{ color: '#F97316', mb: 2 }} />
        <Typography variant="body2" sx={{ color: '#64748B', fontWeight: 600 }}>
          Đang tải thông tin quán ăn...
        </Typography>
      </Box>
    );
  }

  // 2. No Restaurant Registered State
  if (!restaurant) {
    return (
      <Box sx={{ maxWidth: 640, mx: 'auto', py: { xs: 4, md: 8 }, px: 2 }}>
        <Card
          sx={{
            textAlign: 'center',
            p: { xs: 3.5, sm: 5 },
            borderRadius: 4,
            border: '1px solid #E2E8F0',
            boxShadow: '0 8px 24px rgba(0,0,0,0.04)',
          }}
        >
          <DishIcon sx={{ fontSize: 56, mb: 1.5, color: "#f97316" }} />
          <Typography variant="h5" sx={{ fontWeight: 800, color: '#0F172A', mb: 1 }}>
            Bạn chưa đăng ký Nhà Hàng
          </Typography>
          <Typography variant="body2" sx={{ color: '#64748B', maxWidth: 460, mx: 'auto', mb: 3.5, lineHeight: 1.6 }}>
            Đăng ký quán ăn của bạn ngay bây giờ để bắt đầu bán món ăn, nhận đơn trực tiếp và kết nối với mạng lưới tài xế giao hàng!
          </Typography>
          <Button
            variant="contained"
            onClick={() => setOpenRegister(true)}
            sx={{
              bgcolor: '#F97316',
              color: '#FFFFFF',
              fontWeight: 800,
              py: 1.4,
              px: 4,
              borderRadius: 3,
              fontSize: '1rem',
              textTransform: 'none',
              boxShadow: '0 4px 14px rgba(249, 115, 22, 0.3)',
              '&:hover': { bgcolor: '#EA580C' },
            }}
          >
            Đăng ký Quán Ăn Mới
          </Button>
        </Card>

        {/* Modal Đăng Ký Quán Ăn Mới */}
        <Dialog
          open={openRegister}
          onClose={() => !registering && setOpenRegister(false)}
          maxWidth="sm"
          fullWidth
          PaperProps={{ sx: { borderRadius: 4, p: 1 } }}
        >
          <form onSubmit={handleRegister}>
            <DialogTitle sx={{ fontWeight: 800, color: '#0F172A', pb: 1 }}>
              Đăng Ký Quán Ăn Mới
            </DialogTitle>
            <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: '10px !important' }}>
              <TextField
                label="Tên Quán Ăn"
                required
                fullWidth
                value={regForm.name}
                onChange={(e) => setRegForm({ ...regForm, name: e.target.value })}
              />
              <TextField
                label="Số điện thoại hotline quán"
                fullWidth
                value={regForm.phone}
                onChange={(e) => setRegForm({ ...regForm, phone: e.target.value })}
              />
              <AddressAutocomplete
                label="Địa chỉ chi tiết quán"
                required
                value={regForm.address}
                latitude={regForm.latitude}
                longitude={regForm.longitude}
                onChangeAddress={(newAddr) =>
                  setRegForm((prev) => ({ ...prev, address: newAddr }))
                }
                onSelectLocation={({ address, latitude, longitude }) => {
                  setRegForm((prev) => ({
                    ...prev,
                    address,
                    latitude: String(latitude),
                    longitude: String(longitude),
                  }));
                }}
                onChangeCoordinates={({ latitude, longitude }) => {
                  setRegForm((prev) => ({
                    ...prev,
                    latitude: String(latitude),
                    longitude: String(longitude),
                  }));
                }}
              />
              <Box sx={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 2 }}>
                <TextField
                  label="Giờ mở cửa"
                  fullWidth
                  value={regForm.openTime}
                  onChange={(e) => setRegForm({ ...regForm, openTime: e.target.value })}
                />
                <TextField
                  label="Giờ đóng cửa"
                  fullWidth
                  value={regForm.closeTime}
                  onChange={(e) => setRegForm({ ...regForm, closeTime: e.target.value })}
                />
              </Box>
              <TextField
                label="Ảnh đại diện quán (URL)"
                fullWidth
                value={regForm.imageUrl}
                onChange={(e) => setRegForm({ ...regForm, imageUrl: e.target.value })}
              />
            </DialogContent>
            <DialogActions sx={{ px: 3, pb: 2 }}>
              <Button onClick={() => setOpenRegister(false)} disabled={registering} sx={{ textTransform: 'none', fontWeight: 600 }}>
                Hủy
              </Button>
              <Button
                type="submit"
                variant="contained"
                disabled={registering}
                sx={{
                  bgcolor: '#F97316',
                  color: '#FFFFFF',
                  fontWeight: 700,
                  textTransform: 'none',
                  px: 3,
                  '&:hover': { bgcolor: '#EA580C' },
                }}
              >
                {registering ? <CircularProgress size={20} color="inherit" /> : 'Hoàn tất Đăng Ký'}
              </Button>
            </DialogActions>
          </form>
        </Dialog>
      </Box>
    );
  }

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5 }}>
      {/* HEADER ROW */}
      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: 1.5 }}>
        <Box>
          <Typography variant="h5" sx={{ fontWeight: 800, color: '#0F172A', mb: 0.3 }}>
            Đơn hàng
          </Typography>
          <Typography variant="body2" sx={{ color: '#64748B' }}>
            Quản lý đơn hàng, theo dõi trạng thái và phục vụ khách hàng tốt hơn
          </Typography>
        </Box>

        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, flexWrap: 'wrap' }}>
          <Button
            variant="outlined"
            size="small"
            startIcon={<BarChartIcon />}
            onClick={() => navigate('/merchant/analytics')}
            sx={{
              borderRadius: 2.5,
              textTransform: 'none',
              fontWeight: 700,
              borderColor: '#FED7AA',
              bgcolor: '#FFF7ED',
              color: '#C2410C',
              '&:hover': { bgcolor: '#FFEDD5', borderColor: '#FDBA74' },
            }}
          >
            Báo cáo doanh thu
          </Button>

          <FormControlLabel
            control={
              <Switch
                checked={autoRefresh}
                onChange={(e) => handleToggleAutoRefresh(e.target.checked)}
                color="primary"
                size="small"
              />
            }
            label={<Typography variant="caption" sx={{ color: '#64748B', fontWeight: 600 }}>Tự động làm mới (5s)</Typography>}
          />
          <Button
            variant="outlined"
            size="small"
            startIcon={loading ? <CircularProgress size={16} /> : <RefreshIcon />}
            onClick={() => fetchOrders(true)}
            sx={{ borderRadius: 2.5, textTransform: 'none', fontWeight: 700, borderColor: '#E2E8F0', color: '#475569' }}
          >
            Làm mới
          </Button>
        </Box>
      </Box>

      {/* 4 COLUMNS KANBAN LAYOUT (Responsive horizontal scroll on small/split screens) */}
      <Box
        sx={{
          display: 'grid',
          gridTemplateColumns: {
            xs: 'repeat(4, minmax(280px, 1fr))',
            md: 'repeat(3, minmax(260px, 1fr)) 280px',
            lg: 'repeat(3, 1fr) 300px',
          },
          overflowX: 'auto',
          pb: 2,
          gap: 2,
          alignItems: 'start',
          '&::-webkit-scrollbar': { height: 8 },
          '&::-webkit-scrollbar-thumb': { bgcolor: '#CBD5E1', borderRadius: 4 },
        }}
      >
        {/* COLUMN 1: ĐƠN MỚI */}
        <Box
          sx={{
            bgcolor: '#F8FAFC',
            borderRadius: 3.5,
            border: '1px solid #E2E8F0',
            p: 1.5,
            display: 'flex',
            flexDirection: 'column',
            gap: 1.5,
          }}
        >
          {/* Column Header */}
          <Box
            sx={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              p: 1.2,
              borderRadius: 2.5,
              bgcolor: '#EFF6FF',
            }}
          >
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <Box sx={{ width: 10, height: 10, borderRadius: '50%', bgcolor: '#3B82F6' }} />
              <Typography variant="body2" sx={{ fontWeight: 800, color: '#1D4ED8' }}>
                Đơn mới
              </Typography>
            </Box>
            <Chip
              label={newOrders.length}
              size="small"
              sx={{ bgcolor: '#3B82F6', color: '#FFFFFF', fontWeight: 800, height: 22, minWidth: 22, fontSize: '0.75rem' }}
            />
          </Box>

          {/* Cards List */}
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5, minHeight: 180 }}>
            {newOrders.length === 0 ? (
              <Box sx={{ textAlign: 'center', py: 4, color: '#94A3B8' }}>
                <ReceiptIcon sx={{ fontSize: 36, opacity: 0.4, mb: 0.5 }} />
                <Typography variant="caption" display="block">Chưa có đơn mới nào</Typography>
              </Box>
            ) : (
              newOrders.map((order) => (
                <OrderKanbanCard
                  key={order.id}
                  order={order}
                  accentColor="#3B82F6"
                  formatTime={formatTime}
                  formatPrice={formatPrice}
                  onClick={() => setSelectedOrder(order)}
                />
              ))
            )}
          </Box>
        </Box>

        {/* COLUMN 2: ĐANG NẤU */}
        <Box
          sx={{
            bgcolor: '#F8FAFC',
            borderRadius: 3.5,
            border: '1px solid #E2E8F0',
            p: 1.5,
            display: 'flex',
            flexDirection: 'column',
            gap: 1.5,
          }}
        >
          {/* Column Header */}
          <Box
            sx={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              p: 1.2,
              borderRadius: 2.5,
              bgcolor: '#FEF3C7',
            }}
          >
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <Box sx={{ width: 10, height: 10, borderRadius: '50%', bgcolor: '#F59E0B' }} />
              <Typography variant="body2" sx={{ fontWeight: 800, color: '#B45309' }}>
                Đang nấu
              </Typography>
            </Box>
            <Chip
              label={cookingOrders.length}
              size="small"
              sx={{ bgcolor: '#F59E0B', color: '#FFFFFF', fontWeight: 800, height: 22, minWidth: 22, fontSize: '0.75rem' }}
            />
          </Box>

          {/* Cards List */}
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5, minHeight: 180 }}>
            {cookingOrders.length === 0 ? (
              <Box sx={{ textAlign: 'center', py: 4, color: '#94A3B8' }}>
                <DishIcon sx={{ fontSize: 36, opacity: 0.4, mb: 0.5 }} />
                <Typography variant="caption" display="block">Bếp đang trống</Typography>
              </Box>
            ) : (
              cookingOrders.map((order) => (
                <OrderKanbanCard
                  key={order.id}
                  order={order}
                  accentColor="#F59E0B"
                  formatTime={formatTime}
                  formatPrice={formatPrice}
                  onClick={() => setSelectedOrder(order)}
                />
              ))
            )}
          </Box>
        </Box>

        {/* COLUMN 3: SẴN SÀNG GIAO */}
        <Box
          sx={{
            bgcolor: '#F8FAFC',
            borderRadius: 3.5,
            border: '1px solid #E2E8F0',
            p: 1.5,
            display: 'flex',
            flexDirection: 'column',
            gap: 1.5,
          }}
        >
          {/* Column Header */}
          <Box
            sx={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              p: 1.2,
              borderRadius: 2.5,
              bgcolor: '#ECFDF5',
            }}
          >
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <Box sx={{ width: 10, height: 10, borderRadius: '50%', bgcolor: '#10B981' }} />
              <Typography variant="body2" sx={{ fontWeight: 800, color: '#047857' }}>
                Sẵn sàng giao
              </Typography>
            </Box>
            <Chip
              label={readyOrders.length}
              size="small"
              sx={{ bgcolor: '#10B981', color: '#FFFFFF', fontWeight: 800, height: 22, minWidth: 22, fontSize: '0.75rem' }}
            />
          </Box>

          {/* Cards List */}
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5, minHeight: 180 }}>
            {readyOrders.length === 0 ? (
              <Box sx={{ textAlign: 'center', py: 4, color: '#94A3B8' }}>
                <DriverIcon sx={{ fontSize: 36, opacity: 0.4, mb: 0.5 }} />
                <Typography variant="caption" display="block">Chưa có đơn chờ giao</Typography>
              </Box>
            ) : (
              readyOrders.map((order) => (
                <OrderKanbanCard
                  key={order.id}
                  order={order}
                  accentColor="#10B981"
                  formatTime={formatTime}
                  formatPrice={formatPrice}
                  onClick={() => setSelectedOrder(order)}
                />
              ))
            )}
          </Box>
        </Box>

        {/* COLUMN 4: LỊCH SỬ ĐƠN HÀNG (RIGHT SIDEBAR) */}
        <Box
          sx={{
            bgcolor: '#FFFFFF',
            borderRadius: 3.5,
            border: '1px solid #E2E8F0',
            p: 1.5,
            display: 'flex',
            flexDirection: 'column',
            gap: 1.5,
          }}
        >
          {/* Header */}
          <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', pb: 1, borderBottom: '1px solid #F1F5F9' }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <TimeIcon sx={{ fontSize: 18, color: '#64748B' }} />
              <Typography variant="body2" sx={{ fontWeight: 800, color: '#0F172A' }}>
                Lịch sử đơn hàng
              </Typography>
            </Box>
            <ChevronRightIcon sx={{ fontSize: 18, color: '#94A3B8' }} />
          </Box>

          {/* List of past orders */}
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.2, maxHeight: 600, overflowY: 'auto', pr: 0.5 }}>
            {historyOrders.length === 0 ? (
              <Typography variant="caption" sx={{ color: '#94A3B8', textAlign: 'center', py: 3 }}>
                Chưa có lịch sử đơn
              </Typography>
            ) : (
              historyOrders.slice(0, 15).map((order) => {
                const isSuccess = order.status === 'COMPLETED';
                const items = order.orderItems || order.items || [];
                const firstImg = items[0]?.imageUrl || 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=100';
                return (
                  <Box
                    key={order.id}
                    onClick={() => setSelectedOrder(order)}
                    sx={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: 1.2,
                      p: 1,
                      borderRadius: 2.5,
                      cursor: 'pointer',
                      border: '1px solid #F1F5F9',
                      transition: 'all 0.15s',
                      '&:hover': { bgcolor: '#F8FAFC', borderColor: '#E2E8F0' },
                    }}
                  >
                    <Avatar src={firstImg} variant="rounded" sx={{ width: 44, height: 44, borderRadius: 2 }} />
                    <Box sx={{ flex: 1, minWidth: 0 }}>
                      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <Typography variant="caption" sx={{ fontWeight: 800, color: '#0F172A' }}>
                          #OF{order.id}
                        </Typography>
                        <Typography variant="caption" sx={{ color: '#94A3B8', fontSize: '0.7rem' }}>
                          {formatTime(order.createdAt)}
                        </Typography>
                      </Box>
                      <Typography variant="caption" sx={{ fontWeight: 700, color: '#1E293B', display: 'block', mt: 0.2, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                        {order.customerName || `Khách #${order.customerId || order.id}`}
                      </Typography>
                      {(() => {
                        const dProfile = order.driverId ? driverProfilesMap[order.driverId] : null;
                        const dPlate = dProfile?.licensePlate || order.driverLicensePlate || '29B1-688.88';
                        const dName = dProfile?.fullName || order.driverName || `Tài xế #${order.driverId}`;
                        return (
                          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mt: 0.2 }}>
                            <Typography variant="caption" sx={{ color: '#64748B', fontSize: '0.72rem', display: 'flex', alignItems: 'center', gap: 0.5 }}>
                              {order.driverId ? `${dName} (${dPlate})` : 'Chưa có xế'} • {items.length || 1} món
                            </Typography>
                            <Chip
                              label={isSuccess ? 'Hoàn thành' : 'Đã hủy'}
                              size="small"
                              sx={{
                                height: 18,
                                fontSize: '0.65rem',
                                fontWeight: 700,
                                bgcolor: isSuccess ? '#ECFDF5' : '#FEF2F2',
                                color: isSuccess ? '#047857' : '#EF4444',
                              }}
                            />
                          </Box>
                        );
                      })()}
                      <Typography variant="caption" sx={{ fontWeight: 800, color: '#EA580C', display: 'block', textAlign: 'right', mt: 0.2 }}>
                        {formatPrice(order.totalPrice || order.totalAmount)}
                      </Typography>
                    </Box>
                  </Box>
                );
              })
            )}
          </Box>
        </Box>
      </Box>

      {/* POPUP / MODAL CHI TIẾT ĐƠN HÀNG (Y HỆT ẢNH MẪU) */}
      <Dialog
        open={Boolean(selectedOrder)}
        onClose={() => setSelectedOrder(null)}
        maxWidth="sm"
        fullWidth
        PaperProps={{
          sx: {
            borderRadius: 4,
            p: 1.5,
            boxShadow: '0 20px 40px rgba(0,0,0,0.12)',
          },
        }}
      >
        {selectedOrder && (
          <DialogContent sx={{ p: { xs: 2, sm: 3 } }}>
            {/* Header Dialog */}
            <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2.2 }}>
              <Chip
                icon={<ReceiptIcon sx={{ fontSize: '16px !important', color: '#EA580C !important' }} />}
                label={`${getStatusLabel(selectedOrder.status)} #OF${selectedOrder.id}`}
                sx={{
                  bgcolor: '#FFF7ED',
                  color: '#EA580C',
                  fontWeight: 800,
                  fontSize: '0.88rem',
                  border: '1px solid #FED7AA',
                  height: 32,
                  px: 0.5,
                }}
              />
              <IconButton size="small" onClick={() => setSelectedOrder(null)} sx={{ color: '#94A3B8' }}>
                <CloseIcon fontSize="small" />
              </IconButton>
            </Box>

            {/* Section 1: Thông tin khách hàng & Điểm giao hàng */}
            <Box sx={{ p: 2, borderRadius: 3, bgcolor: '#F8FAFC', border: '1px solid #E2E8F0', mb: 2 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 1.2, flexWrap: 'wrap', gap: 1 }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <PersonIcon sx={{ fontSize: 18, color: '#0F172A' }} />
                  <Typography variant="subtitle2" sx={{ fontWeight: 800, color: '#0F172A' }}>
                    Thông tin khách hàng & Điểm giao
                  </Typography>
                </Box>
                <Typography variant="caption" sx={{ color: '#64748B', display: 'flex', alignItems: 'center', gap: 0.5 }}>
                  <TimeIcon sx={{ fontSize: 13 }} /> Đặt lúc: {formatFullDateTime(selectedOrder.createdAt)}
                </Typography>
              </Box>

              {/* Tên khách hàng, SĐT và Trạng thái thanh toán */}
              <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: 1.2, p: 1.2, borderRadius: 2, bgcolor: '#FFFFFF', border: '1px solid #F1F5F9', mb: 1 }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.2 }}>
                  <Avatar sx={{ width: 40, height: 40, bgcolor: '#F1F5F9', color: '#0F172A', fontWeight: 800, fontSize: '0.95rem' }}>
                    {(selectedOrder.customerName || 'K').charAt(0).toUpperCase()}
                  </Avatar>
                  <Box>
                    <Typography variant="body2" sx={{ fontWeight: 800, color: '#0F172A' }}>
                      {selectedOrder.customerName || `Khách hàng #${selectedOrder.customerId || selectedOrder.id}`}
                    </Typography>
                    <Typography variant="caption" sx={{ color: '#64748B', display: 'flex', alignItems: 'center', gap: 0.5, fontWeight: 600 }}>
                      <PhoneIcon sx={{ fontSize: 13, color: '#F97316' }} />
                      {selectedOrder.customerPhone || 'Chưa cập nhật SĐT'}
                    </Typography>
                  </Box>
                </Box>

                <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.8, flexWrap: 'wrap' }}>
                  {(() => {
                    const payInfo = getPaymentMethodInfo(selectedOrder.paymentMethod, selectedOrder.isPaid);
                    return (
                      <>
                        <Chip
                          icon={<WalletIcon sx={{ fontSize: '13px !important', color: `${payInfo.color} !important` }} />}
                          label={payInfo.label}
                          size="small"
                          sx={{
                            bgcolor: payInfo.bgcolor,
                            color: payInfo.color,
                            fontWeight: 700,
                            fontSize: '0.72rem',
                            border: `1px solid ${payInfo.borderColor}`,
                            height: 24,
                          }}
                        />
                        <Chip
                          label={selectedOrder.isPaid ? 'Đã thanh toán' : 'Chưa thanh toán'}
                          size="small"
                          sx={{
                            bgcolor: selectedOrder.isPaid ? '#ECFDF5' : '#FFFBEB',
                            color: selectedOrder.isPaid ? '#047857' : '#B45309',
                            border: selectedOrder.isPaid ? '1px solid #A7F3D0' : '1px solid #FDE68A',
                            fontWeight: 700,
                            fontSize: '0.72rem',
                            height: 24,
                          }}
                        />
                      </>
                    );
                  })()}
                </Box>
              </Box>

              {/* Địa chỉ giao hàng chi tiết */}
              <Box sx={{ p: 1.2, borderRadius: 2, bgcolor: '#FFFFFF', border: '1px solid #F1F5F9' }}>
                <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 1 }}>
                  <LocationIcon sx={{ fontSize: 17, color: '#008cff', mt: 0.2 }} />
                  <Box sx={{ flex: 1 }}>
                    <Typography variant="caption" sx={{ color: '#64748B', fontWeight: 700, display: 'block' }}>
                      Địa chỉ nhận món:
                    </Typography>
                    <Typography variant="body2" sx={{ fontWeight: 600, color: '#0F172A', wordBreak: 'break-word', mt: 0.2, lineHeight: 1.4 }}>
                      {selectedOrder.deliveryAddress || 'Khách chưa nhập địa chỉ chi tiết'}
                    </Typography>
                  </Box>
                </Box>
              </Box>

              {/* Ghi chú từ khách hàng nếu có */}
              {selectedOrder.note && selectedOrder.note.trim() && (
                <Box sx={{ mt: 1, p: 1.2, borderRadius: 2, bgcolor: '#FFFBEB', border: '1px dashed #FDE68A' }}>
                  <Typography variant="caption" sx={{ fontWeight: 700, color: '#B45309', display: 'block' }}>
                    Ghi chú từ khách hàng:
                  </Typography>
                  <Typography variant="body2" sx={{ color: '#78350F', fontStyle: 'italic', mt: 0.2, fontSize: '0.85rem' }}>
                    "{selectedOrder.note.trim()}"
                  </Typography>
                </Box>
              )}
            </Box>

            {/* Section 2: Thông tin Tài xế giao hàng (Kèm Avatar & Biển số xe) */}
            <Box sx={{ p: 2, borderRadius: 3, bgcolor: '#F8FAFC', border: '1px solid #E2E8F0', mb: 2 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 1.5 }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <DriverIcon sx={{ fontSize: 18, color: '#0F172A' }} />
                  <Typography variant="subtitle2" sx={{ fontWeight: 800, color: '#0F172A' }}>
                    Thông tin tài xế giao hàng
                  </Typography>
                </Box>
                {selectedOrder.driverId ? (
                  <Chip
                    label={selectedOrder.status === 'COMPLETED' ? 'Đã giao thành công' : selectedOrder.status === 'DELIVERING' ? 'Đang trên đường giao' : 'Đã tiếp nhận đơn'}
                    size="small"
                    sx={{
                      bgcolor: selectedOrder.status === 'COMPLETED' ? '#ECFDF5' : '#EFF6FF',
                      color: selectedOrder.status === 'COMPLETED' ? '#047857' : '#1D4ED8',
                      border: selectedOrder.status === 'COMPLETED' ? '1px solid #A7F3D0' : '1px solid #BFDBFE',
                      fontWeight: 700,
                      fontSize: '0.72rem',
                      height: 22,
                    }}
                  />
                ) : (
                  <Chip
                    label={selectedOrder.status === 'CANCELLED' || selectedOrder.status === 'REJECTED' ? 'Không có tài xế' : 'Đang quét tìm tài xế'}
                    size="small"
                    sx={{
                      bgcolor: '#FFFBEB',
                      color: '#B45309',
                      border: '1px solid #FDE68A',
                      fontWeight: 700,
                      fontSize: '0.72rem',
                      height: 22,
                    }}
                  />
                )}
              </Box>

              {selectedOrder.driverId ? (() => {
                const driver = driverProfilesMap[selectedOrder.driverId];
                const driverName = driver?.fullName || selectedOrder.driverName || `Tài xế đối tác #${selectedOrder.driverId}`;
                const driverPhone = driver?.phoneNumber || selectedOrder.driverPhone || '0988 123 456';
                const driverAvatar = driver?.avatarUrl || selectedOrder.driverAvatarUrl || '';
                const licensePlate = driver?.licensePlate || selectedOrder.driverLicensePlate || '29B1-688.88';
                const vehicleModel = driver?.vehicleModel || 'Honda Wave Alpha (Đội xe OmniGo)';

                return (
                  <Box sx={{ p: 1.5, borderRadius: 2, bgcolor: '#FFFFFF', border: '1px solid #F1F5F9' }}>
                    <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: 1.5 }}>
                      {/* Driver Avatar, Tên và Số điện thoại */}
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                        <Avatar
                          src={driverAvatar}
                          alt={driverName}
                          sx={{
                            width: 50,
                            height: 50,
                            borderRadius: 2.5,
                            bgcolor: '#E0F2FE',
                            color: '#0284C7',
                            border: '1.5px solid #BAE6FD',
                          }}
                        >
                          <DriverIcon sx={{ fontSize: 28 }} />
                        </Avatar>
                        <Box>
                          <Typography variant="body2" sx={{ fontWeight: 800, color: '#0F172A' }}>
                            {driverName}
                          </Typography>
                          <Typography variant="caption" sx={{ color: '#64748B', display: 'flex', alignItems: 'center', gap: 0.5, fontWeight: 600, mt: 0.3 }}>
                            <PhoneIcon sx={{ fontSize: 13, color: '#0284C7' }} />
                            {driverPhone}
                          </Typography>
                        </Box>
                      </Box>

                      {/* Khối Biển số xe & Mẫu phương tiện */}
                      <Box sx={{ textAlign: { xs: 'left', sm: 'right' } }}>
                        <Box
                          sx={{
                            display: 'inline-flex',
                            alignItems: 'center',
                            gap: 0.8,
                            bgcolor: '#FEF3C7',
                            border: '1.5px solid #F59E0B',
                            px: 1.4,
                            py: 0.5,
                            borderRadius: 2,
                            boxShadow: '0 1px 3px rgba(245, 158, 11, 0.15)',
                          }}
                        >
                          <Typography
                            variant="caption"
                            sx={{
                              fontWeight: 800,
                              color: '#92400E',
                              fontSize: '0.85rem',
                              letterSpacing: '0.8px',
                              fontFamily: 'monospace',
                            }}
                          >
                            {licensePlate}
                          </Typography>
                        </Box>
                        <Typography variant="caption" sx={{ color: '#64748B', display: 'block', mt: 0.4, fontWeight: 600 }}>
                          {vehicleModel}
                        </Typography>
                      </Box>
                    </Box>
                  </Box>
                );
              })() : (
                <Box sx={{ p: 1.2, borderRadius: 2, bgcolor: '#FFFFFF', border: '1px solid #F1F5F9' }}>
                  <Typography variant="body2" sx={{ color: '#64748B', fontStyle: 'italic', fontSize: '0.85rem' }}>
                    {selectedOrder.status === 'CANCELLED' || selectedOrder.status === 'REJECTED'
                      ? 'Đơn hàng đã đóng, không có tài xế tiếp nhận cuốc giao này.'
                      : 'Hệ thống đang quét tìm kiếm tài xế giao hàng rảnh quanh vị trí quán...'}
                  </Typography>
                </Box>
              )}
            </Box>

            {/* Section 3: Danh sách món ăn */}
            <Box sx={{ mb: 2 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 1.2 }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <DishIcon sx={{ fontSize: 18, color: '#0F172A' }} />
                  <Typography variant="subtitle2" sx={{ fontWeight: 800, color: '#0F172A' }}>
                    Danh sách món đã đặt
                  </Typography>
                </Box>
                <Typography variant="caption" sx={{ color: '#64748B', fontWeight: 600 }}>
                  Tổng: {(selectedOrder.orderItems || selectedOrder.items || []).length} món
                </Typography>
              </Box>

              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
                {(selectedOrder.orderItems || selectedOrder.items || []).map((item, idx) => (
                  <Box
                    key={idx}
                    sx={{
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'space-between',
                      gap: 1.5,
                      p: 1.2,
                      borderRadius: 2,
                      bgcolor: '#FFFFFF',
                      border: '1px solid #F1F5F9',
                    }}
                  >
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.2 }}>
                      <Avatar
                        src={item.imageUrl || 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=100'}
                        variant="rounded"
                        sx={{ width: 44, height: 44, borderRadius: 2 }}
                      />
                      <Box>
                        <Typography variant="body2" sx={{ fontWeight: 800, color: '#0F172A' }}>
                          {item.itemName || item.dishName || 'Món ăn'}
                        </Typography>
                        {(item.notes || item.note) && (
                          <Typography variant="caption" sx={{ color: '#94A3B8', display: 'block' }}>
                            {item.notes || item.note}
                          </Typography>
                        )}
                      </Box>
                    </Box>

                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 2.5 }}>
                      <Typography variant="body2" sx={{ color: '#64748B', fontWeight: 700 }}>
                        x{item.quantity || 1}
                      </Typography>
                      <Typography variant="body2" sx={{ fontWeight: 800, color: '#0F172A', minWidth: 75, textAlign: 'right' }}>
                        {formatPrice((item.subtotal || (item.price * item.quantity)) || item.price)}
                      </Typography>
                    </Box>
                  </Box>
                ))}
              </Box>
            </Box>

            {/* Section 4: Bảng kê chi phí & Thời gian */}
            <Box sx={{ p: 2, borderRadius: 3, bgcolor: '#F8FAFC', border: '1px solid #E2E8F0', mb: 2.5 }}>
              {(() => {
                const items = selectedOrder.orderItems || selectedOrder.items || [];
                const foodSubtotal = items.reduce((sum, it) => sum + (Number(it.subtotal) || (Number(it.price) * (Number(it.quantity) || 1)) || 0), 0);
                const dFee = Number(selectedOrder.deliveryFee) || 0;
                const total = Number(selectedOrder.totalPrice) || Number(selectedOrder.totalAmount) || (foodSubtotal + dFee);

                return (
                  <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.8 }}>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <Typography variant="caption" sx={{ color: '#64748B', fontWeight: 600 }}>
                        Tiền món (Tạm tính quán nhận):
                      </Typography>
                      <Typography variant="body2" sx={{ fontWeight: 700, color: '#0F172A' }}>
                        {formatPrice(foodSubtotal > 0 ? foodSubtotal : total - dFee)}
                      </Typography>
                    </Box>

                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <Typography variant="caption" sx={{ color: '#64748B', fontWeight: 600 }}>
                        Phí giao hàng (Tài xế nhận):
                      </Typography>
                      <Typography variant="body2" sx={{ fontWeight: 700, color: '#0F172A' }}>
                        {formatPrice(dFee)}
                      </Typography>
                    </Box>

                    <Divider sx={{ my: 0.5 }} />

                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <Typography variant="subtitle2" sx={{ fontWeight: 800, color: '#0F172A' }}>
                        Tổng tiền khách thanh toán:
                      </Typography>
                      <Typography variant="h6" sx={{ fontWeight: 800, color: '#EA580C', fontSize: '1.15rem' }}>
                        {formatPrice(total)}
                      </Typography>
                    </Box>
                  </Box>
                );
              })()}
            </Box>

            {(selectedOrder.status === 'CANCELLED' || selectedOrder.status === 'REJECTED') && (() => {
              const cancelInfo = getCancelledInfo(selectedOrder);
              if (!cancelInfo) return null;
              return (
                <Box sx={{ p: 2, borderRadius: 3, bgcolor: cancelInfo.bgcolor, border: `1px solid ${cancelInfo.borderColor}`, mb: 2.5 }}>
                  <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 0.5 }}>
                    <Typography variant="subtitle2" sx={{ fontWeight: 800, color: cancelInfo.color }}>
                      {cancelInfo.title}
                    </Typography>
                    <Chip label={cancelInfo.badgeLabel} size="small" sx={{ bgcolor: cancelInfo.color, color: '#fff', fontWeight: 700, height: 22, fontSize: '0.7rem' }} />
                  </Box>
                  <Typography variant="body2" sx={{ color: '#334155', fontWeight: 600, fontSize: '0.85rem' }}>
                    Lý do: <span style={{ color: cancelInfo.color, fontStyle: 'italic' }}>"{cancelInfo.reason}"</span>
                  </Typography>
                  {selectedOrder.isPaid && (
                    <Typography variant="caption" sx={{ color: '#059669', fontWeight: 700, display: 'block', mt: 0.5 }}>
                      Tiền thanh toán của khách đã được hệ thống xử lý hoàn tiền tự động.
                    </Typography>
                  )}
                </Box>
              );
            })()}

            {/* Warning banner when no driver found */}
            {selectedOrder.status === 'NO_DRIVER_FOUND' && (
              <Box sx={{ p: 1.8, borderRadius: 2.5, bgcolor: '#FFFBEB', border: '1px solid #FDE68A', mb: 2.5 }}>
                <Typography variant="body2" sx={{ color: '#B45309', fontWeight: 700 }}>
                  Chưa tìm thấy tài xế sau lượt tìm kiếm đầu tiên!
                </Typography>
                <Typography variant="caption" sx={{ color: '#92400E', display: 'block', mt: 0.3 }}>
                  Bạn có thể bấm <b>"Quét tìm lại tài xế"</b> để hệ thống mở rộng phạm vi tìm kiếm một lần nữa. Nếu vẫn không có tài xế nhận, đơn sẽ tự động chuyển sang trạng thái Hủy để bảo đảm quyền lợi khách hàng.
                </Typography>
              </Box>
            )}

            {/* Action Buttons */}
            <Box sx={{ display: 'flex', gap: 1.5, justifyContent: 'flex-end' }}>
              {selectedOrder.status === 'PENDING' && (
                <>
                  <Button
                    variant="outlined"
                    disabled={actionLoading}
                    onClick={() => setCancelDialog({
                      open: true,
                      orderId: selectedOrder.id,
                      targetStatus: 'REJECTED',
                      reason: 'Hết món / hết nguyên liệu',
                      reasonCode: 'RESTAURANT_OUT_OF_STOCK',
                    })}
                    sx={{
                      flex: 1,
                      py: 1.3,
                      borderRadius: 3,
                      borderColor: '#EF4444',
                      color: '#EF4444',
                      fontWeight: 800,
                      textTransform: 'none',
                      fontSize: '0.95rem',
                      '&:hover': { bgcolor: '#FEF2F2', borderColor: '#DC2626' },
                    }}
                  >
                    Từ chối đơn
                  </Button>
                  <Button
                    variant="contained"
                    disabled={actionLoading}
                    onClick={() => handleUpdateStatus(selectedOrder.id, 'ACCEPTED')}
                    sx={{
                      flex: 1.2,
                      py: 1.3,
                      borderRadius: 3,
                      bgcolor: '#F97316',
                      color: '#FFFFFF',
                      fontWeight: 800,
                      textTransform: 'none',
                      fontSize: '0.95rem',
                      boxShadow: '0 4px 12px rgba(249, 115, 22, 0.3)',
                      '&:hover': { bgcolor: '#EA580C' },
                    }}
                  >
                    Chấp nhận & Nấu
                  </Button>
                </>
              )}

              {(selectedOrder.status === 'ACCEPTED' || selectedOrder.status === 'PREPARING') && (
                <>
                  <Button
                    variant="outlined"
                    disabled={actionLoading}
                    onClick={() => setCancelDialog({
                      open: true,
                      orderId: selectedOrder.id,
                      targetStatus: 'CANCELLED',
                      reason: 'Hết món / hết nguyên liệu',
                      reasonCode: 'RESTAURANT_OUT_OF_STOCK',
                    })}
                    sx={{
                      flex: 1,
                      py: 1.3,
                      borderRadius: 3,
                      borderColor: '#EF4444',
                      color: '#EF4444',
                      fontWeight: 800,
                      textTransform: 'none',
                    }}
                  >
                    Hủy đơn
                  </Button>
                  <Button
                    variant="contained"
                    disabled={actionLoading}
                    onClick={() => handleUpdateStatus(selectedOrder.id, 'READY_FOR_PICKUP')}
                    sx={{
                      flex: 1.5,
                      py: 1.3,
                      borderRadius: 3,
                      bgcolor: '#10B981',
                      color: '#FFFFFF',
                      fontWeight: 800,
                      textTransform: 'none',
                      '&:hover': { bgcolor: '#059669' },
                    }}
                  >
                    Nấu xong & Gọi tài xế
                  </Button>
                </>
              )}

              {(selectedOrder.status === 'READY_FOR_PICKUP' || selectedOrder.status === 'NO_DRIVER_FOUND') && (
                <>
                  <Button
                    variant="outlined"
                    disabled={actionLoading}
                    onClick={() => setCancelDialog({
                      open: true,
                      orderId: selectedOrder.id,
                      targetStatus: 'CANCELLED',
                      reason: 'Hết món / hết nguyên liệu',
                      reasonCode: 'RESTAURANT_OUT_OF_STOCK',
                    })}
                    sx={{ flex: 1, py: 1.3, borderRadius: 3, borderColor: '#EF4444', color: '#EF4444', fontWeight: 800, textTransform: 'none' }}
                  >
                    Hủy đơn
                  </Button>
                  <Button
                    variant="contained"
                    disabled={actionLoading}
                    onClick={() => handleUpdateStatus(selectedOrder.id, 'READY_FOR_PICKUP')}
                    sx={{ flex: 1.5, py: 1.3, borderRadius: 3, bgcolor: '#F97316', color: '#FFFFFF', fontWeight: 800, textTransform: 'none' }}
                  >
                    Quét tìm lại tài xế
                  </Button>
                </>
              )}

              {(selectedOrder.status === 'COMPLETED' || selectedOrder.status === 'CANCELLED' || selectedOrder.status === 'REJECTED') && (
                <Button
                  variant="outlined"
                  onClick={() => setSelectedOrder(null)}
                  sx={{ width: '100%', py: 1.2, borderRadius: 3, fontWeight: 700, textTransform: 'none' }}
                >
                  Đóng
                </Button>
              )}
            </Box>
          </DialogContent>
        )}
      </Dialog>

      {/* Cancellation / Rejection Dialog with Reasons */}
      <Dialog
        open={cancelDialog.open}
        onClose={() => setCancelDialog({ open: false, orderId: null, targetStatus: null, reason: '', reasonCode: 'RESTAURANT_OUT_OF_STOCK' })}
        maxWidth="xs"
        fullWidth
        PaperProps={{ sx: { borderRadius: 3.5, p: 1 } }}
      >
        <DialogTitle sx={{ fontWeight: 800, color: '#DC2626', pb: 1 }}>
          {cancelDialog.targetStatus === 'REJECTED' ? 'Từ chối nhận đơn hàng' : 'Hủy đơn hàng'}
        </DialogTitle>
        <DialogContent sx={{ pt: 1 }}>
          <Typography variant="body2" sx={{ color: '#64748B', mb: 2 }}>
            Vui lòng chọn lý do để thông báo đến khách hàng:
          </Typography>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1, mb: 2 }}>
            {[
              { code: 'RESTAURANT_OUT_OF_STOCK', text: 'Hết món / hết nguyên liệu' },
              { code: 'RESTAURANT_OVERLOADED', text: 'Quán đang quá tải, không kịp làm món' },
              { code: 'RESTAURANT_CLOSING', text: 'Quán sắp đến giờ đóng cửa' },
              { code: 'RESTAURANT_OTHER', text: 'Lý do khác' },
            ].map((r) => (
              <Button
                key={r.code}
                variant={cancelDialog.reasonCode === r.code ? 'contained' : 'outlined'}
                size="small"
                onClick={() => setCancelDialog((prev) => ({ ...prev, reasonCode: r.code, reason: r.text }))}
                sx={{
                  justifyContent: 'flex-start',
                  textTransform: 'none',
                  fontWeight: 700,
                  borderRadius: 2,
                  py: 1,
                  bgcolor: cancelDialog.reasonCode === r.code ? '#EF4444' : 'transparent',
                  borderColor: cancelDialog.reasonCode === r.code ? '#EF4444' : '#CBD5E1',
                  color: cancelDialog.reasonCode === r.code ? '#FFFFFF' : '#334155',
                  '&:hover': { bgcolor: cancelDialog.reasonCode === r.code ? '#DC2626' : '#F1F5F9' },
                }}
              >
                {r.text}
              </Button>
            ))}
          </Box>
          <TextField
            label="Chi tiết lý do (tùy chọn)"
            fullWidth
            size="small"
            multiline
            rows={2}
            value={cancelDialog.reason}
            onChange={(e) => setCancelDialog((prev) => ({ ...prev, reason: e.target.value }))}
            placeholder="Nhập lý do chi tiết..."
          />
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 2 }}>
          <Button
            onClick={() => setCancelDialog({ open: false, orderId: null, targetStatus: null, reason: '', reasonCode: 'RESTAURANT_OUT_OF_STOCK' })}
            sx={{ color: '#64748B', fontWeight: 700, textTransform: 'none' }}
          >
            Bỏ qua
          </Button>
          <Button
            variant="contained"
            disabled={actionLoading}
            onClick={() => handleUpdateStatus(cancelDialog.orderId, cancelDialog.targetStatus, cancelDialog.reason || 'Quán hủy đơn', cancelDialog.reasonCode)}
            sx={{ bgcolor: '#EF4444', color: '#FFFFFF', fontWeight: 800, textTransform: 'none', '&:hover': { bgcolor: '#DC2626' } }}
          >
            {actionLoading ? <CircularProgress size={20} color="inherit" /> : 'Xác nhận hủy'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

// Reusable Order Card for Kanban Columns
const OrderKanbanCard = ({ order, accentColor, formatTime, formatPrice, onClick }) => {
  const items = order.orderItems || order.items || [];
  const firstItemImg = items[0]?.imageUrl || 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=100';
  const totalPrice = order.totalPrice || order.totalAmount || 0;

  return (
    <Card
      onClick={onClick}
      sx={{
        p: 1.8,
        borderRadius: 3,
        bgcolor: '#FFFFFF',
        border: '1px solid #E2E8F0',
        cursor: 'pointer',
        boxShadow: '0 2px 6px rgba(0,0,0,0.03)',
        transition: 'all 0.2s ease',
        '&:hover': {
          transform: 'translateY(-2px)',
          boxShadow: '0 6px 16px rgba(0,0,0,0.06)',
          borderColor: accentColor,
        },
      }}
    >
      {/* Top row: Order ID & Time */}
      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 1 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <Typography variant="body2" sx={{ fontWeight: 800, color: '#0F172A', fontSize: '0.9rem' }}>
            #OF{order.id}
          </Typography>
          <Typography variant="caption" sx={{ color: '#94A3B8', fontSize: '0.75rem' }}>
            {formatTime(order.createdAt)}
          </Typography>
        </Box>
        <ChevronRightIcon sx={{ fontSize: 18, color: '#CBD5E1' }} />
      </Box>

      {/* Customer row */}
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.8, mb: 1.2 }}>
        <PersonIcon sx={{ fontSize: 16, color: '#64748B' }} />
        <Typography variant="body2" sx={{ color: '#334155', fontWeight: 600, fontSize: '0.85rem' }}>
          {order.customerName || `Khách hàng #${order.customerId || order.id}`}
        </Typography>
      </Box>

      {/* Payment tag */}
      {(() => {
        const payInfo = getPaymentMethodInfo(order.paymentMethod, order.isPaid);
        return (
          <Box sx={{ mb: 1.5, display: 'flex', alignItems: 'center', gap: 0.8, flexWrap: 'wrap' }}>
            <Chip
              label={payInfo.label}
              size="small"
              sx={{
                bgcolor: payInfo.bgcolor,
                color: payInfo.color,
                fontWeight: 700,
                fontSize: '0.7rem',
                border: `1px solid ${payInfo.borderColor}`,
                height: 22,
              }}
            />
            <Chip
              label={order.isPaid ? 'Đã TT' : 'Chưa TT'}
              size="small"
              sx={{
                bgcolor: order.isPaid ? '#ECFDF5' : '#FFFBEB',
                color: order.isPaid ? '#047857' : '#B45309',
                border: order.isPaid ? '1px solid #A7F3D0' : '1px solid #FDE68A',
                fontWeight: 700,
                fontSize: '0.65rem',
                height: 20,
              }}
            />
          </Box>
        );
      })()}

      {/* Cancellation info in card */}
      {(order.status === 'CANCELLED' || order.status === 'REJECTED') && (() => {
        const cancelInfo = getCancelledInfo(order);
        if (!cancelInfo) return null;
        return (
          <Box sx={{ mb: 1.2, p: 1, borderRadius: 2, bgcolor: cancelInfo.bgcolor, border: `1px dashed ${cancelInfo.borderColor}` }}>
            <Typography variant="caption" sx={{ color: cancelInfo.color, fontWeight: 800, display: 'block', fontSize: '0.72rem' }}>
              {cancelInfo.badgeLabel}
            </Typography>
            <Typography variant="caption" sx={{ color: '#475569', fontSize: '0.7rem', display: 'block', textOverflow: 'ellipsis', overflow: 'hidden', whiteSpace: 'nowrap' }}>
              {cancelInfo.reason}
            </Typography>
          </Box>
        );
      })()}

      {/* No driver found warning in card */}
      {order.status === 'NO_DRIVER_FOUND' && (
        <Box sx={{ mb: 1.2, p: 0.8, borderRadius: 2, bgcolor: '#FEF3C7', border: '1px solid #FCD34D' }}>
          <Typography variant="caption" sx={{ color: '#B45309', fontWeight: 800, display: 'block', fontSize: '0.72rem' }}>
            Chưa tìm thấy xế (Bấm để quét lại)
          </Typography>
        </Box>
      )}

      {/* Food items & Price row */}
      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', pt: 1, borderTop: '1px solid #F1F5F9' }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <Avatar
            src={firstItemImg}
            variant="rounded"
            sx={{ width: 34, height: 34, borderRadius: 1.8 }}
          />
          <Typography variant="caption" sx={{ color: '#64748B', fontWeight: 600 }}>
            {items.length || 1} món
          </Typography>
        </Box>

        <Typography variant="body2" sx={{ fontWeight: 800, color: '#0F172A', fontSize: '0.95rem' }}>
          {formatPrice(totalPrice)}
        </Typography>
      </Box>
    </Card>
  );
};

export default MerchantOrders;
