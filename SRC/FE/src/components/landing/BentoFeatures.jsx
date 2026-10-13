import {
  Box,
  Container,
  Typography,
  Chip,
  Paper,
} from '@mui/material';
import {
  DirectionsCarOutlined as RideIcon,
  RestaurantMenuOutlined as FoodIcon,
  AccountBalanceWalletOutlined as PaymentIcon,
  MyLocationOutlined as TrackingIcon,
  CheckCircleOutlined as CheckIcon,
  SpeedOutlined as FastIcon,
} from '@mui/icons-material';
import { useColorMode } from '../../context/ThemeContext';

export const BentoFeatures = () => {
  const { mode } = useColorMode();
  const isDark = mode === 'dark';

  return (
    <Box
      id="features"
      component="section"
      sx={{
        py: { xs: 8, sm: 10, md: 12 },
        bgcolor: isDark ? '#0f172a' : '#f8fafc',
        position: 'relative',
      }}
    >
      <Container maxWidth="lg" sx={{ px: { xs: 2, sm: 3, md: 4 } }}>
        {/* Section Header */}
        <Box sx={{ textAlign: 'center', maxWidth: 760, mx: 'auto', mb: { xs: 5, md: 8 } }}>
          <Chip
            label="TIỆN ÍCH NỔI BẬT"
            sx={{
              fontWeight: 800,
              fontSize: '0.78rem',
              letterSpacing: '0.06em',
              py: 0.5,
              px: 1,
              mb: 2,
              borderRadius: '999px',
              bgcolor: isDark ? 'rgba(59, 130, 246, 0.15)' : 'rgba(59, 130, 246, 0.1)',
              color: '#3b82f6',
              border: `1px solid ${isDark ? 'rgba(59, 130, 246, 0.3)' : 'rgba(59, 130, 246, 0.2)'}`,
            }}
          />
          <Typography
            variant="h3"
            sx={{
              fontWeight: 800,
              fontFamily: '"Plus Jakarta Sans", sans-serif',
              fontSize: { xs: '1.85rem', sm: '2.4rem', md: '2.8rem' },
              color: isDark ? '#ffffff' : '#0f172a',
              letterSpacing: '-0.02em',
              mb: 2,
            }}
          >
            Mọi dịch vụ thiết yếu trên một nền tảng
          </Typography>
          <Typography
            variant="body1"
            sx={{
              fontSize: { xs: '0.98rem', sm: '1.08rem' },
              color: isDark ? '#94a3b8' : '#64748b',
              lineHeight: 1.6,
            }}
          >
            Kết nối nhịp nhàng giữa khách hàng, mạng lưới tài xế đối tác và chuỗi nhà hàng ẩm thực chất lượng, mang lại trải nghiệm liền mạch từ lúc đặt tới khi hoàn tất.
          </Typography>
        </Box>

        {/* Asymmetrical Bento Grid */}
        <Box
          sx={{
            display: 'grid',
            gridTemplateColumns: { xs: '1fr', md: 'repeat(12, 1fr)' },
            gap: { xs: 2.5, md: 3 },
          }}
        >
          {/* Card 1: OmniRide (Large Col Span 7) */}
          <Paper
            elevation={0}
            sx={{
              gridColumn: { xs: 'span 1', md: 'span 7' },
              p: { xs: 3, sm: 4 },
              borderRadius: '24px',
              bgcolor: isDark ? '#1e293b' : '#ffffff',
              border: `1px solid ${isDark ? 'rgba(255, 255, 255, 0.08)' : 'rgba(0, 0, 0, 0.06)'}`,
              boxShadow: isDark
                ? '0 10px 30px rgba(0, 0, 0, 0.3)'
                : '0 10px 30px rgba(15, 23, 42, 0.04)',
              display: 'flex',
              flexDirection: 'column',
              justifyContent: 'space-between',
              position: 'relative',
              overflow: 'hidden',
              transition: 'transform 0.25s ease, box-shadow 0.25s ease',
              '&:hover': {
                transform: 'translateY(-3px)',
                boxShadow: isDark
                  ? '0 16px 36px rgba(0, 0, 0, 0.45)'
                  : '0 16px 36px rgba(59, 130, 246, 0.1)',
              },
            }}
          >
            <Box sx={{ mb: 3 }}>
              <Box
                sx={{
                  width: 48,
                  height: 48,
                  borderRadius: '14px',
                  bgcolor: 'rgba(59, 130, 246, 0.12)',
                  color: '#3b82f6',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  mb: 2,
                }}
              >
                <RideIcon sx={{ fontSize: 28 }} />
              </Box>
              <Typography variant="h5" sx={{ fontWeight: 800, mb: 1, color: 'text.primary' }}>
                Đặt xe OmniRide thông minh
              </Typography>
              <Typography variant="body2" sx={{ color: 'text.secondary', fontSize: '0.92rem', lineHeight: 1.6, maxWidth: 500 }}>
                Hệ thống tự động tính cước minh bạch theo lộ trình thực tế, quét tìm tài xế gần bạn nhất và điều phối chuyến đi chỉ trong 20 giây.
              </Typography>
            </Box>

            {/* Visual Mini Mockup for OmniRide */}
            <Box
              sx={{
                p: 2,
                borderRadius: '16px',
                bgcolor: isDark ? '#0f172a' : '#f8fafc',
                border: `1px solid ${isDark ? 'rgba(255, 255, 255, 0.06)' : 'rgba(0, 0, 0, 0.05)'}`,
              }}
            >
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1.5 }}>
                <Typography variant="caption" sx={{ fontWeight: 700, color: '#3b82f6', display: 'flex', alignItems: 'center', gap: 0.5 }}>
                  <FastIcon sx={{ fontSize: 15 }} /> Tự động ghép tài xế gần nhất
                </Typography>
                <Chip size="small" label="20s đếm ngược" sx={{ fontSize: '0.7rem', height: 22, fontWeight: 700 }} />
              </Box>

              <Box sx={{ display: 'flex', gap: 1.5, alignItems: 'center' }}>
                <Box
                  sx={{
                    flex: 1,
                    p: 1.5,
                    borderRadius: '12px',
                    bgcolor: isDark ? 'rgba(255,255,255,0.04)' : '#ffffff',
                    border: '1px solid #3b82f6',
                  }}
                >
                  <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block', fontSize: '0.7rem' }}>
                    OmniBike (Xe máy)
                  </Typography>
                  <Typography variant="subtitle2" sx={{ fontWeight: 800, color: '#3b82f6' }}>
                    18.000đ • Đón ngay
                  </Typography>
                </Box>
                <Box
                  sx={{
                    flex: 1,
                    p: 1.5,
                    borderRadius: '12px',
                    bgcolor: isDark ? 'rgba(255,255,255,0.02)' : '#ffffff',
                    border: `1px solid ${isDark ? 'rgba(255,255,255,0.08)' : '#e2e8f0'}`,
                  }}
                >
                  <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block', fontSize: '0.7rem' }}>
                    OmniCar (Ô tô 4 chỗ)
                  </Typography>
                  <Typography variant="subtitle2" sx={{ fontWeight: 700 }}>
                    42.000đ • Đón 4 phút
                  </Typography>
                </Box>
              </Box>
            </Box>
          </Paper>

          {/* Card 2: OmniFood Delivery (Large Col Span 5) */}
          <Paper
            elevation={0}
            sx={{
              gridColumn: { xs: 'span 1', md: 'span 5' },
              p: { xs: 3, sm: 4 },
              borderRadius: '24px',
              bgcolor: isDark ? '#1e293b' : '#ffffff',
              border: `1px solid ${isDark ? 'rgba(255, 255, 255, 0.08)' : 'rgba(0, 0, 0, 0.06)'}`,
              boxShadow: isDark
                ? '0 10px 30px rgba(0, 0, 0, 0.3)'
                : '0 10px 30px rgba(15, 23, 42, 0.04)',
              display: 'flex',
              flexDirection: 'column',
              justifyContent: 'space-between',
              transition: 'transform 0.25s ease, box-shadow 0.25s ease',
              '&:hover': {
                transform: 'translateY(-3px)',
                boxShadow: isDark
                  ? '0 16px 36px rgba(0, 0, 0, 0.45)'
                  : '0 16px 36px rgba(249, 115, 22, 0.1)',
              },
            }}
          >
            <Box sx={{ mb: 3 }}>
              <Box
                sx={{
                  width: 48,
                  height: 48,
                  borderRadius: '14px',
                  bgcolor: 'rgba(249, 115, 22, 0.12)',
                  color: '#f97316',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  mb: 2,
                }}
              >
                <FoodIcon sx={{ fontSize: 28 }} />
              </Box>
              <Typography variant="h5" sx={{ fontWeight: 800, mb: 1, color: 'text.primary' }}>
                Giao đồ ăn OmniFood
              </Typography>
              <Typography variant="body2" sx={{ color: 'text.secondary', fontSize: '0.92rem', lineHeight: 1.6 }}>
                Duyệt thực đơn phong phú từ các đối tác quán ăn đã được thẩm định, xử lý quy trình 3 bên chuẩn xác và giao đồ ăn tận tay nóng hổi.
              </Typography>
            </Box>

            {/* Visual Mini Mockup for OmniFood */}
            <Box
              sx={{
                p: 2,
                borderRadius: '16px',
                bgcolor: isDark ? '#0f172a' : '#f8fafc',
                border: `1px solid ${isDark ? 'rgba(255, 255, 255, 0.06)' : 'rgba(0, 0, 0, 0.05)'}`,
              }}
            >
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                <Box
                  sx={{
                    width: 40,
                    height: 40,
                    borderRadius: '10px',
                    bgcolor: '#fff7ed',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    fontSize: '1.2rem',
                  }}
                >
                  🍲
                </Box>
                <Box sx={{ flexGrow: 1 }}>
                  <Typography variant="body2" sx={{ fontWeight: 700, lineHeight: 1.2 }}>
                    Phở Bò Tái Nạm Đặc Biệt
                  </Typography>
                  <Typography variant="caption" sx={{ color: '#10b981', fontWeight: 700, display: 'flex', alignItems: 'center', gap: 0.4 }}>
                    <CheckIcon sx={{ fontSize: 13 }} /> Quán đã xác nhận chế biến
                  </Typography>
                </Box>
                <Chip size="small" label="65.000đ" color="warning" sx={{ fontWeight: 800, fontSize: '0.75rem' }} />
              </Box>
            </Box>
          </Paper>

          {/* Card 3: Seamless Payments (Col Span 5) */}
          <Paper
            elevation={0}
            sx={{
              gridColumn: { xs: 'span 1', md: 'span 5' },
              p: { xs: 3, sm: 4 },
              borderRadius: '24px',
              bgcolor: isDark ? '#1e293b' : '#ffffff',
              border: `1px solid ${isDark ? 'rgba(255, 255, 255, 0.08)' : 'rgba(0, 0, 0, 0.06)'}`,
              boxShadow: isDark
                ? '0 10px 30px rgba(0, 0, 0, 0.3)'
                : '0 10px 30px rgba(15, 23, 42, 0.04)',
              display: 'flex',
              flexDirection: 'column',
              justifyContent: 'space-between',
              transition: 'transform 0.25s ease, box-shadow 0.25s ease',
              '&:hover': {
                transform: 'translateY(-3px)',
                boxShadow: isDark
                  ? '0 16px 36px rgba(0, 0, 0, 0.45)'
                  : '0 16px 36px rgba(16, 185, 129, 0.1)',
              },
            }}
          >
            <Box sx={{ mb: 3 }}>
              <Box
                sx={{
                  width: 48,
                  height: 48,
                  borderRadius: '14px',
                  bgcolor: 'rgba(16, 185, 129, 0.12)',
                  color: '#10b981',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  mb: 2,
                }}
              >
                <PaymentIcon sx={{ fontSize: 28 }} />
              </Box>
              <Typography variant="h5" sx={{ fontWeight: 800, mb: 1, color: 'text.primary' }}>
                Thanh toán đa kênh tiện lợi
              </Typography>
              <Typography variant="body2" sx={{ color: 'text.secondary', fontSize: '0.92rem', lineHeight: 1.6 }}>
                Hỗ trợ linh hoạt tiền mặt, ví điện tử OmniPay và quét mã QR MoMo, VNPAY với quy trình đối soát minh bạch tức thì.
              </Typography>
            </Box>

            {/* Visual Mini Mockup for Payments */}
            <Box
              sx={{
                p: 2,
                borderRadius: '16px',
                bgcolor: isDark ? '#0f172a' : '#f8fafc',
                border: `1px solid ${isDark ? 'rgba(255, 255, 255, 0.06)' : 'rgba(0, 0, 0, 0.05)'}`,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-around',
                gap: 1,
              }}
            >
              <Chip
                label="Ví OmniPay"
                size="small"
                sx={{ fontWeight: 700, bgcolor: 'rgba(16, 185, 129, 0.15)', color: '#10b981' }}
              />
              <Chip
                label="VNPAY QR"
                size="small"
                sx={{ fontWeight: 700, bgcolor: 'rgba(0, 102, 204, 0.15)', color: '#0066cc' }}
              />
              <Chip
                label="Ví MoMo"
                size="small"
                sx={{ fontWeight: 700, bgcolor: 'rgba(216, 45, 139, 0.15)', color: '#d82d8b' }}
              />
              <Chip
                label="Tiền mặt"
                size="small"
                sx={{ fontWeight: 600, color: 'text.secondary' }}
              />
            </Box>
          </Paper>

          {/* Card 4: Live Tracking & Navigation (Col Span 7) */}
          <Paper
            elevation={0}
            sx={{
              gridColumn: { xs: 'span 1', md: 'span 7' },
              p: { xs: 3, sm: 4 },
              borderRadius: '24px',
              bgcolor: isDark ? '#1e293b' : '#ffffff',
              border: `1px solid ${isDark ? 'rgba(255, 255, 255, 0.08)' : 'rgba(0, 0, 0, 0.06)'}`,
              boxShadow: isDark
                ? '0 10px 30px rgba(0, 0, 0, 0.3)'
                : '0 10px 30px rgba(15, 23, 42, 0.04)',
              display: 'flex',
              flexDirection: 'column',
              justifyContent: 'space-between',
              transition: 'transform 0.25s ease, box-shadow 0.25s ease',
              '&:hover': {
                transform: 'translateY(-3px)',
                boxShadow: isDark
                  ? '0 16px 36px rgba(0, 0, 0, 0.45)'
                  : '0 16px 36px rgba(147, 51, 234, 0.1)',
              },
            }}
          >
            <Box sx={{ mb: 3 }}>
              <Box
                sx={{
                  width: 48,
                  height: 48,
                  borderRadius: '14px',
                  bgcolor: 'rgba(147, 51, 234, 0.12)',
                  color: '#9333ea',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  mb: 2,
                }}
              >
                <TrackingIcon sx={{ fontSize: 28 }} />
              </Box>
              <Typography variant="h5" sx={{ fontWeight: 800, mb: 1, color: 'text.primary' }}>
                Theo dõi vị trí trực tiếp (Live Tracking)
              </Typography>
              <Typography variant="body2" sx={{ color: 'text.secondary', fontSize: '0.92rem', lineHeight: 1.6, maxWidth: 520 }}>
                Hạ tầng định vị không gian thời gian thực hiển thị xe di chuyển liên tục trên bản đồ, tính toán thời gian tới điểm đón chính xác theo từng mét.
              </Typography>
            </Box>

            {/* Visual Mini Mockup for Tracking */}
            <Box
              sx={{
                p: 2,
                borderRadius: '16px',
                bgcolor: isDark ? '#0f172a' : '#f8fafc',
                border: `1px solid ${isDark ? 'rgba(255, 255, 255, 0.06)' : 'rgba(0, 0, 0, 0.05)'}`,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
              }}
            >
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                <Box
                  sx={{
                    width: 12,
                    height: 12,
                    borderRadius: '50%',
                    bgcolor: '#10b981',
                    boxShadow: '0 0 0 4px rgba(16, 185, 129, 0.25)',
                  }}
                />
                <Box>
                  <Typography variant="body2" sx={{ fontWeight: 700 }}>
                    Tài xế đang di chuyển tới điểm đón
                  </Typography>
                  <Typography variant="caption" sx={{ color: 'text.secondary', fontSize: '0.72rem' }}>
                    Tốc độ 32 km/h • Đường thông thoáng
                  </Typography>
                </Box>
              </Box>
              <Chip
                label="Dự kiến: 2 phút"
                size="small"
                color="secondary"
                sx={{ fontWeight: 800, fontSize: '0.75rem' }}
              />
            </Box>
          </Paper>
        </Box>
      </Container>
    </Box>
  );
};

export default BentoFeatures;
