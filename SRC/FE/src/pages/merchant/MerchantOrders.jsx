import React, { useState, useEffect, useRef } from 'react';
import { useOutletContext, useNavigate } from 'react-router-dom';
import {
  Box,
  Typography,
  Card,
  Grid,
  Chip,
  Button,
  IconButton,
  Dialog,
  DialogContent,
  Avatar,
  Divider,
  CircularProgress,
  Switch,
  FormControlLabel,
  TextField,
  DialogTitle,
  DialogActions,
  LinearProgress,
} from '@mui/material';
import {
  Close as CloseIcon,
  Phone as PhoneIcon,
  AccessTime as TimeIcon,
  Person as PersonIcon,
  ChevronRight as ChevronRightIcon,
  Check as CheckIcon,
  LocalShipping as DriverIcon,
  AccountBalanceWallet as WalletIcon,
  Restaurant as DishIcon,
  Refresh as RefreshIcon,
  ReceiptLong as ReceiptIcon,
  TrendingUp as TrendingUpIcon,
  MonetizationOn as MonetizationOnIcon,
  Fastfood as FastfoodIcon,
  CheckCircle as CheckCircleIcon,
  BarChart as BarChartIcon,
} from '@mui/icons-material';
import foodService from '../../services/foodService';
import toast from 'react-hot-toast';

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
  } catch (e) {
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
  const reason = order.cancelReason;
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
        toast('🔔 Có đơn hàng mới!', {
          icon: '🍲',
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
    fetchOrders(true);
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
  const completedOrders = orders.filter((o) => o.status === 'COMPLETED');
  const cancelledOrders = orders.filter((o) => o.status === 'CANCELLED' || o.status === 'REJECTED');
  const historyOrders = orders.filter(
    (o) => o.status === 'COMPLETED' || o.status === 'CANCELLED' || o.status === 'REJECTED'
  );

  // Business Performance Metrics
  const totalCompletedRevenue = completedOrders.reduce((sum, o) => {
    const foodPrice = (Number(o.totalPrice) || 0) - (Number(o.deliveryFee) || 0);
    return sum + (foodPrice > 0 ? foodPrice : (Number(o.totalPrice) || 0));
  }, 0);

  const totalFinishedOrders = completedOrders.length + cancelledOrders.length;
  const successRateNum = totalFinishedOrders > 0
    ? (completedOrders.length / totalFinishedOrders) * 100
    : (orders.length > 0 ? 100 : 0);
  const successRate = successRateNum.toFixed(1);

  const totalDishesSold = completedOrders.reduce((sum, o) => {
    const items = o.orderItems || o.items || [];
    if (items.length > 0) {
      return sum + items.reduce((iSum, item) => iSum + (Number(item.quantity) || 1), 0);
    }
    return sum;
  }, 0);

  const avgOrderValue = completedOrders.length > 0
    ? Math.round(totalCompletedRevenue / completedOrders.length)
    : 0;

  const activeOrdersCount = newOrders.length + cookingOrders.length + readyOrders.length;

  const formatPrice = (price) => {
    return (price || 0).toLocaleString('vi-VN') + 'đ';
  };

  const formatTime = (dateStr) => {
    if (!dateStr) return 'Vừa xong';
    const d = new Date(dateStr);
    return d.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
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
          <Typography sx={{ fontSize: 64, mb: 1.5 }}>🏪</Typography>
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
            ➕ Đăng ký Quán Ăn Mới
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
              🏪 Đăng Ký Quán Ăn Mới
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
              <TextField
                label="Địa chỉ chi tiết"
                required
                fullWidth
                value={regForm.address}
                onChange={(e) => setRegForm({ ...regForm, address: e.target.value })}
              />
              <Box sx={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 2 }}>
                <TextField
                  label="Vĩ độ (Latitude)"
                  fullWidth
                  value={regForm.latitude}
                  onChange={(e) => setRegForm({ ...regForm, latitude: e.target.value })}
                />
                <TextField
                  label="Kinh độ (Longitude)"
                  fullWidth
                  value={regForm.longitude}
                  onChange={(e) => setRegForm({ ...regForm, longitude: e.target.value })}
                />
              </Box>
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
                      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mt: 0.3 }}>
                        <Typography variant="caption" sx={{ color: '#64748B' }}>
                          {items.length || 1} món
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
                      <Typography variant="caption" sx={{ fontWeight: 800, color: '#0F172A', display: 'block', textAlign: 'right', mt: 0.2 }}>
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
          <DialogContent sx={{ p: { xs: 1.5, sm: 2.5 } }}>
            {/* Header Dialog */}
            <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }}>
              <Chip
                icon={<ReceiptIcon sx={{ fontSize: '16px !important', color: '#EA580C !important' }} />}
                label={`${getStatusLabel(selectedOrder.status)} #OF${selectedOrder.id}`}
                sx={{
                  bgcolor: '#FFF7ED',
                  color: '#EA580C',
                  fontWeight: 800,
                  fontSize: '0.85rem',
                  border: '1px solid #FED7AA',
                  height: 32,
                  px: 0.5,
                }}
              />
              <IconButton size="small" onClick={() => setSelectedOrder(null)} sx={{ color: '#94A3B8' }}>
                <CloseIcon fontSize="small" />
              </IconButton>
            </Box>

            {/* Customer Info Card */}
            <Box
              sx={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                p: 2,
                borderRadius: 3,
                bgcolor: '#F8FAFC',
                border: '1px solid #E2E8F0',
                mb: 2.5,
                flexWrap: 'wrap',
                gap: 1.5,
              }}
            >
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                <Avatar sx={{ width: 48, height: 48, bgcolor: '#E2E8F0', color: '#64748B' }}>
                  <PersonIcon sx={{ fontSize: 28 }} />
                </Avatar>
                <Box>
                  <Typography variant="subtitle1" sx={{ fontWeight: 800, color: '#0F172A', lineHeight: 1.2 }}>
                    {selectedOrder.customerName || `Khách hàng #${selectedOrder.customerId || selectedOrder.id}`}
                  </Typography>
                  <Typography variant="body2" sx={{ color: '#64748B', display: 'flex', alignItems: 'center', gap: 0.5, mt: 0.3, fontWeight: 600 }}>
                    <PhoneIcon sx={{ fontSize: 15, color: '#F97316' }} />
                    {selectedOrder.customerPhone || '0912 345 678'}
                  </Typography>
                </Box>
              </Box>

              <Box sx={{ textAlign: 'right' }}>
                {(() => {
                  const payInfo = getPaymentMethodInfo(selectedOrder.paymentMethod, selectedOrder.isPaid);
                  return (
                    <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'flex-end', gap: 0.8, mb: 0.5, flexWrap: 'wrap' }}>
                      <Chip
                        icon={<WalletIcon sx={{ fontSize: '14px !important', color: `${payInfo.color} !important` }} />}
                        label={payInfo.label}
                        size="small"
                        sx={{
                          bgcolor: payInfo.bgcolor,
                          color: payInfo.color,
                          fontWeight: 700,
                          fontSize: '0.75rem',
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
                    </Box>
                  );
                })()}
                <Typography variant="caption" sx={{ display: 'flex', alignItems: 'center', gap: 0.5, color: '#64748B', justifyContent: 'flex-end' }}>
                  <TimeIcon sx={{ fontSize: 14 }} /> Thời gian đặt: {formatTime(selectedOrder.createdAt)}
                </Typography>
              </Box>
            </Box>

            {/* Food Items List */}
            <Box sx={{ mb: 2.5 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 1.5 }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <DishIcon sx={{ fontSize: 18, color: '#0F172A' }} />
                  <Typography variant="subtitle2" sx={{ fontWeight: 800, color: '#0F172A' }}>
                    Danh sách món
                  </Typography>
                </Box>
                <Typography variant="caption" sx={{ color: '#64748B', fontWeight: 600 }}>
                  Tổng: {(selectedOrder.orderItems || selectedOrder.items || []).length} món
                </Typography>
              </Box>

              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
                {(selectedOrder.orderItems || selectedOrder.items || []).length === 0 ? (
                  <Typography variant="body2" sx={{ color: '#94A3B8', textAlign: 'center', py: 2 }}>
                    Chi tiết món ăn đang tải...
                  </Typography>
                ) : (
                  (selectedOrder.orderItems || selectedOrder.items || []).map((item, idx) => (
                    <Box
                      key={idx}
                      sx={{
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'space-between',
                        gap: 1.5,
                        pb: 1.5,
                        borderBottom: idx !== (selectedOrder.orderItems || selectedOrder.items || []).length - 1 ? '1px solid #F1F5F9' : 'none',
                      }}
                    >
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                        <Avatar
                          src={item.imageUrl || 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=100'}
                          variant="rounded"
                          sx={{ width: 50, height: 50, borderRadius: 2.5 }}
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

                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 3 }}>
                        <Typography variant="body2" sx={{ color: '#64748B', fontWeight: 700 }}>
                          x{item.quantity || 1}
                        </Typography>
                        <Typography variant="body2" sx={{ fontWeight: 800, color: '#0F172A', minWidth: 80, textAlign: 'right' }}>
                          {formatPrice((item.subtotal || (item.price * item.quantity)) || item.price)}
                        </Typography>
                      </Box>
                    </Box>
                  ))
                )}
              </Box>
            </Box>

            {/* Customer Note Card (Soft pink/orange background) */}
            {selectedOrder.note && selectedOrder.note.trim() && (
              <Box
                sx={{
                  p: 2,
                  borderRadius: 3,
                  bgcolor: '#FFF5F5',
                  border: '1px dashed #FECACA',
                  mb: 3,
                }}
              >
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 0.5 }}>
                  <Typography variant="caption" sx={{ fontWeight: 800, color: '#EF4444' }}>
                    🏷️ Ghi chú từ khách hàng
                  </Typography>
                </Box>
                <Typography variant="body2" sx={{ color: '#475569', fontStyle: 'italic', fontSize: '0.88rem' }}>
                  "{selectedOrder.note.trim()}"
                </Typography>
              </Box>
            )}

            {/* Cancellation info card if cancelled/rejected */}
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
                      ✓ Tiền thanh toán của khách đã được hệ thống xử lý hoàn tiền tự động.
                    </Typography>
                  )}
                </Box>
              );
            })()}

            {/* Warning banner when no driver found */}
            {selectedOrder.status === 'NO_DRIVER_FOUND' && (
              <Box sx={{ p: 1.8, borderRadius: 2.5, bgcolor: '#FFFBEB', border: '1px solid #FDE68A', mb: 2.5 }}>
                <Typography variant="body2" sx={{ color: '#B45309', fontWeight: 700 }}>
                  ⚠️ Chưa tìm thấy tài xế sau lượt tìm kiếm đầu tiên!
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
                    ✕ Từ chối đơn
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
                    ✓ Chấp nhận & Nấu
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
                    ✕ Hủy đơn
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
                    🛵 Nấu xong & Gọi tài xế
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
                    ✕ Hủy đơn
                  </Button>
                  <Button
                    variant="contained"
                    disabled={actionLoading}
                    onClick={() => handleUpdateStatus(selectedOrder.id, 'READY_FOR_PICKUP')}
                    sx={{ flex: 1.5, py: 1.3, borderRadius: 3, bgcolor: '#F97316', color: '#FFFFFF', fontWeight: 800, textTransform: 'none' }}
                  >
                    🔄 Quét tìm lại tài xế
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
            ⚠️ Chưa tìm thấy xế (Bấm để quét lại)
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
