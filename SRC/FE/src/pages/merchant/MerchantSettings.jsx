import React, { useState, useEffect } from 'react';
import { useOutletContext } from 'react-router-dom';
import {
  Box,
  Typography,
  Card,
  Grid,
  TextField,
  Button,
  CircularProgress,
  Avatar,
  Divider,
} from '@mui/material';
import {
  Store as StoreIcon,
  Save as SaveIcon,
  Add as AddIcon,
} from '@mui/icons-material';
import foodService from '../../services/foodService';
import toast from 'react-hot-toast';

export const MerchantSettings = () => {
  const { restaurant, refreshRestaurant } = useOutletContext();
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    name: '',
    phone: '',
    address: '',
    latitude: '21.033333',
    longitude: '105.789123',
    openTime: '08:00',
    closeTime: '22:00',
    imageUrl: '',
  });

  useEffect(() => {
    if (restaurant) {
      setFormData({
        name: restaurant.name || '',
        phone: restaurant.phone || '',
        address: restaurant.address || '',
        latitude: restaurant.latitude?.toString() || '21.033333',
        longitude: restaurant.longitude?.toString() || '105.789123',
        openTime: restaurant.openTime || '08:00',
        closeTime: restaurant.closeTime || '22:00',
        imageUrl: restaurant.imageUrl || '',
      });
    }
  }, [restaurant]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.name || !formData.address) {
      toast.error('Vui lòng nhập đầy đủ tên quán và địa chỉ');
      return;
    }

    const payload = {
      name: formData.name.trim(),
      phone: formData.phone.trim(),
      address: formData.address.trim(),
      latitude: parseFloat(formData.latitude) || 21.033333,
      longitude: parseFloat(formData.longitude) || 105.789123,
      openTime: formData.openTime || '08:00',
      closeTime: formData.closeTime || '22:00',
      imageUrl: formData.imageUrl.trim() || 'https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=500',
    };

    try {
      setLoading(true);
      if (restaurant?.id) {
        await foodService.updateRestaurant(restaurant.id, payload);
        toast.success('Cập nhật thông tin quán thành công!');
      } else {
        await foodService.createRestaurant(payload);
        toast.success('Đăng ký quán ăn mới thành công!');
      }
      if (refreshRestaurant) await refreshRestaurant();
    } catch (err) {
      toast.error(err.response?.data?.message || err.message || 'Lỗi lưu thông tin quán');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box sx={{ maxWidth: 800, mx: 'auto', display: 'flex', flexDirection: 'column', gap: 2.5 }}>
      {/* Header */}
      <Box>
        <Typography variant="h5" sx={{ fontWeight: 800, color: '#0F172A', mb: 0.3 }}>
          {restaurant?.id ? 'Cài đặt quán ăn' : 'Đăng ký Quán Ăn Mới'}
        </Typography>
        <Typography variant="body2" sx={{ color: '#64748B' }}>
          {restaurant?.id
            ? 'Quản lý thông tin nhà hàng, giờ mở cửa và địa điểm kinh doanh'
            : 'Tài khoản của bạn chưa có thông tin quán ăn. Hãy đăng ký ngay để bắt đầu bán hàng!'}
        </Typography>
      </Box>

      {/* Settings Form Card */}
      <Card
        component="form"
        onSubmit={handleSubmit}
        sx={{
          p: { xs: 2.5, sm: 3.5 },
          borderRadius: 3.5,
          border: '1px solid #E2E8F0',
          boxShadow: '0 2px 10px rgba(0,0,0,0.03)',
        }}
      >
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 3 }}>
          <Avatar
            src={formData.imageUrl || 'https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=200'}
            alt="Quán ăn"
            sx={{ width: 64, height: 64, borderRadius: 3 }}
          />
          <Box>
            <Typography variant="subtitle1" sx={{ fontWeight: 800, color: '#0F172A' }}>
              {formData.name || 'Tên Quán Ăn'}
            </Typography>
            <Typography variant="caption" sx={{ color: '#64748B' }}>
              {restaurant?.id ? `Mã quán: #${restaurant.id}` : 'Trạng thái: Chưa kích hoạt'}
            </Typography>
          </Box>
        </Box>

        <Divider sx={{ mb: 3 }} />

        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5 }}>
          <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' }, gap: 2 }}>
            <TextField
              label="Tên Quán Ăn"
              required
              fullWidth
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
            />
            <TextField
              label="Số điện thoại hotline"
              fullWidth
              value={formData.phone}
              onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
            />
          </Box>

          <TextField
            label="Địa chỉ chi tiết"
            required
            fullWidth
            value={formData.address}
            onChange={(e) => setFormData({ ...formData, address: e.target.value })}
          />

          <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' }, gap: 2 }}>
            <TextField
              label="Vĩ độ (Latitude)"
              fullWidth
              value={formData.latitude}
              onChange={(e) => setFormData({ ...formData, latitude: e.target.value })}
              helperText="Tọa độ GPS vị trí quán (vd: 21.033333)"
            />
            <TextField
              label="Kinh độ (Longitude)"
              fullWidth
              value={formData.longitude}
              onChange={(e) => setFormData({ ...formData, longitude: e.target.value })}
              helperText="Tọa độ GPS vị trí quán (vd: 105.789123)"
            />
          </Box>

          <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' }, gap: 2 }}>
            <TextField
              label="Giờ mở cửa"
              fullWidth
              placeholder="08:00"
              value={formData.openTime}
              onChange={(e) => setFormData({ ...formData, openTime: e.target.value })}
            />
            <TextField
              label="Giờ đóng cửa"
              fullWidth
              placeholder="22:00"
              value={formData.closeTime}
              onChange={(e) => setFormData({ ...formData, closeTime: e.target.value })}
            />
          </Box>

          <TextField
            label="Đường dẫn ảnh đại diện quán (URL)"
            fullWidth
            value={formData.imageUrl}
            onChange={(e) => setFormData({ ...formData, imageUrl: e.target.value })}
            placeholder="https://..."
          />

          <Box sx={{ display: 'flex', justifyContent: 'flex-end', mt: 1 }}>
            <Button
              type="submit"
              variant="contained"
              disabled={loading}
              startIcon={loading ? <CircularProgress size={18} color="inherit" /> : restaurant?.id ? <SaveIcon /> : <AddIcon />}
              sx={{
                bgcolor: '#F97316',
                color: '#FFFFFF',
                px: 3.5,
                py: 1.2,
                borderRadius: 2.5,
                fontWeight: 700,
                textTransform: 'none',
                fontSize: '0.95rem',
                '&:hover': { bgcolor: '#EA580C' },
              }}
            >
              {restaurant?.id ? 'Lưu thay đổi' : 'Đăng ký Quán Ngay'}
            </Button>
          </Box>
        </Box>
      </Card>
    </Box>
  );
};

export default MerchantSettings;
