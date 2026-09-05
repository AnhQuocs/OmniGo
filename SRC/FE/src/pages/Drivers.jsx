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
  TextField,
  InputAdornment,
  CircularProgress,
  Alert,
  Button,
  Chip,
  ButtonGroup,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  FormHelperText,
  IconButton,
  Tooltip,
} from '@mui/material';
import {
  Search as SearchIcon,
  Refresh as RefreshIcon,
  TwoWheeler as BikeIcon,
  DirectionsCar as CarIcon,
  LocalShipping as DeliveryIcon,
  FiberManualRecord as DotIcon,
  PersonAdd as AddDriverIcon,
  Edit as EditIcon,
  Close as CloseIcon,
  BadgeOutlined as LicensePlateIcon,
  Phone as PhoneIcon,
  Visibility,
  VisibilityOff,
  LockOutlined as LockIcon,
} from '@mui/icons-material';
import toast from 'react-hot-toast';
import driverService from '../services/driverService';
import { parseApiError } from '../utils/errorHandler';

// Standard Regex Patterns
const VIETNAMESE_PHONE_REGEX = /^(0[3|5|7|8|9])[0-9]{8}$/;
const LICENSE_PLATE_REGEX = /^[0-9]{2}[A-Z0-9]{1,3}[-\s]?[0-9]{3,5}(\.[0-9]{2})?$/i;
const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export const Drivers = () => {
  const [drivers, setDrivers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');

  // Dialog State: Create New Driver
  const [openCreateDialog, setOpenCreateDialog] = useState(false);
  const [createSubmitting, setCreateSubmitting] = useState(false);
  const [createGeneralError, setCreateGeneralError] = useState('');
  const [showCreatePassword, setShowCreatePassword] = useState(false);
  const [createForm, setCreateForm] = useState({
    fullName: '',
    phoneNumber: '',
    email: '',
    password: '',
    vehicleType: 'BIKE',
    licensePlate: '',
    vehicleModel: '',
  });
  const [createErrors, setCreateErrors] = useState({
    fullName: '',
    phoneNumber: '',
    email: '',
    password: '',
    vehicleType: '',
    licensePlate: '',
    vehicleModel: '',
  });

  // Dialog State: Edit Driver & Vehicle
  const [openEditDialog, setOpenEditDialog] = useState(false);
  const [editSubmitting, setEditSubmitting] = useState(false);
  const [editingDriverId, setEditingDriverId] = useState(null);
  const [editGeneralError, setEditGeneralError] = useState('');
  const [editForm, setEditForm] = useState({
    fullName: '',
    phoneNumber: '',
    email: '',
    vehicleType: 'BIKE',
    licensePlate: '',
    vehicleModel: '',
  });
  const [editErrors, setEditErrors] = useState({
    fullName: '',
    phoneNumber: '',
    email: '',
    vehicleType: '',
    licensePlate: '',
    vehicleModel: '',
  });

  const fetchDrivers = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await driverService.getAllDrivers({ page: 0, size: 100 });
      let allUsers = [];

      if (response && response.content) {
        allUsers = response.content;
      } else if (Array.isArray(response)) {
        allUsers = response;
      } else if (response?.data) {
        allUsers = Array.isArray(response.data) ? response.data : [response.data];
      }

      // Filter: Only accounts with role DRIVER
      const onlyDrivers = allUsers.filter((u) => u.role === 'DRIVER');
      setDrivers(onlyDrivers);
    } catch (err) {
      const { message } = parseApiError(err, 'Không thể tải danh sách tài xế từ Backend');
      setError(message);
      toast.error(message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDrivers();
  }, []);

  // Validation function for Driver Form
  const validateDriverInput = (form, isEdit = false) => {
    const errors = {};

    // 1. Full name
    if (!form.fullName.trim()) {
      errors.fullName = 'Họ và tên không được để trống';
    } else if (form.fullName.trim().length < 2) {
      errors.fullName = 'Họ và tên phải có ít nhất 2 ký tự';
    }

    // 2. Phone number
    const cleanPhone = form.phoneNumber.trim();
    if (!cleanPhone) {
      errors.phoneNumber = 'Số điện thoại không được để trống';
    } else if (cleanPhone.length !== 10) {
      errors.phoneNumber = `Số điện thoại phải có đúng 10 chữ số (hiện tại: ${cleanPhone.length} số)`;
    } else if (!VIETNAMESE_PHONE_REGEX.test(cleanPhone)) {
      errors.phoneNumber = 'Số điện thoại không hợp lệ (phải bắt đầu bằng 03, 05, 07, 08, 09)';
    }

    // 3. Email (optional, but validated if provided)
    if (form.email && form.email.trim()) {
      if (!EMAIL_REGEX.test(form.email.trim())) {
        errors.email = 'Địa chỉ email không đúng định dạng (Ví dụ: taixe@gmail.com)';
      }
    }

    // 4. Password (required for creation)
    if (!isEdit) {
      if (!form.password) {
        errors.password = 'Mật khẩu đăng nhập không được để trống';
      } else if (form.password.length < 6) {
        errors.password = 'Mật khẩu phải có ít nhất 6 ký tự';
      }
    }

    // 5. License plate
    const cleanPlate = (form.licensePlate || '').trim().toUpperCase();
    if (!cleanPlate) {
      errors.licensePlate = 'Biển số xe không được để trống';
    } else if (!LICENSE_PLATE_REGEX.test(cleanPlate)) {
      errors.licensePlate = 'Biển số xe không đúng định dạng (Ví dụ: 29A-123.45, 51F-888.88, 29B1-567.89)';
    }

    // 6. Vehicle Type
    if (!form.vehicleType) {
      errors.vehicleType = 'Vui lòng chọn loại phương tiện';
    }

    return errors;
  };

  // Handle Add Driver
  const handleOpenCreate = () => {
    setCreateForm({
      fullName: '',
      phoneNumber: '',
      email: '',
      password: '',
      vehicleType: 'BIKE',
      licensePlate: '',
      vehicleModel: '',
    });
    setCreateErrors({
      fullName: '',
      phoneNumber: '',
      email: '',
      password: '',
      vehicleType: '',
      licensePlate: '',
      vehicleModel: '',
    });
    setCreateGeneralError('');
    setShowCreatePassword(false);
    setOpenCreateDialog(true);
  };

  const handleCloseCreate = () => {
    if (!createSubmitting) setOpenCreateDialog(false);
  };

  const handleCreateSubmit = async (e) => {
    e.preventDefault();
    setCreateGeneralError('');

    const validationErrors = validateDriverInput(createForm, false);
    if (Object.keys(validationErrors).length > 0) {
      setCreateErrors(validationErrors);
      const firstError = Object.values(validationErrors)[0];
      toast.error(firstError || 'Vui lòng điền đúng và đủ thông tin');
      return;
    }

    setCreateSubmitting(true);
    try {
      await driverService.createDriver({
        fullName: createForm.fullName.trim(),
        phoneNumber: createForm.phoneNumber.trim(),
        email: createForm.email.trim() || undefined,
        password: createForm.password,
        vehicleType: createForm.vehicleType,
        licensePlate: createForm.licensePlate.trim().toUpperCase(),
        vehicleModel: createForm.vehicleModel.trim() || 'Xe tiêu chuẩn',
      });
      toast.success('Đăng ký tài xế và phương tiện thành công!');
      setOpenCreateDialog(false);
      fetchDrivers();
    } catch (err) {
      const { message, fieldErrors } = parseApiError(err, 'Lỗi khi tạo tài xế mới');
      setCreateErrors((prev) => ({ ...prev, ...fieldErrors }));
      setCreateGeneralError(message);
      toast.error(message);
    } finally {
      setCreateSubmitting(false);
    }
  };

  // Handle Edit Driver
  const handleOpenEdit = (driver) => {
    setEditingDriverId(driver.id);
    setEditForm({
      fullName: driver.fullName || '',
      phoneNumber: driver.phoneNumber || '',
      email: driver.email || '',
      vehicleType: driver.vehicleType || 'BIKE',
      licensePlate: driver.licensePlate || '',
      vehicleModel: driver.vehicleModel || '',
    });
    setEditErrors({
      fullName: '',
      phoneNumber: '',
      email: '',
      vehicleType: '',
      licensePlate: '',
      vehicleModel: '',
    });
    setEditGeneralError('');
    setOpenEditDialog(true);
  };

  const handleCloseEdit = () => {
    if (!editSubmitting) setOpenEditDialog(false);
  };

  const handleEditSubmit = async (e) => {
    e.preventDefault();
    setEditGeneralError('');

    const validationErrors = validateDriverInput(editForm, true);
    if (Object.keys(validationErrors).length > 0) {
      setEditErrors(validationErrors);
      const firstError = Object.values(validationErrors)[0];
      toast.error(firstError || 'Vui lòng kiểm tra lại các trường thông tin');
      return;
    }

    setEditSubmitting(true);
    try {
      // Call Admin Update Driver endpoint
      await driverService.updateDriverAdmin(editingDriverId, {
        fullName: editForm.fullName.trim(),
        phoneNumber: editForm.phoneNumber.trim(),
        email: editForm.email.trim() || undefined,
        vehicleType: editForm.vehicleType,
        licensePlate: editForm.licensePlate.trim().toUpperCase(),
        vehicleModel: editForm.vehicleModel.trim() || 'Xe tiêu chuẩn',
      });
      toast.success(`Cập nhật thông tin tài xế #${editingDriverId} thành công!`);
      setOpenEditDialog(false);
      fetchDrivers();
    } catch (err) {
      try {
        await driverService.updateDriverVehicle(editingDriverId, {
          vehicleType: editForm.vehicleType,
          licensePlate: editForm.licensePlate.trim().toUpperCase(),
          vehicleModel: editForm.vehicleModel.trim() || 'Xe tiêu chuẩn',
        });
        toast.success(`Cập nhật phương tiện tài xế #${editingDriverId} thành công!`);
        setOpenEditDialog(false);
        fetchDrivers();
      } catch (fallbackErr) {
        const { message, fieldErrors } = parseApiError(err || fallbackErr, 'Lỗi khi cập nhật thông tin tài xế');
        setEditErrors((prev) => ({ ...prev, ...fieldErrors }));
        setEditGeneralError(message);
        toast.error(message);
      }
    } finally {
      setEditSubmitting(false);
    }
  };

  const getVehicleChip = (type) => {
    switch (type) {
      case 'CAR_4_SEAT':
        return (
          <Chip
            icon={<CarIcon sx={{ fontSize: '15px !important' }} />}
            label="OmniCar 4 Chỗ"
            size="small"
            sx={{ bgcolor: 'rgba(0, 140, 255, 0.12)', color: '#008cff', fontWeight: 700, fontSize: '0.75rem' }}
          />
        );
      case 'CAR_7_SEAT':
        return (
          <Chip
            icon={<CarIcon sx={{ fontSize: '15px !important' }} />}
            label="OmniCar 7 Chỗ"
            size="small"
            sx={{ bgcolor: 'rgba(99, 102, 241, 0.12)', color: '#6366f1', fontWeight: 700, fontSize: '0.75rem' }}
          />
        );
      case 'EXPRESS':
        return (
          <Chip
            icon={<DeliveryIcon sx={{ fontSize: '15px !important' }} />}
            label="OmniExpress"
            size="small"
            sx={{ bgcolor: 'rgba(255, 51, 102, 0.12)', color: '#ff3366', fontWeight: 700, fontSize: '0.75rem' }}
          />
        );
      case 'BIKE':
      default:
        return (
          <Chip
            icon={<BikeIcon sx={{ fontSize: '15px !important' }} />}
            label="OmniBike 2 Bánh"
            size="small"
            sx={{ bgcolor: 'rgba(21, 202, 32, 0.12)', color: '#15ca20', fontWeight: 700, fontSize: '0.75rem' }}
          />
        );
    }
  };

  const getDriverStatusChip = (driver) => {
    const status = driver.status || (driver.isActive === true ? 'ONLINE' : 'OFFLINE');
    if (status === 'BUSY' || status === 'IN_TRIP') {
      return (
        <Chip
          icon={<DotIcon sx={{ fontSize: '10px !important', color: '#ffb800 !important' }} />}
          label="Đang Chở Khách"
          size="small"
          sx={{
            bgcolor: 'rgba(255, 184, 0, 0.12)',
            color: '#d97706',
            fontWeight: 700,
            fontSize: '0.78rem',
            border: '1px solid rgba(255, 184, 0, 0.3)',
          }}
        />
      );
    }
    if (status === 'ONLINE' || driver.isActive === true) {
      return (
        <Chip
          icon={<DotIcon sx={{ fontSize: '10px !important', color: '#15ca20 !important' }} />}
          label="Trực Tuyến"
          size="small"
          sx={{
            bgcolor: 'rgba(21, 202, 32, 0.12)',
            color: '#15ca20',
            fontWeight: 700,
            fontSize: '0.78rem',
            border: '1px solid rgba(21, 202, 32, 0.3)',
          }}
        />
      );
    }
    return (
      <Chip
        icon={<DotIcon sx={{ fontSize: '10px !important', color: '#94a3b8 !important' }} />}
        label="Ngoại Tuyến"
        size="small"
        sx={{
          bgcolor: 'rgba(148, 163, 184, 0.12)',
          color: 'text.secondary',
          fontWeight: 700,
          fontSize: '0.78rem',
          border: '1px solid rgba(148, 163, 184, 0.25)',
        }}
      />
    );
  };

  const filteredDrivers = drivers.filter((d) => {
    const term = searchTerm.toLowerCase();
    const matchesSearch =
      (d.id && String(d.id).includes(term)) ||
      (d.fullName && d.fullName.toLowerCase().includes(term)) ||
      (d.phoneNumber && d.phoneNumber.includes(term)) ||
      (d.email && d.email.toLowerCase().includes(term)) ||
      (d.licensePlate && d.licensePlate.toLowerCase().includes(term)) ||
      (d.vehicleModel && d.vehicleModel.toLowerCase().includes(term));

    if (!matchesSearch) return false;
    const isOnline = d.status === 'ONLINE' || d.isActive === true;
    if (statusFilter === 'ALL') return true;
    if (statusFilter === 'ONLINE') return isOnline;
    if (statusFilter === 'OFFLINE') return !isOnline;
    return true;
  });

  const onlineCount = drivers.filter((d) => d.status === 'ONLINE' || d.isActive === true).length;
  const offlineCount = drivers.length - onlineCount;

  const paginatedDrivers = filteredDrivers.slice(
    page * rowsPerPage,
    page * rowsPerPage + rowsPerPage
  );

  return (
    <Box sx={{ width: '100%' }} className="page-enter-animation">
      {/* Header bar */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2.5, flexWrap: 'wrap', gap: 1.5 }}>
        <Box>
          <Typography variant="h5" sx={{ fontWeight: 700, fontFamily: '"Poppins", sans-serif' }}>
            Quản Lý Tài Xế & Đội Xe
          </Typography>
          <Typography variant="body2" sx={{ color: 'text.secondary', mt: 0.2 }}>
            Quản lý hồ sơ đối tác, phương tiện và theo dõi trạng thái vận hành từ `user-driver-service`
          </Typography>
        </Box>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
          <Button
            variant="contained"
            startIcon={<AddDriverIcon />}
            onClick={handleOpenCreate}
            sx={{
              bgcolor: '#15ca20',
              '&:hover': { bgcolor: '#12b01c' },
              borderRadius: 2,
              fontWeight: 700,
            }}
          >
            Thêm Tài Xế Mới
          </Button>

          <Button
            variant="outlined"
            startIcon={<RefreshIcon />}
            onClick={fetchDrivers}
            disabled={loading}
            sx={{ borderRadius: 2, fontWeight: 600 }}
          >
            Làm Mới
          </Button>
        </Box>
      </Box>

      {/* 4 Mini Metric Overview Cards */}
      <Grid container spacing={2} sx={{ mb: 2.5 }}>
        <Grid item xs={6} sm={3}>
          <Card sx={{ p: 2, display: 'flex', alignItems: 'center', gap: 1.5 }}>
            <Box sx={{ p: 1.2, borderRadius: 2, bgcolor: 'rgba(0, 140, 255, 0.1)', color: '#008cff' }}>
              <BikeIcon />
            </Box>
            <Box>
              <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600 }}>TỔNG TÀI XẾ</Typography>
              <Typography variant="h6" sx={{ fontWeight: 800 }}>{drivers.length}</Typography>
            </Box>
          </Card>
        </Grid>
        <Grid item xs={6} sm={3}>
          <Card sx={{ p: 2, display: 'flex', alignItems: 'center', gap: 1.5 }}>
            <Box sx={{ p: 1.2, borderRadius: 2, bgcolor: 'rgba(21, 202, 32, 0.15)', color: '#15ca20' }}>
              <DotIcon />
            </Box>
            <Box>
              <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600 }}>TRỰC TUYẾN</Typography>
              <Typography variant="h6" sx={{ fontWeight: 800, color: '#15ca20' }}>{onlineCount}</Typography>
            </Box>
          </Card>
        </Grid>
        <Grid item xs={6} sm={3}>
          <Card sx={{ p: 2, display: 'flex', alignItems: 'center', gap: 1.5 }}>
            <Box sx={{ p: 1.2, borderRadius: 2, bgcolor: 'rgba(148, 163, 184, 0.15)', color: '#64748b' }}>
              <DotIcon />
            </Box>
            <Box>
              <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600 }}>NGOẠI TUYẾN</Typography>
              <Typography variant="h6" sx={{ fontWeight: 800, color: '#64748b' }}>{offlineCount}</Typography>
            </Box>
          </Card>
        </Grid>
        <Grid item xs={6} sm={3}>
          <Card sx={{ p: 2, display: 'flex', alignItems: 'center', gap: 1.5 }}>
            <Box sx={{ p: 1.2, borderRadius: 2, bgcolor: 'rgba(255, 51, 102, 0.15)', color: '#ff3366' }}>
              <LicensePlateIcon />
            </Box>
            <Box>
              <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600 }}>ĐÃ CẤP XE</Typography>
              <Typography variant="h6" sx={{ fontWeight: 800, color: '#ff3366' }}>
                {drivers.filter((d) => d.licensePlate).length}
              </Typography>
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
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2.5, flexWrap: 'wrap', gap: 2 }}>
          <ButtonGroup variant="outlined">
            {[
              { label: 'Tất Cả', value: 'ALL', count: drivers.length },
              { label: 'Trực Tuyến', value: 'ONLINE', count: onlineCount },
              { label: 'Ngoại Tuyến', value: 'OFFLINE', count: offlineCount },
            ].map((tab) => (
              <Button
                key={tab.value}
                variant={statusFilter === tab.value ? 'contained' : 'outlined'}
                onClick={() => {
                  setStatusFilter(tab.value);
                  setPage(0);
                }}
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

          <Box sx={{ maxWidth: 360, width: '100%' }}>
            <TextField
              size="small"
              placeholder="Tìm theo Tên, SĐT, Email, Biển số xe..."
              fullWidth
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
          <Table sx={{ minWidth: 980 }}>
            <TableHead>
              <TableRow>
                <TableCell sx={{ width: 80 }}>MÃ ID</TableCell>
                <TableCell>HỌ VÀ TÊN TÀI XẾ</TableCell>
                <TableCell>SỐ ĐIỆN THOẠI</TableCell>
                <TableCell>EMAIL</TableCell>
                <TableCell>LOẠI XE</TableCell>
                <TableCell>BIỂN SỐ XE</TableCell>
                <TableCell>DÒNG XE / MODEL</TableCell>
                <TableCell align="center">TRẠNG THÁI</TableCell>
                <TableCell align="center" sx={{ width: 100 }}>HÀNH ĐỘNG</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {loading ? (
                <TableRow>
                  <TableCell colSpan={9} align="center" sx={{ py: 6 }}>
                    <CircularProgress size={28} />
                    <Typography variant="body2" sx={{ mt: 1, color: 'text.secondary' }}>
                      Đang tải danh sách tài xế từ Backend...
                    </Typography>
                  </TableCell>
                </TableRow>
              ) : paginatedDrivers.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={9} align="center" sx={{ py: 5, color: 'text.secondary' }}>
                    {searchTerm ? 'Không tìm thấy tài xế phù hợp với từ khóa' : 'Hệ thống chưa có tài khoản nào có role DRIVER'}
                  </TableCell>
                </TableRow>
              ) : (
                paginatedDrivers.map((driver) => (
                  <TableRow key={driver.id} hover>
                    <TableCell sx={{ fontWeight: 800, fontFamily: 'monospace', color: '#008cff' }}>
                      #{driver.id}
                    </TableCell>
                    <TableCell sx={{ fontWeight: 700, color: 'text.primary' }}>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <BikeIcon sx={{ fontSize: 18, color: '#008cff' }} />
                        {driver.fullName || 'Tài xế đối tác'}
                      </Box>
                    </TableCell>
                    <TableCell sx={{ fontWeight: 600, fontFamily: 'monospace' }}>
                      {driver.phoneNumber}
                    </TableCell>
                    <TableCell sx={{ color: 'text.secondary', fontSize: '0.85rem' }}>
                      {driver.email || '—'}
                    </TableCell>
                    <TableCell>
                      {getVehicleChip(driver.vehicleType)}
                    </TableCell>
                    <TableCell>
                      {driver.licensePlate ? (
                        <Chip
                          label={driver.licensePlate}
                          size="small"
                          sx={{
                            fontFamily: 'monospace',
                            fontWeight: 800,
                            letterSpacing: '0.05em',
                            bgcolor: 'rgba(0, 140, 255, 0.08)',
                            border: '1px solid rgba(0, 140, 255, 0.3)',
                            color: '#008cff',
                          }}
                        />
                      ) : (
                        <Typography variant="caption" sx={{ color: 'text.secondary', fontStyle: 'italic' }}>
                          Chưa cập nhật
                        </Typography>
                      )}
                    </TableCell>
                    <TableCell sx={{ fontWeight: 600, fontSize: '0.85rem', color: 'text.primary' }}>
                      {driver.vehicleModel || '—'}
                    </TableCell>
                    <TableCell align="center">
                      {getDriverStatusChip(driver)}
                    </TableCell>
                    <TableCell align="center">
                      <Tooltip title="Chỉnh sửa thông tin tài xế & phương tiện">
                        <IconButton
                          size="small"
                          onClick={() => handleOpenEdit(driver)}
                          sx={{
                            color: '#008cff',
                            bgcolor: 'rgba(0, 140, 255, 0.1)',
                            '&:hover': { bgcolor: 'rgba(0, 140, 255, 0.2)' },
                          }}
                        >
                          <EditIcon fontSize="small" />
                        </IconButton>
                      </Tooltip>
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
          count={filteredDrivers.length}
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

      {/* DIALOG: Thêm Tài Xế Mới */}
      <Dialog open={openCreateDialog} onClose={handleCloseCreate} maxWidth="sm" fullWidth>
        <form onSubmit={handleCreateSubmit} noValidate>
          <DialogTitle sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', pb: 1 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <AddDriverIcon sx={{ color: '#15ca20' }} />
              <Typography variant="h6" sx={{ fontWeight: 700 }}>
                Thêm Tài Xế & Phương Tiện Mới
              </Typography>
            </Box>
            <IconButton size="small" onClick={handleCloseCreate} disabled={createSubmitting}>
              <CloseIcon fontSize="small" />
            </IconButton>
          </DialogTitle>

          <DialogContent dividers sx={{ pt: 2 }}>
            {createGeneralError && (
              <Alert severity="error" sx={{ mb: 2 }}>
                {createGeneralError}
              </Alert>
            )}

            <Typography variant="subtitle2" sx={{ fontWeight: 700, color: '#008cff', mb: 1.5, textTransform: 'uppercase' }}>
              1. Thông Tin Tài Khoản Tài Xế
            </Typography>
            <Grid container spacing={2}>
              <Grid item xs={12} sm={6}>
                <TextField
                  label="Họ và tên tài xế"
                  size="small"
                  fullWidth
                  required
                  value={createForm.fullName}
                  error={Boolean(createErrors.fullName)}
                  helperText={createErrors.fullName}
                  onChange={(e) => {
                    setCreateForm({ ...createForm, fullName: e.target.value });
                    setCreateErrors({ ...createErrors, fullName: '' });
                    setCreateGeneralError('');
                  }}
                  placeholder="Nguyễn Văn A"
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  label="Số điện thoại đăng nhập"
                  size="small"
                  fullWidth
                  required
                  value={createForm.phoneNumber}
                  error={Boolean(createErrors.phoneNumber)}
                  helperText={createErrors.phoneNumber || 'Đúng 10 chữ số (03, 05, 07, 08, 09)'}
                  onChange={(e) => {
                    const val = e.target.value.replace(/\D/g, '').slice(0, 10);
                    setCreateForm({ ...createForm, phoneNumber: val });
                    setCreateErrors({ ...createErrors, phoneNumber: '' });
                    setCreateGeneralError('');
                  }}
                  placeholder="0987654321"
                  slotProps={{
                    input: {
                      startAdornment: (
                        <InputAdornment position="start">
                          <PhoneIcon sx={{ color: 'text.secondary', fontSize: 18 }} />
                        </InputAdornment>
                      ),
                    },
                  }}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  label="Địa chỉ Email"
                  size="small"
                  type="email"
                  fullWidth
                  value={createForm.email}
                  error={Boolean(createErrors.email)}
                  helperText={createErrors.email}
                  onChange={(e) => {
                    setCreateForm({ ...createForm, email: e.target.value });
                    setCreateErrors({ ...createErrors, email: '' });
                    setCreateGeneralError('');
                  }}
                  placeholder="taixe@omnigo.vn"
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  label="Mật khẩu đăng nhập"
                  size="small"
                  type={showCreatePassword ? 'text' : 'password'}
                  fullWidth
                  required
                  value={createForm.password}
                  error={Boolean(createErrors.password)}
                  helperText={createErrors.password || 'Tối thiểu 6 ký tự'}
                  onChange={(e) => {
                    setCreateForm({ ...createForm, password: e.target.value });
                    setCreateErrors({ ...createErrors, password: '' });
                    setCreateGeneralError('');
                  }}
                  placeholder="Nhập mật khẩu"
                  slotProps={{
                    input: {
                      startAdornment: (
                        <InputAdornment position="start">
                          <LockIcon sx={{ color: 'text.secondary', fontSize: 18 }} />
                        </InputAdornment>
                      ),
                      endAdornment: (
                        <InputAdornment position="end">
                          <IconButton
                            onClick={() => setShowCreatePassword((prev) => !prev)}
                            edge="end"
                            size="small"
                            type="button"
                            tabIndex={-1}
                            sx={{ color: 'text.secondary' }}
                          >
                            {showCreatePassword ? <VisibilityOff fontSize="small" /> : <Visibility fontSize="small" />}
                          </IconButton>
                        </InputAdornment>
                      ),
                    },
                  }}
                />
              </Grid>
            </Grid>

            <Typography variant="subtitle2" sx={{ fontWeight: 700, color: '#008cff', mt: 3, mb: 1.5, textTransform: 'uppercase' }}>
              2. Thông Tin Phương Tiện Xe
            </Typography>
            <Grid container spacing={2}>
              <Grid item xs={12} sm={6}>
                <FormControl fullWidth size="small" error={Boolean(createErrors.vehicleType)} required>
                  <InputLabel>Loại Phương Tiện</InputLabel>
                  <Select
                    value={createForm.vehicleType}
                    label="Loại Phương Tiện"
                    onChange={(e) => {
                      setCreateForm({ ...createForm, vehicleType: e.target.value });
                      setCreateErrors({ ...createErrors, vehicleType: '' });
                      setCreateGeneralError('');
                    }}
                  >
                    <MenuItem value="BIKE">OmniBike (Xe máy 2 bánh)</MenuItem>
                    <MenuItem value="CAR_4_SEAT">OmniCar (Ô tô 4 chỗ)</MenuItem>
                    <MenuItem value="CAR_7_SEAT">OmniCar (Ô tô 7 chỗ)</MenuItem>
                    <MenuItem value="EXPRESS">OmniExpress (Giao hàng siêu tốc)</MenuItem>
                  </Select>
                  {createErrors.vehicleType && <FormHelperText>{createErrors.vehicleType}</FormHelperText>}
                </FormControl>
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  label="Biển số xe"
                  size="small"
                  fullWidth
                  required
                  value={createForm.licensePlate}
                  error={Boolean(createErrors.licensePlate)}
                  helperText={createErrors.licensePlate || 'Ví dụ: 29A-123.45, 51F-888.88'}
                  onChange={(e) => {
                    const val = e.target.value.toUpperCase();
                    setCreateForm({ ...createForm, licensePlate: val });
                    setCreateErrors({ ...createErrors, licensePlate: '' });
                    setCreateGeneralError('');
                  }}
                  placeholder="29A-123.45"
                  slotProps={{
                    input: {
                      startAdornment: (
                        <InputAdornment position="start">
                          <LicensePlateIcon sx={{ color: 'text.secondary', fontSize: 18 }} />
                        </InputAdornment>
                      ),
                    },
                  }}
                />
              </Grid>
              <Grid item xs={12}>
                <TextField
                  label="Dòng xe / Model xe"
                  size="small"
                  fullWidth
                  value={createForm.vehicleModel}
                  error={Boolean(createErrors.vehicleModel)}
                  helperText={createErrors.vehicleModel}
                  onChange={(e) => {
                    setCreateForm({ ...createForm, vehicleModel: e.target.value });
                    setCreateErrors({ ...createErrors, vehicleModel: '' });
                    setCreateGeneralError('');
                  }}
                  placeholder="Ví dụ: Honda Vision 2023, Toyota Vios 2022..."
                />
              </Grid>
            </Grid>
          </DialogContent>

          <DialogActions sx={{ p: 2 }}>
            <Button onClick={handleCloseCreate} disabled={createSubmitting} sx={{ fontWeight: 600 }}>
              Hủy
            </Button>
            <Button
              type="submit"
              variant="contained"
              disabled={createSubmitting}
              sx={{ bgcolor: '#15ca20', '&:hover': { bgcolor: '#12b01c' }, fontWeight: 700, px: 3 }}
            >
              {createSubmitting ? <CircularProgress size={20} color="inherit" /> : 'Tạo Tài Xế'}
            </Button>
          </DialogActions>
        </form>
      </Dialog>

      {/* DIALOG: Chỉnh Sửa Thông Tin Tài Xế & Phương Tiện */}
      <Dialog open={openEditDialog} onClose={handleCloseEdit} maxWidth="sm" fullWidth>
        <form onSubmit={handleEditSubmit} noValidate>
          <DialogTitle sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', pb: 1 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <EditIcon sx={{ color: '#008cff' }} />
              <Typography variant="h6" sx={{ fontWeight: 700 }}>
                Chỉnh Sửa Tài Xế #{editingDriverId}
              </Typography>
            </Box>
            <IconButton size="small" onClick={handleCloseEdit} disabled={editSubmitting}>
              <CloseIcon fontSize="small" />
            </IconButton>
          </DialogTitle>

          <DialogContent dividers sx={{ pt: 2 }}>
            {editGeneralError && (
              <Alert severity="error" sx={{ mb: 2 }}>
                {editGeneralError}
              </Alert>
            )}

            <Typography variant="subtitle2" sx={{ fontWeight: 700, color: '#008cff', mb: 1.5, textTransform: 'uppercase' }}>
              1. Cập Nhật Thông Tin Cá Nhân
            </Typography>
            <Grid container spacing={2}>
              <Grid item xs={12} sm={6}>
                <TextField
                  label="Họ và tên tài xế"
                  size="small"
                  fullWidth
                  required
                  value={editForm.fullName}
                  error={Boolean(editErrors.fullName)}
                  helperText={editErrors.fullName}
                  onChange={(e) => {
                    setEditForm({ ...editForm, fullName: e.target.value });
                    setEditErrors({ ...editErrors, fullName: '' });
                    setEditGeneralError('');
                  }}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  label="Số điện thoại"
                  size="small"
                  fullWidth
                  required
                  value={editForm.phoneNumber}
                  error={Boolean(editErrors.phoneNumber)}
                  helperText={editErrors.phoneNumber || 'Đúng 10 chữ số (03, 05, 07, 08, 09)'}
                  onChange={(e) => {
                    const val = e.target.value.replace(/\D/g, '').slice(0, 10);
                    setEditForm({ ...editForm, phoneNumber: val });
                    setEditErrors({ ...editErrors, phoneNumber: '' });
                    setEditGeneralError('');
                  }}
                  slotProps={{
                    input: {
                      startAdornment: (
                        <InputAdornment position="start">
                          <PhoneIcon sx={{ color: 'text.secondary', fontSize: 18 }} />
                        </InputAdornment>
                      ),
                    },
                  }}
                />
              </Grid>
              <Grid item xs={12}>
                <TextField
                  label="Địa chỉ Email"
                  size="small"
                  type="email"
                  fullWidth
                  value={editForm.email}
                  error={Boolean(editErrors.email)}
                  helperText={editErrors.email}
                  onChange={(e) => {
                    setEditForm({ ...editForm, email: e.target.value });
                    setEditErrors({ ...editErrors, email: '' });
                    setEditGeneralError('');
                  }}
                />
              </Grid>
            </Grid>

            <Typography variant="subtitle2" sx={{ fontWeight: 700, color: '#008cff', mt: 3, mb: 1.5, textTransform: 'uppercase' }}>
              2. Cập Nhật Thông Tin Phương Tiện
            </Typography>
            <Grid container spacing={2}>
              <Grid item xs={12} sm={6}>
                <FormControl fullWidth size="small" error={Boolean(editErrors.vehicleType)} required>
                  <InputLabel>Loại Phương Tiện</InputLabel>
                  <Select
                    value={editForm.vehicleType}
                    label="Loại Phương Tiện"
                    onChange={(e) => {
                      setEditForm({ ...editForm, vehicleType: e.target.value });
                      setEditErrors({ ...editErrors, vehicleType: '' });
                      setEditGeneralError('');
                    }}
                  >
                    <MenuItem value="BIKE">OmniBike (Xe máy 2 bánh)</MenuItem>
                    <MenuItem value="CAR_4_SEAT">OmniCar (Ô tô 4 chỗ)</MenuItem>
                    <MenuItem value="CAR_7_SEAT">OmniCar (Ô tô 7 chỗ)</MenuItem>
                    <MenuItem value="EXPRESS">OmniExpress (Giao hàng siêu tốc)</MenuItem>
                  </Select>
                  {editErrors.vehicleType && <FormHelperText>{editErrors.vehicleType}</FormHelperText>}
                </FormControl>
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  label="Biển số xe"
                  size="small"
                  fullWidth
                  required
                  value={editForm.licensePlate}
                  error={Boolean(editErrors.licensePlate)}
                  helperText={editErrors.licensePlate || 'Ví dụ: 29A-123.45, 51F-888.88'}
                  onChange={(e) => {
                    const val = e.target.value.toUpperCase();
                    setEditForm({ ...editForm, licensePlate: val });
                    setEditErrors({ ...editErrors, licensePlate: '' });
                    setEditGeneralError('');
                  }}
                  placeholder="29A-123.45"
                  slotProps={{
                    input: {
                      startAdornment: (
                        <InputAdornment position="start">
                          <LicensePlateIcon sx={{ color: 'text.secondary', fontSize: 18 }} />
                        </InputAdornment>
                      ),
                    },
                  }}
                />
              </Grid>
              <Grid item xs={12}>
                <TextField
                  label="Dòng xe / Model xe"
                  size="small"
                  fullWidth
                  value={editForm.vehicleModel}
                  error={Boolean(editErrors.vehicleModel)}
                  helperText={editErrors.vehicleModel}
                  onChange={(e) => {
                    setEditForm({ ...editForm, vehicleModel: e.target.value });
                    setEditErrors({ ...editErrors, vehicleModel: '' });
                    setEditGeneralError('');
                  }}
                  placeholder="Ví dụ: Honda SH 150i, Toyota Camry 2023..."
                />
              </Grid>
            </Grid>
          </DialogContent>

          <DialogActions sx={{ p: 2 }}>
            <Button onClick={handleCloseEdit} disabled={editSubmitting} sx={{ fontWeight: 600 }}>
              Hủy
            </Button>
            <Button
              type="submit"
              variant="contained"
              disabled={editSubmitting}
              sx={{ bgcolor: '#008cff', '&:hover': { bgcolor: '#0070cc' }, fontWeight: 700, px: 3 }}
            >
              {editSubmitting ? <CircularProgress size={20} color="inherit" /> : 'Lưu Thay Đổi'}
            </Button>
          </DialogActions>
        </form>
      </Dialog>
    </Box>
  );
};

export default Drivers;
