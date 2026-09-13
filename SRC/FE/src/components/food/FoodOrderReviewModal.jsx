import React, { useState, useEffect } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Rating,
  TextField,
  Typography,
  Box,
  IconButton,
  Chip,
  Stack,
  Alert,
  CircularProgress,
  Divider,
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import StarIcon from '@mui/icons-material/Star';
import PhotoCameraIcon from '@mui/icons-material/PhotoCamera';
import DeleteIcon from '@mui/icons-material/Delete';
import StorefrontIcon from '@mui/icons-material/Storefront';
import DeliveryDiningIcon from '@mui/icons-material/DeliveryDining';
import foodService from '../../services/foodService';
import toast from 'react-hot-toast';

const RESTAURANT_TAGS = [
  'Đồ ăn nóng hổi',
  'Ngon chuẩn vị',
  'Đóng gói cẩn thận',
  'Phần ăn đầy đặn',
  'Menu đa dạng',
  'Giao đúng món',
];

const DRIVER_TAGS = [
  'Giao siêu nhanh',
  'Thân thiện lịch sự',
  'Bảo quản đồ ăn tốt',
  'Nhiệt tình tìm địa chỉ',
];

export default function FoodOrderReviewModal({
  open,
  onClose,
  order,
  existingReview = null,
  onSuccess,
  readOnly = false,
}) {
  const [restaurantRating, setRestaurantRating] = useState(5);
  const [restaurantComment, setRestaurantComment] = useState('');
  const [restaurantImages, setRestaurantImages] = useState([]);
  const [driverRating, setDriverRating] = useState(5);
  const [driverComment, setDriverComment] = useState('');
  const [loading, setLoading] = useState(false);

  const isEditing = Boolean(existingReview && existingReview.id);

  useEffect(() => {
    if (open) {
      if (existingReview) {
        setRestaurantRating(existingReview.restaurantRating || 5);
        setRestaurantComment(existingReview.restaurantComment || '');
        setRestaurantImages(existingReview.restaurantImages || existingReview.images || []);
        setDriverRating(existingReview.driverRating || 5);
        setDriverComment(existingReview.driverComment || '');
      } else {
        setRestaurantRating(5);
        setRestaurantComment('');
        setRestaurantImages([]);
        setDriverRating(5);
        setDriverComment('');
      }
    }
  }, [open, existingReview]);

  const handleToggleRestaurantTag = (tag) => {
    if (readOnly) return;
    if (restaurantComment.includes(tag)) {
      setRestaurantComment((prev) => prev.replace(tag, '').replace(/,\s*,/g, ',').trim());
    } else {
      setRestaurantComment((prev) => (prev ? `${prev}, ${tag}` : tag));
    }
  };

  const handleToggleDriverTag = (tag) => {
    if (readOnly) return;
    if (driverComment.includes(tag)) {
      setDriverComment((prev) => prev.replace(tag, '').replace(/,\s*,/g, ',').trim());
    } else {
      setDriverComment((prev) => (prev ? `${prev}, ${tag}` : tag));
    }
  };

  const handleImageUpload = (e) => {
    if (readOnly) return;
    const files = Array.from(e.target.files || []);
    if (files.length === 0) return;

    const allowedExtensions = /\.(png|jpe?g|webp)$/i;
    const allowedMimeTypes = ['image/png', 'image/jpeg', 'image/webp'];

    for (const file of files) {
      const hasValidExt = allowedExtensions.test(file.name || '');
      const hasValidMime = !file.type || allowedMimeTypes.includes(file.type.toLowerCase());
      if (!hasValidExt || !hasValidMime) {
        toast.error(`File "${file.name}" không hợp lệ! Chỉ cho phép định dạng PNG, JPG, WEBP.`);
        e.target.value = '';
        return;
      }
    }

    const remainingSlots = 5 - restaurantImages.length;
    if (remainingSlots <= 0) {
      toast.error('Tối đa 5 ảnh cho mỗi đánh giá');
      e.target.value = '';
      return;
    }

    const filesToProcess = files.slice(0, remainingSlots);
    if (files.length > remainingSlots) {
      toast.warning(`Chỉ cho phép tối đa 5 ảnh. Đã nhận ${remainingSlots} ảnh!`);
    }

    filesToProcess.forEach((file) => {
      const reader = new FileReader();
      reader.onloadend = () => {
        if (reader.result) {
          setRestaurantImages((prev) => (prev.length < 5 ? [...prev, reader.result] : prev));
        }
      };
      reader.readAsDataURL(file);
    });
    e.target.value = '';
  };

  const handleRemoveImage = (index) => {
    if (readOnly) return;
    setRestaurantImages((prev) => prev.filter((_, i) => i !== index));
  };

  const handleSubmit = async () => {
    if (readOnly) return;
    if (!restaurantRating || restaurantRating < 1) {
      toast.error('Vui lòng chọn số sao đánh giá quán ăn');
      return;
    }

    setLoading(true);
    try {
      if (isEditing) {
        const payload = {
          restaurantRating,
          restaurantComment,
          restaurantImages,
          images: restaurantImages,
          driverRating,
          driverComment,
        };
        await foodService.updateReview(existingReview.id, payload);
        toast.success('Cập nhật đánh giá thành công!');
      } else {
        const payload = {
          orderId: order?.id,
          restaurantRating,
          restaurantComment,
          restaurantImages,
          images: restaurantImages,
          driverRating,
          driverComment,
        };
        await foodService.createReview(payload);
        toast.success('Cảm ơn bạn đã gửi đánh giá!');
      }
      if (onSuccess) onSuccess();
      onClose();
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Gửi đánh giá thất bại';
      toast.error(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle sx={{ m: 0, p: 2, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Typography variant="h6" fontWeight={700}>
          {readOnly
            ? `🔍 Chi Tiết Đánh Giá Đơn Hàng #${order?.id}`
            : isEditing
            ? `✏️ Chỉnh Sửa Đánh Giá Đơn Hàng #${order?.id}`
            : `⭐ Đánh Giá Đơn Đồ Ăn #${order?.id}`}
        </Typography>
        <IconButton onClick={onClose} size="small">
          <CloseIcon />
        </IconButton>
      </DialogTitle>

      <DialogContent dividers>
        {readOnly ? (
          existingReview ? (
            <Alert severity="info" sx={{ mb: 2.5, borderRadius: 2 }}>
              Đánh giá từ khách hàng cho đơn hàng #{order?.id}. Quản trị viên chỉ có quyền xem chi tiết.
            </Alert>
          ) : (
            <Alert severity="warning" sx={{ mb: 2.5, borderRadius: 2 }}>
              Đơn hàng #{order?.id} này hiện chưa có đánh giá nào từ khách hàng.
            </Alert>
          )
        ) : (
          <Alert severity="info" sx={{ mb: 2.5, borderRadius: 2 }}>
            {isEditing
              ? 'Bạn có thể chỉnh sửa đánh giá này trong vòng 48 giờ kể từ khi gửi lần đầu.'
              : 'Đánh giá của bạn sẽ giúp quán ăn và tài xế cải thiện chất lượng phục vụ tốt hơn.'}
          </Alert>
        )}

        {/* PHẢN HỒI TỪ QUÁN ĂN NẾU CÓ */}
        {(existingReview?.merchantReply || existingReview?.restaurantReply) && (
          <Box
            sx={{
              mb: 2.5,
              p: 2,
              borderRadius: 2,
              bgcolor: '#eff6ff',
              border: '1px solid #bfdbfe',
              borderLeft: '4px solid #2563eb',
            }}
          >
            <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 0.5 }}>
              <StorefrontIcon sx={{ fontSize: 18, color: '#1d4ed8' }} />
              <Typography variant="subtitle2" fontWeight={700} color="#1d4ed8">
                Phản hồi từ Quán ăn
              </Typography>
              {(existingReview?.merchantRepliedAt || existingReview?.restaurantReplyAt) && (
                <Typography variant="caption" color="text.secondary">
                  • {new Date(existingReview.merchantRepliedAt || existingReview.restaurantReplyAt).toLocaleDateString('vi-VN')}
                </Typography>
              )}
            </Stack>
            <Typography variant="body2" color="#1e293b" sx={{ fontStyle: 'italic', pl: 3.2 }}>
              "{existingReview.merchantReply || existingReview.restaurantReply}"
            </Typography>
          </Box>
        )}

        {/* SECTION 1: QUÁN ĂN */}
        <Box sx={{ mb: 3 }}>
          <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 1 }}>
            <StorefrontIcon color="primary" />
            <Typography variant="subtitle1" fontWeight={700}>
              Chất lượng Quán ăn: {order?.restaurantName || 'Nhà hàng'}
            </Typography>
          </Stack>

          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, my: 1 }}>
            <Rating
              value={restaurantRating}
              readOnly={readOnly}
              onChange={(_, val) => setRestaurantRating(val || 1)}
              size="large"
              emptyIcon={<StarIcon style={{ opacity: 0.3 }} fontSize="inherit" />}
            />
            <Typography variant="body2" color="text.secondary" fontWeight={600}>
              {restaurantRating === 5 && 'Tuyệt vời'}
              {restaurantRating === 4 && 'Hài lòng'}
              {restaurantRating === 3 && 'Bình thường'}
              {restaurantRating === 2 && 'Không hài lòng'}
              {restaurantRating === 1 && 'Rất tệ'}
            </Typography>
          </Box>

          {/* Quick Tags - Chỉ hiển thị khi cho phép chỉnh sửa */}
          {!readOnly && (
            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.8, my: 1.5 }}>
              {RESTAURANT_TAGS.map((tag) => {
                const selected = restaurantComment.includes(tag);
                return (
                  <Chip
                    key={tag}
                    label={tag}
                    size="small"
                    variant={selected ? 'filled' : 'outlined'}
                    color={selected ? 'primary' : 'default'}
                    onClick={() => handleToggleRestaurantTag(tag)}
                    sx={{ cursor: 'pointer' }}
                  />
                );
              })}
            </Box>
          )}

          <TextField
            fullWidth
            multiline
            rows={3}
            InputProps={{ readOnly: Boolean(readOnly) }}
            placeholder={readOnly ? 'Khách hàng không để lại nhận xét bằng lời.' : 'Chia sẻ cảm nhận chi tiết của bạn về món ăn, hương vị, cách đóng gói...'}
            value={restaurantComment}
            onChange={(e) => setRestaurantComment(e.target.value)}
            sx={{ mt: 1 }}
          />

          {/* Upload hình ảnh món ăn */}
          <Box sx={{ mt: 2 }}>
            <Typography variant="body2" fontWeight={600} sx={{ mb: 1 }}>
              {readOnly ? 'Hình ảnh món ăn từ khách hàng:' : 'Thêm hình ảnh món ăn thực tế (Tối đa 5 ảnh):'}
            </Typography>
            {restaurantImages.length === 0 && readOnly ? (
              <Typography variant="body2" color="text.secondary" sx={{ fontStyle: 'italic', fontSize: '0.85rem' }}>
                Khách hàng không đính kèm hình ảnh.
              </Typography>
            ) : (
              <Stack direction="row" spacing={1.5} alignItems="center" sx={{ flexWrap: 'wrap', gap: 1 }}>
                {restaurantImages.map((img, idx) => (
                  <Box
                    key={idx}
                    sx={{
                      position: 'relative',
                      width: 72,
                      height: 72,
                      borderRadius: 1.5,
                      overflow: 'hidden',
                      border: '1px solid #ddd',
                    }}
                  >
                    <img
                      src={img}
                      alt={`review-${idx}`}
                      style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                    />
                    {!readOnly && (
                      <IconButton
                        size="small"
                        onClick={() => handleRemoveImage(idx)}
                        sx={{
                          position: 'absolute',
                          top: 2,
                          right: 2,
                          bgcolor: 'rgba(0,0,0,0.6)',
                          color: '#fff',
                          p: '2px',
                          '&:hover': { bgcolor: 'rgba(255,0,0,0.8)' },
                        }}
                      >
                        <DeleteIcon sx={{ fontSize: 14 }} />
                      </IconButton>
                    )}
                  </Box>
                ))}

                {!readOnly && restaurantImages.length < 5 && (
                  <Button
                    component="label"
                    variant="outlined"
                    sx={{
                      width: 72,
                      height: 72,
                      borderRadius: 1.5,
                      borderStyle: 'dashed',
                      display: 'flex',
                      flexDirection: 'column',
                      justifyContent: 'center',
                      alignItems: 'center',
                      p: 0,
                    }}
                  >
                    <PhotoCameraIcon color="action" />
                    <Typography variant="caption" sx={{ fontSize: 10, mt: 0.5 }}>
                      Tải ảnh
                    </Typography>
                    <input type="file" hidden accept=".png,.jpg,.jpeg,.webp,image/png,image/jpeg,image/webp" multiple onChange={handleImageUpload} />
                  </Button>
                )}
              </Stack>
            )}
          </Box>
        </Box>

        <Divider sx={{ my: 2.5 }} />

        {/* SECTION 2: TÀI XẾ */}
        <Box>
          <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 1 }}>
            <DeliveryDiningIcon color="success" />
            <Typography variant="subtitle1" fontWeight={700}>
              Đánh giá Tài xế giao hàng
            </Typography>
          </Stack>

          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, my: 1 }}>
            <Rating
              value={driverRating}
              readOnly={readOnly}
              onChange={(_, val) => setDriverRating(val || 1)}
              size="large"
              emptyIcon={<StarIcon style={{ opacity: 0.3 }} fontSize="inherit" />}
            />
            <Typography variant="body2" color="text.secondary" fontWeight={600}>
              {driverRating === 5 && 'Cực kỳ đúng giờ & thân thiện'}
              {driverRating === 4 && 'Tốt'}
              {driverRating === 3 && 'Bình thường'}
              {driverRating === 2 && 'Giao muộn'}
              {driverRating === 1 && 'Thái độ kém'}
            </Typography>
          </Box>

          {/* Quick Tags cho tài xế - Chỉ hiển thị khi chỉnh sửa */}
          {!readOnly && (
            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.8, my: 1.5 }}>
              {DRIVER_TAGS.map((tag) => {
                const selected = driverComment.includes(tag);
                return (
                  <Chip
                    key={tag}
                    label={tag}
                    size="small"
                    variant={selected ? 'filled' : 'outlined'}
                    color={selected ? 'success' : 'default'}
                    onClick={() => handleToggleDriverTag(tag)}
                    sx={{ cursor: 'pointer' }}
                  />
                );
              })}
            </Box>
          )}

          <TextField
            fullWidth
            size="small"
            InputProps={{ readOnly: Boolean(readOnly) }}
            placeholder={readOnly ? 'Khách hàng không để lại ghi chú cho tài xế.' : 'Nhận xét thêm về thái độ hoặc tốc độ giao hàng của tài xế...'}
            value={driverComment}
            onChange={(e) => setDriverComment(e.target.value)}
          />
        </Box>
      </DialogContent>

      <DialogActions sx={{ p: 2 }}>
        {readOnly ? (
          <Button onClick={onClose} variant="contained" color="primary" sx={{ px: 3, fontWeight: 700, borderRadius: 2, ml: 'auto' }}>
            Đóng
          </Button>
        ) : (
          <>
            <Button onClick={onClose} disabled={loading} color="inherit">
              Hủy
            </Button>
            <Button
              onClick={handleSubmit}
              variant="contained"
              color="primary"
              disabled={loading}
              startIcon={loading ? <CircularProgress size={18} color="inherit" /> : null}
              sx={{ px: 3, fontWeight: 700, borderRadius: 2 }}
            >
              {isEditing ? 'Lưu Thay Đổi' : 'Gửi Đánh Giá'}
            </Button>
          </>
        )}
      </DialogActions>
    </Dialog>
  );
}
