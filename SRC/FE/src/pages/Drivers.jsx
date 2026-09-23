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
  Stack,
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
  CheckCircle as ApproveIcon,
  Cancel as RejectIcon,
  LockOpen as UnlockIcon,
  Lock as LockIcon,
  HourglassEmpty as PendingIcon,
  Visibility as ViewDocIcon,
  ZoomIn as ZoomIcon,
  AssignmentTurnedIn as VerificationIcon,
  OpenInNew as OpenInNewIcon,
  CardMembership as DocCardIcon,
  CloudUpload as CloudUploadIcon,
  Delete as DeleteIcon,
} from '@mui/icons-material';
import toast from 'react-hot-toast';
import driverService from '../services/driverService';
import userService from '../services/userService';
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
  const [uploadingDocKeys, setUploadingDocKeys] = useState({});
  const [createForm, setCreateForm] = useState({
    fullName: '',
    phoneNumber: '',
    email: '',
    password: '',
    vehicleType: 'BIKE',
    licensePlate: '',
    vehicleModel: '',
    cccdNumber: '',
    cccdFrontImage: '',
    cccdBackImage: '',
    gplxNumber: '',
    gplxFrontImage: '',
    gplxBackImage: '',
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
  const [uploadingEditDocKeys, setUploadingEditDocKeys] = useState({});
  const [editForm, setEditForm] = useState({
    fullName: '',
    phoneNumber: '',
    email: '',
    vehicleType: 'BIKE',
    licensePlate: '',
    vehicleModel: '',
    cccdNumber: '',
    cccdFrontImage: '',
    cccdBackImage: '',
    gplxNumber: '',
    gplxFrontImage: '',
    gplxBackImage: '',
  });
  const [editErrors, setEditErrors] = useState({
    fullName: '',
    phoneNumber: '',
    email: '',
    vehicleType: '',
    licensePlate: '',
    vehicleModel: '',
  });

  // Dialog State: Reject Approval
  const [openRejectDialog, setOpenRejectDialog] = useState(false);
  const [selectedDriverForReject, setSelectedDriverForReject] = useState(null);
  const [rejectReason, setRejectReason] = useState('');
  const [rejectSubmitting, setRejectSubmitting] = useState(false);

  // Dialog State: Thẩm định hồ sơ đối tác & Giấy tờ (CCCD / GPLX)
  const [openVerifyDialog, setOpenVerifyDialog] = useState(false);
  const [selectedDriverForVerify, setSelectedDriverForVerify] = useState(null);

  // Dialog State: Phóng to hình ảnh gốc (Lightbox)
  const [openZoomDialog, setOpenZoomDialog] = useState(false);
  const [zoomImageUrl, setZoomImageUrl] = useState('');
  const [zoomImageTitle, setZoomImageTitle] = useState('');

  // Dialog State: Lock / Unlock Driver
  const [openLockDialog, setOpenLockDialog] = useState(false);
  const [selectedDriverForLock, setSelectedDriverForLock] = useState(null);
  const [lockReason, setLockReason] = useState('');
  const [lockSubmitting, setLockSubmitting] = useState(false);

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

    if (!form.fullName.trim()) {
      errors.fullName = 'Họ và tên không được để trống';
    } else if (form.fullName.trim().length < 2) {
      errors.fullName = 'Họ và tên phải có ít nhất 2 ký tự';
    }

    const cleanPhone = form.phoneNumber.trim();
    if (!cleanPhone) {
      errors.phoneNumber = 'Số điện thoại không được để trống';
    } else if (cleanPhone.length !== 10) {
      errors.phoneNumber = `Số điện thoại phải có đúng 10 chữ số (hiện tại: ${cleanPhone.length} số)`;
    } else if (!VIETNAMESE_PHONE_REGEX.test(cleanPhone)) {
      errors.phoneNumber = 'Số điện thoại không hợp lệ (phải bắt đầu bằng 03, 05, 07, 08, 09)';
    }

    if (form.email && form.email.trim()) {
      if (!EMAIL_REGEX.test(form.email.trim())) {
        errors.email = 'Địa chỉ email không đúng định dạng (Ví dụ: taixe@gmail.com)';
      }
    }

    if (!isEdit) {
      if (!form.password) {
        errors.password = 'Mật khẩu đăng nhập không được để trống';
      } else if (form.password.length < 6) {
        errors.password = 'Mật khẩu phải có ít nhất 6 ký tự';
      }
    }

    const cleanPlate = (form.licensePlate || '').trim().toUpperCase();
    if (!cleanPlate) {
      errors.licensePlate = 'Biển số xe không được để trống';
    } else if (!LICENSE_PLATE_REGEX.test(cleanPlate)) {
      errors.licensePlate = 'Biển số xe không đúng định dạng (Ví dụ: 29A-123.45, 51F-888.88, 29B1-567.89)';
    }

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
      cccdNumber: '',
      cccdFrontImage: '',
      cccdBackImage: '',
      gplxNumber: '',
      gplxFrontImage: '',
      gplxBackImage: '',
    });
    setCreateErrors({
      fullName: '',
      phoneNumber: '',
      email: '',
      password: '',
      vehicleType: '',
      licensePlate: '',
      vehicleModel: '',
      cccdNumber: '',
      gplxNumber: '',
    });
    setUploadingDocKeys({});
    setCreateGeneralError('');
    setShowCreatePassword(false);
    setOpenCreateDialog(true);
  };

  const handleUploadDoc = async (file, key) => {
    if (!file) return;
    setUploadingDocKeys((prev) => ({ ...prev, [key]: true }));
    try {
      // 1. Show immediate preview
      const reader = new FileReader();
      reader.onload = (e) => {
        setCreateForm((prev) => ({ ...prev, [key]: e.target.result }));
      };
      reader.readAsDataURL(file);

      // 2. Upload to Cloudinary via backend API
      const res = await driverService.uploadDocument(file);
      const url = res?.data?.url || res?.url;
      if (url) {
        setCreateForm((prev) => ({ ...prev, [key]: url }));
        toast.success('Đã tải ảnh lên Cloudinary thành công');
      }
    } catch (err) {
      console.warn('Lỗi khi tải ảnh lên Cloudinary qua API, giữ ảnh để BE lưu trữ:', err);
    } finally {
      setUploadingDocKeys((prev) => ({ ...prev, [key]: false }));
    }
  };

  const handleRemoveDoc = (key) => {
    setCreateForm((prev) => ({ ...prev, [key]: '' }));
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
      const defaultDocPlaceholder = 'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="400" height="250" viewBox="0 0 400 250"><rect width="400" height="250" fill="%23e2e8f0"/><text x="50%" y="50%" font-size="16" fill="%2364748b" text-anchor="middle" dominant-baseline="middle">Hồ sơ giấy tờ mẫu OmniGo</text></svg>';

      await driverService.createDriver({
        fullName: createForm.fullName.trim(),
        phoneNumber: createForm.phoneNumber.trim(),
        email: createForm.email.trim() || undefined,
        password: createForm.password,
        vehicleType: createForm.vehicleType,
        licensePlate: createForm.licensePlate.trim().toUpperCase(),
        vehicleModel: createForm.vehicleModel.trim() || 'Xe tiêu chuẩn',
        cccdNumber: createForm.cccdNumber || undefined,
        cccdFrontImage: createForm.cccdFrontImage || defaultDocPlaceholder,
        cccdBackImage: createForm.cccdBackImage || defaultDocPlaceholder,
        gplxNumber: createForm.gplxNumber || undefined,
        gplxFrontImage: createForm.gplxFrontImage || defaultDocPlaceholder,
        gplxBackImage: createForm.gplxBackImage || defaultDocPlaceholder,
      });
      toast.success('Đăng ký tài xế thành công (Đang ở trạng thái Chờ Duyệt)!');
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
  const normalizeVehicleType = (type) => {
    if (!type) return 'BIKE';
    const upper = String(type).trim().toUpperCase();
    if (upper === 'MOTORBIKE' || upper === 'BIKE' || upper === 'XE MÁY' || upper === 'XE_MAY') return 'BIKE';
    if (upper === 'CAR_4_SEATS' || upper === 'CAR_4_SEAT' || upper === 'CAR_4' || upper === 'CAR4') return 'CAR_4_SEAT';
    if (upper === 'CAR_7_SEATS' || upper === 'CAR_7_SEAT' || upper === 'CAR_7' || upper === 'CAR7') return 'CAR_7_SEAT';
    if (upper === 'EXPRESS' || upper === 'DELIVERY') return 'EXPRESS';
    return ['BIKE', 'CAR_4_SEAT', 'CAR_7_SEAT', 'EXPRESS'].includes(upper) ? upper : 'BIKE';
  };

  const handleOpenEdit = (driver) => {
    setEditingDriverId(driver.id);
    setEditForm({
      fullName: driver.fullName || '',
      phoneNumber: driver.phoneNumber || '',
      email: driver.email || '',
      vehicleType: normalizeVehicleType(driver.vehicleType),
      licensePlate: driver.licensePlate || '',
      vehicleModel: driver.vehicleModel || '',
      cccdNumber: driver.cccdNumber || '',
      cccdFrontImage: driver.cccdFrontImage || '',
      cccdBackImage: driver.cccdBackImage || '',
      gplxNumber: driver.gplxNumber || '',
      gplxFrontImage: driver.gplxFrontImage || '',
      gplxBackImage: driver.gplxBackImage || '',
    });
    setUploadingEditDocKeys({});
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

  const handleUploadEditDoc = async (file, key) => {
    if (!file) return;
    setUploadingEditDocKeys((prev) => ({ ...prev, [key]: true }));
    try {
      // 1. Show immediate preview
      const reader = new FileReader();
      reader.onload = (e) => {
        setEditForm((prev) => ({ ...prev, [key]: e.target.result }));
      };
      reader.readAsDataURL(file);

      // 2. Upload to Cloudinary via backend API
      const res = await driverService.uploadDocument(file);
      const url = res?.data?.url || res?.url;
      if (url) {
        setEditForm((prev) => ({ ...prev, [key]: url }));
        toast.success('Đã tải ảnh lên Cloudinary thành công');
      }
    } catch (err) {
      console.warn('Lỗi khi tải ảnh lên Cloudinary qua API, giữ ảnh để BE lưu trữ:', err);
    } finally {
      setUploadingEditDocKeys((prev) => ({ ...prev, [key]: false }));
    }
  };

  const handleRemoveEditDoc = (key) => {
    setEditForm((prev) => ({ ...prev, [key]: '' }));
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
      await driverService.updateDriverAdmin(editingDriverId, {
        fullName: editForm.fullName.trim(),
        phoneNumber: editForm.phoneNumber.trim(),
        email: editForm.email.trim() || undefined,
        vehicleType: editForm.vehicleType,
        licensePlate: editForm.licensePlate.trim().toUpperCase(),
        vehicleModel: editForm.vehicleModel.trim() || 'Xe tiêu chuẩn',
        cccdNumber: editForm.cccdNumber || undefined,
        cccdFrontImage: editForm.cccdFrontImage || undefined,
        cccdBackImage: editForm.cccdBackImage || undefined,
        gplxNumber: editForm.gplxNumber || undefined,
        gplxFrontImage: editForm.gplxFrontImage || undefined,
        gplxBackImage: editForm.gplxBackImage || undefined,
      });
      toast.success(`Cập nhật thông tin tài xế #${editingDriverId} thành công!`);
      setOpenEditDialog(false);
      fetchDrivers();
    } catch (err) {
      const { message, fieldErrors } = parseApiError(err, 'Lỗi khi cập nhật thông tin tài xế');
      setEditErrors((prev) => ({ ...prev, ...fieldErrors }));
      setEditGeneralError(message);
      toast.error(message);
    } finally {
      setEditSubmitting(false);
    }
  };

  // Handle Approve Driver
  const handleApproveDriver = async (driver) => {
    try {
      await driverService.approveDriver(driver.id, {
        status: 'APPROVED',
      });
      toast.success(`Đã phê duyệt hồ sơ tài xế #${driver.id} (${driver.fullName}) thành công!`);
      if (selectedDriverForVerify?.id === driver.id) {
        setSelectedDriverForVerify((prev) => prev ? { ...prev, approvalStatus: 'APPROVED' } : null);
      }
      fetchDrivers();
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Lỗi khi duyệt tài xế';
      toast.error(msg);
    }
  };

  // Handle Open Verification Dialog
  const handleOpenVerifyDialog = (driver) => {
    setSelectedDriverForVerify(driver);
    setOpenVerifyDialog(true);
  };

  const handleCloseVerifyDialog = () => {
    setOpenVerifyDialog(false);
    setSelectedDriverForVerify(null);
  };

  // Handle Zoom Image Lightbox
  const handleOpenZoom = (imageUrl, title) => {
    if (!imageUrl) return;
    setZoomImageUrl(imageUrl);
    setZoomImageTitle(title || 'Xem Chi Tiết Giấy Tờ');
    setOpenZoomDialog(true);
  };

  const handleCloseZoom = () => {
    setOpenZoomDialog(false);
    setZoomImageUrl('');
    setZoomImageTitle('');
  };

  // Handle Reject Driver Dialog
  const handleOpenRejectDialog = (driver) => {
    setSelectedDriverForReject(driver);
    setRejectReason('Ảnh giấy tờ hoặc thông tin phương tiện không hợp lệ');
    setOpenRejectDialog(true);
  };

  const handleCloseRejectDialog = () => {
    if (!rejectSubmitting) {
      setOpenRejectDialog(false);
      setSelectedDriverForReject(null);
    }
  };

  const handleRejectSubmit = async () => {
    if (!selectedDriverForReject) return;
    if (!rejectReason.trim()) {
      toast.error('Vui lòng nhập lý do từ chối hồ sơ');
      return;
    }

    setRejectSubmitting(true);
    try {
      await driverService.approveDriver(selectedDriverForReject.id, {
        status: 'REJECTED',
        reason: rejectReason.trim(),
      });
      toast.success(`Đã từ chối hồ sơ tài xế #${selectedDriverForReject.id}!`);
      if (selectedDriverForVerify?.id === selectedDriverForReject.id) {
        setSelectedDriverForVerify((prev) => prev ? { ...prev, approvalStatus: 'REJECTED', rejectionReason: rejectReason.trim() } : null);
      }
      setOpenRejectDialog(false);
      setSelectedDriverForReject(null);
      fetchDrivers();
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Lỗi khi từ chối hồ sơ tài xế';
      toast.error(msg);
    } finally {
      setRejectSubmitting(false);
    }
  };

  // Handle Lock / Unlock Driver
  const handleOpenLockDriver = (driver) => {
    setSelectedDriverForLock(driver);
    setLockReason(driver.isLocked ? '' : 'Vi phạm quy định vận chuyển/tác phong');
    setOpenLockDialog(true);
  };

  const handleCloseLockDriver = () => {
    if (!lockSubmitting) {
      setOpenLockDialog(false);
      setSelectedDriverForLock(null);
    }
  };

  const handleToggleLockSubmit = async () => {
    if (!selectedDriverForLock) return;
    const isLocking = !selectedDriverForLock.isLocked;

    if (isLocking && !lockReason.trim()) {
      toast.error('Vui lòng nhập lý do khóa tài xế');
      return;
    }

    setLockSubmitting(true);
    try {
      await userService.toggleLockUser(selectedDriverForLock.id, {
        isLocked: isLocking,
        reason: isLocking ? lockReason.trim() : '',
      });
      toast.success(isLocking ? 'Đã khóa tài khoản tài xế' : 'Đã mở khóa tài khoản tài xế');
      setOpenLockDialog(false);
      setSelectedDriverForLock(null);
      fetchDrivers();
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Lỗi khóa/mở khóa tài xế';
      toast.error(msg);
    } finally {
      setLockSubmitting(false);
    }
  };

  const getVehicleChip = (type) => {
    const cleanType = normalizeVehicleType(type);
    switch (cleanType) {
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

  const getApprovalChip = (driver) => {
    const status = driver.approvalStatus || 'APPROVED';
    if (status === 'PENDING_APPROVAL') {
      return (
        <Chip
          icon={<PendingIcon sx={{ fontSize: '14px !important', color: '#d97706 !important' }} />}
          label="Chờ Duyệt"
          size="small"
          sx={{
            bgcolor: 'rgba(255, 184, 0, 0.15)',
            color: '#d97706',
            fontWeight: 700,
            fontSize: '0.75rem',
            border: '1px solid rgba(255, 184, 0, 0.4)',
          }}
        />
      );
    }
    if (status === 'REJECTED') {
      return (
        <Tooltip title={`Lý do từ chối: ${driver.rejectionReason || 'Không có lý do'}`}>
          <Chip
            icon={<RejectIcon sx={{ fontSize: '14px !important', color: '#ef4444 !important' }} />}
            label="Từ Chối"
            size="small"
            sx={{
              bgcolor: 'rgba(239, 68, 68, 0.12)',
              color: '#ef4444',
              fontWeight: 700,
              fontSize: '0.75rem',
              border: '1px solid rgba(239, 68, 68, 0.3)',
            }}
          />
        </Tooltip>
      );
    }
    return (
      <Chip
        icon={<ApproveIcon sx={{ fontSize: '14px !important', color: '#15ca20 !important' }} />}
        label="Đã Duyệt"
        size="small"
        sx={{
          bgcolor: 'rgba(21, 202, 32, 0.12)',
          color: '#15ca20',
          fontWeight: 700,
          fontSize: '0.75rem',
          border: '1px solid rgba(21, 202, 32, 0.3)',
        }}
      />
    );
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
            fontSize: '0.75rem',
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
            fontSize: '0.75rem',
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
          fontSize: '0.75rem',
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
    const approval = d.approvalStatus || 'APPROVED';

    if (statusFilter === 'ALL') return true;
    if (statusFilter === 'PENDING') return approval === 'PENDING_APPROVAL';
    if (statusFilter === 'APPROVED') return approval === 'APPROVED';
    if (statusFilter === 'REJECTED') return approval === 'REJECTED';
    if (statusFilter === 'ONLINE') return isOnline;
    if (statusFilter === 'OFFLINE') return !isOnline;
    if (statusFilter === 'LOCKED') return Boolean(d.isLocked);
    return true;
  });

  const pendingCount = drivers.filter((d) => (d.approvalStatus || 'APPROVED') === 'PENDING_APPROVAL').length;
  const approvedCount = drivers.filter((d) => (d.approvalStatus || 'APPROVED') === 'APPROVED').length;
  const rejectedCount = drivers.filter((d) => d.approvalStatus === 'REJECTED').length;
  const lockedCount = drivers.filter((d) => d.isLocked).length;
  const onlineCount = drivers.filter((d) => d.status === 'ONLINE' || d.isActive === true).length;
  const offlineCount = drivers.length - onlineCount;

  const paginatedDrivers = filteredDrivers.slice(
    page * rowsPerPage,
    page * rowsPerPage + rowsPerPage
  );

  return (
    <Box sx={{ width: '100%', maxWidth: '100%', overflowX: 'hidden' }} className="page-enter-animation">
      {/* Header bar */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: { xs: 'flex-start', sm: 'center' }, mb: 2.5, flexDirection: { xs: 'column', sm: 'row' }, gap: 1.5 }}>
        <Box>
          <Typography variant="h5" sx={{ fontWeight: 800 }}>
            Quản Lý & Duyệt Tài Xế
          </Typography>
          <Typography variant="body2" sx={{ color: 'text.secondary', mt: 0.3, fontSize: { xs: '0.8rem', sm: '0.85rem' } }}>
            Thẩm định hồ sơ đăng ký, quản lý trạng thái phương tiện và khóa/mở khóa đối tác
          </Typography>
        </Box>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.2, width: { xs: '100%', sm: 'auto' }, flexWrap: 'wrap' }}>
          <Button
            variant="contained"
            startIcon={<AddDriverIcon />}
            onClick={handleOpenCreate}
            sx={{
              bgcolor: '#15ca20',
              '&:hover': { bgcolor: '#12b01c' },
              borderRadius: 2,
              fontWeight: 700,
              fontSize: '0.82rem',
              flex: { xs: 1, sm: 'none' },
            }}
          >
            Thêm Tài Xế Mới
          </Button>

          <Button
            variant="outlined"
            startIcon={<RefreshIcon />}
            onClick={fetchDrivers}
            disabled={loading}
            sx={{ borderRadius: 2, fontWeight: 600, fontSize: '0.82rem', flex: { xs: 1, sm: 'none' } }}
          >
            Làm Mới
          </Button>
        </Box>
      </Box>

      {/* Mini Metric Overview Cards */}
      <Grid container spacing={1.5} sx={{ mb: 2.5, width: '100%' }}>
        <Grid size={{ xs: 6, sm: 3 }}>
          <Card sx={{ p: { xs: 1.5, sm: 2 }, display: 'flex', alignItems: 'center', gap: 1.2 }}>
            <Box sx={{ p: 1, borderRadius: 2, bgcolor: 'rgba(0, 140, 255, 0.1)', color: '#008cff', display: 'flex' }}>
              <BikeIcon sx={{ fontSize: { xs: 20, sm: 24 } }} />
            </Box>
            <Box>
              <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600, fontSize: { xs: '0.7rem', sm: '0.75rem' } }}>Tổng tài xế</Typography>
              <Typography variant="h6" sx={{ fontWeight: 800, fontSize: { xs: '1rem', sm: '1.25rem' } }}>{drivers.length}</Typography>
            </Box>
          </Card>
        </Grid>
        <Grid size={{ xs: 6, sm: 3 }}>
          <Card sx={{ p: { xs: 1.5, sm: 2 }, display: 'flex', alignItems: 'center', gap: 1.2 }}>
            <Box sx={{ p: 1, borderRadius: 2, bgcolor: 'rgba(255, 184, 0, 0.15)', color: '#d97706', display: 'flex' }}>
              <PendingIcon sx={{ fontSize: { xs: 20, sm: 24 } }} />
            </Box>
            <Box>
              <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600, fontSize: { xs: '0.7rem', sm: '0.75rem' } }}>Chờ duyệt</Typography>
              <Typography variant="h6" sx={{ fontWeight: 800, color: '#d97706', fontSize: { xs: '1rem', sm: '1.25rem' } }}>{pendingCount}</Typography>
            </Box>
          </Card>
        </Grid>
        <Grid size={{ xs: 6, sm: 3 }}>
          <Card sx={{ p: { xs: 1.5, sm: 2 }, display: 'flex', alignItems: 'center', gap: 1.2 }}>
            <Box sx={{ p: 1, borderRadius: 2, bgcolor: 'rgba(21, 202, 32, 0.15)', color: '#15ca20', display: 'flex' }}>
              <DotIcon sx={{ fontSize: { xs: 20, sm: 24 } }} />
            </Box>
            <Box>
              <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600, fontSize: { xs: '0.7rem', sm: '0.75rem' } }}>Trực tuyến</Typography>
              <Typography variant="h6" sx={{ fontWeight: 800, color: '#15ca20', fontSize: { xs: '1rem', sm: '1.25rem' } }}>{onlineCount}</Typography>
            </Box>
          </Card>
        </Grid>
        <Grid size={{ xs: 6, sm: 3 }}>
          <Card sx={{ p: { xs: 1.5, sm: 2 }, display: 'flex', alignItems: 'center', gap: 1.2 }}>
            <Box sx={{ p: 1, borderRadius: 2, bgcolor: 'rgba(239, 68, 68, 0.15)', color: '#ef4444', display: 'flex' }}>
              <LockIcon sx={{ fontSize: { xs: 20, sm: 24 } }} />
            </Box>
            <Box>
              <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600, fontSize: { xs: '0.7rem', sm: '0.75rem' } }}>Bị khóa</Typography>
              <Typography variant="h6" sx={{ fontWeight: 800, color: '#ef4444', fontSize: { xs: '1rem', sm: '1.25rem' } }}>{lockedCount}</Typography>
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
      <Card sx={{ p: { xs: 1.8, sm: 2.5 }, minHeight: 'calc(90vh - 160px)', display: 'flex', flexDirection: 'column' }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2.5, flexDirection: { xs: 'column', md: 'row' }, gap: 2 }}>
          <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', width: { xs: '100%', md: 'auto' } }}>
            {[
              { label: 'Tất Cả', value: 'ALL', count: drivers.length },
              { label: 'Chờ Duyệt', value: 'PENDING', count: pendingCount, color: 'warning' },
              { label: 'Đã Duyệt', value: 'APPROVED', count: approvedCount },
              { label: 'Từ Chối', value: 'REJECTED', count: rejectedCount },
              { label: 'Trực Tuyến', value: 'ONLINE', count: onlineCount },
              { label: 'Đã Khóa', value: 'LOCKED', count: lockedCount },
            ].map((tab) => (
              <Button
                key={tab.value}
                variant={statusFilter === tab.value ? 'contained' : 'outlined'}
                onClick={() => {
                  setStatusFilter(tab.value);
                  setPage(0);
                }}
                size="small"
                sx={{
                  px: { xs: 1.5, sm: 2 },
                  py: 0.8,
                  fontWeight: 700,
                  fontSize: { xs: '0.78rem', sm: '0.85rem' },
                  borderRadius: 2,
                  flex: { xs: 1, sm: 'none' },
                  ...(tab.color === 'warning' && statusFilter !== tab.value ? { color: '#d97706', borderColor: '#d97706' } : {}),
                }}
              >
                {tab.label} ({tab.count})
              </Button>
            ))}
          </Box>

          <Box sx={{ width: { xs: '100%', md: 360 } }}>
            <TextField
              size="small"
              placeholder="Tìm theo Tên, SĐT, Email, Biển số..."
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

        <TableContainer sx={{ border: 1, borderColor: 'divider', borderRadius: 2, overflowX: 'auto', width: '100%', flex: 1, minHeight: 380 }}>
          <Table sx={{ minWidth: 950 }} size="small">
            <TableHead>
              <TableRow>
                <TableCell sx={{ width: 70 }}>ID</TableCell>
                <TableCell>Tài xế</TableCell>
                <TableCell>Số điện thoại</TableCell>
                <TableCell>Phương tiện</TableCell>
                <TableCell>Biển số xe</TableCell>
                <TableCell align="center">Duyệt hồ sơ</TableCell>
                <TableCell align="center">Vận hành</TableCell>
                <TableCell align="center">Tài khoản</TableCell>
                <TableCell align="right" sx={{ pr: 3, width: 130, whiteSpace: 'nowrap' }}>Hành động</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {loading ? (
                <TableRow>
                  <TableCell colSpan={9} align="center" sx={{ py: 6 }}>
                    <CircularProgress size={28} />
                    <Typography variant="body2" sx={{ mt: 1, color: 'text.secondary' }}>
                      Đang tải danh sách tài xế...
                    </Typography>
                  </TableCell>
                </TableRow>
              ) : paginatedDrivers.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={9} align="center" sx={{ py: 5, color: 'text.secondary' }}>
                    {searchTerm ? 'Không tìm thấy tài xế phù hợp' : 'Không có tài xế nào theo bộ lọc này'}
                  </TableCell>
                </TableRow>
              ) : (
                paginatedDrivers.map((driver) => {
                  const isPending = (driver.approvalStatus || 'APPROVED') === 'PENDING_APPROVAL';
                  const isRejected = driver.approvalStatus === 'REJECTED';

                  return (
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
                      <TableCell align="center">
                        <Box sx={{ cursor: 'pointer' }} onClick={() => handleOpenVerifyDialog(driver)}>
                          {getApprovalChip(driver)}
                        </Box>
                      </TableCell>
                      <TableCell align="center">
                        {getDriverStatusChip(driver)}
                      </TableCell>
                      <TableCell align="center">
                        {driver.isLocked ? (
                          <Tooltip title={`Lý do: ${driver.lockedReason || 'Không có lý do'}`}>
                            <Box sx={{ display: 'inline-flex', alignItems: 'center', gap: 0.6, color: '#dc2626', fontWeight: 600, fontSize: '0.84rem' }}>
                              <Box sx={{ width: 6, height: 6, borderRadius: '50%', bgcolor: '#dc2626' }} />
                              Bị khóa
                            </Box>
                          </Tooltip>
                        ) : (
                          <Box sx={{ display: 'inline-flex', alignItems: 'center', gap: 0.6, color: '#16a34a', fontWeight: 600, fontSize: '0.84rem' }}>
                            <Box sx={{ width: 6, height: 6, borderRadius: '50%', bgcolor: '#16a34a' }} />
                            Hoạt động
                          </Box>
                        )}
                      </TableCell>
                      <TableCell align="right" sx={{ pr: 2.5, whiteSpace: 'nowrap' }}>
                        <Stack direction="row" spacing={0.8} justifyContent="flex-end" alignItems="center">
                          {/* Thẩm định hồ sơ & Giấy tờ (CCCD, GPLX) */}
                          <Tooltip title="Thẩm định hồ sơ & Giấy tờ (CCCD, GPLX)">
                            <IconButton
                              size="small"
                              onClick={() => handleOpenVerifyDialog(driver)}
                              sx={{
                                color: isPending ? '#d97706' : '#6366f1',
                                bgcolor: isPending ? 'rgba(255, 184, 0, 0.15)' : 'rgba(99, 102, 241, 0.1)',
                                '&:hover': { bgcolor: isPending ? 'rgba(255, 184, 0, 0.25)' : 'rgba(99, 102, 241, 0.2)' },
                              }}
                            >
                              <ViewDocIcon fontSize="small" />
                            </IconButton>
                          </Tooltip>

                          {/* Phê duyệt nhanh */}
                          {isPending && (
                            <Tooltip title="Phê duyệt hồ sơ đối tác">
                              <IconButton
                                size="small"
                                onClick={() => handleApproveDriver(driver)}
                                sx={{ color: '#15ca20', bgcolor: 'rgba(21, 202, 32, 0.1)', '&:hover': { bgcolor: 'rgba(21, 202, 32, 0.2)' } }}
                              >
                                <ApproveIcon fontSize="small" />
                              </IconButton>
                            </Tooltip>
                          )}

                          {/* Từ chối */}
                          {isPending && (
                            <Tooltip title="Từ chối hồ sơ (Nhập lý do)">
                              <IconButton
                                size="small"
                                onClick={() => handleOpenRejectDialog(driver)}
                                sx={{ color: '#ef4444', bgcolor: 'rgba(239, 68, 68, 0.1)', '&:hover': { bgcolor: 'rgba(239, 68, 68, 0.2)' } }}
                              >
                                <RejectIcon fontSize="small" />
                              </IconButton>
                            </Tooltip>
                          )}

                          {/* Khóa / Mở khóa tài khoản */}
                          <Tooltip title={driver.isLocked ? 'Mở khóa tài khoản' : 'Khóa tài khoản tài xế'}>
                            <IconButton
                              size="small"
                              onClick={() => handleOpenLockDriver(driver)}
                              sx={{
                                color: driver.isLocked ? '#15ca20' : '#ef4444',
                                bgcolor: driver.isLocked ? 'rgba(21, 202, 32, 0.1)' : 'rgba(239, 68, 68, 0.1)',
                                '&:hover': { bgcolor: driver.isLocked ? 'rgba(21, 202, 32, 0.2)' : 'rgba(239, 68, 68, 0.2)' },
                              }}
                            >
                              {driver.isLocked ? <UnlockIcon fontSize="small" /> : <LockIcon fontSize="small" />}
                            </IconButton>
                          </Tooltip>

                          {/* Sửa thông tin */}
                          <Tooltip title="Chỉnh sửa thông tin">
                            <IconButton
                              size="small"
                              onClick={() => handleOpenEdit(driver)}
                              sx={{ color: '#008cff', bgcolor: 'rgba(0, 140, 255, 0.1)', '&:hover': { bgcolor: 'rgba(0, 140, 255, 0.2)' } }}
                            >
                              <EditIcon fontSize="small" />
                            </IconButton>
                          </Tooltip>
                        </Stack>
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
          sx={{ mt: 'auto', pt: 1.5 }}
        />
      </Card>

      {/* DIALOG: Thêm Tài Xế Mới */}
      <Dialog open={openCreateDialog} onClose={handleCloseCreate} maxWidth="md" fullWidth>
        <form onSubmit={handleCreateSubmit} noValidate>
          <DialogTitle component="div" sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', pb: 1 }}>
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
              <Grid size={{ xs: 12, sm: 6 }}>
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
                  placeholder="Ví dụ: Nguyễn Văn An"
                />
              </Grid>
              <Grid size={{ xs: 12, sm: 6 }}>
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
              <Grid size={{ xs: 12, sm: 6 }}>
                <TextField
                  label="Địa chỉ Email (Tùy chọn)"
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
                  placeholder="taixe@gmail.com"
                />
              </Grid>
              <Grid size={{ xs: 12, sm: 6 }}>
                <TextField
                  label="Mật khẩu khởi tạo"
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
                  placeholder="••••••••"
                />
              </Grid>
            </Grid>

            <Typography variant="subtitle2" sx={{ fontWeight: 700, color: '#008cff', mt: 3, mb: 1.5, textTransform: 'uppercase' }}>
              2. Đăng Ký Phương Tiện Vận Chuyển
            </Typography>
            <Grid container spacing={2}>
              <Grid size={{ xs: 12, sm: 6 }}>
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
              <Grid size={{ xs: 12, sm: 6 }}>
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
              <Grid size={12}>
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
                  placeholder="Ví dụ: Honda Vision 2023, Toyota Vios..."
                />
              </Grid>
            </Grid>

            <Typography variant="subtitle2" sx={{ fontWeight: 700, color: '#008cff', mt: 3, mb: 1.5, textTransform: 'uppercase', display: 'flex', alignItems: 'center', gap: 1 }}>
              <DocCardIcon sx={{ fontSize: 18 }} />
              3. Hồ Sơ & Giấy Tờ Đối Tác (CCCD & GPLX)
            </Typography>

            {/* CCCD Section */}
            <Card variant="outlined" sx={{ p: 2, mb: 2, bgcolor: '#f8fafc', borderRadius: 2 }}>
              <Grid container spacing={2} alignItems="center">
                <Grid size={12}>
                  <TextField
                    label="Số Căn cước công dân (CCCD / CMND)"
                    size="small"
                    fullWidth
                    value={createForm.cccdNumber}
                    onChange={(e) => setCreateForm({ ...createForm, cccdNumber: e.target.value.replace(/\D/g, '').slice(0, 12) })}
                    placeholder="Ví dụ: 001207879012"
                    helperText="Nhập dãy 9 hoặc 12 chữ số CCCD định danh"
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <Typography variant="caption" sx={{ fontWeight: 700, mb: 0.8, display: 'block', color: 'text.secondary' }}>
                    Ảnh CCCD Mặt Trước
                  </Typography>
                  {createForm.cccdFrontImage ? (
                    <Box sx={{ position: 'relative', border: '1px solid #e2e8f0', borderRadius: 2, overflow: 'hidden', height: 130, bgcolor: '#000' }}>
                      <Box component="img" src={createForm.cccdFrontImage} alt="CCCD Mặt trước" sx={{ width: '100%', height: '100%', objectFit: 'contain' }} />
                      <IconButton
                        size="small"
                        onClick={() => handleRemoveDoc('cccdFrontImage')}
                        sx={{ position: 'absolute', top: 5, right: 5, bgcolor: 'rgba(0,0,0,0.6)', color: '#fff', '&:hover': { bgcolor: '#ef4444' } }}
                      >
                        <DeleteIcon fontSize="small" />
                      </IconButton>
                    </Box>
                  ) : (
                    <Button
                      variant="outlined"
                      component="label"
                      fullWidth
                      disabled={Boolean(uploadingDocKeys.cccdFrontImage)}
                      startIcon={uploadingDocKeys.cccdFrontImage ? <CircularProgress size={18} /> : <CloudUploadIcon />}
                      sx={{ height: 130, borderStyle: 'dashed', borderWidth: 2, borderRadius: 2, display: 'flex', flexDirection: 'column', gap: 0.8 }}
                    >
                      <Typography variant="body2" sx={{ fontWeight: 600, textTransform: 'none' }}>
                        {uploadingDocKeys.cccdFrontImage ? 'Đang tải lên...' : 'Chọn ảnh CCCD mặt trước'}
                      </Typography>
                      <Typography variant="caption" sx={{ color: 'text.secondary', textTransform: 'none' }}>
                        PNG, JPG, WEBP tối đa 5MB
                      </Typography>
                      <input
                        type="file"
                        hidden
                        accept="image/*"
                        onChange={(e) => {
                          if (e.target.files?.[0]) handleUploadDoc(e.target.files[0], 'cccdFrontImage');
                        }}
                      />
                    </Button>
                  )}
                </Grid>

                <Grid size={{ xs: 12, sm: 6 }}>
                  <Typography variant="caption" sx={{ fontWeight: 700, mb: 0.8, display: 'block', color: 'text.secondary' }}>
                    Ảnh CCCD Mặt Sau
                  </Typography>
                  {createForm.cccdBackImage ? (
                    <Box sx={{ position: 'relative', border: '1px solid #e2e8f0', borderRadius: 2, overflow: 'hidden', height: 130, bgcolor: '#000' }}>
                      <Box component="img" src={createForm.cccdBackImage} alt="CCCD Mặt sau" sx={{ width: '100%', height: '100%', objectFit: 'contain' }} />
                      <IconButton
                        size="small"
                        onClick={() => handleRemoveDoc('cccdBackImage')}
                        sx={{ position: 'absolute', top: 5, right: 5, bgcolor: 'rgba(0,0,0,0.6)', color: '#fff', '&:hover': { bgcolor: '#ef4444' } }}
                      >
                        <DeleteIcon fontSize="small" />
                      </IconButton>
                    </Box>
                  ) : (
                    <Button
                      variant="outlined"
                      component="label"
                      fullWidth
                      disabled={Boolean(uploadingDocKeys.cccdBackImage)}
                      startIcon={uploadingDocKeys.cccdBackImage ? <CircularProgress size={18} /> : <CloudUploadIcon />}
                      sx={{ height: 130, borderStyle: 'dashed', borderWidth: 2, borderRadius: 2, display: 'flex', flexDirection: 'column', gap: 0.8 }}
                    >
                      <Typography variant="body2" sx={{ fontWeight: 600, textTransform: 'none' }}>
                        {uploadingDocKeys.cccdBackImage ? 'Đang tải lên...' : 'Chọn ảnh CCCD mặt sau'}
                      </Typography>
                      <Typography variant="caption" sx={{ color: 'text.secondary', textTransform: 'none' }}>
                        PNG, JPG, WEBP tối đa 5MB
                      </Typography>
                      <input
                        type="file"
                        hidden
                        accept="image/*"
                        onChange={(e) => {
                          if (e.target.files?.[0]) handleUploadDoc(e.target.files[0], 'cccdBackImage');
                        }}
                      />
                    </Button>
                  )}
                </Grid>
              </Grid>
            </Card>

            {/* GPLX Section */}
            <Card variant="outlined" sx={{ p: 2, bgcolor: '#f8fafc', borderRadius: 2 }}>
              <Grid container spacing={2} alignItems="center">
                <Grid size={12}>
                  <TextField
                    label="Số Giấy phép lái xe (GPLX)"
                    size="small"
                    fullWidth
                    value={createForm.gplxNumber}
                    onChange={(e) => setCreateForm({ ...createForm, gplxNumber: e.target.value.replace(/\D/g, '').slice(0, 15) })}
                    placeholder="Ví dụ: 099020000012"
                    helperText="Nhập số GPLX hợp lệ"
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <Typography variant="caption" sx={{ fontWeight: 700, mb: 0.8, display: 'block', color: 'text.secondary' }}>
                    Ảnh GPLX Mặt Trước
                  </Typography>
                  {createForm.gplxFrontImage ? (
                    <Box sx={{ position: 'relative', border: '1px solid #e2e8f0', borderRadius: 2, overflow: 'hidden', height: 130, bgcolor: '#000' }}>
                      <Box component="img" src={createForm.gplxFrontImage} alt="GPLX Mặt trước" sx={{ width: '100%', height: '100%', objectFit: 'contain' }} />
                      <IconButton
                        size="small"
                        onClick={() => handleRemoveDoc('gplxFrontImage')}
                        sx={{ position: 'absolute', top: 5, right: 5, bgcolor: 'rgba(0,0,0,0.6)', color: '#fff', '&:hover': { bgcolor: '#ef4444' } }}
                      >
                        <DeleteIcon fontSize="small" />
                      </IconButton>
                    </Box>
                  ) : (
                    <Button
                      variant="outlined"
                      component="label"
                      fullWidth
                      disabled={Boolean(uploadingDocKeys.gplxFrontImage)}
                      startIcon={uploadingDocKeys.gplxFrontImage ? <CircularProgress size={18} /> : <CloudUploadIcon />}
                      sx={{ height: 130, borderStyle: 'dashed', borderWidth: 2, borderRadius: 2, display: 'flex', flexDirection: 'column', gap: 0.8 }}
                    >
                      <Typography variant="body2" sx={{ fontWeight: 600, textTransform: 'none' }}>
                        {uploadingDocKeys.gplxFrontImage ? 'Đang tải lên...' : 'Chọn ảnh GPLX mặt trước'}
                      </Typography>
                      <Typography variant="caption" sx={{ color: 'text.secondary', textTransform: 'none' }}>
                        PNG, JPG, WEBP tối đa 5MB
                      </Typography>
                      <input
                        type="file"
                        hidden
                        accept="image/*"
                        onChange={(e) => {
                          if (e.target.files?.[0]) handleUploadDoc(e.target.files[0], 'gplxFrontImage');
                        }}
                      />
                    </Button>
                  )}
                </Grid>

                <Grid size={{ xs: 12, sm: 6 }}>
                  <Typography variant="caption" sx={{ fontWeight: 700, mb: 0.8, display: 'block', color: 'text.secondary' }}>
                    Ảnh GPLX Mặt Sau
                  </Typography>
                  {createForm.gplxBackImage ? (
                    <Box sx={{ position: 'relative', border: '1px solid #e2e8f0', borderRadius: 2, overflow: 'hidden', height: 130, bgcolor: '#000' }}>
                      <Box component="img" src={createForm.gplxBackImage} alt="GPLX Mặt sau" sx={{ width: '100%', height: '100%', objectFit: 'contain' }} />
                      <IconButton
                        size="small"
                        onClick={() => handleRemoveDoc('gplxBackImage')}
                        sx={{ position: 'absolute', top: 5, right: 5, bgcolor: 'rgba(0,0,0,0.6)', color: '#fff', '&:hover': { bgcolor: '#ef4444' } }}
                      >
                        <DeleteIcon fontSize="small" />
                      </IconButton>
                    </Box>
                  ) : (
                    <Button
                      variant="outlined"
                      component="label"
                      fullWidth
                      disabled={Boolean(uploadingDocKeys.gplxBackImage)}
                      startIcon={uploadingDocKeys.gplxBackImage ? <CircularProgress size={18} /> : <CloudUploadIcon />}
                      sx={{ height: 130, borderStyle: 'dashed', borderWidth: 2, borderRadius: 2, display: 'flex', flexDirection: 'column', gap: 0.8 }}
                    >
                      <Typography variant="body2" sx={{ fontWeight: 600, textTransform: 'none' }}>
                        {uploadingDocKeys.gplxBackImage ? 'Đang tải lên...' : 'Chọn ảnh GPLX mặt sau'}
                      </Typography>
                      <Typography variant="caption" sx={{ color: 'text.secondary', textTransform: 'none' }}>
                        PNG, JPG, WEBP tối đa 5MB
                      </Typography>
                      <input
                        type="file"
                        hidden
                        accept="image/*"
                        onChange={(e) => {
                          if (e.target.files?.[0]) handleUploadDoc(e.target.files[0], 'gplxBackImage');
                        }}
                      />
                    </Button>
                  )}
                </Grid>
              </Grid>
            </Card>
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
              {createSubmitting ? <CircularProgress size={20} color="inherit" /> : 'Đăng Ký Tài Xế'}
            </Button>
          </DialogActions>
        </form>
      </Dialog>

      {/* DIALOG: Chỉnh Sửa Thông Tin Tài Xế */}
      <Dialog open={openEditDialog} onClose={handleCloseEdit} maxWidth="md" fullWidth>
        <form onSubmit={handleEditSubmit} noValidate>
          <DialogTitle component="div" sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', pb: 1 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <EditIcon sx={{ color: '#008cff' }} />
              <Typography variant="h6" sx={{ fontWeight: 700 }}>
                Chỉnh Sửa Hồ Sơ Tài Xế #{editingDriverId}
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
              <Grid size={{ xs: 12, sm: 6 }}>
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
              <Grid size={{ xs: 12, sm: 6 }}>
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
              <Grid size={12}>
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
              <Grid size={{ xs: 12, sm: 6 }}>
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
              <Grid size={{ xs: 12, sm: 6 }}>
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
              <Grid size={12}>
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

            <Typography variant="subtitle2" sx={{ fontWeight: 700, color: '#008cff', mt: 3, mb: 1.5, textTransform: 'uppercase', display: 'flex', alignItems: 'center', gap: 1 }}>
              <DocCardIcon sx={{ fontSize: 18 }} />
              3. Cập Nhật Hồ Sơ Giấy Tờ (CCCD & GPLX)
            </Typography>

            {/* CCCD Section */}
            <Card variant="outlined" sx={{ p: 2, mb: 2, bgcolor: '#f8fafc', borderRadius: 2 }}>
              <Grid container spacing={2} alignItems="center">
                <Grid size={12}>
                  <TextField
                    label="Số Căn cước công dân (CCCD / CMND)"
                    size="small"
                    fullWidth
                    value={editForm.cccdNumber}
                    onChange={(e) => setEditForm({ ...editForm, cccdNumber: e.target.value.replace(/\D/g, '').slice(0, 12) })}
                    placeholder="Ví dụ: 001207879012"
                    helperText="Cập nhật mã số CCCD 9 hoặc 12 số của đối tác"
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <Typography variant="caption" sx={{ fontWeight: 700, mb: 0.8, display: 'block', color: 'text.secondary' }}>
                    Ảnh CCCD Mặt Trước
                  </Typography>
                  {editForm.cccdFrontImage ? (
                    <Box sx={{ position: 'relative', border: '1px solid #e2e8f0', borderRadius: 2, overflow: 'hidden', height: 130, bgcolor: '#000' }}>
                      <Box component="img" src={editForm.cccdFrontImage} alt="CCCD Mặt trước" sx={{ width: '100%', height: '100%', objectFit: 'contain' }} />
                      <IconButton
                        size="small"
                        onClick={() => handleRemoveEditDoc('cccdFrontImage')}
                        sx={{ position: 'absolute', top: 5, right: 5, bgcolor: 'rgba(0,0,0,0.6)', color: '#fff', '&:hover': { bgcolor: '#ef4444' } }}
                      >
                        <DeleteIcon fontSize="small" />
                      </IconButton>
                    </Box>
                  ) : (
                    <Button
                      variant="outlined"
                      component="label"
                      fullWidth
                      disabled={Boolean(uploadingEditDocKeys.cccdFrontImage)}
                      startIcon={uploadingEditDocKeys.cccdFrontImage ? <CircularProgress size={18} /> : <CloudUploadIcon />}
                      sx={{ height: 130, borderStyle: 'dashed', borderWidth: 2, borderRadius: 2, display: 'flex', flexDirection: 'column', gap: 0.8 }}
                    >
                      <Typography variant="body2" sx={{ fontWeight: 600, textTransform: 'none' }}>
                        {uploadingEditDocKeys.cccdFrontImage ? 'Đang tải lên...' : 'Chọn ảnh CCCD mặt trước'}
                      </Typography>
                      <Typography variant="caption" sx={{ color: 'text.secondary', textTransform: 'none' }}>
                        PNG, JPG, WEBP tối đa 5MB
                      </Typography>
                      <input
                        type="file"
                        hidden
                        accept="image/*"
                        onChange={(e) => {
                          if (e.target.files?.[0]) handleUploadEditDoc(e.target.files[0], 'cccdFrontImage');
                        }}
                      />
                    </Button>
                  )}
                </Grid>

                <Grid size={{ xs: 12, sm: 6 }}>
                  <Typography variant="caption" sx={{ fontWeight: 700, mb: 0.8, display: 'block', color: 'text.secondary' }}>
                    Ảnh CCCD Mặt Sau
                  </Typography>
                  {editForm.cccdBackImage ? (
                    <Box sx={{ position: 'relative', border: '1px solid #e2e8f0', borderRadius: 2, overflow: 'hidden', height: 130, bgcolor: '#000' }}>
                      <Box component="img" src={editForm.cccdBackImage} alt="CCCD Mặt sau" sx={{ width: '100%', height: '100%', objectFit: 'contain' }} />
                      <IconButton
                        size="small"
                        onClick={() => handleRemoveEditDoc('cccdBackImage')}
                        sx={{ position: 'absolute', top: 5, right: 5, bgcolor: 'rgba(0,0,0,0.6)', color: '#fff', '&:hover': { bgcolor: '#ef4444' } }}
                      >
                        <DeleteIcon fontSize="small" />
                      </IconButton>
                    </Box>
                  ) : (
                    <Button
                      variant="outlined"
                      component="label"
                      fullWidth
                      disabled={Boolean(uploadingEditDocKeys.cccdBackImage)}
                      startIcon={uploadingEditDocKeys.cccdBackImage ? <CircularProgress size={18} /> : <CloudUploadIcon />}
                      sx={{ height: 130, borderStyle: 'dashed', borderWidth: 2, borderRadius: 2, display: 'flex', flexDirection: 'column', gap: 0.8 }}
                    >
                      <Typography variant="body2" sx={{ fontWeight: 600, textTransform: 'none' }}>
                        {uploadingEditDocKeys.cccdBackImage ? 'Đang tải lên...' : 'Chọn ảnh CCCD mặt sau'}
                      </Typography>
                      <Typography variant="caption" sx={{ color: 'text.secondary', textTransform: 'none' }}>
                        PNG, JPG, WEBP tối đa 5MB
                      </Typography>
                      <input
                        type="file"
                        hidden
                        accept="image/*"
                        onChange={(e) => {
                          if (e.target.files?.[0]) handleUploadEditDoc(e.target.files[0], 'cccdBackImage');
                        }}
                      />
                    </Button>
                  )}
                </Grid>
              </Grid>
            </Card>

            {/* GPLX Section */}
            <Card variant="outlined" sx={{ p: 2, bgcolor: '#f8fafc', borderRadius: 2 }}>
              <Grid container spacing={2} alignItems="center">
                <Grid size={12}>
                  <TextField
                    label="Số Giấy phép lái xe (GPLX)"
                    size="small"
                    fullWidth
                    value={editForm.gplxNumber}
                    onChange={(e) => setEditForm({ ...editForm, gplxNumber: e.target.value.replace(/\D/g, '').slice(0, 15) })}
                    placeholder="Ví dụ: 099020000012"
                    helperText="Cập nhật số bằng lái xe mới"
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <Typography variant="caption" sx={{ fontWeight: 700, mb: 0.8, display: 'block', color: 'text.secondary' }}>
                    Ảnh GPLX Mặt Trước
                  </Typography>
                  {editForm.gplxFrontImage ? (
                    <Box sx={{ position: 'relative', border: '1px solid #e2e8f0', borderRadius: 2, overflow: 'hidden', height: 130, bgcolor: '#000' }}>
                      <Box component="img" src={editForm.gplxFrontImage} alt="GPLX Mặt trước" sx={{ width: '100%', height: '100%', objectFit: 'contain' }} />
                      <IconButton
                        size="small"
                        onClick={() => handleRemoveEditDoc('gplxFrontImage')}
                        sx={{ position: 'absolute', top: 5, right: 5, bgcolor: 'rgba(0,0,0,0.6)', color: '#fff', '&:hover': { bgcolor: '#ef4444' } }}
                      >
                        <DeleteIcon fontSize="small" />
                      </IconButton>
                    </Box>
                  ) : (
                    <Button
                      variant="outlined"
                      component="label"
                      fullWidth
                      disabled={Boolean(uploadingEditDocKeys.gplxFrontImage)}
                      startIcon={uploadingEditDocKeys.gplxFrontImage ? <CircularProgress size={18} /> : <CloudUploadIcon />}
                      sx={{ height: 130, borderStyle: 'dashed', borderWidth: 2, borderRadius: 2, display: 'flex', flexDirection: 'column', gap: 0.8 }}
                    >
                      <Typography variant="body2" sx={{ fontWeight: 600, textTransform: 'none' }}>
                        {uploadingEditDocKeys.gplxFrontImage ? 'Đang tải lên...' : 'Chọn ảnh GPLX mặt trước'}
                      </Typography>
                      <Typography variant="caption" sx={{ color: 'text.secondary', textTransform: 'none' }}>
                        PNG, JPG, WEBP tối đa 5MB
                      </Typography>
                      <input
                        type="file"
                        hidden
                        accept="image/*"
                        onChange={(e) => {
                          if (e.target.files?.[0]) handleUploadEditDoc(e.target.files[0], 'gplxFrontImage');
                        }}
                      />
                    </Button>
                  )}
                </Grid>

                <Grid size={{ xs: 12, sm: 6 }}>
                  <Typography variant="caption" sx={{ fontWeight: 700, mb: 0.8, display: 'block', color: 'text.secondary' }}>
                    Ảnh GPLX Mặt Sau
                  </Typography>
                  {editForm.gplxBackImage ? (
                    <Box sx={{ position: 'relative', border: '1px solid #e2e8f0', borderRadius: 2, overflow: 'hidden', height: 130, bgcolor: '#000' }}>
                      <Box component="img" src={editForm.gplxBackImage} alt="GPLX Mặt sau" sx={{ width: '100%', height: '100%', objectFit: 'contain' }} />
                      <IconButton
                        size="small"
                        onClick={() => handleRemoveEditDoc('gplxBackImage')}
                        sx={{ position: 'absolute', top: 5, right: 5, bgcolor: 'rgba(0,0,0,0.6)', color: '#fff', '&:hover': { bgcolor: '#ef4444' } }}
                      >
                        <DeleteIcon fontSize="small" />
                      </IconButton>
                    </Box>
                  ) : (
                    <Button
                      variant="outlined"
                      component="label"
                      fullWidth
                      disabled={Boolean(uploadingEditDocKeys.gplxBackImage)}
                      startIcon={uploadingEditDocKeys.gplxBackImage ? <CircularProgress size={18} /> : <CloudUploadIcon />}
                      sx={{ height: 130, borderStyle: 'dashed', borderWidth: 2, borderRadius: 2, display: 'flex', flexDirection: 'column', gap: 0.8 }}
                    >
                      <Typography variant="body2" sx={{ fontWeight: 600, textTransform: 'none' }}>
                        {uploadingEditDocKeys.gplxBackImage ? 'Đang tải lên...' : 'Chọn ảnh GPLX mặt sau'}
                      </Typography>
                      <Typography variant="caption" sx={{ color: 'text.secondary', textTransform: 'none' }}>
                        PNG, JPG, WEBP tối đa 5MB
                      </Typography>
                      <input
                        type="file"
                        hidden
                        accept="image/*"
                        onChange={(e) => {
                          if (e.target.files?.[0]) handleUploadEditDoc(e.target.files[0], 'gplxBackImage');
                        }}
                      />
                    </Button>
                  )}
                </Grid>
              </Grid>
            </Card>
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

      {/* DIALOG: Từ Chối Hồ Sơ Tài Xế */}
      <Dialog open={openRejectDialog} onClose={handleCloseRejectDialog} maxWidth="xs" fullWidth>
        <DialogTitle component="div" sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', pb: 1 }}>
          <Typography variant="h6" sx={{ fontWeight: 800, color: '#ef4444' }}>
            Từ chối duyệt hồ sơ tài xế
          </Typography>
          <IconButton onClick={handleCloseRejectDialog} size="small" disabled={rejectSubmitting}>
            <CloseIcon fontSize="small" />
          </IconButton>
        </DialogTitle>
        <DialogContent dividers>
          <Typography variant="body2" sx={{ mb: 2, color: 'text.secondary' }}>
            Vui lòng nhập lý do từ chối để thông báo cho tài xế {selectedDriverForReject?.fullName} (#{selectedDriverForReject?.id}):
          </Typography>
          <TextField
            label="Lý do từ chối hồ sơ"
            fullWidth
            multiline
            rows={3}
            required
            value={rejectReason}
            onChange={(e) => setRejectReason(e.target.value)}
            placeholder="Ví dụ: Bằng lái xe đã hết hạn, Biển số xe mờ không rõ nét..."
            size="small"
          />
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={handleCloseRejectDialog} disabled={rejectSubmitting} color="inherit">
            Hủy
          </Button>
          <Button
            onClick={handleRejectSubmit}
            variant="contained"
            color="error"
            disabled={rejectSubmitting}
          >
            {rejectSubmitting ? <CircularProgress size={20} color="inherit" /> : 'Xác Nhận Từ Chối'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* DIALOG: Khóa / Mở Khóa Tài Xế */}
      <Dialog open={openLockDialog} onClose={handleCloseLockDriver} maxWidth="xs" fullWidth>
        <DialogTitle component="div" sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', pb: 1 }}>
          <Typography variant="h6" sx={{ fontWeight: 800 }}>
            {selectedDriverForLock?.isLocked ? 'Xác nhận mở khóa tài xế' : 'Khóa tài khoản tài xế'}
          </Typography>
          <IconButton onClick={handleCloseLockDriver} size="small" disabled={lockSubmitting}>
            <CloseIcon fontSize="small" />
          </IconButton>
        </DialogTitle>
        <DialogContent dividers>
          <Typography variant="body2" sx={{ mb: 2, color: 'text.secondary' }}>
            {selectedDriverForLock?.isLocked
              ? `Bạn có chắc chắn muốn mở khóa cho tài xế ${selectedDriverForLock?.fullName} (${selectedDriverForLock?.phoneNumber}) không?`
              : `Khóa tài xế ${selectedDriverForLock?.fullName} (${selectedDriverForLock?.phoneNumber}) sẽ thu hồi quyền nhận cuốc và chuyển trạng thái về Ngoại Tuyến.`}
          </Typography>
          {!selectedDriverForLock?.isLocked && (
            <TextField
              label="Lý do khóa tài xế"
              fullWidth
              multiline
              rows={3}
              required
              value={lockReason}
              onChange={(e) => setLockReason(e.target.value)}
              placeholder="Nhập lý do vi phạm (ví dụ: Hủy chuyến nhiều lần, vi phạm quy tắc ứng xử...)"
              size="small"
            />
          )}
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={handleCloseLockDriver} disabled={lockSubmitting} color="inherit">
            Hủy
          </Button>
          <Button
            onClick={handleToggleLockSubmit}
            variant="contained"
            color={selectedDriverForLock?.isLocked ? 'success' : 'error'}
            disabled={lockSubmitting}
          >
            {lockSubmitting ? <CircularProgress size={20} color="inherit" /> : selectedDriverForLock?.isLocked ? 'Mở Khóa Ngay' : 'Xác Nhận Khóa'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* DIALOG: Thẩm Định & Phê Duyệt Hồ Sơ Tài Xế (CCCD, GPLX) */}
      <Dialog
        open={openVerifyDialog}
        onClose={handleCloseVerifyDialog}
        maxWidth="md"
        fullWidth
        PaperProps={{
          sx: { borderRadius: 2.5, overflow: 'hidden' },
        }}
      >
        <DialogTitle
          component="div"
          sx={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            bgcolor: '#f8fafc',
            borderBottom: '1px solid #e2e8f0',
            py: 1.5,
            px: 2.5,
          }}
        >
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.2 }}>
            <Box
              sx={{
                p: 0.8,
                borderRadius: 2,
                bgcolor: 'rgba(99, 102, 241, 0.1)',
                color: '#6366f1',
                display: 'flex',
              }}
            >
              <VerificationIcon sx={{ fontSize: 22 }} />
            </Box>
            <Box>
              <Typography variant="subtitle1" sx={{ fontWeight: 800, lineHeight: 1.2 }}>
                Thẩm Định Hồ Sơ Đối Tác Tài Xế
              </Typography>
              <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600 }}>
                Mã tài xế: #{selectedDriverForVerify?.id} — {selectedDriverForVerify?.fullName}
              </Typography>
            </Box>
          </Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            {selectedDriverForVerify && getApprovalChip(selectedDriverForVerify)}
            <IconButton onClick={handleCloseVerifyDialog} size="small">
              <CloseIcon fontSize="small" />
            </IconButton>
          </Box>
        </DialogTitle>

        <DialogContent dividers sx={{ p: { xs: 2, sm: 3 } }}>
          {/* Thông tin cảnh báo nếu bị từ chối */}
          {selectedDriverForVerify?.approvalStatus === 'REJECTED' && (
            <Alert severity="error" sx={{ mb: 2.5, borderRadius: 2 }}>
              <strong>Hồ sơ đã bị từ chối:</strong> {selectedDriverForVerify.rejectionReason || 'Không có lý do cụ thể'}
            </Alert>
          )}

          {/* Khối 1: Thông tin cơ bản & Phương tiện */}
          <Typography variant="subtitle2" sx={{ fontWeight: 800, color: '#1e293b', mb: 1.5, display: 'flex', alignItems: 'center', gap: 0.8 }}>
            <BikeIcon sx={{ fontSize: 18, color: '#008cff' }} />
            THÔNG TIN ĐỐI TÁC & PHƯƠNG TIỆN HOẠT ĐỘNG
          </Typography>
          <Card variant="outlined" sx={{ p: 2, mb: 3, bgcolor: '#f8fafc', borderRadius: 2 }}>
            <Grid container spacing={2}>
              <Grid size={{ xs: 12, sm: 4 }}>
                <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600 }}>Họ và tên</Typography>
                <Typography variant="body2" sx={{ fontWeight: 700 }}>{selectedDriverForVerify?.fullName || 'Chưa cập nhật'}</Typography>
              </Grid>
              <Grid size={{ xs: 12, sm: 4 }}>
                <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600 }}>Số điện thoại</Typography>
                <Typography variant="body2" sx={{ fontWeight: 700, fontFamily: 'monospace' }}>{selectedDriverForVerify?.phoneNumber || '—'}</Typography>
              </Grid>
              <Grid size={{ xs: 12, sm: 4 }}>
                <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600 }}>Địa chỉ Email</Typography>
                <Typography variant="body2" sx={{ fontWeight: 600 }}>{selectedDriverForVerify?.email || 'Chưa cập nhật'}</Typography>
              </Grid>
              <Grid size={{ xs: 12, sm: 4 }}>
                <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600 }}>Loại phương tiện</Typography>
                <Box sx={{ mt: 0.3 }}>{getVehicleChip(selectedDriverForVerify?.vehicleType)}</Box>
              </Grid>
              <Grid size={{ xs: 12, sm: 4 }}>
                <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600 }}>Biển số xe</Typography>
                <Typography variant="body2" sx={{ fontWeight: 800, fontFamily: 'monospace', color: '#008cff' }}>{selectedDriverForVerify?.licensePlate || 'Chưa cập nhật'}</Typography>
              </Grid>
              <Grid size={{ xs: 12, sm: 4 }}>
                <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600 }}>Dòng xe</Typography>
                <Typography variant="body2" sx={{ fontWeight: 600 }}>{selectedDriverForVerify?.vehicleModel || 'Chưa cập nhật'}</Typography>
              </Grid>
            </Grid>
          </Card>

          {/* Khối 2: Giấy tờ CCCD */}
          <Box sx={{ mb: 3 }}>
            <Box
              sx={{
                p: 1.5,
                mb: 2,
                borderRadius: 2,
                bgcolor: '#f0fdf4',
                border: '1px solid #bbf7d0',
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                flexWrap: 'wrap',
                gap: 1.5,
              }}
            >
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.2 }}>
                <Box sx={{ p: 0.8, borderRadius: 1.5, bgcolor: '#dcfce7', color: '#16a34a', display: 'flex' }}>
                  <DocCardIcon sx={{ fontSize: 22 }} />
                </Box>
                <Box>
                  <Typography variant="subtitle2" sx={{ fontWeight: 800, color: '#14532d', lineHeight: 1.2 }}>
                    1. CĂN CƯỚC CÔNG DÂN (CCCD / CMND)
                  </Typography>
                  <Typography variant="caption" sx={{ color: '#15803d', fontWeight: 500 }}>
                    Đối chiếu thông tin nhân thân định danh
                  </Typography>
                </Box>
              </Box>

              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <Typography variant="caption" sx={{ color: '#166534', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.04em' }}>
                  Số định danh CCCD:
                </Typography>
                <Box
                  sx={{
                    px: 1.8,
                    py: 0.5,
                    bgcolor: '#ffffff',
                    border: '1.5px solid #86efac',
                    borderRadius: 2,
                    boxShadow: '0 1px 3px rgba(22, 163, 74, 0.12)',
                    fontFamily: 'monospace',
                    fontWeight: 800,
                    fontSize: '1rem',
                    letterSpacing: '0.12em',
                    color: '#15803d',
                  }}
                >
                  {selectedDriverForVerify?.cccdNumber || 'Chưa cập nhật'}
                </Box>
              </Box>
            </Box>

            <Grid container spacing={2}>
              {/* CCCD Mặt trước */}
              <Grid size={{ xs: 12, sm: 6 }}>
                <Card variant="outlined" sx={{ p: 1.5, borderRadius: 2, textAlign: 'center', bgcolor: '#fafafa' }}>
                  <Typography variant="caption" sx={{ fontWeight: 700, display: 'block', mb: 1, color: '#475569' }}>
                    ẢNH CCCD MẶT TRƯỚC
                  </Typography>
                  {selectedDriverForVerify?.cccdFrontImage ? (
                    <Box sx={{ position: 'relative', overflow: 'hidden', borderRadius: 1.5, '&:hover .zoom-btn': { opacity: 1 } }}>
                      <Box
                        component="img"
                        src={selectedDriverForVerify.cccdFrontImage}
                        alt="CCCD Mặt Trước"
                        sx={{
                          width: '100%',
                          height: 180,
                          objectFit: 'contain',
                          bgcolor: '#000',
                          borderRadius: 1.5,
                          cursor: 'pointer',
                        }}
                        onClick={() => handleOpenZoom(selectedDriverForVerify.cccdFrontImage, 'Ảnh CCCD Mặt Trước')}
                      />
                      <Button
                        className="zoom-btn"
                        size="small"
                        variant="contained"
                        startIcon={<ZoomIcon />}
                        onClick={() => handleOpenZoom(selectedDriverForVerify.cccdFrontImage, 'Ảnh CCCD Mặt Trước')}
                        sx={{
                          position: 'absolute',
                          bottom: 10,
                          right: 10,
                          opacity: 0.85,
                          transition: 'opacity 0.2s',
                          fontSize: '0.75rem',
                          textTransform: 'none',
                        }}
                      >
                        Phóng to
                      </Button>
                    </Box>
                  ) : (
                    <Box sx={{ py: 6, bgcolor: '#f1f5f9', borderRadius: 1.5, color: 'text.secondary' }}>
                      <Typography variant="caption">Chưa tải lên ảnh mặt trước</Typography>
                    </Box>
                  )}
                </Card>
              </Grid>

              {/* CCCD Mặt sau */}
              <Grid size={{ xs: 12, sm: 6 }}>
                <Card variant="outlined" sx={{ p: 1.5, borderRadius: 2, textAlign: 'center', bgcolor: '#fafafa' }}>
                  <Typography variant="caption" sx={{ fontWeight: 700, display: 'block', mb: 1, color: '#475569' }}>
                    ẢNH CCCD MẶT SAU
                  </Typography>
                  {selectedDriverForVerify?.cccdBackImage ? (
                    <Box sx={{ position: 'relative', overflow: 'hidden', borderRadius: 1.5, '&:hover .zoom-btn': { opacity: 1 } }}>
                      <Box
                        component="img"
                        src={selectedDriverForVerify.cccdBackImage}
                        alt="CCCD Mặt Sau"
                        sx={{
                          width: '100%',
                          height: 180,
                          objectFit: 'contain',
                          bgcolor: '#000',
                          borderRadius: 1.5,
                          cursor: 'pointer',
                        }}
                        onClick={() => handleOpenZoom(selectedDriverForVerify.cccdBackImage, 'Ảnh CCCD Mặt Sau')}
                      />
                      <Button
                        className="zoom-btn"
                        size="small"
                        variant="contained"
                        startIcon={<ZoomIcon />}
                        onClick={() => handleOpenZoom(selectedDriverForVerify.cccdBackImage, 'Ảnh CCCD Mặt Sau')}
                        sx={{
                          position: 'absolute',
                          bottom: 10,
                          right: 10,
                          opacity: 0.85,
                          transition: 'opacity 0.2s',
                          fontSize: '0.75rem',
                          textTransform: 'none',
                        }}
                      >
                        Phóng to
                      </Button>
                    </Box>
                  ) : (
                    <Box sx={{ py: 6, bgcolor: '#f1f5f9', borderRadius: 1.5, color: 'text.secondary' }}>
                      <Typography variant="caption">Chưa tải lên ảnh mặt sau</Typography>
                    </Box>
                  )}
                </Card>
              </Grid>
            </Grid>
          </Box>

          {/* Khối 3: Giấy tờ GPLX */}
          <Box>
            <Box
              sx={{
                p: 1.5,
                mb: 2,
                borderRadius: 2,
                bgcolor: '#eff6ff',
                border: '1px solid #bfdbfe',
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                flexWrap: 'wrap',
                gap: 1.5,
              }}
            >
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.2 }}>
                <Box sx={{ p: 0.8, borderRadius: 1.5, bgcolor: '#dbeafe', color: '#2563eb', display: 'flex' }}>
                  <DocCardIcon sx={{ fontSize: 22 }} />
                </Box>
                <Box>
                  <Typography variant="subtitle2" sx={{ fontWeight: 800, color: '#1e3a8a', lineHeight: 1.2 }}>
                    2. GIẤY PHÉP LÁI XE (GPLX)
                  </Typography>
                  <Typography variant="caption" sx={{ color: '#1d4ed8', fontWeight: 500 }}>
                    Hạng bằng lái xe hợp lệ theo quy chuẩn
                  </Typography>
                </Box>
              </Box>

              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <Typography variant="caption" sx={{ color: '#1e40af', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.04em' }}>
                  Số GPLX:
                </Typography>
                <Box
                  sx={{
                    px: 1.8,
                    py: 0.5,
                    bgcolor: '#ffffff',
                    border: '1.5px solid #93c5fd',
                    borderRadius: 2,
                    boxShadow: '0 1px 3px rgba(37, 99, 235, 0.12)',
                    fontFamily: 'monospace',
                    fontWeight: 800,
                    fontSize: '1rem',
                    letterSpacing: '0.12em',
                    color: '#1d4ed8',
                  }}
                >
                  {selectedDriverForVerify?.gplxNumber || 'Chưa cập nhật'}
                </Box>
              </Box>
            </Box>

            <Grid container spacing={2}>
              {/* GPLX Mặt trước */}
              <Grid size={{ xs: 12, sm: 6 }}>
                <Card variant="outlined" sx={{ p: 1.5, borderRadius: 2, textAlign: 'center', bgcolor: '#fafafa' }}>
                  <Typography variant="caption" sx={{ fontWeight: 700, display: 'block', mb: 1, color: '#475569' }}>
                    ẢNH GPLX MẶT TRƯỚC
                  </Typography>
                  {selectedDriverForVerify?.gplxFrontImage ? (
                    <Box sx={{ position: 'relative', overflow: 'hidden', borderRadius: 1.5, '&:hover .zoom-btn': { opacity: 1 } }}>
                      <Box
                        component="img"
                        src={selectedDriverForVerify.gplxFrontImage}
                        alt="GPLX Mặt Trước"
                        sx={{
                          width: '100%',
                          height: 180,
                          objectFit: 'contain',
                          bgcolor: '#000',
                          borderRadius: 1.5,
                          cursor: 'pointer',
                        }}
                        onClick={() => handleOpenZoom(selectedDriverForVerify.gplxFrontImage, 'Ảnh GPLX Mặt Trước')}
                      />
                      <Button
                        className="zoom-btn"
                        size="small"
                        variant="contained"
                        startIcon={<ZoomIcon />}
                        onClick={() => handleOpenZoom(selectedDriverForVerify.gplxFrontImage, 'Ảnh GPLX Mặt Trước')}
                        sx={{
                          position: 'absolute',
                          bottom: 10,
                          right: 10,
                          opacity: 0.85,
                          transition: 'opacity 0.2s',
                          fontSize: '0.75rem',
                          textTransform: 'none',
                        }}
                      >
                        Phóng to
                      </Button>
                    </Box>
                  ) : (
                    <Box sx={{ py: 6, bgcolor: '#f1f5f9', borderRadius: 1.5, color: 'text.secondary' }}>
                      <Typography variant="caption">Chưa tải lên ảnh mặt trước</Typography>
                    </Box>
                  )}
                </Card>
              </Grid>

              {/* GPLX Mặt sau */}
              <Grid size={{ xs: 12, sm: 6 }}>
                <Card variant="outlined" sx={{ p: 1.5, borderRadius: 2, textAlign: 'center', bgcolor: '#fafafa' }}>
                  <Typography variant="caption" sx={{ fontWeight: 700, display: 'block', mb: 1, color: '#475569' }}>
                    ẢNH GPLX MẶT SAU
                  </Typography>
                  {selectedDriverForVerify?.gplxBackImage ? (
                    <Box sx={{ position: 'relative', overflow: 'hidden', borderRadius: 1.5, '&:hover .zoom-btn': { opacity: 1 } }}>
                      <Box
                        component="img"
                        src={selectedDriverForVerify.gplxBackImage}
                        alt="GPLX Mặt Sau"
                        sx={{
                          width: '100%',
                          height: 180,
                          objectFit: 'contain',
                          bgcolor: '#000',
                          borderRadius: 1.5,
                          cursor: 'pointer',
                        }}
                        onClick={() => handleOpenZoom(selectedDriverForVerify.gplxBackImage, 'Ảnh GPLX Mặt Sau')}
                      />
                      <Button
                        className="zoom-btn"
                        size="small"
                        variant="contained"
                        startIcon={<ZoomIcon />}
                        onClick={() => handleOpenZoom(selectedDriverForVerify.gplxBackImage, 'Ảnh GPLX Mặt Sau')}
                        sx={{
                          position: 'absolute',
                          bottom: 10,
                          right: 10,
                          opacity: 0.85,
                          transition: 'opacity 0.2s',
                          fontSize: '0.75rem',
                          textTransform: 'none',
                        }}
                      >
                        Phóng to
                      </Button>
                    </Box>
                  ) : (
                    <Box sx={{ py: 6, bgcolor: '#f1f5f9', borderRadius: 1.5, color: 'text.secondary' }}>
                      <Typography variant="caption">Chưa tải lên ảnh mặt sau</Typography>
                    </Box>
                  )}
                </Card>
              </Grid>
            </Grid>
          </Box>
        </DialogContent>

        <DialogActions sx={{ p: 2, bgcolor: '#f8fafc', borderTop: '1px solid #e2e8f0', justifyContent: 'space-between', flexWrap: 'wrap', gap: 1.5 }}>
          {selectedDriverForVerify?.approvalStatus === 'APPROVED' ? (
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, color: '#15803d', bgcolor: '#f0fdf4', border: '1px solid #bbf7d0', px: 2, py: 0.7, borderRadius: 2 }}>
              <ApproveIcon sx={{ fontSize: 18, color: '#16a34a' }} />
              <Typography variant="body2" sx={{ fontWeight: 700, fontSize: '0.82rem' }}>
                Hồ sơ đối tác đã được thẩm định & phê duyệt thành công
              </Typography>
            </Box>
          ) : (
            <Box />
          )}

          <Box sx={{ display: 'flex', gap: 1.2, ml: 'auto' }}>
            <Button onClick={handleCloseVerifyDialog} color="inherit" sx={{ fontWeight: 600 }}>
              Đóng
            </Button>

            <Button
              variant="outlined"
              color="error"
              startIcon={<RejectIcon />}
              onClick={() => {
                handleOpenRejectDialog(selectedDriverForVerify);
              }}
              sx={{ fontWeight: 700, borderRadius: 2 }}
            >
              {selectedDriverForVerify?.approvalStatus === 'APPROVED' ? 'Thu Hồi / Từ Chối' : 'Từ Chối Hồ Sơ'}
            </Button>

            {selectedDriverForVerify?.approvalStatus !== 'APPROVED' && (
              <Button
                variant="contained"
                startIcon={<ApproveIcon />}
                onClick={() => {
                  handleApproveDriver(selectedDriverForVerify);
                }}
                sx={{
                  bgcolor: '#15ca20',
                  '&:hover': { bgcolor: '#12b01c' },
                  fontWeight: 700,
                  borderRadius: 2,
                }}
              >
                Phê Duyệt Hồ Sơ
              </Button>
            )}
          </Box>
        </DialogActions>
      </Dialog>

      {/* DIALOG: Phóng to hình ảnh gốc (Lightbox) */}
      <Dialog
        open={openZoomDialog}
        onClose={handleCloseZoom}
        maxWidth="lg"
        PaperProps={{
          sx: { bgcolor: '#0f172a', color: 'white', borderRadius: 2.5, overflow: 'hidden' },
        }}
      >
        <DialogTitle sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', py: 1.5, px: 2.5, borderBottom: '1px solid rgba(255,255,255,0.1)' }}>
          <Typography variant="subtitle1" sx={{ fontWeight: 700, color: '#f8fafc' }}>
            {zoomImageTitle}
          </Typography>
          <IconButton onClick={handleCloseZoom} size="small" sx={{ color: '#94a3b8', '&:hover': { color: '#ffffff' } }}>
            <CloseIcon fontSize="small" />
          </IconButton>
        </DialogTitle>
        <DialogContent sx={{ p: 2, display: 'flex', justifyContent: 'center', alignItems: 'center', bgcolor: '#020617' }}>
          {zoomImageUrl && (
            <Box
              component="img"
              src={zoomImageUrl}
              alt={zoomImageTitle}
              sx={{
                maxWidth: '100%',
                maxHeight: '78vh',
                objectFit: 'contain',
                borderRadius: 1,
                boxShadow: '0 10px 25px rgba(0,0,0,0.5)',
              }}
            />
          )}
        </DialogContent>
      </Dialog>
    </Box>
  );
};

export default Drivers;
