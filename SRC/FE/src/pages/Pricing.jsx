import { useState, useEffect, useCallback } from 'react';
import {
  Box,
  Card,
  Typography,
  Grid,
  TextField,
  Button,
  CircularProgress,
  Alert,
  InputAdornment,
  Divider,
  Tabs,
  Tab,
  Paper,
  Chip,
  MenuItem,
  Select,
  FormControl,
  Stack,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  IconButton,
} from '@mui/material';
import {
  Refresh as RefreshIcon,
  Save as SaveIcon,
  Calculate as PricingIcon,
  NearMe as DispatchIcon,
  DirectionsCar as CarIcon,
  LocalShipping as DeliveryIcon,
  Route as RouteIcon,
  ElectricBolt as SurgeIcon,
  Close as CloseIcon,
  Edit as EditIcon,
  TwoWheeler as BikeIcon,
  RestartAlt as ResetIcon,
} from '@mui/icons-material';
import toast from 'react-hot-toast';
import pricingService from '../services/pricingService';
import dispatchConfigService from '../services/dispatchConfigService';
import { useColorMode } from '../context/ThemeContext';

export const Pricing = () => {
  const { mode } = useColorMode();
  const isDark = mode === 'dark';
  const [activeTab, setActiveTab] = useState(0);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);

  // Form cấu hình giá cước
  const [form, setForm] = useState({
    // 1. Cước cơ bản
    baseFare: 15000,
    pricePerKm: 15000,
    pricePerMinute: 1000,
    // 2. Hệ số loại xe
    bikeMultiplier: 1.0,
    car4SeatMultiplier: 1.5,
    car7SeatMultiplier: 2.0,
    // 3. Phí giao đồ ăn
    deliveryBaseFare: 15000,
    deliveryBaseKm: 2.0,
    deliveryPricePerKm: 5000,
    // 4. OSRM Routing Engine & Fallback
    haversineFallbackMultiplier: 1.35,
    minDistanceBypassMeters: 50,
    // 5. Hệ số tăng giá cao điểm (Surge)
    surgeLightMultiplier: 1.2,
    surgeModerateMultiplier: 1.5,
    surgeSevereMultiplier: 2.0,
  });

  // Modal State: null | 'BASE_RIDE' | 'VEHICLE' | 'FOOD_DELIVERY' | 'ROUTES_FALLBACK' | 'SURGE'
  const [activeModal, setActiveModal] = useState(null);
  const [backupForm, setBackupForm] = useState(null);

  // State mô phỏng
  const [simService, setSimService] = useState('RIDE'); // 'RIDE' | 'DELIVERY'
  const [simVehicle, setSimVehicle] = useState('BIKE'); // 'BIKE' | 'CAR_4_SEAT' | 'CAR_7_SEAT'
  const [simSurge, setSimSurge] = useState('NORMAL'); // 'NORMAL' | 'LIGHT' | 'MODERATE' | 'SEVERE'
  const [simDistance, setSimDistance] = useState(5);
  const [simDuration, setSimDuration] = useState(15);

  // === Dispatch Config State ===
  const [dispatchLoading, setDispatchLoading] = useState(true);
  const [dispatchSaving, setDispatchSaving] = useState(false);
  const [dispatchForm, setDispatchForm] = useState({
    maxRideDistanceKm: 8,
    maxFoodDeliveryDistanceKm: 5.5,
  });

  const fetchPricingConfig = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await pricingService.getPricingConfig();
      if (data) {
        setForm({
          baseFare: data.baseFare ?? 15000,
          pricePerKm: data.pricePerKm ?? 15000,
          pricePerMinute: data.pricePerMinute ?? 1000,
          bikeMultiplier: data.bikeMultiplier ?? 1.0,
          car4SeatMultiplier: data.car4SeatMultiplier ?? 1.5,
          car7SeatMultiplier: data.car7SeatMultiplier ?? 2.0,
          deliveryBaseFare: data.deliveryBaseFare ?? 15000,
          deliveryBaseKm: data.deliveryBaseKm ?? 2.0,
          deliveryPricePerKm: data.deliveryPricePerKm ?? 5000,
          haversineFallbackMultiplier: data.haversineFallbackMultiplier ?? 1.35,
          minDistanceBypassMeters: data.minDistanceBypassMeters ?? 50,
          surgeLightMultiplier: data.surgeLightMultiplier ?? 1.2,
          surgeModerateMultiplier: data.surgeModerateMultiplier ?? 1.5,
          surgeSevereMultiplier: data.surgeSevereMultiplier ?? 2.0,
        });
      }
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Không thể tải cấu hình giá cước từ Backend';
      setError(msg);
      toast.error(msg);
    } finally {
      setLoading(false);
    }
  }, []);

  const fetchDispatchConfig = useCallback(async () => {
    setDispatchLoading(true);
    try {
      const data = await dispatchConfigService.getDispatchConfig();
      if (data) {
        setDispatchForm({
          maxRideDistanceKm: data.maxRideDistanceKm ?? 8,
          maxFoodDeliveryDistanceKm: data.maxFoodDeliveryDistanceKm ?? 5.5,
        });
      }
    } catch {
      toast.error('Không thể tải cấu hình bán kính điều phối');
    } finally {
      setDispatchLoading(false);
    }
  }, []);

  useEffect(() => {
    let active = true;

    async function init() {
      try {
        const data = await pricingService.getPricingConfig();
        if (active && data) {
          setForm({
            baseFare: data.baseFare ?? 15000,
            pricePerKm: data.pricePerKm ?? 15000,
            pricePerMinute: data.pricePerMinute ?? 1000,
            bikeMultiplier: data.bikeMultiplier ?? 1.0,
            car4SeatMultiplier: data.car4SeatMultiplier ?? 1.5,
            car7SeatMultiplier: data.car7SeatMultiplier ?? 2.0,
            deliveryBaseFare: data.deliveryBaseFare ?? 15000,
            deliveryBaseKm: data.deliveryBaseKm ?? 2.0,
            deliveryPricePerKm: data.deliveryPricePerKm ?? 5000,
            haversineFallbackMultiplier: data.haversineFallbackMultiplier ?? 1.35,
            minDistanceBypassMeters: data.minDistanceBypassMeters ?? 50,
            surgeLightMultiplier: data.surgeLightMultiplier ?? 1.2,
            surgeModerateMultiplier: data.surgeModerateMultiplier ?? 1.5,
            surgeSevereMultiplier: data.surgeSevereMultiplier ?? 2.0,
          });
        }
      } catch (err) {
        if (active) {
          const msg = err.response?.data?.message || err.message || 'Không thể tải cấu hình giá cước từ Backend';
          setError(msg);
          toast.error(msg);
        }
      } finally {
        if (active) setLoading(false);
      }

      try {
        const data = await dispatchConfigService.getDispatchConfig();
        if (active && data) {
          setDispatchForm({
            maxRideDistanceKm: data.maxRideDistanceKm ?? 8,
            maxFoodDeliveryDistanceKm: data.maxFoodDeliveryDistanceKm ?? 5.5,
          });
        }
      } catch {
        if (active) toast.error('Không thể tải cấu hình bán kính điều phối');
      } finally {
        if (active) setDispatchLoading(false);
      }
    }

    init();
    return () => {
      active = false;
    };
  }, []);

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

  const openModal = (type) => {
    setBackupForm({ ...form });
    setActiveModal(type);
  };

  const closeModal = () => {
    if (backupForm) {
      setForm(backupForm);
    }
    setActiveModal(null);
    setBackupForm(null);
  };

  const handleSaveModal = async () => {
    setSaving(true);
    const saveToast = toast.loading('Đang lưu cấu hình giá cước vào Backend & Redis...');
    try {
      const payload = {
        baseFare: Number(form.baseFare),
        pricePerKm: Number(form.pricePerKm),
        pricePerMinute: Number(form.pricePerMinute),
        bikeMultiplier: Number(form.bikeMultiplier),
        car4SeatMultiplier: Number(form.car4SeatMultiplier),
        car7SeatMultiplier: Number(form.car7SeatMultiplier),
        deliveryBaseFare: Number(form.deliveryBaseFare),
        deliveryBaseKm: Number(form.deliveryBaseKm),
        deliveryPricePerKm: Number(form.deliveryPricePerKm),
        haversineFallbackMultiplier: Number(form.haversineFallbackMultiplier),
        minDistanceBypassMeters: Number(form.minDistanceBypassMeters),
        surgeLightMultiplier: Number(form.surgeLightMultiplier),
        surgeModerateMultiplier: Number(form.surgeModerateMultiplier),
        surgeSevereMultiplier: Number(form.surgeSevereMultiplier),
      };

      const res = await pricingService.updatePricingConfig(payload);
      toast.dismiss(saveToast);
      toast.success(res.message || 'Cập nhật cấu hình thành công!');
      setError(null);
      setActiveModal(null);
      setBackupForm(null);
    } catch (err) {
      toast.dismiss(saveToast);
      const msg = err.response?.data?.message || err.message || 'Lỗi khi cập nhật cấu hình';
      setError(msg);
      toast.error(msg);
    } finally {
      setSaving(false);
    }
  };

  const handleResetDefault = () => {
    setForm({
      baseFare: 15000,
      pricePerKm: 15000,
      pricePerMinute: 1000,
      bikeMultiplier: 1.0,
      car4SeatMultiplier: 1.5,
      car7SeatMultiplier: 2.0,
      deliveryBaseFare: 15000,
      deliveryBaseKm: 2.0,
      deliveryPricePerKm: 5000,
      haversineFallbackMultiplier: 1.35,
      minDistanceBypassMeters: 50,
      surgeLightMultiplier: 1.2,
      surgeModerateMultiplier: 1.5,
      surgeSevereMultiplier: 2.0,
    });
    toast('Đã nạp bộ thông số mặc định (hãy mở modal để lưu lại)', { icon: 'ℹ️' });
  };

  // Tính toán mô phỏng cước
  const getSimulatedVehicleMultiplier = () => {
    if (simVehicle === 'CAR_4_SEAT') return Number(form.car4SeatMultiplier) || 1.5;
    if (simVehicle === 'CAR_7_SEAT') return Number(form.car7SeatMultiplier) || 2.0;
    return Number(form.bikeMultiplier) || 1.0;
  };

  const getSimulatedSurgeMultiplier = () => {
    if (simSurge === 'LIGHT') return Number(form.surgeLightMultiplier) || 1.2;
    if (simSurge === 'MODERATE') return Number(form.surgeModerateMultiplier) || 1.5;
    if (simSurge === 'SEVERE') return Number(form.surgeSevereMultiplier) || 2.0;
    return 1.0;
  };

  const calculateSimulatedFare = () => {
    if (simService === 'DELIVERY') {
      const bKm = Number(form.deliveryBaseKm) || 2.0;
      const bFare = Number(form.deliveryBaseFare) || 15000;
      const pKm = Number(form.deliveryPricePerKm) || 5000;
      let total = bFare;
      if (simDistance > bKm) {
        total += (simDistance - bKm) * pKm;
      }
      return Math.round(total / 1000) * 1000;
    }

    const vMult = getSimulatedVehicleMultiplier();
    const sMult = getSimulatedSurgeMultiplier();
    const bFare = (Number(form.baseFare) || 0) * vMult;
    const dFare = (Number(form.pricePerKm) || 0) * simDistance * vMult;
    const tFare = (Number(form.pricePerMinute) || 0) * simDuration * vMult;
    const raw = bFare + dFare + tFare;
    return Math.round((raw * sMult) / 1000) * 1000;
  };

  const calculatedFare = calculateSimulatedFare();

  return (
    <Box sx={{ width: '100%', maxWidth: '100%', overflowX: 'hidden' }} className="page-enter-animation">
      {/* Top Header */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: { xs: 'flex-start', sm: 'center' }, mb: 2.5, flexDirection: { xs: 'column', sm: 'row' }, gap: 1.5 }}>
        <Box>
          <Typography variant="h5" sx={{ fontWeight: 700, fontFamily: '"Poppins", sans-serif' }}>
            Cấu Hình Hệ Thống
          </Typography>
          <Typography variant="body2" sx={{ color: 'text.secondary', mt: 0.3, fontSize: { xs: '0.8rem', sm: '0.85rem' } }}>
            Quản lý bảng giá cước, hệ số xe, biểu phí ship đồ ăn, OSRM Routing Engine và bán kính điều phối trong Redis theo thời gian thực
          </Typography>
        </Box>
        <Stack direction="row" spacing={1.5} sx={{ width: { xs: '100%', sm: 'auto' } }}>
          <Button
            variant="outlined"
            startIcon={<ResetIcon />}
            onClick={handleResetDefault}
            disabled={activeTab === 0 ? loading : dispatchLoading}
            sx={{ borderRadius: 2, flex: { xs: 1, sm: 'none' }, fontSize: { xs: '0.78rem', sm: '0.875rem' }, py: { xs: 0.8, sm: 1 } }}
          >
            Khôi Phục Mặc Định
          </Button>
          <Button
            variant="contained"
            startIcon={<RefreshIcon />}
            onClick={activeTab === 0 ? fetchPricingConfig : fetchDispatchConfig}
            disabled={activeTab === 0 ? loading : dispatchLoading}
            sx={{ bgcolor: '#008cff', '&:hover': { bgcolor: '#0070cc' }, borderRadius: 2, flex: { xs: 1, sm: 'none' }, fontSize: { xs: '0.78rem', sm: '0.875rem' }, py: { xs: 0.8, sm: 1 } }}
          >
            Làm Mới
          </Button>
        </Stack>
      </Box>

      {/* 2 Tabs chính cân xứng, hiện đại */}
      <Paper
        elevation={0}
        sx={{
          borderRadius: 3,
          mb: 3,
          p: 0.6,
          bgcolor: isDark ? 'rgba(255, 255, 255, 0.05)' : '#f1f5f9',
          border: 1,
          borderColor: isDark ? 'rgba(255, 255, 255, 0.1)' : 'rgba(0, 0, 0, 0.06)',
        }}
      >
        <Tabs
          value={activeTab}
          onChange={(_, val) => setActiveTab(val)}
          variant="fullWidth"
          TabIndicatorProps={{ style: { display: 'none' } }}
          sx={{
            minHeight: { xs: 46, sm: 52 },
            '& .MuiTabs-flexContainer': {
              gap: 1,
            },
            '& .MuiTab-root': {
              flex: 1,
              borderRadius: 2.5,
              textTransform: 'none',
              fontWeight: 700,
              fontSize: { xs: '0.85rem', sm: '0.96rem' },
              minHeight: { xs: 46, sm: 52 },
              color: 'text.secondary',
              py: { xs: 1.1, sm: 1.4 },
              px: { xs: 1.5, sm: 3 },
              transition: 'all 0.25s cubic-bezier(0.4, 0, 0.2, 1)',
              display: 'flex',
              flexDirection: 'row',
              alignItems: 'center',
              justifyContent: 'center',
              gap: 1.2,
              '&:hover:not(.Mui-selected)': {
                bgcolor: isDark ? 'rgba(255, 255, 255, 0.04)' : 'rgba(255, 255, 255, 0.6)',
                color: 'text.primary',
              },
              '&.Mui-selected': {
                bgcolor: isDark ? '#1e293b' : '#ffffff',
                color: '#008cff',
                boxShadow: isDark
                  ? '0 4px 14px rgba(0, 0, 0, 0.4)'
                  : '0 2px 10px rgba(0, 0, 0, 0.08), 0 1px 3px rgba(0, 0, 0, 0.05)',
              },
            },
          }}
        >
          <Tab
            icon={<PricingIcon sx={{ fontSize: { xs: 19, sm: 22 } }} />}
            iconPosition="start"
            label="Bảng Giá Cước & Thuật Toán Động"
          />
          <Tab
            icon={<DispatchIcon sx={{ fontSize: { xs: 19, sm: 22 } }} />}
            iconPosition="start"
            label="Cấu Hình Bán Kính Điều Phối"
          />
        </Tabs>
      </Paper>

      {/* TAB 1: Cấu hình giá cước & thuật toán */}
      {activeTab === 0 && (
        <Box>
          {error && (
            <Alert severity="error" sx={{ mb: 2.5, borderRadius: 2 }}>
              {error}
            </Alert>
          )}

          {loading ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}>
              <CircularProgress size={40} />
            </Box>
          ) : (
            <Grid container spacing={2.5} sx={{ width: '100%' }}>
              {/* Cột trái: 5 Danh Mục Dạng Thẻ Gọn Gàng (Click để mở Modal) */}
              <Grid size={{ xs: 12, lg: 7 }}>
                <Stack spacing={2}>
                  {/* Mục 1: Bảng giá chuyến xe cơ bản */}
                  <Card
                    onClick={() => openModal('BASE_RIDE')}
                    sx={{
                      p: { xs: 1.8, sm: 2.2 },
                      borderRadius: 2.5,
                      border: '1px solid',
                      borderColor: 'divider',
                      cursor: 'pointer',
                      transition: 'all 0.22s ease-in-out',
                      '&:hover': {
                        transform: 'translateY(-2px)',
                        boxShadow: '0 8px 24px rgba(0, 140, 255, 0.12)',
                        borderColor: '#008cff',
                      },
                    }}
                  >
                    <Box sx={{ display: 'flex', flexDirection: { xs: 'column', sm: 'row' }, alignItems: { xs: 'flex-start', sm: 'center' }, justifyContent: 'space-between', gap: { xs: 1.2, sm: 1.5 }, mb: 1.5 }}>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, width: { xs: '100%', sm: 'auto' }, flex: 1 }}>
                        <Box
                          sx={{
                            width: { xs: 38, sm: 42 },
                            height: { xs: 38, sm: 42 },
                            minWidth: { xs: 38, sm: 42 },
                            borderRadius: 2,
                            bgcolor: 'rgba(0, 140, 255, 0.1)',
                            color: '#008cff',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                          }}
                        >
                          <PricingIcon sx={{ fontSize: { xs: 20, sm: 22 } }} />
                        </Box>
                        <Box sx={{ flex: 1, minWidth: 0 }}>
                          <Typography variant="subtitle1" sx={{ fontWeight: 700, lineHeight: 1.25, fontSize: { xs: '0.92rem', sm: '1rem' } }}>
                            1. Bảng Giá Chuyến Xe Cơ Bản
                          </Typography>
                          <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block', mt: 0.3, fontSize: { xs: '0.74rem', sm: '0.78rem' } }}>
                            Mức giá chuẩn áp dụng cho xe máy (BIKE) làm mốc quy chiếu
                          </Typography>
                        </Box>
                      </Box>
                      <Button
                        size="small"
                        variant="outlined"
                        startIcon={<EditIcon sx={{ fontSize: 16 }} />}
                        sx={{
                          borderRadius: 1.8,
                          textTransform: 'none',
                          fontWeight: 600,
                          fontSize: { xs: '0.78rem', sm: '0.82rem' },
                          py: { xs: 0.4, sm: 0.5 },
                          px: 1.5,
                          alignSelf: { xs: 'flex-end', sm: 'center' },
                          flexShrink: 0,
                        }}
                      >
                        Chỉnh sửa
                      </Button>
                    </Box>
                    <Stack direction="row" spacing={0.8} flexWrap="wrap" useFlexGap sx={{ mt: 1 }}>
                      <Chip
                        size="small"
                        label={`Mở cửa: ${Number(form.baseFare).toLocaleString()} đ`}
                        sx={{
                          bgcolor: 'rgba(0, 140, 255, 0.08)',
                          fontWeight: 600,
                          color: '#008cff',
                          fontSize: { xs: '0.74rem', sm: '0.8rem' },
                          height: 'auto',
                          py: 0.4,
                          maxWidth: '100%',
                          '& .MuiChip-label': { px: 1, whiteSpace: 'normal', wordBreak: 'break-word' },
                        }}
                      />
                      <Chip
                        size="small"
                        label={`Giá km: ${Number(form.pricePerKm).toLocaleString()} đ/km`}
                        sx={{
                          bgcolor: 'rgba(0, 140, 255, 0.08)',
                          fontWeight: 600,
                          color: '#008cff',
                          fontSize: { xs: '0.74rem', sm: '0.8rem' },
                          height: 'auto',
                          py: 0.4,
                          maxWidth: '100%',
                          '& .MuiChip-label': { px: 1, whiteSpace: 'normal', wordBreak: 'break-word' },
                        }}
                      />
                      <Chip
                        size="small"
                        label={`Giá phút: ${Number(form.pricePerMinute).toLocaleString()} đ/p`}
                        sx={{
                          bgcolor: 'rgba(0, 140, 255, 0.08)',
                          fontWeight: 600,
                          color: '#008cff',
                          fontSize: { xs: '0.74rem', sm: '0.8rem' },
                          height: 'auto',
                          py: 0.4,
                          maxWidth: '100%',
                          '& .MuiChip-label': { px: 1, whiteSpace: 'normal', wordBreak: 'break-word' },
                        }}
                      />
                    </Stack>
                  </Card>

                  {/* Mục 2: Hệ số loại xe */}
                  <Card
                    onClick={() => openModal('VEHICLE')}
                    sx={{
                      p: { xs: 1.8, sm: 2.2 },
                      borderRadius: 2.5,
                      border: '1px solid',
                      borderColor: 'divider',
                      cursor: 'pointer',
                      transition: 'all 0.22s ease-in-out',
                      '&:hover': {
                        transform: 'translateY(-2px)',
                        boxShadow: '0 8px 24px rgba(46, 125, 50, 0.12)',
                        borderColor: '#2e7d32',
                      },
                    }}
                  >
                    <Box sx={{ display: 'flex', flexDirection: { xs: 'column', sm: 'row' }, alignItems: { xs: 'flex-start', sm: 'center' }, justifyContent: 'space-between', gap: { xs: 1.2, sm: 1.5 }, mb: 1.5 }}>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, width: { xs: '100%', sm: 'auto' }, flex: 1 }}>
                        <Box
                          sx={{
                            width: { xs: 38, sm: 42 },
                            height: { xs: 38, sm: 42 },
                            minWidth: { xs: 38, sm: 42 },
                            borderRadius: 2,
                            bgcolor: 'rgba(46, 125, 50, 0.1)',
                            color: '#2e7d32',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                          }}
                        >
                          <CarIcon sx={{ fontSize: { xs: 20, sm: 22 } }} />
                        </Box>
                        <Box sx={{ flex: 1, minWidth: 0 }}>
                          <Typography variant="subtitle1" sx={{ fontWeight: 700, lineHeight: 1.25, fontSize: { xs: '0.92rem', sm: '1rem' } }}>
                            2. Hệ Số Giá Theo Loại Xe (Vehicle Multipliers)
                          </Typography>
                          <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block', mt: 0.3, fontSize: { xs: '0.74rem', sm: '0.78rem' } }}>
                            Hệ số nhân tương ứng với cước mở cửa, cước km và cước thời gian
                          </Typography>
                        </Box>
                      </Box>
                      <Button
                        size="small"
                        variant="outlined"
                        color="success"
                        startIcon={<EditIcon sx={{ fontSize: 16 }} />}
                        sx={{
                          borderRadius: 1.8,
                          textTransform: 'none',
                          fontWeight: 600,
                          fontSize: { xs: '0.78rem', sm: '0.82rem' },
                          py: { xs: 0.4, sm: 0.5 },
                          px: 1.5,
                          alignSelf: { xs: 'flex-end', sm: 'center' },
                          flexShrink: 0,
                        }}
                      >
                        Chỉnh sửa
                      </Button>
                    </Box>
                    <Stack direction="row" spacing={0.8} flexWrap="wrap" useFlexGap sx={{ mt: 1 }}>
                      <Chip
                        size="small"
                        icon={<BikeIcon fontSize="small" />}
                        label={`Xe máy: ${form.bikeMultiplier}x`}
                        sx={{
                          bgcolor: 'rgba(46, 125, 50, 0.08)',
                          fontWeight: 600,
                          color: '#2e7d32',
                          fontSize: { xs: '0.74rem', sm: '0.8rem' },
                          height: 'auto',
                          py: 0.4,
                          maxWidth: '100%',
                          '& .MuiChip-label': { px: 1, whiteSpace: 'normal', wordBreak: 'break-word' },
                        }}
                      />
                      <Chip
                        size="small"
                        icon={<CarIcon fontSize="small" />}
                        label={`Ô tô 4 chỗ: ${form.car4SeatMultiplier}x`}
                        sx={{
                          bgcolor: 'rgba(46, 125, 50, 0.08)',
                          fontWeight: 600,
                          color: '#2e7d32',
                          fontSize: { xs: '0.74rem', sm: '0.8rem' },
                          height: 'auto',
                          py: 0.4,
                          maxWidth: '100%',
                          '& .MuiChip-label': { px: 1, whiteSpace: 'normal', wordBreak: 'break-word' },
                        }}
                      />
                      <Chip
                        size="small"
                        icon={<CarIcon fontSize="small" />}
                        label={`Ô tô 7 chỗ: ${form.car7SeatMultiplier}x`}
                        sx={{
                          bgcolor: 'rgba(46, 125, 50, 0.08)',
                          fontWeight: 600,
                          color: '#2e7d32',
                          fontSize: { xs: '0.74rem', sm: '0.8rem' },
                          height: 'auto',
                          py: 0.4,
                          maxWidth: '100%',
                          '& .MuiChip-label': { px: 1, whiteSpace: 'normal', wordBreak: 'break-word' },
                        }}
                      />
                    </Stack>
                  </Card>

                  {/* Mục 3: Biểu phí giao đồ ăn */}
                  <Card
                    onClick={() => openModal('FOOD_DELIVERY')}
                    sx={{
                      p: { xs: 1.8, sm: 2.2 },
                      borderRadius: 2.5,
                      border: '1px solid',
                      borderColor: 'divider',
                      cursor: 'pointer',
                      transition: 'all 0.22s ease-in-out',
                      '&:hover': {
                        transform: 'translateY(-2px)',
                        boxShadow: '0 8px 24px rgba(237, 108, 2, 0.12)',
                        borderColor: '#ed6c02',
                      },
                    }}
                  >
                    <Box sx={{ display: 'flex', flexDirection: { xs: 'column', sm: 'row' }, alignItems: { xs: 'flex-start', sm: 'center' }, justifyContent: 'space-between', gap: { xs: 1.2, sm: 1.5 }, mb: 1.5 }}>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, width: { xs: '100%', sm: 'auto' }, flex: 1 }}>
                        <Box
                          sx={{
                            width: { xs: 38, sm: 42 },
                            height: { xs: 38, sm: 42 },
                            minWidth: { xs: 38, sm: 42 },
                            borderRadius: 2,
                            bgcolor: 'rgba(237, 108, 2, 0.1)',
                            color: '#ed6c02',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                          }}
                        >
                          <DeliveryIcon sx={{ fontSize: { xs: 20, sm: 22 } }} />
                        </Box>
                        <Box sx={{ flex: 1, minWidth: 0 }}>
                          <Typography variant="subtitle1" sx={{ fontWeight: 700, lineHeight: 1.25, fontSize: { xs: '0.92rem', sm: '1rem' } }}>
                            3. Biểu Phí Giao Đồ Ăn (Food Delivery Fee)
                          </Typography>
                          <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block', mt: 0.3, fontSize: { xs: '0.74rem', sm: '0.78rem' } }}>
                            Tính tự động theo khoảng cách đường bộ xe máy giữa nhà hàng và khách
                          </Typography>
                        </Box>
                      </Box>
                      <Button
                        size="small"
                        variant="outlined"
                        color="warning"
                        startIcon={<EditIcon sx={{ fontSize: 16 }} />}
                        sx={{
                          borderRadius: 1.8,
                          textTransform: 'none',
                          fontWeight: 600,
                          fontSize: { xs: '0.78rem', sm: '0.82rem' },
                          py: { xs: 0.4, sm: 0.5 },
                          px: 1.5,
                          alignSelf: { xs: 'flex-end', sm: 'center' },
                          flexShrink: 0,
                        }}
                      >
                        Chỉnh sửa
                      </Button>
                    </Box>
                    <Stack direction="row" spacing={0.8} flexWrap="wrap" useFlexGap sx={{ mt: 1 }}>
                      <Chip
                        size="small"
                        label={`Phí khởi điểm: ${Number(form.deliveryBaseFare).toLocaleString()} đ`}
                        sx={{
                          bgcolor: 'rgba(237, 108, 2, 0.08)',
                          fontWeight: 600,
                          color: '#ed6c02',
                          fontSize: { xs: '0.74rem', sm: '0.8rem' },
                          height: 'auto',
                          py: 0.4,
                          maxWidth: '100%',
                          '& .MuiChip-label': { px: 1, whiteSpace: 'normal', wordBreak: 'break-word' },
                        }}
                      />
                      <Chip
                        size="small"
                        label={`Cự ly mốc đầu: ${form.deliveryBaseKm} km`}
                        sx={{
                          bgcolor: 'rgba(237, 108, 2, 0.08)',
                          fontWeight: 600,
                          color: '#ed6c02',
                          fontSize: { xs: '0.74rem', sm: '0.8rem' },
                          height: 'auto',
                          py: 0.4,
                          maxWidth: '100%',
                          '& .MuiChip-label': { px: 1, whiteSpace: 'normal', wordBreak: 'break-word' },
                        }}
                      />
                      <Chip
                        size="small"
                        label={`Phí mỗi km thêm: ${Number(form.deliveryPricePerKm).toLocaleString()} đ/km`}
                        sx={{
                          bgcolor: 'rgba(237, 108, 2, 0.08)',
                          fontWeight: 600,
                          color: '#ed6c02',
                          fontSize: { xs: '0.74rem', sm: '0.8rem' },
                          height: 'auto',
                          py: 0.4,
                          maxWidth: '100%',
                          '& .MuiChip-label': { px: 1, whiteSpace: 'normal', wordBreak: 'break-word' },
                        }}
                      />
                    </Stack>
                  </Card>

                  {/* Mục 4: OSRM Routing Engine & Fallback */}
                  <Card
                    onClick={() => openModal('ROUTES_FALLBACK')}
                    sx={{
                      p: { xs: 1.8, sm: 2.2 },
                      borderRadius: 2.5,
                      border: '1px solid',
                      borderColor: 'divider',
                      cursor: 'pointer',
                      transition: 'all 0.22s ease-in-out',
                      '&:hover': {
                        transform: 'translateY(-2px)',
                        boxShadow: '0 8px 24px rgba(156, 39, 176, 0.12)',
                        borderColor: '#9c27b0',
                      },
                    }}
                  >
                    <Box sx={{ display: 'flex', flexDirection: { xs: 'column', sm: 'row' }, alignItems: { xs: 'flex-start', sm: 'center' }, justifyContent: 'space-between', gap: { xs: 1.2, sm: 1.5 }, mb: 1.5 }}>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, width: { xs: '100%', sm: 'auto' }, flex: 1 }}>
                        <Box
                          sx={{
                            width: { xs: 38, sm: 42 },
                            height: { xs: 38, sm: 42 },
                            minWidth: { xs: 38, sm: 42 },
                            borderRadius: 2,
                            bgcolor: 'rgba(156, 39, 176, 0.1)',
                            color: '#9c27b0',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                          }}
                        >
                          <RouteIcon sx={{ fontSize: { xs: 20, sm: 22 } }} />
                        </Box>
                        <Box sx={{ flex: 1, minWidth: 0 }}>
                          <Typography variant="subtitle1" sx={{ fontWeight: 700, lineHeight: 1.25, fontSize: { xs: '0.92rem', sm: '1rem' } }}>
                            4. OSRM Routing Engine & Cơ Chế Fallback
                          </Typography>
                          <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block', mt: 0.3, fontSize: { xs: '0.74rem', sm: '0.78rem' } }}>
                            Kiểm soát thuật toán quy đổi đường chim bay dự phòng (Haversine) và khoảng cách bypass khi trùng điểm
                          </Typography>
                        </Box>
                      </Box>
                      <Button
                        size="small"
                        variant="outlined"
                        color="secondary"
                        startIcon={<EditIcon sx={{ fontSize: 16 }} />}
                        sx={{
                          borderRadius: 1.8,
                          textTransform: 'none',
                          fontWeight: 600,
                          fontSize: { xs: '0.78rem', sm: '0.82rem' },
                          py: { xs: 0.4, sm: 0.5 },
                          px: 1.5,
                          alignSelf: { xs: 'flex-end', sm: 'center' },
                          flexShrink: 0,
                        }}
                      >
                        Chỉnh sửa
                      </Button>
                    </Box>
                    <Stack direction="row" spacing={0.8} flexWrap="wrap" useFlexGap sx={{ mt: 1 }}>
                      <Chip
                        size="small"
                        label={`Bù uốn lượn Fallback: ${form.haversineFallbackMultiplier}x`}
                        sx={{
                          bgcolor: 'rgba(156, 39, 176, 0.08)',
                          fontWeight: 600,
                          color: '#9c27b0',
                          fontSize: { xs: '0.74rem', sm: '0.8rem' },
                          height: 'auto',
                          py: 0.4,
                          maxWidth: '100%',
                          '& .MuiChip-label': { px: 1, whiteSpace: 'normal', wordBreak: 'break-word' },
                        }}
                      />
                      <Chip
                        size="small"
                        label={`Bypass API nếu cự ly < ${form.minDistanceBypassMeters} m`}
                        sx={{
                          bgcolor: 'rgba(156, 39, 176, 0.08)',
                          fontWeight: 600,
                          color: '#9c27b0',
                          fontSize: { xs: '0.74rem', sm: '0.8rem' },
                          height: 'auto',
                          py: 0.4,
                          maxWidth: '100%',
                          '& .MuiChip-label': { px: 1, whiteSpace: 'normal', wordBreak: 'break-word' },
                        }}
                      />
                    </Stack>
                  </Card>

                  {/* Mục 5: Tăng giá H3 Surge */}
                  <Card
                    onClick={() => openModal('SURGE')}
                    sx={{
                      p: { xs: 1.8, sm: 2.2 },
                      borderRadius: 2.5,
                      border: '1px solid',
                      borderColor: 'divider',
                      cursor: 'pointer',
                      transition: 'all 0.22s ease-in-out',
                      '&:hover': {
                        transform: 'translateY(-2px)',
                        boxShadow: '0 8px 24px rgba(211, 47, 47, 0.12)',
                        borderColor: '#d32f2f',
                      },
                    }}
                  >
                    <Box sx={{ display: 'flex', flexDirection: { xs: 'column', sm: 'row' }, alignItems: { xs: 'flex-start', sm: 'center' }, justifyContent: 'space-between', gap: { xs: 1.2, sm: 1.5 }, mb: 1.5 }}>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, width: { xs: '100%', sm: 'auto' }, flex: 1 }}>
                        <Box
                          sx={{
                            width: { xs: 38, sm: 42 },
                            height: { xs: 38, sm: 42 },
                            minWidth: { xs: 38, sm: 42 },
                            borderRadius: 2,
                            bgcolor: 'rgba(211, 47, 47, 0.1)',
                            color: '#d32f2f',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                          }}
                        >
                          <SurgeIcon sx={{ fontSize: { xs: 20, sm: 22 } }} />
                        </Box>
                        <Box sx={{ flex: 1, minWidth: 0 }}>
                          <Typography variant="subtitle1" sx={{ fontWeight: 700, lineHeight: 1.25, fontSize: { xs: '0.92rem', sm: '1rem' } }}>
                            5. Hệ Số Tăng Giá Giờ Cao Điểm (H3 Dynamic Surge)
                          </Typography>
                          <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block', mt: 0.3, fontSize: { xs: '0.74rem', sm: '0.78rem' } }}>
                            Hệ số nhân theo mức độ thiếu hụt tài xế (Cung - Cầu Hexagon H3)
                          </Typography>
                        </Box>
                      </Box>
                      <Button
                        size="small"
                        variant="outlined"
                        color="error"
                        startIcon={<EditIcon sx={{ fontSize: 16 }} />}
                        sx={{
                          borderRadius: 1.8,
                          textTransform: 'none',
                          fontWeight: 600,
                          fontSize: { xs: '0.78rem', sm: '0.82rem' },
                          py: { xs: 0.4, sm: 0.5 },
                          px: 1.5,
                          alignSelf: { xs: 'flex-end', sm: 'center' },
                          flexShrink: 0,
                        }}
                      >
                        Chỉnh sửa
                      </Button>
                    </Box>
                    <Stack direction="row" spacing={0.8} flexWrap="wrap" useFlexGap sx={{ mt: 1 }}>
                      <Chip
                        size="small"
                        label={`Vừa (MODERATE): ${form.surgeLightMultiplier}x`}
                        sx={{
                          bgcolor: 'rgba(211, 47, 47, 0.08)',
                          fontWeight: 600,
                          color: '#d32f2f',
                          fontSize: { xs: '0.74rem', sm: '0.8rem' },
                          height: 'auto',
                          py: 0.4,
                          maxWidth: '100%',
                          '& .MuiChip-label': { px: 1, whiteSpace: 'normal', wordBreak: 'break-word' },
                        }}
                      />
                      <Chip
                        size="small"
                        label={`Cao (HIGH): ${form.surgeModerateMultiplier}x`}
                        sx={{
                          bgcolor: 'rgba(211, 47, 47, 0.08)',
                          fontWeight: 600,
                          color: '#d32f2f',
                          fontSize: { xs: '0.74rem', sm: '0.8rem' },
                          height: 'auto',
                          py: 0.4,
                          maxWidth: '100%',
                          '& .MuiChip-label': { px: 1, whiteSpace: 'normal', wordBreak: 'break-word' },
                        }}
                      />
                      <Chip
                        size="small"
                        label={`Khẩn cấp (SEVERE): ${form.surgeSevereMultiplier}x`}
                        sx={{
                          bgcolor: 'rgba(211, 47, 47, 0.08)',
                          fontWeight: 600,
                          color: '#d32f2f',
                          fontSize: { xs: '0.74rem', sm: '0.8rem' },
                          height: 'auto',
                          py: 0.4,
                          maxWidth: '100%',
                          '& .MuiChip-label': { px: 1, whiteSpace: 'normal', wordBreak: 'break-word' },
                        }}
                      />
                    </Stack>
                  </Card>
                </Stack>
              </Grid>

              {/* Cột phải: Công cụ mô phỏng cước tức thời */}
              <Grid size={{ xs: 12, lg: 5 }}>
                <Card sx={{ p: { xs: 2, sm: 3 }, borderRadius: 2.5, position: { xs: 'static', lg: 'sticky' }, top: 20 }}>
                  <Typography variant="h6" sx={{ fontWeight: 700, mb: 0.5, fontSize: { xs: '0.98rem', sm: '1.05rem' } }}>
                    Công Cụ Mô Phỏng Cước Tức Thời
                  </Typography>
                  <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block', mb: 2.5, fontSize: { xs: '0.74rem', sm: '0.78rem' } }}>
                    Kiểm tra ngay kết quả tính cước theo các thông số cấu hình bạn vừa thiết lập
                  </Typography>

                  <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                    {/* Chọn dịch vụ */}
                    <Box>
                      <Typography variant="caption" sx={{ fontWeight: 600, display: 'block', mb: 0.5 }}>
                        Dịch vụ trải nghiệm
                      </Typography>
                      <Stack direction="row" spacing={1}>
                        <Chip
                          icon={<CarIcon fontSize="small" />}
                          label="Chuyến xe (Ride)"
                          clickable
                          color={simService === 'RIDE' ? 'primary' : 'default'}
                          onClick={() => setSimService('RIDE')}
                          sx={{ fontWeight: 600 }}
                        />
                        <Chip
                          icon={<DeliveryIcon fontSize="small" />}
                          label="Đồ ăn (Delivery)"
                          clickable
                          color={simService === 'DELIVERY' ? 'warning' : 'default'}
                          onClick={() => setSimService('DELIVERY')}
                          sx={{ fontWeight: 600 }}
                        />
                      </Stack>
                    </Box>

                    {simService === 'RIDE' && (
                      <>
                        <Box>
                          <Typography variant="caption" sx={{ fontWeight: 600, display: 'block', mb: 0.5 }}>
                            Loại phương tiện
                          </Typography>
                          <FormControl fullWidth size="small">
                            <Select
                              value={simVehicle}
                              onChange={(e) => setSimVehicle(e.target.value)}
                            >
                              <MenuItem value="BIKE">Xe máy ({form.bikeMultiplier}x)</MenuItem>
                              <MenuItem value="CAR_4_SEAT">Ô tô 4 chỗ ({form.car4SeatMultiplier}x)</MenuItem>
                              <MenuItem value="CAR_7_SEAT">Ô tô 7 chỗ ({form.car7SeatMultiplier}x)</MenuItem>
                            </Select>
                          </FormControl>
                        </Box>

                        <Box>
                          <Typography variant="caption" sx={{ fontWeight: 600, display: 'block', mb: 0.5 }}>
                            Mức cao điểm (Surge)
                          </Typography>
                          <FormControl fullWidth size="small">
                            <Select
                              value={simSurge}
                              onChange={(e) => setSimSurge(e.target.value)}
                            >
                              <MenuItem value="NORMAL">Bình thường (1.0x)</MenuItem>
                              <MenuItem value="LIGHT">Tăng vừa ({form.surgeLightMultiplier}x)</MenuItem>
                              <MenuItem value="MODERATE">Tăng cao ({form.surgeModerateMultiplier}x)</MenuItem>
                              <MenuItem value="SEVERE">Khẩn cấp ({form.surgeSevereMultiplier}x)</MenuItem>
                            </Select>
                          </FormControl>
                        </Box>
                      </>
                    )}

                    <Box>
                      <Typography variant="caption" sx={{ fontWeight: 600, display: 'block', mb: 0.5 }}>
                        Quãng đường thực tế (km)
                      </Typography>
                      <TextField
                        size="small"
                        type="number"
                        value={simDistance}
                        onChange={(e) => setSimDistance(Math.max(0.1, Number(e.target.value)))}
                        fullWidth
                        slotProps={{
                          input: { endAdornment: <InputAdornment position="end">km</InputAdornment> },
                          htmlInput: { step: 0.5, min: 0.1 },
                        }}
                      />
                    </Box>

                    {simService === 'RIDE' && (
                      <Box>
                        <Typography variant="caption" sx={{ fontWeight: 600, display: 'block', mb: 0.5 }}>
                          Thời gian dự kiến (phút)
                        </Typography>
                        <TextField
                          size="small"
                          type="number"
                          value={simDuration}
                          onChange={(e) => setSimDuration(Math.max(1, Number(e.target.value)))}
                          fullWidth
                          slotProps={{
                            input: { endAdornment: <InputAdornment position="end">phút</InputAdornment> },
                            htmlInput: { step: 1, min: 1 },
                          }}
                        />
                      </Box>
                    )}

                    <Divider sx={{ my: 0.5 }} />

                    {/* Hộp hiển thị kết quả */}
                    <Box
                      sx={{
                        p: 2,
                        borderRadius: 2,
                        bgcolor: 'rgba(0, 140, 255, 0.06)',
                        border: '1px solid rgba(0, 140, 255, 0.2)',
                      }}
                    >
                      <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block', fontWeight: 600 }}>
                        Tổng tiền cước người dùng thanh toán:
                      </Typography>
                      <Typography
                        variant="h4"
                        sx={{
                          fontWeight: 800,
                          color: '#008cff',
                          my: 0.5,
                          fontFamily: '"Poppins", sans-serif',
                        }}
                      >
                        {calculatedFare.toLocaleString('vi-VN')} đ
                      </Typography>

                      {simService === 'RIDE' ? (
                        <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block', fontSize: '0.78rem', lineHeight: 1.5 }}>
                          Công thức: [Mở cửa ({Number(form.baseFare).toLocaleString()}đ × {getSimulatedVehicleMultiplier()}x) + Km ({Number(form.pricePerKm).toLocaleString()}đ × {simDistance}km × {getSimulatedVehicleMultiplier()}x) + Phút ({Number(form.pricePerMinute).toLocaleString()}đ × {simDuration}p × {getSimulatedVehicleMultiplier()}x)] × Surge {getSimulatedSurgeMultiplier()}x
                        </Typography>
                      ) : (
                        <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block', fontSize: '0.78rem', lineHeight: 1.5 }}>
                          Công thức: Cước khởi điểm {Number(form.deliveryBaseFare).toLocaleString()}đ ({form.deliveryBaseKm}km đầu) {simDistance > Number(form.deliveryBaseKm) ? `+ Phụ trội (${(simDistance - Number(form.deliveryBaseKm)).toFixed(1)}km × ${Number(form.deliveryPricePerKm).toLocaleString()}đ)` : ''}
                        </Typography>
                      )}
                    </Box>
                  </Box>
                </Card>
              </Grid>
            </Grid>
          )}

          {/* ==================== 5 MODAL DIALOGS CHO TỪNG MỤC ==================== */}

          {/* Modal 1: Bảng giá chuyến xe cơ bản */}
          <Dialog
            open={activeModal === 'BASE_RIDE'}
            onClose={closeModal}
            maxWidth="sm"
            fullWidth
            PaperProps={{ sx: { borderRadius: 3, p: { xs: 0.5, sm: 1 }, m: { xs: 1.5, sm: 3 } } }}
          >
            <DialogTitle sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', pb: 1 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.2 }}>
                <Box sx={{ width: 36, height: 36, borderRadius: 2, bgcolor: 'rgba(0, 140, 255, 0.1)', color: '#008cff', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                  <PricingIcon />
                </Box>
                <Box>
                  <Typography variant="h6" sx={{ fontWeight: 700, fontSize: '1.1rem' }}>
                    1. Bảng Giá Chuyến Xe Cơ Bản
                  </Typography>
                  <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block' }}>
                    Áp dụng cho xe máy (BIKE) làm mốc quy chiếu cước cơ bản
                  </Typography>
                </Box>
              </Box>
              <IconButton onClick={closeModal} size="small">
                <CloseIcon />
              </IconButton>
            </DialogTitle>
            <Divider />
            <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2.2, pt: 2.5 }}>
              <Box>
                <Typography variant="body2" sx={{ fontWeight: 600, mb: 0.8 }}>
                  Giá mở cửa (Base Fare)
                </Typography>
                <TextField
                  name="baseFare"
                  type="number"
                  value={form.baseFare}
                  onChange={handleChange}
                  fullWidth
                  slotProps={{
                    input: { endAdornment: <InputAdornment position="end">đ</InputAdornment> },
                    htmlInput: { step: 1000, min: 0 },
                  }}
                  helperText="Cước cố định ngay khi xe lăn bánh nhận khách"
                />
              </Box>
              <Box>
                <Typography variant="body2" sx={{ fontWeight: 600, mb: 0.8 }}>
                  Giá mỗi km (Price/Km)
                </Typography>
                <TextField
                  name="pricePerKm"
                  type="number"
                  value={form.pricePerKm}
                  onChange={handleChange}
                  fullWidth
                  slotProps={{
                    input: { endAdornment: <InputAdornment position="end">đ/km</InputAdornment> },
                    htmlInput: { step: 500, min: 0 },
                  }}
                  helperText="Đơn giá tính cho mỗi km di chuyển thực tế"
                />
              </Box>
              <Box>
                <Typography variant="body2" sx={{ fontWeight: 600, mb: 0.8 }}>
                  Giá mỗi phút (Price/Min)
                </Typography>
                <TextField
                  name="pricePerMinute"
                  type="number"
                  value={form.pricePerMinute}
                  onChange={handleChange}
                  fullWidth
                  slotProps={{
                    input: { endAdornment: <InputAdornment position="end">đ/phút</InputAdornment> },
                    htmlInput: { step: 100, min: 0 },
                  }}
                  helperText="Cước thời gian lưu thông trên đường"
                />
              </Box>
            </DialogContent>
            <DialogActions sx={{ px: { xs: 2, sm: 3 }, pb: { xs: 2, sm: 2.5 }, flexDirection: { xs: 'column-reverse', sm: 'row' }, gap: 1 }}>
              <Button onClick={closeModal} variant="outlined" color="inherit" sx={{ borderRadius: 2, width: { xs: '100%', sm: 'auto' } }}>
                Hủy bỏ
              </Button>
              <Button
                onClick={handleSaveModal}
                variant="contained"
                disabled={saving}
                startIcon={saving ? <CircularProgress size={18} sx={{ color: '#fff' }} /> : <SaveIcon />}
                sx={{ borderRadius: 2, bgcolor: '#008cff', '&:hover': { bgcolor: '#0070cc' }, px: 3, width: { xs: '100%', sm: 'auto' } }}
              >
                {saving ? 'Đang lưu...' : 'Lưu Cấu Hình'}
              </Button>
            </DialogActions>
          </Dialog>

          {/* Modal 2: Hệ số giá loại xe */}
          <Dialog
            open={activeModal === 'VEHICLE'}
            onClose={closeModal}
            maxWidth="sm"
            fullWidth
            PaperProps={{ sx: { borderRadius: 3, p: { xs: 0.5, sm: 1 }, m: { xs: 1.5, sm: 3 } } }}
          >
            <DialogTitle sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', pb: 1 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.2 }}>
                <Box sx={{ width: 36, height: 36, borderRadius: 2, bgcolor: 'rgba(46, 125, 50, 0.1)', color: '#2e7d32', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                  <CarIcon />
                </Box>
                <Box>
                  <Typography variant="h6" sx={{ fontWeight: 700, fontSize: '1.1rem' }}>
                    2. Hệ Số Giá Theo Loại Xe
                  </Typography>
                  <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block' }}>
                    Hệ số nhân tương ứng với cước mở cửa, km và thời gian của từng dòng xe
                  </Typography>
                </Box>
              </Box>
              <IconButton onClick={closeModal} size="small">
                <CloseIcon />
              </IconButton>
            </DialogTitle>
            <Divider />
            <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2.2, pt: 2.5 }}>
              <Box>
                <Typography variant="body2" sx={{ fontWeight: 600, mb: 0.8 }}>
                  Xe máy (BIKE / EXPRESS)
                </Typography>
                <TextField
                  name="bikeMultiplier"
                  type="number"
                  value={form.bikeMultiplier}
                  onChange={handleChange}
                  fullWidth
                  slotProps={{
                    input: { endAdornment: <InputAdornment position="end">x</InputAdornment> },
                    htmlInput: { step: 0.1, min: 0.5, max: 10 },
                  }}
                  helperText="Chuẩn gốc: 1.0x (áp dụng nguyên giá mốc quy chiếu)"
                />
              </Box>
              <Box>
                <Typography variant="body2" sx={{ fontWeight: 600, mb: 0.8 }}>
                  Ô tô 4 chỗ (CAR_4_SEAT)
                </Typography>
                <TextField
                  name="car4SeatMultiplier"
                  type="number"
                  value={form.car4SeatMultiplier}
                  onChange={handleChange}
                  fullWidth
                  slotProps={{
                    input: { endAdornment: <InputAdornment position="end">x</InputAdornment> },
                    htmlInput: { step: 0.1, min: 0.5, max: 10 },
                  }}
                  helperText="Mặc định: 1.5x (nhân 1.5 lần so với xe máy)"
                />
              </Box>
              <Box>
                <Typography variant="body2" sx={{ fontWeight: 600, mb: 0.8 }}>
                  Ô tô 7 chỗ (CAR_7_SEAT)
                </Typography>
                <TextField
                  name="car7SeatMultiplier"
                  type="number"
                  value={form.car7SeatMultiplier}
                  onChange={handleChange}
                  fullWidth
                  slotProps={{
                    input: { endAdornment: <InputAdornment position="end">x</InputAdornment> },
                    htmlInput: { step: 0.1, min: 0.5, max: 10 },
                  }}
                  helperText="Mặc định: 2.0x (nhân 2.0 lần so với xe máy)"
                />
              </Box>
            </DialogContent>
            <DialogActions sx={{ px: { xs: 2, sm: 3 }, pb: { xs: 2, sm: 2.5 }, flexDirection: { xs: 'column-reverse', sm: 'row' }, gap: 1 }}>
              <Button onClick={closeModal} variant="outlined" color="inherit" sx={{ borderRadius: 2, width: { xs: '100%', sm: 'auto' } }}>
                Hủy bỏ
              </Button>
              <Button
                onClick={handleSaveModal}
                variant="contained"
                disabled={saving}
                startIcon={saving ? <CircularProgress size={18} sx={{ color: '#fff' }} /> : <SaveIcon />}
                sx={{ borderRadius: 2, bgcolor: '#2e7d32', '&:hover': { bgcolor: '#1b5e20' }, px: 3, width: { xs: '100%', sm: 'auto' } }}
              >
                {saving ? 'Đang lưu...' : 'Lưu Cấu Hình'}
              </Button>
            </DialogActions>
          </Dialog>

          {/* Modal 3: Biểu phí giao đồ ăn */}
          <Dialog
            open={activeModal === 'FOOD_DELIVERY'}
            onClose={closeModal}
            maxWidth="sm"
            fullWidth
            PaperProps={{ sx: { borderRadius: 3, p: { xs: 0.5, sm: 1 }, m: { xs: 1.5, sm: 3 } } }}
          >
            <DialogTitle sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', pb: 1 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.2 }}>
                <Box sx={{ width: 36, height: 36, borderRadius: 2, bgcolor: 'rgba(237, 108, 2, 0.1)', color: '#ed6c02', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                  <DeliveryIcon />
                </Box>
                <Box>
                  <Typography variant="h6" sx={{ fontWeight: 700, fontSize: '1.1rem' }}>
                    3. Biểu Phí Giao Đồ Ăn (Food Delivery Fee)
                  </Typography>
                  <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block' }}>
                    Tính theo khoảng cách đường bộ xe máy giữa nhà hàng và khách nhận món
                  </Typography>
                </Box>
              </Box>
              <IconButton onClick={closeModal} size="small">
                <CloseIcon />
              </IconButton>
            </DialogTitle>
            <Divider />
            <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2.2, pt: 2.5 }}>
              <Box>
                <Typography variant="body2" sx={{ fontWeight: 600, mb: 0.8 }}>
                  Phí ship khởi điểm
                </Typography>
                <TextField
                  name="deliveryBaseFare"
                  type="number"
                  value={form.deliveryBaseFare}
                  onChange={handleChange}
                  fullWidth
                  slotProps={{
                    input: { endAdornment: <InputAdornment position="end">đ</InputAdornment> },
                    htmlInput: { step: 1000, min: 0 },
                  }}
                  helperText="Cước cơ sở áp dụng cho mốc cự ly ban đầu"
                />
              </Box>
              <Box>
                <Typography variant="body2" sx={{ fontWeight: 600, mb: 0.8 }}>
                  Cự ly khởi điểm áp dụng
                </Typography>
                <TextField
                  name="deliveryBaseKm"
                  type="number"
                  value={form.deliveryBaseKm}
                  onChange={handleChange}
                  fullWidth
                  slotProps={{
                    input: { endAdornment: <InputAdornment position="end">km</InputAdornment> },
                    htmlInput: { step: 0.5, min: 0.5 },
                  }}
                  helperText="Khoảng cách tối đa hưởng mức phí ship khởi điểm"
                />
              </Box>
              <Box>
                <Typography variant="body2" sx={{ fontWeight: 600, mb: 0.8 }}>
                  Phí mỗi km tiếp theo
                </Typography>
                <TextField
                  name="deliveryPricePerKm"
                  type="number"
                  value={form.deliveryPricePerKm}
                  onChange={handleChange}
                  fullWidth
                  slotProps={{
                    input: { endAdornment: <InputAdornment position="end">đ/km</InputAdornment> },
                    htmlInput: { step: 500, min: 0 },
                  }}
                  helperText="Đơn giá tính cho mỗi km khi cự ly vượt qua cự ly khởi điểm"
                />
              </Box>
            </DialogContent>
            <DialogActions sx={{ px: { xs: 2, sm: 3 }, pb: { xs: 2, sm: 2.5 }, flexDirection: { xs: 'column-reverse', sm: 'row' }, gap: 1 }}>
              <Button onClick={closeModal} variant="outlined" color="inherit" sx={{ borderRadius: 2, width: { xs: '100%', sm: 'auto' } }}>
                Hủy bỏ
              </Button>
              <Button
                onClick={handleSaveModal}
                variant="contained"
                disabled={saving}
                startIcon={saving ? <CircularProgress size={18} sx={{ color: '#fff' }} /> : <SaveIcon />}
                sx={{ borderRadius: 2, bgcolor: '#ed6c02', '&:hover': { bgcolor: '#c75c00' }, px: 3, width: { xs: '100%', sm: 'auto' } }}
              >
                {saving ? 'Đang lưu...' : 'Lưu Cấu Hình'}
              </Button>
            </DialogActions>
          </Dialog>

          {/* Modal 4: OSRM Routing Engine & Fallback */}
          <Dialog
            open={activeModal === 'ROUTES_FALLBACK'}
            onClose={closeModal}
            maxWidth="sm"
            fullWidth
            PaperProps={{ sx: { borderRadius: 3, p: { xs: 0.5, sm: 1 }, m: { xs: 1.5, sm: 3 } } }}
          >
            <DialogTitle sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', pb: 1 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.2 }}>
                <Box sx={{ width: 36, height: 36, borderRadius: 2, bgcolor: 'rgba(156, 39, 176, 0.1)', color: '#9c27b0', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                  <RouteIcon />
                </Box>
                <Box>
                  <Typography variant="h6" sx={{ fontWeight: 700, fontSize: '1.1rem' }}>
                    4. OSRM Routing Engine & Cơ Chế Fallback
                  </Typography>
                  <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block' }}>
                    Kiểm soát thuật toán quy đổi đường chim bay dự phòng (Haversine) và khoảng cách bypass khi trùng điểm
                  </Typography>
                </Box>
              </Box>
              <IconButton onClick={closeModal} size="small">
                <CloseIcon />
              </IconButton>
            </DialogTitle>
            <Divider />
            <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2.2, pt: 2.5 }}>
              <Box>
                <Typography variant="body2" sx={{ fontWeight: 600, mb: 0.8 }}>
                  Hệ số uốn lượn Fallback Haversine
                </Typography>
                <TextField
                  name="haversineFallbackMultiplier"
                  type="number"
                  value={form.haversineFallbackMultiplier}
                  onChange={handleChange}
                  fullWidth
                  slotProps={{
                    input: { endAdornment: <InputAdornment position="end">x</InputAdornment> },
                    htmlInput: { step: 0.05, min: 1.0, max: 3.0 },
                  }}
                  helperText="Hệ số nhân với khoảng cách chim bay bù ngõ hẻm khi OSRM gián đoạn (mặc định: 1.35x)"
                />
              </Box>
              <Box>
                <Typography variant="body2" sx={{ fontWeight: 600, mb: 0.8 }}>
                  Khoảng cách tối thiểu Bypass API
                </Typography>
                <TextField
                  name="minDistanceBypassMeters"
                  type="number"
                  value={form.minDistanceBypassMeters}
                  onChange={handleChange}
                  fullWidth
                  slotProps={{
                    input: { endAdornment: <InputAdornment position="end">mét</InputAdornment> },
                    htmlInput: { step: 5, min: 10, max: 500 },
                  }}
                  helperText="Dưới ngưỡng này tự động coi là trùng điểm đón/trả và bypass không gọi OSRM (mặc định: 50m)"
                />
              </Box>
            </DialogContent>
            <DialogActions sx={{ px: { xs: 2, sm: 3 }, pb: { xs: 2, sm: 2.5 }, flexDirection: { xs: 'column-reverse', sm: 'row' }, gap: 1 }}>
              <Button onClick={closeModal} variant="outlined" color="inherit" sx={{ borderRadius: 2, width: { xs: '100%', sm: 'auto' } }}>
                Hủy bỏ
              </Button>
              <Button
                onClick={handleSaveModal}
                variant="contained"
                disabled={saving}
                startIcon={saving ? <CircularProgress size={18} sx={{ color: '#fff' }} /> : <SaveIcon />}
                sx={{ borderRadius: 2, bgcolor: '#9c27b0', '&:hover': { bgcolor: '#7b1fa2' }, px: 3, width: { xs: '100%', sm: 'auto' } }}
              >
                {saving ? 'Đang lưu...' : 'Lưu Cấu Hình'}
              </Button>
            </DialogActions>
          </Dialog>

          {/* Modal 5: Hệ số tăng giá cao điểm (H3 Surge) */}
          <Dialog
            open={activeModal === 'SURGE'}
            onClose={closeModal}
            maxWidth="sm"
            fullWidth
            PaperProps={{ sx: { borderRadius: 3, p: { xs: 0.5, sm: 1 }, m: { xs: 1.5, sm: 3 } } }}
          >
            <DialogTitle sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', pb: 1 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.2 }}>
                <Box sx={{ width: 36, height: 36, borderRadius: 2, bgcolor: 'rgba(211, 47, 47, 0.1)', color: '#d32f2f', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                  <SurgeIcon />
                </Box>
                <Box>
                  <Typography variant="h6" sx={{ fontWeight: 700, fontSize: '1.1rem' }}>
                    5. Hệ Số Tăng Giá Giờ Cao Điểm (H3 Dynamic Surge)
                  </Typography>
                  <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block' }}>
                    Tự động áp dụng theo mức độ thiếu hụt tài xế từng ô H3 Hexagon
                  </Typography>
                </Box>
              </Box>
              <IconButton onClick={closeModal} size="small">
                <CloseIcon />
              </IconButton>
            </DialogTitle>
            <Divider />
            <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2.2, pt: 2.5 }}>
              <Box>
                <Typography variant="body2" sx={{ fontWeight: 600, mb: 0.8 }}>
                  Mức Tăng Vừa (MODERATE)
                </Typography>
                <TextField
                  name="surgeLightMultiplier"
                  type="number"
                  value={form.surgeLightMultiplier}
                  onChange={handleChange}
                  fullWidth
                  slotProps={{
                    input: { endAdornment: <InputAdornment position="end">x</InputAdornment> },
                    htmlInput: { step: 0.1, min: 1.0, max: 5.0 },
                  }}
                  helperText="Áp dụng khi nhu cầu cao hơn nguồn cung ở mức vừa phải (mặc định: 1.2x)"
                />
              </Box>
              <Box>
                <Typography variant="body2" sx={{ fontWeight: 600, mb: 0.8 }}>
                  Mức Tăng Cao (HIGH)
                </Typography>
                <TextField
                  name="surgeModerateMultiplier"
                  type="number"
                  value={form.surgeModerateMultiplier}
                  onChange={handleChange}
                  fullWidth
                  slotProps={{
                    input: { endAdornment: <InputAdornment position="end">x</InputAdornment> },
                    htmlInput: { step: 0.1, min: 1.0, max: 5.0 },
                  }}
                  helperText="Áp dụng khi lượng khách vượt trội số tài xế rảnh (mặc định: 1.5x)"
                />
              </Box>
              <Box>
                <Typography variant="body2" sx={{ fontWeight: 600, mb: 0.8 }}>
                  Mức Khẩn Cấp (SEVERE)
                </Typography>
                <TextField
                  name="surgeSevereMultiplier"
                  type="number"
                  value={form.surgeSevereMultiplier}
                  onChange={handleChange}
                  fullWidth
                  slotProps={{
                    input: { endAdornment: <InputAdornment position="end">x</InputAdornment> },
                    htmlInput: { step: 0.1, min: 1.0, max: 5.0 },
                  }}
                  helperText="Áp dụng khi khu vực gần như cạn kiệt tài xế (mặc định: 2.0x)"
                />
              </Box>
            </DialogContent>
            <DialogActions sx={{ px: { xs: 2, sm: 3 }, pb: { xs: 2, sm: 2.5 }, flexDirection: { xs: 'column-reverse', sm: 'row' }, gap: 1 }}>
              <Button onClick={closeModal} variant="outlined" color="inherit" sx={{ borderRadius: 2, width: { xs: '100%', sm: 'auto' } }}>
                Hủy bỏ
              </Button>
              <Button
                onClick={handleSaveModal}
                variant="contained"
                disabled={saving}
                startIcon={saving ? <CircularProgress size={18} sx={{ color: '#fff' }} /> : <SaveIcon />}
                sx={{ borderRadius: 2, bgcolor: '#d32f2f', '&:hover': { bgcolor: '#b71c1c' }, px: 3, width: { xs: '100%', sm: 'auto' } }}
              >
                {saving ? 'Đang lưu...' : 'Lưu Cấu Hình'}
              </Button>
            </DialogActions>
          </Dialog>
        </Box>
      )}

      {/* TAB 2: Cấu hình bán kính điều phối */}
      {activeTab === 1 && (
        <Box>
          {dispatchLoading ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', py: 6 }}>
              <CircularProgress size={36} />
            </Box>
          ) : (
            <Grid container spacing={2.5} sx={{ width: '100%' }}>
              <Grid size={{ xs: 12, md: 7 }}>
                <Card sx={{ p: { xs: 2, sm: 3 }, height: '100%', borderRadius: 2.5 }}>
                  <Typography variant="h6" sx={{ fontWeight: 700, mb: 0.5, fontSize: '1.05rem' }}>
                    Thông Số Bán Kính Quét Tài Xế
                  </Typography>
                  <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block', mb: 2.5 }}>
                    Admin có thể thay đổi bán kính theo nhu cầu thực tế, hiệu lực ngay lập tức không cần restart
                  </Typography>

                  <Box component="form" onSubmit={handleDispatchSave} sx={{ display: 'flex', flexDirection: 'column', gap: 2.2 }}>
                    <Box>
                      <Typography variant="body2" sx={{ fontWeight: 600, mb: 0.8 }}>
                        Bán kính tối đa – Cuốc xe (Ride-Hailing)
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
                        Bán kính tối đa – Giao đồ ăn (Food Delivery)
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

              <Grid size={{ xs: 12, md: 5 }}>
                <Card sx={{ p: { xs: 2, sm: 3 }, height: '100%', borderRadius: 2.5 }}>
                  <Typography variant="h6" sx={{ fontWeight: 700, mb: 0.5, fontSize: '1.05rem' }}>
                    Hướng Dẫn Sử Dụng
                  </Typography>
                  <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block', mb: 2.5 }}>
                    Giải thích ý nghĩa các thông số bán kính điều phối
                  </Typography>

                  <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                    <Box sx={{ p: 2, borderRadius: 2, bgcolor: 'rgba(0, 140, 255, 0.08)', border: '1px solid rgba(0, 140, 255, 0.25)' }}>
                      <Typography variant="body2" sx={{ fontWeight: 700, mb: 0.5, color: '#008cff' }}>Cuốc xe</Typography>
                      <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block' }}>
                        Khi khách đặt xe, hệ thống quét tìm tài xế rảnh trong bán kính tối đa này tính từ <b>điểm đón khách</b>. Quét theo nấc: 3km &rarr; 5km &rarr; max.
                      </Typography>
                    </Box>
                    <Box sx={{ p: 2, borderRadius: 2, bgcolor: 'rgba(255, 152, 0, 0.08)', border: '1px solid rgba(255, 152, 0, 0.25)' }}>
                      <Typography variant="body2" sx={{ fontWeight: 700, mb: 0.5, color: '#f57c00' }}>Giao đồ ăn</Typography>
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
      )}
    </Box>
  );
};

export default Pricing;
