import React from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Rating,
  Typography,
  Box,
  IconButton,
  Alert,
  Avatar,
  Chip,
  Divider,
  Stack,
  Card,
} from '@mui/material';
import {
  Close as CloseIcon,
  Star as StarIcon,
  DirectionsCar as RideIcon,
  Person as PersonIcon,
  TwoWheeler as BikeIcon,
  Phone as PhoneIcon,
  FormatQuote as QuoteIcon,
  AccessTime as TimeIcon,
} from '@mui/icons-material';
import { formatVietnamDateTime } from '../../utils/dateTime';

export const BookingReviewModal = ({
  open,
  onClose,
  booking,
  review,
  customer,
  driver,
}) => {
  if (!booking) return null;

  const hasReview = Boolean(review && (review.id || review.rating));

  const cName = review?.customerName || customer?.fullName || `Khách hàng #${booking.customerId}`;
  const cAvatar = review?.customerAvatarUrl || customer?.avatarUrl || '';
  const cPhone = customer?.phoneNumber || '';

  const dName = driver?.fullName || (booking.driverId ? `Tài xế #${booking.driverId}` : 'Tài xế đối tác');
  const dAvatar = driver?.avatarUrl || booking.driverAvatarUrl || '';
  const dPhone = driver?.phoneNumber || '';
  const dPlate = driver?.licensePlate || 'Chưa cập nhật';
  const dModel = driver?.vehicleModel || driver?.vehicleType || 'Xe đối tác OmniGo';

  const getRatingLabel = (rating) => {
    switch (rating) {
      case 5: return 'Tuyệt vời & Lái xe rất an toàn (5 sao)';
      case 4: return 'Hài lòng & Đúng giờ (4 sao)';
      case 3: return 'Bình thường (3 sao)';
      case 2: return 'Chưa hài lòng (2 sao)';
      case 1: return 'Rất không hài lòng (1 sao)';
      default: return `${rating || 5} sao`;
    }
  };

  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth="sm"
      fullWidth
      PaperProps={{
        sx: {
          borderRadius: 3.5,
          p: 0.5,
          boxShadow: '0 20px 45px rgba(0,0,0,0.2)',
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
              bgcolor: 'rgba(245, 158, 11, 0.12)',
              color: '#F59E0B',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <StarIcon />
          </Box>
          <Box>
            <Typography variant="subtitle1" sx={{ fontWeight: 800, lineHeight: 1.2 }}>
              Đánh Giá Chuyến Xe #{booking.id}
            </Typography>
            <Typography variant="caption" sx={{ color: 'text.secondary', fontSize: '0.75rem' }}>
              Điểm đón: {booking.pickupAddress || booking.startAddress || 'Điểm đón'} ➔ {booking.dropoffAddress || booking.endAddress || 'Điểm đến'}
            </Typography>
          </Box>
        </Box>
        <IconButton onClick={onClose} size="small" sx={{ color: 'text.secondary' }}>
          <CloseIcon fontSize="small" />
        </IconButton>
      </DialogTitle>

      <DialogContent dividers sx={{ p: 2.5 }}>
        {hasReview ? (
          <Alert severity="info" sx={{ mb: 2.5, borderRadius: 2, fontSize: '0.84rem' }}>
            Đánh giá từ khách hàng cho chuyến xe #{booking.id}. Quản trị viên chỉ có quyền xem chi tiết.
          </Alert>
        ) : (
          <Alert severity="warning" sx={{ mb: 2.5, borderRadius: 2, fontSize: '0.84rem' }}>
            Chuyến xe #{booking.id} này hiện chưa có đánh giá nào từ khách hàng.
          </Alert>
        )}

        {hasReview && (
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5 }}>
            {/* Rating Stars & Badge */}
            <Box
              sx={{
                p: 2,
                borderRadius: 2.5,
                bgcolor: '#FFFBEB',
                border: '1px solid #FDE68A',
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                gap: 1,
                textAlign: 'center',
              }}
            >
              <Rating
                value={review.rating || 5}
                readOnly
                size="large"
                emptyIcon={<StarIcon style={{ opacity: 0.3 }} fontSize="inherit" />}
              />
              <Typography variant="body1" sx={{ fontWeight: 800, color: '#B45309' }}>
                {getRatingLabel(review.rating)}
              </Typography>
              {review.createdAt && (
                <Typography variant="caption" sx={{ color: '#92400E', display: 'flex', alignItems: 'center', gap: 0.5 }}>
                  <TimeIcon sx={{ fontSize: 13 }} />
                  Đã đánh giá lúc: {formatVietnamDateTime(review.createdAt)}
                </Typography>
              )}
            </Box>

            {/* Comment Section (Read-only Quote Box) */}
            <Box>
              <Typography variant="subtitle2" sx={{ fontWeight: 800, color: '#0F172A', mb: 1, display: 'flex', alignItems: 'center', gap: 0.8 }}>
                <QuoteIcon sx={{ color: '#F59E0B', fontSize: 20 }} />
                Nhận xét của khách hàng:
              </Typography>
              <Box
                sx={{
                  p: 2,
                  bgcolor: '#F8FAFC',
                  border: '1px solid #E2E8F0',
                  borderRadius: 2.5,
                  minHeight: 56,
                  color: review.comment ? '#1E293B' : '#94A3B8',
                  fontSize: '0.9rem',
                  lineHeight: 1.6,
                  fontStyle: review.comment ? 'normal' : 'italic',
                  userSelect: 'text',
                }}
              >
                {review.comment || '(Khách hàng không để lại nhận xét bằng lời)'}
              </Box>
            </Box>

            <Divider />

            {/* 2 Blocks: Customer & Driver Summary */}
            <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' }, gap: 1.5 }}>
              {/* Khách hàng */}
              <Card sx={{ p: 1.5, bgcolor: '#F8FAFC', border: '1px solid #E2E8F0', borderRadius: 2 }}>
                <Typography variant="caption" sx={{ fontWeight: 800, color: '#0369A1', display: 'block', mb: 1 }}>
                  👤 NGƯỜI ĐÁNH GIÁ (KHÁCH HÀNG)
                </Typography>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.2 }}>
                  <Avatar src={cAvatar} sx={{ width: 38, height: 38, bgcolor: '#E0F2FE', color: '#0369A1', fontWeight: 800, fontSize: '0.9rem' }}>
                    {cName.charAt(0).toUpperCase()}
                  </Avatar>
                  <Box sx={{ overflow: 'hidden' }}>
                    <Typography variant="body2" sx={{ fontWeight: 700, color: '#0F172A', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                      {cName}
                    </Typography>
                    {cPhone && (
                      <Typography variant="caption" sx={{ color: '#64748B', display: 'flex', alignItems: 'center', gap: 0.4 }}>
                        <PhoneIcon sx={{ fontSize: 11, color: '#16A34A' }} /> {cPhone}
                      </Typography>
                    )}
                  </Box>
                </Box>
              </Card>

              {/* Tài xế được đánh giá */}
              <Card sx={{ p: 1.5, bgcolor: '#F8FAFC', border: '1px solid #E2E8F0', borderRadius: 2 }}>
                <Typography variant="caption" sx={{ fontWeight: 800, color: '#B45309', display: 'block', mb: 1 }}>
                  🛵 TÀI XẾ ĐƯỢC ĐÁNH GIÁ
                </Typography>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.2 }}>
                  <Avatar src={dAvatar} sx={{ width: 38, height: 38, bgcolor: '#FEF3C7', color: '#B45309', fontWeight: 800, fontSize: '0.9rem' }}>
                    {dName.charAt(0).toUpperCase()}
                  </Avatar>
                  <Box sx={{ overflow: 'hidden' }}>
                    <Typography variant="body2" sx={{ fontWeight: 700, color: '#0F172A', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                      {dName}
                    </Typography>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.8, mt: 0.2 }}>
                      <Chip label={dPlate} size="small" sx={{ height: 18, fontSize: '0.65rem', fontWeight: 800, bgcolor: '#0F172A', color: '#FFFFFF' }} />
                      <Typography variant="caption" sx={{ color: '#64748B', fontSize: '0.7rem' }}>{dModel}</Typography>
                    </Box>
                  </Box>
                </Box>
              </Card>
            </Box>
          </Box>
        )}
      </DialogContent>

      <DialogActions sx={{ p: 2 }}>
        <Button onClick={onClose} variant="contained" sx={{ px: 3, fontWeight: 700, borderRadius: 2, ml: 'auto', bgcolor: '#0F172A', '&:hover': { bgcolor: '#1E293B' } }}>
          Đóng
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default BookingReviewModal;

