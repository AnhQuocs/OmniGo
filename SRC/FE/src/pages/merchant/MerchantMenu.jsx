import { useState, useEffect } from 'react';
import { useOutletContext } from 'react-router-dom';
import {
  Box,
  Typography,
  Card,
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
} from '@mui/material';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  CheckCircle as AvailableIcon,
  Block as UnavailableIcon,
  Restaurant as DishIcon,
  Refresh as RefreshIcon,
} from '@mui/icons-material';
import foodService from '../../services/foodService';
import toast from 'react-hot-toast';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import UndoDeleteSnackbar from '../../components/common/UndoDeleteSnackbar';
import useUndoDelete from '../../hooks/useUndoDelete';
import MenuItemImageField from '../../components/food/MenuItemImageField';

export const MerchantMenu = () => {
  const { restaurant, loadingRes } = useOutletContext();
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
  const [imageFile, setImageFile] = useState(null);

  // Confirm Delete Dialog State
    // Undo Delete Hook (5s countdown)
  const {
    pendingItem: pendingDeleteFood,
    secondsLeft: deleteSecondsLeft,
    progressPercent: deleteProgressPercent,
    startDeleteWithUndo,
    handleUndo: handleUndoDeleteFood,
    handleDismiss: handleDismissDeleteFood,
  } = useUndoDelete({
    itemType: 'món ăn',
    onCommitDelete: async (item) => {
      await foodService.deleteMenuItem(item.id);
    },
    onRestoreItem: (item) => {
      setMenuItems((prev) => {
        if (prev.some((m) => m.id === item.id)) return prev;
        return [item, ...prev];
      });
    },
  });

  const [deleteConfirmItem, setDeleteConfirmItem] = useState(null);
  const [deleting, setDeleting] = useState(false);

  const fetchMenu = async () => {
    if (!restaurant?.id) return;
    try {
      setLoading(true);
      const res = await foodService.getMenuItems(restaurant.id);
      const items = Array.isArray(res) ? res : res?.data || [];
      setMenuItems(items);
    } catch (err) {
      console.warn('Lỗi tải thực đơn:', err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (restaurant?.id) {
      fetchMenu();
    } else if (!loadingRes) {
      setLoading(false);
    }
  }, [restaurant?.id, loadingRes]);

  const handleOpenAdd = () => {
    setImageFile(null);
    setEditingItem(null);
    setFormData({
      name: '',
      category: 'Món chính',
      price: '',
      description: '',
      imageUrl: '',
      isAvailable: true,
    });
    setOpenModal(true);
  };

  const handleOpenEdit = (item) => {
    setImageFile(null);
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
    } catch {
      toast.error('Lỗi đổi trạng thái món ăn');
    }
  };

  const handleOpenDeleteConfirm = (item) => {
    setDeleteConfirmItem(item);
  };

  const handleConfirmDelete = () => {
    if (!deleteConfirmItem) return;
    const itemToDelete = deleteConfirmItem;
    // Ẩn ngay thẻ món ăn khỏi danh sách
    setMenuItems((prev) => prev.filter((m) => m.id !== itemToDelete.id));
    setDeleteConfirmItem(null);
    // Kích hoạt thanh đếm ngược 5s để hoàn tác
    startDeleteWithUndo(itemToDelete);
  };

  const handleSubmitForm = async (e) => {
    e.preventDefault();
    if (submitting) return;
    if (!formData.name || !formData.price) {
      toast.error('Vui lòng nhập tên món và giá tiền');
      return;
    }

    try {
      setSubmitting(true);
      let imageUrl = formData.imageUrl.trim();
      if (imageFile) {
        imageUrl = await foodService.uploadImage(imageFile);
        if (!imageUrl) throw new Error('Không thể tải ảnh lên. Vui lòng thử lại.');
        setFormData((prev) => ({ ...prev, imageUrl }));
        setImageFile(null);
      }
      const payload = {
        restaurantId: restaurant.id,
        name: formData.name.trim(),
        category: formData.category.trim() || 'Món chính',
        price: parseFloat(formData.price),
        description: formData.description.trim(),
        imageUrl: imageUrl || 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400',
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

  if (loadingRes) {
    return (
      <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', py: 12 }}>
        <CircularProgress sx={{ color: '#F97316', mb: 2 }} />
        <Typography variant="body2" sx={{ color: '#64748B', fontWeight: 600 }}>
          Đang tải thông tin quán ăn và thực đơn...
        </Typography>
      </Box>
    );
  }

  if (!restaurant) {
    return (
      <Box sx={{ maxWidth: 640, mx: 'auto', py: 6, px: 2 }}>
        <Card sx={{ textAlign: 'center', p: 4, borderRadius: 3.5, border: '1px solid #E2E8F0', boxShadow: '0 4px 16px rgba(0,0,0,0.03)' }}>
          <DishIcon sx={{ fontSize: 56, mb: 1.5, color: '#F97316' }} />
          <Typography variant="h6" sx={{ fontWeight: 800, color: '#0F172A', mb: 1 }}>
            Bạn chưa đăng ký Nhà Hàng
          </Typography>
          <Typography variant="body2" sx={{ color: '#64748B', maxWidth: 460, mx: 'auto' }}>
            Vui lòng đăng ký quán ăn để bắt đầu thiết lập thực đơn và mở bán món ăn trực tuyến.
          </Typography>
        </Card>
      </Box>
    );
  }

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

        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.2, width: { xs: '100%', sm: 'auto' }, flexWrap: 'wrap' }}>
          <TextField
            size="small"
            placeholder="Tìm kiếm món ăn..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            sx={{ flex: { xs: 1, sm: 'none' }, minWidth: { xs: 140, sm: 220 } }}
          />
          <IconButton
            onClick={fetchMenu}
            disabled={loading}
            title="Làm mới thực đơn"
            sx={{
              bgcolor: '#F1F5F9',
              border: '1px solid #E2E8F0',
              borderRadius: 2.5,
              p: 1,
              '&:hover': { bgcolor: '#E2E8F0' },
            }}
          >
            <RefreshIcon fontSize="small" sx={{ color: '#475569' }} />
          </IconButton>
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
              flex: { xs: 1, sm: 'none' },
              whiteSpace: 'nowrap',
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
                    <IconButton size="small" onClick={() => handleOpenDeleteConfirm(item)} title="Xóa món">
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
      <Dialog open={openModal} onClose={() => !submitting && setOpenModal(false)} maxWidth="xs" fullWidth PaperProps={{ sx: { borderRadius: 3.5, p: 1 } }}>
        <form onSubmit={handleSubmitForm}>
          <DialogTitle sx={{ fontWeight: 800, color: '#0F172A', pb: 1 }}>
            {editingItem ? 'Chỉnh sửa món ăn' : 'Thêm món ăn mới'}
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
            <MenuItemImageField
              value={formData.imageUrl}
              file={imageFile}
              disabled={submitting}
              onFileChange={setImageFile}
              onChange={(imageUrl) => setFormData((prev) => ({ ...prev, imageUrl }))}
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
          <DialogActions sx={{ px: { xs: 2, sm: 3 }, pb: 2, flexDirection: { xs: 'column-reverse', sm: 'row' }, gap: 1 }}>
            <Button disabled={submitting} onClick={() => setOpenModal(false)} sx={{ textTransform: 'none', fontWeight: 600, width: { xs: '100%', sm: 'auto' } }}>
              Hủy
            </Button>
            <Button
              type="submit"
              variant="contained"
              disabled={submitting}
              sx={{ bgcolor: '#F97316', fontWeight: 700, textTransform: 'none', '&:hover': { bgcolor: '#EA580C' }, width: { xs: '100%', sm: 'auto' } }}
            >
              {submitting ? <CircularProgress size={20} color="inherit" /> : 'Lưu món ăn'}
            </Button>
          </DialogActions>
        </form>
      </Dialog>

      {/* Confirm Delete Dialog */}
      <ConfirmDialog
        open={Boolean(deleteConfirmItem)}
        title="Xác nhận xóa món ăn"
        content={
          deleteConfirmItem ? (
            <span>
              Bạn có chắc chắn muốn xóa món <b>"{deleteConfirmItem.name}"</b> khỏi thực đơn không? Thẻ món ăn sẽ được ẩn ngay và bạn có 5 giây để hoàn tác nếu đổi ý.
            </span>
          ) : ''
        }
        confirmText="Xác nhận xóa"
        cancelText="Hủy"
        confirmColor="error"
        loading={false}
        onClose={() => setDeleteConfirmItem(null)}
        onConfirm={handleConfirmDelete}
      />

      {/* Thanh Hoàn tác 5s sau khi xóa */}
      <UndoDeleteSnackbar
        open={Boolean(pendingDeleteFood)}
        itemName={pendingDeleteFood?.name}
        itemType="món ăn"
        secondsLeft={deleteSecondsLeft}
        progressPercent={deleteProgressPercent}
        onUndo={handleUndoDeleteFood}
        onDismiss={handleDismissDeleteFood}
      />
    </Box>
  );
};

export default MerchantMenu;
