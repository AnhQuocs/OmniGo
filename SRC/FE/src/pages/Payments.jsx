import React, { useState, useEffect } from 'react';
import {
  Box,
  Card,
  Grid,
  Typography,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TablePagination,
  Chip,
  TextField,
  InputAdornment,
  CircularProgress,
  Alert,
  Button,
  ButtonGroup,
} from '@mui/material';
import {
  Search as SearchIcon,
  Refresh as RefreshIcon,
  Payment as PaymentIcon,
  AccountBalanceWallet as WalletIcon,
  CheckCircle as SuccessIcon,
  AttachMoney as MoneyIcon,
} from '@mui/icons-material';
import { useLocation, useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import paymentService from '../services/paymentService';

export const Payments = () => {
  const location = useLocation();
  const navigate = useNavigate();

  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [searchTerm, setSearchTerm] = useState('');
  const [typeFilter, setTypeFilter] = useState('ALL');

  // Sync filter from URL query param
  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const typeParam = params.get('type');
    const gatewayParam = params.get('gateway');

    if (gatewayParam === 'ONLINE') {
      setTypeFilter('ONLINE_GATEWAY');
    } else if (typeParam) {
      setTypeFilter(typeParam.toUpperCase());
    } else {
      setTypeFilter('ALL');
    }
    setPage(0);
  }, [location.search]);

  const fetchTransactions = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await paymentService.getAllTransactions();
      setTransactions(Array.isArray(data) ? data : []);
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Không thể tải danh sách giao dịch từ Backend';
      setError(msg);
      toast.error(msg);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTransactions();
  }, []);

  const handleTabChange = (val) => {
    setTypeFilter(val);
    setPage(0);
    if (val === 'ALL') {
      navigate('/payments');
    } else if (val === 'ONLINE_GATEWAY') {
      navigate('/payments?gateway=ONLINE');
    } else {
      navigate(`/payments?type=${val}`);
    }
  };

  const getStatusChip = (status) => {
    switch (status) {
      case 'SUCCESS':
        return <Chip label="Thành Công" size="small" sx={{ bgcolor: 'rgba(21, 202, 32, 0.15)', color: '#15ca20', fontWeight: 700 }} />;
      case 'FAILED':
        return <Chip label="Thất Bại" size="small" sx={{ bgcolor: 'rgba(255, 51, 102, 0.15)', color: '#ff3366', fontWeight: 700 }} />;
      case 'PENDING':
      default:
        return <Chip label="Đang Xử Lý" size="small" sx={{ bgcolor: 'rgba(255, 184, 0, 0.15)', color: '#ffb800', fontWeight: 700 }} />;
    }
  };

  const formatDateTime = (dateStr) => {
    if (!dateStr) return '—';
    try {
      const d = new Date(dateStr);
      return d.toLocaleString('vi-VN', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
    } catch {
      return dateStr;
    }
  };

  const totalCount = transactions.length;
  const successCount = transactions.filter((t) => t.status === 'SUCCESS').length;
  const totalVolume = transactions
    .filter((t) => t.status === 'SUCCESS' && t.amount)
    .reduce((sum, t) => sum + Number(t.amount), 0);
  const tripPaymentsCount = transactions.filter((t) => t.transactionType === 'TRIP_PAYMENT').length;

  const filteredTransactions = transactions.filter((t) => {
    const term = searchTerm.toLowerCase();
    const matchSearch =
      (t.id && String(t.id).includes(term)) ||
      (t.orderId && t.orderId.toLowerCase().includes(term)) ||
      (t.gatewayTransId && t.gatewayTransId.toLowerCase().includes(term)) ||
      (t.paymentMethod && t.paymentMethod.toLowerCase().includes(term)) ||
      (t.transactionType && t.transactionType.toLowerCase().includes(term));

    if (!matchSearch) return false;
    if (typeFilter === 'ALL') return true;
    if (typeFilter === 'ONLINE_GATEWAY') return ['VNPAY', 'MOMO'].includes(t.paymentMethod);
    if (typeFilter === 'WALLET') return ['WALLET', 'TOPUP', 'WITHDRAW'].includes(t.transactionType) || t.paymentMethod === 'WALLET';
    return t.transactionType === typeFilter;
  });

  const paginatedTransactions = filteredTransactions.slice(
    page * rowsPerPage,
    page * rowsPerPage + rowsPerPage
  );

  return (
    <Box sx={{ width: '100%' }} className="page-enter-animation">
      {/* Header */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2.5, flexWrap: 'wrap', gap: 1.5 }}>
        <Box>
          <Typography variant="h5" sx={{ fontWeight: 700, fontFamily: '"Poppins", sans-serif' }}>
            Quản Lý Giao Dịch & Thanh Toán
          </Typography>
          <Typography variant="body2" sx={{ color: 'text.secondary', mt: 0.2 }}>
            Đối soát dòng tiền, nạp ví và cổng VNPay / MoMo từ `payment-service`
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={<RefreshIcon />}
          onClick={fetchTransactions}
          disabled={loading}
          sx={{ bgcolor: '#008cff', '&:hover': { bgcolor: '#0070cc' }, borderRadius: 2 }}
        >
          Làm Mới Dữ Liệu
        </Button>
      </Box>

      {/* 4 Summary Stats Mini Cards */}
      <Grid container spacing={2} sx={{ mb: 2.5 }}>
        <Grid item xs={6} sm={3}>
          <Card sx={{ p: 2, display: 'flex', alignItems: 'center', gap: 1.5 }}>
            <Box sx={{ p: 1.2, borderRadius: 2, bgcolor: 'rgba(0, 140, 255, 0.1)', color: '#008cff' }}>
              <PaymentIcon />
            </Box>
            <Box>
              <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600 }}>TỔNG GIAO DỊCH</Typography>
              <Typography variant="h6" sx={{ fontWeight: 800 }}>{totalCount}</Typography>
            </Box>
          </Card>
        </Grid>
        <Grid item xs={6} sm={3}>
          <Card sx={{ p: 2, display: 'flex', alignItems: 'center', gap: 1.5 }}>
            <Box sx={{ p: 1.2, borderRadius: 2, bgcolor: 'rgba(21, 202, 32, 0.15)', color: '#15ca20' }}>
              <SuccessIcon />
            </Box>
            <Box>
              <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600 }}>THÀNH CÔNG</Typography>
              <Typography variant="h6" sx={{ fontWeight: 800, color: '#15ca20' }}>{successCount}</Typography>
            </Box>
          </Card>
        </Grid>
        <Grid item xs={6} sm={3}>
          <Card sx={{ p: 2, display: 'flex', alignItems: 'center', gap: 1.5 }}>
            <Box sx={{ p: 1.2, borderRadius: 2, bgcolor: 'rgba(255, 51, 102, 0.15)', color: '#ff3366' }}>
              <MoneyIcon />
            </Box>
            <Box>
              <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600 }}>TỔNG GMV DÒNG TIỀN</Typography>
              <Typography variant="h6" sx={{ fontWeight: 800, color: '#ff3366' }}>
                {Number(totalVolume).toLocaleString('vi-VN')} đ
              </Typography>
            </Box>
          </Card>
        </Grid>
        <Grid item xs={6} sm={3}>
          <Card sx={{ p: 2, display: 'flex', alignItems: 'center', gap: 1.5 }}>
            <Box sx={{ p: 1.2, borderRadius: 2, bgcolor: 'rgba(255, 184, 0, 0.15)', color: '#ffb800' }}>
              <WalletIcon />
            </Box>
            <Box>
              <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600 }}>CUỐC XE ĐÃ TRẢ</Typography>
              <Typography variant="h6" sx={{ fontWeight: 800, color: '#d97706' }}>{tripPaymentsCount}</Typography>
            </Box>
          </Card>
        </Grid>
      </Grid>

      {error && (
        <Alert severity="error" sx={{ mb: 2.5 }}>
          {error}
        </Alert>
      )}

      {/* Main Table Card */}
      <Card sx={{ p: 2.5 }}>
        {/* Filter buttons & Search */}
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2.5, flexWrap: 'wrap', gap: 2 }}>
          <ButtonGroup variant="outlined">
            {[
              { label: 'Tất Cả', value: 'ALL', count: totalCount },
              { label: 'Thanh Toán Cuốc', value: 'TRIP_PAYMENT', count: tripPaymentsCount },
              { label: 'Giao Dịch Ví', value: 'WALLET', count: transactions.filter((t) => ['WALLET', 'TOPUP', 'WITHDRAW'].includes(t.transactionType) || t.paymentMethod === 'WALLET').length },
              { label: 'Cổng VNPAY / MoMo', value: 'ONLINE_GATEWAY', count: transactions.filter((t) => ['VNPAY', 'MOMO'].includes(t.paymentMethod)).length },
            ].map((tab) => (
              <Button
                key={tab.value}
                variant={typeFilter === tab.value ? 'contained' : 'outlined'}
                onClick={() => handleTabChange(tab.value)}
                sx={{
                  px: 2,
                  py: 0.8,
                  fontWeight: 600,
                  fontSize: '0.85rem',
                  bgcolor: typeFilter === tab.value ? '#008cff' : 'transparent',
                }}
              >
                {tab.label} ({tab.count})
              </Button>
            ))}
          </ButtonGroup>

          <Box sx={{ maxWidth: 340, width: '100%' }}>
            <TextField
              size="small"
              fullWidth
              placeholder="Tìm theo Mã đơn, Cổng, Loại giao dịch..."
              value={searchTerm}
              onChange={(e) => {
                setSearchTerm(e.target.value);
                setPage(0);
              }}
              slotProps={{
                input: {
                  startAdornment: (
                    <InputAdornment position="start">
                      <SearchIcon sx={{ color: 'text.secondary', fontSize: 18 }} />
                    </InputAdornment>
                  ),
                },
              }}
            />
          </Box>
        </Box>

        {/* Payments Data Table with Complete Columns */}
        <TableContainer>
          <Table sx={{ minWidth: 950 }}>
            <TableHead>
              <TableRow>
                <TableCell sx={{ width: 80 }}>MÃ GD</TableCell>
                <TableCell>MÃ ĐƠN HÀNG (ORDER ID)</TableCell>
                <TableCell>MÃ CUỐC XE</TableCell>
                <TableCell>MÃ VÍ</TableCell>
                <TableCell align="right">SỐ TIỀN</TableCell>
                <TableCell>LOẠI GIAO DỊCH</TableCell>
                <TableCell>CỔNG THANH TOÁN</TableCell>
                <TableCell>MÃ GD CỔNG</TableCell>
                <TableCell>TRẠNG THÁI</TableCell>
                <TableCell>NGÀY TẠO</TableCell>
                <TableCell>CẬP NHẬT</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {loading ? (
                <TableRow>
                  <TableCell colSpan={11} align="center" sx={{ py: 6 }}>
                    <CircularProgress size={28} />
                    <Typography variant="body2" sx={{ mt: 1, color: 'text.secondary' }}>
                      Đang tải danh sách giao dịch tài chính từ Backend...
                    </Typography>
                  </TableCell>
                </TableRow>
              ) : paginatedTransactions.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={11} align="center" sx={{ py: 5, color: 'text.secondary' }}>
                    {searchTerm ? 'Không tìm thấy giao dịch phù hợp với từ khóa' : 'Chưa có dữ liệu giao dịch nào trong hệ thống'}
                  </TableCell>
                </TableRow>
              ) : (
                paginatedTransactions.map((tx) => (
                  <TableRow key={tx.id} hover>
                    <TableCell sx={{ fontWeight: 800, fontFamily: 'monospace', color: '#008cff' }}>
                      #{tx.id}
                    </TableCell>
                    <TableCell sx={{ fontFamily: 'monospace', fontWeight: 600 }}>
                      {tx.orderId}
                    </TableCell>
                    <TableCell sx={{ color: 'text.primary' }}>
                      {tx.bookingId ? `Cuốc #${tx.bookingId}` : '—'}
                    </TableCell>
                    <TableCell sx={{ color: 'text.secondary' }}>
                      {tx.walletId ? `Ví #${tx.walletId}` : '—'}
                    </TableCell>
                    <TableCell align="right" sx={{ fontWeight: 800, color: 'text.primary' }}>
                      {tx.amount ? `${Number(tx.amount).toLocaleString('vi-VN')} đ` : '—'}
                    </TableCell>
                    <TableCell>
                      <Chip
                        label={tx.transactionType || 'TRIP_PAYMENT'}
                        size="small"
                        sx={{ fontWeight: 600, fontSize: '0.72rem' }}
                      />
                    </TableCell>
                    <TableCell>
                      <Chip
                        label={tx.paymentMethod || 'VNPAY'}
                        size="small"
                        color={tx.paymentMethod === 'MOMO' ? 'secondary' : 'primary'}
                        variant="outlined"
                        sx={{ fontWeight: 700, fontSize: '0.72rem' }}
                      />
                    </TableCell>
                    <TableCell sx={{ fontFamily: 'monospace', fontSize: '0.8rem', color: 'text.secondary' }}>
                      {tx.gatewayTransId || '—'}
                    </TableCell>
                    <TableCell>
                      {getStatusChip(tx.status)}
                    </TableCell>
                    <TableCell sx={{ fontSize: '0.8rem', color: 'text.secondary', whiteSpace: 'nowrap' }}>
                      {formatDateTime(tx.createdAt)}
                    </TableCell>
                    <TableCell sx={{ fontSize: '0.8rem', color: 'text.secondary', whiteSpace: 'nowrap' }}>
                      {formatDateTime(tx.updatedAt)}
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </TableContainer>

        <TablePagination
          rowsPerPageOptions={[5, 10, 25, 50]}
          component="div"
          count={filteredTransactions.length}
          rowsPerPage={rowsPerPage}
          page={page}
          onPageChange={(_, newPage) => setPage(newPage)}
          onRowsPerPageChange={(e) => {
            setRowsPerPage(parseInt(e.target.value, 10));
            setPage(0);
          }}
          labelRowsPerPage="Dòng trên trang:"
          labelDisplayedRows={({ from, to, count }) => `${from}-${to} trên tổng ${count}`}
        />
      </Card>
    </Box>
  );
};

export default Payments;
