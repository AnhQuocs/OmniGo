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
} from '@mui/material';
import {
  Search as SearchIcon,
  Refresh as RefreshIcon,
  Store as RestaurantIcon,
  CheckCircle as OpenIcon,
  Schedule as BusyIcon,
  Block as ClosedIcon,
  MenuBook as MenuIcon,
  Close as CloseIcon,
  Visibility as ViewIcon,
  Add as AddIcon,
  Person as PersonIcon,
  Lock as LockIcon,
  Phone as PhoneIcon,
  Email as EmailIcon,
  LocationOn as LocationIcon,
  Image as ImageIcon,
  AccessTime as TimeIcon,
} from '@mui/icons-material';
import toast from 'react-hot-toast';
import foodService from '../services/foodService';

export const Restaurants = () => {
  const [restaurants, setRestaurants] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');

  // Menu Modal State
  const [selectedRestaurant, setSelectedRestaurant] = useState(null);
  const [menuItems, setMenuItems] = useState([]);
  const [loadingMenu, setLoadingMenu] = useState(false);
  const [openMenuModal, setOpenMenuModal] = useState(false);

  // Status Change Dialog State
  const [statusDialogData, setStatusDialogData] = useState(null);
  const [newStatus, setNewStatus] = useState('');
  const [updatingStatus, setUpdatingStatus] = useState(false);

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

    setSubmittingPartner(true);
    try {
      const payload = {
        name: partnerFormData.name.trim(),
        phone: partnerFormData.phone.trim() || partnerFormData.ownerPhone.trim(),
        address: partnerFormData.address.trim(),
        latitude: parseFloat(partnerFormData.latitude),
        longitude: parseFloat(partnerFormData.longitude),
        imageUrl: partnerFormData.imageUrl.trim() || 'https://images.unsplash.com/photo-1555396273-367ea4eb4db5',
        openTime: partnerFormData.openTime || '08:00',
        closeTime: partnerFormData.closeTime || '22:00',
        ownerName: partnerFormData.ownerName.trim(),
        ownerPhone: partnerFormData.ownerPhone.trim(),
        email: partnerFormData.email.trim() || undefined,
        password: partnerFormData.password,
      };

      await foodService.registerPartnerRestaurant(payload);
      toast.success(`Đăng ký đối tác "${payload.name}" và tạo tài khoản RESTAURANT thành công!`);
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

  const handleOpenStatusDialog = (restaurant) => {
    setStatusDialogData(restaurant);
    setNewStatus(restaurant.status || 'OPEN');
  };

  const handleSaveStatus = async () => {
    if (!statusDialogData || !newStatus) return;
    setUpdatingStatus(true);
    try {
      await foodService.updateRestaurantStatus(statusDialogData.id, newStatus);
      toast.success(`Đã cập nhật trạng thái quán sang: ${newStatus}`);
      setStatusDialogData(null);
      fetchRestaurants();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Lỗi cập nhật trạng thái quán');
    } finally {
      setUpdatingStatus(false);
    }
  };

  // Filter logic
  const filteredRestaurants = restaurants.filter((r) => {
    const matchSearch =
      searchTerm === '' ||
      r.name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      r.address?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      r.phone?.includes(searchTerm);

    const matchStatus = statusFilter === 'ALL' || r.status === statusFilter;

    return matchSearch && matchStatus;
  });

  const paginatedList = filteredRestaurants.slice(
    page * rowsPerPage,
    page * rowsPerPage + rowsPerPage
  );

  const stats = {
    total: restaurants.length,
    open: restaurants.filter((r) => r.status === 'OPEN').length,
    busy: restaurants.filter((r) => r.status === 'BUSY').length,
    closed: restaurants.filter((r) => r.status === 'CLOSED').length,
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
      {/* Header */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box>
          <Typography variant="h5" sx={{ fontWeight: 800, color: 'text.primary' }}>
            🏪 Quản Lý Nhà Hàng & Đối Tác Ẩm Thực
          </Typography>
          <Typography variant="body2" sx={{ color: 'text.secondary' }}>
            Theo dõi danh sách quán ăn, thực đơn món và trạng thái hoạt động trực tiếp trên toàn hệ thống
          </Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 1.5 }}>
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
            }}
          >
            Làm Mới
          </Button>
        </Box>
      </Box>

      {/* Stats Cards */}
      <Grid container spacing={2.5} sx={{ mb: 3 }}>
        <Grid item xs={12} sm={6} md={3}>
          <Card
            sx={{
              p: 2.5,
              borderRadius: 2.5,
              bgcolor: 'background.paper',
              border: 1,
              borderColor: 'divider',
              display: 'flex',
              alignItems: 'center',
              gap: 2,
            }}
          >
            <Avatar sx={{ bgcolor: 'rgba(0, 140, 255, 0.12)', color: '#008cff', width: 48, height: 48 }}>
              <RestaurantIcon />
            </Avatar>
            <Box>
              <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600 }}>
                Tổng Nhà Hàng
              </Typography>
              <Typography variant="h5" sx={{ fontWeight: 800, color: 'text.primary' }}>
                {stats.total}
              </Typography>
            </Box>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card
            sx={{
              p: 2.5,
              borderRadius: 2.5,
              bgcolor: 'background.paper',
              border: 1,
              borderColor: 'divider',
              display: 'flex',
              alignItems: 'center',
              gap: 2,
            }}
          >
            <Avatar sx={{ bgcolor: 'rgba(21, 202, 32, 0.12)', color: '#15ca20', width: 48, height: 48 }}>
              <OpenIcon />
            </Avatar>
            <Box>
              <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600 }}>
                Đang Mở Cửa (Nhận Đơn)
              </Typography>
              <Typography variant="h5" sx={{ fontWeight: 800, color: '#15ca20' }}>
                {stats.open}
              </Typography>
            </Box>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card
            sx={{
              p: 2.5,
              borderRadius: 2.5,
              bgcolor: 'background.paper',
              border: 1,
              borderColor: 'divider',
              display: 'flex',
              alignItems: 'center',
              gap: 2,
            }}
          >
            <Avatar sx={{ bgcolor: 'rgba(255, 170, 0, 0.12)', color: '#ffaa00', width: 48, height: 48 }}>
              <BusyIcon />
            </Avatar>
            <Box>
              <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600 }}>
                Tạm Bận / Quá Tải
              </Typography>
              <Typography variant="h5" sx={{ fontWeight: 800, color: '#ffaa00' }}>
                {stats.busy}
              </Typography>
            </Box>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card
            sx={{
              p: 2.5,
              borderRadius: 2.5,
              bgcolor: 'background.paper',
              border: 1,
              borderColor: 'divider',
              display: 'flex',
              alignItems: 'center',
              gap: 2,
            }}
          >
            <Avatar sx={{ bgcolor: 'rgba(255, 51, 102, 0.12)', color: '#ff3366', width: 48, height: 48 }}>
              <ClosedIcon />
            </Avatar>
            <Box>
              <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600 }}>
                Đang Đóng Cửa
              </Typography>
              <Typography variant="h5" sx={{ fontWeight: 800, color: '#ff3366' }}>
                {stats.closed}
              </Typography>
            </Box>
          </Card>
        </Grid>
      </Grid>

      {/* Main Table Container */}
      <Card sx={{ borderRadius: 2.5, bgcolor: 'background.paper', border: 1, borderColor: 'divider' }}>
        {/* Filter & Search Bar */}
        <Box
          sx={{
            p: 2,
            display: 'flex',
            flexDirection: { xs: 'column', sm: 'row' },
            gap: 2,
            justifyContent: 'space-between',
            alignItems: 'center',
            borderBottom: 1,
            borderColor: 'divider',
          }}
        >
          <ButtonGroup size="small" variant="outlined">
            <Button
              variant={statusFilter === 'ALL' ? 'contained' : 'outlined'}
              onClick={() => { setStatusFilter('ALL'); setPage(0); }}
              sx={{ textTransform: 'none', fontWeight: 600 }}
            >
              Tất cả ({stats.total})
            </Button>
            <Button
              variant={statusFilter === 'OPEN' ? 'contained' : 'outlined'}
              onClick={() => { setStatusFilter('OPEN'); setPage(0); }}
              sx={{ textTransform: 'none', fontWeight: 600, color: statusFilter === 'OPEN' ? '#fff' : '#15ca20' }}
            >
              Mở cửa ({stats.open})
            </Button>
            <Button
              variant={statusFilter === 'BUSY' ? 'contained' : 'outlined'}
              onClick={() => { setStatusFilter('BUSY'); setPage(0); }}
              sx={{ textTransform: 'none', fontWeight: 600, color: statusFilter === 'BUSY' ? '#fff' : '#ffaa00' }}
            >
              Đang bận ({stats.busy})
            </Button>
            <Button
              variant={statusFilter === 'CLOSED' ? 'contained' : 'outlined'}
              onClick={() => { setStatusFilter('CLOSED'); setPage(0); }}
              sx={{ textTransform: 'none', fontWeight: 600, color: statusFilter === 'CLOSED' ? '#fff' : '#ff3366' }}
            >
              Đóng cửa ({stats.closed})
            </Button>
          </ButtonGroup>

          <TextField
            size="small"
            placeholder="Tìm theo tên quán, địa chỉ, SĐT..."
            value={searchTerm}
            onChange={(e) => { setSearchTerm(e.target.value); setPage(0); }}
            sx={{ width: { xs: '100%', sm: 300 } }}
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
        <TableContainer>
          <Table sx={{ minWidth: 700 }}>
            <TableHead sx={{ bgcolor: 'action.hover' }}>
              <TableRow>
                <TableCell sx={{ fontWeight: 700, width: 60 }}>#ID</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Nhà Hàng</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Địa Chỉ</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Số Điện Thoại</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Giờ Phục Vụ</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Trạng Thái</TableCell>
                <TableCell sx={{ fontWeight: 700, textAlign: 'center' }}>Thao Tác</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {loading ? (
                <TableRow>
                  <TableCell colSpan={7} align="center" sx={{ py: 6 }}>
                    <CircularProgress size={36} />
                    <Typography variant="body2" sx={{ mt: 1.5, color: 'text.secondary' }}>
                      Đang tải danh sách quán ăn...
                    </Typography>
                  </TableCell>
                </TableRow>
              ) : paginatedList.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={7} align="center" sx={{ py: 6 }}>
                    <RestaurantIcon sx={{ fontSize: 48, color: 'text.secondary', mb: 1 }} />
                    <Typography variant="body1" sx={{ fontWeight: 600, color: 'text.secondary' }}>
                      Không tìm thấy nhà hàng nào
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
                          <Typography variant="caption" sx={{ color: 'text.secondary' }}>
                            Chủ quán ID: #{r.ownerId || 'N/A'}
                          </Typography>
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
                    <TableCell>
                      <Box sx={{ cursor: 'pointer' }} onClick={() => handleOpenStatusDialog(r)} title="Bấm để đổi trạng thái">
                        {getStatusChip(r.status)}
                      </Box>
                    </TableCell>
                    <TableCell align="center">
                      <Box sx={{ display: 'flex', justifyContent: 'center', gap: 1 }}>
                        <Button
                          size="small"
                          variant="outlined"
                          startIcon={<MenuIcon />}
                          onClick={() => handleOpenMenu(r)}
                          sx={{ textTransform: 'none', borderRadius: 1.5, fontSize: '0.8rem', fontWeight: 600 }}
                        >
                          Thực đơn
                        </Button>
                        <Button
                          size="small"
                          variant="contained"
                          onClick={() => handleOpenStatusDialog(r)}
                          sx={{
                            textTransform: 'none',
                            borderRadius: 1.5,
                            fontSize: '0.8rem',
                            fontWeight: 600,
                            bgcolor: '#334155',
                            '&:hover': { bgcolor: '#1e293b' },
                          }}
                        >
                          Đổi Trạng Thái
                        </Button>
                      </Box>
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
        <DialogTitle sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
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
        <DialogContent sx={{ p: 3 }}>
          {loadingMenu ? (
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
                <Grid item xs={12} sm={6} md={4} key={item.id}>
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
          )}
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={() => setOpenMenuModal(false)} variant="contained" sx={{ textTransform: 'none' }}>
            Đóng
          </Button>
        </DialogActions>
      </Dialog>

      {/* Modal: Đổi trạng thái Quán */}
      <Dialog
        open={Boolean(statusDialogData)}
        onClose={() => setStatusDialogData(null)}
        maxWidth="xs"
        fullWidth
        PaperProps={{ sx: { borderRadius: 3 } }}
      >
        <DialogTitle sx={{ fontWeight: 800 }}>
          🔄 Cập Nhật Trạng Thái Quán
        </DialogTitle>
        <DialogContent sx={{ pt: 2 }}>
          <Typography variant="body2" sx={{ mb: 2 }}>
            Quán: <b>{statusDialogData?.name}</b> (ID: #{statusDialogData?.id})
          </Typography>

          <FormControl fullWidth size="small">
            <InputLabel>Trạng Thái Hoạt Động</InputLabel>
            <Select
              value={newStatus}
              label="Trạng Thái Hoạt Động"
              onChange={(e) => setNewStatus(e.target.value)}
            >
              <MenuItem value="OPEN">🟢 OPEN - Đang mở cửa (Nhận đặt món)</MenuItem>
              <MenuItem value="BUSY">🟡 BUSY - Quán đang bận (Tạm dừng nhận đơn)</MenuItem>
              <MenuItem value="CLOSED">🔴 CLOSED - Tạm đóng cửa (Xem thực đơn, không mua)</MenuItem>
            </Select>
          </FormControl>
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={() => setStatusDialogData(null)} disabled={updatingStatus} sx={{ textTransform: 'none' }}>
            Hủy
          </Button>
          <Button
            onClick={handleSaveStatus}
            variant="contained"
            disabled={updatingStatus}
            sx={{ bgcolor: '#008cff', textTransform: 'none', fontWeight: 700 }}
          >
            {updatingStatus ? 'Đang lưu...' : 'Lưu Trạng Thái'}
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
        <DialogTitle sx={{ fontWeight: 800, bgcolor: 'background.paper', borderBottom: 1, borderColor: 'divider', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
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

                {/* Row 3: Address */}
                <Box>
                  <Typography variant="subtitle2" sx={{ fontWeight: 700, mb: 0.8 }}>
                    Địa chỉ nhà hàng <span style={{ color: '#ef4444' }}>*</span>
                  </Typography>
                  <TextField
                    fullWidth
                    required
                    name="address"
                    value={partnerFormData.address}
                    onChange={handlePartnerInputChange}
                    placeholder="VD: 123 Nguyễn Thị Minh Khai, Phường Bến Nghé, Quận 1, TP. Hồ Chí Minh"
                    InputProps={{
                      startAdornment: (
                        <InputAdornment position="start">
                          <LocationIcon sx={{ color: 'text.secondary' }} />
                        </InputAdornment>
                      ),
                    }}
                  />
                </Box>

                {/* Row 4: Coordinates (Lat & Lon) */}
                <Box sx={{ display: 'flex', gap: 2, flexDirection: { xs: 'column', sm: 'row' } }}>
                  <Box sx={{ flex: 1 }}>
                    <Typography variant="subtitle2" sx={{ fontWeight: 700, mb: 0.8 }}>
                      Tọa độ Vĩ độ (Latitude) <span style={{ color: '#ef4444' }}>*</span>
                    </Typography>
                    <TextField
                      fullWidth
                      required
                      name="latitude"
                      type="number"
                      value={partnerFormData.latitude}
                      onChange={handlePartnerInputChange}
                      placeholder="VD: 10.7769"
                    />
                  </Box>
                  <Box sx={{ flex: 1 }}>
                    <Typography variant="subtitle2" sx={{ fontWeight: 700, mb: 0.8 }}>
                      Tọa độ Kinh độ (Longitude) <span style={{ color: '#ef4444' }}>*</span>
                    </Typography>
                    <TextField
                      fullWidth
                      required
                      name="longitude"
                      type="number"
                      value={partnerFormData.longitude}
                      onChange={handlePartnerInputChange}
                      placeholder="VD: 106.7009"
                    />
                  </Box>
                </Box>

                {/* Row 5: Image URL */}
                <Box>
                  <Typography variant="subtitle2" sx={{ fontWeight: 700, mb: 0.8 }}>
                    Link ảnh đại diện nhà hàng (URL)
                  </Typography>
                  <TextField
                    fullWidth
                    name="imageUrl"
                    value={partnerFormData.imageUrl}
                    onChange={handlePartnerInputChange}
                    placeholder="VD: https://images.unsplash.com/..."
                    InputProps={{
                      startAdornment: (
                        <InputAdornment position="start">
                          <ImageIcon sx={{ color: 'text.secondary' }} />
                        </InputAdornment>
                      ),
                    }}
                  />
                </Box>

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
    </Box>
  );
};

export default Restaurants;
