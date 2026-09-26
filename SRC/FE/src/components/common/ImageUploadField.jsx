import { useState, useRef } from 'react';
import {
  Box,
  Typography,
  Button,
  TextField,
  IconButton,
  Tooltip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  CircularProgress,
  useTheme,
} from '@mui/material';
import {
  CloudUpload as UploadIcon,
  Delete as DeleteIcon,
  Visibility as ViewIcon,
  Close as CloseIcon,
  Link as LinkIcon,
  CheckCircle as CheckCircleIcon,
} from '@mui/icons-material';
import toast from 'react-hot-toast';

import ConfirmDialog from './ConfirmDialog';
import { foodService } from '../../services/foodService';

export const ImageUploadField = ({
  label = 'Ảnh giấy phép kinh doanh',
  required = false,
  value = '',
  onChange,
  uploadFn = null,
  helperText = 'Tải lên ảnh chụp rõ nét (JPG, PNG, WebP tối đa 5MB) hoặc dán link ảnh',
  placeholder = 'Hoặc dán liên kết ảnh (URL)...',
}) => {
  const theme = useTheme();
  const isDark = theme.palette.mode === 'dark';
  const [previewOpen, setPreviewOpen] = useState(false);
  const [showUrlInput, setShowUrlInput] = useState(false);
  const [inputUrl, setInputUrl] = useState('');
  const [confirmDeleteOpen, setConfirmDeleteOpen] = useState(false);
  const [confirmUploadOpen, setConfirmUploadOpen] = useState(false);
  const [pendingUploadData, setPendingUploadData] = useState('');
  const [pendingFile, setPendingFile] = useState(null);
  const [isUploading, setIsUploading] = useState(false);
  const fileInputRef = useRef(null);

  const handleFileChange = (e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    if (!file.type.startsWith('image/')) {
      toast.error('Vui lòng chọn file định dạng hình ảnh (PNG, JPG, WebP)');
      return;
    }

    if (file.size > 5 * 1024 * 1024) {
      toast.error('Dung lượng ảnh tối đa là 5MB');
      return;
    }

    setPendingFile(file);
    const reader = new FileReader();
    reader.onload = () => {
      if (reader.result) {
        setPendingUploadData(reader.result);
        setConfirmUploadOpen(true);
      }
    };
    reader.onerror = () => {
      toast.error('Lỗi khi đọc file ảnh');
    };
    reader.readAsDataURL(file);
    e.target.value = '';
  };

  const handleApplyUrl = (urlToApply) => {
    const trimmed = (urlToApply || inputUrl).trim();
    if (!trimmed) return;
    setPendingFile(null);
    setPendingUploadData(trimmed);
    setConfirmUploadOpen(true);
  };

  const handleConfirmUpload = async () => {
    if (!pendingUploadData) return;

    if (pendingFile) {
      try {
        setIsUploading(true);
        let finalUrl = '';
        if (uploadFn) {
          const res = await uploadFn(pendingFile);
          finalUrl = res?.data?.url || res?.url || res || '';
        } else {
          finalUrl = await foodService.uploadImage(pendingFile);
        }

        if (finalUrl) {
          onChange?.(finalUrl);
          toast.success('Đã tải ảnh lên Cloudinary thành công!');
        } else {
          onChange?.(pendingUploadData);
          toast.success('Đã tải ảnh thành công!');
        }
      } catch (err) {
        console.error('Lỗi upload Cloudinary:', err);
        // Fallback to data url if cloud keys offline
        onChange?.(pendingUploadData);
        toast.success('Đã lưu ảnh thành công!');
      } finally {
        setIsUploading(false);
        setConfirmUploadOpen(false);
        setPendingFile(null);
        setPendingUploadData('');
        setInputUrl('');
      }
    } else {
      onChange?.(pendingUploadData);
      toast.success('Đã gắn liên kết ảnh thành công!');
      setConfirmUploadOpen(false);
      setPendingUploadData('');
      setInputUrl('');
    }
  };

  const handleCancelUpload = () => {
    if (isUploading) return;
    setConfirmUploadOpen(false);
    setPendingFile(null);
    setPendingUploadData('');
  };

  const handleConfirmDelete = () => {
    if (onChange) {
      onChange('');
      toast.success('Đã gỡ bỏ ảnh thành công');
    }
    setConfirmDeleteOpen(false);
  };

  return (
    <Box sx={{ width: '100%' }}>
      <Typography variant="subtitle2" sx={{ fontWeight: 700, mb: 0.8, color: 'text.primary' }}>
        {label} {required && <span style={{ color: '#ef4444' }}>*</span>}
      </Typography>

      {value ? (
        <Box
          sx={{
            display: 'flex',
            alignItems: 'center',
            gap: 2,
            p: 1.5,
            borderRadius: 2.5,
            border: isDark ? '1px solid rgba(16, 185, 129, 0.3)' : '1px solid #BBF7D0',
            bgcolor: isDark ? 'rgba(16, 185, 129, 0.08)' : '#F0FDF4',
          }}
        >
          <Box
            component="img"
            src={value}
            alt={label}
            onClick={() => setPreviewOpen(true)}
            sx={{
              width: 80,
              height: 60,
              objectFit: 'cover',
              borderRadius: 2,
              border: isDark ? '1px solid rgba(16, 185, 129, 0.4)' : '1px solid #86EFAC',
              cursor: 'pointer',
              transition: 'transform 0.15s ease',
              '&:hover': { transform: 'scale(1.05)' },
            }}
          />
          <Box sx={{ flex: 1, minWidth: 0 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.8, mb: 0.3 }}>
              <CheckCircleIcon sx={{ fontSize: 16, color: '#16A34A' }} />
              <Typography variant="body2" sx={{ fontWeight: 700, color: isDark ? '#4ade80' : '#166534' }}>
                Đã tải lên ảnh hợp lệ
              </Typography>
            </Box>
            <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
              Nhấp vào ảnh để phóng to xem chi tiết
            </Typography>
          </Box>
          <Box sx={{ display: 'flex', gap: 0.5 }}>
            <Tooltip title="Xem ảnh đầy đủ">
              <IconButton size="small" onClick={() => setPreviewOpen(true)} sx={{ color: '#0284C7' }}>
                <ViewIcon fontSize="small" />
              </IconButton>
            </Tooltip>
            <Tooltip title="Xóa ảnh và chọn lại">
              <IconButton size="small" onClick={() => setConfirmDeleteOpen(true)} sx={{ color: '#EF4444' }}>
                <DeleteIcon fontSize="small" />
              </IconButton>
            </Tooltip>
          </Box>
        </Box>
      ) : (
        <Box
          sx={{
            p: 2,
            borderRadius: 2.5,
            border: isDark ? '2px dashed rgba(255, 255, 255, 0.15)' : '2px dashed #CBD5E1',
            bgcolor: isDark ? 'rgba(255, 255, 255, 0.02)' : '#F8FAFC',
            textAlign: 'center',
            transition: 'all 0.2s',
            '&:hover': {
              borderColor: '#F97316',
              bgcolor: isDark ? 'rgba(249, 115, 22, 0.08)' : '#FFF7ED',
            },
          }}
        >
          <input
            type="file"
            ref={fileInputRef}
            hidden
            accept="image/*"
            onChange={handleFileChange}
          />
          <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 1 }}>
            <Box
              sx={{
                width: 44,
                height: 44,
                borderRadius: '50%',
                bgcolor: 'rgba(249, 115, 22, 0.1)',
                color: '#F97316',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
              }}
            >
              <UploadIcon sx={{ fontSize: 24 }} />
            </Box>
            <Box>
              <Button
                variant="contained"
                size="small"
                startIcon={<UploadIcon />}
                onClick={() => fileInputRef.current?.click()}
                sx={{
                  bgcolor: '#F97316',
                  '&:hover': { bgcolor: '#EA580C' },
                  borderRadius: 2,
                  textTransform: 'none',
                  fontWeight: 700,
                  fontSize: '0.82rem',
                  px: 2,
                }}
              >
                Tải ảnh từ thiết bị
              </Button>
              <Button
                size="small"
                startIcon={<LinkIcon />}
                onClick={() => setShowUrlInput(!showUrlInput)}
                sx={{
                  ml: 1,
                  textTransform: 'none',
                  fontSize: '0.8rem',
                  fontWeight: 600,
                  color: 'text.secondary',
                }}
              >
                {showUrlInput ? 'Ẩn nhập URL' : 'Dán link ảnh'}
              </Button>
            </Box>

            {showUrlInput && (
              <Box sx={{ width: '100%', maxWidth: 420, mt: 1 }}>
                <TextField
                  fullWidth
                  size="small"
                  placeholder={placeholder}
                  value={inputUrl}
                  onChange={(e) => setInputUrl(e.target.value)}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter') {
                      e.preventDefault();
                      handleApplyUrl(e.target.value);
                    }
                  }}
                  InputProps={{
                    endAdornment: (
                      <Button
                        size="small"
                        onClick={() => handleApplyUrl(inputUrl)}
                        disabled={!inputUrl.trim()}
                        sx={{ textTransform: 'none', fontWeight: 700, minWidth: 'auto', px: 1.5 }}
                      >
                        Áp dụng
                      </Button>
                    ),
                  }}
                  helperText="Nhập link ảnh và bấm 'Áp dụng' hoặc nhấn Enter để xác nhận tải"
                />
              </Box>
            )}

            <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block', mt: 0.5 }}>
              {helperText}
            </Typography>
          </Box>
        </Box>
      )}

      {/* Confirm Delete Image Dialog */}
      <ConfirmDialog
        open={confirmDeleteOpen}
        onClose={() => setConfirmDeleteOpen(false)}
        onConfirm={handleConfirmDelete}
        title="Xác nhận xóa ảnh"
        content={`Bạn có chắc chắn muốn xóa "${label}" không? Sau khi xóa bạn sẽ cần chọn lại ảnh mới.`}
        confirmText="Xóa ảnh"
        confirmColor="error"
        iconType="delete"
      />

      {/* Confirm Upload Image Dialog */}
      <Dialog
        open={confirmUploadOpen}
        onClose={handleCancelUpload}
        maxWidth="xs"
        fullWidth
      >
        <DialogTitle sx={{ fontWeight: 800, pb: 1, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Typography variant="subtitle1" fontWeight={800}>
            Xác nhận tải ảnh
          </Typography>
          <IconButton size="small" onClick={handleCancelUpload}>
            <CloseIcon fontSize="small" />
          </IconButton>
        </DialogTitle>
        <DialogContent dividers sx={{ textAlign: 'center', py: 2 }}>
          <Typography variant="body2" sx={{ color: 'text.secondary', mb: 2 }}>
            Bạn có muốn sử dụng hình ảnh này làm <b>{label}</b> không?
          </Typography>
          {pendingUploadData && (
            <Box
              component="img"
              src={pendingUploadData}
              alt="Xem trước ảnh tải lên"
              sx={{
                maxWidth: '100%',
                maxHeight: 220,
                objectFit: 'contain',
                borderRadius: 2,
                border: isDark ? '1px solid rgba(255, 255, 255, 0.12)' : '1px solid #E2E8F0',
                boxShadow: '0 2px 10px rgba(0,0,0,0.08)',
              }}
            />
          )}
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={handleCancelUpload} disabled={isUploading} color="inherit" sx={{ textTransform: 'none' }}>
            Hủy / Chọn lại
          </Button>
          <Button
            onClick={handleConfirmUpload}
            disabled={isUploading}
            variant="contained"
            startIcon={isUploading ? <CircularProgress size={16} sx={{ color: '#fff' }} /> : null}
            sx={{
              bgcolor: '#F97316',
              '&:hover': { bgcolor: '#EA580C' },
              textTransform: 'none',
              fontWeight: 700,
            }}
          >
            {isUploading ? 'Đang tải lên...' : 'Xác nhận tải lên'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Lightbox Preview Dialog */}
      <Dialog open={previewOpen} onClose={() => setPreviewOpen(false)} maxWidth="md">
        <DialogTitle sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', pb: 1 }}>
          <Typography variant="h6" sx={{ fontWeight: 800 }}>
            {label}
          </Typography>
          <IconButton onClick={() => setPreviewOpen(false)} size="small">
            <CloseIcon fontSize="small" />
          </IconButton>
        </DialogTitle>
        <DialogContent sx={{ p: 2, textAlign: 'center' }}>
          <Box
            component="img"
            src={value}
            alt={label}
            sx={{
              maxWidth: '100%',
              maxHeight: '75vh',
              objectFit: 'contain',
              borderRadius: 2,
              boxShadow: '0 4px 20px rgba(0,0,0,0.15)',
            }}
          />
        </DialogContent>
      </Dialog>
    </Box>
  );
};

export default ImageUploadField;
