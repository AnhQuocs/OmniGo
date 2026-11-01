import { useState, useEffect, useCallback, useMemo } from 'react';
import {
  Box,
  Card,
  Typography,
  Grid,
  TextField,
  Button,
  CircularProgress,
  Divider,
  Chip,
  MenuItem,
  Select,
  FormControl,
  InputLabel,
  Stack,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  IconButton,
  Switch,
  LinearProgress,
  Tooltip,
  Paper,
  InputAdornment,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
} from '@mui/material';
import {
  Refresh as RefreshIcon,
  Add as AddIcon,
  LocalOffer as VoucherIcon,
  DeleteOutlined as DeleteIcon,
  AccessTime as TimeIcon,
  CheckCircleOutlined as ActiveIcon,
  Block as InactiveIcon,
  History as ExpiredIcon,
  Search as SearchIcon,
  ContentCopy as CopyIcon,
  DirectionsCar as RideIcon,
  Fastfood as FoodIcon,
  AllInclusive as AllServiceIcon,
  Percent as PercentIcon,
  AttachMoney as MoneyIcon,
  ConfirmationNumber as TicketIcon,
  People as PeopleIcon,
  Close as CloseIcon,
  EditOutlined as EditIcon,
  HourglassEmpty as UpcomingIcon,
} from '@mui/icons-material';
import toast from 'react-hot-toast';
import voucherService from '../services/voucherService';
import { useColorMode } from '../context/ThemeContext';
import { ConfirmDialog } from '../components/common/ConfirmDialog';
import UndoDeleteSnackbar from '../components/common/UndoDeleteSnackbar';
import useUndoDelete from '../hooks/useUndoDelete';

export const Vouchers = () => {
  const { mode } = useColorMode();
  const isDark = mode === 'dark';

  const [vouchers, setVouchers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [serviceFilter, setServiceFilter] = useState('ALL');

    // Undo Delete Hook (5s countdown)
  const {
    pendingItem: pendingDeleteVoucher,
    secondsLeft: voucherSecondsLeft,
    startDeleteWithUndo: startDeleteVoucherWithUndo,
    handleUndo: handleUndoDeleteVoucher,
    handleDismiss: handleDismissDeleteVoucher,
  } = useUndoDelete({
    itemType: 'voucher',
    onCommitDelete: async (item) => {
      await voucherService.deleteVoucher(item.code);
    },
    onRestoreItem: (item) => {
      setVouchers((prev) => {
        if (prev.some((v) => v.code === item.code)) return prev;
        return [item, ...prev];
      });
    },
  });

  // Confirm Delete Dialog state
  const [deleteDialog, setDeleteDialog] = useState({
    open: false,
    voucherCode: '',
    loading: false,
  });

  // Modal tạo / chỉnh sửa voucher
  const [openModal, setOpenModal] = useState(false);
  const [editingVoucher, setEditingVoucher] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  // Helper tính trạng thái chính xác theo ngày giờ thực tế
  const getVoucherStatusInfo = useCallback((v) => {
    if (!v) return { status: 'INACTIVE', label: 'Tạm dừng', color: 'default', isExpired: false, isUpcoming: false, isActive: false };
    const now = new Date();
    const start = v.startAt ? new Date(v.startAt) : null;
    const expire = v.expireAt ? new Date(v.expireAt) : null;

    if ((expire && now > expire) || v.status === 'EXPIRED') {
      return {
        status: 'EXPIRED',
        label: 'Hết hạn',
        color: 'error',
        isExpired: true,
        isUpcoming: false,
        isActive: false,
      };
    }
    if (start && now < start) {
      return {
        status: 'UPCOMING',
        label: 'Chưa đến hạn',
        color: 'info',
        isExpired: false,
        isUpcoming: true,
        isActive: false,
      };
    }
    if (v.status === 'INACTIVE') {
      return {
        status: 'INACTIVE',
        label: 'Tạm dừng',
        color: 'default',
        isExpired: false,
        isUpcoming: false,
        isActive: false,
      };
    }
    return {
      status: 'ACTIVE',
      label: 'Kích hoạt',
      color: 'success',
      isExpired: false,
      isUpcoming: false,
      isActive: true,
    };
  }, []);

  // Modal lịch sử sử dụng của user
  const [openUsagesModal, setOpenUsagesModal] = useState(false);
  const [selectedVoucherCode, setSelectedVoucherCode] = useState('');
  const [usagesList, setUsagesList] = useState([]);
  const [loadingUsages, setLoadingUsages] = useState(false);

  // Form data
  const initialForm = {
    code: '',
    title: '',
    description: '',
    discountType: 'PERCENTAGE',
    discountValue: 20,
    maxDiscountAmount: 30000,
    minOrderAmount: 50000,
    startAt: new Date().toISOString().slice(0, 16),
    expireAt: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString().slice(0, 16),
    totalUsageLimit: '',
    usageLimitPerUser: 1,
    applicableService: 'ALL',
  };
  const [formData, setFormData] = useState(initialForm);

  // Fetch vouchers
  const fetchVouchers = useCallback(async () => {
    try {
      setLoading(true);
      const data = await voucherService.getAllVouchers();
      setVouchers(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error('Lỗi tải danh sách voucher:', err);
      toast.error('Không thể tải danh sách mã khuyến mãi từ Redis');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchVouchers();
  }, [fetchVouchers]);

  // Copy code to clipboard
  const handleCopyCode = (code) => {
    navigator.clipboard.writeText(code);
    toast.success(`Đã sao chép mã: ${code}`);
  };

  // Toggle Status
  const handleToggleStatus = async (code) => {
    try {
      const res = await voucherService.toggleVoucherStatus(code);
      if (res && res.success) {
        toast.success(`Đã cập nhật trạng thái voucher ${code}`);
        fetchVouchers();
      }
    } catch (err) {
      toast.error(err.response?.data?.message || 'Lỗi khi cập nhật trạng thái');
    }
  };

  // Delete Voucher Confirm Dialog
  const handleOpenDeleteDialog = (code) => {
    setDeleteDialog({
      open: true,
      voucherCode: code,
      loading: false,
    });
  };

  const handleCloseDeleteDialog = () => {
    if (deleteDialog.loading) return;
    setDeleteDialog({
      open: false,
      voucherCode: '',
      loading: false,
    });
  };

  const handleConfirmDelete = () => {
    const code = deleteDialog.voucherCode;
    if (!code) return;
    const voucherToDelete = vouchers.find((v) => v.code === code) || { code, name: code };
    // Ẩn ngay thẻ / dòng voucher khỏi danh sách
    setVouchers((prev) => prev.filter((v) => v.code !== code));
    handleCloseDeleteDialog();
    // Kích hoạt thanh đếm ngược 5s để hoàn tác
    startDeleteVoucherWithUndo(voucherToDelete);
  };

  // View usages history
  const handleViewUsages = async (code) => {
    setSelectedVoucherCode(code);
    setOpenUsagesModal(true);
    setLoadingUsages(true);
    try {
      const data = await voucherService.getVoucherUsages(code);
      setUsagesList(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error('Lỗi tải lịch sử dùng voucher:', err);
      toast.error('Không thể tải lịch sử sử dụng voucher');
    } finally {
      setLoadingUsages(false);
    }
  };

  // Mở modal tạo mới
  const handleOpenCreateModal = () => {
    setEditingVoucher(null);
    setFormData(initialForm);
    setOpenModal(true);
  };

  // Mở modal chỉnh sửa
  const handleOpenEditModal = (voucher) => {
    setEditingVoucher(voucher);
    setFormData({
      code: voucher.code || '',
      title: voucher.title || '',
      description: voucher.description || '',
      discountType: voucher.discountType || 'PERCENTAGE',
      discountValue: voucher.discountValue ?? 20,
      maxDiscountAmount: voucher.maxDiscountAmount ?? 30000,
      minOrderAmount: voucher.minOrderAmount ?? 0,
      startAt: voucher.startAt ? voucher.startAt.slice(0, 16) : new Date().toISOString().slice(0, 16),
      expireAt: voucher.expireAt ? voucher.expireAt.slice(0, 16) : new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString().slice(0, 16),
      totalUsageLimit: voucher.totalUsageLimit > 0 ? voucher.totalUsageLimit : '',
      usageLimitPerUser: voucher.usageLimitPerUser ?? 1,
      applicableService: voucher.applicableService || 'ALL',
    });
    setOpenModal(true);
  };

  // Submit Tạo mới hoặc Cập nhật Voucher
  const handleSaveVoucher = async (e) => {
    if (e && e.preventDefault) e.preventDefault();
    if (!formData.code.trim()) {
      toast.error('Vui lòng nhập mã voucher!');
      return;
    }
    if (!formData.title.trim()) {
      toast.error('Vui lòng nhập tiêu đề voucher!');
      return;
    }
    if (new Date(formData.expireAt) <= new Date(formData.startAt)) {
      toast.error('Thời gian hết hạn phải sau thời gian kích hoạt!');
      return;
    }

    try {
      setSubmitting(true);
      const payload = {
        ...formData,
        code: formData.code.trim().toUpperCase(),
        startAt: formData.startAt.length === 16 ? formData.startAt + ':00' : formData.startAt,
        expireAt: formData.expireAt.length === 16 ? formData.expireAt + ':00' : formData.expireAt,
        discountValue: Number(formData.discountValue),
        maxDiscountAmount: formData.discountType === 'PERCENTAGE' ? Number(formData.maxDiscountAmount) : null,
        minOrderAmount: Number(formData.minOrderAmount) || 0,
        totalUsageLimit: formData.totalUsageLimit !== '' && Number(formData.totalUsageLimit) > 0 ? Number(formData.totalUsageLimit) : 0,
        usageLimitPerUser: Number(formData.usageLimitPerUser) || 1,
      };

      if (editingVoucher) {
        const res = await voucherService.updateVoucher(editingVoucher.code, payload);
        if (res && res.success) {
          toast.success(`Đã cập nhật voucher '${payload.code}' thành công!`);
          setOpenModal(false);
          setEditingVoucher(null);
          setFormData(initialForm);
          fetchVouchers();
        }
      } else {
        const res = await voucherService.createVoucher(payload);
        if (res && res.success) {
          toast.success(`Đã tạo voucher '${payload.code}' và lưu vào pricing_db + Redis!`);
          setOpenModal(false);
          setFormData(initialForm);
          fetchVouchers();
        }
      }
    } catch (err) {
      console.error('Lỗi khi lưu voucher:', err);
      toast.error(err.response?.data?.message || err.message || (editingVoucher ? 'Lỗi khi cập nhật mã khuyến mãi' : 'Lỗi khi tạo mã khuyến mãi'));
    } finally {
      setSubmitting(false);
    }
  };

  // Statistics
  const stats = useMemo(() => {
    const total = vouchers.length;
    let active = 0;
    let upcoming = 0;
    let expired = 0;
    let totalUsed = 0;

    vouchers.forEach((v) => {
      totalUsed += v.usedCount || 0;
      const statusInfo = getVoucherStatusInfo(v);
      if (statusInfo.status === 'ACTIVE') active++;
      else if (statusInfo.status === 'UPCOMING') upcoming++;
      else if (statusInfo.status === 'EXPIRED') expired++;
    });

    return { total, active, upcoming, expired, totalUsed };
  }, [vouchers, getVoucherStatusInfo]);

  // Filtered Vouchers
  const filteredVouchers = useMemo(() => {
    return vouchers.filter((v) => {
      const matchSearch =
        v.code.toLowerCase().includes(searchQuery.toLowerCase()) ||
        (v.title && v.title.toLowerCase().includes(searchQuery.toLowerCase()));

      const statusInfo = getVoucherStatusInfo(v);
      let matchStatus = true;
      if (statusFilter !== 'ALL') {
        matchStatus = statusInfo.status === statusFilter;
      }

      let matchService = true;
      if (serviceFilter !== 'ALL') {
        matchService = v.applicableService === serviceFilter;
      }

      return matchSearch && matchStatus && matchService;
    });
  }, [vouchers, searchQuery, statusFilter, serviceFilter, getVoucherStatusInfo]);

  const formatCurrency = (val) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val || 0);
  };

  const formatDate = (isoStr) => {
    if (!isoStr) return '';
    const date = new Date(isoStr);
    return date.toLocaleString('vi-VN', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  return (
    <Box sx={{ p: { xs: 2, md: 3 }, minHeight: '100vh', bgcolor: 'background.default' }}>
      {/* Header */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: { xs: 'flex-start', sm: 'center' }, mb: 3, flexDirection: { xs: 'column', sm: 'row' }, gap: 2 }}>
        <Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
            <TicketIcon sx={{ fontSize: 32, color: '#f97316' }} />
            <Typography variant="h5" sx={{ fontWeight: 800, color: 'text.primary', fontSize: { xs: '1.2rem', sm: '1.5rem' } }}>
              Quản Lý Mã Khuyến Mãi (Vouchers)
            </Typography>
          </Box>
        </Box>

        <Stack direction="row" spacing={1.5} sx={{ width: { xs: '100%', sm: 'auto' } }}>
          <Button
            variant="outlined"
            startIcon={<RefreshIcon />}
            onClick={fetchVouchers}
            disabled={loading}
            sx={{ borderRadius: 2, flex: { xs: 1, sm: 'none' } }}
          >
            Làm mới
          </Button>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={handleOpenCreateModal}
            sx={{
              borderRadius: 2,
              bgcolor: '#f97316',
              '&:hover': { bgcolor: '#ea580c' },
              fontWeight: 700,
              flex: { xs: 1, sm: 'none' },
              whiteSpace: 'nowrap',
            }}
          >
            Thêm Voucher Mới
          </Button>
        </Stack>
      </Box>

      {/* Stat Cards */}
      <Grid container spacing={1.5} sx={{ mb: 3 }}>
        <Grid size={{ xs: 6, sm: 4, md: 2.4 }}>
          <Card sx={{ p: { xs: 1.5, sm: 2 }, borderRadius: 3, display: 'flex', alignItems: 'center', gap: 1.5, border: 1, borderColor: 'divider' }}>
            <Box sx={{ p: { xs: 1, sm: 1.5 }, borderRadius: 2.5, bgcolor: 'rgba(249, 115, 22, 0.1)', color: '#f97316' }}>
              <VoucherIcon sx={{ fontSize: { xs: 22, sm: 26 } }} />
            </Box>
            <Box>
              <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600, fontSize: { xs: '0.7rem', sm: '0.75rem' } }}>Tổng số Voucher</Typography>
              <Typography variant="h5" sx={{ fontWeight: 800, fontSize: { xs: '1.1rem', sm: '1.4rem' } }}>{stats.total}</Typography>
            </Box>
          </Card>
        </Grid>

        <Grid size={{ xs: 6, sm: 4, md: 2.4 }}>
          <Card sx={{ p: { xs: 1.5, sm: 2 }, borderRadius: 3, display: 'flex', alignItems: 'center', gap: 1.5, border: 1, borderColor: 'divider' }}>
            <Box sx={{ p: { xs: 1, sm: 1.5 }, borderRadius: 2.5, bgcolor: 'rgba(34, 197, 94, 0.1)', color: '#22c55e' }}>
              <ActiveIcon sx={{ fontSize: { xs: 22, sm: 26 } }} />
            </Box>
            <Box>
              <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600, fontSize: { xs: '0.7rem', sm: '0.75rem' } }}>Đang hoạt động</Typography>
              <Typography variant="h5" sx={{ fontWeight: 800, color: '#22c55e', fontSize: { xs: '1.1rem', sm: '1.4rem' } }}>{stats.active}</Typography>
            </Box>
          </Card>
        </Grid>

        <Grid size={{ xs: 6, sm: 4, md: 2.4 }}>
          <Card sx={{ p: { xs: 1.5, sm: 2 }, borderRadius: 3, display: 'flex', alignItems: 'center', gap: 1.5, border: 1, borderColor: 'divider' }}>
            <Box sx={{ p: { xs: 1, sm: 1.5 }, borderRadius: 2.5, bgcolor: 'rgba(59, 130, 246, 0.1)', color: '#3b82f6' }}>
              <UpcomingIcon sx={{ fontSize: { xs: 22, sm: 26 } }} />
            </Box>
            <Box>
              <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600, fontSize: { xs: '0.7rem', sm: '0.75rem' } }}>Chưa đến hạn</Typography>
              <Typography variant="h5" sx={{ fontWeight: 800, color: '#3b82f6', fontSize: { xs: '1.1rem', sm: '1.4rem' } }}>{stats.upcoming}</Typography>
            </Box>
          </Card>
        </Grid>

        <Grid size={{ xs: 6, sm: 4, md: 2.4 }}>
          <Card sx={{ p: { xs: 1.5, sm: 2 }, borderRadius: 3, display: 'flex', alignItems: 'center', gap: 1.5, border: 1, borderColor: 'divider' }}>
            <Box sx={{ p: { xs: 1, sm: 1.5 }, borderRadius: 2.5, bgcolor: 'rgba(239, 68, 68, 0.1)', color: '#ef4444' }}>
              <ExpiredIcon sx={{ fontSize: { xs: 22, sm: 26 } }} />
            </Box>
            <Box>
              <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600, fontSize: { xs: '0.7rem', sm: '0.75rem' } }}>Đã hết hạn</Typography>
              <Typography variant="h5" sx={{ fontWeight: 800, color: '#ef4444', fontSize: { xs: '1.1rem', sm: '1.4rem' } }}>{stats.expired}</Typography>
            </Box>
          </Card>
        </Grid>

        <Grid size={{ xs: 12, sm: 4, md: 2.4 }}>
          <Card sx={{ p: { xs: 1.5, sm: 2 }, borderRadius: 3, display: 'flex', alignItems: 'center', gap: 1.5, border: 1, borderColor: 'divider' }}>
            <Box sx={{ p: { xs: 1, sm: 1.5 }, borderRadius: 2.5, bgcolor: 'rgba(168, 85, 247, 0.1)', color: '#a855f7' }}>
              <TimeIcon sx={{ fontSize: { xs: 22, sm: 26 } }} />
            </Box>
            <Box>
              <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600, fontSize: { xs: '0.7rem', sm: '0.75rem' } }}>Lượt sử dụng</Typography>
              <Typography variant="h5" sx={{ fontWeight: 800, color: '#a855f7', fontSize: { xs: '1.1rem', sm: '1.4rem' } }}>{stats.totalUsed}</Typography>
            </Box>
          </Card>
        </Grid>
      </Grid>

      {/* Search & Filters */}
      <Card sx={{ p: 2, mb: 3, borderRadius: 3, border: 1, borderColor: 'divider' }}>
        <Grid container spacing={2} alignItems="center">
          <Grid size={{ xs: 12, md: 6 }}>
            <TextField
              fullWidth
              size="small"
              placeholder="Tìm theo mã voucher hoặc tiêu đề..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <SearchIcon sx={{ color: 'text.secondary' }} />
                  </InputAdornment>
                ),
              }}
            />
          </Grid>
          <Grid size={{ xs: 6, md: 3 }}>
            <FormControl fullWidth size="small">
              <InputLabel>Trạng thái</InputLabel>
              <Select
                value={statusFilter}
                label="Trạng thái"
                onChange={(e) => setStatusFilter(e.target.value)}
              >
                <MenuItem value="ALL">Tất cả trạng thái</MenuItem>
                <MenuItem value="ACTIVE">Đang kích hoạt</MenuItem>
                <MenuItem value="UPCOMING">Chưa đến hạn</MenuItem>
                <MenuItem value="INACTIVE">Đang tạm dừng</MenuItem>
                <MenuItem value="EXPIRED">Đã hết hạn</MenuItem>
              </Select>
            </FormControl>
          </Grid>
          <Grid size={{ xs: 6, md: 3 }}>
            <FormControl fullWidth size="small">
              <InputLabel>Dịch vụ</InputLabel>
              <Select
                value={serviceFilter}
                label="Dịch vụ"
                onChange={(e) => setServiceFilter(e.target.value)}
              >
                <MenuItem value="ALL">Tất cả dịch vụ</MenuItem>
                <MenuItem value="RIDE_HAILING">Đặt xe (Ride Hailing)</MenuItem>
                <MenuItem value="FOOD_DELIVERY">Giao đồ ăn (Food)</MenuItem>
              </Select>
            </FormControl>
          </Grid>
        </Grid>
      </Card>

      {/* Voucher List */}
      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}>
          <CircularProgress sx={{ color: '#f97316' }} />
        </Box>
      ) : filteredVouchers.length === 0 ? (
        <Paper sx={{ p: 6, textAlign: 'center', borderRadius: 3, border: '1px dashed', borderColor: 'divider' }}>
          <VoucherIcon sx={{ fontSize: 48, color: 'text.disabled', mb: 1.5 }} />
          <Typography variant="h6" sx={{ fontWeight: 700, color: 'text.secondary' }}>
            Không tìm thấy mã khuyến mãi nào
          </Typography>
          <Typography variant="body2" sx={{ color: 'text.disabled', mt: 0.5 }}>
            Bấm "Thêm Voucher Mới" để tạo và đưa mã vào hệ thống.
          </Typography>
        </Paper>
      ) : (
        <Grid container spacing={2.5}>
          {filteredVouchers.map((v) => {
            const statusInfo = getVoucherStatusInfo(v);
            const usagePercent = v.totalUsageLimit > 0 ? Math.min(100, Math.round((v.usedCount / v.totalUsageLimit) * 100)) : 0;

            return (
              <Grid size={{ xs: 12, md: 6, lg: 4 }} key={v.code}>
                <Card
                  sx={{
                    p: 2.5,
                    borderRadius: 3,
                    border: 1,
                    borderColor: statusInfo.isActive
                      ? 'rgba(249, 115, 22, 0.35)'
                      : statusInfo.isUpcoming
                      ? 'rgba(59, 130, 246, 0.35)'
                      : 'divider',
                    bgcolor: isDark ? 'background.paper' : '#ffffff',
                    position: 'relative',
                    boxShadow: statusInfo.isActive
                      ? '0 4px 20px rgba(249, 115, 22, 0.08)'
                      : statusInfo.isUpcoming
                      ? '0 4px 20px rgba(59, 130, 246, 0.08)'
                      : 'none',
                    transition: 'transform 0.2s, box-shadow 0.2s',
                    '&:hover': {
                      transform: 'translateY(-2px)',
                      boxShadow: '0 8px 24px rgba(0,0,0,0.12)',
                    },
                  }}
                >
                  {/* Top Bar: Code & Actions */}
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 1.5 }}>
                    <Box
                      onClick={() => handleCopyCode(v.code)}
                      sx={{
                        display: 'inline-flex',
                        alignItems: 'center',
                        gap: 1,
                        px: 1.5,
                        py: 0.5,
                        borderRadius: 2,
                        bgcolor: isDark ? 'rgba(249, 115, 22, 0.15)' : '#fff7ed',
                        border: '1px dashed #f97316',
                        cursor: 'pointer',
                        '&:hover': { opacity: 0.8 },
                      }}
                    >
                      <Typography sx={{ fontWeight: 800, color: '#f97316', letterSpacing: '0.05em' }}>
                        {v.code}
                      </Typography>
                      <Tooltip title="Sao chép mã">
                        <CopyIcon sx={{ fontSize: 16, color: '#f97316' }} />
                      </Tooltip>
                    </Box>

                    <Stack direction="row" alignItems="center" spacing={0.5}>
                      <Tooltip title="Xem lịch sử User sử dụng mã">
                        <IconButton
                          size="small"
                          sx={{ color: '#3b82f6', bgcolor: 'rgba(59, 130, 246, 0.1)' }}
                          onClick={() => handleViewUsages(v.code)}
                        >
                          <PeopleIcon fontSize="small" />
                        </IconButton>
                      </Tooltip>
                      <Tooltip title="Chỉnh sửa mã voucher">
                        <IconButton
                          size="small"
                          sx={{ color: '#f59e0b', bgcolor: 'rgba(245, 158, 11, 0.1)' }}
                          onClick={() => handleOpenEditModal(v)}
                        >
                          <EditIcon fontSize="small" />
                        </IconButton>
                      </Tooltip>
                      <Tooltip title={statusInfo.isExpired ? 'Mã đã hết hạn' : v.status === 'ACTIVE' ? 'Bấm để tạm dừng' : 'Bấm để kích hoạt'}>
                        <span>
                          <Switch
                            size="small"
                            checked={v.status === 'ACTIVE'}
                            disabled={statusInfo.isExpired}
                            onChange={() => handleToggleStatus(v.code)}
                            color="warning"
                          />
                        </span>
                      </Tooltip>
                      <Tooltip title="Xóa vĩnh viễn">
                        <IconButton size="small" color="error" onClick={() => handleOpenDeleteDialog(v.code)}>
                          <DeleteIcon fontSize="small" />
                        </IconButton>
                      </Tooltip>
                    </Stack>
                  </Box>

                  {/* Title & Description */}
                  <Typography variant="subtitle1" sx={{ fontWeight: 700, mb: 0.5 }}>
                    {v.title}
                  </Typography>
                  <Typography variant="body2" sx={{ color: 'text.secondary', mb: 2, minHeight: 40 }}>
                    {v.description || 'Không có mô tả chi tiết'}
                  </Typography>

                  {/* Discount highlight badge */}
                  <Box sx={{ p: 1.5, borderRadius: 2, bgcolor: isDark ? 'rgba(255,255,255,0.03)' : '#f8fafc', mb: 2 }}>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <Typography variant="body2" sx={{ color: 'text.secondary', fontWeight: 600 }}>
                        Mức giảm giá:
                      </Typography>
                      <Typography variant="subtitle2" sx={{ fontWeight: 800, color: '#f97316' }}>
                        {v.discountType === 'PERCENTAGE'
                          ? `Giảm ${v.discountValue}% (Tối đa ${formatCurrency(v.maxDiscountAmount)})`
                          : `Giảm ${formatCurrency(v.discountValue)}`}
                      </Typography>
                    </Box>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mt: 0.8 }}>
                      <Typography variant="caption" sx={{ color: 'text.secondary' }}>
                        Đơn tối thiểu:
                      </Typography>
                      <Typography variant="caption" sx={{ fontWeight: 700 }}>
                        {formatCurrency(v.minOrderAmount)}
                      </Typography>
                    </Box>
                  </Box>

                  {/* Quota Progress */}
                  <Box sx={{ mb: 2 }}>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.5 }}>
                      <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600 }}>
                        {v.totalUsageLimit <= 0
                          ? `Đã dùng: ${v.usedCount} lượt (Không giới hạn)`
                          : `Đã dùng: ${v.usedCount} / ${v.totalUsageLimit} (${v.remainingCount} còn lại)`}
                      </Typography>
                      <Typography variant="caption" sx={{ fontWeight: 700, color: v.totalUsageLimit <= 0 ? '#10b981' : usagePercent > 90 ? '#ef4444' : 'text.primary' }}>
                        {v.totalUsageLimit <= 0 ? '∞' : `${usagePercent}%`}
                      </Typography>
                    </Box>
                    <LinearProgress
                      variant="determinate"
                      value={v.totalUsageLimit <= 0 ? 100 : usagePercent}
                      sx={{
                        height: 6,
                        borderRadius: 3,
                        bgcolor: isDark ? 'rgba(255,255,255,0.1)' : '#e2e8f0',
                        '& .MuiLinearProgress-bar': {
                          bgcolor: v.totalUsageLimit <= 0 ? '#10b981' : usagePercent > 90 ? '#ef4444' : '#f97316',
                          borderRadius: 3,
                        },
                      }}
                    />
                  </Box>

                  <Divider sx={{ my: 1.5 }} />

                  {/* Footer tags */}
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 1 }}>
                    <Stack direction="row" spacing={1} alignItems="center">
                      {/* Service Badge */}
                      <Chip
                        size="small"
                        icon={
                          v.applicableService === 'RIDE_HAILING' ? <RideIcon sx={{ fontSize: '14px !important' }} /> :
                          v.applicableService === 'FOOD_DELIVERY' ? <FoodIcon sx={{ fontSize: '14px !important' }} /> :
                          <AllServiceIcon sx={{ fontSize: '14px !important' }} />
                        }
                        label={
                          v.applicableService === 'RIDE_HAILING' ? 'Đặt xe' :
                          v.applicableService === 'FOOD_DELIVERY' ? 'Giao món' : 'Tất cả dịch vụ'
                        }
                        sx={{ fontSize: '0.72rem', fontWeight: 600 }}
                      />

                      {/* Status Badge */}
                      <Chip
                        size="small"
                        label={statusInfo.label}
                        color={statusInfo.color}
                        sx={{ fontSize: '0.72rem', fontWeight: 700 }}
                      />
                    </Stack>

                    <Typography
                      variant="caption"
                      sx={{
                        color: statusInfo.isExpired ? '#ef4444' : statusInfo.isUpcoming ? '#3b82f6' : 'text.secondary',
                        display: 'flex',
                        alignItems: 'center',
                        gap: 0.5,
                        fontWeight: statusInfo.isExpired || statusInfo.isUpcoming ? 600 : 400,
                      }}
                    >
                      <TimeIcon sx={{ fontSize: 13 }} />
                      {statusInfo.isUpcoming
                        ? `Bắt đầu: ${formatDate(v.startAt)}`
                        : statusInfo.isExpired
                        ? `Đã hết hạn: ${formatDate(v.expireAt)}`
                        : `Hết hạn: ${formatDate(v.expireAt)}`}
                    </Typography>
                  </Box>
                </Card>
              </Grid>
            );
          })}
        </Grid>
      )}

      {/* Modal Lịch Sử Người Dùng Sử Dụng Voucher */}
      <Dialog
        open={openUsagesModal}
        onClose={() => setOpenUsagesModal(false)}
        maxWidth="md"
        fullWidth
      >
        <DialogTitle sx={{ fontWeight: 800, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
            <PeopleIcon sx={{ color: '#3b82f6' }} /> Lịch sử Người Dùng sử dụng mã: {selectedVoucherCode}
          </Box>
          <IconButton size="small" onClick={() => setOpenUsagesModal(false)}>
            <CloseIcon fontSize="small" />
          </IconButton>
        </DialogTitle>
        <DialogContent dividers>
          {loadingUsages ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', py: 6 }}>
              <CircularProgress size={32} sx={{ color: '#3b82f6' }} />
            </Box>
          ) : usagesList.length === 0 ? (
            <Box sx={{ py: 6, textAlign: 'center' }}>
              <Typography variant="body1" sx={{ color: 'text.secondary', fontWeight: 600 }}>
                Chưa có khách hàng nào sử dụng mã khuyến mãi này.
              </Typography>
            </Box>
          ) : (
            <TableContainer component={Paper} elevation={0} sx={{ border: 1, borderColor: 'divider', borderRadius: 2 }}>
              <Table size="small">
                <TableHead sx={{ bgcolor: isDark ? 'rgba(255,255,255,0.05)' : '#f8fafc' }}>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 700 }}>User ID</TableCell>
                    <TableCell sx={{ fontWeight: 700 }}>Mã Đơn / Cuốc xe</TableCell>
                    <TableCell sx={{ fontWeight: 700 }}>Dịch vụ</TableCell>
                    <TableCell sx={{ fontWeight: 700 }}>Giá trị gốc</TableCell>
                    <TableCell sx={{ fontWeight: 700 }}>Tiền giảm</TableCell>
                    <TableCell sx={{ fontWeight: 700 }}>Thực trả</TableCell>
                    <TableCell sx={{ fontWeight: 700 }}>Thời gian dùng</TableCell>
                    <TableCell sx={{ fontWeight: 700 }}>Trạng thái</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {usagesList.map((usage) => (
                    <TableRow key={usage.id} hover>
                      <TableCell sx={{ fontWeight: 600 }}>{usage.userId}</TableCell>
                      <TableCell>{usage.orderId || 'Chưa gắn mã'}</TableCell>
                      <TableCell>
                        <Chip
                          size="small"
                          label={usage.serviceType === 'FOOD_DELIVERY' ? 'Giao món' : 'Đặt xe'}
                          sx={{ fontSize: '0.7rem', fontWeight: 600 }}
                        />
                      </TableCell>
                      <TableCell>{formatCurrency(usage.originalAmount)}</TableCell>
                      <TableCell sx={{ fontWeight: 700, color: '#f97316' }}>
                        -{formatCurrency(usage.discountAmount)}
                      </TableCell>
                      <TableCell sx={{ fontWeight: 700 }}>
                        {formatCurrency(usage.finalAmount)}
                      </TableCell>
                      <TableCell sx={{ fontSize: '0.78rem' }}>
                        {formatDate(usage.usedAt)}
                      </TableCell>
                      <TableCell>
                        <Chip
                          size="small"
                          label={usage.status === 'REFUNDED' ? 'Đã hoàn lại' : 'Đã sử dụng'}
                          color={usage.status === 'REFUNDED' ? 'error' : 'success'}
                          sx={{ fontSize: '0.7rem', fontWeight: 700 }}
                        />
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          )}
        </DialogContent>
        <DialogActions sx={{ px: 3, py: 1.5 }}>
          <Button onClick={() => setOpenUsagesModal(false)} variant="contained" sx={{ borderRadius: 2 }}>
            Đóng
          </Button>
        </DialogActions>
      </Dialog>

      {/* Modal Tạo / Chỉnh sửa Voucher */}
      <Dialog open={openModal} onClose={() => setOpenModal(false)} maxWidth="md" fullWidth>
        <DialogTitle sx={{ fontWeight: 800, display: 'flex', alignItems: 'center', gap: 1.5, pb: 1.5 }}>
          <TicketIcon sx={{ color: '#f97316' }} />
          {editingVoucher ? `Chỉnh Sửa Mã Khuyến Mãi: ${editingVoucher.code}` : 'Thêm Mã Khuyến Mãi Mới (Lưu DB & Redis)'}
        </DialogTitle>
        <DialogContent dividers sx={{ p: 3 }}>
          <Box component="form" onSubmit={handleSaveVoucher}>
            <Grid container spacing={2.5}>
              {/* Row 1: Code & Dịch vụ */}
              <Grid size={{ xs: 12, sm: 6 }}>
                <TextField
                  fullWidth
                  required
                  label="Mã Voucher (Code)"
                  placeholder="VD: OMNIGO50 (3-20 ký tự)"
                  value={formData.code}
                  helperText={editingVoucher ? 'Có thể chỉnh sửa nếu muốn đổi sang mã mới' : 'Từ 3-20 ký tự gồm chữ cái, số, gạch nối'}
                  onChange={(e) => setFormData({ ...formData, code: e.target.value.toUpperCase() })}
                />
              </Grid>

              <Grid size={{ xs: 12, sm: 6 }}>
                <FormControl fullWidth required>
                  <InputLabel>Dịch vụ áp dụng</InputLabel>
                  <Select
                    value={formData.applicableService}
                    label="Dịch vụ áp dụng"
                    onChange={(e) => setFormData({ ...formData, applicableService: e.target.value })}
                  >
                    <MenuItem value="ALL">Tất cả dịch vụ (ALL)</MenuItem>
                    <MenuItem value="RIDE_HAILING">Chỉ đặt xe (Ride Hailing)</MenuItem>
                    <MenuItem value="FOOD_DELIVERY">Chỉ giao đồ ăn (Food Delivery)</MenuItem>
                  </Select>
                </FormControl>
              </Grid>

              {/* Row 2: Tiêu đề */}
              <Grid size={12}>
                <TextField
                  fullWidth
                  required
                  label="Tiêu đề hiển thị"
                  placeholder="VD: Giảm 20% cho chuyến xe hoặc đơn đồ ăn đầu tiên"
                  value={formData.title}
                  onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                />
              </Grid>

              {/* Row 3: Mô tả chi tiết */}
              <Grid size={12}>
                <TextField
                  fullWidth
                  multiline
                  rows={2}
                  label="Mô tả chi tiết"
                  placeholder="Điều kiện áp dụng và thông tin chi tiết của khuyến mãi..."
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                />
              </Grid>

              {/* Row 4: Loại giảm giá & Giá trị giảm */}
              <Grid size={{ xs: 12, sm: 6 }}>
                <FormControl fullWidth required>
                  <InputLabel>Loại giảm giá</InputLabel>
                  <Select
                    value={formData.discountType}
                    label="Loại giảm giá"
                    onChange={(e) => setFormData({ ...formData, discountType: e.target.value })}
                  >
                    <MenuItem value="PERCENTAGE">Giảm theo %</MenuItem>
                    <MenuItem value="FIXED_AMOUNT">Giảm số tiền cố định (VNĐ)</MenuItem>
                  </Select>
                </FormControl>
              </Grid>

              <Grid size={{ xs: 12, sm: 6 }}>
                <TextField
                  fullWidth
                  required
                  type="number"
                  label={formData.discountType === 'PERCENTAGE' ? 'Phần trăm giảm (%)' : 'Số tiền giảm (VNĐ)'}
                  placeholder={formData.discountType === 'PERCENTAGE' ? 'VD: 20' : 'VD: 30000'}
                  value={formData.discountValue}
                  onChange={(e) => setFormData({ ...formData, discountValue: e.target.value })}
                  InputProps={{
                    endAdornment: (
                      <InputAdornment position="end">
                        {formData.discountType === 'PERCENTAGE' ? <PercentIcon /> : <MoneyIcon />}
                      </InputAdornment>
                    ),
                  }}
                />
              </Grid>

              {/* Row 5: Mức giảm tối đa & Đơn tối thiểu (Cố định vị trí, không bao giờ nhảy hàng) */}
              <Grid size={{ xs: 12, sm: 6 }}>
                <TextField
                  fullWidth
                  required={formData.discountType === 'PERCENTAGE'}
                  disabled={formData.discountType !== 'PERCENTAGE'}
                  type="number"
                  label={formData.discountType === 'PERCENTAGE' ? 'Mức giảm tối đa (VNĐ) *' : 'Mức giảm tối đa (VNĐ)'}
                  placeholder={formData.discountType === 'PERCENTAGE' ? 'VD: 30000' : 'Không áp dụng khi giảm theo tiền mặt'}
                  value={formData.discountType === 'PERCENTAGE' ? formData.maxDiscountAmount : ''}
                  onChange={(e) => setFormData({ ...formData, maxDiscountAmount: e.target.value })}
                />
              </Grid>

              <Grid size={{ xs: 12, sm: 6 }}>
                <TextField
                  fullWidth
                  type="number"
                  label="Đơn tối thiểu (VNĐ)"
                  placeholder="VD: 50000 (0 nếu không yêu cầu)"
                  value={formData.minOrderAmount}
                  onChange={(e) => setFormData({ ...formData, minOrderAmount: e.target.value })}
                />
              </Grid>

              {/* Row 6: Tổng số lượng phát hành & Giới hạn mỗi khách hàng */}
              <Grid size={{ xs: 12, sm: 6 }}>
                <TextField
                  fullWidth
                  type="number"
                  label="Tổng số lượng phát hành"
                  placeholder="Để trống = Không giới hạn (vô hạn)"
                  value={formData.totalUsageLimit}
                  onChange={(e) => setFormData({ ...formData, totalUsageLimit: e.target.value })}
                />
              </Grid>

              <Grid size={{ xs: 12, sm: 6 }}>
                <TextField
                  fullWidth
                  required
                  type="number"
                  label="Giới hạn mỗi khách hàng"
                  placeholder="VD: 1 (lần / khách)"
                  value={formData.usageLimitPerUser}
                  onChange={(e) => setFormData({ ...formData, usageLimitPerUser: e.target.value })}
                />
              </Grid>

              {/* Row 7: Thời gian kích hoạt & Thời gian hết hạn (TTL) */}
              <Grid size={{ xs: 12, sm: 6 }}>
                <TextField
                  fullWidth
                  required
                  type="datetime-local"
                  label="Thời gian kích hoạt"
                  InputLabelProps={{ shrink: true }}
                  value={formData.startAt}
                  onChange={(e) => setFormData({ ...formData, startAt: e.target.value })}
                />
              </Grid>

              <Grid size={{ xs: 12, sm: 6 }}>
                <TextField
                  fullWidth
                  required
                  type="datetime-local"
                  label="Thời gian hết hạn (TTL Redis)"
                  InputLabelProps={{ shrink: true }}
                  value={formData.expireAt}
                  onChange={(e) => setFormData({ ...formData, expireAt: e.target.value })}
                />
              </Grid>
            </Grid>
          </Box>
        </DialogContent>
        <DialogActions sx={{ px: 3, py: 2 }}>
          <Button onClick={() => setOpenModal(false)} color="inherit" disabled={submitting}>
            Hủy
          </Button>
          <Button
            onClick={handleSaveVoucher}
            variant="contained"
            disabled={submitting}
            sx={{
              bgcolor: '#f97316',
              '&:hover': { bgcolor: '#ea580c' },
              fontWeight: 700,
            }}
          >
            {submitting ? <CircularProgress size={22} color="inherit" /> : (editingVoucher ? 'Cập Nhật Voucher' : 'Tạo Voucher Mới')}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Confirm Delete Dialog */}
      <ConfirmDialog
        open={deleteDialog.open}
        title="Xác nhận xóa Voucher"
        content={`Bạn có chắc muốn xóa voucher '${deleteDialog.voucherCode}' không? Thẻ voucher sẽ được ẩn ngay và bạn có 5 giây để hoàn tác nếu đổi ý.`}
        confirmText="Xác nhận xóa"
        cancelText="Hủy"
        confirmColor="error"
        loading={false}
        onConfirm={handleConfirmDelete}
        onClose={handleCloseDeleteDialog}
        iconType="delete"
      />

      {/* Thanh Hoàn tác 5s sau khi xóa */}
      <UndoDeleteSnackbar
        open={Boolean(pendingDeleteVoucher)}
        itemName={pendingDeleteVoucher?.code || pendingDeleteVoucher?.name}
        itemType="voucher"
        secondsLeft={voucherSecondsLeft}
        onUndo={handleUndoDeleteVoucher}
        onDismiss={handleDismissDeleteVoucher}
      />
    </Box>
  );
};

export default Vouchers;
