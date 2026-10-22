import { useEffect, useState } from 'react';
import { Box, Button, TextField, ToggleButton, ToggleButtonGroup, Typography } from '@mui/material';
import { CloudUpload, Link } from '@mui/icons-material';

export default function MenuItemImageField({ value, file, onChange, onFileChange, disabled }) {
  const [mode, setMode] = useState('upload');
  const [preview, setPreview] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    if (!file) {
      setPreview('');
      return;
    }
    const url = URL.createObjectURL(file);
    setPreview(url);
    return () => URL.revokeObjectURL(url);
  }, [file]);

  const selectFile = (event) => {
    const selected = event.target.files?.[0];
    event.target.value = '';
    if (!selected) return;
    if (!['image/jpeg', 'image/png', 'image/webp'].includes(selected.type)) {
      setError('Vui lòng chọn ảnh JPG, PNG hoặc WebP.');
      return;
    }
    if (selected.size > 5 * 1024 * 1024) {
      setError('Dung lượng ảnh tối đa là 5MB.');
      return;
    }
    setError('');
    onFileChange(selected);
  };

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
      <Typography variant="subtitle2" fontWeight={700}>Ảnh món ăn</Typography>
      <ToggleButtonGroup
        exclusive fullWidth size="small" value={mode} disabled={disabled}
        aria-label="Cách chọn ảnh món ăn"
        onChange={(_, next) => {
          if (!next) return;
          setMode(next);
          setError('');
          if (next === 'url') onFileChange(null);
        }}
      >
        <ToggleButton value="upload" sx={{ gap: 1, textTransform: 'none' }}><CloudUpload fontSize="small" />Tải ảnh lên</ToggleButton>
        <ToggleButton value="url" sx={{ gap: 1, textTransform: 'none' }}><Link fontSize="small" />Dán link ảnh</ToggleButton>
      </ToggleButtonGroup>
      {mode === 'upload' ? (
        <Box sx={{ p: 2, border: '2px dashed #CBD5E1', borderRadius: 2, textAlign: 'center', bgcolor: '#F8FAFC' }}>
          <Button component="label" variant="outlined" disabled={disabled} startIcon={<CloudUpload />} sx={{ textTransform: 'none' }}>
            {file || value ? 'Chọn ảnh thay thế' : 'Chọn ảnh từ máy'}
            <input hidden type="file" accept="image/jpeg,image/png,image/webp" disabled={disabled} onChange={selectFile} />
          </Button>
          <Typography variant="caption" display="block" sx={{ mt: 1, overflowWrap: 'anywhere' }}>
            {file ? file.name : 'JPG, PNG, WebP • Tối đa 5MB'}
          </Typography>
        </Box>
      ) : (
        <TextField
          label="Đường dẫn ảnh món ăn" placeholder="https://..." type="url" fullWidth
          value={value} disabled={disabled} onChange={(event) => onChange(event.target.value)}
          inputProps={{ pattern: 'https?://.+' }} helperText="Dán link ảnh bắt đầu bằng http:// hoặc https://"
        />
      )}
      {error && <Typography color="error" variant="caption" role="alert">{error}</Typography>}
      {(preview || value) && (
        <Box>
          <Box component="img" src={preview || value} alt="Xem trước ảnh món ăn" sx={{ width: '100%', height: 150, objectFit: 'contain', borderRadius: 2, bgcolor: '#F1F5F9' }} />
          <Button size="small" color="error" disabled={disabled} onClick={() => { onFileChange(null); onChange(''); setError(''); }}>Xóa ảnh</Button>
        </Box>
      )}
    </Box>
  );
}
