import React, { useState, useEffect } from 'react';
import { useOutletContext, useNavigate } from 'react-router-dom';
import {
  Box,
  Typography,
  Card,
  Chip,
  Button,
  Avatar,
  CircularProgress,
  LinearProgress,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Tooltip,
} from '@mui/material';
import {
  TrendingUp as TrendingUpIcon,
  MonetizationOn as MonetizationOnIcon,
  Fastfood as FastfoodIcon,
  ReceiptLong as ReceiptIcon,
  Cancel as CancelIcon,
  AccountBalanceWallet as WalletIcon,
  Refresh as RefreshIcon,
  InfoOutlined as InfoIcon,
  ArrowForward as ArrowForwardIcon,
  Store as StoreIcon,
} from '@mui/icons-material';
import foodService from '../../services/foodService';
import toast from 'react-hot-toast';

export const MerchantAnalytics = () => {
  const { restaurant, loadingRes } = useOutletContext();
  const navigate = useNavigate();

  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [timeFilter, setTimeFilter] = useState('ALL'); // 'ALL' | 'TODAY' | 'WEEK'

  const fetchOrders = async (showLoading = false) => {
    if (!restaurant?.id) return;
    if (showLoading) setLoading(true);
    try {
      const res = await foodService.getRestaurantOrders(restaurant.id);
      // foodService.getRestaurantOrders returns array directly or inside data
      const orderList = Array.isArray(res) ? res : (Array.isArray(res?.data) ? res.data : []);
      setOrders(orderList);
    } catch (err) {
      console.error('Lỗi khi tải dữ liệu thống kê:', err);
      toast.error('Không thể tải dữ liệu đơn hàng');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (restaurant?.id) {
      fetchOrders(true);
    }
  }, [restaurant?.id]);

  // Filter orders by time period
  const filteredOrders = orders.filter((o) => {
    if (timeFilter === 'ALL') return true;
    if (!o.createdAt) return true;
    const orderDate = new Date(o.createdAt);
    const now = new Date();
    if (timeFilter === 'TODAY') {
      return orderDate.toDateString() === now.toDateString();
    }
    if (timeFilter === 'WEEK') {
      const oneWeekAgo = new Date();
      oneWeekAgo.setDate(now.getDate() - 7);
      return orderDate >= oneWeekAgo;
    }
    return true;
  });

  // Calculate Key Business Metrics
  const completedOrders = filteredOrders.filter((o) => o.status === 'COMPLETED');
  const cancelledOrders = filteredOrders.filter((o) => o.status === 'CANCELLED' || o.status === 'REJECTED');
  const inProgressOrders = filteredOrders.filter(
    (o) => o.status === 'PENDING' || o.status === 'ACCEPTED' || o.status === 'PREPARING' || o.status === 'READY_FOR_PICKUP' || o.status === 'DELIVERING'
  );

  // 1. Food Revenue from Completed Orders (Excludes delivery fee)
  const totalCompletedRevenue = completedOrders.reduce((sum, o) => {
    const foodPrice = (Number(o.totalPrice) || 0) - (Number(o.deliveryFee) || 0);
    return sum + (foodPrice > 0 ? foodPrice : (Number(o.totalPrice) || 0));
  }, 0);

  // 2. Success Rate (%)
  const totalFinishedOrders = completedOrders.length + cancelledOrders.length;
  const successRateNum = totalFinishedOrders > 0
    ? (completedOrders.length / totalFinishedOrders) * 100
    : (filteredOrders.length > 0 ? 100 : 0);
  const successRate = successRateNum.toFixed(1);

  // 3. Total Dishes/Items Sold
  const totalDishesSold = completedOrders.reduce((sum, o) => {
    const items = o.orderItems || o.items || [];
    if (items.length > 0) {
      return sum + items.reduce((iSum, item) => iSum + (Number(item.quantity) || 1), 0);
    }
    return sum;
  }, 0);

  // 4. Average Order Value (AOV - Giá trị trung bình mỗi đơn)
  const avgOrderValue = completedOrders.length > 0
    ? Math.round(totalCompletedRevenue / completedOrders.length)
    : 0;

  // Best Sellers (Top món ăn bán chạy nhất từ các đơn hoàn thành)
  const itemSalesMap = {};
  completedOrders.forEach((o) => {
    const items = o.orderItems || o.items || [];
    items.forEach((it) => {
      const name = it.itemName || it.dishName || `Món #${it.menuItemId || it.id}`;
      const qty = Number(it.quantity) || 1;
      const price = Number(it.price) || 0;
      if (!itemSalesMap[name]) {
        itemSalesMap[name] = { name, quantity: 0, revenue: 0 };
      }
      itemSalesMap[name].quantity += qty;
      itemSalesMap[name].revenue += (it.subtotal ? Number(it.subtotal) : (price * qty));
    });
  });

  const bestSellers = Object.values(itemSalesMap)
    .sort((a, b) => b.quantity - a.quantity)
    .slice(0, 5);

  // Payment Breakdown
  const paymentBreakdown = {
    CASH: { count: 0, amount: 0, label: 'Tiền mặt (COD)', color: '#0284C7', bgcolor: '#F0F9FF' },
    WALLET: { count: 0, amount: 0, label: 'Ví OmniPay', color: '#7C3AED', bgcolor: '#F5F3FF' },
    VNPAY: { count: 0, amount: 0, label: 'Cổng VNPAY', color: '#0284C7', bgcolor: '#EFF6FF' },
    MOMO: { count: 0, amount: 0, label: 'Ví MoMo', color: '#DB2777', bgcolor: '#FDF2F8' },
  };

  completedOrders.forEach((o) => {
    const m = (o.paymentMethod || 'CASH').toUpperCase();
    const foodPrice = Math.max(0, (Number(o.totalPrice) || 0) - (Number(o.deliveryFee) || 0));
    if (paymentBreakdown[m]) {
      paymentBreakdown[m].count++;
      paymentBreakdown[m].amount += foodPrice;
    } else {
      paymentBreakdown.CASH.count++;
      paymentBreakdown.CASH.amount += foodPrice;
    }
  });

  // Cancellation Breakdown
  const cancelStats = {
    RESTAURANT: { count: 0, label: 'Quán hủy / Hết món' },
    CUSTOMER: { count: 0, label: 'Khách hàng hủy' },
    DRIVER: { count: 0, label: 'Tài xế hủy' },
    SYSTEM: { count: 0, label: 'Không tìm thấy tài xế / Quá hạn' },
  };

  cancelledOrders.forEach((o) => {
    const by = (o.cancelledBy || '').toUpperCase();
    if (by === 'RESTAURANT') cancelStats.RESTAURANT.count++;
    else if (by === 'CUSTOMER') cancelStats.CUSTOMER.count++;
    else if (by === 'DRIVER') cancelStats.DRIVER.count++;
    else cancelStats.SYSTEM.count++;
  });

  const formatPrice = (price) => {
    return (price || 0).toLocaleString('vi-VN') + 'đ';
  };

  if (loadingRes) {
    return (
      <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', py: 14 }}>
        <CircularProgress sx={{ color: '#F97316', mb: 2 }} />
        <Typography variant="body2" sx={{ color: '#64748B', fontWeight: 600 }}>
          Đang tải dữ liệu quán...
        </Typography>
      </Box>
    );
  }

  if (!restaurant) {
    return (
      <Box sx={{ maxWidth: 640, mx: 'auto', py: 8, px: 2, textAlign: 'center' }}>
        <Card sx={{ p: 4, borderRadius: 4, border: '1px solid #E2E8F0' }}>
          <StoreIcon sx={{ fontSize: 48, color: '#F97316', mb: 1.5 }} />
          <Typography variant="h6" sx={{ fontWeight: 800, color: '#0F172A', mb: 1 }}>
            Chưa có nhà hàng
          </Typography>
          <Typography variant="body2" sx={{ color: '#64748B', mb: 3 }}>
            Vui lòng đăng ký quán ăn để bắt đầu theo dõi số liệu và doanh thu bán hàng.
          </Typography>
          <Button variant="contained" onClick={() => navigate('/merchant/settings')} sx={{ bgcolor: '#F97316' }}>
            Đăng ký quán ngay
          </Button>
        </Card>
      </Box>
    );
  }

  return (
    <Box sx={{ p: { xs: 2, md: 3.5 }, maxWidth: 1440, mx: 'auto' }}>
      {/* HEADER SECTION */}
      <Box
        sx={{
          display: 'flex',
          flexDirection: { xs: 'column', sm: 'row' },
          alignItems: { xs: 'flex-start', sm: 'center' },
          justifyContent: 'space-between',
          gap: 2,
          mb: 3,
        }}
      >
        <Box>
          <Typography variant="h5" sx={{ fontWeight: 800, color: '#0F172A', mb: 0.5 }}>
            Báo Cáo & Thống Kê Doanh Thu
          </Typography>
          <Typography variant="body2" sx={{ color: '#64748B' }}>
            Tổng quan hiệu suất bán hàng của <b>{restaurant.name}</b>
          </Typography>
        </Box>

        {/* Time Period Filter & Refresh */}
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, flexWrap: 'wrap' }}>
          <Box sx={{ display: 'flex', bgcolor: '#F1F5F9', p: 0.5, borderRadius: 2.5 }}>
            {[
              { key: 'ALL', label: 'Tất cả' },
              { key: 'TODAY', label: 'Hôm nay' },
              { key: 'WEEK', label: '7 ngày qua' },
            ].map((tab) => (
              <Button
                key={tab.key}
                size="small"
                onClick={() => setTimeFilter(tab.key)}
                sx={{
                  borderRadius: 2,
                  textTransform: 'none',
                  fontWeight: 700,
                  fontSize: '0.82rem',
                  px: 1.8,
                  py: 0.5,
                  bgcolor: timeFilter === tab.key ? '#FFFFFF' : 'transparent',
                  color: timeFilter === tab.key ? '#0F172A' : '#64748B',
                  boxShadow: timeFilter === tab.key ? '0 1px 3px rgba(0,0,0,0.1)' : 'none',
                  '&:hover': { bgcolor: timeFilter === tab.key ? '#FFFFFF' : '#E2E8F0' },
                }}
              >
                {tab.label}
              </Button>
            ))}
          </Box>

          <Button
            variant="outlined"
            size="small"
            startIcon={loading ? <CircularProgress size={16} /> : <RefreshIcon />}
            onClick={() => fetchOrders(true)}
            sx={{
              borderRadius: 2.5,
              textTransform: 'none',
              fontWeight: 700,
              borderColor: '#E2E8F0',
              color: '#475569',
              bgcolor: '#FFFFFF',
            }}
          >
            Làm mới
          </Button>

          <Button
            variant="contained"
            size="small"
            endIcon={<ArrowForwardIcon />}
            onClick={() => navigate('/merchant/orders')}
            sx={{
              borderRadius: 2.5,
              textTransform: 'none',
              fontWeight: 700,
              bgcolor: '#F97316',
              '&:hover': { bgcolor: '#EA580C' },
            }}
          >
            Quản lý đơn hàng
          </Button>
        </Box>
      </Box>

      {/* 4 CORE KPI HERO CARDS (Responsive CSS Grid, zero attribute warnings) */}
      <Box
        sx={{
          display: 'grid',
          gridTemplateColumns: {
            xs: '1fr',
            sm: 'repeat(2, 1fr)',
            lg: 'repeat(4, 1fr)',
          },
          gap: 2.5,
          mb: 3.5,
        }}
      >
        {/* Metric 1: Revenue from Completed Orders */}
        <Card
          sx={{
            p: 2.5,
            borderRadius: 3.5,
            bgcolor: '#FFFFFF',
            border: '1px solid #BBF7D0',
            boxShadow: '0 4px 12px rgba(16, 185, 129, 0.08)',
            height: '100%',
            display: 'flex',
            flexDirection: 'column',
            justifyContent: 'space-between',
          }}
        >
          <Box>
            <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 1.5 }}>
              <Typography variant="caption" sx={{ fontWeight: 800, color: '#15803D', textTransform: 'uppercase', letterSpacing: 0.5 }}>
                Doanh Thu Hoàn Tất
              </Typography>
              <Avatar sx={{ bgcolor: '#DCFCE7', color: '#16A34A', width: 40, height: 40 }}>
                <MonetizationOnIcon />
              </Avatar>
            </Box>
            <Typography variant="h4" sx={{ fontWeight: 800, color: '#14532D', mb: 0.8 }}>
              {formatPrice(totalCompletedRevenue)}
            </Typography>
          </Box>
          <Box sx={{ pt: 1.5, borderTop: '1px dashed #DCFCE7' }}>
            <Typography variant="caption" sx={{ color: '#166534', fontWeight: 600, display: 'block' }}>
              Thực nhận từ <b>{completedOrders.length}</b> đơn hoàn thành
            </Typography>
            <Typography variant="caption" sx={{ color: '#64748B', fontSize: '0.72rem' }}>
              (Đã loại trừ phí vận chuyển của tài xế)
            </Typography>
          </Box>
        </Card>

        {/* Metric 2: Success Rate */}
        <Card
          sx={{
            p: 2.5,
            borderRadius: 3.5,
            bgcolor: '#FFFFFF',
            border: '1px solid #BFDBFE',
            boxShadow: '0 4px 12px rgba(59, 130, 246, 0.08)',
            height: '100%',
            display: 'flex',
            flexDirection: 'column',
            justifyContent: 'space-between',
          }}
        >
          <Box>
            <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 1.5 }}>
              <Typography variant="caption" sx={{ fontWeight: 800, color: '#1D4ED8', textTransform: 'uppercase', letterSpacing: 0.5 }}>
                Tỉ Lệ Đơn Thành Công
              </Typography>
              <Avatar sx={{ bgcolor: '#DBEAFE', color: '#2563EB', width: 40, height: 40 }}>
                <TrendingUpIcon />
              </Avatar>
            </Box>
            <Typography variant="h4" sx={{ fontWeight: 800, color: '#1E3A8A', mb: 0.8 }}>
              {successRate}%
            </Typography>
            <LinearProgress
              variant="determinate"
              value={Math.min(100, Math.max(0, successRateNum))}
              sx={{
                height: 8,
                borderRadius: 4,
                bgcolor: '#DBEAFE',
                mb: 1,
                '& .MuiLinearProgress-bar': {
                  bgcolor: successRateNum >= 80 ? '#10B981' : successRateNum >= 50 ? '#F59E0B' : '#EF4444',
                  borderRadius: 4,
                },
              }}
            />
          </Box>
          <Box sx={{ pt: 1, borderTop: '1px dashed #DBEAFE' }}>
            <Typography variant="caption" sx={{ color: '#1E40AF', fontWeight: 600, display: 'block' }}>
              <b>{completedOrders.length}</b> thành công / <b>{cancelledOrders.length}</b> đơn bị hủy
            </Typography>
            <Typography variant="caption" sx={{ color: '#64748B', fontSize: '0.72rem' }}>
              Tổng {totalFinishedOrders} đơn kết thúc
            </Typography>
          </Box>
        </Card>

        {/* Metric 3: Total Items Sold */}
        <Card
          sx={{
            p: 2.5,
            borderRadius: 3.5,
            bgcolor: '#FFFFFF',
            border: '1px solid #FED7AA',
            boxShadow: '0 4px 12px rgba(249, 115, 22, 0.08)',
            height: '100%',
            display: 'flex',
            flexDirection: 'column',
            justifyContent: 'space-between',
          }}
        >
          <Box>
            <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 1.5 }}>
              <Typography variant="caption" sx={{ fontWeight: 800, color: '#C2410C', textTransform: 'uppercase', letterSpacing: 0.5 }}>
                Số Món Đã Bán
              </Typography>
              <Avatar sx={{ bgcolor: '#FFEDD5', color: '#EA580C', width: 40, height: 40 }}>
                <FastfoodIcon />
              </Avatar>
            </Box>
            <Typography variant="h4" sx={{ fontWeight: 800, color: '#7C2D12', mb: 0.8 }}>
              {totalDishesSold.toLocaleString('vi-VN')} <span style={{ fontSize: '1.1rem', fontWeight: 600 }}>phần</span>
            </Typography>
          </Box>
          <Box sx={{ pt: 1.5, borderTop: '1px dashed #FED7AA' }}>
            <Typography variant="caption" sx={{ color: '#9A3412', fontWeight: 600, display: 'block' }}>
              Tổng số lượng món & suất ăn
            </Typography>
            <Typography variant="caption" sx={{ color: '#64748B', fontSize: '0.72rem' }}>
              Đã chế biến & bàn giao thành công
            </Typography>
          </Box>
        </Card>

        {/* Metric 4: Average Order Value (AOV) with Detailed Explanation */}
        <Card
          sx={{
            p: 2.5,
            borderRadius: 3.5,
            bgcolor: '#FFFFFF',
            border: '1px solid #DDD6FE',
            boxShadow: '0 4px 12px rgba(124, 58, 237, 0.08)',
            height: '100%',
            display: 'flex',
            flexDirection: 'column',
            justifyContent: 'space-between',
          }}
        >
          <Box>
            <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 1.5 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.6 }}>
                <Typography variant="caption" sx={{ fontWeight: 800, color: '#6D28D9', textTransform: 'uppercase', letterSpacing: 0.5 }}>
                  TB / Đơn (AOV)
                </Typography>
                <Tooltip title="Giá trị trung bình mỗi đơn hàng (Average Order Value) = Tổng doanh thu ÷ Số đơn hoàn thành. Thể hiện mức chi tiêu trung bình của 1 khách trên mỗi lần đặt món." arrow>
                  <InfoIcon sx={{ fontSize: 16, color: '#8B5CF6', cursor: 'pointer' }} />
                </Tooltip>
              </Box>
              <Avatar sx={{ bgcolor: '#EDE9FE', color: '#7C3AED', width: 40, height: 40 }}>
                <ReceiptIcon />
              </Avatar>
            </Box>
            <Typography variant="h4" sx={{ fontWeight: 800, color: '#4C1D95', mb: 0.8 }}>
              {formatPrice(avgOrderValue)}
            </Typography>
          </Box>
          <Box sx={{ pt: 1.5, borderTop: '1px dashed #DDD6FE' }}>
            <Typography variant="caption" sx={{ color: '#5B21B6', fontWeight: 600, display: 'block' }}>
              Mức chi tiêu trung bình / đơn
            </Typography>
            <Typography variant="caption" sx={{ color: '#64748B', fontSize: '0.72rem' }}>
              Đang phục vụ: <b>{inProgressOrders.length}</b> đơn hoạt động
            </Typography>
          </Box>
        </Card>
      </Box>

      {/* EXPLANATION BANNER ABOUT AOV */}
      <Card
        sx={{
          mb: 3.5,
          p: 2,
          borderRadius: 3,
          bgcolor: '#FAF5FF',
          border: '1px solid #E9D5FF',
          display: 'flex',
          alignItems: { xs: 'flex-start', sm: 'center' },
          gap: 2,
        }}
      >
        <Avatar sx={{ bgcolor: '#7C3AED', color: '#FFFFFF', width: 36, height: 36 }}>
          💡
        </Avatar>
        <Box sx={{ flex: 1 }}>
          <Typography variant="subtitle2" sx={{ fontWeight: 800, color: '#581C87', mb: 0.2 }}>
            Giải thích chỉ số TB / Đơn (Average Order Value - AOV):
          </Typography>
          <Typography variant="body2" sx={{ color: '#6B21A8', fontSize: '0.85rem' }}>
            <b>TB / Đơn</b> là số tiền trung bình một khách hàng chi trả cho mỗi đơn món ăn thành công ({formatPrice(totalCompletedRevenue)} ÷ {completedOrders.length} đơn = <b>{formatPrice(avgOrderValue)}</b>/đơn). 
            Quán có thể tăng doanh thu nhanh chóng bằng cách tạo combo món, kèm nước uống hoặc gợi ý món ăn thêm để nâng cao giá trị trung bình mỗi đơn!
          </Typography>
        </Box>
      </Card>

      {/* DETAILED BREAKDOWNS SECTION */}
      <Box
        sx={{
          display: 'grid',
          gridTemplateColumns: {
            xs: '1fr',
            md: '7fr 5fr',
          },
          gap: 3,
        }}
      >
        {/* Left Column: Best Selling Dishes Table */}
        <Card sx={{ p: 2.5, borderRadius: 3.5, border: '1px solid #E2E8F0', height: '100%' }}>
          <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <FastfoodIcon sx={{ color: '#EA580C', fontSize: 22 }} />
              <Typography variant="subtitle1" sx={{ fontWeight: 800, color: '#0F172A' }}>
                Top Món Bán Chạy Nhất
              </Typography>
            </Box>
            <Chip label={`${bestSellers.length} món`} size="small" sx={{ bgcolor: '#FFF7ED', color: '#EA580C', fontWeight: 700 }} />
          </Box>

          {bestSellers.length === 0 ? (
            <Box sx={{ py: 6, textAlign: 'center', color: '#94A3B8' }}>
              <FastfoodIcon sx={{ fontSize: 40, mb: 1, opacity: 0.5 }} />
              <Typography variant="body2">Chưa có dữ liệu món đã bán trong khoảng thời gian này</Typography>
            </Box>
          ) : (
            <TableContainer component={Paper} elevation={0} sx={{ border: '1px solid #F1F5F9', borderRadius: 2.5 }}>
              <Table size="small">
                <TableHead sx={{ bgcolor: '#F8FAFC' }}>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 800, color: '#475569', fontSize: '0.8rem' }}>HẠNG</TableCell>
                    <TableCell sx={{ fontWeight: 800, color: '#475569', fontSize: '0.8rem' }}>TÊN MÓN ĂN</TableCell>
                    <TableCell align="right" sx={{ fontWeight: 800, color: '#475569', fontSize: '0.8rem' }}>SỐ LƯỢNG BÁN</TableCell>
                    <TableCell align="right" sx={{ fontWeight: 800, color: '#475569', fontSize: '0.8rem' }}>DOANH THU</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {bestSellers.map((item, index) => (
                    <TableRow key={index} hover>
                      <TableCell sx={{ fontWeight: 800, color: index === 0 ? '#EA580C' : index === 1 ? '#2563EB' : '#64748B' }}>
                        #{index + 1}
                      </TableCell>
                      <TableCell sx={{ fontWeight: 700, color: '#1E293B' }}>
                        {item.name}
                      </TableCell>
                      <TableCell align="right" sx={{ fontWeight: 800, color: '#EA580C' }}>
                        {item.quantity} <span style={{ fontSize: '0.75rem', fontWeight: 500, color: '#64748B' }}>phần</span>
                      </TableCell>
                      <TableCell align="right" sx={{ fontWeight: 800, color: '#059669' }}>
                        {formatPrice(item.revenue)}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          )}
        </Card>

        {/* Right Column: Payment Methods & Cancellation Analysis */}
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
          {/* Payment Method Distribution */}
          <Card sx={{ p: 2.5, borderRadius: 3.5, border: '1px solid #E2E8F0' }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
              <WalletIcon sx={{ color: '#7C3AED', fontSize: 22 }} />
              <Typography variant="subtitle1" sx={{ fontWeight: 800, color: '#0F172A' }}>
                Doanh Thu Theo Phương Thức
              </Typography>
            </Box>

            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
              {Object.keys(paymentBreakdown).map((key) => {
                const p = paymentBreakdown[key];
                if (p.count === 0 && totalCompletedRevenue > 0) return null;
                return (
                  <Box
                    key={key}
                    sx={{
                      p: 1.5,
                      borderRadius: 2.5,
                      bgcolor: p.bgcolor,
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'space-between',
                    }}
                  >
                    <Box>
                      <Typography variant="body2" sx={{ fontWeight: 700, color: p.color }}>
                        {p.label}
                      </Typography>
                      <Typography variant="caption" sx={{ color: '#64748B' }}>
                        {p.count} đơn hoàn thành
                      </Typography>
                    </Box>
                    <Typography variant="subtitle2" sx={{ fontWeight: 800, color: '#0F172A' }}>
                      {formatPrice(p.amount)}
                    </Typography>
                  </Box>
                );
              })}
            </Box>
          </Card>

          {/* Cancellation Breakdown */}
          <Card sx={{ p: 2.5, borderRadius: 3.5, border: '1px solid #E2E8F0' }}>
            <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <CancelIcon sx={{ color: '#EF4444', fontSize: 22 }} />
                <Typography variant="subtitle1" sx={{ fontWeight: 800, color: '#0F172A' }}>
                  Phân Tích Đơn Hủy ({cancelledOrders.length})
                </Typography>
              </Box>
              <Chip
                label={totalFinishedOrders > 0 ? `${((cancelledOrders.length / totalFinishedOrders) * 100).toFixed(1)}% tỉ lệ hủy` : '0%'}
                size="small"
                sx={{ bgcolor: '#FEE2E2', color: '#DC2626', fontWeight: 700 }}
              />
            </Box>

            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
              {Object.keys(cancelStats).map((k) => {
                const cs = cancelStats[k];
                return (
                  <Box
                    key={k}
                    sx={{
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'space-between',
                      py: 0.8,
                      borderBottom: '1px solid #F1F5F9',
                    }}
                  >
                    <Typography variant="body2" sx={{ color: '#475569', fontSize: '0.85rem' }}>
                      {cs.label}
                    </Typography>
                    <Typography variant="body2" sx={{ fontWeight: 800, color: cs.count > 0 ? '#DC2626' : '#94A3B8' }}>
                      {cs.count} đơn
                    </Typography>
                  </Box>
                );
              })}
            </Box>
          </Card>
        </Box>
      </Box>
    </Box>
  );
};

export default MerchantAnalytics;
