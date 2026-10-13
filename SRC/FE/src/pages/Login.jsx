import { useState } from 'react';
import {
  Box,
  Card,
  CardContent,
  TextField,
  Button,
  Typography,
  Alert,
  CircularProgress,
  InputAdornment,
  IconButton,
  Chip,
} from '@mui/material';
import {
  VisibilityOutlined as Visibility,
  VisibilityOffOutlined as VisibilityOff,
  LockOutlined as LockIcon,
  PhoneOutlined as PhoneIcon,
  Store as StoreIcon,
  Person as PersonIcon,
  Email as EmailIcon,
  AccessTime as TimeIcon,
  Close as CloseIcon,
  CheckCircle as CheckCircleIcon,
  ArrowBack as ArrowBackIcon,
  AdminPanelSettings as AdminIcon,
  Restaurant as RestaurantIcon,
} from '@mui/icons-material';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Tabs,
  Tab,
  Stack,
  Avatar,
} from '@mui/material';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate, useLocation } from 'react-router-dom';
import toast from 'react-hot-toast';
import { loginUser, clearAuthError } from '../redux/authSlice';
import foodService from '../services/foodService';
import AddressAutocomplete from '../components/common/AddressAutocomplete';
import ImageUploadField from '../components/common/ImageUploadField';

export const Login = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const location = useLocation();
  const { loading, error } = useSelector((state) => state.auth);

  const [phoneNumber, setPhoneNumber] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [phoneError, setPhoneError] = useState('');

  // Register Partner States
  const [openRegisterModal, setOpenRegisterModal] = useState(() => {
    return new URLSearchParams(location.search).get('register') === 'partner';
  });
  const [registerTab, setRegisterTab] = useState(0);
  const [registerSubmitting, setRegisterSubmitting] = useState(false);
  const [registerSuccessModal, setRegisterSuccessModal] = useState(false);
  const [registeredRestaurantInfo, setRegisteredRestaurantInfo] = useState(null);

  const [registerData, setRegisterData] = useState({
    ownerName: '',
    ownerPhone: '',
    email: '',
    password: '',
    name: '',
    phone: '',
    address: '',
    latitude: 21.033333,
    longitude: 105.789123,
    imageUrl: '',
    licenseImageUrl: '',
    openTime: '08:00',
    closeTime: '22:00',
  });

  const resetRegisterForm = () => {
    setRegisterData({
      ownerName: '',
      ownerPhone: '',
      email: '',
      password: '',
      name: '',
      phone: '',
      address: '',
      latitude: 21.033333,
      longitude: 105.789123,
      imageUrl: '',
      licenseImageUrl: '',
      openTime: '08:00',
      closeTime: '22:00',
    });
    setRegisterTab(0);
  };

  const handleRegisterInputChange = (e) => {
    const { name, value } = e.target;
    setRegisterData((prev) => ({ ...prev, [name]: value }));
  };

  const handleNextRegisterTab = () => {
    if (!registerData.ownerName.trim()) {
      toast.error('Vui lòng nhập họ tên chủ nhà hàng');
      return;
    }
    const cleanPhone = registerData.ownerPhone.replace(/\D/g, '').trim();
    if (!cleanPhone || cleanPhone.length !== 10) {
      toast.error('Số điện thoại đăng nhập của chủ quán phải gồm đúng 10 số');
      return;
    }
    if (!registerData.password || registerData.password.length < 6) {
      toast.error('Mật khẩu đăng nhập phải có ít nhất 6 ký tự');
      return;
    }
    setRegisterTab(1);
  };

  const handleSubmitRegister = async (e) => {
    if (e) e.preventDefault();
    if (!registerData.name.trim()) {
      toast.error('Vui lòng nhập tên nhà hàng / quán ăn');
      setRegisterTab(1);
      return;
    }
    if (!registerData.address.trim()) {
      toast.error('Vui lòng nhập hoặc chọn địa chỉ quán ăn');
      setRegisterTab(1);
      return;
    }
    if (!registerData.licenseImageUrl || !registerData.licenseImageUrl.trim()) {
      toast.error('Vui lòng tải lên ảnh Giấy phép kinh doanh của quán');
      setRegisterTab(1);
      return;
    }

    try {
      setRegisterSubmitting(true);
      const payload = {
        name: registerData.name.trim(),
        phone: registerData.phone.trim() || registerData.ownerPhone.trim(),
        address: registerData.address.trim(),
        latitude: parseFloat(registerData.latitude) || 21.033333,
        longitude: parseFloat(registerData.longitude) || 105.789123,
        imageUrl:
          registerData.imageUrl.trim() ||
          'https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=500',
        licenseImageUrl: registerData.licenseImageUrl.trim(),
        openTime: registerData.openTime || '08:00',
        closeTime: registerData.closeTime || '22:00',
        ownerName: registerData.ownerName.trim(),
        ownerPhone: registerData.ownerPhone.trim(),
        email: registerData.email.trim() || undefined,
        password: registerData.password,
      };

      await foodService.registerPartnerRestaurant(payload);

      setRegisteredRestaurantInfo({
        name: payload.name,
        phone: payload.ownerPhone,
      });
      setPhoneNumber(payload.ownerPhone);
      setOpenRegisterModal(false);
      setRegisterSuccessModal(true);
      resetRegisterForm();
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Lỗi khi đăng ký đối tác';
      toast.error(msg);
    } finally {
      setRegisterSubmitting(false);
    }
  };

  const from = location.state?.from?.pathname || '/';

  const handleSubmit = async (e) => {
    e.preventDefault();
    setPhoneError('');

    const cleanPhone = phoneNumber.trim();
    if (!cleanPhone) {
      setPhoneError('Vui lòng nhập số điện thoại');
      toast.error('Vui lòng nhập số điện thoại');
      return;
    }
    if (cleanPhone.length !== 10 || !/^(0[3|5|7|8|9])[0-9]{8}$/.test(cleanPhone)) {
      setPhoneError('Số điện thoại phải gồm đúng 10 chữ số (bắt đầu bằng 03, 05, 07, 08, 09)');
      toast.error('Số điện thoại phải gồm đúng 10 chữ số');
      return;
    }
    if (!password) {
      toast.error('Vui lòng nhập mật khẩu');
      return;
    }

    dispatch(clearAuthError());
    const loadingToast = toast.loading('Đang xác thực thông tin đăng nhập...');

    try {
      const resultAction = await dispatch(loginUser({ phoneNumber: cleanPhone, password }));

      if (loginUser.fulfilled.match(resultAction)) {
        toast.dismiss(loadingToast);
        const loggedUser = resultAction.payload.user;
        toast.success(`Đăng nhập thành công! Xin chào ${loggedUser.fullName || loggedUser.phoneNumber}`);
        if (loggedUser.role === 'RESTAURANT') {
          navigate('/merchant/orders', { replace: true });
        } else {
          navigate(from === '/' ? '/dashboard' : from, { replace: true });
        }
      } else {
        toast.dismiss(loadingToast);
        const errMsg = resultAction.payload || 'Đăng nhập thất bại';
        toast.error(errMsg);
      }
    } catch (err) {
      toast.dismiss(loadingToast);
      toast.error(err.message || 'Lỗi kết nối máy chủ');
    }
  };

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        bgcolor: 'background.default',
        p: { xs: 2, sm: 2.5 },
      }}
    >
      {/* Back to Landing Page Button */}
      <Box sx={{ maxWidth: 420, width: '100%', mb: 2 }}>
        <Button
          startIcon={<ArrowBackIcon />}
          onClick={() => navigate('/')}
          sx={{
            textTransform: 'none',
            fontWeight: 700,
            fontSize: '0.88rem',
            color: 'text.secondary',
            borderRadius: 2.5,
            px: 2,
            py: 0.9,
            bgcolor: (theme) => theme.palette.mode === 'dark' ? 'rgba(255,255,255,0.04)' : 'rgba(0,0,0,0.03)',
            border: `1px solid`,
            borderColor: 'divider',
            transition: 'all 0.2s ease',
            '&:hover': {
              color: 'text.primary',
              borderColor: '#f97316',
              bgcolor: (theme) => theme.palette.mode === 'dark' ? 'rgba(249, 115, 22, 0.08)' : 'rgba(249, 115, 22, 0.04)',
              transform: 'translateX(-3px)',
            },
          }}
        >
          Quay lại trang chủ OmniGo
        </Button>
      </Box>

      <Card
        sx={{
          maxWidth: 420,
          width: '100%',
          p: { xs: 2.5, sm: 4 },
          bgcolor: 'background.paper',
          border: 1,
          borderColor: 'divider',
          borderRadius: 3.5,
        }}
      >
        <CardContent sx={{ p: '0 !important' }}>
          {/* Header Title */}
          <Box sx={{ mb: 3.5, textAlign: 'center' }}>
            <Box
              sx={{
                width: 48,
                height: 48,
                borderRadius: 2.5,
                bgcolor: 'primary.main',
                color: 'primary.contrastText',
                display: 'inline-flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontWeight: 900,
                fontSize: '1.5rem',
                mb: 1.5,
              }}
            >
              O
            </Box>
            <Typography variant="h5" sx={{ fontWeight: 800, color: 'text.primary', mb: 0.5 }}>
              OmniGo & OmniFood Portal
            </Typography>
            <Typography variant="body1" sx={{ color: 'text.secondary', fontSize: '0.95rem' }}>
              Cổng Quản trị Viên & Đối Tác Nhà Hàng
            </Typography>
          </Box>

          {error && (
            <Alert
              severity="error"
              sx={{
                mb: 3,
                fontSize: '0.9rem',
                fontWeight: 500,
              }}
            >
              {error}
            </Alert>
          )}

          <Box component="form" onSubmit={handleSubmit} sx={{ display: 'flex', flexDirection: 'column', gap: 2.5 }}>
            <Box>
              <Typography variant="body2" sx={{ fontWeight: 700, color: 'text.primary', mb: 0.8, fontSize: '0.95rem' }}>
                Số điện thoại
              </Typography>
              <TextField
                placeholder="Nhập số điện thoại tài khoản admin"
                fullWidth
                value={phoneNumber}
                error={Boolean(phoneError)}
                helperText={phoneError}
                onChange={(e) => {
                  const val = e.target.value.replace(/\D/g, '').slice(0, 10);
                  setPhoneNumber(val);
                  setPhoneError('');
                }}
                required
                slotProps={{
                  input: {
                    startAdornment: (
                      <InputAdornment position="start">
                        <PhoneIcon sx={{ color: 'text.secondary' }} />
                      </InputAdornment>
                    ),
                  },
                }}
              />
            </Box>

            <Box>
              <Typography variant="body2" sx={{ fontWeight: 700, color: 'text.primary', mb: 0.8, fontSize: '0.95rem' }}>
                Mật khẩu
              </Typography>
              <TextField
                type={showPassword ? 'text' : 'password'}
                placeholder="Nhập mật khẩu"
                fullWidth
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                slotProps={{
                  input: {
                    startAdornment: (
                      <InputAdornment position="start">
                        <LockIcon sx={{ color: 'text.secondary' }} />
                      </InputAdornment>
                    ),
                    endAdornment: (
                      <InputAdornment position="end">
                        <IconButton
                          onClick={() => setShowPassword(!showPassword)}
                          edge="end"
                          type="button"
                          size="small"
                          sx={{ color: 'text.secondary' }}
                        >
                          {showPassword ? <VisibilityOff /> : <Visibility />}
                        </IconButton>
                      </InputAdornment>
                    ),
                  },
                }}
              />
            </Box>

            <Button
              type="submit"
              variant="contained"
              fullWidth
              disabled={loading}
              sx={{
                mt: 1.5,
                py: 1.4,
                fontSize: '1rem',
                fontWeight: 700,
              }}
            >
              {loading ? <CircularProgress size={24} sx={{ color: 'primary.contrastText' }} /> : 'Đăng Nhập'}
            </Button>
          </Box>

          {/* TÀI KHOẢN MẪU / QUICK-FILL DEMO ACCOUNTS */}
          <Box
            sx={{
              mt: 3,
              p: 2,
              borderRadius: 2.5,
              bgcolor: (theme) =>
                theme.palette.mode === 'dark'
                  ? 'rgba(255, 255, 255, 0.03)'
                  : 'rgba(0, 0, 0, 0.02)',
              border: '1px dashed',
              borderColor: 'divider',
            }}
          >
            <Typography
              variant="caption"
              sx={{
                fontWeight: 700,
                color: 'text.secondary',
                textTransform: 'uppercase',
                letterSpacing: 0.5,
                fontSize: '0.74rem',
                display: 'block',
                mb: 1.5,
              }}
            >
              ⚡ Tài khoản mẫu (Bấm để điền nhanh):
            </Typography>

            <Stack spacing={1.2}>
              {/* Account 1: ADMIN */}
              <Box
                onClick={() => {
                  setPhoneNumber('0923456789');
                  setPassword('123456');
                  setPhoneError('');
                  toast.success('Đã chọn tài khoản ADMIN (0923456789)');
                }}
                sx={{
                  p: 1.2,
                  borderRadius: 2,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  cursor: 'pointer',
                  bgcolor: (theme) =>
                    phoneNumber === '0923456789'
                      ? theme.palette.mode === 'dark'
                        ? 'rgba(59, 130, 246, 0.16)'
                        : 'rgba(59, 130, 246, 0.08)'
                      : theme.palette.mode === 'dark'
                        ? 'rgba(255, 255, 255, 0.02)'
                        : '#ffffff',
                  border: '1px solid',
                  borderColor: (theme) =>
                    phoneNumber === '0923456789'
                      ? '#3b82f6'
                      : theme.palette.mode === 'dark'
                        ? 'rgba(255, 255, 255, 0.08)'
                        : '#e2e8f0',
                  transition: 'all 0.2s ease',
                  '&:hover': {
                    borderColor: '#3b82f6',
                    bgcolor: (theme) =>
                      theme.palette.mode === 'dark'
                        ? 'rgba(59, 130, 246, 0.12)'
                        : 'rgba(59, 130, 246, 0.06)',
                    transform: 'translateY(-1px)',
                  },
                }}
              >
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.2 }}>
                  <Avatar
                    sx={{
                      width: 34,
                      height: 34,
                      bgcolor: 'rgba(59, 130, 246, 0.12)',
                      color: '#3b82f6',
                    }}
                  >
                    <AdminIcon sx={{ fontSize: 18 }} />
                  </Avatar>
                  <Box>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.8 }}>
                      <Typography variant="body2" sx={{ fontWeight: 700, color: 'text.primary', fontSize: '0.85rem' }}>
                        0923456789
                      </Typography>
                      <Chip
                        label="ADMIN"
                        size="small"
                        sx={{
                          height: 19,
                          fontSize: '0.65rem',
                          fontWeight: 800,
                          bgcolor: 'rgba(59, 130, 246, 0.12)',
                          color: '#3b82f6',
                        }}
                      />
                    </Box>
                    <Typography variant="caption" sx={{ color: 'text.secondary', fontSize: '0.72rem' }}>
                      Mật khẩu: <b>123456</b> (Quản trị hệ thống)
                    </Typography>
                  </Box>
                </Box>
                <Typography variant="caption" sx={{ color: '#3b82f6', fontWeight: 700, fontSize: '0.75rem' }}>
                  Chọn ➡
                </Typography>
              </Box>

              {/* Account 2: RESTAURANT */}
              <Box
                onClick={() => {
                  setPhoneNumber('0923456111');
                  setPassword('123456');
                  setPhoneError('');
                  toast.success('Đã chọn tài khoản RESTAURANT (0923456111)');
                }}
                sx={{
                  p: 1.2,
                  borderRadius: 2,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  cursor: 'pointer',
                  bgcolor: (theme) =>
                    phoneNumber === '0923456111'
                      ? theme.palette.mode === 'dark'
                        ? 'rgba(249, 115, 22, 0.16)'
                        : 'rgba(249, 115, 22, 0.08)'
                      : theme.palette.mode === 'dark'
                        ? 'rgba(255, 255, 255, 0.02)'
                        : '#ffffff',
                  border: '1px solid',
                  borderColor: (theme) =>
                    phoneNumber === '0923456111'
                      ? '#f97316'
                      : theme.palette.mode === 'dark'
                        ? 'rgba(255, 255, 255, 0.08)'
                        : '#e2e8f0',
                  transition: 'all 0.2s ease',
                  '&:hover': {
                    borderColor: '#f97316',
                    bgcolor: (theme) =>
                      theme.palette.mode === 'dark'
                        ? 'rgba(249, 115, 22, 0.12)'
                        : 'rgba(249, 115, 22, 0.06)',
                    transform: 'translateY(-1px)',
                  },
                }}
              >
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.2 }}>
                  <Avatar
                    sx={{
                      width: 34,
                      height: 34,
                      bgcolor: 'rgba(249, 115, 22, 0.12)',
                      color: '#f97316',
                    }}
                  >
                    <RestaurantIcon sx={{ fontSize: 18 }} />
                  </Avatar>
                  <Box>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.8 }}>
                      <Typography variant="body2" sx={{ fontWeight: 700, color: 'text.primary', fontSize: '0.85rem' }}>
                        0923456111
                      </Typography>
                      <Chip
                        label="RESTAURANT"
                        size="small"
                        sx={{
                          height: 19,
                          fontSize: '0.65rem',
                          fontWeight: 800,
                          bgcolor: 'rgba(249, 115, 22, 0.12)',
                          color: '#f97316',
                        }}
                      />
                    </Box>
                    <Typography variant="caption" sx={{ color: 'text.secondary', fontSize: '0.72rem' }}>
                      Mật khẩu: <b>123456</b> (Chủ quán ăn / Nhà hàng)
                    </Typography>
                  </Box>
                </Box>
                <Typography variant="caption" sx={{ color: '#f97316', fontWeight: 700, fontSize: '0.75rem' }}>
                  Chọn ➡
                </Typography>
              </Box>
            </Stack>
          </Box>

          {/* REGISTER AS PARTNER RESTAURANT LINK / BUTTON */}
          <Box sx={{ mt: 3, pt: 2, borderTop: '1px dashed', borderColor: 'divider', textAlign: 'center' }}>
            <Typography variant="body2" sx={{ color: 'text.secondary', mb: 1.5, fontSize: '0.88rem' }}>
              Bạn muốn mở quán bán đồ ăn trên OmniFood?
            </Typography>
            <Button
              fullWidth
              variant="outlined"
              color="warning"
              startIcon={<StoreIcon />}
              onClick={() => {
                resetRegisterForm();
                setOpenRegisterModal(true);
              }}
              sx={{
                py: 1.1,
                borderRadius: 2.5,
                fontWeight: 700,
                fontSize: '0.9rem',
                textTransform: 'none',
                borderColor: (theme) => (theme.palette.mode === 'dark' ? 'rgba(249, 115, 22, 0.3)' : '#FED7AA'),
                bgcolor: (theme) => (theme.palette.mode === 'dark' ? 'rgba(249, 115, 22, 0.08)' : '#FFF7ED'),
                color: '#EA580C',
                '&:hover': {
                  bgcolor: (theme) => (theme.palette.mode === 'dark' ? 'rgba(249, 115, 22, 0.16)' : '#FFEDD5'),
                  borderColor: '#FDBA74',
                },
              }}
            >
              Đăng ký đối tác nhà hàng ngay
            </Button>
            <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block', mt: 1, fontSize: '0.75rem' }}>
              * Quán đăng ký xong sẽ được Quản trị viên xét duyệt trước khi hoạt động
            </Typography>
          </Box>
        </CardContent>
      </Card>

      {/* DIALOG: ĐĂNG KÝ ĐỐI TÁC NHÀ HÀNG MỚI */}
      <Dialog
        open={openRegisterModal}
        onClose={() => !registerSubmitting && setOpenRegisterModal(false)}
        maxWidth="md"
        fullWidth
        PaperProps={{ sx: { borderRadius: 3.5, overflow: 'hidden' } }}
      >
        <DialogTitle
          component="div"
          sx={{
            fontWeight: 800,
            bgcolor: 'background.paper',
            borderBottom: 1,
            borderColor: 'divider',
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            py: 2,
          }}
        >
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
            <Avatar sx={{ bgcolor: 'rgba(249, 115, 22, 0.15)', color: '#F97316' }}>
              <StoreIcon />
            </Avatar>
            <Box>
              <Typography variant="h6" sx={{ fontWeight: 800, color: 'text.primary' }}>
                Đăng Ký Đối Tác Quán Ăn OmniFood
              </Typography>
              <Typography variant="caption" sx={{ color: 'text.secondary' }}>
                Gia nhập nền tảng bán đồ ăn, tiếp cận hàng triệu khách hàng
              </Typography>
            </Box>
          </Box>
          <IconButton
            onClick={() => setOpenRegisterModal(false)}
            disabled={registerSubmitting}
            size="small"
          >
            <CloseIcon />
          </IconButton>
        </DialogTitle>

        <DialogContent sx={{ p: 0 }}>
          <Box
            sx={{
              borderBottom: 1,
              borderColor: 'divider',
              px: 3,
              pt: 1.5,
              bgcolor: (theme) => (theme.palette.mode === 'dark' ? 'rgba(255, 255, 255, 0.02)' : '#F8FAFC'),
            }}
          >
            <Tabs
              value={registerTab}
              onChange={(e, val) => setRegisterTab(val)}
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
                icon={<StoreIcon sx={{ fontSize: 20 }} />}
                iconPosition="start"
                label="2. Thông Tin Nhà Hàng"
                sx={{ textTransform: 'none', fontWeight: 700 }}
              />
            </Tabs>
          </Box>

          <Box sx={{ p: 3 }}>
            {/* Tab 0: Owner Account */}
            {registerTab === 0 && (
              <Stack spacing={2.5}>
                <Alert severity="info" sx={{ borderRadius: 2, fontSize: '0.85rem' }}>
                  Thông tin tài khoản đăng nhập dành cho chủ nhà hàng để quản lý đơn món và thực đơn.
                </Alert>

                <Box>
                  <Typography variant="subtitle2" sx={{ fontWeight: 700, mb: 0.8 }}>
                    Họ và tên chủ nhà hàng <span style={{ color: '#ef4444' }}>*</span>
                  </Typography>
                  <TextField
                    fullWidth
                    required
                    name="ownerName"
                    value={registerData.ownerName}
                    onChange={handleRegisterInputChange}
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

                <Box sx={{ display: 'flex', gap: 2, flexDirection: { xs: 'column', sm: 'row' } }}>
                  <Box sx={{ flex: 1 }}>
                    <Typography variant="subtitle2" sx={{ fontWeight: 700, mb: 0.8 }}>
                      Số điện thoại đăng nhập <span style={{ color: '#ef4444' }}>*</span>
                    </Typography>
                    <TextField
                      fullWidth
                      required
                      name="ownerPhone"
                      value={registerData.ownerPhone}
                      onChange={(e) => {
                        const val = e.target.value.replace(/\D/g, '').slice(0, 10);
                        setRegisterData((prev) => ({ ...prev, ownerPhone: val }));
                      }}
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
                      value={registerData.email}
                      onChange={handleRegisterInputChange}
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

                <Box>
                  <Typography variant="subtitle2" sx={{ fontWeight: 700, mb: 0.8 }}>
                    Mật khẩu đăng nhập <span style={{ color: '#ef4444' }}>*</span>
                  </Typography>
                  <TextField
                    fullWidth
                    required
                    type="password"
                    name="password"
                    value={registerData.password}
                    onChange={handleRegisterInputChange}
                    placeholder="Tối thiểu 6 ký tự"
                    helperText="Mật khẩu này sẽ dùng để đăng nhập vào cổng Quản trị Nhà hàng"
                    InputProps={{
                      startAdornment: (
                        <InputAdornment position="start">
                          <LockIcon sx={{ color: 'text.secondary' }} />
                        </InputAdornment>
                      ),
                    }}
                  />
                </Box>
              </Stack>
            )}

            {/* Tab 1: Restaurant Info */}
            {registerTab === 1 && (
              <Stack spacing={2.5}>
                <Alert severity="warning" sx={{ borderRadius: 2, fontSize: '0.85rem' }}>
                  ⏳ <b>Lưu ý quan trọng:</b> Sau khi hoàn tất đăng ký, hồ sơ quán sẽ được gửi đến Quản trị viên để xét duyệt trước khi đi vào hoạt động chính thức.
                </Alert>

                <Box>
                  <Typography variant="subtitle2" sx={{ fontWeight: 700, mb: 0.8 }}>
                    Tên nhà hàng / quán ăn <span style={{ color: '#ef4444' }}>*</span>
                  </Typography>
                  <TextField
                    fullWidth
                    required
                    name="name"
                    value={registerData.name}
                    onChange={handleRegisterInputChange}
                    placeholder="VD: Cơm Tấm Sài Gòn - Cơ Sở 1"
                    InputProps={{
                      startAdornment: (
                        <InputAdornment position="start">
                          <StoreIcon sx={{ color: 'text.secondary' }} />
                        </InputAdornment>
                      ),
                    }}
                  />
                </Box>

                <Box>
                  <Typography variant="subtitle2" sx={{ fontWeight: 700, mb: 0.8 }}>
                    Số điện thoại hotline quán (tùy chọn)
                  </Typography>
                  <TextField
                    fullWidth
                    name="phone"
                    value={registerData.phone}
                    onChange={handleRegisterInputChange}
                    placeholder="VD: 0283899999 (mặc định lấy theo SĐT chủ quán)"
                    InputProps={{
                      startAdornment: (
                        <InputAdornment position="start">
                          <PhoneIcon sx={{ color: 'text.secondary' }} />
                        </InputAdornment>
                      ),
                    }}
                  />
                </Box>

                {/* Address & Coordinates with Autocomplete */}
                <Box>
                  <AddressAutocomplete
                    label="Địa chỉ chi tiết quán ăn"
                    required
                    value={registerData.address}
                    latitude={registerData.latitude}
                    longitude={registerData.longitude}
                    onChangeAddress={(newAddr) =>
                      setRegisterData((prev) => ({ ...prev, address: newAddr }))
                    }
                    onSelectLocation={({ address, latitude, longitude }) => {
                      setRegisterData((prev) => ({
                        ...prev,
                        address,
                        latitude,
                        longitude,
                      }));
                    }}
                    onChangeCoordinates={({ latitude, longitude }) => {
                      setRegisterData((prev) => ({
                        ...prev,
                        latitude,
                        longitude,
                      }));
                    }}
                  />
                </Box>

                <Box sx={{ display: 'flex', gap: 2, flexDirection: { xs: 'column', sm: 'row' } }}>
                  <Box sx={{ flex: 1 }}>
                    <Typography variant="subtitle2" sx={{ fontWeight: 700, mb: 0.8 }}>
                      Giờ mở cửa
                    </Typography>
                    <TextField
                      fullWidth
                      name="openTime"
                      value={registerData.openTime}
                      onChange={handleRegisterInputChange}
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
                      value={registerData.closeTime}
                      onChange={handleRegisterInputChange}
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

                {/* Upload Ảnh đại diện quán */}
                <ImageUploadField
                  label="Ảnh đại diện quán ăn"
                  value={registerData.imageUrl}
                  onChange={(url) =>
                    setRegisterData((prev) => ({ ...prev, imageUrl: url }))
                  }
                  helperText="Tải lên ảnh đại diện quán ăn hoặc dán link ảnh"
                  placeholder="Dán liên kết ảnh quán (URL)..."
                />

                {/* Upload Giấy phép kinh doanh */}
                <ImageUploadField
                  label="Ảnh giấy phép kinh doanh"
                  required
                  value={registerData.licenseImageUrl}
                  onChange={(url) =>
                    setRegisterData((prev) => ({ ...prev, licenseImageUrl: url }))
                  }
                  helperText="Tải lên ảnh chụp rõ nét Giấy phép kinh doanh hoặc Giấy chứng nhận đăng ký hộ kinh doanh để ban quản trị duyệt quán"
                />
              </Stack>
            )}
          </Box>
        </DialogContent>

        <DialogActions sx={{ p: 2.5, bgcolor: 'background.paper', borderTop: 1, borderColor: 'divider' }}>
          <Button
            onClick={() => setOpenRegisterModal(false)}
            disabled={registerSubmitting}
            sx={{ textTransform: 'none', color: 'text.secondary' }}
          >
            Hủy
          </Button>

          {registerTab === 0 ? (
            <Button
              variant="contained"
              onClick={handleNextRegisterTab}
              sx={{
                bgcolor: '#F97316',
                '&:hover': { bgcolor: '#EA580C' },
                textTransform: 'none',
                fontWeight: 700,
                px: 3,
              }}
            >
              Tiếp tục: Thông tin quán ➡
            </Button>
          ) : (
            <Box sx={{ display: 'flex', gap: 1.5 }}>
              <Button
                onClick={() => setRegisterTab(0)}
                disabled={registerSubmitting}
                sx={{ textTransform: 'none' }}
              >
                ⬅ Quay lại bước 1
              </Button>
              <Button
                variant="contained"
                onClick={handleSubmitRegister}
                disabled={registerSubmitting}
                sx={{
                  bgcolor: '#10b981',
                  '&:hover': { bgcolor: '#059669' },
                  textTransform: 'none',
                  fontWeight: 700,
                  px: 3,
                }}
              >
                {registerSubmitting ? (
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <CircularProgress size={18} color="inherit" />
                    <span>Đang gửi hồ sơ...</span>
                  </Box>
                ) : (
                  'Hoàn Tất & Gửi Duyệt'
                )}
              </Button>
            </Box>
          )}
        </DialogActions>
      </Dialog>

      {/* MODAL THÔNG BÁO ĐĂNG KÝ THÀNH CÔNG VÀ CHỜ DUYỆT */}
      <Dialog
        open={registerSuccessModal}
        onClose={() => setRegisterSuccessModal(false)}
        maxWidth="xs"
        fullWidth
        PaperProps={{ sx: { borderRadius: 3.5, p: 2, textAlign: 'center' } }}
      >
        <DialogTitle sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', pb: 1 }}>
          <Box
            sx={{
              width: 56,
              height: 56,
              borderRadius: '50%',
              bgcolor: 'rgba(16, 185, 129, 0.12)',
              color: '#10b981',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              mb: 1.5,
            }}
          >
            <CheckCircleIcon sx={{ fontSize: 36 }} />
          </Box>
          <Typography variant="h6" sx={{ fontWeight: 800, color: 'text.primary' }}>
            Đăng Ký Đối Tác Thành Công!
          </Typography>
        </DialogTitle>
        <DialogContent sx={{ py: 1 }}>
          <Typography variant="body2" sx={{ color: 'text.secondary', lineHeight: 1.6, mb: 2 }}>
            Hồ sơ nhà hàng <b>{registeredRestaurantInfo?.name}</b> đã được tạo thành công trên hệ thống.
          </Typography>
          <Alert severity="warning" sx={{ textAlign: 'left', borderRadius: 2.5, mb: 1, fontSize: '0.85rem' }}>
            ⏳ <b>Chờ Admin phê duyệt:</b> Nhà hàng đang ở trạng thái chờ Quản trị viên duyệt. Sau khi được duyệt, quán sẽ tự động được kích hoạt để mở bán.
          </Alert>
          <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block', mt: 1 }}>
            Bạn có thể đăng nhập bằng SĐT <b>{registeredRestaurantInfo?.phone}</b> để theo dõi trạng thái hồ sơ quán.
          </Typography>
        </DialogContent>
        <DialogActions sx={{ justifyContent: 'center', pt: 1, pb: 1 }}>
          <Button
            variant="contained"
            fullWidth
            onClick={() => setRegisterSuccessModal(false)}
            sx={{
              bgcolor: '#F97316',
              '&:hover': { bgcolor: '#EA580C' },
              fontWeight: 700,
              textTransform: 'none',
              borderRadius: 2.5,
              py: 1,
            }}
          >
            Đăng nhập ngay
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default Login;
