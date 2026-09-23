import React from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Typography,
  Button,
  Box,
  CircularProgress,
} from '@mui/material';
import WarningIcon from '@mui/icons-material/WarningAmberRounded';
import DeleteIcon from '@mui/icons-material/Delete';
import QuestionIcon from '@mui/icons-material/HelpOutlineRounded';
import LogoutIcon from '@mui/icons-material/LogoutRounded';

export const ConfirmDialog = ({
  open,
  title = 'Xác nhận xóa',
  content = 'Bạn có chắc chắn muốn thực hiện thao tác này? Thao tác không thể hoàn tác.',
  confirmText = 'Xóa',
  cancelText = 'Hủy',
  confirmColor = 'error',
  loading = false,
  onConfirm,
  onClose,
  iconType = 'delete',
}) => {
  const getIcon = () => {
    if (iconType === 'logout') {
      return (
        <Box
          sx={{
            width: 52,
            height: 52,
            borderRadius: '50%',
            bgcolor: 'rgba(239, 68, 68, 0.1)',
            color: '#ef4444',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            mb: 1.5,
          }}
        >
          <LogoutIcon sx={{ fontSize: 28 }} />
        </Box>
      );
    }
    if (iconType === 'delete') {
      return (
        <Box
          sx={{
            width: 52,
            height: 52,
            borderRadius: '50%',
            bgcolor: 'rgba(239, 68, 68, 0.1)',
            color: '#ef4444',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            mb: 1.5,
          }}
        >
          <DeleteIcon sx={{ fontSize: 28 }} />
        </Box>
      );
    }
    if (iconType === 'warning') {
      return (
        <Box
          sx={{
            width: 52,
            height: 52,
            borderRadius: '50%',
            bgcolor: 'rgba(245, 158, 11, 0.1)',
            color: '#f59e0b',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            mb: 1.5,
          }}
        >
          <WarningIcon sx={{ fontSize: 28 }} />
        </Box>
      );
    }
    return (
      <Box
        sx={{
          width: 52,
          height: 52,
          borderRadius: '50%',
          bgcolor: 'rgba(0, 140, 255, 0.1)',
          color: '#008cff',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          mb: 1.5,
        }}
      >
        <QuestionIcon sx={{ fontSize: 28 }} />
      </Box>
    );
  };

  return (
    <Dialog
      open={open}
      onClose={loading ? undefined : onClose}
      maxWidth="xs"
      fullWidth
      PaperProps={{
        sx: {
          borderRadius: 3.5,
          p: 1,
          textAlign: 'center',
        },
      }}
    >
      <DialogTitle component="div" sx={{ pt: 2, pb: 0.5, display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
        {getIcon()}
        <Typography variant="h6" sx={{ fontWeight: 800, color: 'text.primary' }}>
          {title}
        </Typography>
      </DialogTitle>

      <DialogContent sx={{ py: 1.5 }}>
        <Typography variant="body2" sx={{ color: 'text.secondary', lineHeight: 1.6 }}>
          {content}
        </Typography>
      </DialogContent>

      <DialogActions sx={{ justifyContent: 'center', gap: 1.5, pb: 2, px: 2 }}>
        <Button
          variant="outlined"
          onClick={onClose}
          disabled={loading}
          sx={{
            minWidth: 100,
            borderRadius: 2,
            textTransform: 'none',
            fontWeight: 700,
            color: 'text.secondary',
            borderColor: 'divider',
          }}
        >
          {cancelText}
        </Button>
        <Button
          variant="contained"
          color={confirmColor}
          onClick={onConfirm}
          disabled={loading}
          sx={{
            minWidth: 100,
            borderRadius: 2,
            textTransform: 'none',
            fontWeight: 700,
            boxShadow: 'none',
          }}
        >
          {loading ? <CircularProgress size={18} color="inherit" /> : confirmText}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default ConfirmDialog;
