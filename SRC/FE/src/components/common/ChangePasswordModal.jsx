import React, { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Button,
  IconButton,
  InputAdornment,
  Typography,
  Box,
  Alert,
  CircularProgress,
} from '@mui/material';
import {
  LockReset as LockResetIcon,
  Visibility,
  VisibilityOff,
  Close as CloseIcon,
  Key as KeyIcon,
  CheckCircle as CheckIcon,
} from '@mui/icons-material';
import authService from '../../services/authService';
import toast from 'react-hot-toast';

export const ChangePasswordModal = ({ open, onClose, user }) => {
  const [oldPassword, setOldPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  const [showOldPassword, setShowOldPassword] = useState(false);
  const [showNewPassword, setShowNewPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const [loading, setLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');

  const resetForm = () => {
    setOldPassword('');
    setNewPassword('');
    setConfirmPassword('');
    setShowOldPassword(false);
    setShowNewPassword(false);
    setShowConfirmPassword(false);
    setErrorMsg('');
  };

  const handleClose = () => {
    if (loading) return;
    resetForm();
    onClose();
  };

  const handleSubmit = async (e) => {
    if (e) e.preventDefault();
    setErrorMsg('');

    if (!oldPassword) {
      setErrorMsg('Vui lòng nhập mật khẩu hiện tại.');
      return;
    }
    if (!newPassword) {
      setErrorMsg('Vui lòng nhập mật khẩu mới.');
      return;
    }
    if (newPassword.length < 6) {
      setErrorMsg('Mật khẩu mới phải có ít nhất 6 ký tự.');
      return;
    }
    if (newPassword === oldPassword) {
      setErrorMsg('Mật khẩu mới không được trùng với mật khẩu hiện tại.');
      return;
    }
    if (!confirmPassword) {
      setErrorMsg('Vui lòng xác nhận mật khẩu mới.');
      return;
    }
    if (newPassword !== confirmPassword) {
      setErrorMsg('Xác nhận mật khẩu mới không trùng khớp.');
      return;
    }

    try {
      setLoading(true);
      const res = await authService.changePassword({
        oldPassword,
        newPassword,
        confirmPassword,
      });

      toast.success(res?.message || 'Đổi mật khẩu thành công! Vui lòng ghi nhớ mật khẩu mới.');
      handleClose();
    } catch (err) {
      const msg = err.response?.data?.message || err.response?.data?.error || err.message || 'Lỗi khi đổi mật khẩu.';
      setErrorMsg(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog
      open={open}
      onClose={handleClose}
      maxWidth="xs"
      fullWidth
      PaperProps={{
        sx: {
          borderRadius: 3.5,
          p: 1,
          boxShadow: '0 20px 40px rgba(0,0,0,0.2)',
        },
      }}
    >
      <DialogTitle sx={{ m: 0, p: 2, display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.2 }}>
          <Box
            sx={{
              width: 40,
              height: 40,
              borderRadius: 2.5,
              bgcolor: (theme) => (theme.palette.mode === 'dark' ? 'rgba(249, 115, 22, 0.2)' : '#FFF7ED'),
              color: '#F97316',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <LockResetIcon />
          </Box>
          <Box>
            <Typography variant="subtitle1" sx={{ fontWeight: 800, lineHeight: 1.2 }}>
              Đổi mật khẩu
            </Typography>
            <Typography variant="caption" sx={{ color: 'text.secondary', fontSize: '0.75rem' }}>
              {user?.fullName ? `${user.fullName} (${user?.phoneNumber || ''})` : user?.phoneNumber || 'Tài khoản hiện tại'}
            </Typography>
          </Box>
        </Box>
        <IconButton
          onClick={handleClose}
          disabled={loading}
          size="small"
          sx={{ color: 'text.secondary' }}
        >
          <CloseIcon fontSize="small" />
        </IconButton>
      </DialogTitle>

      <form onSubmit={handleSubmit}>
        <DialogContent sx={{ px: 2, py: 1.5 }}>
          {errorMsg && (
            <Alert severity="error" sx={{ mb: 2, borderRadius: 2, fontSize: '0.82rem' }}>
              {errorMsg}
            </Alert>
          )}

          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            {/* Current Password */}
            <TextField
              label="Mật khẩu hiện tại"
              type={showOldPassword ? 'text' : 'password'}
              value={oldPassword}
              onChange={(e) => {
                setOldPassword(e.target.value);
                if (errorMsg) setErrorMsg('');
              }}
              fullWidth
              size="small"
              required
              autoFocus
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <KeyIcon fontSize="small" sx={{ color: 'text.secondary' }} />
                  </InputAdornment>
                ),
                endAdornment: (
                  <InputAdornment position="end">
                    <IconButton
                      size="small"
                      onClick={() => setShowOldPassword((prev) => !prev)}
                      edge="end"
                    >
                      {showOldPassword ? <VisibilityOff fontSize="small" /> : <Visibility fontSize="small" />}
                    </IconButton>
                  </InputAdornment>
                ),
              }}
            />

            {/* New Password */}
            <TextField
              label="Mật khẩu mới"
              type={showNewPassword ? 'text' : 'password'}
              value={newPassword}
              onChange={(e) => {
                setNewPassword(e.target.value);
                if (errorMsg) setErrorMsg('');
              }}
              fullWidth
              size="small"
              required
              helperText="Tối thiểu 6 ký tự, khác mật khẩu hiện tại"
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <KeyIcon fontSize="small" sx={{ color: 'text.secondary' }} />
                  </InputAdornment>
                ),
                endAdornment: (
                  <InputAdornment position="end">
                    <IconButton
                      size="small"
                      onClick={() => setShowNewPassword((prev) => !prev)}
                      edge="end"
                    >
                      {showNewPassword ? <VisibilityOff fontSize="small" /> : <Visibility fontSize="small" />}
                    </IconButton>
                  </InputAdornment>
                ),
              }}
            />

            {/* Confirm New Password */}
            <TextField
              label="Xác nhận mật khẩu mới"
              type={showConfirmPassword ? 'text' : 'password'}
              value={confirmPassword}
              onChange={(e) => {
                setConfirmPassword(e.target.value);
                if (errorMsg) setErrorMsg('');
              }}
              fullWidth
              size="small"
              required
              error={Boolean(confirmPassword && newPassword !== confirmPassword)}
              helperText={
                confirmPassword && newPassword !== confirmPassword
                  ? 'Mật khẩu xác nhận không khớp'
                  : ''
              }
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <CheckIcon fontSize="small" sx={{ color: 'text.secondary' }} />
                  </InputAdornment>
                ),
                endAdornment: (
                  <InputAdornment position="end">
                    <IconButton
                      size="small"
                      onClick={() => setShowConfirmPassword((prev) => !prev)}
                      edge="end"
                    >
                      {showConfirmPassword ? <VisibilityOff fontSize="small" /> : <Visibility fontSize="small" />}
                    </IconButton>
                  </InputAdornment>
                ),
              }}
            />
          </Box>
        </DialogContent>

        <DialogActions sx={{ px: 2, pb: 2, pt: 1, gap: 1 }}>
          <Button
            onClick={handleClose}
            disabled={loading}
            color="inherit"
            sx={{ borderRadius: 2, textTransform: 'none', fontWeight: 600 }}
          >
            Hủy
          </Button>
          <Button
            type="submit"
            variant="contained"
            disabled={loading}
            sx={{
              borderRadius: 2,
              textTransform: 'none',
              fontWeight: 700,
              bgcolor: '#F97316',
              '&:hover': { bgcolor: '#EA580C' },
              px: 2.5,
            }}
          >
            {loading ? <CircularProgress size={22} color="inherit" /> : 'Cập nhật mật khẩu'}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
};

export default ChangePasswordModal;
