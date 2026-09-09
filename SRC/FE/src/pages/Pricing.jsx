import React, { useState, useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Grid,
  TextField,
  Button,
  CircularProgress,
  Alert,
  InputAdornment,
  Divider,
} from '@mui/material';
import {
  Refresh as RefreshIcon,
  Save as SaveIcon,
} from '@mui/icons-material';
import toast from 'react-hot-toast';
import pricingService from '../services/pricingService';
import dispatchConfigService from '../services/dispatchConfigService';

export const Pricing = () => {
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);

  const [form, setForm] = useState({
    baseFare: '',
    pricePerKm: '',
    pricePerMinute: '',
  });

  const [simDistance, setSimDistance] = useState(5);
  const [simDuration, setSimDuration] = useState(15);

  // === Dispatch Config State (hoàn toàn tách biệt) ===
  const [dispatchLoading, setDispatchLoading] = useState(false);
  const [dispatchSaving, setDispatchSaving] = useState(false);
  const [dispatchForm, setDispatchForm] = useState({
    maxRideDistanceKm: '',
    maxFoodDeliveryDistanceKm: '',
  });

  const fetchPricingConfig = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await pricingService.getPricingConfig();
      if (data) {
        setForm({
          baseFare: data.baseFare ?? 0,
          pricePerKm: data.pricePerKm ?? 0,
          pricePerMinute: data.pricePerMinute ?? 0,
        });
      }
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Không thể tải cấu hình giá cước từ Backend';
      setError(msg);
      toast.error(msg);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPricingConfig();
    fetchDispatchConfig();
  }, []);

  // === Dispatch Config Handlers ===
  const fetchDispatchConfig = async () => {
    setDispatchLoading(true);
    try {
      const data = await dispatchConfigService.getDispatchConfig();
      if (data) {
        setDispatchForm({
          maxRideDistanceKm: data.maxRideDistanceKm ?? 8,
          maxFoodDeliveryDistanceKm: data.maxFoodDeliveryDistanceKm ?? 5.5,
        });
      }
    } catch (err) {
      toast.error('Không thể tải cấu hình bán kính điều phối');
    } finally {
      setDispatchLoading(false);
    }
  };

  const handleDispatchChange = (e) => {
    const { name, value } = e.target;
    setDispatchForm((prev) => ({
      ...prev,
      [name]: value === '' ? '' : Number(value),
    }));
  };

  const handleDispatchSave = async (e) => {
    e.preventDefault();
    if (dispatchForm.maxRideDistanceKm === '' || dispatchForm.maxFoodDeliveryDistanceKm === '') {
      toast.error('Vui lòng nhập đầy đủ các thông số bán kính');
      return;
    }
    setDispatchSaving(true);
    const saveToast = toast.loading('Đang lưu cấu hình bán kính vào Redis...');
    try {
      const res = await dispatchConfigService.updateDispatchConfig({
        maxRideDistanceKm: Number(dispatchForm.maxRideDistanceKm),
        maxFoodDeliveryDistanceKm: Number(dispatchForm.maxFoodDeliveryDistanceKm),
      });
      toast.dismiss(saveToast);
      toast.success(res.message || 'Cập nhật cấu hình bán kính thành công!');
    } catch (err) {
      toast.dismiss(saveToast);
      toast.error(err.response?.data?.message || 'Lỗi khi cập nhật cấu hình bán kính');
    } finally {
      setDispatchSaving(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({
      ...prev,
      [name]: value === '' ? '' : Number(value),
    }));
  };

  const handleSave = async (e) => {
    e.preventDefault();
    if (form.baseFare === '' || form.pricePerKm === '' || form.pricePerMinute === '') {
      toast.error('Vui lòng nhập đầy đủ các thông số giá cước');
      return;
    }

    setSaving(true);
    const saveToast = toast.loading('Đang lưu cấu hình giá cước vào Backend & Redis...');
    try {
      const payload = {
        baseFare: Number(form.baseFare),
        pricePerKm: Number(form.pricePerKm),
        pricePerMinute: Number(form.pricePerMinute),
      };

      const res = await pricingService.updatePricingConfig(payload);
      toast.dismiss(saveToast);
      toast.success(res.message || 'Cập nhật cấu hình giá cước thành công!');
      setError(null);
    } catch (err) {
      toast.dismiss(saveToast);
      const msg = err.response?.data?.message || err.message || 'Lỗi khi cập nhật cấu hình giá';
      setError(msg);
      toast.error(msg);
    } finally {
      setSaving(false);
    }
  };

  const calculatedFare =
    (Number(form.baseFare) || 0) +
    (Number(form.pricePerKm) || 0) * simDistance +
    (Number(form.pricePerMinute) || 0) * simDuration;

  return (
    <Box sx={{ width: '100%', maxWidth: '100%', overflowX: 'hidden' }} className="page-enter-animation">
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: { xs: 'flex-start', sm: 'center' }, mb: 2.5, flexDirection: { xs: 'column', sm: 'row' }, gap: 1.5 }}>
        <Box>
          <Typography variant="h5" sx={{ fontWeight: 700, fontFamily: '"Poppins", sans-serif' }}>
            Cấu Hình Giá Cước & Thuật Toán
          </Typography>
          <Typography variant="body2" sx={{ color: 'text.secondary', mt: 0.3, fontSize: { xs: '0.8rem', sm: '0.85rem' } }}>
            Thông số tính toán cước chuyến đi được lưu trữ trực tiếp trong Redis
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={<RefreshIcon />}
          onClick={fetchPricingConfig}
          disabled={loading}
          sx={{ bgcolor: '#008cff', '&:hover': { bgcolor: '#0070cc' }, borderRadius: 2, width: { xs: '100%', sm: 'auto' } }}
        >
          Làm Mới
        </Button>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 2.5 }}>
          {error}
        </Alert>
      )}

      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}>
          <CircularProgress size={36} />
        </Box>
      ) : (
        <Grid container spacing={2.5}>
          {/* Main Config Box */}
          <Grid item xs={12} md={7}>
            <Card sx={{ p: { xs: 2, sm: 3 }, height: '100%' }}>
              <Typography variant="h6" sx={{ fontWeight: 700, mb: 0.5, fontSize: '1.05rem' }}>
                Thông Số Bảng Giá Cơ Sở
              </Typography>
              <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block', mb: 2.5 }}>
                Áp dụng tính giá tự động khi khách hàng đặt cuốc xe
              </Typography>

              <Box component="form" onSubmit={handleSave} sx={{ display: 'flex', flexDirection: 'column', gap: 2.2 }}>
                <Box>
                  <Typography variant="body2" sx={{ fontWeight: 600, mb: 0.8 }}>
                    Giá mở cửa cố định (Base Fare)
                  </Typography>
                  <TextField
                    name="baseFare"
                    type="number"
                    value={form.baseFare}
                    onChange={handleChange}
                    required
                    fullWidth
                    slotProps={{
                      input: {
                        endAdornment: <InputAdornment position="end">VND</InputAdornment>,
                      },
                    }}
                    helperText="Khoản tiền tối thiểu cho mỗi cuốc xe"
                  />
                </Box>

                <Box>
                  <Typography variant="body2" sx={{ fontWeight: 600, mb: 0.8 }}>
                    Đơn giá tính theo khoảng cách (Price Per Km)
                  </Typography>
                  <TextField
                    name="pricePerKm"
                    type="number"
                    value={form.pricePerKm}
                    onChange={handleChange}
                    required
                    fullWidth
                    slotProps={{
                      input: {
                        endAdornment: <InputAdornment position="end">VND / km</InputAdornment>,
                      },
                    }}
                    helperText="Đơn giá nhân với tổng số km di chuyển"
                  />
                </Box>

                <Box>
                  <Typography variant="body2" sx={{ fontWeight: 600, mb: 0.8 }}>
                    Đơn giá tính theo thời gian (Price Per Minute)
                  </Typography>
                  <TextField
                    name="pricePerMinute"
                    type="number"
                    value={form.pricePerMinute}
                    onChange={handleChange}
                    required
                    fullWidth
                    slotProps={{
                      input: {
                        endAdornment: <InputAdornment position="end">VND / phút</InputAdornment>,
                      },
                    }}
                    helperText="Đơn giá nhân với tổng thời gian di chuyển thực tế"
                  />
                </Box>

                <Button
                  type="submit"
                  variant="contained"
                  startIcon={saving ? <CircularProgress size={18} sx={{ color: '#ffffff' }} /> : <SaveIcon />}
                  disabled={saving}
                  sx={{
                    mt: 1,
                    py: 1.2,
                    fontWeight: 700,
                    width: { xs: '100%', sm: 'auto' },
                    alignSelf: { xs: 'stretch', sm: 'flex-start' },
                    px: 3.5,
                    borderRadius: 2,
                    bgcolor: '#008cff',
                    '&:hover': { bgcolor: '#0070cc' },
                  }}
                >
                  {saving ? 'Đang Lưu...' : 'Lưu Cấu Hình'}
                </Button>
              </Box>
            </Card>
          </Grid>

          {/* Calculator Preview Box */}
          <Grid item xs={12} md={5}>
            <Card sx={{ p: { xs: 2, sm: 3 }, height: '100%' }}>
              <Typography variant="h6" sx={{ fontWeight: 700, mb: 0.5, fontSize: '1.05rem' }}>
                Mô Phỏng Cước Thực Tế
              </Typography>
              <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block', mb: 3 }}>
                Kiểm tra công thức tính giá cước tức thời
              </Typography>

              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.2, mb: 3 }}>
                <Box>
                  <Typography variant="body2" sx={{ fontWeight: 600, mb: 0.6 }}>
                    Quãng đường dự kiến (km)
                  </Typography>
                  <TextField
                    type="number"
                    value={simDistance}
                    onChange={(e) => setSimDistance(Math.max(0, Number(e.target.value)))}
                    fullWidth
                    slotProps={{ input: { endAdornment: <InputAdornment position="end">km</InputAdornment> } }}
                  />
                </Box>
                <Box>
                  <Typography variant="body2" sx={{ fontWeight: 600, mb: 0.6 }}>
                    Thời gian dự kiến (phút)
                  </Typography>
                  <TextField
                    type="number"
                    value={simDuration}
                    onChange={(e) => setSimDuration(Math.max(0, Number(e.target.value)))}
                    fullWidth
                    slotProps={{ input: { endAdornment: <InputAdornment position="end">phút</InputAdornment> } }}
                  />
                </Box>
              </Box>

              <Divider sx={{ my: 2.5 }} />

              <Box
                sx={{
                  p: 2.5,
                  borderRadius: 2,
                  bgcolor: 'rgba(0, 140, 255, 0.08)',
                  border: '1px solid rgba(0, 140, 255, 0.25)',
                }}
              >
                <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600, display: 'block', mb: 0.5 }}>
                  Tổng tiền cước người dùng thanh toán:
                </Typography>
                <Typography variant="h4" sx={{ fontWeight: 800, color: '#008cff', mb: 0.8, letterSpacing: '-0.02em' }}>
                  {calculatedFare.toLocaleString('vi-VN')} đ
                </Typography>
                <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block', fontSize: '0.78rem' }}>
                  Công thức: {Number(form.baseFare).toLocaleString()}đ + ({Number(form.pricePerKm).toLocaleString()}đ × {simDistance}km) + ({Number(form.pricePerMinute).toLocaleString()}đ × {simDuration}p)
                </Typography>
              </Box>
            </Card>
          </Grid>
        </Grid>
      )}

      {/* === CẤU HÌNH BÁN KÍNH ĐIỀU PHỐI === */}
      <Divider sx={{ my: 4 }} />

      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: { xs: 'flex-start', sm: 'center' }, mb: 2.5, flexDirection: { xs: 'column', sm: 'row' }, gap: 1.5 }}>
        <Box>
          <Typography variant="h5" sx={{ fontWeight: 700, fontFamily: '"Poppins", sans-serif' }}>
            Cấu Hình Bán Kính Điều Phối
          </Typography>
          <Typography variant="body2" sx={{ color: 'text.secondary', mt: 0.3, fontSize: { xs: '0.8rem', sm: '0.85rem' } }}>
            Bán kính tối đa (km) mà hệ thống quét tìm tài xế khi có cuốc xe hoặc đơn giao đồ ăn — lưu trữ trong Redis
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={<RefreshIcon />}
          onClick={fetchDispatchConfig}
          disabled={dispatchLoading}
          sx={{ bgcolor: '#008cff', '&:hover': { bgcolor: '#0070cc' }, borderRadius: 2, width: { xs: '100%', sm: 'auto' } }}
        >
          Làm Mới
        </Button>
      </Box>

      {dispatchLoading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 6 }}>
          <CircularProgress size={36} />
        </Box>
      ) : (
        <Grid container spacing={2.5}>
          <Grid item xs={12} md={7}>
            <Card sx={{ p: { xs: 2, sm: 3 }, height: '100%' }}>
              <Typography variant="h6" sx={{ fontWeight: 700, mb: 0.5, fontSize: '1.05rem' }}>
                Thông Số Bán Kính Quét Tài Xế
              </Typography>
              <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block', mb: 2.5 }}>
                Admin có thể thay đổi bán kính theo nhu cầu thực tế, hiệu lực ngay lập tức không cần restart
              </Typography>

              <Box component="form" onSubmit={handleDispatchSave} sx={{ display: 'flex', flexDirection: 'column', gap: 2.2 }}>
                <Box>
                  <Typography variant="body2" sx={{ fontWeight: 600, mb: 0.8 }}>
                    🚕 Bán kính tối đa — Cuốc xe (Ride-Hailing)
                  </Typography>
                  <TextField
                    name="maxRideDistanceKm"
                    type="number"
                    value={dispatchForm.maxRideDistanceKm}
                    onChange={handleDispatchChange}
                    required
                    fullWidth
                    slotProps={{
                      input: {
                        endAdornment: <InputAdornment position="end">km</InputAdornment>,
                      },
                      htmlInput: { step: 'any', min: 1, max: 50 },
                    }}
                    helperText="Hệ thống sẽ quét tìm tài xế trong bán kính này khi khách hàng đặt xe"
                  />
                </Box>

                <Box>
                  <Typography variant="body2" sx={{ fontWeight: 600, mb: 0.8 }}>
                    🍲 Bán kính tối đa — Giao đồ ăn (Food Delivery)
                  </Typography>
                  <TextField
                    name="maxFoodDeliveryDistanceKm"
                    type="number"
                    value={dispatchForm.maxFoodDeliveryDistanceKm}
                    onChange={handleDispatchChange}
                    required
                    fullWidth
                    slotProps={{
                      input: {
                        endAdornment: <InputAdornment position="end">km</InputAdornment>,
                      },
                      htmlInput: { step: 'any', min: 1, max: 50 },
                    }}
                    helperText="Hệ thống sẽ quét tìm tài xế giao hàng quanh nhà hàng trong bán kính này"
                  />
                </Box>

                <Button
                  type="submit"
                  variant="contained"
                  startIcon={dispatchSaving ? <CircularProgress size={18} sx={{ color: '#ffffff' }} /> : <SaveIcon />}
                  disabled={dispatchSaving}
                  sx={{
                    mt: 1,
                    py: 1.2,
                    fontWeight: 700,
                    width: { xs: '100%', sm: 'auto' },
                    alignSelf: { xs: 'stretch', sm: 'flex-start' },
                    px: 3.5,
                    borderRadius: 2,
                    bgcolor: '#008cff',
                    '&:hover': { bgcolor: '#0070cc' },
                  }}
                >
                  {dispatchSaving ? 'Đang Lưu...' : 'Lưu Cấu Hình Bán Kính'}
                </Button>
              </Box>
            </Card>
          </Grid>

          <Grid item xs={12} md={5}>
            <Card sx={{ p: { xs: 2, sm: 3 }, height: '100%' }}>
              <Typography variant="h6" sx={{ fontWeight: 700, mb: 0.5, fontSize: '1.05rem' }}>
                Hướng Dẫn Sử Dụng
              </Typography>
              <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block', mb: 2.5 }}>
                Giải thích ý nghĩa các thông số bán kính điều phối
              </Typography>

              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                <Box sx={{ p: 2, borderRadius: 2, bgcolor: 'rgba(0, 140, 255, 0.08)', border: '1px solid rgba(0, 140, 255, 0.25)' }}>
                  <Typography variant="body2" sx={{ fontWeight: 700, mb: 0.5, color: '#008cff' }}>🚕 Cuốc xe</Typography>
                  <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block' }}>
                    Khi khách đặt xe, hệ thống quét tìm tài xế rảnh trong bán kính tối đa này tính từ <b>điểm đón khách</b>. Quét theo nấc: 3km → 5km → max.
                  </Typography>
                </Box>
                <Box sx={{ p: 2, borderRadius: 2, bgcolor: 'rgba(255, 152, 0, 0.08)', border: '1px solid rgba(255, 152, 0, 0.25)' }}>
                  <Typography variant="body2" sx={{ fontWeight: 700, mb: 0.5, color: '#f57c00' }}>🍲 Giao đồ ăn</Typography>
                  <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block' }}>
                    Khi nhà hàng bấm tìm tài xế, hệ thống quét tìm tài xế rảnh trong bán kính tối đa này tính từ <b>vị trí nhà hàng</b>.
                  </Typography>
                </Box>
                <Alert severity="info" sx={{ fontSize: '0.8rem' }}>
                  Thay đổi có hiệu lực <b>ngay lập tức</b> cho tất cả các cuốc xe / đơn giao đồ ăn mới, không cần khởi động lại hệ thống.
                </Alert>
              </Box>
            </Card>
          </Grid>
        </Grid>
      )}
    </Box>
  );
};

export default Pricing;
