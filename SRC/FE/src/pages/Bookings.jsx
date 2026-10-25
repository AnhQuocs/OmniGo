import { useState, useEffect } from 'react';
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
  Tooltip,
} from '@mui/material';
import {
  Search as SearchIcon,
  Refresh as RefreshIcon,
  DirectionsCar as RideIcon,
  CheckCircle as CompletedIcon,
  Schedule as PendingIcon,
  Cancel as CancelIcon,
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
  const queryParams = new URLSearchParams(location.search);
  const statusFilter = queryParams.get('status')?.toUpperCase() || 'ALL';
  const [autoSync] = useState(true);

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
    let active = true;
    async function init() {
      try {
        const data = await bookingService.getAllBookings();
        if (active && data) {
          setBookings(Array.isArray(data) ? data : (data.content || []));
        }
      } catch (err) {
        if (active) {
          const msg = err.response?.data?.message || err.message || 'Không thể tải danh sách chuyến xe';
          toast.error(msg);
        }
      } finally {
        if (active) setLoading(false);
      }
    }
    init();
    return () => { active = false; };
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

const KNOWN_LOCATIONS = {
  '21.0245,105.8574': 'Tràng Tiền, Hoàn Kiếm, Hà Nội',
  '21.0585,105.8202': 'Xuân La, Tây Hồ, Hà Nội',
  '21.0285,105.8542': 'Đinh Tiên Hoàng, Hoàn Kiếm, Hà Nội',
  '21.0031,105.8431': 'Đại Cồ Việt, Hai Bà Trưng, Hà Nội',
  '21.0378,105.7836': 'Duy Tân, Cầu Giấy, Hà Nội',
  '21.0135,105.7988': 'Trung Hòa, Cầu Giấy, Hà Nội',
  '21.0333,105.8500': 'Điện Biên Phủ, Ba Đình, Hà Nội',
  '21.0307,105.7845': 'Trần Thái Tông, Cầu Giấy, Hà Nội',
  '21.0175,105.8540': 'Phố Huế, Hai Bà Trưng, Hà Nội',
};

const formatLocation = (lat, lng, addr) => {
  if (addr && String(addr).trim()) return addr;
  if (lat === undefined || lat === null || lng === undefined || lng === null) return '—';
  const key = `${Number(lat).toFixed(4)},${Number(lng).toFixed(4)}`;
  if (KNOWN_LOCATIONS[key]) return KNOWN_LOCATIONS[key];
  const nLat = Number(lat);
  const nLng = Number(lng);
  if (nLat >= 20.9 && nLat <= 21.2 && nLng >= 105.7 && nLng <= 106.0) {
    if (nLat > 21.05) return 'Tây Hồ, Hà Nội';
    if (nLat > 21.03) return 'Ba Đình, Hà Nội';
    if (nLng < 105.80) return 'Cầu Giấy, Hà Nội';
    if (nLat < 21.01) return 'Hai Bà Trưng, Hà Nội';
    return 'Hoàn Kiếm, Hà Nội';
  }
  return `Vị trí (${nLat.toFixed(4)}, ${nLng.toFixed(4)})`;
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
    <Box sx={{ width: '100%', maxWidth: '100%', overflowX: 'hidden' }} className="page-enter-animation">
      {/* Header bar with Realtime Indicator & Refresh */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: { xs: 'flex-start', sm: 'center' }, mb: 2.5, flexDirection: { xs: 'column', sm: 'row' }, gap: 1.5 }}>
        <Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.2, flexWrap: 'wrap' }}>
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
          <Typography variant="body2" sx={{ color: 'text.secondary', mt: 0.3, fontSize: { xs: '0.8rem', sm: '0.85rem' } }}>
            Theo dõi trạng thái, lộ trình và điều phối chuyến xe từ `booking-service`
          </Typography>
        </Box>

        <Button
          variant="contained"
          startIcon={<RefreshIcon />}
          onClick={() => fetchBookings(false)}
          disabled={loading}
          sx={{ bgcolor: '#008cff', '&:hover': { bgcolor: '#0070cc' }, borderRadius: 2, width: { xs: '100%', sm: 'auto' } }}
        >
          Làm Mới
        </Button>
      </Box>

      {/* 4 Summary Stats Mini Cards */}
      <Grid container spacing={1.5} sx={{ mb: 2.5, width: '100%' }}>
        <Grid size={{ xs: 6, sm: 3 }}>
          <Card sx={{ p: { xs: 1.5, sm: 2 }, display: 'flex', alignItems: 'center', gap: 1.2 }}>
            <Box sx={{ p: 1, borderRadius: 2, bgcolor: 'rgba(0, 140, 255, 0.1)', color: '#008cff', display: 'flex' }}>
              <RideIcon sx={{ fontSize: { xs: 20, sm: 24 } }} />
            </Box>
            <Box>
              <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600, fontSize: { xs: '0.7rem', sm: '0.75rem' } }}>Tổng cuốc xe</Typography>
              <Typography variant="h6" sx={{ fontWeight: 800, fontSize: { xs: '1rem', sm: '1.25rem' } }}>{totalCount}</Typography>
            </Box>
          </Card>
        </Grid>
        <Grid size={{ xs: 6, sm: 3 }}>
          <Card sx={{ p: { xs: 1.5, sm: 2 }, display: 'flex', alignItems: 'center', gap: 1.2 }}>
            <Box sx={{ p: 1, borderRadius: 2, bgcolor: 'rgba(0, 140, 255, 0.15)', color: '#008cff', display: 'flex' }}>
              <PendingIcon sx={{ fontSize: { xs: 20, sm: 24 } }} />
            </Box>
            <Box>
              <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600, fontSize: { xs: '0.7rem', sm: '0.75rem' } }}>Đang chạy</Typography>
              <Typography variant="h6" sx={{ fontWeight: 800, color: '#008cff', fontSize: { xs: '1rem', sm: '1.25rem' } }}>{activeCount}</Typography>
            </Box>
          </Card>
        </Grid>
        <Grid size={{ xs: 6, sm: 3 }}>
          <Card sx={{ p: { xs: 1.5, sm: 2 }, display: 'flex', alignItems: 'center', gap: 1.2 }}>
            <Box sx={{ p: 1, borderRadius: 2, bgcolor: 'rgba(21, 202, 32, 0.15)', color: '#15ca20', display: 'flex' }}>
              <CompletedIcon sx={{ fontSize: { xs: 20, sm: 24 } }} />
            </Box>
            <Box>
              <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600, fontSize: { xs: '0.7rem', sm: '0.75rem' } }}>Hoàn thành</Typography>
              <Typography variant="h6" sx={{ fontWeight: 800, color: '#15ca20', fontSize: { xs: '1rem', sm: '1.25rem' } }}>{completedCount}</Typography>
            </Box>
          </Card>
        </Grid>
        <Grid size={{ xs: 6, sm: 3 }}>
          <Card sx={{ p: { xs: 1.5, sm: 2 }, display: 'flex', alignItems: 'center', gap: 1.2 }}>
            <Box sx={{ p: 1, borderRadius: 2, bgcolor: 'rgba(255, 51, 102, 0.15)', color: '#ff3366', display: 'flex' }}>
              <CancelIcon sx={{ fontSize: { xs: 20, sm: 24 } }} />
            </Box>
            <Box>
              <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600, fontSize: { xs: '0.7rem', sm: '0.75rem' } }}>Đã hủy</Typography>
              <Typography variant="h6" sx={{ fontWeight: 800, color: '#ff3366', fontSize: { xs: '1rem', sm: '1.25rem' } }}>{cancelledCount}</Typography>
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
      <Card sx={{ p: { xs: 1.8, sm: 2.5 }, minHeight: 'calc(90vh - 160px)', display: 'flex', flexDirection: 'column' }}>
        {/* Filter buttons & Search */}
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2.5, flexDirection: { xs: 'column', md: 'row' }, gap: 2 }}>
          <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', width: { xs: '100%', md: 'auto' } }}>
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
                size="small"
                sx={{
                  px: { xs: 1.5, sm: 2 },
                  py: 0.8,
                  fontWeight: 600,
                  fontSize: { xs: '0.78rem', sm: '0.85rem' },
                  borderRadius: 2,
                  flex: { xs: 1, sm: 'none' },
                  bgcolor: statusFilter === tab.value ? '#008cff' : 'transparent',
                }}
              >
                {tab.label} ({tab.count})
              </Button>
            ))}
          </Box>

          <Box sx={{ width: { xs: '100%', md: 340 } }}>
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

        <TableContainer sx={{ overflowX: 'auto', width: '100%', flex: 1, minHeight: 380 }}>
          <Table sx={{ minWidth: 1050 }} size="small">
            <TableHead>
              <TableRow>
                <TableCell sx={{ width: 80, whiteSpace: 'nowrap' }}>Mã cuốc</TableCell>
                <TableCell sx={{ minWidth: 100, whiteSpace: 'nowrap' }}>Khách hàng</TableCell>
                <TableCell sx={{ minWidth: 110, whiteSpace: 'nowrap' }}>Tài xế</TableCell>
                <TableCell sx={{ minWidth: 160, maxWidth: 240 }}>Điểm đón</TableCell>
                <TableCell sx={{ minWidth: 160, maxWidth: 240 }}>Điểm đến</TableCell>
                <TableCell align="center" sx={{ minWidth: 95, whiteSpace: 'nowrap' }}>Quãng đường</TableCell>
                <TableCell align="right" sx={{ minWidth: 105, whiteSpace: 'nowrap' }}>Tiền cước</TableCell>
                <TableCell sx={{ minWidth: 100, whiteSpace: 'nowrap' }}>Thanh toán</TableCell>
                <TableCell sx={{ minWidth: 120, whiteSpace: 'nowrap' }}>Trạng thái</TableCell>
                <TableCell sx={{ minWidth: 130, whiteSpace: 'nowrap' }}>Thời gian đặt</TableCell>
                <TableCell sx={{ minWidth: 130, whiteSpace: 'nowrap' }}>Hoàn thành</TableCell>
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
                    <TableCell sx={{ minWidth: 160, maxWidth: 240, whiteSpace: 'normal', wordBreak: 'break-word' }}>
                      <Tooltip title={`Tọa độ GPS: ${formatCoordinate(b.startLatitude, b.startLongitude)}`}>
                        <Typography variant="body2" sx={{ fontSize: '0.82rem', fontWeight: 500, color: 'text.primary', lineHeight: 1.4 }}>
                          {formatLocation(b.startLatitude, b.startLongitude, b.startAddress)}
                        </Typography>
                      </Tooltip>
                    </TableCell>
                    <TableCell sx={{ minWidth: 160, maxWidth: 240, whiteSpace: 'normal', wordBreak: 'break-word' }}>
                      <Tooltip title={`Tọa độ GPS: ${formatCoordinate(b.endLatitude, b.endLongitude)}`}>
                        <Typography variant="body2" sx={{ fontSize: '0.82rem', fontWeight: 500, color: 'text.primary', lineHeight: 1.4 }}>
                          {formatLocation(b.endLatitude, b.endLongitude, b.endAddress)}
                        </Typography>
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
          sx={{ mt: 'auto', pt: 1.5 }}
        />
      </Card>
    </Box>
  );
};

export default Bookings;
