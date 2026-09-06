import React, { useState, useEffect } from 'react';
import {
  Box,
  Grid,
  Card,
  Typography,
  CircularProgress,
  Chip,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Button,
  Tooltip,
} from '@mui/material';
import {
  DirectionsCar as RideIcon,
  AttachMoney as MoneyIcon,
  People as CustomerIcon,
  TwoWheeler as DriverIcon,
  Refresh as RefreshIcon,
  ArrowForward as ArrowForwardIcon,
  PlayArrow as PlayIcon,
  Pause as PauseIcon,
  FiberManualRecord as DotIcon,
  Fastfood as FoodIcon,
  Store as RestaurantIcon,
} from '@mui/icons-material';
import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip as RechartsTooltip,
  ResponsiveContainer,
  BarChart,
  Bar,
  PieChart,
  Pie,
  Cell,
} from 'recharts';
import { useTheme } from '@mui/material/styles';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import userService from '../services/userService';
import pricingService from '../services/pricingService';
import bookingService from '../services/bookingService';
import paymentService from '../services/paymentService';
import foodService from '../services/foodService';

export const Dashboard = () => {
  const theme = useTheme();
  const navigate = useNavigate();
  const isDark = theme.palette.mode === 'dark';

  const [loading, setLoading] = useState(true);
  const [lastUpdated, setLastUpdated] = useState(new Date());
  const [autoSync, setAutoSync] = useState(true);
  const [countdown, setCountdown] = useState(10);

  const [stats, setStats] = useState({
    totalCustomers: 0,
    totalDrivers: 0,
    activeDrivers: 0,
    totalBookings: 0,
    completedBookings: 0,
    inProgressBookings: 0,
    cancelledBookings: 0,
    pendingBookings: 0,
    totalGmv: 0,
    platformCommission: 0,
    totalTransactions: 0,
    successTransactions: 0,
    totalFoodOrders: 0,
    completedFoodOrders: 0,
    foodRevenue: 0,
    totalRestaurants: 0,
    openRestaurants: 0,
  });

  const [weeklyRideData, setWeeklyRideData] = useState([]);
  const [monthlyTripData, setMonthlyTripData] = useState([]);
  const [serviceFleetData, setServiceFleetData] = useState([]);
  const [recentBookings, setRecentBookings] = useState([]);

  const sparklineRides = [{ v: 8 }, { v: 12 }, { v: 15 }, { v: 11 }, { v: 18 }, { v: 14 }, { v: 22 }, { v: 19 }, { v: 25 }, { v: 28 }];
  const sparklineRevenue = [{ v: 10 }, { v: 14 }, { v: 12 }, { v: 19 }, { v: 16 }, { v: 24 }, { v: 21 }, { v: 28 }, { v: 26 }, { v: 35 }];
  const sparklineUsers = [{ v: 4 }, { v: 6 }, { v: 9 }, { v: 8 }, { v: 12 }, { v: 11 }, { v: 15 }, { v: 14 }, { v: 18 }, { v: 20 }];
  const sparklineDrivers = [{ v: 5 }, { v: 7 }, { v: 6 }, { v: 10 }, { v: 9 }, { v: 12 }, { v: 11 }, { v: 14 }, { v: 13 }, { v: 16 }];

  const processWeeklyData = (bookings) => {
    const days = [
      { day: 'T2', booked: 0, completed: 0 },
      { day: 'T3', booked: 0, completed: 0 },
      { day: 'T4', booked: 0, completed: 0 },
      { day: 'T5', booked: 0, completed: 0 },
      { day: 'T6', booked: 0, completed: 0 },
      { day: 'T7', booked: 0, completed: 0 },
      { day: 'CN', booked: 0, completed: 0 },
    ];

    if (bookings && bookings.length > 0) {
      bookings.forEach((b) => {
        const date = b.createdAt ? new Date(b.createdAt) : new Date();
        const dayOfWeek = date.getDay();
        const index = dayOfWeek === 0 ? 6 : dayOfWeek - 1;
        if (days[index]) {
          days[index].booked += 1;
          if (b.status === 'COMPLETED') {
            days[index].completed += 1;
          }
        }
      });
    }
    return days;
  };

  const processMonthlyData = (bookings) => {
    const monthsMap = {
      1: { month: 'T1', count: 0 },
      2: { month: 'T2', count: 0 },
      3: { month: 'T3', count: 0 },
      4: { month: 'T4', count: 0 },
      5: { month: 'T5', count: 0 },
      6: { month: 'T6', count: 0 },
      7: { month: 'T7', count: 0 },
      8: { month: 'T8', count: 0 },
      9: { month: 'T9', count: 0 },
      10: { month: 'T10', count: 0 },
      11: { month: 'T11', count: 0 },
      12: { month: 'T12', count: 0 },
    };

    if (bookings && bookings.length > 0) {
      bookings.forEach((b) => {
        const date = b.createdAt ? new Date(b.createdAt) : new Date();
        const monthNum = date.getMonth() + 1;
        if (monthsMap[monthNum]) {
          monthsMap[monthNum].count += 1;
        }
      });
    }

    const currentMonth = new Date().getMonth() + 1;
    const result = [];
    for (let i = 5; i >= 0; i--) {
      let m = currentMonth - i;
      if (m <= 0) m += 12;
      result.push(monthsMap[m] || { month: `T${m}`, count: 0 });
    }
    return result;
  };

  const processFleetDistribution = (bookings) => {
    let bikeCount = 0;
    let carCount = 0;
    let deliveryCount = 0;

    if (bookings && bookings.length > 0) {
      bookings.forEach((b) => {
        const dist = b.distanceInKm || 0;
        if (dist > 10) carCount++;
        else if (dist > 4) bikeCount++;
        else deliveryCount++;
      });
    }

    const total = bikeCount + carCount + deliveryCount;
    if (total === 0) {
      return [
        { name: 'OmniBike (Xe máy 2 bánh)', value: 55, color: '#15ca20' },
        { name: 'OmniCar (Ô tô 4-7 chỗ)', value: 30, color: '#008cff' },
        { name: 'OmniExpress (Giao hàng siêu tốc)', value: 15, color: '#ff3366' },
      ];
    }

    return [
      { name: 'OmniBike (Xe máy 2 bánh)', value: Math.round((bikeCount / total) * 100) || 50, color: '#15ca20' },
      { name: 'OmniCar (Ô tô 4-7 chỗ)', value: Math.round((carCount / total) * 100) || 35, color: '#008cff' },
      { name: 'OmniExpress (Giao hàng)', value: Math.round((deliveryCount / total) * 100) || 15, color: '#ff3366' },
    ];
  };

  const fetchDashboardData = async (isBackground = false) => {
    if (!isBackground) setLoading(true);
    try {
      const [
        usersRes,
        pricingRes,
        bookingStatsRes,
        paymentStatsRes,
        allBookingsRes,
        allTransactionsRes,
        foodStatsRes,
        restaurantsRes,
      ] = await Promise.allSettled([
        userService.getAllUsers({ page: 0, size: 100 }),
        pricingService.getPricingConfig(),
        bookingService.getBookingStats(),
        paymentService.getPaymentStats(),
        bookingService.getAllBookings(),
        paymentService.getAllTransactions(),
        foodService.getFoodStats(),
        foodService.getAllRestaurants(),
      ]);

      // Process Users & Drivers
      let allUsers = [];
      if (usersRes.status === 'fulfilled' && usersRes.value) {
        if (usersRes.value.content) {
          allUsers = usersRes.value.content;
        } else if (Array.isArray(usersRes.value)) {
          allUsers = usersRes.value;
        } else if (usersRes.value.data) {
          allUsers = Array.isArray(usersRes.value.data) ? usersRes.value.data : [usersRes.value.data];
        }
      }

      const totalCustomers = allUsers.filter((u) => u.role === 'CUSTOMER').length;
      const driversList = allUsers.filter((u) => u.role === 'DRIVER');
      const totalDrivers = driversList.length;
      const activeDrivers = driversList.filter((d) => d.status === 'ONLINE' || d.isActive === true).length;

      // Process Bookings
      const bStats = bookingStatsRes.status === 'fulfilled' ? bookingStatsRes.value : {};
      const allBookings = allBookingsRes.status === 'fulfilled' && Array.isArray(allBookingsRes.value) ? allBookingsRes.value : [];
      setRecentBookings(allBookings.slice(0, 5));

      const totalBookings = Number(bStats.totalBookings ?? allBookings.length);
      const completedBookings = Number(bStats.completedBookings ?? allBookings.filter((b) => b.status === 'COMPLETED').length);
      const inProgressBookings = Number(bStats.inProgressBookings ?? allBookings.filter((b) => ['ACCEPTED', 'ARRIVED', 'IN_PROGRESS'].includes(b.status)).length);
      const cancelledBookings = Number(bStats.cancelledBookings ?? allBookings.filter((b) => b.status === 'CANCELLED').length);
      const pendingBookings = Number(bStats.pendingBookings ?? allBookings.filter((b) => b.status === 'PENDING').length);

      // Process Payments
      const pStats = paymentStatsRes.status === 'fulfilled' ? paymentStatsRes.value : {};
      const allTransactions = allTransactionsRes.status === 'fulfilled' && Array.isArray(allTransactionsRes.value) ? allTransactionsRes.value : [];

      const totalTransactions = Number(pStats.totalTransactions ?? allTransactions.length);
      const successTransactions = Number(pStats.successTransactions ?? allTransactions.filter((t) => t.status === 'SUCCESS').length);

      // Tổng 100% GMV toàn bộ các chuyến xe hoàn thành (COMPLETED), bao gồm cả Tiền mặt và Online
      const computedGmv = allBookings
        .filter((b) => b.status === 'COMPLETED' && b.price)
        .reduce((sum, b) => sum + Number(b.price), 0);

      const totalGmv = Number(bStats.totalGmv ?? computedGmv ?? 0);
      const platformCommission = Math.round(totalGmv * 0.20);

      // Process Food Delivery
      const fStats = foodStatsRes.status === 'fulfilled' ? foodStatsRes.value : {};
      const allRestaurants = restaurantsRes.status === 'fulfilled' && Array.isArray(restaurantsRes.value) ? restaurantsRes.value : [];
      const totalFoodOrders = Number(fStats.totalOrders || 0);
      const completedFoodOrders = Number(fStats.completedOrders || 0);
      const foodRevenue = Number(fStats.totalRevenue || 0);
      const totalRestaurants = allRestaurants.length;
      const openRestaurants = allRestaurants.filter((r) => r.status === 'OPEN').length;

      setStats({
        totalCustomers,
        totalDrivers,
        activeDrivers,
        totalBookings,
        completedBookings,
        inProgressBookings,
        cancelledBookings,
        pendingBookings,
        totalGmv,
        platformCommission,
        totalTransactions,
        successTransactions,
        totalFoodOrders,
        completedFoodOrders,
        foodRevenue,
        totalRestaurants,
        openRestaurants,
      });

      setWeeklyRideData(processWeeklyData(allBookings));
      setMonthlyTripData(processMonthlyData(allBookings));
      setServiceFleetData(processFleetDistribution(allBookings));
      setLastUpdated(new Date());
    } catch {
      if (!isBackground) toast.error('Lỗi khi nạp dữ liệu thống kê từ Backend');
    } finally {
      if (!isBackground) setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboardData();
  }, []);

  // Realtime live auto-sync every 10s
  useEffect(() => {
    if (!autoSync) return;
    const interval = setInterval(() => {
      setCountdown((prev) => {
        if (prev <= 1) {
          fetchDashboardData(true);
          return 10;
        }
        return prev - 1;
      });
    }, 1000);

    return () => clearInterval(interval);
  }, [autoSync]);

  const getBookingStatusBadge = (status) => {
    switch (status) {
      case 'COMPLETED':
        return <Chip label="Hoàn Thành" size="small" sx={{ bgcolor: 'rgba(21, 202, 32, 0.15)', color: '#15ca20', fontWeight: 700, fontSize: '0.72rem' }} />;
      case 'IN_PROGRESS':
      case 'ACCEPTED':
      case 'ARRIVED':
        return <Chip label="Đang Chạy" size="small" sx={{ bgcolor: 'rgba(0, 140, 255, 0.15)', color: '#008cff', fontWeight: 700, fontSize: '0.72rem' }} />;
      case 'CANCELLED':
        return <Chip label="Đã Hủy" size="small" sx={{ bgcolor: 'rgba(255, 51, 102, 0.15)', color: '#ff3366', fontWeight: 700, fontSize: '0.72rem' }} />;
      default:
        return <Chip label="Chờ Ghép" size="small" sx={{ bgcolor: 'rgba(255, 184, 0, 0.15)', color: '#ffb800', fontWeight: 700, fontSize: '0.72rem' }} />;
    }
  };

  return (
    <Box sx={{ width: '100%' }} className="page-enter-animation">
      {/* Header bar with Realtime Controls & 3D Toggle */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2.5, flexWrap: 'wrap', gap: 1.5 }}>
        <Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.2 }}>
            <Typography variant="h5" sx={{ fontWeight: 700, fontFamily: '"Poppins", sans-serif' }}>
              Tổng Quan Vận Hành
            </Typography>
            <Chip
              icon={<DotIcon sx={{ fontSize: '10px !important', color: autoSync ? '#15ca20 !important' : '#94a3b8 !important' }} />}
              label={autoSync ? `REALTIME (${countdown}s)` : 'TẠM DỪNG'}
              size="small"
              className={autoSync ? 'realtime-live-pulse' : ''}
              sx={{
                bgcolor: autoSync ? 'rgba(21, 202, 32, 0.12)' : 'rgba(148, 163, 184, 0.12)',
                color: autoSync ? '#15ca20' : 'text.secondary',
                fontWeight: 800,
                fontSize: '0.72rem',
                letterSpacing: '0.04em',
              }}
            />
          </Box>
          <Typography variant="body2" sx={{ color: 'text.secondary', mt: 0.2 }}>
            Cập nhật lần cuối: {lastUpdated.toLocaleTimeString('vi-VN')} • Dữ liệu kết nối trực tiếp từ hệ thống Backend
          </Typography>
        </Box>

        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
          {/* Realtime Auto-sync Toggle */}
          <Button
            variant="outlined"
            size="small"
            startIcon={autoSync ? <PauseIcon /> : <PlayIcon />}
            onClick={() => setAutoSync(!autoSync)}
            sx={{ borderRadius: 2, fontWeight: 600, fontSize: '0.8rem' }}
          >
            {autoSync ? 'Dừng Sync' : 'Bật Sync'}
          </Button>

          {/* Manual Refresh */}
          <Button
            variant="outlined"
            size="small"
            startIcon={<RefreshIcon />}
            onClick={() => fetchDashboardData(false)}
            disabled={loading}
            sx={{ borderRadius: 2, fontWeight: 600, fontSize: '0.8rem' }}
          >
            Làm Mới
          </Button>
        </Box>
      </Box>

      {/* 4 Clean Fixed KPI Metric Cards Grid */}
      <Grid container spacing={2.5} sx={{ mb: 2.5 }}>
        {/* Card 1: Completed Bookings */}
        <Grid item xs={12} sm={6} lg={3}>
          <Card
            className="page-enter-animation"
            sx={{
              p: 2.5,
              height: '100%',
              display: 'flex',
              flexDirection: 'column',
              justifyContent: 'space-between',
            }}
          >
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
              <Box>
                <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.04em' }}>
                  Chuyến Xe Hoàn Thành
                </Typography>
                <Typography variant="h4" sx={{ fontWeight: 800, color: 'text.primary', my: 0.5 }}>
                  {loading ? <CircularProgress size={22} /> : stats.completedBookings.toLocaleString('vi-VN')}
                </Typography>
              </Box>
              <Box sx={{ p: 1.2, borderRadius: 2.5, bgcolor: 'rgba(0, 140, 255, 0.12)', boxShadow: '0 4px 10px rgba(0, 140, 255, 0.2)' }}>
                <RideIcon sx={{ color: '#008cff', fontSize: 26 }} />
              </Box>
            </Box>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', mt: 1.5 }}>
              <Box sx={{ width: 90, height: 24 }}>
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={sparklineRides}><Bar dataKey="v" fill="#008cff" radius={[2, 2, 0, 0]} /></BarChart>
                </ResponsiveContainer>
              </Box>
              <Chip
                label={`Tổng: ${stats.totalBookings} cuốc`}
                size="small"
                sx={{ bgcolor: 'rgba(0, 140, 255, 0.12)', color: '#008cff', fontWeight: 700, fontSize: '0.72rem' }}
              />
            </Box>
          </Card>
        </Grid>

        {/* Card 2: Total GMV Revenue */}
        <Grid item xs={12} sm={6} lg={3}>
          <Card
            className="page-enter-animation"
            sx={{
              p: 2.5,
              height: '100%',
              display: 'flex',
              flexDirection: 'column',
              justifyContent: 'space-between',
            }}
          >
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
              <Box>
                <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.04em' }}>
                  Tổng GMV Toàn Sàn
                </Typography>
                <Typography variant="h4" sx={{ fontWeight: 800, color: 'text.primary', my: 0.5 }}>
                  {loading ? <CircularProgress size={22} /> : `${Number(stats.totalGmv).toLocaleString('vi-VN')} đ`}
                </Typography>
              </Box>
              <Box sx={{ p: 1.2, borderRadius: 2.5, bgcolor: 'rgba(255, 51, 102, 0.12)', boxShadow: '0 4px 10px rgba(255, 51, 102, 0.2)' }}>
                <MoneyIcon sx={{ color: '#ff3366', fontSize: 26 }} />
              </Box>
            </Box>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', mt: 1.5 }}>
              <Box sx={{ width: 90, height: 24 }}>
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={sparklineRevenue}><Bar dataKey="v" fill="#ff3366" radius={[2, 2, 0, 0]} /></BarChart>
                </ResponsiveContainer>
              </Box>
              <Tooltip title={`Doanh thu sàn thu 20% từ các cuốc xe hoàn thành: ${Number(stats.platformCommission).toLocaleString('vi-VN')} đ`}>
                <Chip
                  label={`Phí sàn (20%): ${Number(stats.platformCommission).toLocaleString('vi-VN')} đ`}
                  size="small"
                  sx={{ bgcolor: 'rgba(21, 202, 32, 0.15)', color: '#15ca20', fontWeight: 700, fontSize: '0.72rem' }}
                />
              </Tooltip>
            </Box>
          </Card>
        </Grid>

        {/* Card 3: Customers Registered */}
        <Grid item xs={12} sm={6} lg={3}>
          <Card
            className="page-enter-animation"
            sx={{
              p: 2.5,
              height: '100%',
              display: 'flex',
              flexDirection: 'column',
              justifyContent: 'space-between',
            }}
          >
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
              <Box>
                <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.04em' }}>
                  Khách Hàng Đăng Ký
                </Typography>
                <Typography variant="h4" sx={{ fontWeight: 800, color: 'text.primary', my: 0.5 }}>
                  {loading ? <CircularProgress size={22} /> : `${stats.totalCustomers} Khách`}
                </Typography>
              </Box>
              <Box sx={{ p: 1.2, borderRadius: 2.5, bgcolor: 'rgba(21, 202, 32, 0.12)', boxShadow: '0 4px 10px rgba(21, 202, 32, 0.2)' }}>
                <CustomerIcon sx={{ color: '#15ca20', fontSize: 26 }} />
              </Box>
            </Box>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', mt: 1.5 }}>
              <Box sx={{ width: 90, height: 24 }}>
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={sparklineUsers}><Bar dataKey="v" fill="#15ca20" radius={[2, 2, 0, 0]} /></BarChart>
                </ResponsiveContainer>
              </Box>
              <Chip
                label="Role CUSTOMER"
                size="small"
                sx={{ bgcolor: 'rgba(21, 202, 32, 0.15)', color: '#15ca20', fontWeight: 700, fontSize: '0.72rem' }}
              />
            </Box>
          </Card>
        </Grid>

        {/* Card 4: Drivers Active */}
        <Grid item xs={12} sm={6} lg={3}>
          <Card
            className="page-enter-animation"
            sx={{
              p: 2.5,
              height: '100%',
              display: 'flex',
              flexDirection: 'column',
              justifyContent: 'space-between',
            }}
          >
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
              <Box>
                <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.04em' }}>
                  Tài Xế Hoạt Động
                </Typography>
                <Typography variant="h4" sx={{ fontWeight: 800, color: 'text.primary', my: 0.5 }}>
                  {loading ? <CircularProgress size={22} /> : `${stats.totalDrivers} Tài Xế`}
                </Typography>
              </Box>
              <Box sx={{ p: 1.2, borderRadius: 2.5, bgcolor: 'rgba(255, 184, 0, 0.12)', boxShadow: '0 4px 10px rgba(255, 184, 0, 0.2)' }}>
                <DriverIcon sx={{ color: '#ffb800', fontSize: 26 }} />
              </Box>
            </Box>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', mt: 1.5 }}>
              <Box sx={{ width: 90, height: 24 }}>
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={sparklineDrivers}><Bar dataKey="v" fill="#ffb800" radius={[2, 2, 0, 0]} /></BarChart>
                </ResponsiveContainer>
              </Box>
              <Chip
                label={`${stats.activeDrivers} Trực tuyến`}
                size="small"
                sx={{ bgcolor: 'rgba(255, 184, 0, 0.15)', color: '#d97706', fontWeight: 700, fontSize: '0.72rem' }}
              />
            </Box>
          </Card>
        </Grid>
      </Grid>

      {/* 2 Food Delivery Telemetry Cards */}
      <Grid container spacing={2.5} sx={{ mb: 2.5 }}>
        {/* Food Orders Card */}
        <Grid item xs={12} sm={6} md={6}>
          <Card
            sx={{
              p: 2.5,
              borderRadius: 2.5,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              bgcolor: 'background.paper',
              border: 1,
              borderColor: 'divider',
              cursor: 'pointer',
              transition: 'transform 0.2s',
              '&:hover': { transform: 'translateY(-2px)' },
            }}
            onClick={() => navigate('/food-orders')}
          >
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
              <Box sx={{ p: 1.5, borderRadius: 2.5, bgcolor: 'rgba(249, 115, 22, 0.12)', color: '#f97316' }}>
                <FoodIcon sx={{ fontSize: 32 }} />
              </Box>
              <Box>
                <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 700, textTransform: 'uppercase' }}>
                  Đơn Giao Đồ Ăn (Food Delivery)
                </Typography>
                <Typography variant="h5" sx={{ fontWeight: 800, color: 'text.primary', my: 0.3 }}>
                  {stats.totalFoodOrders} Đơn Hàng
                </Typography>
                <Typography variant="caption" sx={{ color: '#15ca20', fontWeight: 600 }}>
                  ✅ {stats.completedFoodOrders} Hoàn tất thành công
                </Typography>
              </Box>
            </Box>
            <Box sx={{ textAlign: 'right' }}>
              <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600, display: 'block' }}>
                Doanh Thu Đồ Ăn
              </Typography>
              <Typography variant="h6" sx={{ fontWeight: 800, color: '#f97316' }}>
                {Number(stats.foodRevenue).toLocaleString('vi-VN')} đ
              </Typography>
              <Button size="small" endIcon={<ArrowForwardIcon />} sx={{ textTransform: 'none', mt: 0.5, p: 0, fontSize: '0.78rem' }}>
                Xem đơn
              </Button>
            </Box>
          </Card>
        </Grid>

        {/* Restaurants Card */}
        <Grid item xs={12} sm={6} md={6}>
          <Card
            sx={{
              p: 2.5,
              borderRadius: 2.5,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              bgcolor: 'background.paper',
              border: 1,
              borderColor: 'divider',
              cursor: 'pointer',
              transition: 'transform 0.2s',
              '&:hover': { transform: 'translateY(-2px)' },
            }}
            onClick={() => navigate('/restaurants')}
          >
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
              <Box sx={{ p: 1.5, borderRadius: 2.5, bgcolor: 'rgba(0, 140, 255, 0.12)', color: '#008cff' }}>
                <RestaurantIcon sx={{ fontSize: 32 }} />
              </Box>
              <Box>
                <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 700, textTransform: 'uppercase' }}>
                  Đối Tác Nhà Hàng
                </Typography>
                <Typography variant="h5" sx={{ fontWeight: 800, color: 'text.primary', my: 0.3 }}>
                  {stats.totalRestaurants} Nhà Hàng
                </Typography>
                <Typography variant="caption" sx={{ color: '#15ca20', fontWeight: 600 }}>
                  🟢 {stats.openRestaurants} Quán đang mở cửa nhận đơn
                </Typography>
              </Box>
            </Box>
            <Box sx={{ textAlign: 'right' }}>
              <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600, display: 'block' }}>
                Tỷ lệ hoạt động
              </Typography>
              <Typography variant="h6" sx={{ fontWeight: 800, color: '#008cff' }}>
                {stats.totalRestaurants > 0 ? `${Math.round((stats.openRestaurants / stats.totalRestaurants) * 100)}%` : '100%'}
              </Typography>
              <Button size="small" endIcon={<ArrowForwardIcon />} sx={{ textTransform: 'none', mt: 0.5, p: 0, fontSize: '0.78rem' }}>
                Quản lý quán
              </Button>
            </Box>
          </Card>
        </Grid>
      </Grid>

      {/* Main Charts Row with 3D Depth */}
      <Grid container spacing={2.5} sx={{ mb: 2.5 }}>
        {/* Left Chart (8 cols): Ride Dispatch Overview */}
        <Grid item xs={12} lg={8}>
          <Card sx={{ p: 2.5, height: '100%' }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2, flexWrap: 'wrap', gap: 1 }}>
              <Box>
                <Typography variant="h6" sx={{ fontWeight: 700, fontSize: '1.05rem', color: 'text.primary' }}>
                  Biểu Đồ Xu Hướng Cuốc Xe Trong Tuần
                </Typography>
                <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block' }}>
                  Lượng chuyến đặt và tỷ lệ chuyến xe hoàn thành theo thời gian thực
                </Typography>
              </Box>

              <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.8 }}>
                  <Box sx={{ width: 12, height: 12, borderRadius: '3px', bgcolor: '#008cff' }} />
                  <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600 }}>Hoàn thành</Typography>
                </Box>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.8 }}>
                  <Box sx={{ width: 12, height: 12, borderRadius: '3px', bgcolor: '#ff7849' }} />
                  <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600 }}>Tổng đặt</Typography>
                </Box>
              </Box>
            </Box>

            <Box sx={{ width: '100%', height: 270 }}>
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={weeklyRideData} margin={{ top: 10, right: 10, left: -15, bottom: 0 }}>
                  <defs>
                    <linearGradient id="completedGrad" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#008cff" stopOpacity={0.4} />
                      <stop offset="95%" stopColor="#008cff" stopOpacity={0.0} />
                    </linearGradient>
                    <linearGradient id="bookedGrad" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#ff7849" stopOpacity={0.3} />
                      <stop offset="95%" stopColor="#ff7849" stopOpacity={0.0} />
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" stroke={isDark ? 'rgba(255,255,255,0.06)' : '#e2e8f0'} />
                  <XAxis dataKey="day" stroke={isDark ? '#94a3b8' : '#64748b'} fontSize={12} tickLine={false} />
                  <YAxis stroke={isDark ? '#94a3b8' : '#64748b'} fontSize={12} tickLine={false} allowDecimals={false} />
                  <RechartsTooltip
                    contentStyle={{
                      backgroundColor: isDark ? '#172334' : '#ffffff',
                      border: `1px solid ${isDark ? '#2a374a' : '#e2e8f0'}`,
                      borderRadius: 8,
                      color: isDark ? '#ffffff' : '#1e293b',
                      fontSize: '0.85rem',
                    }}
                  />
                  <Area type="monotone" dataKey="completed" name="Hoàn thành" stroke="#008cff" strokeWidth={2.5} fillOpacity={1} fill="url(#completedGrad)" />
                  <Area type="monotone" dataKey="booked" name="Tổng đặt" stroke="#ff7849" strokeWidth={2.5} fillOpacity={1} fill="url(#bookedGrad)" />
                </AreaChart>
              </ResponsiveContainer>
            </Box>
          </Card>
        </Grid>

        {/* Right Chart (4 cols): Monthly Growth Bar Chart */}
        <Grid item xs={12} lg={4}>
          <Card sx={{ p: 2.5, height: '100%' }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
              <Box>
                <Typography variant="h6" sx={{ fontWeight: 700, fontSize: '1.05rem', color: 'text.primary' }}>
                  Sản Lượng Chuyến Theo Tháng
                </Typography>
                <Typography variant="caption" sx={{ color: 'text.secondary' }}>
                  Số chuyến xe 6 tháng gần nhất
                </Typography>
              </Box>
            </Box>

            <Box sx={{ width: '100%', height: 270 }}>
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={monthlyTripData} margin={{ top: 15, right: 10, left: -20, bottom: 0 }}>
                  <defs>
                    <linearGradient id="barTripGrad" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="0%" stopColor="#ff3366" />
                      <stop offset="100%" stopColor="#ff9900" />
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" stroke={isDark ? 'rgba(255,255,255,0.06)' : '#e2e8f0'} />
                  <XAxis dataKey="month" stroke={isDark ? '#94a3b8' : '#64748b'} fontSize={12} tickLine={false} />
                  <YAxis stroke={isDark ? '#94a3b8' : '#64748b'} fontSize={12} tickLine={false} allowDecimals={false} />
                  <RechartsTooltip
                    contentStyle={{
                      backgroundColor: isDark ? '#172334' : '#ffffff',
                      border: `1px solid ${isDark ? '#2a374a' : '#e2e8f0'}`,
                      borderRadius: 8,
                      color: isDark ? '#ffffff' : '#1e293b',
                      fontSize: '0.85rem',
                    }}
                    formatter={(val) => [`${val.toLocaleString('vi-VN')} chuyến`, 'Số cuốc xe']}
                  />
                  <Bar dataKey="count" fill="url(#barTripGrad)" radius={[8, 8, 0, 0]} barSize={22} />
                </BarChart>
              </ResponsiveContainer>
            </Box>
          </Card>
        </Grid>
      </Grid>

      {/* Bottom Row: Fleet Distribution & Live Recent Tables */}
      <Grid container spacing={2.5}>
        {/* Fleet Distribution Donut */}
        <Grid item xs={12} lg={4}>
          <Card sx={{ p: 2.5, height: '100%', display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
              <Typography variant="h6" sx={{ fontWeight: 700, fontSize: '1.05rem', color: 'text.primary' }}>
                Phân Bố Dịch Vụ Xe
              </Typography>
            </Box>

            <Box sx={{ position: 'relative', width: '100%', height: 230 }}>
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={serviceFleetData}
                    cx="50%"
                    cy="50%"
                    innerRadius={65}
                    outerRadius={88}
                    paddingAngle={5}
                    dataKey="value"
                  >
                    {serviceFleetData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.color} />
                    ))}
                  </Pie>
                </PieChart>
              </ResponsiveContainer>
              <Box
                sx={{
                  position: 'absolute',
                  top: '50%',
                  left: '50%',
                  transform: 'translate(-50%, -50%)',
                  textAlign: 'center',
                }}
              >
                <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600, display: 'block' }}>
                  Tổng Đội Xe
                </Typography>
                <Typography variant="h5" sx={{ fontWeight: 800, color: 'text.primary', lineHeight: 1.1 }}>
                  {stats.totalDrivers || 0}
                </Typography>
                <Typography variant="caption" sx={{ color: '#15ca20', fontWeight: 700, fontSize: '0.72rem' }}>
                  {stats.activeDrivers} Trực Tuyến
                </Typography>
              </Box>
            </Box>

            <Box sx={{ pt: 1.5, borderTop: 1, borderColor: 'divider', display: 'flex', flexDirection: 'column', gap: 1 }}>
              {serviceFleetData.map((item, i) => (
                <Box key={i} sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <Box sx={{ width: 10, height: 10, borderRadius: '50%', bgcolor: item.color }} />
                    <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600 }}>
                      {item.name}
                    </Typography>
                  </Box>
                  <Typography variant="caption" sx={{ fontWeight: 700, color: 'text.primary' }}>
                    {item.value}%
                  </Typography>
                </Box>
              ))}
            </Box>
          </Card>
        </Grid>

        {/* Live Recent Bookings Table */}
        <Grid item xs={12} lg={8}>
          <Card sx={{ p: 2.5, height: '100%' }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
              <Box>
                <Typography variant="h6" sx={{ fontWeight: 700, fontSize: '1.05rem', color: 'text.primary' }}>
                  Chuyến Xe Mới Nhất
                </Typography>
                <Typography variant="caption" sx={{ color: 'text.secondary' }}>
                  Dữ liệu trực tiếp thời gian thực từ booking-service
                </Typography>
              </Box>
              <Button
                size="small"
                endIcon={<ArrowForwardIcon />}
                onClick={() => navigate('/bookings')}
                sx={{ textTransform: 'none', fontWeight: 700 }}
              >
                Xem tất cả
              </Button>
            </Box>

            <TableContainer>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>MÃ CUỐC</TableCell>
                    <TableCell>KHÁCH HÀNG</TableCell>
                    <TableCell>TÀI XẾ</TableCell>
                    <TableCell>TIỀN CƯỚC</TableCell>
                    <TableCell>THANH TOÁN</TableCell>
                    <TableCell>TRẠNG THÁI</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {loading ? (
                    <TableRow>
                      <TableCell colSpan={6} align="center" sx={{ py: 3 }}>
                        <CircularProgress size={20} />
                      </TableCell>
                    </TableRow>
                  ) : recentBookings.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={6} align="center" sx={{ py: 3, color: 'text.secondary' }}>
                        Chưa có chuyến xe nào được tạo
                      </TableCell>
                    </TableRow>
                  ) : (
                    recentBookings.map((b) => (
                      <TableRow key={b.id} hover>
                        <TableCell sx={{ fontWeight: 700, color: '#008cff', fontFamily: 'monospace' }}>
                          #{b.id}
                        </TableCell>
                        <TableCell>User #{b.customerId}</TableCell>
                        <TableCell>{b.driverId ? `Tài xế #${b.driverId}` : 'Chưa gán'}</TableCell>
                        <TableCell sx={{ fontWeight: 700 }}>
                          {b.price ? `${Number(b.price).toLocaleString('vi-VN')} đ` : '—'}
                        </TableCell>
                        <TableCell>
                          <Chip label={b.paymentMethod || 'CASH'} size="small" variant="outlined" sx={{ fontSize: '0.7rem' }} />
                        </TableCell>
                        <TableCell>{getBookingStatusBadge(b.status)}</TableCell>
                      </TableRow>
                    ))
                  )}
                </TableBody>
              </Table>
            </TableContainer>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};

export default Dashboard;
