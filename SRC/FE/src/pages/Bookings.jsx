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
  Tooltip,
} from '@mui/material';
import {
  Search as SearchIcon,
  Refresh as RefreshIcon,
  DirectionsCar as RideIcon,
  CheckCircle as CompletedIcon,
  Schedule as PendingIcon,
  Cancel as CancelIcon,
  LocationOn as LocationIcon,
  FiberManualRecord as DotIcon,
} from '@mui/icons-material';
import { useLocation, useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import bookingService from '../services/bookingService';

export const Bookings = () => {
  const location = useLocation();
  const navigate = useNavigate();

  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [autoSync, setAutoSync] = useState(true);

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const statusParam = params.get('status');
    if (statusParam) {
      setStatusFilter(statusParam.toUpperCase());
    } else {
      setStatusFilter('ALL');
    }
    setPage(0);
  }, [location.search]);

  const fetchBookings = async (isBackground = false) => {
    if (!isBackground) setLoading(true);
    setError(null);
    try {
      const data = await bookingService.getAllBookings();
      setBookings(Array.isArray(data) ? data : []);
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Không thể tải danh sách chuyến xe từ Backend';
      if (!isBackground) {
        setError(msg);
        toast.error(msg);
      }
    } finally {
      if (!isBackground) setLoading(false);
    }
  };

  useEffect(() => {
    fetchBookings();
  }, []);

  // Realtime auto-sync every 10s
  useEffect(() => {
    if (!autoSync) return;
    const interval = setInterval(() => {
      fetchBookings(true);
    }, 10000);
    return () => clearInterval(interval);
  }, [autoSync]);

  const handleTabChange = (val) => {
    setStatusFilter(val);
    setPage(0);
    if (val === 'ALL') {
      navigate('/bookings');
    } else {
      navigate(`/bookings?status=${val}`);
    }
  };

  const getStatusChip = (status) => {
    switch (status) {
      case 'COMPLETED':
        return <Chip label="Hoàn Thành" size="small" sx={{ bgcolor: 'rgba(21, 202, 32, 0.15)', color: '#15ca20', fontWeight: 700 }} />;
      case 'IN_PROGRESS':
        return <Chip label="Đang Di Chuyển" size="small" sx={{ bgcolor: 'rgba(0, 140, 255, 0.15)', color: '#008cff', fontWeight: 700 }} />;
      case 'ACCEPTED':
        return <Chip label="Tài Xế Đã Nhận" size="small" sx={{ bgcolor: 'rgba(56, 189, 248, 0.15)', color: '#0284c7', fontWeight: 700 }} />;
      case 'ARRIVED':
        return <Chip label="Tài Xế Đã Đến" size="small" sx={{ bgcolor: 'rgba(99, 102, 241, 0.15)', color: '#6366f1', fontWeight: 700 }} />;
      case 'CANCELLED':
        return <Chip label="Đã Hủy" size="small" sx={{ bgcolor: 'rgba(255, 51, 102, 0.15)', color: '#ff3366', fontWeight: 700 }} />;
      case 'PENDING':
      default:
        return <Chip label="Chờ Ghép Xe" size="small" sx={{ bgcolor: 'rgba(255, 184, 0, 0.15)', color: '#ffb800', fontWeight: 700 }} />;
    }
  };

  const formatCoordinate = (lat, lng) => {
    if (lat === undefined || lat === null || lng === undefined || lng === null) return '—';
    return `${Number(lat).toFixed(4)}, ${Number(lng).toFixed(4)}`;
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

  const totalCount = bookings.length;
  const completedCount = bookings.filter((b) => b.status === 'COMPLETED').length;
  const activeCount = bookings.filter((b) => ['PENDING', 'ACCEPTED', 'ARRIVED', 'IN_PROGRESS'].includes(b.status)).length;
  const cancelledCount = bookings.filter((b) => b.status === 'CANCELLED').length;

  const filteredBookings = bookings.filter((b) => {
    const term = searchTerm.toLowerCase();
    const matchSearch =
      (b.id && String(b.id).includes(term)) ||
      (b.customerId && String(b.customerId).includes(term)) ||
      (b.driverId && String(b.driverId).includes(term)) ||
      (b.paymentMethod && b.paymentMethod.toLowerCase().includes(term));

    if (!matchSearch) return false;
    if (statusFilter === 'ALL') return true;
    if (statusFilter === 'ACTIVE') return ['PENDING', 'ACCEPTED', 'ARRIVED', 'IN_PROGRESS'].includes(b.status);
    return b.status === statusFilter;
  });

  const paginatedBookings = filteredBookings.slice(
    page * rowsPerPage,
    page * rowsPerPage + rowsPerPage
  );

  return (
    <Box sx={{ width: '100%' }} className="page-enter-animation">
      {/* Header bar with Realtime Indicator & Refresh */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2.5, flexWrap: 'wrap', gap: 1.5 }}>
        <Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.2 }}>
            <Typography variant="h5" sx={{ fontWeight: 700, fontFamily: '"Poppins", sans-serif' }}>
              Quản Lý Chuyến Xe (Bookings)
            </Typography>
            <Chip
              icon={<DotIcon sx={{ fontSize: '10px !important', color: autoSync ? '#15ca20 !important' : '#94a3b8 !important' }} />}
              label={autoSync ? 'REALTIME LIVE' : 'SYNC TẮT'}
              size="small"
              className={autoSync ? 'realtime-live-pulse' : ''}
              sx={{
                bgcolor: autoSync ? 'rgba(21, 202, 32, 0.12)' : 'rgba(148, 163, 184, 0.12)',
                color: autoSync ? '#15ca20' : 'text.secondary',
                fontWeight: 800,
                fontSize: '0.72rem',
              }}
            />
          </Box>
          <Typography variant="body2" sx={{ color: 'text.secondary', mt: 0.2 }}>
            Theo dõi trạng thái, lộ trình và điều phối chuyến xe từ `booking-service`
          </Typography>
        </Box>

        <Button
          variant="contained"
          startIcon={<RefreshIcon />}
          onClick={() => fetchBookings(false)}
          disabled={loading}
          sx={{ bgcolor: '#008cff', '&:hover': { bgcolor: '#0070cc' }, borderRadius: 2 }}
        >
          Làm Mới
        </Button>
      </Box>

      {/* 4 Summary Stats Mini Cards */}
      <Grid container spacing={2} sx={{ mb: 2.5 }}>
        <Grid item xs={6} sm={3}>
          <Card sx={{ p: 2, display: 'flex', alignItems: 'center', gap: 1.5 }}>
            <Box sx={{ p: 1.2, borderRadius: 2, bgcolor: 'rgba(0, 140, 255, 0.1)', color: '#008cff' }}>
              <RideIcon />
            </Box>
            <Box>
              <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600 }}>TỔNG CUỐC XE</Typography>
              <Typography variant="h6" sx={{ fontWeight: 800 }}>{totalCount}</Typography>
            </Box>
          </Card>
        </Grid>
        <Grid item xs={6} sm={3}>
          <Card sx={{ p: 2, display: 'flex', alignItems: 'center', gap: 1.5 }}>
            <Box sx={{ p: 1.2, borderRadius: 2, bgcolor: 'rgba(0, 140, 255, 0.15)', color: '#008cff' }}>
              <PendingIcon />
            </Box>
            <Box>
              <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600 }}>ĐANG CHẠY</Typography>
              <Typography variant="h6" sx={{ fontWeight: 800, color: '#008cff' }}>{activeCount}</Typography>
            </Box>
          </Card>
        </Grid>
        <Grid item xs={6} sm={3}>
          <Card sx={{ p: 2, display: 'flex', alignItems: 'center', gap: 1.5 }}>
            <Box sx={{ p: 1.2, borderRadius: 2, bgcolor: 'rgba(21, 202, 32, 0.15)', color: '#15ca20' }}>
              <CompletedIcon />
            </Box>
            <Box>
              <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600 }}>HOÀN THÀNH</Typography>
              <Typography variant="h6" sx={{ fontWeight: 800, color: '#15ca20' }}>{completedCount}</Typography>
            </Box>
          </Card>
        </Grid>
        <Grid item xs={6} sm={3}>
          <Card sx={{ p: 2, display: 'flex', alignItems: 'center', gap: 1.5 }}>
            <Box sx={{ p: 1.2, borderRadius: 2, bgcolor: 'rgba(255, 51, 102, 0.15)', color: '#ff3366' }}>
              <CancelIcon />
            </Box>
            <Box>
              <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600 }}>ĐÃ HỦY</Typography>
              <Typography variant="h6" sx={{ fontWeight: 800, color: '#ff3366' }}>{cancelledCount}</Typography>
            </Box>
          </Card>
        </Grid>
      </Grid>

      {error && (
        <Alert severity="error" sx={{ mb: 2.5 }}>
          {error}
        </Alert>
      )}

      {/* Main Table View */}
      <Card sx={{ p: 2.5 }}>
        {/* Filter buttons & Search */}
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2.5, flexWrap: 'wrap', gap: 2 }}>
          <ButtonGroup variant="outlined">
            {[
              { label: 'Tất Cả', value: 'ALL', count: totalCount },
              { label: 'Đang Chạy', value: 'ACTIVE', count: activeCount },
              { label: 'Hoàn Thành', value: 'COMPLETED', count: completedCount },
              { label: 'Đã Hủy', value: 'CANCELLED', count: cancelledCount },
            ].map((tab) => (
              <Button
                key={tab.value}
                variant={statusFilter === tab.value ? 'contained' : 'outlined'}
                onClick={() => handleTabChange(tab.value)}
                sx={{
                  px: 2,
                  py: 0.8,
                  fontWeight: 600,
                  fontSize: '0.85rem',
                  bgcolor: statusFilter === tab.value ? '#008cff' : 'transparent',
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
              placeholder="Tìm theo Mã ID, Khách, Tài xế..."
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

        <TableContainer>
          <Table sx={{ minWidth: 950 }}>
            <TableHead>
              <TableRow>
                <TableCell sx={{ width: 80 }}>MÃ CUỐC</TableCell>
                <TableCell>KHÁCH HÀNG</TableCell>
                <TableCell>TÀI XẾ</TableCell>
                <TableCell>ĐIỂM ĐÓN (TỌA ĐỘ)</TableCell>
                <TableCell>ĐIỂM ĐẾN (TỌA ĐỘ)</TableCell>
                <TableCell align="center">QUÃNG ĐƯỜNG</TableCell>
                <TableCell align="right">TIỀN CƯỚC</TableCell>
                <TableCell>THANH TOÁN</TableCell>
                <TableCell>TRẠNG THÁI</TableCell>
                <TableCell>THỜI GIAN ĐẶT</TableCell>
                <TableCell>HOÀN THÀNH</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {loading ? (
                <TableRow>
                  <TableCell colSpan={11} align="center" sx={{ py: 6 }}>
                    <CircularProgress size={28} />
                    <Typography variant="body2" sx={{ mt: 1, color: 'text.secondary' }}>
                      Đang truy vấn dữ liệu chuyến xe từ Backend...
                    </Typography>
                  </TableCell>
                </TableRow>
              ) : paginatedBookings.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={11} align="center" sx={{ py: 5, color: 'text.secondary' }}>
                    {searchTerm ? 'Không tìm thấy chuyến xe nào phù hợp với từ khóa' : 'Chưa có dữ liệu chuyến xe nào trong hệ thống'}
                  </TableCell>
                </TableRow>
              ) : (
                paginatedBookings.map((b) => (
                  <TableRow key={b.id} hover>
                    <TableCell sx={{ fontWeight: 800, fontFamily: 'monospace', color: '#008cff' }}>
                      #{b.id}
                    </TableCell>
                    <TableCell sx={{ fontWeight: 600 }}>
                      Khách #{b.customerId}
                    </TableCell>
                    <TableCell>
                      {b.driverId ? (
                        <Chip
                          label={`Tài xế #${b.driverId}`}
                          size="small"
                          sx={{ bgcolor: 'rgba(0, 140, 255, 0.1)', color: '#008cff', fontWeight: 600, fontSize: '0.75rem' }}
                        />
                      ) : (
                        <Typography variant="caption" sx={{ color: 'text.secondary', fontStyle: 'italic' }}>
                          Chưa nhận
                        </Typography>
                      )}
                    </TableCell>
                    <TableCell>
                      <Tooltip title={`Lat: ${b.startLatitude}, Lng: ${b.startLongitude}`}>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                          <LocationIcon sx={{ fontSize: 15, color: '#15ca20' }} />
                          <Typography variant="caption" sx={{ fontFamily: 'monospace' }}>
                            {formatCoordinate(b.startLatitude, b.startLongitude)}
                          </Typography>
                        </Box>
                      </Tooltip>
                    </TableCell>
                    <TableCell>
                      <Tooltip title={`Lat: ${b.endLatitude}, Lng: ${b.endLongitude}`}>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                          <LocationIcon sx={{ fontSize: 15, color: '#ff3366' }} />
                          <Typography variant="caption" sx={{ fontFamily: 'monospace' }}>
                            {formatCoordinate(b.endLatitude, b.endLongitude)}
                          </Typography>
                        </Box>
                      </Tooltip>
                    </TableCell>
                    <TableCell align="center" sx={{ fontWeight: 700 }}>
                      {b.distanceInKm ? `${Number(b.distanceInKm).toFixed(1)} km` : '—'}
                    </TableCell>
                    <TableCell align="right" sx={{ fontWeight: 800, color: 'text.primary' }}>
                      {b.price ? `${Number(b.price).toLocaleString('vi-VN')} đ` : '—'}
                    </TableCell>
                    <TableCell>
                      <Chip
                        label={b.paymentMethod || 'CASH'}
                        size="small"
                        variant="outlined"
                        sx={{ fontWeight: 700, fontSize: '0.72rem' }}
                      />
                    </TableCell>
                    <TableCell>
                      {getStatusChip(b.status)}
                    </TableCell>
                    <TableCell sx={{ fontSize: '0.8rem', color: 'text.secondary', whiteSpace: 'nowrap' }}>
                      {formatDateTime(b.createdAt)}
                    </TableCell>
                    <TableCell sx={{ fontSize: '0.8rem', color: 'text.secondary', whiteSpace: 'nowrap' }}>
                      {formatDateTime(b.completedAt)}
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
          count={filteredBookings.length}
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

export default Bookings;
