import React, { useState, useEffect } from 'react';
import { useOutletContext } from 'react-router-dom';
import {
  Box,
  Typography,
  Card,
  Grid,
  Button,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  FormControlLabel,
  Switch,
  Chip,
  CircularProgress,
  Avatar,
} from '@mui/material';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  CheckCircle as AvailableIcon,
  Block as UnavailableIcon,
  Search as SearchIcon,
  Restaurant as DishIcon,
} from '@mui/icons-material';
import foodService from '../../services/foodService';
import toast from 'react-hot-toast';

export const MerchantMenu = () => {
  const { restaurant } = useOutletContext();
  const [menuItems, setMenuItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');

  // Dialog State
  const [openModal, setOpenModal] = useState(false);
  const [editingItem, setEditingItem] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    category: '',
    price: '',
    description: '',
    imageUrl: '',
    isAvailable: true,
  });
  const [submitting, setSubmitting] = useState(false);

  const fetchMenu = async () => {
    if (!restaurant?.id) return;
    try {
      setLoading(true);
      const res = await foodService.getMenuItems(restaurant.id);
      setMenuItems(Array.isArray(res) ? res : res?.data || []);
    } catch (err) {
      console.warn('Lỗi tải thực đơn:', err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMenu();
  }, [restaurant?.id]);

  const handleOpenAdd = () => {
    setEditingItem(null);
    setFormData({
      name: '',
      category: 'Món chính',
      price: '',
      description: '',
      imageUrl: 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400',
      isAvailable: true,
    });
    setOpenModal(true);
  };

  const handleOpenEdit = (item) => {
    setEditingItem(item);
    setFormData({
      name: item.name || '',
      category: item.category || 'Món chính',
      price: item.price || '',
      description: item.description || '',
      imageUrl: item.imageUrl || '',
      isAvailable: item.isAvailable !== false,
    });
    setOpenModal(true);
  };

  const handleToggleAvailable = async (item) => {
    try {
      const updated = !item.isAvailable;
      await foodService.updateMenuItem(item.id, {
        restaurantId: restaurant.id,
        name: item.name,
        category: item.category,
        price: item.price,
        description: item.description,
        imageUrl: item.imageUrl,
        isAvailable: updated,
      });
      toast.success(`Đã chuyển món sang: ${updated ? 'Còn món' : 'Hết món'}`);
      setMenuItems((prev) =>
        prev.map((m) => (m.id === item.id ? { ...m, isAvailable: updated } : m))
      );
    } catch (err) {
      toast.error('Lỗi đổi trạng thái món ăn');
    }
  };

  const handleDeleteItem = async (itemId) => {
    if (!window.confirm('Bạn có chắc muốn xóa món ăn này khỏi thực đơn?')) return;
    try {
      await foodService.deleteMenuItem(itemId);
      toast.success('Đã xóa món ăn thành công!');
      setMenuItems((prev) => prev.filter((m) => m.id !== itemId));
    } catch (err) {
      toast.error('Lỗi xóa món ăn');
    }
  };

  const handleSubmitForm = async (e) => {
    e.preventDefault();
    if (!formData.name || !formData.price) {
      toast.error('Vui lòng nhập tên món và giá tiền');
      return;
    }

    try {
      setSubmitting(true);
      const payload = {
        restaurantId: restaurant.id,
        name: formData.name.trim(),
        category: formData.category.trim() || 'Món chính',
        price: parseFloat(formData.price),
        description: formData.description.trim(),
        imageUrl: formData.imageUrl.trim() || 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400',
        isAvailable: formData.isAvailable,
      };

      if (editingItem) {
        await foodService.updateMenuItem(editingItem.id, payload);
        toast.success('Cập nhật món ăn thành công!');
      } else {
        await foodService.createMenuItem(restaurant.id, payload);
        toast.success('Thêm món ăn mới thành công!');
      }
      setOpenModal(false);
      await fetchMenu();
    } catch (err) {
      toast.error(err.response?.data?.message || err.message || 'Lỗi lưu món ăn');
    } finally {
      setSubmitting(false);
    }
  };

  const filteredItems = menuItems.filter((item) =>
    (item.name || '').toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5 }}>
      {/* Header */}
      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: 1.5 }}>
        <Box>
          <Typography variant="h5" sx={{ fontWeight: 800, color: '#0F172A', mb: 0.3 }}>
            Thực đơn món ăn
          </Typography>
          <Typography variant="body2" sx={{ color: '#64748B' }}>
            Quản lý các món ăn, giá tiền và trạng thái còn món của quán
          </Typography>
        </Box>

        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
          <TextField
            size="small"
            placeholder="Tìm kiếm món ăn..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            sx={{ width: 220 }}
          />
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={handleOpenAdd}
            sx={{
              bgcolor: '#F97316',
              borderRadius: 2.5,
              fontWeight: 700,
              textTransform: 'none',
              '&:hover': { bgcolor: '#EA580C' },
            }}
          >
            Thêm món mới
          </Button>
        </Box>
      </Box>

      {/* Grid of Food Items */}
      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}>
          <CircularProgress color="primary" />
        </Box>
      ) : filteredItems.length === 0 ? (
        <Card sx={{ textAlign: 'center', py: 6, borderRadius: 3, border: '1px dashed #CBD5E1' }}>
          <DishIcon sx={{ fontSize: 48, color: '#94A3B8', mb: 1 }} />
          <Typography variant="body1" sx={{ color: '#64748B', fontWeight: 600 }}>
            Chưa có món ăn nào trong thực đơn
          </Typography>
          <Button variant="outlined" startIcon={<AddIcon />} onClick={handleOpenAdd} sx={{ mt: 2, borderRadius: 2 }}>
            Tạo món đầu tiên
          </Button>
        </Card>
      ) : (
        <Box
          sx={{
            display: 'grid',
            gridTemplateColumns: {
              xs: '1fr',
              sm: 'repeat(2, 1fr)',
              md: 'repeat(3, 1fr)',
              lg: 'repeat(4, 1fr)',
            },
            gap: 2,
          }}
        >
          {filteredItems.map((item) => (
            <Card
              key={item.id}
              sx={{
                borderRadius: 3.5,
                border: '1px solid #E2E8F0',
                overflow: 'hidden',
                boxShadow: '0 2px 8px rgba(0,0,0,0.03)',
                display: 'flex',
                flexDirection: 'column',
                transition: 'all 0.2s',
                '&:hover': { transform: 'translateY(-2px)', boxShadow: '0 8px 18px rgba(0,0,0,0.06)' },
              }}
            >
              {/* Image & Status Tag */}
              <Box sx={{ position: 'relative', height: 160, bgcolor: '#F1F5F9' }}>
                <Box
                  component="img"
                  src={item.imageUrl || 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400'}
                  alt={item.name}
                  sx={{ width: '100%', height: '100%', objectFit: 'cover' }}
                />
                <Chip
                  label={item.isAvailable ? 'Còn món' : 'Hết món'}
                  size="small"
                  sx={{
                    position: 'absolute',
                    top: 10,
                    right: 10,
                    bgcolor: item.isAvailable ? '#10B981' : '#EF4444',
                    color: '#FFFFFF',
                    fontWeight: 800,
                    fontSize: '0.72rem',
                    height: 22,
                  }}
                />
              </Box>

              {/* Item Info */}
              <Box sx={{ p: 2, flex: 1, display: 'flex', flexDirection: 'column' }}>
                <Typography variant="caption" sx={{ color: '#F97316', fontWeight: 800, textTransform: 'uppercase' }}>
                  {item.category || 'Món ăn'}
                </Typography>
                <Typography variant="subtitle1" sx={{ fontWeight: 800, color: '#0F172A', mb: 0.5, lineHeight: 1.2 }}>
                  {item.name}
                </Typography>
                <Typography
                  variant="body2"
                  sx={{
                    color: '#64748B',
                    fontSize: '0.82rem',
                    mb: 1.5,
                    flex: 1,
                    display: '-webkit-box',
                    WebkitLineClamp: 2,
                    WebkitBoxOrient: 'vertical',
                    overflow: 'hidden',
                  }}
                >
                  {item.description || 'Không có mô tả'}
                </Typography>

                {/* Price & Actions */}
                <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', pt: 1, borderTop: '1px solid #F1F5F9' }}>
                  <Typography variant="subtitle1" sx={{ fontWeight: 800, color: '#F97316' }}>
                    {(item.price || 0).toLocaleString('vi-VN')} đ
                  </Typography>

                  <Box sx={{ display: 'flex', gap: 0.5 }}>
                    <IconButton size="small" onClick={() => handleToggleAvailable(item)} title={item.isAvailable ? 'Đổi sang Hết món' : 'Đổi sang Còn món'}>
                      {item.isAvailable ? <UnavailableIcon fontSize="small" sx={{ color: '#EF4444' }} /> : <AvailableIcon fontSize="small" sx={{ color: '#10B981' }} />}
                    </IconButton>
                    <IconButton size="small" onClick={() => handleOpenEdit(item)} title="Chỉnh sửa món">
                      <EditIcon fontSize="small" sx={{ color: '#3B82F6' }} />
                    </IconButton>
                    <IconButton size="small" onClick={() => handleDeleteItem(item.id)} title="Xóa món">
                      <DeleteIcon fontSize="small" sx={{ color: '#EF4444' }} />
                    </IconButton>
                  </Box>
                </Box>
              </Box>
            </Card>
          ))}
        </Box>
      )}

      {/* Add / Edit Dialog */}
      <Dialog open={openModal} onClose={() => setOpenModal(false)} maxWidth="xs" fullWidth PaperProps={{ sx: { borderRadius: 3.5, p: 1 } }}>
        <form onSubmit={handleSubmitForm}>
          <DialogTitle sx={{ fontWeight: 800, color: '#0F172A', pb: 1 }}>
            {editingItem ? '✏️ Chỉnh sửa món ăn' : '🍽️ Thêm món ăn mới'}
          </DialogTitle>
          <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: '10px !important' }}>
            <TextField
              label="Tên món ăn"
              fullWidth
              required
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
            />
            <TextField
              label="Danh mục (vd: Cơm, Phở, Đồ uống)"
              fullWidth
              value={formData.category}
              onChange={(e) => setFormData({ ...formData, category: e.target.value })}
            />
            <TextField
              label="Đơn giá (VNĐ)"
              type="number"
              fullWidth
              required
              value={formData.price}
              onChange={(e) => setFormData({ ...formData, price: e.target.value })}
            />
            <TextField
              label="Đường dẫn ảnh món ăn (URL)"
              fullWidth
              value={formData.imageUrl}
              onChange={(e) => setFormData({ ...formData, imageUrl: e.target.value })}
            />
            <TextField
              label="Mô tả chi tiết"
              multiline
              rows={2}
              fullWidth
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
            />
            <FormControlLabel
              control={
                <Switch
                  checked={formData.isAvailable}
                  onChange={(e) => setFormData({ ...formData, isAvailable: e.target.checked })}
                  color="primary"
                />
              }
              label={<Typography variant="body2" sx={{ fontWeight: 600 }}>Còn hàng để bán</Typography>}
            />
          </DialogContent>
          <DialogActions sx={{ px: 3, pb: 2 }}>
            <Button onClick={() => setOpenModal(false)} sx={{ textTransform: 'none', fontWeight: 600 }}>
              Hủy
            </Button>
            <Button
              type="submit"
              variant="contained"
              disabled={submitting}
              sx={{ bgcolor: '#F97316', fontWeight: 700, textTransform: 'none', '&:hover': { bgcolor: '#EA580C' } }}
            >
              {submitting ? <CircularProgress size={20} color="inherit" /> : 'Lưu món ăn'}
            </Button>
          </DialogActions>
        </form>
      </Dialog>
    </Box>
  );
};

export default MerchantMenu;
