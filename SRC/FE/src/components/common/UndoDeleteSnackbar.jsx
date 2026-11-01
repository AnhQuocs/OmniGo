import React from 'react';
import {
  Box,
  Paper,
  Typography,
  Button,
  CircularProgress,
  IconButton,
  Slide,
} from '@mui/material';
import {
  Undo as UndoIcon,
  Close as CloseIcon,
} from '@mui/icons-material';

export const UndoDeleteSnackbar = ({
  open,
  itemName = 'mục đã chọn',
  itemType = 'thẻ',
  secondsLeft = 5,
  totalSeconds = 5,
  onUndo,
  onDismiss,
}) => {
  if (!open) return null;

  // Percentage for countdown progress (0% when done, 100% at start)
  const progressPercent = Math.max(0, Math.min(100, (secondsLeft / totalSeconds) * 100));

  return (
    <Slide in={open} direction="up" mountOnEnter unmountOnExit>
      <Paper
        elevation={8}
        sx={{
          position: 'fixed',
          bottom: { xs: 20, sm: 30 },
          left: '50%',
          transform: 'translateX(-50%) !important',
          zIndex: 9999,
          display: 'flex',
          alignItems: 'center',
          gap: { xs: 1.5, sm: 2 },
          px: { xs: 2, sm: 2.5 },
          py: 1.5,
          borderRadius: 3.5,
          bgcolor: '#0F172A',
          color: '#F8FAFC',
          border: '1px solid rgba(255, 255, 255, 0.12)',
          boxShadow: '0 16px 36px rgba(0, 0, 0, 0.45)',
          maxWidth: { xs: '92vw', sm: 540 },
          width: 'max-content',
        }}
      >
        {/* Countdown Ring */}
        <Box sx={{ position: 'relative', display: 'inline-flex', alignItems: 'center', justifyContent: 'center' }}>
          <CircularProgress
            variant="determinate"
            value={progressPercent}
            size={36}
            thickness={4.5}
            sx={{
              color: '#F59E0B',
              transition: 'all 1s linear',
            }}
          />
          <Typography
            variant="caption"
            sx={{
              position: 'absolute',
              fontWeight: 800,
              fontSize: '0.75rem',
              color: '#FDE047',
            }}
          >
            {secondsLeft}s
          </Typography>
        </Box>

        {/* Message */}
        <Box sx={{ minWidth: { xs: 140, sm: 220 } }}>
          <Typography
            variant="body2"
            sx={{
              fontWeight: 600,
              fontSize: { xs: '0.82rem', sm: '0.88rem' },
              lineHeight: 1.3,
              color: '#F8FAFC',
            }}
          >
            Đã ẩn {itemType} <b>"{itemName}"</b>
          </Typography>
          <Typography
            variant="caption"
            sx={{
              color: '#94A3B8',
              fontSize: '0.74rem',
              display: 'block',
            }}
          >
            Sẽ xóa vĩnh viễn sau {secondsLeft} giây nếu không hoàn tác
          </Typography>
        </Box>

        {/* Undo Button */}
        <Button
          variant="contained"
          size="small"
          onClick={onUndo}
          startIcon={<UndoIcon />}
          sx={{
            bgcolor: '#F59E0B',
            color: '#0F172A',
            fontWeight: 800,
            textTransform: 'none',
            fontSize: { xs: '0.78rem', sm: '0.84rem' },
            borderRadius: 2.2,
            px: { xs: 1.5, sm: 2 },
            py: 0.6,
            '&:hover': {
              bgcolor: '#D97706',
              color: '#FFFFFF',
            },
            whiteSpace: 'nowrap',
          }}
        >
          Hoàn tác
        </Button>

        {/* Optional quick dismiss (commit) */}
        {onDismiss && (
          <IconButton
            size="small"
            onClick={onDismiss}
            sx={{ color: '#94A3B8', '&:hover': { color: '#F8FAFC' }, p: 0.5 }}
            title="Xóa ngay"
          >
            <CloseIcon fontSize="small" />
          </IconButton>
        )}
      </Paper>
    </Slide>
  );
};

export default UndoDeleteSnackbar;
