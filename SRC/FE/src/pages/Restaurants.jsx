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
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Avatar,
  Divider,
  MenuItem,
  Select,
  FormControl,
  InputLabel,
  Tab,
  Tabs,
  Stack,
  Tooltip,
} from '@mui/material';
import {
  Search as SearchIcon,
  Refresh as RefreshIcon,
  Store as RestaurantIcon,
  CheckCircle as OpenIcon,
  CheckCircleRounded as CheckCircleIcon,
  Schedule as BusyIcon,
  Block as ClosedIcon,
  MenuBook as MenuIcon,
  Close as CloseIcon,
  Visibility as ViewIcon,
  Add as AddIcon,
  Person as PersonIcon,
  Lock as LockIcon,
  LockOpen as UnlockIcon,
  Phone as PhoneIcon,
  Email as EmailIcon,
  LocationOn as LocationIcon,
  Image as ImageIcon,
  AccessTime as TimeIcon,
  Assignment as LicenseIcon,
} from '@mui/icons-material';
import toast from 'react-hot-toast';
import foodService from '../services/foodService';
import RestaurantReviewsList from '../components/food/RestaurantReviewsList';
import AddressAutocomplete from '../components/common/AddressAutocomplete';
import ImageUploadField from '../components/common/ImageUploadField';

export const Restaurants = () => {
  const [restaurants, setRestaurants] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [mainTab, setMainTab] = useState(0); // 0: Quán đã duyệt (Omni), 1: Đơn chờ duyệt
  const [partnerAppFilter, setPartnerAppFilter] = useState('PENDING'); // 'PENDING' | 'REJECTED' | 'ALL'
  const [appraisalModal, setAppraisalModal] = useState({ open: false, restaurant: null });

  // Menu Modal State
  const [selectedRestaurant, setSelectedRestaurant] = useState(null);
  const [menuItems, setMenuItems] = useState([]);
  const [loadingMenu, setLoadingMenu] = useState(false);
  const [openMenuModal, setOpenMenuModal] = useState(false);
  const [menuModalTab, setMenuModalTab] = useState(0);


  // Lock Restaurant Dialog State
  const [openLockModal, setOpenLockModal] = useState(false);
  const [selectedRestaurantForLock, setSelectedRestaurantForLock] = useState(null);
  const [lockReason, setLockReason] = useState('');
  const [lockSubmitting, setLockSubmitting] = useState(false);

  const handleOpenLockRestaurant = (restaurant) => {
    setSelectedRestaurantForLock(restaurant);
    setLockReason(restaurant.isLocked ? '' : 'Vi phạm an toàn vệ sinh thực phẩm / khiếu nại');
    setOpenLockModal(true);
  };

  const handleCloseLockRestaurant = () => {
    if (!lockSubmitting) {
      setOpenLockModal(false);
      setSelectedRestaurantForLock(null);
    }
  };

  const handleToggleLockRestaurantSubmit = async () => {
    if (!selectedRestaurantForLock) return;
    const isLocking = !selectedRestaurantForLock.isLocked;

    if (isLocking && !lockReason.trim()) {
      toast.error('Vui lòng nhập lý do khóa gian hàng');
      return;
    }

    setLockSubmitting(true);
    try {
      await foodService.toggleLockRestaurant(selectedRestaurantForLock.id, {
        isLocked: isLocking,
        reason: isLocking ? lockReason.trim() : '',
      });
      toast.success(isLocking ? 'Đã khóa gian hàng nhà hàng thành công' : 'Đã mở khóa gian hàng thành công');
      setOpenLockModal(false);
      setSelectedRestaurantForLock(null);
      fetchRestaurants();
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Thao tác thất bại';
      toast.error(msg);
    } finally {
      setLockSubmitting(false);
    }
  };

  // View License Dialog State
  const [viewLicenseDialog, setViewLicenseDialog] = useState({ open: false, restaurant: null });

  // Reject Application Dialog State
  const [rejectDialog, setRejectDialog] = useState({ open: false, restaurant: null, reason: '' });
  const [rejectSubmitting, setRejectSubmitting] = useState(false);

  // Add Partner Modal State
  const [openAddPartnerModal, setOpenAddPartnerModal] = useState(false);
  const [submittingPartner, setSubmittingPartner] = useState(false);
  const [partnerTab, setPartnerTab] = useState(0);
  const [partnerFormData, setPartnerFormData] = useState({
    name: '',
    phone: '',
    address: '',
    latitude: 10.7769,
    longitude: 106.7009,
    imageUrl: '',
    licenseImageUrl: '',
    openTime: '08:00',
    closeTime: '22:00',
    ownerName: '',
    ownerPhone: '',
    email: '',
    password: '',
  });

  const resetPartnerForm = () => {
    setPartnerFormData({
      name: '',
      phone: '',
      address: '',
      latitude: 10.7769,
      longitude: 106.7009,
      imageUrl: '',
      licenseImageUrl: '',
      openTime: '08:00',
      closeTime: '22:00',
      ownerName: '',
      ownerPhone: '',
      email: '',
      password: '',
    });
    setPartnerTab(0);
  };

  const handlePartnerInputChange = (e) => {
    const { name, value } = e.target;
    setPartnerFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSubmitAddPartner = async (e) => {
    if (e) e.preventDefault();

    // Validate Owner info
    if (!partnerFormData.ownerName.trim()) {
      toast.error('Vui lòng nhập họ tên chủ nhà hàng');
      setPartnerTab(0);
      return;
    }
    if (!partnerFormData.ownerPhone.trim()) {
      toast.error('Vui lòng nhập số điện thoại đăng nhập của chủ nhà hàng');
      setPartnerTab(0);
      return;
    }
    if (!partnerFormData.password || partnerFormData.password.length < 6) {
      toast.error('Mật khẩu đăng nhập phải có ít nhất 6 ký tự');
      setPartnerTab(0);
      return;
    }

    // Validate Restaurant info
    if (!partnerFormData.name.trim()) {
      toast.error('Vui lòng nhập tên nhà hàng / quán ăn');
      setPartnerTab(1);
      return;
    }
    if (!partnerFormData.address.trim()) {
      toast.error('Vui lòng nhập địa chỉ nhà hàng');
      setPartnerTab(1);
      return;
    }
    if (partnerFormData.latitude === '' || partnerFormData.longitude === '') {
      toast.error('Vui lòng nhập tọa độ vĩ độ và kinh độ của quán');
      setPartnerTab(1);
      return;
    }
    if (!partnerFormData.licenseImageUrl || !partnerFormData.licenseImageUrl.trim()) {
      toast.error('Vui lòng tải lên ảnh Giấy phép kinh doanh của quán');
      setPartnerTab(1);
      return;
    }

    setSubmittingPartner(true);
    try {
      const payload = {
        name: partnerFormData.name.trim(),
        phone: partnerFormData.phone.trim() || partnerFormData.ownerPhone.trim(),
        address: partnerFormData.address.trim(),
        latitude: parseFloat(partnerFormData.latitude),
        longitude: parseFloat(partnerFormData.longitude),
        imageUrl: partnerFormData.imageUrl.trim() || 'https://images.unsplash.com/photo-1555396273-367ea4eb4db5',
        licenseImageUrl: partnerFormData.licenseImageUrl.trim(),
        openTime: partnerFormData.openTime || '08:00',
        closeTime: partnerFormData.closeTime || '22:00',
        ownerName: partnerFormData.ownerName.trim(),
        ownerPhone: partnerFormData.ownerPhone.trim(),
        email: partnerFormData.email.trim() || undefined,
        password: partnerFormData.password,
        autoApprove: true,
      };

      await foodService.registerPartnerRestaurant(payload);
      toast.success(`Thêm đối tác nhà hàng "${payload.name}" thành công! Quán đã được tự động duyệt hoạt động.`);
      setOpenAddPartnerModal(false);
      resetPartnerForm();
      fetchRestaurants();
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Lỗi khi đăng ký đối tác nhà hàng';
      toast.error(msg);
    } finally {
      setSubmittingPartner(false);
    }
  };

  const fetchRestaurants = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await foodService.getAllRestaurants(searchTerm);
      setRestaurants(Array.isArray(data) ? data : []);
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Không thể tải danh sách nhà hàng';
      setError(msg);
      toast.error(msg);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRestaurants();
  }, []);

  const handleOpenMenu = async (restaurant) => {
    setSelectedRestaurant(restaurant);
    setMenuModalTab(0);
    setOpenMenuModal(true);
    setLoadingMenu(true);
    try {
      const items = await foodService.getMenuItems(restaurant.id);
      setMenuItems(items);
    } catch (err) {
      toast.error('Không thể tải thực đơn nhà hàng');
      setMenuItems([]);
    } finally {
      setLoadingMenu(false);
    }
  };


  const handleApproveRestaurant = async (restaurant) => {
    try {
      await foodService.approveRestaurant(restaurant.id);
      toast.success(`Đã phê duyệt nhà hàng "${restaurant.name}" thành công! Quán đã có thể mở cửa hoạt động.`);
      fetchRestaurants();
    } catch (err) {
      toast.error('Lỗi khi phê duyệt nhà hàng: ' + (err.response?.data?.message || err.message || ''));
    }
  };

  const handleOpenReject = (restaurant) => {
    setRejectDialog({
      open: true,
      restaurant,
      reason: 'Hồ sơ chưa đạt yêu cầu (giấy phép kinh doanh không hợp lệ hoặc thông tin chưa chính xác)',
    });
  };

  const handleSubmitReject = async () => {
    if (!rejectDialog.restaurant) return;
    if (!rejectDialog.reason.trim()) {
      toast.error('Vui lòng nhập lý do từ chối hồ sơ');
      return;
    }
    setRejectSubmitting(true);
    try {
      await foodService.rejectRestaurant(rejectDialog.restaurant.id, rejectDialog.reason.trim());
      toast.success(`Đã từ chối hồ sơ đăng ký của "${rejectDialog.restaurant.name}"`);
      setRejectDialog({ open: false, restaurant: null, reason: '' });
      setPartnerAppFilter('REJECTED');
      fetchRestaurants();
    } catch (err) {
      toast.error('Lỗi khi từ chối hồ sơ: ' + (err.response?.data?.message || err.message || ''));
    } finally {
      setRejectSubmitting(false);
    }
  };

  const isRejectedRestaurant = (r) => {
    if (!r || !r.isLocked) return false;
    const reason = (r.lockedReason || '').toLowerCase();
    return reason.includes('từ chối') || reason.includes('reject');
  };

  const isPendingRestaurant = (r) => {
    if (!r || !r.isLocked) return false;
    if (isRejectedRestaurant(r)) return false;
    const reason = (r.lockedReason || '').toLowerCase();
    return reason.includes('chờ') || reason.includes('duyệt') || reason.includes('partner') || !reason;
  };

  const approvedRestaurants = restaurants.filter((r) => !isPendingRestaurant(r) && !isRejectedRestaurant(r));
  const pendingRestaurants = restaurants.filter((r) => isPendingRestaurant(r));
  const rejectedRestaurants = restaurants.filter((r) => isRejectedRestaurant(r));
  const applicationRestaurants = restaurants.filter((r) => isPendingRestaurant(r) || isRejectedRestaurant(r));

  // Filter logic based on main tab (0: Approved, 1: Pending & Rejected Applications)
  const currentList =
    mainTab === 0
      ? approvedRestaurants
      : partnerAppFilter === 'PENDING'
      ? pendingRestaurants
      : partnerAppFilter === 'REJECTED'
      ? rejectedRestaurants
      : applicationRestaurants;

  const filteredRestaurants = currentList.filter((r) => {
    const matchSearch =
      searchTerm === '' ||
      r.name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      r.address?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      r.phone?.includes(searchTerm);

    if (mainTab === 1) return matchSearch;

    const matchStatus =
      statusFilter === 'ALL'
        ? true
        : r.status === statusFilter;

    return matchSearch && matchStatus;
  });

  const paginatedList = filteredRestaurants.slice(
    page * rowsPerPage,
    page * rowsPerPage + rowsPerPage
  );

  const stats = {
    total: restaurants.length,
    approved: approvedRestaurants.length,
    open: approvedRestaurants.filter((r) => r.status === 'OPEN').length,
    busy: approvedRestaurants.filter((r) => r.status === 'BUSY').length,
    closed: approvedRestaurants.filter((r) => r.status === 'CLOSED').length,
    pending: pendingRestaurants.length,
    rejected: rejectedRestaurants.length,
    applications: applicationRestaurants.length,
  };

  const getStatusChip = (status) => {
    switch (status) {
      case 'OPEN':
        return (
          <Chip
            label="🟢 Đang Mở Cửa"
            size="small"
            sx={{ bgcolor: 'rgba(21, 202, 32, 0.15)', color: '#15ca20', fontWeight: 700 }}
          />
        );
      case 'BUSY':
        return (
          <Chip
            label="🟡 Đang Bận"
            size="small"
            sx={{ bgcolor: 'rgba(255, 170, 0, 0.15)', color: '#ffaa00', fontWeight: 700 }}
          />
        );
      case 'CLOSED':
      default:
        return (
          <Chip
            label="🔴 Đóng Cửa"
            size="small"
            sx={{ bgcolor: 'rgba(255, 51, 102, 0.15)', color: '#ff3366', fontWeight: 700 }}
          />
        );
    }
  };

  return (
    <Box sx={{ p: { xs: 2, md: 3 } }}>
      {/* Header bar */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: { xs: 'flex-start', sm: 'center' }, mb: 2.5, flexDirection: { xs: 'column', sm: 'row' }, gap: 1.5 }}>
        <Box>
          <Typography variant="h5" sx={{ fontWeight: 800 }}>
            Quản Lý Nhà Hàng & Quán Ăn
          </Typography>
          <Typography variant="body2" sx={{ color: 'text.secondary', mt: 0.3, fontSize: { xs: '0.8rem', sm: '0.85rem' } }}>
            Quản lý thông tin nhà hàng đối tác, duyệt thực đơn và cấu hình trạng thái
          </Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 1.2, width: { xs: '100%', sm: 'auto' }, flexWrap: 'wrap' }}>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => {
              resetPartnerForm();
              setOpenAddPartnerModal(true);
            }}
            sx={{
              bgcolor: '#10b981',
              '&:hover': { bgcolor: '#059669' },
              borderRadius: 2,
              textTransform: 'none',
              fontWeight: 700,
              fontSize: '0.82rem',
              flex: { xs: 1, sm: 'none' },
              boxShadow: '0 4px 12px rgba(16, 185, 129, 0.3)',
            }}
          >
            Thêm Đối Tác Nhà Hàng
          </Button>
          <Button
            variant="outlined"
            startIcon={<RefreshIcon />}
            onClick={fetchRestaurants}
            disabled={loading}
            sx={{
              borderRadius: 2,
              textTransform: 'none',
              fontWeight: 700,
              fontSize: '0.82rem',
              flex: { xs: 1, sm: 'none' },
            }}
          >
            Làm Mới
          </Button>
        </Box>
      </Box>

      {/* Stats Cards */}
      <Grid container spacing={1.5} sx={{ mb: 2.5, width: '100%' }}>
        <Grid size={{ xs: 6, sm: 3 }}>
          <Card
            sx={{
              p: { xs: 1.5, sm: 2 },
              borderRadius: 2.5,
              bgcolor: 'background.paper',
              border: 1,
              borderColor: 'divider',
              display: 'flex',
              alignItems: 'center',
              gap: 1.2,
            }}
          >
            <Avatar sx={{ bgcolor: 'rgba(0, 140, 255, 0.12)', color: '#008cff', width: { xs: 38, sm: 44 }, height: { xs: 38, sm: 44 } }}>
              <RestaurantIcon sx={{ fontSize: { xs: 20, sm: 24 } }} />
            </Avatar>
            <Box>
              <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600, fontSize: { xs: '0.7rem', sm: '0.75rem' } }}>
                Tổng quán
              </Typography>
              <Typography variant="h6" sx={{ fontWeight: 800, color: 'text.primary', fontSize: { xs: '1rem', sm: '1.25rem' } }}>
                {stats.total}
              </Typography>
            </Box>
          </Card>
        </Grid>

        <Grid size={{ xs: 6, sm: 3 }}>
          <Card
            sx={{
              p: { xs: 1.5, sm: 2 },
              borderRadius: 2.5,
              bgcolor: 'background.paper',
              border: 1,
              borderColor: 'divider',
              display: 'flex',
              alignItems: 'center',
              gap: 1.2,
            }}
          >
            <Avatar sx={{ bgcolor: 'rgba(21, 202, 32, 0.12)', color: '#15ca20', width: { xs: 38, sm: 44 }, height: { xs: 38, sm: 44 } }}>
              <OpenIcon sx={{ fontSize: { xs: 20, sm: 24 } }} />
            </Avatar>
            <Box>
              <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600, fontSize: { xs: '0.7rem', sm: '0.75rem' } }}>
                Đang mở cửa
              </Typography>
              <Typography variant="h6" sx={{ fontWeight: 800, color: '#15ca20', fontSize: { xs: '1rem', sm: '1.25rem' } }}>
                {stats.open}
              </Typography>
            </Box>
          </Card>
        </Grid>

        <Grid size={{ xs: 6, sm: 3 }}>
          <Card
            sx={{
              p: { xs: 1.5, sm: 2 },
              borderRadius: 2.5,
              bgcolor: 'background.paper',
              border: 1,
              borderColor: 'divider',
              display: 'flex',
              alignItems: 'center',
              gap: 1.2,
            }}
          >
            <Avatar sx={{ bgcolor: 'rgba(255, 170, 0, 0.12)', color: '#ffaa00', width: { xs: 38, sm: 44 }, height: { xs: 38, sm: 44 } }}>
              <BusyIcon sx={{ fontSize: { xs: 20, sm: 24 } }} />
            </Avatar>
            <Box>
              <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600, fontSize: { xs: '0.7rem', sm: '0.75rem' } }}>
                Tạm bận
              </Typography>
              <Typography variant="h6" sx={{ fontWeight: 800, color: '#ffaa00', fontSize: { xs: '1rem', sm: '1.25rem' } }}>
                {stats.busy}
              </Typography>
            </Box>
          </Card>
        </Grid>

        <Grid size={{ xs: 6, sm: 3 }}>
          <Card
            sx={{
              p: { xs: 1.5, sm: 2 },
              borderRadius: 2.5,
              bgcolor: 'background.paper',
              border: 1,
              borderColor: 'divider',
              display: 'flex',
              alignItems: 'center',
              gap: 1.2,
            }}
          >
            <Avatar sx={{ bgcolor: 'rgba(255, 51, 102, 0.12)', color: '#ff3366', width: { xs: 38, sm: 44 }, height: { xs: 38, sm: 44 } }}>
              <ClosedIcon sx={{ fontSize: { xs: 20, sm: 24 } }} />
            </Avatar>
            <Box>
              <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600, fontSize: { xs: '0.7rem', sm: '0.75rem' } }}>
                Đóng cửa
              </Typography>
              <Typography variant="h6" sx={{ fontWeight: 800, color: '#ff3366', fontSize: { xs: '1rem', sm: '1.25rem' } }}>
                {stats.closed}
              </Typography>
            </Box>
          </Card>
        </Grid>
      </Grid>

      {/* Main Table Container */}
      <Card sx={{ borderRadius: 2.5, bgcolor: 'background.paper', border: 1, borderColor: 'divider', minHeight: 'calc(90vh - 160px)', display: 'flex', flexDirection: 'column' }}>
        {/* Main 2 Tabs: Quán đã duyệt vs Đơn chờ duyệt */}
        <Box sx={{ borderBottom: 1, borderColor: 'divider', px: { xs: 1.5, sm: 2 }, pt: 0.5, bgcolor: 'action.hover' }}>
          <Tabs
            value={mainTab}
            onChange={(_, val) => {
              setMainTab(val);
              setPage(0);
            }}
            textColor="primary"
            indicatorColor="primary"
            sx={{
              minHeight: 48,
              '& .MuiTab-root': {
                textTransform: 'none',
                fontWeight: 700,
                fontSize: { xs: '0.85rem', sm: '0.95rem' },
                minHeight: 48,
              },
            }}
          >
            <Tab
              icon={<RestaurantIcon sx={{ fontSize: 20 }} />}
              iconPosition="start"
              label={`Quán Ăn Omni (${stats.approved})`}
            />
            <Tab
              icon={<BusyIcon sx={{ fontSize: 20 }} />}
              iconPosition="start"
              label={
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <span>Đơn Quán Chờ Duyệt</span>
                  {stats.pending > 0 && (
                    <Chip
                      label={stats.pending}
                      size="small"
                      sx={{
                        height: 20,
                        fontSize: '0.72rem',
                        fontWeight: 800,
                        bgcolor: '#f59e0b',
                        color: '#fff',
                      }}
                    />
                  )}
                </Box>
              }
            />
          </Tabs>
        </Box>

        {/* Filter & Search Bar */}
        <Box
          sx={{
            p: { xs: 1.5, sm: 2 },
            display: 'flex',
            flexDirection: { xs: 'column', md: 'row' },
            gap: 2,
            justifyContent: 'space-between',
            alignItems: 'center',
            borderBottom: 1,
            borderColor: 'divider',
          }}
        >
          {mainTab === 0 ? (
            <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', width: { xs: '100%', md: 'auto' } }}>
              <Button
                variant={statusFilter === 'ALL' ? 'contained' : 'outlined'}
                onClick={() => { setStatusFilter('ALL'); setPage(0); }}
                size="small"
                sx={{ textTransform: 'none', fontWeight: 600, fontSize: { xs: '0.78rem', sm: '0.85rem' }, borderRadius: 2, flex: { xs: 1, sm: 'none' } }}
              >
                Tất cả ({stats.approved})
              </Button>
              <Button
                variant={statusFilter === 'OPEN' ? 'contained' : 'outlined'}
                onClick={() => { setStatusFilter('OPEN'); setPage(0); }}
                size="small"
                sx={{ textTransform: 'none', fontWeight: 600, fontSize: { xs: '0.78rem', sm: '0.85rem' }, borderRadius: 2, flex: { xs: 1, sm: 'none' }, color: statusFilter === 'OPEN' ? '#fff' : '#15ca20' }}
              >
                Mở cửa ({stats.open})
              </Button>
              <Button
                variant={statusFilter === 'BUSY' ? 'contained' : 'outlined'}
                onClick={() => { setStatusFilter('BUSY'); setPage(0); }}
                size="small"
                sx={{ textTransform: 'none', fontWeight: 600, fontSize: { xs: '0.78rem', sm: '0.85rem' }, borderRadius: 2, flex: { xs: 1, sm: 'none' }, color: statusFilter === 'BUSY' ? '#fff' : '#ffaa00' }}
              >
                Đang bận ({stats.busy})
              </Button>
              <Button
                variant={statusFilter === 'CLOSED' ? 'contained' : 'outlined'}
                onClick={() => { setStatusFilter('CLOSED'); setPage(0); }}
                size="small"
                sx={{ textTransform: 'none', fontWeight: 600, fontSize: { xs: '0.78rem', sm: '0.85rem' }, borderRadius: 2, flex: { xs: 1, sm: 'none' }, color: statusFilter === 'CLOSED' ? '#fff' : '#ff3366' }}
              >
                Đóng cửa ({stats.closed})
              </Button>
            </Box>
          ) : (
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, flexWrap: 'wrap', width: { xs: '100%', md: 'auto' } }}>
              <Button
                variant={partnerAppFilter === 'PENDING' ? 'contained' : 'outlined'}
                onClick={() => { setPartnerAppFilter('PENDING'); setPage(0); }}
                size="small"
                sx={{
                  textTransform: 'none',
                  fontWeight: 700,
                  fontSize: { xs: '0.78rem', sm: '0.85rem' },
                  borderRadius: 2,
                  flex: { xs: 1, sm: 'none' },
                  bgcolor: partnerAppFilter === 'PENDING' ? '#f59e0b' : 'transparent',
                  color: partnerAppFilter === 'PENDING' ? '#fff' : '#d97706',
                  borderColor: '#f59e0b',
                  '&:hover': {
                    bgcolor: partnerAppFilter === 'PENDING' ? '#d97706' : 'rgba(245, 158, 11, 0.08)',
                    borderColor: '#d97706',
                  },
                }}
              >
                Chờ duyệt ({stats.pending})
              </Button>
              <Button
                variant={partnerAppFilter === 'REJECTED' ? 'contained' : 'outlined'}
                onClick={() => { setPartnerAppFilter('REJECTED'); setPage(0); }}
                size="small"
                sx={{
                  textTransform: 'none',
                  fontWeight: 700,
                  fontSize: { xs: '0.78rem', sm: '0.85rem' },
                  borderRadius: 2,
                  flex: { xs: 1, sm: 'none' },
                  bgcolor: partnerAppFilter === 'REJECTED' ? '#ef4444' : 'transparent',
                  color: partnerAppFilter === 'REJECTED' ? '#fff' : '#dc2626',
                  borderColor: '#ef4444',
                  '&:hover': {
                    bgcolor: partnerAppFilter === 'REJECTED' ? '#dc2626' : 'rgba(239, 68, 68, 0.08)',
                    borderColor: '#dc2626',
                  },
                }}
              >
                Đã từ chối ({stats.rejected})
              </Button>
              <Button
                variant={partnerAppFilter === 'ALL' ? 'contained' : 'outlined'}
                onClick={() => { setPartnerAppFilter('ALL'); setPage(0); }}
                size="small"
                sx={{
                  textTransform: 'none',
                  fontWeight: 700,
                  fontSize: { xs: '0.78rem', sm: '0.85rem' },
                  borderRadius: 2,
                  flex: { xs: 1, sm: 'none' },
                }}
              >
                Tất cả đơn ({stats.applications})
              </Button>
            </Box>
          )}

          <TextField
            size="small"
            placeholder={mainTab === 0 ? "Tìm theo tên quán, địa chỉ, SĐT..." : "Tìm đơn theo tên quán, địa chỉ, SĐT..."}
            value={searchTerm}
            onChange={(e) => { setSearchTerm(e.target.value); setPage(0); }}
            sx={{ width: { xs: '100%', md: 320 } }}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon fontSize="small" sx={{ color: 'text.secondary' }} />
                </InputAdornment>
              ),
            }}
          />
        </Box>

        {error && (
          <Alert severity="error" sx={{ m: 2 }}>
            {error}
          </Alert>
        )}

        {/* Table */}
        <TableContainer sx={{ overflowX: 'auto', width: '100%', flex: 1, minHeight: 380 }}>
          <Table sx={{ minWidth: 700 }} size="small">
            <TableHead sx={{ bgcolor: 'action.hover' }}>
              <TableRow>
                <TableCell sx={{ fontWeight: 700, width: 60 }}>#ID</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Nhà hàng</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Địa chỉ</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Số điện thoại</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Giờ phục vụ</TableCell>
                {mainTab === 0 ? (
                  <>
                    <TableCell sx={{ fontWeight: 700 }}>Trạng thái</TableCell>
                    <TableCell sx={{ fontWeight: 700 }}>Gian hàng</TableCell>
                  </>
                ) : (
                  <>
                    <TableCell sx={{ fontWeight: 700 }}>Chủ quán</TableCell>
                    <TableCell sx={{ fontWeight: 700 }}>Trạng thái hồ sơ</TableCell>
                  </>
                )}
                <TableCell sx={{ fontWeight: 700, textAlign: 'center' }}>Thao tác</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {loading ? (
                <TableRow>
                  <TableCell colSpan={8} align="center" sx={{ py: 6 }}>
                    <CircularProgress size={36} />
                    <Typography variant="body2" sx={{ mt: 1.5, color: 'text.secondary' }}>
                      Đang tải danh sách quán ăn...
                    </Typography>
                  </TableCell>
                </TableRow>
              ) : paginatedList.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={8} align="center" sx={{ py: 6 }}>
                    <RestaurantIcon sx={{ fontSize: 48, color: 'text.secondary', mb: 1 }} />
                    <Typography variant="body1" sx={{ fontWeight: 600, color: 'text.secondary' }}>
                      {mainTab === 0
                        ? 'Không tìm thấy nhà hàng nào'
                        : partnerAppFilter === 'PENDING'
                        ? 'Không có đơn đăng ký quán nào chờ duyệt'
                        : partnerAppFilter === 'REJECTED'
                        ? 'Không có hồ sơ quán nào bị từ chối'
                        : 'Không tìm thấy đơn đăng ký quán nào'}
                    </Typography>
                  </TableCell>
                </TableRow>
              ) : (
                paginatedList.map((r) => (
                  <TableRow key={r.id} hover sx={{ '&:last-child td, &:last-child th': { border: 0 } }}>
                    <TableCell sx={{ fontWeight: 700, color: 'text.secondary' }}>
                      #{r.id}
                    </TableCell>
                    <TableCell>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                        <Avatar
                          src={r.imageUrl}
                          alt={r.name}
                          variant="rounded"
                          sx={{ width: 44, height: 44, borderRadius: 2 }}
                        >
                          🍲
                        </Avatar>
                        <Box>
                          <Typography variant="body2" sx={{ fontWeight: 700, color: 'text.primary' }}>
                            {r.name}
                          </Typography>
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.6, mt: 0.2 }}>
                            <Typography variant="caption" sx={{ color: '#f59e0b', fontWeight: 700 }}>
                              ⭐ {Number(r.rating || 5.0).toFixed(1)}
                            </Typography>
                            <Typography variant="caption" sx={{ color: 'text.secondary' }}>
                              ({r.reviewCount || 0} đánh giá)
                            </Typography>
                            <Typography variant="caption" sx={{ color: 'text.secondary' }}>
                              • Chủ: #{r.ownerId || 'N/A'}
                            </Typography>
                          </Box>
                        </Box>
                      </Box>
                    </TableCell>
                    <TableCell sx={{ maxWidth: 220, fontSize: '0.85rem' }}>
                      {r.address}
                    </TableCell>
                    <TableCell sx={{ fontWeight: 600 }}>
                      {r.phone || 'Chưa cập nhật'}
                    </TableCell>
                    <TableCell sx={{ fontSize: '0.85rem' }}>
                      🕒 {r.openTime || '07:00'} - {r.closeTime || '22:00'}
                    </TableCell>
                    {mainTab === 0 ? (
                      <>
                        <TableCell>
                          {getStatusChip(r.status)}
                        </TableCell>
                        <TableCell>
                          {r.isLocked ? (
                            <Tooltip title={`Lý do: ${r.lockedReason || 'Khóa bởi Admin'}`}>
                              <Chip
                                label="ĐÃ KHÓA"
                                size="small"
                                sx={{
                                  fontWeight: 800,
                                  bgcolor: 'rgba(239, 68, 68, 0.12)',
                                  color: '#dc2626',
                                  border: '1px solid rgba(239, 68, 68, 0.25)',
                                }}
                              />
                            </Tooltip>
                          ) : (
                            <Chip
                              label="HOẠT ĐỘNG"
                              color="success"
                              size="small"
                              variant="outlined"
                              sx={{ fontWeight: 700 }}
                            />
                          )}
                        </TableCell>
                      </>
                    ) : (
                      <>
                        <TableCell sx={{ fontWeight: 600, color: 'text.primary' }}>
                          Chủ quán #{r.ownerId || 'N/A'}
                        </TableCell>
                        <TableCell>
                          {isRejectedRestaurant(r) ? (
                            <Box>
                              <Tooltip title={r.lockedReason || 'Hồ sơ bị từ chối'}>
                                <Chip
                                  label="ĐÃ TỪ CHỐI"
                                  size="small"
                                  sx={{
                                    fontWeight: 800,
                                    bgcolor: 'rgba(239, 68, 68, 0.15)',
                                    color: '#dc2626',
                                    border: '1px solid rgba(239, 68, 68, 0.3)',
                                  }}
                                />
                              </Tooltip>
                              {r.lockedReason && (
                                <Typography
                                  variant="caption"
                                  sx={{
                                    display: 'block',
                                    color: '#dc2626',
                                    fontSize: '0.72rem',
                                    maxWidth: 220,
                                    overflow: 'hidden',
                                    textOverflow: 'ellipsis',
                                    whiteSpace: 'nowrap',
                                    mt: 0.3,
                                  }}
                                  title={r.lockedReason}
                                >
                                  {r.lockedReason.replace(/^Từ chối hồ sơ đăng ký:\s*/i, '')}
                                </Typography>
                              )}
                            </Box>
                          ) : (
                            <Chip
                              label="CHỜ DUYỆT"
                              size="small"
                              sx={{
                                fontWeight: 800,
                                bgcolor: 'rgba(245, 158, 11, 0.15)',
                                color: '#d97706',
                                border: '1px solid rgba(245, 158, 11, 0.3)',
                              }}
                            />
                          )}
                        </TableCell>
                      </>
                    )}
                    <TableCell align="center">
                      {mainTab === 0 ? (
                        <Stack direction="row" spacing={0.8} justifyContent="center" alignItems="center">
                          {r.licenseImageUrl && (
                            <Tooltip title="Xem Giấy phép kinh doanh">
                              <IconButton
                                size="small"
                                onClick={() => setViewLicenseDialog({ open: true, restaurant: r })}
                                sx={{
                                  color: '#7c3aed',
                                  bgcolor: 'rgba(124, 58, 237, 0.08)',
                                  border: '1px solid rgba(124, 58, 237, 0.25)',
                                  borderRadius: 1.5,
                                  p: 0.8,
                                  '&:hover': {
                                    bgcolor: 'rgba(124, 58, 237, 0.2)',
                                    transform: 'scale(1.05)',
                                  },
                                  transition: 'all 0.2s',
                                }}
                              >
                                <LicenseIcon fontSize="small" />
                              </IconButton>
                            </Tooltip>
                          )}
                          <Tooltip title="Xem thực đơn & Đánh giá">
                            <IconButton
                              size="small"
                              onClick={() => handleOpenMenu(r)}
                              sx={{
                                color: '#0284c7',
                                bgcolor: 'rgba(2, 132, 199, 0.08)',
                                border: '1px solid rgba(2, 132, 199, 0.2)',
                                borderRadius: 1.5,
                                p: 0.8,
                                '&:hover': {
                                  bgcolor: 'rgba(2, 132, 199, 0.18)',
                                  transform: 'scale(1.05)',
                                },
                                transition: 'all 0.2s',
                              }}
                            >
                              <MenuIcon fontSize="small" />
                            </IconButton>
                          </Tooltip>
                          <Tooltip title={r.isLocked ? 'Mở khóa gian hàng' : 'Khóa gian hàng'}>
                            <IconButton
                              size="small"
                              onClick={() => handleOpenLockRestaurant(r)}
                              sx={{
                                color: r.isLocked ? '#16a34a' : '#ef4444',
                                bgcolor: r.isLocked ? 'rgba(22, 163, 74, 0.08)' : 'rgba(239, 68, 68, 0.08)',
                                border: '1px solid',
                                borderColor: r.isLocked ? 'rgba(22, 163, 74, 0.25)' : 'rgba(239, 68, 68, 0.25)',
                                borderRadius: 1.5,
                                p: 0.8,
                                '&:hover': {
                                  bgcolor: r.isLocked ? 'rgba(22, 163, 74, 0.18)' : 'rgba(239, 68, 68, 0.18)',
                                  transform: 'scale(1.05)',
                                },
                                transition: 'all 0.2s',
                              }}
                            >
                              {r.isLocked ? <UnlockIcon fontSize="small" /> : <LockIcon fontSize="small" />}
                            </IconButton>
                          </Tooltip>
                        </Stack>
                      ) : (
                        <Button
                          size="small"
                          variant="contained"
                          startIcon={<ViewIcon sx={{ fontSize: 16 }} />}
                          onClick={() => setAppraisalModal({ open: true, restaurant: r })}
                          sx={{
                            bgcolor: isRejectedRestaurant(r) ? '#64748b' : '#f59e0b',
                            '&:hover': { bgcolor: isRejectedRestaurant(r) ? '#475569' : '#d97706' },
                            textTransform: 'none',
                            fontWeight: 700,
                            fontSize: '0.8rem',
                            py: 0.5,
                            px: 1.8,
                            borderRadius: 2,
                            whiteSpace: 'nowrap',
                            boxShadow: isRejectedRestaurant(r) ? 'none' : '0 2px 8px rgba(245, 158, 11, 0.25)',
                          }}
                        >
                          Xem chi tiết
                        </Button>
                      )}
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </TableContainer>

        {/* Pagination */}
        <TablePagination
          component="div"
          count={filteredRestaurants.length}
          page={page}
          onPageChange={(_, newPage) => setPage(newPage)}
          rowsPerPage={rowsPerPage}
          onRowsPerPageChange={(e) => {
            setRowsPerPage(parseInt(e.target.value, 10));
            setPage(0);
          }}
          labelRowsPerPage="Số hàng mỗi trang:"
          rowsPerPageOptions={[5, 10, 25, 50]}
          sx={{ mt: 'auto', pt: 1.5 }}
        />
      </Card>

      {/* Modal: Xem thực đơn món ăn của quán */}
      <Dialog
        open={openMenuModal}
        onClose={() => setOpenMenuModal(false)}
        maxWidth="md"
        fullWidth
        PaperProps={{ sx: { borderRadius: 3 } }}
      >
        <DialogTitle component="div" sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
            <MenuIcon sx={{ color: '#008cff' }} />
            <Box>
              <Typography variant="h6" sx={{ fontWeight: 800 }}>
                Thực Đơn: {selectedRestaurant?.name}
              </Typography>
              <Typography variant="caption" sx={{ color: 'text.secondary' }}>
                📍 {selectedRestaurant?.address}
              </Typography>
            </Box>
          </Box>
          <IconButton onClick={() => setOpenMenuModal(false)} size="small">
            <CloseIcon />
          </IconButton>
        </DialogTitle>
        <Divider />
        <Tabs
          value={menuModalTab}
          onChange={(_, v) => setMenuModalTab(v)}
          sx={{ px: 3, borderBottom: 1, borderColor: 'divider', bgcolor: '#f8fafc' }}
        >
          <Tab
            label={`🍽️ Thực Đơn (${menuItems.length})`}
            sx={{ textTransform: 'none', fontWeight: 700 }}
          />
          <Tab
            label={`⭐ Đánh Giá & Nhận Xét (${selectedRestaurant?.reviewCount || 0})`}
            sx={{ textTransform: 'none', fontWeight: 700 }}
          />
        </Tabs>
        <DialogContent sx={{ p: 3 }}>
          {menuModalTab === 0 ? (
            loadingMenu ? (
              <Box sx={{ display: 'flex', justifyContent: 'center', py: 5 }}>
                <CircularProgress />
              </Box>
            ) : menuItems.length === 0 ? (
              <Box sx={{ textAlign: 'center', py: 5 }}>
                <Typography variant="body1" sx={{ color: 'text.secondary' }}>
                  Quán chưa có món ăn nào trong thực đơn.
                </Typography>
              </Box>
            ) : (
              <Grid container spacing={2}>
                {menuItems.map((item) => (
                  <Grid size={{ xs: 12, sm: 6, md: 4 }} key={item.id}>
                    <Card
                      sx={{
                        height: '100%',
                        display: 'flex',
                        flexDirection: 'column',
                        borderRadius: 2,
                        border: 1,
                        borderColor: 'divider',
                        overflow: 'hidden',
                      }}
                    >
                      <Box
                        component="img"
                        src={item.imageUrl || 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c'}
                        alt={item.name}
                        sx={{ height: 120, width: '100%', objectFit: 'cover' }}
                      />
                      <Box sx={{ p: 1.5, flexGrow: 1, display: 'flex', flexDirection: 'column' }}>
                        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 0.5 }}>
                          <Typography variant="subtitle2" sx={{ fontWeight: 700 }}>
                            {item.name}
                          </Typography>
                          <Chip
                            label={item.isAvailable !== false ? 'Còn món' : 'Hết món'}
                            size="small"
                            color={item.isAvailable !== false ? 'success' : 'default'}
                            sx={{ fontSize: '0.7rem', height: 20 }}
                          />
                        </Box>
                        <Typography variant="caption" sx={{ color: 'text.secondary', mb: 1, flexGrow: 1 }}>
                          {item.description || 'Không có mô tả chi tiết'}
                        </Typography>
                        <Typography variant="body2" sx={{ fontWeight: 800, color: '#f97316' }}>
                          {Number(item.price || 0).toLocaleString('vi-VN')} đ
                        </Typography>
                      </Box>
                    </Card>
                  </Grid>
                ))}
              </Grid>
            )
          ) : (
            <RestaurantReviewsList restaurantId={selectedRestaurant?.id} />
          )}
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={() => setOpenMenuModal(false)} variant="contained" sx={{ textTransform: 'none' }}>
            Đóng
          </Button>
        </DialogActions>
      </Dialog>


      {/* Modal: Thêm Đối Tác Nhà Hàng Mới */}
      <Dialog
        open={openAddPartnerModal}
        onClose={() => !submittingPartner && setOpenAddPartnerModal(false)}
        maxWidth="md"
        fullWidth
        PaperProps={{ sx: { borderRadius: 3, overflow: 'hidden' } }}
      >
        <DialogTitle component="div" sx={{ fontWeight: 800, bgcolor: 'background.paper', borderBottom: 1, borderColor: 'divider', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
            <Avatar sx={{ bgcolor: 'rgba(16, 185, 129, 0.15)', color: '#10b981' }}>
              <AddIcon />
            </Avatar>
            <Box>
              <Typography variant="h6" sx={{ fontWeight: 800 }}>
                Thêm Mới Đối Tác Nhà Hàng
              </Typography>
              <Typography variant="caption" sx={{ color: 'text.secondary' }}>
                Đồng thời tạo tài khoản phân quyền RESTAURANT và khởi tạo hồ sơ quán
              </Typography>
            </Box>
          </Box>
          <IconButton onClick={() => setOpenAddPartnerModal(false)} disabled={submittingPartner} size="small">
            <CloseIcon />
          </IconButton>
        </DialogTitle>

        <DialogContent sx={{ p: 0 }}>
          <Box sx={{ borderBottom: 1, borderColor: 'divider', px: 3, pt: 2, bgcolor: 'background.default' }}>
            <Tabs
              value={partnerTab}
              onChange={(e, val) => setPartnerTab(val)}
              textColor="primary"
              indicatorColor="primary"
              variant="fullWidth"
            >
              <Tab
                icon={<PersonIcon sx={{ fontSize: 20 }} />}
                iconPosition="start"
                label="1. Tài Khoản Chủ Quán"
                sx={{ textTransform: 'none', fontWeight: 700 }}
              />
              <Tab
                icon={<RestaurantIcon sx={{ fontSize: 20 }} />}
                iconPosition="start"
                label="2. Thông Tin Nhà Hàng"
                sx={{ textTransform: 'none', fontWeight: 700 }}
              />
            </Tabs>
          </Box>

          <Box sx={{ p: 3 }}>
            {/* Tab 0: Owner Account */}
            {partnerTab === 0 && (
              <Stack spacing={2.5}>
                <Alert severity="info" sx={{ borderRadius: 2, fontSize: '0.875rem' }}>
                  Tài khoản được tạo sẽ có phân quyền <b>RESTAURANT</b>, dùng để đăng nhập và quản lý thực đơn/đơn hàng dành riêng cho quán.
                </Alert>

                {/* Row 1: Owner Name */}
                <Box>
                  <Typography variant="subtitle2" sx={{ fontWeight: 700, mb: 0.8 }}>
                    Họ và tên chủ nhà hàng <span style={{ color: '#ef4444' }}>*</span>
                  </Typography>
                  <TextField
                    fullWidth
                    required
                    name="ownerName"
                    value={partnerFormData.ownerName}
                    onChange={handlePartnerInputChange}
                    placeholder="VD: Nguyễn Văn A"
                    InputProps={{
                      startAdornment: (
                        <InputAdornment position="start">
                          <PersonIcon sx={{ color: 'text.secondary' }} />
                        </InputAdornment>
                      ),
                    }}
                  />
                </Box>

                {/* Row 2: Phone & Email */}
                <Box sx={{ display: 'flex', gap: 2, flexDirection: { xs: 'column', sm: 'row' } }}>
                  <Box sx={{ flex: 1 }}>
                    <Typography variant="subtitle2" sx={{ fontWeight: 700, mb: 0.8 }}>
                      Số điện thoại đăng nhập <span style={{ color: '#ef4444' }}>*</span>
                    </Typography>
                    <TextField
                      fullWidth
                      required
                      name="ownerPhone"
                      value={partnerFormData.ownerPhone}
                      onChange={handlePartnerInputChange}
                      placeholder="VD: 0987654321"
                      InputProps={{
                        startAdornment: (
                          <InputAdornment position="start">
                            <PhoneIcon sx={{ color: 'text.secondary' }} />
                          </InputAdornment>
                        ),
                      }}
                    />
                  </Box>
                  <Box sx={{ flex: 1 }}>
                    <Typography variant="subtitle2" sx={{ fontWeight: 700, mb: 0.8 }}>
                      Địa chỉ Email (tùy chọn)
                    </Typography>
                    <TextField
                      fullWidth
                      name="email"
                      type="email"
                      value={partnerFormData.email}
                      onChange={handlePartnerInputChange}
                      placeholder="VD: chuquan@gmail.com"
                      InputProps={{
                        startAdornment: (
                          <InputAdornment position="start">
                            <EmailIcon sx={{ color: 'text.secondary' }} />
                          </InputAdornment>
                        ),
                      }}
                    />
                  </Box>
                </Box>

                {/* Row 3: Password */}
                <Box>
                  <Typography variant="subtitle2" sx={{ fontWeight: 700, mb: 0.8 }}>
                    Mật khẩu đăng nhập <span style={{ color: '#ef4444' }}>*</span>
                  </Typography>
                  <TextField
                    fullWidth
                    required
                    name="password"
                    type="password"
                    value={partnerFormData.password}
                    onChange={handlePartnerInputChange}
                    placeholder="Nhập mật khẩu (tối thiểu 6 ký tự)"
                    InputProps={{
                      startAdornment: (
                        <InputAdornment position="start">
                          <LockIcon sx={{ color: 'text.secondary' }} />
                        </InputAdornment>
                      ),
                    }}
                  />
                </Box>

                <Box sx={{ display: 'flex', justifyContent: 'flex-end', pt: 1 }}>
                  <Button
                    variant="contained"
                    onClick={() => setPartnerTab(1)}
                    sx={{ textTransform: 'none', fontWeight: 700, py: 1, px: 3, borderRadius: 2 }}
                  >
                    Tiếp Tục: Điền Thông Tin Quán ➔
                  </Button>
                </Box>
              </Stack>
            )}

            {/* Tab 1: Restaurant Profile */}
            {partnerTab === 1 && (
              <Stack spacing={2.5}>
                {/* Row 1: Restaurant Name */}
                <Box>
                  <Typography variant="subtitle2" sx={{ fontWeight: 700, mb: 0.8 }}>
                    Tên nhà hàng / quán ăn <span style={{ color: '#ef4444' }}>*</span>
                  </Typography>
                  <TextField
                    fullWidth
                    required
                    name="name"
                    value={partnerFormData.name}
                    onChange={handlePartnerInputChange}
                    placeholder="VD: Cơm Tấm Sài Gòn 123"
                    InputProps={{
                      startAdornment: (
                        <InputAdornment position="start">
                          <RestaurantIcon sx={{ color: 'text.secondary' }} />
                        </InputAdornment>
                      ),
                    }}
                  />
                </Box>

                {/* Row 2: Hotline & Address */}
                <Box>
                  <Typography variant="subtitle2" sx={{ fontWeight: 700, mb: 0.8 }}>
                    Hotline quán (nếu trống sẽ lấy SĐT chủ)
                  </Typography>
                  <TextField
                    fullWidth
                    name="phone"
                    value={partnerFormData.phone}
                    onChange={handlePartnerInputChange}
                    placeholder="VD: 0283899999"
                    InputProps={{
                      startAdornment: (
                        <InputAdornment position="start">
                          <PhoneIcon sx={{ color: 'text.secondary' }} />
                        </InputAdornment>
                      ),
                    }}
                  />
                </Box>

                {/* Row 3: Address & Coordinates with Autocomplete */}
                <Box>
                  <AddressAutocomplete
                    label="Địa chỉ nhà hàng"
                    required
                    value={partnerFormData.address}
                    latitude={partnerFormData.latitude}
                    longitude={partnerFormData.longitude}
                    onChangeAddress={(newAddr) =>
                      setPartnerFormData((prev) => ({ ...prev, address: newAddr }))
                    }
                    onSelectLocation={({ address, latitude, longitude }) => {
                      setPartnerFormData((prev) => ({
                        ...prev,
                        address,
                        latitude,
                        longitude,
                      }));
                    }}
                    onChangeCoordinates={({ latitude, longitude }) => {
                      setPartnerFormData((prev) => ({
                        ...prev,
                        latitude,
                        longitude,
                      }));
                    }}
                  />
                </Box>

                {/* Ảnh đại diện nhà hàng */}
                <ImageUploadField
                  label="Ảnh đại diện quán ăn"
                  value={partnerFormData.imageUrl}
                  onChange={(url) =>
                    setPartnerFormData((prev) => ({ ...prev, imageUrl: url }))
                  }
                  helperText="Tải lên ảnh đại diện quán ăn hoặc dán link ảnh"
                  placeholder="Dán liên kết ảnh quán ăn (URL)..."
                />

                {/* Row 6: Open & Close Time */}
                <Box sx={{ display: 'flex', gap: 2, flexDirection: { xs: 'column', sm: 'row' } }}>
                  <Box sx={{ flex: 1 }}>
                    <Typography variant="subtitle2" sx={{ fontWeight: 700, mb: 0.8 }}>
                      Giờ mở cửa
                    </Typography>
                    <TextField
                      fullWidth
                      name="openTime"
                      value={partnerFormData.openTime}
                      onChange={handlePartnerInputChange}
                      placeholder="08:00"
                      InputProps={{
                        startAdornment: (
                          <InputAdornment position="start">
                            <TimeIcon sx={{ color: 'text.secondary' }} />
                          </InputAdornment>
                        ),
                      }}
                    />
                  </Box>
                  <Box sx={{ flex: 1 }}>
                    <Typography variant="subtitle2" sx={{ fontWeight: 700, mb: 0.8 }}>
                      Giờ đóng cửa
                    </Typography>
                    <TextField
                      fullWidth
                      name="closeTime"
                      value={partnerFormData.closeTime}
                      onChange={handlePartnerInputChange}
                      placeholder="22:00"
                      InputProps={{
                        startAdornment: (
                          <InputAdornment position="start">
                            <TimeIcon sx={{ color: 'text.secondary' }} />
                          </InputAdornment>
                        ),
                      }}
                    />
                  </Box>
                </Box>

                {/* Giấy phép kinh doanh */}
                <ImageUploadField
                  label="Ảnh giấy phép kinh doanh"
                  required
                  value={partnerFormData.licenseImageUrl}
                  onChange={(url) =>
                    setPartnerFormData((prev) => ({ ...prev, licenseImageUrl: url }))
                  }
                  helperText="Tải lên ảnh chụp rõ nét Giấy phép kinh doanh / Giấy chứng nhận ĐKKD để lưu trữ và kiểm duyệt"
                />
              </Stack>
            )}
          </Box>
        </DialogContent>

        <DialogActions sx={{ p: 2.5, bgcolor: 'background.paper', borderTop: 1, borderColor: 'divider' }}>
          <Button
            onClick={() => setOpenAddPartnerModal(false)}
            disabled={submittingPartner}
            sx={{ textTransform: 'none' }}
          >
            Hủy
          </Button>
          {partnerTab === 1 && (
            <Button
              onClick={() => setPartnerTab(0)}
              disabled={submittingPartner}
              sx={{ textTransform: 'none' }}
            >
              ⬅ Quay lại bước 1
            </Button>
          )}
          <Button
            variant="contained"
            onClick={handleSubmitAddPartner}
            disabled={submittingPartner}
            sx={{
              bgcolor: '#10b981',
              '&:hover': { bgcolor: '#059669' },
              textTransform: 'none',
              fontWeight: 700,
              px: 3,
            }}
          >
            {submittingPartner ? (
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <CircularProgress size={18} color="inherit" />
                <span>Đang khởi tạo...</span>
              </Box>
            ) : (
              'Hoàn Tất Đăng Ký Đối Tác'
            )}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Modal: Khóa / Mở Khóa Gian Hàng */}
      <Dialog open={openLockModal} onClose={handleCloseLockRestaurant} maxWidth="xs" fullWidth>
        <DialogTitle component="div" sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', pb: 1 }}>
          <Typography variant="h6" sx={{ fontWeight: 800 }}>
            {selectedRestaurantForLock?.isLocked ? 'Mở khóa gian hàng quán ăn' : 'Khóa gian hàng quán ăn'}
          </Typography>
          <IconButton onClick={handleCloseLockRestaurant} size="small" disabled={lockSubmitting}>
            <CloseIcon fontSize="small" />
          </IconButton>
        </DialogTitle>
        <DialogContent dividers>
          <Typography variant="body2" sx={{ mb: 2, color: 'text.secondary' }}>
            {selectedRestaurantForLock?.isLocked
              ? `Bạn có chắc muốn mở khóa cho gian hàng "${selectedRestaurantForLock?.name}" không? Quán sẽ có thể mở cửa phục vụ trở lại.`
              : `Khóa gian hàng "${selectedRestaurantForLock?.name}" sẽ tự động chuyển quán sang trạng thái Đóng cửa và ẩn khỏi kết quả tìm kiếm của khách hàng.`}
          </Typography>
          {!selectedRestaurantForLock?.isLocked && (
            <TextField
              label="Lý do khóa gian hàng"
              fullWidth
              multiline
              rows={3}
              required
              value={lockReason}
              onChange={(e) => setLockReason(e.target.value)}
              placeholder="Nhập lý do vi phạm (ví dụ: Vi phạm vệ sinh ATTP, gian lận giá...)"
              size="small"
            />
          )}
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={handleCloseLockRestaurant} disabled={lockSubmitting} color="inherit">
            Hủy
          </Button>
          <Button
            onClick={handleToggleLockRestaurantSubmit}
            variant="contained"
            color={selectedRestaurantForLock?.isLocked ? 'success' : 'error'}
            disabled={lockSubmitting}
          >
            {lockSubmitting ? <CircularProgress size={20} color="inherit" /> : selectedRestaurantForLock?.isLocked ? 'Mở Khóa Ngay' : 'Xác Nhận Khóa'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Modal: Xem Giấy Phép Kinh Doanh */}
      <Dialog
        open={viewLicenseDialog.open}
        onClose={() => setViewLicenseDialog({ open: false, restaurant: null })}
        maxWidth="md"
        fullWidth
      >
        <DialogTitle
          component="div"
          sx={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            bgcolor: 'background.paper',
            pb: 1.5,
          }}
        >
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.2 }}>
            <LicenseIcon sx={{ color: '#7c3aed', fontSize: 28 }} />
            <Box>
              <Typography variant="h6" sx={{ fontWeight: 800 }}>
                Giấy phép kinh doanh
              </Typography>
              <Typography variant="caption" sx={{ color: 'text.secondary' }}>
                Nhà hàng: {viewLicenseDialog.restaurant?.name} (ID: #{viewLicenseDialog.restaurant?.id})
              </Typography>
            </Box>
          </Box>
          <IconButton
            onClick={() => setViewLicenseDialog({ open: false, restaurant: null })}
            size="small"
          >
            <CloseIcon fontSize="small" />
          </IconButton>
        </DialogTitle>
        <DialogContent dividers sx={{ textAlign: 'center', bgcolor: '#f8fafc', p: 3 }}>
          {viewLicenseDialog.restaurant?.licenseImageUrl ? (
            <Box
              component="img"
              src={viewLicenseDialog.restaurant.licenseImageUrl}
              alt={`GPKD - ${viewLicenseDialog.restaurant?.name}`}
              sx={{
                maxWidth: '100%',
                maxHeight: '70vh',
                objectFit: 'contain',
                borderRadius: 2,
                boxShadow: '0 4px 20px rgba(0,0,0,0.12)',
              }}
            />
          ) : (
            <Typography variant="body2" sx={{ color: 'text.secondary', py: 4 }}>
              Không có hình ảnh giấy phép kinh doanh
            </Typography>
          )}
        </DialogContent>
        <DialogActions sx={{ p: 2, justifyContent: 'space-between' }}>
          <Box sx={{ display: 'flex', gap: 2 }}>
            <Typography variant="caption" sx={{ color: 'text.secondary' }}>
              📍 {viewLicenseDialog.restaurant?.address}
            </Typography>
            <Typography variant="caption" sx={{ color: 'text.secondary' }}>
              📞 {viewLicenseDialog.restaurant?.phone}
            </Typography>
          </Box>
          <Button
            onClick={() => setViewLicenseDialog({ open: false, restaurant: null })}
            variant="outlined"
            sx={{ textTransform: 'none' }}
          >
            Đóng
          </Button>
        </DialogActions>
      </Dialog>

      {/* Modal: Từ Chối Đơn Đăng Ký Quán */}
      <Dialog
        open={rejectDialog.open}
        onClose={() => !rejectSubmitting && setRejectDialog({ open: false, restaurant: null, reason: '' })}
        maxWidth="xs"
        fullWidth
      >
        <DialogTitle sx={{ fontWeight: 800, pb: 1, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Typography variant="h6" sx={{ fontWeight: 800, color: '#ef4444' }}>
            Từ chối duyệt hồ sơ quán
          </Typography>
          <IconButton
            size="small"
            disabled={rejectSubmitting}
            onClick={() => setRejectDialog({ open: false, restaurant: null, reason: '' })}
          >
            <CloseIcon fontSize="small" />
          </IconButton>
        </DialogTitle>
        <DialogContent dividers>
          <Typography variant="body2" sx={{ mb: 2, color: 'text.secondary' }}>
            Từ chối duyệt cho quán <b>"{rejectDialog.restaurant?.name}"</b> (Mã: #{rejectDialog.restaurant?.id}). Quán sẽ không được phép hoạt động trên nền tảng.
          </Typography>
          <TextField
            label="Lý do từ chối hồ sơ"
            fullWidth
            multiline
            rows={3}
            required
            value={rejectDialog.reason}
            onChange={(e) => setRejectDialog((prev) => ({ ...prev, reason: e.target.value }))}
            placeholder="Nhập lý do từ chối (VD: Giấy phép kinh doanh không khớp địa chỉ, ảnh mờ...)"
            size="small"
          />
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button
            onClick={() => setRejectDialog({ open: false, restaurant: null, reason: '' })}
            disabled={rejectSubmitting}
            color="inherit"
            sx={{ textTransform: 'none' }}
          >
            Hủy
          </Button>
          <Button
            onClick={handleSubmitReject}
            variant="contained"
            color="error"
            disabled={rejectSubmitting}
            sx={{ textTransform: 'none', fontWeight: 700 }}
          >
            {rejectSubmitting ? (
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <CircularProgress size={16} color="inherit" />
                <span>Đang xử lý...</span>
              </Box>
            ) : (
              'Xác nhận từ chối'
            )}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Modal: Thẩm Định Chi Tiết Hồ Sơ Quán Chờ Duyệt */}
      <Dialog
        open={appraisalModal.open}
        onClose={() => setAppraisalModal({ open: false, restaurant: null })}
        maxWidth="md"
        fullWidth
      >
        <DialogTitle component="div" sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', pb: 1.5 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.2 }}>
            <Box sx={{ p: 1, borderRadius: 2, bgcolor: isRejectedRestaurant(appraisalModal.restaurant) ? '#fee2e2' : '#fef3c7', color: isRejectedRestaurant(appraisalModal.restaurant) ? '#dc2626' : '#d97706', display: 'flex' }}>
              <RestaurantIcon sx={{ fontSize: 24 }} />
            </Box>
            <Box>
              <Typography variant="h6" sx={{ fontWeight: 800, lineHeight: 1.2 }}>
                Thẩm Định Hồ Sơ Đăng Ký Quán Ăn
              </Typography>
              <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600 }}>
                Mã nhà hàng: #{appraisalModal.restaurant?.id} — {appraisalModal.restaurant?.name}
              </Typography>
            </Box>
          </Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            {isRejectedRestaurant(appraisalModal.restaurant) ? (
              <Chip label="ĐÃ TỪ CHỐI" size="small" sx={{ bgcolor: '#fee2e2', color: '#dc2626', fontWeight: 800 }} />
            ) : (
              <Chip label="CHỜ PHÊ DUYỆT" size="small" sx={{ bgcolor: '#fef3c7', color: '#d97706', fontWeight: 800 }} />
            )}
            <IconButton size="small" onClick={() => setAppraisalModal({ open: false, restaurant: null })}>
              <CloseIcon fontSize="small" />
            </IconButton>
          </Box>
        </DialogTitle>

        <DialogContent dividers sx={{ p: { xs: 2, sm: 3 } }}>
          {appraisalModal.restaurant && (
            <Stack spacing={2.5}>
              {isRejectedRestaurant(appraisalModal.restaurant) && (
                <Alert severity="error" sx={{ borderRadius: 2 }}>
                  <Typography variant="subtitle2" sx={{ fontWeight: 700 }}>
                    Hồ sơ đăng ký này đã bị từ chối duyệt
                  </Typography>
                  <Typography variant="body2" sx={{ mt: 0.5 }}>
                    <b>Lý do từ chối:</b> {appraisalModal.restaurant.lockedReason || 'Không có lý do cụ thể'}
                  </Typography>
                </Alert>
              )}

              {/* Khối 1: Thông tin quán ăn */}
              <Typography variant="subtitle2" sx={{ fontWeight: 800, color: '#1e293b', display: 'flex', alignItems: 'center', gap: 0.8 }}>
                <RestaurantIcon sx={{ fontSize: 18, color: '#f59e0b' }} />
                1. THÔNG TIN NHÀ HÀNG & ĐỊA ĐIỂM
              </Typography>
              <Card variant="outlined" sx={{ p: 2, bgcolor: '#f8fafc', borderRadius: 2 }}>
                <Grid container spacing={2}>
                  <Grid size={{ xs: 12, sm: 4 }} sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
                    <Box
                      component="img"
                      src={appraisalModal.restaurant.imageUrl || 'https://images.unsplash.com/photo-1555396273-367ea4eb4db5'}
                      alt={appraisalModal.restaurant.name}
                      sx={{
                        width: '100%',
                        maxHeight: 140,
                        objectFit: 'cover',
                        borderRadius: 2,
                        border: '1px solid #e2e8f0',
                      }}
                    />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 8 }}>
                    <Grid container spacing={1.5}>
                      <Grid size={{ xs: 12, sm: 6 }}>
                        <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600 }}>Tên nhà hàng</Typography>
                        <Typography variant="body2" sx={{ fontWeight: 800, color: 'text.primary' }}>{appraisalModal.restaurant.name}</Typography>
                      </Grid>
                      <Grid size={{ xs: 12, sm: 6 }}>
                        <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600 }}>Hotline nhà hàng</Typography>
                        <Typography variant="body2" sx={{ fontWeight: 700, fontFamily: 'monospace' }}>{appraisalModal.restaurant.phone || 'Chưa cập nhật'}</Typography>
                      </Grid>
                      <Grid size={12}>
                        <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600 }}>Địa chỉ quán</Typography>
                        <Typography variant="body2" sx={{ fontWeight: 600 }}>{appraisalModal.restaurant.address}</Typography>
                      </Grid>
                      <Grid size={{ xs: 12, sm: 6 }}>
                        <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600 }}>Tọa độ GPS</Typography>
                        <Typography variant="caption" sx={{ display: 'block', fontWeight: 700, fontFamily: 'monospace', color: '#0284c7' }}>
                          {appraisalModal.restaurant.latitude}, {appraisalModal.restaurant.longitude}
                        </Typography>
                      </Grid>
                      <Grid size={{ xs: 12, sm: 6 }}>
                        <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600 }}>Thời gian phục vụ</Typography>
                        <Typography variant="body2" sx={{ fontWeight: 600 }}>
                          🕒 {appraisalModal.restaurant.openTime || '08:00'} - {appraisalModal.restaurant.closeTime || '22:00'}
                        </Typography>
                      </Grid>
                    </Grid>
                  </Grid>
                </Grid>
              </Card>

              {/* Khối 2: Thông tin chủ quán */}
              <Typography variant="subtitle2" sx={{ fontWeight: 800, color: '#1e293b', display: 'flex', alignItems: 'center', gap: 0.8 }}>
                <PersonIcon sx={{ fontSize: 18, color: '#008cff' }} />
                2. THÔNG TIN CHỦ SỞ HỮU / ĐẠI DIỆN PHÁP LUẬT
              </Typography>
              <Card variant="outlined" sx={{ p: 2, bgcolor: '#f8fafc', borderRadius: 2 }}>
                <Grid container spacing={2}>
                  <Grid size={{ xs: 12, sm: 4 }}>
                    <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600 }}>Mã tài khoản chủ quán</Typography>
                    <Typography variant="body2" sx={{ fontWeight: 800, color: '#008cff' }}>#{appraisalModal.restaurant.ownerId || 'N/A'}</Typography>
                  </Grid>
                  <Grid size={{ xs: 12, sm: 4 }}>
                    <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600 }}>Số điện thoại liên hệ</Typography>
                    <Typography variant="body2" sx={{ fontWeight: 700, fontFamily: 'monospace' }}>{appraisalModal.restaurant.phone || '—'}</Typography>
                  </Grid>
                  <Grid size={{ xs: 12, sm: 4 }}>
                    <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600 }}>Trạng thái hồ sơ</Typography>
                    <Typography variant="body2" sx={{ fontWeight: 700, color: isRejectedRestaurant(appraisalModal.restaurant) ? '#dc2626' : '#d97706' }}>
                      {isRejectedRestaurant(appraisalModal.restaurant) ? 'Đã bị từ chối' : 'Chờ Quản trị viên duyệt'}
                    </Typography>
                  </Grid>
                </Grid>
              </Card>

              {/* Khối 3: Giấy phép kinh doanh */}
              <Typography variant="subtitle2" sx={{ fontWeight: 800, color: '#1e293b', display: 'flex', alignItems: 'center', gap: 0.8 }}>
                <LicenseIcon sx={{ fontSize: 18, color: '#7c3aed' }} />
                3. HỒ SƠ GIẤY PHÉP KINH DOANH (GPKD)
              </Typography>
              <Card variant="outlined" sx={{ p: 2, borderRadius: 2, textAlign: 'center', bgcolor: '#fafafa' }}>
                {appraisalModal.restaurant.licenseImageUrl ? (
                  <Box>
                    <Box
                      component="img"
                      src={appraisalModal.restaurant.licenseImageUrl}
                      alt="GPKD"
                      sx={{
                        maxWidth: '100%',
                        maxHeight: 320,
                        objectFit: 'contain',
                        borderRadius: 2,
                        border: '1px solid #e2e8f0',
                        boxShadow: '0 2px 10px rgba(0,0,0,0.06)',
                        cursor: 'pointer',
                      }}
                      onClick={() => setViewLicenseDialog({ open: true, restaurant: appraisalModal.restaurant })}
                    />
                    <Box sx={{ mt: 1 }}>
                      <Button
                        size="small"
                        startIcon={<ViewIcon />}
                        onClick={() => setViewLicenseDialog({ open: true, restaurant: appraisalModal.restaurant })}
                        sx={{ textTransform: 'none', fontWeight: 700, color: '#7c3aed' }}
                      >
                        Xem ảnh gốc phóng to
                      </Button>
                    </Box>
                  </Box>
                ) : (
                  <Box sx={{ py: 4, color: 'text.secondary' }}>
                    <Typography variant="body2">Chưa có ảnh Giấy phép kinh doanh đính kèm</Typography>
                  </Box>
                )}
              </Card>
            </Stack>
          )}
        </DialogContent>

        <DialogActions sx={{ p: 2, bgcolor: '#f8fafc', borderTop: '1px solid #e2e8f0', justifyContent: 'space-between' }}>
          <Button onClick={() => setAppraisalModal({ open: false, restaurant: null })} color="inherit" sx={{ fontWeight: 600 }}>
            Đóng
          </Button>
          <Box sx={{ display: 'flex', gap: 1.5 }}>
            {isRejectedRestaurant(appraisalModal.restaurant) ? (
              <Button
                variant="contained"
                color="success"
                startIcon={<CheckCircleIcon />}
                onClick={async () => {
                  const rest = appraisalModal.restaurant;
                  setAppraisalModal({ open: false, restaurant: null });
                  await handleApproveRestaurant(rest);
                }}
                sx={{ fontWeight: 700, borderRadius: 2, textTransform: 'none', px: 2.5 }}
              >
                Xem xét lại & Phê duyệt quán
              </Button>
            ) : (
              <>
                <Button
                  variant="outlined"
                  color="error"
                  onClick={() => {
                    const rest = appraisalModal.restaurant;
                    setAppraisalModal({ open: false, restaurant: null });
                    handleOpenReject(rest);
                  }}
                  sx={{ fontWeight: 700, borderRadius: 2, textTransform: 'none' }}
                >
                  Từ chối hồ sơ
                </Button>
                <Button
                  variant="contained"
                  color="success"
                  startIcon={<CheckCircleIcon />}
                  onClick={async () => {
                    const rest = appraisalModal.restaurant;
                    setAppraisalModal({ open: false, restaurant: null });
                    await handleApproveRestaurant(rest);
                  }}
                  sx={{ fontWeight: 700, borderRadius: 2, textTransform: 'none', px: 2.5 }}
                >
                  Phê duyệt quán
                </Button>
              </>
            )}
          </Box>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default Restaurants;
