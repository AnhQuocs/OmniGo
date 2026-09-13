import React from 'react';
import { useOutletContext } from 'react-router-dom';
import {
  Box,
  Typography,
  Card,
  CircularProgress,
  Stack,
  Button,
} from '@mui/material';
import {
  Star as StarIcon,
  Refresh as RefreshIcon,
  Store as StoreIcon,
} from '@mui/icons-material';
import RestaurantReviewsList from '../../components/food/RestaurantReviewsList';

export const MerchantReviews = () => {
  const { restaurant, loadingRes, refreshRestaurant } = useOutletContext();

  if (loadingRes) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', py: 8 }}>
        <CircularProgress color="primary" />
      </Box>
    );
  }

  if (!restaurant || !restaurant.id) {
    return (
      <Card sx={{ p: 4, textAlign: 'center', borderRadius: 3, boxShadow: '0 4px 12px rgba(0,0,0,0.05)' }}>
        <StoreIcon sx={{ fontSize: 56, color: '#94A3B8', mb: 1.5 }} />
        <Typography variant="h6" fontWeight={700} color="#1E293B">
          Chưa có thông tin nhà hàng
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5, mb: 2 }}>
          Bạn cần đăng ký hoặc liên kết gian hàng nhà hàng để xem và trả lời đánh giá của khách hàng.
        </Typography>
        <Button variant="contained" onClick={refreshRestaurant} startIcon={<RefreshIcon />}>
          Tải lại thông tin
        </Button>
      </Card>
    );
  }

  return (
    <Box sx={{ maxWidth: 1200, mx: 'auto', p: { xs: 2, md: 3 } }}>
      {/* Header */}
      <Stack
        direction={{ xs: 'column', sm: 'row' }}
        justifyContent="space-between"
        alignItems={{ xs: 'flex-start', sm: 'center' }}
        spacing={2}
        sx={{ mb: 3 }}
      >
        <Box>
          <Stack direction="row" spacing={1.5} alignItems="center">
            <Box
              sx={{
                width: 40,
                height: 40,
                borderRadius: 2.5,
                background: 'linear-gradient(135deg, #F59E0B 0%, #D97706 100%)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: '#FFFFFF',
              }}
            >
              <StarIcon />
            </Box>
            <Box>
              <Typography variant="h5" fontWeight={800} color="#0F172A">
                Đánh Giá & Nhận Xét
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Lắng nghe phản hồi của khách hàng và tương tác phản hồi để cải thiện chất lượng phục vụ
              </Typography>
            </Box>
          </Stack>
        </Box>
      </Stack>

      {/* Reviews Content with Merchant Privileges (can reply) */}
      <RestaurantReviewsList restaurantId={restaurant.id} isMerchantOwner={true} />
    </Box>
  );
};

export default MerchantReviews;
