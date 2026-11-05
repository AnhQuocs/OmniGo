import React, { useState, useEffect, useMemo } from 'react';
import { useOutletContext, useNavigate } from 'react-router-dom';
import {
  Box,
  Typography,
  Card,
  CardContent,
  Chip,
  Button,
  Avatar,
  CircularProgress,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TablePagination,
  Paper,
  Tooltip,
  TextField,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Grid,
  InputAdornment,
  MenuItem,
  Select,
  FormControl,
  InputLabel,
  Divider,
  IconButton,
} from '@mui/material';
import {
  AccountBalanceWallet as WalletIcon,
  TrendingUp as TrendingUpIcon,
  TrendingDown as TrendingDownIcon,
  MonetizationOn as MonetizationOnIcon,
  ReceiptLong as ReceiptIcon,
  Refresh as RefreshIcon,
  AccountBalance as BankIcon,
  InfoOutlined as InfoIcon,
  FilterList as FilterIcon,
  CheckCircle as SuccessIcon,
  HourglassEmpty as PendingIcon,
  Cancel as FailedIcon,
  ArrowForward as ArrowForwardIcon,
  Close as CloseIcon,
  CalendarMonth as CalendarIcon,
  Search as SearchIcon,
  Restaurant as RestaurantIcon,
  Percent as PercentIcon,
} from '@mui/icons-material';
import paymentService from '../../services/paymentService';
import foodService from '../../services/foodService';
import toast from 'react-hot-toast';
import { formatVietnamDateTime } from '../../utils/dateTime';

export const MerchantWallet = () => {
  const { restaurant } = useOutletContext();
  const navigate = useNavigate();

  // State
  const [wallet, setWallet] = useState(null);
  const [transactions, setTransactions] = useState([]);
  const [restaurantOrders, setRestaurantOrders] = useState([]);
  const [platformFeeRate, setPlatformFeeRate] = useState(20);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);

  // Filters
  const [timeFilter, setTimeFilter] = useState('ALL'); // 'ALL' | 'TODAY' | 'WEEK' | 'MONTH' | 'CUSTOM'
  const [typeFilter, setTypeFilter] = useState('ALL'); // 'ALL' | 'FOOD_RESTAURANT_INCOME' | 'COMMISSION_FEE' | 'WITHDRAWAL'
  const [statusFilter, setStatusFilter] = useState('ALL'); // 'ALL' | 'SUCCESS' | 'PENDING' | 'FAILED'
  const [searchTerm, setSearchTerm] = useState('');
  const [customStartDate, setCustomStartDate] = useState('');
  const [customEndDate, setCustomEndDate] = useState('');

  // Pagination State
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);

  // Reset page to 0 when filters change
  useEffect(() => {
    setPage(0);
  }, [timeFilter, typeFilter, statusFilter, searchTerm, customStartDate, customEndDate]);

  // Withdraw Modal State
  const [withdrawModalOpen, setWithdrawModalOpen] = useState(false);
  const [withdrawAmount, setWithdrawAmount] = useState('');
  const [bankName, setBankName] = useState('Vietcombank');
  const [bankAccountNumber, setBankAccountNumber] = useState('');
  const [bankAccountName, setBankAccountName] = useState('');
  const [withdrawing, setWithdrawing] = useState(false);

  // Format currency
  const formatVND = (val) => {
    return `${Math.round(Number(val) || 0).toLocaleString('vi-VN')} đ`;
  };

  // Fetch Wallet, Transactions & Platform Fee
  const fetchData = async (isManualRefresh = false) => {
    try {
      if (isManualRefresh) setRefreshing(true);
      else setLoading(true);

      let currentResId = restaurant?.id;
      if (!currentResId) {
        try {
          const myRes = await foodService.getMyRestaurant();
          currentResId = myRes?.data?.id || myRes?.id;
        } catch (e) {}
      }

      const [walletRes, txRes, feeRes, ordersRes] = await Promise.allSettled([
        paymentService.getMyWallet(),
        paymentService.getMyTransactions(),
        paymentService.getPublicPlatformFee(),
        currentResId ? foodService.getRestaurantOrders(currentResId) : Promise.resolve([]),
      ]);

      if (walletRes.status === 'fulfilled') {
        const wData = walletRes.value?.data || walletRes.value;
        setWallet(wData);
      }

      if (txRes.status === 'fulfilled') {
        const rawTxs = txRes.value?.data || txRes.value;
        setTransactions(Array.isArray(rawTxs) ? rawTxs : []);
      }

      if (feeRes.status === 'fulfilled') {
        const fData = feeRes.value?.data || feeRes.value;
        if (fData && fData.percentage !== undefined) {
          setPlatformFeeRate(Number(fData.percentage));
        }
      }

      if (ordersRes && ordersRes.status === 'fulfilled') {
        const oData = ordersRes.value?.data || ordersRes.value;
        setRestaurantOrders(Array.isArray(oData) ? oData : []);
      }

      if (isManualRefresh) {
        toast.success('Đã cập nhật dữ liệu ví mới nhất!');
      }
    } catch (err) {
      console.error('Lỗi khi tải dữ liệu ví nhà hàng:', err);
      toast.error('Không thể tải thông tin ví doanh thu');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  // Orders Map for accurate payment methods
  const ordersMap = useMemo(() => {
    const map = {};
    if (Array.isArray(restaurantOrders)) {
      restaurantOrders.forEach((o) => {
        if (o && o.id) map[o.id] = o;
      });
    }
    return map;
  }, [restaurantOrders]);

  // Filter Transactions
  const filteredTransactions = useMemo(() => {
    let list = [...transactions];

    // Filter by type
    if (typeFilter !== 'ALL') {
      list = list.filter((t) => t.transactionType === typeFilter);
    }

    // Filter by status
    if (statusFilter !== 'ALL') {
      list = list.filter((t) => t.status === statusFilter);
    }

    // Filter by search (orderId, bookingId)
    if (searchTerm.trim()) {
      const term = searchTerm.trim().toLowerCase();
      list = list.filter((t) => {
        const orderIdStr = t.orderId ? String(t.orderId).toLowerCase() : '';
        const bookingIdStr = t.bookingId ? String(t.bookingId).toLowerCase() : '';
        return orderIdStr.includes(term) || bookingIdStr.includes(term);
      });
    }

    // Filter by Time
    const now = new Date();
    if (timeFilter === 'TODAY') {
      const startOfDay = new Date(now.getFullYear(), now.getMonth(), now.getDate());
      list = list.filter((t) => new Date(t.createdAt) >= startOfDay);
    } else if (timeFilter === 'WEEK') {
      const sevenDaysAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
      list = list.filter((t) => new Date(t.createdAt) >= sevenDaysAgo);
    } else if (timeFilter === 'MONTH') {
      const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1);
      list = list.filter((t) => new Date(t.createdAt) >= startOfMonth);
    } else if (timeFilter === 'CUSTOM') {
      if (customStartDate) {
        const start = new Date(customStartDate);
        start.setHours(0, 0, 0, 0);
        list = list.filter((t) => new Date(t.createdAt) >= start);
      }
      if (customEndDate) {
        const end = new Date(customEndDate);
        end.setHours(23, 59, 59, 999);
        list = list.filter((t) => new Date(t.createdAt) <= end);
      }
    }

    return list;
  }, [transactions, typeFilter, statusFilter, searchTerm, timeFilter, customStartDate, customEndDate]);

  // Paginated Transactions Slice
  const paginatedTransactions = useMemo(() => {
    const start = page * rowsPerPage;
    return filteredTransactions.slice(start, start + rowsPerPage);
  }, [filteredTransactions, page, rowsPerPage]);

  // Financial Metrics Calculation
  const metrics = useMemo(() => {
    let totalGrossIncome = 0;
    let totalCommission = 0;
    let totalWithdrawn = 0;

    transactions.forEach((tx) => {
      if (tx.status === 'SUCCESS') {
        const amt = Number(tx.amount || 0);
        if (tx.transactionType === 'FOOD_RESTAURANT_INCOME') {
          totalGrossIncome += amt;
        } else if (tx.transactionType === 'COMMISSION_FEE') {
          totalCommission += amt;
        } else if (tx.transactionType === 'WITHDRAWAL') {
          totalWithdrawn += amt;
        }
      }
    });

    const netReceived = totalGrossIncome - totalCommission;

    return {
      totalGrossIncome,
      totalCommission,
      totalWithdrawn,
      netReceived,
      currentBalance: Number(wallet?.balance || 0),
    };
  }, [transactions, wallet]);

  // Handle Withdraw Submission
  const handleWithdrawSubmit = async (e) => {
    e.preventDefault();
    const amount = Number(withdrawAmount);
    if (!withdrawAmount || isNaN(amount) || amount <= 0) {
      toast.error('Số tiền rút không hợp lệ! Vui lòng nhập số tiền lớn hơn 0 đ.');
      return;
    }
    if (amount > metrics.currentBalance) {
      toast.error(`Số tiền rút (${formatVND(amount)}) vượt quá số dư ví khả dụng hiện tại (${formatVND(metrics.currentBalance)})!`);
      return;
    }
    if (!bankAccountNumber.trim()) {
      toast.error('Vui lòng nhập số tài khoản ngân hàng nhận tiền');
      return;
    }
    if (!bankAccountName.trim()) {
      toast.error('Vui lòng nhập tên chủ tài khoản nhận tiền');
      return;
    }

    try {
      setWithdrawing(true);
      await paymentService.withdrawWallet(amount);
      toast.success(`Đã rút thành công ${formatVND(amount)} về tài khoản ngân hàng ${bankName}! Số dư đã được cập nhật.`);
      setWithdrawModalOpen(false);
      setWithdrawAmount('');
      fetchData(false);
    } catch (err) {
      console.error('Lỗi rút tiền:', err);
      toast.error(err.response?.data?.message || 'Yêu cầu rút tiền không thành công');
    } finally {
      setWithdrawing(false);
    }
  };

  // Helper: Match Order Info
  const getTransactionDetails = (tx) => {
    const isIncome = tx.transactionType === 'FOOD_RESTAURANT_INCOME';
    const isFee = tx.transactionType === 'COMMISSION_FEE';
    const isWithdraw = tx.transactionType === 'WITHDRAWAL';

    let title = 'Giao dịch hệ thống';
    let typeChipColor = 'default';
    let sign = '+';
    let amountColor = '#16a34a';

    if (isIncome) {
      title = `Doanh thu tiền món đơn #${tx.bookingId || '--'}`;
      typeChipColor = 'success';
      sign = '+';
      amountColor = '#16a34a';
    } else if (isFee) {
      // Find corresponding income for exact commission %
      let percent = platformFeeRate;
      if (tx.bookingId) {
        const matchedIncome = transactions.find(
          (t) => t.bookingId === tx.bookingId && t.transactionType === 'FOOD_RESTAURANT_INCOME'
        );
        if (matchedIncome && Number(matchedIncome.amount) > 0) {
          percent = Math.round((Number(tx.amount) / Number(matchedIncome.amount)) * 100);
        }
      }
      title = `Khấu trừ phí sàn (${percent}%) đơn #${tx.bookingId || '--'}`;
      typeChipColor = 'warning';
      sign = '-';
      amountColor = '#ea580c';
    } else if (isWithdraw) {
      title = 'Rút tiền về tài khoản ngân hàng';
      typeChipColor = 'info';
      sign = '-';
      amountColor = '#dc2626';
    }

    return { title, typeChipColor, sign, amountColor };
  };

  // Helper: Match Payment Method Display & Badge
  const getMethodBadge = (tx) => {
    if (tx.transactionType === 'WITHDRAWAL') {
      return {
        label: 'Rút tiền',
        color: '#dc2626',
        bg: 'rgba(220, 38, 38, 0.08)',
        border: 'rgba(220, 38, 38, 0.25)',
      };
    }

    // Determine actual method (from matched order or tx)
    const order = tx.bookingId ? ordersMap[tx.bookingId] : null;
    let m = order?.paymentMethod || tx.paymentMethod;
    if (m) m = String(m).toUpperCase();
    if (!m || m === 'WALLET') {
      if (
        tx.orderId?.startsWith('VNP') ||
        (tx.bookingId && transactions.some((t) => t.bookingId === tx.bookingId && t.paymentMethod === 'VNPAY'))
      ) {
        m = 'VNPAY';
      } else if (
        tx.orderId?.startsWith('MOMO') ||
        (tx.bookingId && transactions.some((t) => t.bookingId === tx.bookingId && t.paymentMethod === 'MOMO'))
      ) {
        m = 'MOMO';
      }
    }

    if (m === 'VNPAY') {
      return {
        label: 'Cổng VNPAY',
        color: '#0284c7',
        bg: 'rgba(2, 132, 199, 0.1)',
        border: 'rgba(2, 132, 199, 0.25)',
      };
    }
    if (m === 'MOMO') {
      return {
        label: 'Ví MoMo',
        color: '#c026d3',
        bg: 'rgba(217, 70, 239, 0.1)',
        border: 'rgba(217, 70, 239, 0.25)',
      };
    }
    if (m === 'CASH') {
      return {
        label: 'Tiền mặt',
        color: '#16a34a',
        bg: 'rgba(22, 163, 74, 0.1)',
        border: 'rgba(22, 163, 74, 0.25)',
      };
    }
    return {
      label: 'Ví OmniGo',
      color: '#475569',
      bg: '#F1F5F9',
      border: '#E2E8F0',
    };
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', minHeight: '60vh', gap: 2 }}>
        <CircularProgress size={44} sx={{ color: '#F97316' }} />
        <Typography variant="body2" sx={{ color: '#64748B', fontWeight: 600 }}>
          Đang tải dữ liệu ví doanh thu quán ăn...
        </Typography>
      </Box>
    );
  }

  return (
    <Box sx={{ width: '100%', maxWidth: 1400, mx: 'auto', minWidth: 0 }}>
      {/* HEADER SECTION */}
      <Box
        sx={{
          display: 'flex',
          flexDirection: { xs: 'column', sm: 'row' },
          justifyContent: 'space-between',
          alignItems: { xs: 'flex-start', sm: 'center' },
          gap: 2,
          mb: 3.5,
        }}
      >
        <Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 0.5 }}>
            <Typography variant="h5" sx={{ fontWeight: 800, color: '#0F172A', fontSize: { xs: '1.35rem', sm: '1.6rem' } }}>
              Ví Doanh Thu & Quyết Toán
            </Typography>
            <Chip
              label={`Sàn: ${platformFeeRate}%`}
              size="small"
              sx={{ bgcolor: 'rgba(249, 115, 22, 0.12)', color: '#ea580c', fontWeight: 700, fontSize: '0.78rem' }}
            />
          </Box>
          <Typography variant="body2" sx={{ color: '#64748B', fontSize: '0.88rem' }}>
            {restaurant?.name ? `${restaurant.name} • ` : ''}Quản lý số dư, theo dõi đối soát doanh thu bán món, khấu trừ phí sàn và thực nhận theo từng đơn hàng.
          </Typography>
        </Box>

        <Box sx={{ display: 'flex', gap: 1.5, width: { xs: '100%', sm: 'auto' } }}>
          <Button
            variant="outlined"
            onClick={() => fetchData(true)}
            disabled={refreshing}
            startIcon={<RefreshIcon className={refreshing ? 'spin' : ''} />}
            sx={{
              borderRadius: 2.5,
              textTransform: 'none',
              fontWeight: 700,
              color: '#475569',
              borderColor: '#CBD5E1',
              flex: { xs: 1, sm: 'none' },
              '&:hover': { borderColor: '#94A3B8', bgcolor: '#F8FAFC' },
            }}
          >
            Làm mới
          </Button>

          <Button
            variant="contained"
            onClick={() => setWithdrawModalOpen(true)}
            startIcon={<BankIcon />}
            sx={{
              borderRadius: 2.5,
              textTransform: 'none',
              fontWeight: 700,
              bgcolor: '#16A34A',
              color: '#FFFFFF',
              boxShadow: '0 4px 12px rgba(22, 163, 74, 0.25)',
              flex: { xs: 1, sm: 'none' },
              '&:hover': { bgcolor: '#15803D' },
            }}
          >
            Rút Tiền
          </Button>
        </Box>
      </Box>

      {/* METRIC SUMMARY CARDS */}
      <Grid container spacing={2.5} sx={{ mb: 3.5 }}>
        {/* Card 1: Số dư ví hiện tại */}
        <Grid size={{ xs: 12, sm: 6, lg: 3 }}>
          <Card
            elevation={0}
            sx={{
              p: 2.5,
              borderRadius: 4,
              border: '1px solid #E2E8F0',
              bgcolor: '#FFFFFF',
              position: 'relative',
              overflow: 'hidden',
              boxShadow: '0 4px 20px rgba(0, 0, 0, 0.03)',
            }}
          >
            <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 1.5 }}>
              <Typography variant="body2" sx={{ fontWeight: 700, color: '#64748B' }}>
                Số Dư Ví Khả Dụng
              </Typography>
              <Avatar sx={{ bgcolor: 'rgba(22, 163, 74, 0.12)', color: '#16a34a', width: 38, height: 38 }}>
                <WalletIcon fontSize="small" />
              </Avatar>
            </Box>
            <Typography variant="h4" sx={{ fontWeight: 900, color: '#16a34a', fontSize: '1.75rem', mb: 0.5 }}>
              {formatVND(metrics.currentBalance)}
            </Typography>
            <Typography variant="caption" sx={{ color: '#64748B', display: 'block', fontWeight: 500 }}>
              Sẵn sàng rút về tài khoản ngân hàng
            </Typography>
          </Card>
        </Grid>

        {/* Card 2: Tổng Doanh Thu Tiền Món */}
        <Grid size={{ xs: 12, sm: 6, lg: 3 }}>
          <Card
            elevation={0}
            sx={{
              p: 2.5,
              borderRadius: 4,
              border: '1px solid #E2E8F0',
              bgcolor: '#FFFFFF',
              boxShadow: '0 4px 20px rgba(0, 0, 0, 0.03)',
            }}
          >
            <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 1.5 }}>
              <Typography variant="body2" sx={{ fontWeight: 700, color: '#64748B' }}>
                Tổng Doanh Thu Tiền Món
              </Typography>
              <Avatar sx={{ bgcolor: 'rgba(249, 115, 22, 0.12)', color: '#ea580c', width: 38, height: 38 }}>
                <MonetizationOnIcon fontSize="small" />
              </Avatar>
            </Box>
            <Typography variant="h4" sx={{ fontWeight: 900, color: '#0F172A', fontSize: '1.75rem', mb: 0.5 }}>
              {formatVND(metrics.totalGrossIncome)}
            </Typography>
            <Typography variant="caption" sx={{ color: '#64748B', display: 'block', fontWeight: 500 }}>
              Tiền món khách trả (Chưa trừ phí sàn)
            </Typography>
          </Card>
        </Grid>

        {/* Card 3: Khấu Trừ Phí Sàn */}
        <Grid size={{ xs: 12, sm: 6, lg: 3 }}>
          <Card
            elevation={0}
            sx={{
              p: 2.5,
              borderRadius: 4,
              border: '1px solid #E2E8F0',
              bgcolor: '#FFFFFF',
              boxShadow: '0 4px 20px rgba(0, 0, 0, 0.03)',
            }}
          >
            <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 1.5 }}>
              <Typography variant="body2" sx={{ fontWeight: 700, color: '#64748B' }}>
                Khấu Trừ Phí Sàn ({platformFeeRate}%)
              </Typography>
              <Avatar sx={{ bgcolor: 'rgba(234, 88, 12, 0.12)', color: '#ea580c', width: 38, height: 38 }}>
                <PercentIcon fontSize="small" />
              </Avatar>
            </Box>
            <Typography variant="h4" sx={{ fontWeight: 900, color: '#EA580C', fontSize: '1.75rem', mb: 0.5 }}>
              -{formatVND(metrics.totalCommission)}
            </Typography>
            <Typography variant="caption" sx={{ color: '#64748B', display: 'block', fontWeight: 500 }}>
              Tỉ lệ hoa hồng sàn hiện tại: {platformFeeRate}%
            </Typography>
          </Card>
        </Grid>

        {/* Card 4: Thực Nhận Về Quán */}
        <Grid size={{ xs: 12, sm: 6, lg: 3 }}>
          <Card
            elevation={0}
            sx={{
              p: 2.5,
              borderRadius: 4,
              border: '1px solid #E2E8F0',
              bgcolor: '#FFFFFF',
              boxShadow: '0 4px 20px rgba(0, 0, 0, 0.03)',
            }}
          >
            <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 1.5 }}>
              <Typography variant="body2" sx={{ fontWeight: 700, color: '#64748B' }}>
                Thực Nhận Về Ví Quán
              </Typography>
              <Avatar sx={{ bgcolor: 'rgba(2, 132, 199, 0.12)', color: '#0284c7', width: 38, height: 38 }}>
                <TrendingUpIcon fontSize="small" />
              </Avatar>
            </Box>
            <Typography variant="h4" sx={{ fontWeight: 900, color: '#0284c7', fontSize: '1.75rem', mb: 0.5 }}>
              {formatVND(metrics.netReceived)}
            </Typography>
            <Typography variant="caption" sx={{ color: '#64748B', display: 'block', fontWeight: 500 }}>
              Doanh thu ròng sau khi khấu trừ phí sàn
            </Typography>
          </Card>
        </Grid>
      </Grid>

      {/* FILTER & CONTROLS TOOLBAR */}
      <Card
        elevation={0}
        sx={{
          p: 2.5,
          borderRadius: 4,
          border: '1px solid #E2E8F0',
          bgcolor: '#FFFFFF',
          mb: 3,
          boxShadow: '0 2px 12px rgba(0, 0, 0, 0.02)',
        }}
      >
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          {/* Top row: Quick Time Filter Chips */}
          <Box sx={{ display: 'flex', alignItems: 'center', flexWrap: 'wrap', gap: 1 }}>
            <Typography variant="caption" sx={{ fontWeight: 800, color: '#475569', mr: 1, textTransform: 'uppercase' }}>
              Thời gian:
            </Typography>
            {[
              { id: 'ALL', label: 'Tất cả' },
              { id: 'TODAY', label: 'Hôm nay' },
              { id: 'WEEK', label: '7 ngày qua' },
              { id: 'MONTH', label: 'Tháng này' },
              { id: 'CUSTOM', label: 'Tùy chọn ngày' },
            ].map((btn) => (
              <Chip
                key={btn.id}
                label={btn.label}
                clickable
                onClick={() => setTimeFilter(btn.id)}
                sx={{
                  fontWeight: 700,
                  fontSize: '0.82rem',
                  bgcolor: timeFilter === btn.id ? '#F97316' : '#F1F5F9',
                  color: timeFilter === btn.id ? '#FFFFFF' : '#475569',
                  '&:hover': {
                    bgcolor: timeFilter === btn.id ? '#EA580C' : '#E2E8F0',
                  },
                }}
              />
            ))}
          </Box>

          {/* Date Range Inputs for Custom Filter */}
          {timeFilter === 'CUSTOM' && (
            <Box
              sx={{
                display: 'flex',
                alignItems: 'center',
                flexWrap: 'wrap',
                gap: 2,
                p: 1.5,
                bgcolor: '#F8FAFC',
                borderRadius: 2.5,
                border: '1px solid #E2E8F0',
              }}
            >
              <Typography variant="caption" sx={{ fontWeight: 800, color: '#0F172A', mr: 0.5 }}>
                Khoảng ngày:
              </Typography>
              <TextField
                label="Từ ngày"
                type="date"
                size="small"
                value={customStartDate}
                onChange={(e) => setCustomStartDate(e.target.value)}
                InputLabelProps={{ shrink: true }}
                sx={{ width: 160, '& .MuiOutlinedInput-root': { borderRadius: 2, bgcolor: '#FFFFFF' } }}
              />
              <TextField
                label="Đến ngày"
                type="date"
                size="small"
                value={customEndDate}
                onChange={(e) => setCustomEndDate(e.target.value)}
                InputLabelProps={{ shrink: true }}
                sx={{ width: 160, '& .MuiOutlinedInput-root': { borderRadius: 2, bgcolor: '#FFFFFF' } }}
              />
              {(customStartDate || customEndDate) && (
                <Button
                  size="small"
                  onClick={() => {
                    setCustomStartDate('');
                    setCustomEndDate('');
                    setTimeFilter('ALL');
                  }}
                  sx={{ textTransform: 'none', color: '#EF4444', fontWeight: 600, fontSize: '0.8rem' }}
                >
                  Xóa lọc ngày
                </Button>
              )}
            </Box>
          )}

          <Divider sx={{ my: 0.5 }} />

          {/* Bottom row: Type, Status, Search */}
          <Grid container spacing={2} alignItems="center">
            <Grid size={{ xs: 12, sm: 4, md: 3 }}>
              <FormControl size="small" fullWidth>
                <InputLabel>Loại giao dịch</InputLabel>
                <Select
                  value={typeFilter}
                  label="Loại giao dịch"
                  onChange={(e) => setTypeFilter(e.target.value)}
                  sx={{ borderRadius: 2 }}
                >
                  <MenuItem value="ALL">Tất cả giao dịch</MenuItem>
                  <MenuItem value="FOOD_RESTAURANT_INCOME">Thu nhập bán món (+)</MenuItem>
                  <MenuItem value="COMMISSION_FEE">Khấu trừ phí sàn (-)</MenuItem>
                  <MenuItem value="WITHDRAWAL">Rút tiền ví (-)</MenuItem>
                </Select>
              </FormControl>
            </Grid>

            <Grid size={{ xs: 12, sm: 4, md: 3 }}>
              <FormControl size="small" fullWidth>
                <InputLabel>Trạng thái</InputLabel>
                <Select
                  value={statusFilter}
                  label="Trạng thái"
                  onChange={(e) => setStatusFilter(e.target.value)}
                  sx={{ borderRadius: 2 }}
                >
                  <MenuItem value="ALL">Tất cả trạng thái</MenuItem>
                  <MenuItem value="SUCCESS">Thành công</MenuItem>
                  <MenuItem value="PENDING">Đang xử lý</MenuItem>
                  <MenuItem value="FAILED">Thất bại / Hủy</MenuItem>
                </Select>
              </FormControl>
            </Grid>

            <Grid size={{ xs: 12, sm: 4, md: 6 }}>
              <TextField
                size="small"
                fullWidth
                placeholder="Tìm theo mã đơn #, mã giao dịch..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <SearchIcon fontSize="small" sx={{ color: '#94A3B8' }} />
                    </InputAdornment>
                  ),
                  sx: { borderRadius: 2 },
                }}
              />
            </Grid>
          </Grid>
        </Box>
      </Card>

      {/* TRANSACTIONS TABLE */}
      <Card
        elevation={0}
        sx={{
          borderRadius: 4,
          border: '1px solid #E2E8F0',
          bgcolor: '#FFFFFF',
          overflow: 'hidden',
          boxShadow: '0 4px 20px rgba(0, 0, 0, 0.03)',
        }}
      >
        <Box sx={{ p: 2.5, display: 'flex', alignItems: 'center', justifyContent: 'space-between', borderBottom: '1px solid #F1F5F9' }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <ReceiptIcon sx={{ color: '#F97316' }} />
            <Typography variant="h6" sx={{ fontWeight: 800, color: '#0F172A', fontSize: '1.05rem' }}>
              Lịch Sử Giao Dịch & Khấu Trừ Chi Tiết ({filteredTransactions.length})
            </Typography>
          </Box>
          <Typography variant="caption" sx={{ color: '#64748B', fontWeight: 600 }}>
            Hiển thị {filteredTransactions.length === 0 ? 0 : page * rowsPerPage + 1}–
            {Math.min((page + 1) * rowsPerPage, filteredTransactions.length)} trên tổng số {filteredTransactions.length} giao dịch
          </Typography>
        </Box>

        {filteredTransactions.length === 0 ? (
          <Box sx={{ p: 6, textAlign: 'center' }}>
            <Avatar sx={{ bgcolor: '#F8FAFC', color: '#94A3B8', width: 64, height: 64, mx: 'auto', mb: 2 }}>
              <ReceiptIcon sx={{ fontSize: 32 }} />
            </Avatar>
            <Typography variant="subtitle1" sx={{ fontWeight: 700, color: '#334155', mb: 0.5 }}>
              Chưa có giao dịch phù hợp
            </Typography>
            <Typography variant="body2" sx={{ color: '#64748B' }}>
              Các giao dịch bán món, khấu trừ phí sàn và rút tiền sẽ được ghi nhận tự động tại đây.
            </Typography>
          </Box>
        ) : (
          <TableContainer>
            <Table sx={{ minWidth: 750 }}>
              <TableHead sx={{ bgcolor: '#F8FAFC' }}>
                <TableRow>
                  <TableCell sx={{ fontWeight: 800, color: '#475569', fontSize: '0.82rem' }}>MÃ GIAO DỊCH / ĐƠN</TableCell>
                  <TableCell sx={{ fontWeight: 800, color: '#475569', fontSize: '0.82rem' }}>THỜI GIAN</TableCell>
                  <TableCell sx={{ fontWeight: 800, color: '#475569', fontSize: '0.82rem' }}>LOẠI GIAO DỊCH</TableCell>
                  <TableCell sx={{ fontWeight: 800, color: '#475569', fontSize: '0.82rem' }}>PHƯƠNG THỨC</TableCell>
                  <TableCell align="right" sx={{ fontWeight: 800, color: '#475569', fontSize: '0.82rem' }}>SỐ TIỀN</TableCell>
                  <TableCell align="center" sx={{ fontWeight: 800, color: '#475569', fontSize: '0.82rem' }}>TRẠNG THÁI</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {paginatedTransactions.map((tx) => {
                  const { title, sign, amountColor } = getTransactionDetails(tx);
                  const isSuccess = tx.status === 'SUCCESS';
                  const isPending = tx.status === 'PENDING';
                  const methodBadge = getMethodBadge(tx);

                  return (
                    <TableRow key={tx.id} hover sx={{ '&:last-child td, &:last-child th': { border: 0 } }}>
                      {/* Mã Giao Dịch & Đơn */}
                      <TableCell>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                          {tx.bookingId && (
                            <Chip
                              label={`Đơn #${tx.bookingId}`}
                              size="small"
                              sx={{
                                fontWeight: 800,
                                fontSize: '0.75rem',
                                bgcolor: 'rgba(249, 115, 22, 0.1)',
                                color: '#ea580c',
                                cursor: 'pointer',
                              }}
                              onClick={() => navigate('/merchant/orders')}
                            />
                          )}
                          <Typography variant="body2" sx={{ fontWeight: 600, color: '#1E293B', fontSize: '0.85rem' }}>
                            {tx.orderId || `TX-${tx.id}`}
                          </Typography>
                        </Box>
                      </TableCell>

                      {/* Thời Gian */}
                      <TableCell>
                        <Typography variant="body2" sx={{ color: '#475569', fontSize: '0.84rem', fontWeight: 500 }}>
                          {tx.createdAt ? formatVietnamDateTime(tx.createdAt) : '--'}
                        </Typography>
                      </TableCell>

                      {/* Loại Giao Dịch */}
                      <TableCell>
                        <Typography variant="body2" sx={{ fontWeight: 700, color: '#0F172A', fontSize: '0.86rem' }}>
                          {title}
                        </Typography>
                        <Typography variant="caption" sx={{ color: '#64748B' }}>
                          {tx.transactionType === 'FOOD_RESTAURANT_INCOME'
                            ? 'Thu nhập từ tiền món đơn hoàn tất'
                            : tx.transactionType === 'COMMISSION_FEE'
                            ? 'Khấu trừ tự động vào ví theo cấu hình sàn'
                            : tx.transactionType === 'WITHDRAWAL'
                            ? 'Chuyển tiền về tài khoản ngân hàng'
                            : tx.transactionType}
                        </Typography>
                      </TableCell>

                      {/* Phương Thức */}
                      <TableCell>
                        <Chip
                          label={methodBadge.label}
                          size="small"
                          sx={{
                            fontWeight: 700,
                            fontSize: '0.75rem',
                            bgcolor: methodBadge.bg,
                            color: methodBadge.color,
                            border: '1px solid',
                            borderColor: methodBadge.border,
                          }}
                        />
                      </TableCell>

                      {/* Số Tiền */}
                      <TableCell align="right">
                        <Typography
                          variant="body1"
                          sx={{
                            fontWeight: 800,
                            color: amountColor,
                            fontSize: '0.98rem',
                          }}
                        >
                          {sign}
                          {formatVND(tx.amount)}
                        </Typography>
                      </TableCell>

                      {/* Trạng Thái */}
                      <TableCell align="center">
                        <Chip
                          icon={
                            isSuccess ? (
                              <SuccessIcon sx={{ fontSize: '14px !important', color: '#16a34a !important' }} />
                            ) : isPending ? (
                              <PendingIcon sx={{ fontSize: '14px !important', color: '#d97706 !important' }} />
                            ) : (
                              <FailedIcon sx={{ fontSize: '14px !important', color: '#dc2626 !important' }} />
                            )
                          }
                          label={isSuccess ? 'Thành công' : isPending ? 'Đang xử lý' : 'Thất bại'}
                          size="small"
                          sx={{
                            fontWeight: 700,
                            fontSize: '0.74rem',
                            bgcolor: isSuccess ? '#DCFCE7' : isPending ? '#FEF3C7' : '#FEE2E2',
                            color: isSuccess ? '#15803D' : isPending ? '#B45309' : '#B91C1C',
                          }}
                        />
                      </TableCell>
                    </TableRow>
                  );
                })}
              </TableBody>
            </Table>
          </TableContainer>
        )}

        {filteredTransactions.length > 0 && (
          <TablePagination
            rowsPerPageOptions={[5, 10, 20, 50]}
            component="div"
            count={filteredTransactions.length}
            rowsPerPage={rowsPerPage}
            page={page}
            onPageChange={(_, newPage) => setPage(newPage)}
            onRowsPerPageChange={(e) => {
              setRowsPerPage(parseInt(e.target.value, 10));
              setPage(0);
            }}
            labelRowsPerPage="Số dòng mỗi trang:"
            labelDisplayedRows={({ from, to, count }) =>
              `${from}–${to} trên tổng số ${count !== -1 ? count : `hơn ${to}`} giao dịch`
            }
            sx={{
              borderTop: '1px solid #F1F5F9',
              bgcolor: '#FFFFFF',
              borderBottomLeftRadius: 16,
              borderBottomRightRadius: 16,
              '& .MuiTablePagination-selectLabel, & .MuiTablePagination-displayedRows': {
                fontWeight: 600,
                color: '#64748B',
                fontSize: '0.85rem',
              },
            }}
          />
        )}
      </Card>

      {/* WITHDRAW MODAL */}
      <Dialog
        open={withdrawModalOpen}
        onClose={() => !withdrawing && setWithdrawModalOpen(false)}
        maxWidth="sm"
        fullWidth
        PaperProps={{ sx: { borderRadius: 4, p: 1 } }}
      >
        <DialogTitle sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', pb: 1 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
            <Avatar sx={{ bgcolor: 'rgba(22, 163, 74, 0.12)', color: '#16a34a' }}>
              <BankIcon />
            </Avatar>
            <Box>
              <Typography variant="h6" sx={{ fontWeight: 800, color: '#0F172A', fontSize: '1.15rem' }}>
                Rút Tiền Về Tài Khoản Ngân Hàng
              </Typography>
              <Typography variant="caption" sx={{ color: '#64748B' }}>
                Số dư ví khả dụng: <b style={{ color: '#16a34a' }}>{formatVND(metrics.currentBalance)}</b>
              </Typography>
            </Box>
          </Box>
          <IconButton onClick={() => setWithdrawModalOpen(false)} disabled={withdrawing}>
            <CloseIcon fontSize="small" />
          </IconButton>
        </DialogTitle>

        <form onSubmit={handleWithdrawSubmit}>
          <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2.5, pt: 1.5 }}>
            {/* Quick Amount Selector */}
            <Box>
              <Typography variant="caption" sx={{ fontWeight: 700, color: '#475569', display: 'block', mb: 1 }}>
                Chọn nhanh số tiền rút:
              </Typography>
              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                {[10000, 20000, 50000, 100000, 200000].map((amt) => (
                  <Chip
                    key={amt}
                    label={formatVND(amt)}
                    clickable
                    disabled={amt > metrics.currentBalance}
                    onClick={() => setWithdrawAmount(String(amt))}
                    sx={{
                      fontWeight: 700,
                      fontSize: '0.8rem',
                      bgcolor: Number(withdrawAmount) === amt ? 'rgba(22, 163, 74, 0.15)' : '#F1F5F9',
                      color: Number(withdrawAmount) === amt ? '#16a34a' : '#475569',
                      borderColor: Number(withdrawAmount) === amt ? '#16a34a' : 'transparent',
                      borderWidth: 1,
                      borderStyle: 'solid',
                    }}
                  />
                ))}
                <Chip
                  label={`Tất cả số dư (${formatVND(metrics.currentBalance)})`}
                  clickable
                  disabled={metrics.currentBalance <= 0}
                  onClick={() => setWithdrawAmount(String(Math.floor(metrics.currentBalance)))}
                  sx={{
                    fontWeight: 700,
                    fontSize: '0.8rem',
                    bgcolor: 'rgba(249, 115, 22, 0.12)',
                    color: '#ea580c',
                    border: '1px solid #fed7aa',
                  }}
                />
              </Box>
            </Box>

            {/* Input Amount */}
            <TextField
              label="Số tiền muốn rút (VND) *"
              type="number"
              fullWidth
              value={withdrawAmount}
              onChange={(e) => setWithdrawAmount(e.target.value)}
              placeholder="VD: 50000"
              error={
                Boolean(withdrawAmount) &&
                (Number(withdrawAmount) <= 0 || Number(withdrawAmount) > metrics.currentBalance)
              }
              helperText={
                Boolean(withdrawAmount) && Number(withdrawAmount) <= 0
                  ? 'Số tiền rút phải lớn hơn 0 đ'
                  : Boolean(withdrawAmount) && Number(withdrawAmount) > metrics.currentBalance
                  ? `Số tiền vượt quá số dư ví khả dụng (${formatVND(metrics.currentBalance)})`
                  : `Số tiền tối đa có thể rút: ${formatVND(metrics.currentBalance)}`
              }
              InputProps={{
                endAdornment: <InputAdornment position="end">VND</InputAdornment>,
                sx: { borderRadius: 2.5 },
              }}
            />

            {/* Bank Select */}
            <FormControl fullWidth>
              <InputLabel>Ngân hàng nhận tiền *</InputLabel>
              <Select
                value={bankName}
                label="Ngân hàng nhận tiền *"
                onChange={(e) => setBankName(e.target.value)}
                sx={{ borderRadius: 2.5 }}
              >
                <MenuItem value="Vietcombank">Vietcombank (Ngoại thương Việt Nam)</MenuItem>
                <MenuItem value="Techcombank">Techcombank (Kỹ thương Việt Nam)</MenuItem>
                <MenuItem value="MBBank">MBBank (Quân Đội)</MenuItem>
                <MenuItem value="VietinBank">VietinBank (Công thương Việt Nam)</MenuItem>
                <MenuItem value="BIDV">BIDV (Đầu tư & Phát triển Việt Nam)</MenuItem>
                <MenuItem value="ACB">ACB (Á Châu)</MenuItem>
                <MenuItem value="VPBank">VPBank (Việt Nam Thịnh Vượng)</MenuItem>
                <MenuItem value="TPBank">TPBank (Tiên Phong)</MenuItem>
              </Select>
            </FormControl>

            {/* Account Number */}
            <TextField
              label="Số tài khoản ngân hàng *"
              fullWidth
              value={bankAccountNumber}
              onChange={(e) => setBankAccountNumber(e.target.value.trim())}
              placeholder="VD: 1029384756"
              InputProps={{ sx: { borderRadius: 2.5 } }}
            />

            {/* Account Name */}
            <TextField
              label="Tên chủ tài khoản (In hoa không dấu) *"
              fullWidth
              value={bankAccountName}
              onChange={(e) => setBankAccountName(e.target.value.toUpperCase())}
              placeholder="VD: NGUYEN VAN A"
              InputProps={{ sx: { borderRadius: 2.5 } }}
            />
          </DialogContent>

          <DialogActions sx={{ p: 2.5, pt: 1 }}>
            <Button
              onClick={() => setWithdrawModalOpen(false)}
              disabled={withdrawing}
              sx={{ borderRadius: 2.5, textTransform: 'none', fontWeight: 700, color: '#64748B' }}
            >
              Hủy
            </Button>
            <Button
              type="submit"
              variant="contained"
              disabled={withdrawing || !withdrawAmount}
              startIcon={withdrawing ? <CircularProgress size={16} color="inherit" /> : <BankIcon />}
              sx={{
                borderRadius: 2.5,
                textTransform: 'none',
                fontWeight: 700,
                bgcolor: '#16A34A',
                '&:hover': { bgcolor: '#15803D' },
              }}
            >
              {withdrawing ? 'Đang xử lý rút...' : 'Xác Nhận Rút Tiền'}
            </Button>
          </DialogActions>
        </form>
      </Dialog>
    </Box>
  );
};

export default MerchantWallet;
