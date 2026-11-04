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
  progressPercent,
  onUndo,
  onDismiss,
}) => {
  if (!open) return null;

  // Percentage for countdown progress (0% when done, 100% at start)
  const computedProgress = progressPercent !== undefined
    ? Math.max(0, Math.min(100, progressPercent))
    : Math.max(0, Math.min(100, (secondsLeft / totalSeconds) * 100));

  return (
    <Slide in={open} direction="up" mountOnEnter unmountOnExit>
      <Paper
        elevation={6}
        sx={{
          position: 'fixed',
          bottom: { xs: 24, sm: 32 },
          left: '50%',
          transform: 'translateX(-50%) !important',
          zIndex: 9999,
          display: 'flex',
          alignItems: 'center',
          gap: { xs: 1.5, sm: 2.2 },
          px: { xs: 2, sm: 2.5 },
          py: 1.4,
          borderRadius: 4,
          bgcolor: '#FFFFFF',
          color: '#0F172A',
          border: '1px solid #E2E8F0',
          boxShadow: '0 20px 45px rgba(15, 23, 42, 0.14), 0 4px 14px rgba(0, 0, 0, 0.05)',
          maxWidth: { xs: '92vw', sm: 540 },
          width: 'max-content',
        }}
      >
        {/* Countdown Ring with Smooth Continuous Progress */}
        <Box sx={{ position: 'relative', display: 'inline-flex', alignItems: 'center', justifyContent: 'center' }}>
          {/* Subtle Background Track */}
          <CircularProgress
            variant="determinate"
            value={100}
            size={38}
            thickness={4.5}
            sx={{
              color: '#F1F5F9',
            }}
          />
          {/* Smooth Animated Ring */}
          <CircularProgress
            variant="determinate"
            value={computedProgress}
            size={38}
            thickness={4.5}
            sx={{
              color: '#F97316',
              position: 'absolute',
              left: 0,
              '& .MuiCircularProgress-circle': {
                strokeLinecap: 'round',
                transition: 'stroke-dashoffset 40ms linear',
              },
            }}
          />
          <Typography
            variant="caption"
            sx={{
              position: 'absolute',
              fontWeight: 800,
              fontSize: '0.78rem',
              color: '#EA580C',
              userSelect: 'none',
            }}
          >
            {secondsLeft}s
          </Typography>
        </Box>

        {/* Message Content */}
        <Box sx={{ minWidth: { xs: 140, sm: 220 } }}>
          <Typography
            variant="body2"
            sx={{
              fontWeight: 700,
              fontSize: { xs: '0.84rem', sm: '0.9rem' },
              lineHeight: 1.3,
              color: '#0F172A',
            }}
          >
            Đã ẩn {itemType} <b>"{itemName}"</b>
          </Typography>
          <Typography
            variant="caption"
            sx={{
              color: '#64748B',
              fontSize: '0.75rem',
              display: 'block',
              mt: 0.2,
            }}
          >
            Sẽ xóa vĩnh viễn sau {secondsLeft} giây nếu không hoàn tác
          </Typography>
        </Box>

        {/* Undo Action Button */}
        <Button
          variant="contained"
          size="small"
          onClick={onUndo}
          startIcon={<UndoIcon sx={{ fontSize: 18 }} />}
          sx={{
            bgcolor: '#F97316',
            color: '#FFFFFF',
            fontWeight: 800,
            textTransform: 'none',
            fontSize: { xs: '0.82rem', sm: '0.88rem' },
            borderRadius: 2.5,
            px: { xs: 1.8, sm: 2.4 },
            py: 0.7,
            boxShadow: '0 3px 10px rgba(249, 115, 22, 0.35)',
            '&:hover': {
              bgcolor: '#EA580C',
              boxShadow: '0 4px 14px rgba(234, 88, 12, 0.45)',
            },
            whiteSpace: 'nowrap',
          }}
        >
          Hoàn tác
        </Button>

        {/* Dismiss Button */}
        <IconButton
          size="small"
          onClick={onDismiss}
          title="Xóa ngay"
          sx={{
            color: '#94A3B8',
            p: 0.6,
            borderRadius: 2,
            '&:hover': {
              color: '#334155',
              bgcolor: '#F1F5F9',
            },
          }}
        >
          <CloseIcon fontSize="small" />
        </IconButton>
      </Paper>
    </Slide>
  );
};

export default UndoDeleteSnackbar;
