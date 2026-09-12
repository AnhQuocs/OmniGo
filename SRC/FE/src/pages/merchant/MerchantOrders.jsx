import React, { useState, useEffect, useRef } from 'react';
import { useOutletContext } from 'react-router-dom';
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

export const MerchantOrders = () => {
  const { restaurant, loadingRes, refreshRestaurant } = useOutletContext();
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
  const handleUpdateStatus = async (orderId, newStatus) => {
    try {
      setActionLoading(true);
      await foodService.updateOrderStatus(orderId, newStatus);
      toast.success(
        newStatus === 'ACCEPTED'
          ? 'Đã nhận đơn và chuyển sang Đang nấu!'
          : newStatus === 'PREPARING'
          ? 'Bắt đầu nấu món!'
          : newStatus === 'READY_FOR_PICKUP'
          ? 'Đã xong món & đang tìm tài xế giao!'
          : newStatus === 'CANCELLED' || newStatus === 'REJECTED'
          ? 'Đã từ chối / hủy đơn hàng.'
          : 'Đã cập nhật trạng thái đơn!'
      );
      setSelectedOrder(null);
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

        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
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
                label={`Đơn mới #OF${selectedOrder.id}`}
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
                <Chip
                  icon={<WalletIcon sx={{ fontSize: '14px !important', color: '#EA580C !important' }} />}
                  label="Thanh toán: E-wallet"
                  size="small"
                  sx={{
                    bgcolor: '#FFF7ED',
                    color: '#EA580C',
                    fontWeight: 700,
                    fontSize: '0.75rem',
                    border: '1px solid #FED7AA',
                    height: 24,
                    mb: 0.5,
                  }}
                />
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
                          <Typography variant="caption" sx={{ color: '#94A3B8', display: 'block' }}>
                            {item.notes || 'Không hành, ít mỡ'}
                          </Typography>
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
                "{selectedOrder.note || 'Vui lòng làm món ít mỡ, không hành. Cảm ơn shop!'}"
              </Typography>
            </Box>

            {/* Action Buttons */}
            <Box sx={{ display: 'flex', gap: 1.5, justifyContent: 'flex-end' }}>
              {selectedOrder.status === 'PENDING' && (
                <>
                  <Button
                    variant="outlined"
                    disabled={actionLoading}
                    onClick={() => handleUpdateStatus(selectedOrder.id, 'REJECTED')}
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
                    onClick={() => handleUpdateStatus(selectedOrder.id, 'CANCELLED')}
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
                    onClick={() => handleUpdateStatus(selectedOrder.id, 'CANCELLED')}
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
      <Box sx={{ mb: 1.5 }}>
        <Chip
          label="Thanh toán: E-wallet"
          size="small"
          sx={{
            bgcolor: '#EFF6FF',
            color: '#2563EB',
            fontWeight: 700,
            fontSize: '0.7rem',
            height: 22,
          }}
        />
      </Box>

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
