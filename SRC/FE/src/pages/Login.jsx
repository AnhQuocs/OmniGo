import React, { useState } from 'react';
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
  Divider,
} from '@mui/material';
import {
  VisibilityOutlined as Visibility,
  VisibilityOffOutlined as VisibilityOff,
  LockOutlined as LockIcon,
  PhoneOutlined as PhoneIcon,
} from '@mui/icons-material';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate, useLocation } from 'react-router-dom';
import toast from 'react-hot-toast';
import { loginUser, clearAuthError } from '../redux/authSlice';
import { API_BASE_URL } from '../services/api';

export const Login = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const location = useLocation();
  const { loading, error } = useSelector((state) => state.auth);

  const [phoneNumber, setPhoneNumber] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);

  const from = location.state?.from?.pathname || '/';

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!phoneNumber.trim() || !password.trim()) {
      toast.error('Vui lòng nhập đầy đủ Số điện thoại và Mật khẩu');
      return;
    }

    dispatch(clearAuthError());
    const loadingToast = toast.loading('Đang xác thực thông tin đăng nhập...');

    try {
      const resultAction = await dispatch(loginUser({ phoneNumber, password }));

      if (loginUser.fulfilled.match(resultAction)) {
        toast.dismiss(loadingToast);
        toast.success(`Đăng nhập thành công! Xin chào ${resultAction.payload.user.fullName || resultAction.payload.user.phoneNumber}`);
        navigate(from, { replace: true });
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
        alignItems: 'center',
        justifyContent: 'center',
        bgcolor: 'background.default',
        p: 2.5,
      }}
    >
      <Card
        sx={{
          maxWidth: 420,
          width: '100%',
          p: { xs: 3, sm: 4 },
          bgcolor: 'background.paper',
          border: 1,
          borderColor: 'divider',
          borderRadius: 4,
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
              OmniGo Admin Portal
            </Typography>
            <Typography variant="body1" sx={{ color: 'text.secondary', fontSize: '0.95rem' }}>
              Hệ thống Quản trị Đa dịch vụ Gọi xe & Giao hàng
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
                onChange={(e) => setPhoneNumber(e.target.value)}
                required
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <PhoneIcon sx={{ color: 'text.secondary' }} />
                    </InputAdornment>
                  ),
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
                InputProps={{
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
                      >
                        {showPassword ? <VisibilityOff /> : <Visibility />}
                      </IconButton>
                    </InputAdornment>
                  ),
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

          <Divider sx={{ my: 3 }} />

          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600 }}>
              Cổng Gateway kết nối:
            </Typography>
            <Typography variant="caption" sx={{ color: 'primary.main', fontWeight: 700, fontFamily: 'monospace' }}>
              {API_BASE_URL}
            </Typography>
          </Box>
        </CardContent>
      </Card>
    </Box>
  );
};

export default Login;
