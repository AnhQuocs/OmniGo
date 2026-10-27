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
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  Avatar,
  Divider,
  Stack,
} from '@mui/material';
import {
  Search as SearchIcon,
  Refresh as RefreshIcon,
  DirectionsCar as RideIcon,
  CheckCircle as CompletedIcon,
  Schedule as PendingIcon,
  Cancel as CancelIcon,
  FiberManualRecord as DotIcon,
  Visibility as ViewIcon,
  Close as CloseIcon,
  Person as PersonIcon,
  Phone as PhoneIcon,
  Email as EmailIcon,
  TwoWheeler as BikeIcon,
  LocationOn as LocationIcon,
  TripOrigin as PickupIcon,
  Place as DropoffIcon,
  AccessTime as TimeIcon,
  Badge as BadgeIcon,
  CreditCard as PaymentIcon,
} from '@mui/icons-material';
import { useLocation, useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import bookingService from '../services/bookingService';
import userService from '../services/userService';
import driverService from '../services/driverService';

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

const getVehicleTypeBadge = (vType) => {
  const type = (vType || 'BIKE').toUpperCase();
  switch (type) {
    case 'CAR_4':
      return {
        label: 'Ô tô 4 chỗ',
        icon: <RideIcon sx={{ fontSize: 15 }} />,
        color: '#0284c7',
        bgcolor: 'rgba(2, 132, 199, 0.1)',
      };
    case 'CAR_7':
      return {
        label: 'Ô tô 7 chỗ',
        icon: <RideIcon sx={{ fontSize: 15 }} />,
        color: '#7c3aed',
        bgcolor: 'rgba(124, 58, 237, 0.1)',
      };
    case 'BIKE':
    default:
      return {
        label: 'Xe máy',
        icon: <BikeIcon sx={{ fontSize: 15 }} />,
        color: '#16a34a',
        bgcolor: 'rgba(22, 163, 74, 0.1)',
      };
  }
};

export const Bookings = () => {
  const location = useLocation();
  const navigate = useNavigate();

  const [bookings, setBookings] = useState([]);
  const [usersMap, setUsersMap] = useState({});
  const [driverProfilesMap, setDriverProfilesMap] = useState({});
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedBooking, setSelectedBooking] = useState(null);

  const queryParams = new URLSearchParams(location.search);
  const statusFilter = queryParams.get('status')?.toUpperCase() || 'ALL';
  const [autoSync] = useState(true);

  // Load profiles map and bookings data
  const fetchData = async (isBackground = false) => {
    if (!isBackground) setLoading(true);
    setError(null);
    try {
      const [bookingList, usersRes, driverRes] = await Promise.all([
        bookingService.getAllBookings(),
        userService.getAllUsers({ page: 0, size: 200 }).catch(() => null),
        driverService.getAllDriverProfiles().catch(() => null),
      ]);

      const data = Array.isArray(bookingList) ? bookingList : (bookingList?.content || []);
      setBookings(data);

      if (usersRes) {
        const uList = usersRes?.content || (Array.isArray(usersRes) ? usersRes : usersRes?.data?.content || usersRes?.data || []);
        if (Array.isArray(uList)) {
          const uMap = {};
          uList.forEach((u) => {
            if (u.id) uMap[u.id] = u;
          });
          setUsersMap(uMap);
        }
      }

      if (driverRes) {
        const dList = driverRes?.data || (Array.isArray(driverRes) ? driverRes : []);
        if (Array.isArray(dList)) {
          const dMap = {};
          dList.forEach((d) => {
            if (d.driverId) dMap[d.driverId] = d;
          });
          setDriverProfilesMap(dMap);
        }
      }
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
    fetchData();
  }, []);

  // Realtime auto-sync every 10s
  useEffect(() => {
    if (!autoSync) return;
    const interval = setInterval(() => {
      fetchData(true);
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

  const totalCount = bookings.length;
  const completedCount = bookings.filter((b) => b.status === 'COMPLETED').length;
  const activeCount = bookings.filter((b) => ['PENDING', 'ACCEPTED', 'ARRIVED', 'IN_PROGRESS'].includes(b.status)).length;
  const cancelledCount = bookings.filter((b) => b.status === 'CANCELLED').length;

  const filteredBookings = bookings.filter((b) => {
    const term = searchTerm.toLowerCase();
    const customer = usersMap[b.customerId];
    const driver = driverProfilesMap[b.driverId] || usersMap[b.driverId];

    const matchSearch =
      (b.id && String(b.id).includes(term)) ||
      (b.customerId && String(b.customerId).includes(term)) ||
      (b.driverId && String(b.driverId).includes(term)) ||
      (customer?.fullName && customer.fullName.toLowerCase().includes(term)) ||
      (customer?.phoneNumber && customer.phoneNumber.includes(term)) ||
      (driver?.fullName && driver.fullName.toLowerCase().includes(term)) ||
      (driver?.phoneNumber && driver.phoneNumber.includes(term)) ||
      (driver?.licensePlate && driver.licensePlate.toLowerCase().includes(term)) ||
      (b.pickupAddress && b.pickupAddress.toLowerCase().includes(term)) ||
      (b.dropoffAddress && b.dropoffAddress.toLowerCase().includes(term)) ||
      (b.vehicleType && b.vehicleType.toLowerCase().includes(term)) ||
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
            Theo dõi trạng thái, lộ trình, thông tin khách hàng và điều phối chuyến xe từ hệ thống
          </Typography>
        </Box>

        <Button
          variant="contained"
          startIcon={<RefreshIcon />}
          onClick={() => fetchData(false)}
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
              placeholder="Tìm theo Mã ID, Khách, Tài xế, Biển số..."
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
          <Table sx={{ minWidth: 1350 }} size="small">
            <TableHead sx={{ bgcolor: 'action.hover' }}>
              <TableRow>
                <TableCell sx={{ width: 85, whiteSpace: 'nowrap', fontWeight: 700 }}>#Mã cuốc</TableCell>
                <TableCell sx={{ minWidth: 175, whiteSpace: 'nowrap', fontWeight: 700 }}>Khách hàng</TableCell>
                <TableCell sx={{ minWidth: 160, whiteSpace: 'nowrap', fontWeight: 700 }}>Tài xế</TableCell>
                <TableCell sx={{ minWidth: 210, maxWidth: 280, whiteSpace: 'nowrap', fontWeight: 700 }}>Điểm đón</TableCell>
                <TableCell sx={{ minWidth: 210, maxWidth: 280, whiteSpace: 'nowrap', fontWeight: 700 }}>Điểm đến</TableCell>
                <TableCell align="center" sx={{ minWidth: 110, whiteSpace: 'nowrap', fontWeight: 700 }}>Phương tiện</TableCell>
                <TableCell align="right" sx={{ minWidth: 105, whiteSpace: 'nowrap', fontWeight: 700 }}>Tiền cước</TableCell>
                <TableCell sx={{ minWidth: 95, whiteSpace: 'nowrap', fontWeight: 700 }}>Thanh toán</TableCell>
                <TableCell sx={{ minWidth: 115, whiteSpace: 'nowrap', fontWeight: 700 }}>Trạng thái</TableCell>
                <TableCell sx={{ minWidth: 125, whiteSpace: 'nowrap', fontWeight: 700 }}>Thời gian đặt</TableCell>
                <TableCell align="center" sx={{ width: 85, whiteSpace: 'nowrap', fontWeight: 700 }}>Thao tác</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {loading ? (
                <TableRow>
                  <TableCell colSpan={11} align="center" sx={{ py: 6 }}>
                    <CircularProgress size={30} />
                    <Typography variant="body2" sx={{ mt: 1.5, color: 'text.secondary' }}>
                      Đang đồng bộ dữ liệu chuyến xe & hồ sơ từ Backend...
                    </Typography>
                  </TableCell>
                </TableRow>
              ) : paginatedBookings.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={11} align="center" sx={{ py: 6, color: 'text.secondary' }}>
                    <RideIcon sx={{ fontSize: 44, color: 'text.secondary', mb: 1, opacity: 0.6 }} />
                    <Typography variant="body1" sx={{ fontWeight: 600 }}>
                      {searchTerm ? 'Không tìm thấy chuyến xe nào phù hợp' : 'Chưa có chuyến xe nào trong hệ thống'}
                    </Typography>
                  </TableCell>
                </TableRow>
              ) : (
                paginatedBookings.map((b) => {
                  const customer = usersMap[b.customerId];
                  const driver = driverProfilesMap[b.driverId] || usersMap[b.driverId];
                  const cName = customer?.fullName || `Khách hàng #${b.customerId}`;
                  const cPhone = customer?.phoneNumber || '—';
                  const cAvatar = customer?.avatarUrl || b.customerAvatarUrl || '';

                  const dName = driver?.fullName || (b.driverId ? `Tài xế đối tác #${b.driverId}` : '');
                  const dPhone = driver?.phoneNumber || '—';
                  const dAvatar = driver?.avatarUrl || b.driverAvatarUrl || '';
                  const dPlate = driver?.licensePlate || '';

                  const pickupDisplay = b.pickupAddress || b.startAddress || formatLocation(b.startLatitude, b.startLongitude);
                  const dropoffDisplay = b.dropoffAddress || b.endAddress || formatLocation(b.endLatitude, b.endLongitude);
                  const vehicleBadge = getVehicleTypeBadge(b.vehicleType);

                  return (
                    <TableRow key={b.id} hover>
                      <TableCell sx={{ fontWeight: 800, fontFamily: 'monospace', color: '#008cff', whiteSpace: 'nowrap' }}>
                        #{b.id}
                      </TableCell>

                      {/* Khách hàng Column */}
                      <TableCell sx={{ whiteSpace: 'nowrap', minWidth: 175 }}>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.2 }}>
                          <Avatar
                            src={cAvatar}
                            alt={cName}
                            sx={{ width: 34, height: 34, fontSize: '0.85rem', bgcolor: '#e0f2fe', color: '#0369a1', fontWeight: 700, flexShrink: 0 }}
                          >
                            {cName.charAt(0).toUpperCase()}
                          </Avatar>
                          <Box sx={{ minWidth: 0 }}>
                            <Typography variant="body2" sx={{ fontWeight: 700, color: 'text.primary', lineHeight: 1.2, whiteSpace: 'nowrap' }}>
                              {cName}
                            </Typography>
                            <Typography variant="caption" sx={{ color: 'text.secondary', display: 'flex', alignItems: 'center', gap: 0.4, mt: 0.2, whiteSpace: 'nowrap' }}>
                              <PhoneIcon sx={{ fontSize: 11, color: '#16a34a' }} />
                              {cPhone}
                            </Typography>
                          </Box>
                        </Box>
                      </TableCell>

                      {/* Tài xế Column */}
                      <TableCell sx={{ whiteSpace: 'nowrap', minWidth: 160 }}>
                        {b.driverId ? (
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.2 }}>
                            <Avatar
                              src={dAvatar}
                              alt={dName}
                              sx={{ width: 34, height: 34, fontSize: '0.85rem', bgcolor: '#fef3c7', color: '#b45309', fontWeight: 700, flexShrink: 0 }}
                            >
                              {dName.charAt(0).toUpperCase()}
                            </Avatar>
                            <Box sx={{ minWidth: 0 }}>
                              <Typography variant="body2" sx={{ fontWeight: 700, color: 'text.primary', lineHeight: 1.2, whiteSpace: 'nowrap' }}>
                                {dName}
                              </Typography>
                              <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.8, mt: 0.2 }}>
                                <Typography variant="caption" sx={{ color: 'text.secondary', whiteSpace: 'nowrap' }}>
                                  {dPhone}
                                </Typography>
                                {dPlate && (
                                  <Chip
                                    label={dPlate}
                                    size="small"
                                    sx={{
                                      height: 18,
                                      fontSize: '0.68rem',
                                      fontWeight: 800,
                                      bgcolor: '#0f172a',
                                      color: '#f8fafc',
                                      borderRadius: 1,
                                    }}
                                  />
                                )}
                              </Box>
                            </Box>
                          </Box>
                        ) : (
                          <Chip
                            label="Chưa nhận"
                            size="small"
                            variant="outlined"
                            sx={{ color: 'text.secondary', fontSize: '0.72rem', fontStyle: 'italic' }}
                          />
                        )}
                      </TableCell>

                      {/* Điểm đón */}
                      <TableCell sx={{ minWidth: 210, maxWidth: 280 }}>
                        <Tooltip title={`Tọa độ GPS đón: ${formatCoordinate(b.startLatitude, b.startLongitude)} | ${pickupDisplay}`}>
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.8 }}>
                            <PickupIcon sx={{ fontSize: 16, color: '#16a34a', flexShrink: 0 }} />
                            <Typography
                              variant="body2"
                              sx={{
                                fontSize: '0.82rem',
                                fontWeight: 600,
                                color: 'text.primary',
                                lineHeight: 1.35,
                                whiteSpace: 'nowrap',
                                overflow: 'hidden',
                                textOverflow: 'ellipsis',
                              }}
                            >
                              {pickupDisplay}
                            </Typography>
                          </Box>
                        </Tooltip>
                      </TableCell>

                      {/* Điểm đến */}
                      <TableCell sx={{ minWidth: 210, maxWidth: 280 }}>
                        <Tooltip title={`Tọa độ GPS đến: ${formatCoordinate(b.endLatitude, b.endLongitude)} | ${dropoffDisplay}`}>
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.8 }}>
                            <DropoffIcon sx={{ fontSize: 16, color: '#dc2626', flexShrink: 0 }} />
                            <Typography
                              variant="body2"
                              sx={{
                                fontSize: '0.82rem',
                                fontWeight: 600,
                                color: 'text.primary',
                                lineHeight: 1.35,
                                whiteSpace: 'nowrap',
                                overflow: 'hidden',
                                textOverflow: 'ellipsis',
                              }}
                            >
                              {dropoffDisplay}
                            </Typography>
                          </Box>
                        </Tooltip>
                      </TableCell>

                      {/* Phương tiện & Quãng đường */}
                      <TableCell align="center">
                        <Chip
                          icon={vehicleBadge.icon}
                          label={vehicleBadge.label}
                          size="small"
                          sx={{
                            bgcolor: vehicleBadge.bgcolor,
                            color: vehicleBadge.color,
                            fontWeight: 700,
                            fontSize: '0.72rem',
                            height: 22,
                            mb: 0.4,
                          }}
                        />
                        <Typography variant="caption" sx={{ display: 'block', fontWeight: 700, color: 'text.secondary' }}>
                          {b.distanceInKm ? `${Number(b.distanceInKm).toFixed(1)} km` : '—'}
                        </Typography>
                      </TableCell>

                      {/* Tiền cước */}
                      <TableCell align="right" sx={{ fontWeight: 800, color: 'text.primary', whiteSpace: 'nowrap' }}>
                        {b.price ? `${Number(b.price).toLocaleString('vi-VN')} đ` : '—'}
                      </TableCell>

                      {/* Thanh toán */}
                      <TableCell>
                        <Chip
                          label={b.paymentMethod || 'CASH'}
                          size="small"
                          variant="outlined"
                          sx={{ fontWeight: 700, fontSize: '0.72rem' }}
                        />
                      </TableCell>

                      {/* Trạng thái */}
                      <TableCell>
                        {getStatusChip(b.status)}
                      </TableCell>

                      {/* Thời gian đặt */}
                      <TableCell sx={{ fontSize: '0.8rem', color: 'text.secondary', whiteSpace: 'nowrap' }}>
                        {formatDateTime(b.createdAt)}
                      </TableCell>

                      {/* Thao tác (Action: Xem chi tiết) */}
                      <TableCell align="center" sx={{ whiteSpace: 'nowrap' }}>
                        <Tooltip title="Xem chi tiết chuyến xe">
                          <IconButton
                            size="small"
                            onClick={() => setSelectedBooking(b)}
                            sx={{
                              color: '#008cff',
                              bgcolor: 'rgba(0, 140, 255, 0.08)',
                              border: '1px solid rgba(0, 140, 255, 0.2)',
                              borderRadius: 1.5,
                              p: 0.8,
                              '&:hover': {
                                bgcolor: 'rgba(0, 140, 255, 0.18)',
                                transform: 'scale(1.05)',
                              },
                              transition: 'all 0.2s',
                            }}
                          >
                            <ViewIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                      </TableCell>
                    </TableRow>
                  );
                })
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

      {/* MODAL: CHI TIẾT ĐƠN BOOKING CHUYẾN XE */}
      <Dialog
        open={Boolean(selectedBooking)}
        onClose={() => setSelectedBooking(null)}
        maxWidth="md"
        fullWidth
        PaperProps={{ sx: { borderRadius: 3 } }}
      >
        <DialogTitle component="div" sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', pb: 1.5 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
            <Box sx={{ p: 1, borderRadius: 2, bgcolor: 'rgba(0, 140, 255, 0.1)', color: '#008cff', display: 'flex' }}>
              <RideIcon sx={{ fontSize: 26 }} />
            </Box>
            <Box>
              <Typography variant="h6" sx={{ fontWeight: 800, fontFamily: '"Poppins", sans-serif' }}>
                Chi Tiết Chuyến Xe #{selectedBooking?.id}
              </Typography>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 0.4 }}>
                <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600 }}>
                  Trạng thái:
                </Typography>
                {selectedBooking && getStatusChip(selectedBooking.status)}
              </Box>
            </Box>
          </Box>
          <IconButton onClick={() => setSelectedBooking(null)} size="small">
            <CloseIcon />
          </IconButton>
        </DialogTitle>
        <Divider />

        <DialogContent sx={{ p: 3 }}>
          {selectedBooking && (() => {
            const customer = usersMap[selectedBooking.customerId];
            const driver = driverProfilesMap[selectedBooking.driverId] || usersMap[selectedBooking.driverId];

            const cName = customer?.fullName || `Khách hàng #${selectedBooking.customerId}`;
            const cPhone = customer?.phoneNumber || 'Chưa cập nhật SĐT';
            const cEmail = customer?.email || 'Chưa cập nhật email';
            const cAvatar = customer?.avatarUrl || selectedBooking.customerAvatarUrl || '';

            const dName = driver?.fullName || (selectedBooking.driverId ? `Tài xế #${selectedBooking.driverId}` : '');
            const dPhone = driver?.phoneNumber || 'Chưa cập nhật SĐT';
            const dEmail = driver?.email || 'Chưa cập nhật email';
            const dAvatar = driver?.avatarUrl || selectedBooking.driverAvatarUrl || '';
            const dPlate = driver?.licensePlate || 'Chưa có biển số';
            const dModel = driver?.vehicleModel || driver?.vehicleType || 'Xe máy đối tác OmniGo';

            const pickupAddress = selectedBooking.pickupAddress || selectedBooking.startAddress || formatLocation(selectedBooking.startLatitude, selectedBooking.startLongitude);
            const dropoffAddress = selectedBooking.dropoffAddress || selectedBooking.endAddress || formatLocation(selectedBooking.endLatitude, selectedBooking.endLongitude);
            const vehicleBadge = getVehicleTypeBadge(selectedBooking.vehicleType);

            return (
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5 }}>
                {/* Row 1: Khách hàng & Tài xế */}
                <Grid container spacing={2}>
                  {/* Khối 1: Thông tin Khách hàng */}
                  <Grid size={{ xs: 12, md: 6 }}>
                    <Card sx={{ p: 2.5, bgcolor: '#F8FAFC', border: '1px solid #E2E8F0', borderRadius: 2.5, height: '100%' }}>
                      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 1.5 }}>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                          <PersonIcon sx={{ fontSize: 20, color: '#0F172A' }} />
                          <Typography variant="subtitle2" sx={{ fontWeight: 800, color: '#0F172A' }}>
                            Thông tin Khách Hàng
                          </Typography>
                        </Box>
                        <Chip
                          label={`User #${selectedBooking.customerId}`}
                          size="small"
                          sx={{ bgcolor: '#E0F2FE', color: '#0369A1', fontWeight: 700, fontSize: '0.72rem', height: 22 }}
                        />
                      </Box>

                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 1.5 }}>
                        <Avatar
                          src={cAvatar}
                          alt={cName}
                          sx={{
                            width: 54,
                            height: 54,
                            bgcolor: '#E0F2FE',
                            color: '#0369A1',
                            fontWeight: 800,
                            fontSize: '1.2rem',
                            border: '2px solid #BAE6FD',
                          }}
                        >
                          {cName.charAt(0).toUpperCase()}
                        </Avatar>
                        <Box>
                          <Typography variant="body1" sx={{ fontWeight: 800, color: '#0F172A', fontSize: '1.05rem' }}>
                            {cName}
                          </Typography>
                          <Typography variant="body2" sx={{ color: '#64748B', display: 'flex', alignItems: 'center', gap: 0.6, mt: 0.3, fontWeight: 600 }}>
                            <PhoneIcon sx={{ fontSize: 14, color: '#16A34A' }} />
                            {cPhone}
                          </Typography>
                        </Box>
                      </Box>

                      <Box sx={{ p: 1.5, borderRadius: 2, bgcolor: '#FFFFFF', border: '1px solid #F1F5F9', display: 'flex', flexDirection: 'column', gap: 0.8 }}>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.8 }}>
                          <EmailIcon sx={{ fontSize: 15, color: '#64748B' }} />
                          <Typography variant="caption" sx={{ color: '#64748B', fontWeight: 600 }}>Email:</Typography>
                          <Typography variant="caption" sx={{ fontWeight: 700, color: '#0F172A' }}>{cEmail}</Typography>
                        </Box>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.8 }}>
                          <BadgeIcon sx={{ fontSize: 15, color: '#64748B' }} />
                          <Typography variant="caption" sx={{ color: '#64748B', fontWeight: 600 }}>Tài khoản:</Typography>
                          <Chip
                            label={customer?.isLocked ? 'Bị khóa' : 'Hoạt động'}
                            size="small"
                            sx={{
                              height: 18,
                              fontSize: '0.65rem',
                              fontWeight: 700,
                              bgcolor: customer?.isLocked ? '#FEE2E2' : '#DCFCE7',
                              color: customer?.isLocked ? '#DC2626' : '#16A34A',
                            }}
                          />
                        </Box>
                      </Box>
                    </Card>
                  </Grid>

                  {/* Khối 2: Thông tin Tài xế */}
                  <Grid size={{ xs: 12, md: 6 }}>
                    <Card sx={{ p: 2.5, bgcolor: '#F8FAFC', border: '1px solid #E2E8F0', borderRadius: 2.5, height: '100%' }}>
                      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 1.5 }}>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                          <BikeIcon sx={{ fontSize: 20, color: '#0F172A' }} />
                          <Typography variant="subtitle2" sx={{ fontWeight: 800, color: '#0F172A' }}>
                            Thông tin Tài Xế
                          </Typography>
                        </Box>
                        {selectedBooking.driverId ? (
                          <Chip
                            label={`Tài xế #${selectedBooking.driverId}`}
                            size="small"
                            sx={{ bgcolor: '#FEF3C7', color: '#B45309', fontWeight: 700, fontSize: '0.72rem', height: 22 }}
                          />
                        ) : (
                          <Chip
                            label="Chưa ghép xe"
                            size="small"
                            sx={{ bgcolor: '#F1F5F9', color: '#64748B', fontWeight: 700, fontSize: '0.72rem', height: 22 }}
                          />
                        )}
                      </Box>

                      {selectedBooking.driverId ? (
                        <>
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 1.5 }}>
                            <Avatar
                              src={dAvatar}
                              alt={dName}
                              sx={{
                                width: 54,
                                height: 54,
                                bgcolor: '#FEF3C7',
                                color: '#B45309',
                                fontWeight: 800,
                                fontSize: '1.2rem',
                                border: '2px solid #FDE68A',
                              }}
                            >
                              {dName.charAt(0).toUpperCase()}
                            </Avatar>
                            <Box>
                              <Typography variant="body1" sx={{ fontWeight: 800, color: '#0F172A', fontSize: '1.05rem' }}>
                                {dName}
                              </Typography>
                              <Typography variant="body2" sx={{ color: '#64748B', display: 'flex', alignItems: 'center', gap: 0.6, mt: 0.3, fontWeight: 600 }}>
                                <PhoneIcon sx={{ fontSize: 14, color: '#F97316' }} />
                                {dPhone}
                              </Typography>
                            </Box>
                          </Box>

                          <Box sx={{ p: 1.5, borderRadius: 2, bgcolor: '#FFFFFF', border: '1px solid #F1F5F9', display: 'flex', flexDirection: 'column', gap: 0.8 }}>
                            <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                              <Typography variant="caption" sx={{ color: '#64748B', fontWeight: 600 }}>Biển số xe:</Typography>
                              <Chip
                                label={dPlate}
                                size="small"
                                sx={{
                                  height: 22,
                                  fontSize: '0.75rem',
                                  fontWeight: 800,
                                  bgcolor: '#0F172A',
                                  color: '#F8FAFC',
                                  letterSpacing: '0.5px',
                                  borderRadius: 1.5,
                                }}
                              />
                            </Box>
                            <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                              <Typography variant="caption" sx={{ color: '#64748B', fontWeight: 600 }}>Dòng xe / Loại xe:</Typography>
                              <Typography variant="caption" sx={{ fontWeight: 700, color: '#0F172A' }}>{dModel}</Typography>
                            </Box>
                            {driver?.cccdNumber && (
                              <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                                <Typography variant="caption" sx={{ color: '#64748B', fontWeight: 600 }}>CCCD xác minh:</Typography>
                                <Typography variant="caption" sx={{ fontWeight: 700, color: '#0F172A' }}>{driver.cccdNumber}</Typography>
                              </Box>
                            )}
                          </Box>
                        </>
                      ) : (
                        <Box sx={{ p: 3, borderRadius: 2, bgcolor: '#FFFFFF', border: '1px dashed #CBD5E1', textAlign: 'center' }}>
                          <RideIcon sx={{ fontSize: 36, color: '#94A3B8', mb: 1 }} />
                          <Typography variant="body2" sx={{ color: '#64748B', fontWeight: 600 }}>
                            {selectedBooking.status === 'CANCELLED'
                              ? 'Chuyến xe đã bị hủy mà chưa có tài xế nào tiếp nhận.'
                              : 'Hệ thống đang điều phối và tìm tài xế gần nhất...'}
                          </Typography>
                        </Box>
                      )}
                    </Card>
                  </Grid>
                </Grid>

                {/* Row 2: Lộ trình chi tiết Điểm đón & Điểm đến */}
                <Card sx={{ p: 2.5, bgcolor: '#F8FAFC', border: '1px solid #E2E8F0', borderRadius: 2.5 }}>
                  <Typography variant="subtitle2" sx={{ fontWeight: 800, color: '#0F172A', mb: 2, display: 'flex', alignItems: 'center', gap: 1 }}>
                    <LocationIcon sx={{ fontSize: 20, color: '#008CFF' }} />
                    Lộ Trình Di Chuyển & Điểm Đón / Điểm Đến
                  </Typography>

                  <Grid container spacing={2}>
                    <Grid size={{ xs: 12, md: 6 }}>
                      <Box sx={{ p: 2, bgcolor: '#FFFFFF', borderRadius: 2, border: '1.5px solid #BBF7D0', height: '100%' }}>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                          <PickupIcon sx={{ fontSize: 18, color: '#16A34A' }} />
                          <Typography variant="subtitle2" sx={{ fontWeight: 800, color: '#16A34A' }}>
                            ĐIỂM ĐÓN (PICKUP)
                          </Typography>
                        </Box>
                        <Typography variant="body1" sx={{ fontWeight: 700, color: '#0F172A', lineHeight: 1.4, mb: 1 }}>
                          {pickupAddress}
                        </Typography>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.6 }}>
                          <Typography variant="caption" sx={{ color: '#64748B', fontWeight: 600 }}>Tọa độ GPS:</Typography>
                          <Typography variant="caption" sx={{ fontFamily: 'monospace', fontWeight: 700, color: '#0369A1', bgcolor: '#F0F9FF', px: 0.8, py: 0.2, borderRadius: 1 }}>
                            {formatCoordinate(selectedBooking.startLatitude, selectedBooking.startLongitude)}
                          </Typography>
                        </Box>
                      </Box>
                    </Grid>

                    <Grid size={{ xs: 12, md: 6 }}>
                      <Box sx={{ p: 2, bgcolor: '#FFFFFF', borderRadius: 2, border: '1.5px solid #FECACA', height: '100%' }}>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                          <DropoffIcon sx={{ fontSize: 18, color: '#DC2626' }} />
                          <Typography variant="subtitle2" sx={{ fontWeight: 800, color: '#DC2626' }}>
                            ĐIỂM ĐẾN (DESTINATION)
                          </Typography>
                        </Box>
                        <Typography variant="body1" sx={{ fontWeight: 700, color: '#0F172A', lineHeight: 1.4, mb: 1 }}>
                          {dropoffAddress}
                        </Typography>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.6 }}>
                          <Typography variant="caption" sx={{ color: '#64748B', fontWeight: 600 }}>Tọa độ GPS:</Typography>
                          <Typography variant="caption" sx={{ fontFamily: 'monospace', fontWeight: 700, color: '#B91C1C', bgcolor: '#FEF2F2', px: 0.8, py: 0.2, borderRadius: 1 }}>
                            {formatCoordinate(selectedBooking.endLatitude, selectedBooking.endLongitude)}
                          </Typography>
                        </Box>
                      </Box>
                    </Grid>
                  </Grid>

                  <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mt: 2, p: 1.5, bgcolor: '#EFF6FF', borderRadius: 2, flexWrap: 'wrap', gap: 1 }}>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <RideIcon sx={{ fontSize: 18, color: '#1D4ED8' }} />
                      <Typography variant="body2" sx={{ fontWeight: 700, color: '#1E40AF' }}>
                        Quãng đường: {selectedBooking.distanceInKm ? `${Number(selectedBooking.distanceInKm).toFixed(2)} km` : '—'}
                      </Typography>
                    </Box>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <Chip
                        icon={vehicleBadge.icon}
                        label={`Phương tiện: ${vehicleBadge.label}`}
                        size="small"
                        sx={{ bgcolor: vehicleBadge.bgcolor, color: vehicleBadge.color, fontWeight: 700 }}
                      />
                    </Box>
                  </Box>
                </Card>

                {/* Row 3: Cước phí, Thanh toán & Mốc thời gian */}
                <Grid container spacing={2}>
                  <Grid size={{ xs: 12, md: 6 }}>
                    <Card sx={{ p: 2.5, bgcolor: '#F8FAFC', border: '1px solid #E2E8F0', borderRadius: 2.5, height: '100%' }}>
                      <Typography variant="subtitle2" sx={{ fontWeight: 800, color: '#0F172A', mb: 1.5, display: 'flex', alignItems: 'center', gap: 1 }}>
                        <PaymentIcon sx={{ fontSize: 18, color: '#0F172A' }} />
                        Cước Phí & Thanh Toán
                      </Typography>
                      <Box sx={{ p: 2, bgcolor: '#FFFFFF', borderRadius: 2, border: '1px solid #F1F5F9' }}>
                        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', mb: 1 }}>
                          <Typography variant="body2" sx={{ color: '#64748B', fontWeight: 600 }}>Thành tiền:</Typography>
                          <Typography variant="h5" sx={{ fontWeight: 800, color: '#008CFF' }}>
                            {selectedBooking.price ? `${Number(selectedBooking.price).toLocaleString('vi-VN')} đ` : '0 đ'}
                          </Typography>
                        </Box>
                        <Divider sx={{ my: 1 }} />
                        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                          <Typography variant="caption" sx={{ color: '#64748B', fontWeight: 600 }}>Phương thức thanh toán:</Typography>
                          <Chip
                            label={selectedBooking.paymentMethod || 'CASH'}
                            size="small"
                            sx={{ fontWeight: 800, fontSize: '0.75rem', bgcolor: '#F1F5F9', color: '#0F172A' }}
                          />
                        </Box>
                      </Box>
                    </Card>
                  </Grid>

                  <Grid size={{ xs: 12, md: 6 }}>
                    <Card sx={{ p: 2.5, bgcolor: '#F8FAFC', border: '1px solid #E2E8F0', borderRadius: 2.5, height: '100%' }}>
                      <Typography variant="subtitle2" sx={{ fontWeight: 800, color: '#0F172A', mb: 1.5, display: 'flex', alignItems: 'center', gap: 1 }}>
                        <TimeIcon sx={{ fontSize: 18, color: '#0F172A' }} />
                        Các Mốc Thời Gian
                      </Typography>
                      <Box sx={{ p: 2, bgcolor: '#FFFFFF', borderRadius: 2, border: '1px solid #F1F5F9', display: 'flex', flexDirection: 'column', gap: 1 }}>
                        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                          <Typography variant="caption" sx={{ color: '#64748B', fontWeight: 600 }}>Thời điểm đặt xe:</Typography>
                          <Typography variant="caption" sx={{ fontWeight: 700, color: '#0F172A' }}>{formatDateTime(selectedBooking.createdAt)}</Typography>
                        </Box>
                        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                          <Typography variant="caption" sx={{ color: '#64748B', fontWeight: 600 }}>Tài xế nhận cuốc:</Typography>
                          <Typography variant="caption" sx={{ fontWeight: 700, color: '#0F172A' }}>{formatDateTime(selectedBooking.acceptedAt)}</Typography>
                        </Box>
                        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                          <Typography variant="caption" sx={{ color: '#64748B', fontWeight: 600 }}>Thời điểm hoàn thành:</Typography>
                          <Typography variant="caption" sx={{ fontWeight: 700, color: '#0F172A' }}>{formatDateTime(selectedBooking.completedAt)}</Typography>
                        </Box>
                      </Box>
                    </Card>
                  </Grid>
                </Grid>
              </Box>
            );
          })()}
        </DialogContent>
      </Dialog>
    </Box>
  );
};

export default Bookings;
