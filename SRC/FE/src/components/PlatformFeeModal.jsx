import React, { useEffect, useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Typography,
  TextField,
  Button,
  Alert,
  Stack,
  Box,
  IconButton,
  Divider,
  InputAdornment,
  CircularProgress,
  Chip,
} from '@mui/material';
import {
  Percent as PercentIcon,
  Close as CloseIcon,
  Refresh as RefreshIcon,
  Save as SaveIcon,
} from '@mui/icons-material';
import paymentService from '../services/paymentService';
import toast from 'react-hot-toast';

export default function PlatformFeeModal({ open, onClose }) {
  const [percentage, setPercentage] = useState('');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  const load = async () => {
    setBusy(true);
    setError('');
    setSuccess(false);
    try {
      const data = await paymentService.getPlatformFee();
      setPercentage(String(data.percentage));
    } catch {
      setError('Không thể tải tỷ lệ sàn. Vui lòng thử lại.');
    } finally {
      setBusy(false);
    }
  };

  useEffect(() => {
    if (open) {
      load();
    }
  }, [open]);

  const valid =
    percentage.trim() !== '' &&
    Number.isFinite(Number(percentage)) &&
    Number(percentage) >= 0 &&
    Number(percentage) <= 100 &&
    /^\d+(\.\d{1,2})?$/.test(percentage);

  const save = async () => {
    setBusy(true);
    setError('');
    setSuccess(false);
    try {
      const data = await paymentService.updatePlatformFee(Number(percentage));
      setPercentage(String(data.percentage));
      setSuccess(true);
      toast.success('Đã lưu cấu hình tỷ lệ sàn thành công!');
      setTimeout(() => {
        if (onClose) onClose();
      }, 700);
    } catch {
      setError('Không thể lưu tỷ lệ sàn. Vui lòng thử lại.');
      toast.error('Lỗi khi lưu tỷ lệ sàn');
    } finally {
      setBusy(false);
    }
  };

  return (
    <Dialog
      open={Boolean(open)}
      onClose={busy ? undefined : onClose}
      maxWidth="sm"
      fullWidth
      PaperProps={{
        sx: {
          borderRadius: 3,
          m: { xs: 1.5, sm: 2 },
          boxShadow: '0 20px 45px rgba(0,0,0,0.18)',
        },
      }}
    >
      <DialogTitle sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', pb: 1.5 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
          <Box
            sx={{
              width: 42,
              height: 42,
              borderRadius: 2.5,
              bgcolor: 'rgba(249, 115, 22, 0.12)',
              color: '#ea580c',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              flexShrink: 0,
            }}
          >
            <PercentIcon sx={{ fontSize: 24 }} />
          </Box>
          <Box>
            <Typography variant="h6" sx={{ fontWeight: 800, fontSize: '1.1rem', color: '#0F172A' }}>
              Cấu Hình Tỷ Lệ Sàn
            </Typography>
            <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 500 }}>
              Quy định tỷ lệ hoa hồng khấu trừ trên chuyến xe và đơn giao đồ ăn
            </Typography>
          </Box>
        </Box>
        <IconButton size="small" onClick={onClose} disabled={busy}>
          <CloseIcon fontSize="small" />
        </IconButton>
      </DialogTitle>

      <Divider />

      <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2.2, pt: 2.5 }}>
        <Alert severity="info" sx={{ borderRadius: 2, '& .MuiAlert-message': { fontSize: '0.85rem', lineHeight: 1.5 } }}>
          Tỷ lệ dùng chung cho <b>tiền cuốc xe</b>, <b>tiền ship đồ ăn của tài xế</b> và <b>tiền món ăn của quán</b>.
          Phí được tự động tính và khấu trừ khi quyết toán đơn hàng; các đơn đã quyết toán trước đó sẽ giữ nguyên.
        </Alert>

        {error && <Alert severity="error" sx={{ borderRadius: 2 }}>{error}</Alert>}
        {success && <Alert severity="success" sx={{ borderRadius: 2 }}>Đã lưu tỷ lệ sàn thành công!</Alert>}

        <Box>
          <Typography variant="body2" sx={{ fontWeight: 700, mb: 1, color: '#334155' }}>
            Tỷ lệ chiết khấu sàn (%)
          </Typography>
          <TextField
            fullWidth
            type="number"
            value={percentage}
            disabled={busy}
            onChange={(e) => {
              setPercentage(e.target.value);
              setSuccess(false);
              setError('');
            }}
            slotProps={{
              htmlInput: { min: 0, max: 100, step: 0.1 },
              input: {
                endAdornment: (
                  <InputAdornment position="end">
                    <Typography sx={{ fontWeight: 800, color: '#EA580C', fontSize: '1rem' }}>%</Typography>
                  </InputAdornment>
                ),
              },
            }}
            helperText="Nhập từ 0 đến 100%, tối đa 2 chữ số thập phân (VD: 20 hoặc 15.5)."
          />
        </Box>

        {/* Quick select presets */}
        <Box>
          <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600, display: 'block', mb: 1 }}>
            Mức đề xuất nhanh:
          </Typography>
          <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap>
            {['10', '15', '20', '25', '30'].map((preset) => (
              <Chip
                key={preset}
                label={`${preset}%${preset === '20' ? ' (Mặc định)' : ''}`}
                size="small"
                clickable
                color={percentage === preset ? 'warning' : 'default'}
                variant={percentage === preset ? 'filled' : 'outlined'}
                onClick={() => {
                  setPercentage(preset);
                  setSuccess(false);
                  setError('');
                }}
                disabled={busy}
                sx={{ fontWeight: 700, borderRadius: 2 }}
              />
            ))}
          </Stack>
        </Box>
      </DialogContent>

      <Divider />

      <DialogActions sx={{ px: 3, py: 2, display: 'flex', justifyContent: 'space-between' }}>
        <Button
          variant="outlined"
          color="inherit"
          startIcon={<RefreshIcon />}
          disabled={busy}
          onClick={load}
          sx={{ borderRadius: 2, textTransform: 'none', fontWeight: 600 }}
        >
          {busy ? <CircularProgress size={16} sx={{ mr: 1 }} /> : null}
          Tải lại
        </Button>

        <Stack direction="row" spacing={1.5}>
          <Button
            variant="outlined"
            onClick={onClose}
            disabled={busy}
            sx={{ borderRadius: 2, textTransform: 'none', fontWeight: 600, color: '#64748B', borderColor: '#CBD5E1' }}
          >
            Đóng
          </Button>
          <Button
            variant="contained"
            disabled={busy || !valid}
            onClick={save}
            startIcon={busy ? <CircularProgress size={18} color="inherit" /> : <SaveIcon />}
            sx={{
              borderRadius: 2,
              bgcolor: '#F97316',
              '&:hover': { bgcolor: '#EA580C' },
              textTransform: 'none',
              fontWeight: 700,
              px: 2.5,
            }}
          >
            {busy ? 'Đang lưu...' : 'Lưu tỷ lệ sàn'}
          </Button>
        </Stack>
      </DialogActions>
    </Dialog>
  );
}

