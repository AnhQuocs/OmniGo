import {
  Box,
  Container,
  Typography,
  Button,
  Chip,
  Paper,
  Stack,
  Avatar,
} from '@mui/material';
import {
  ExploreOutlined as ExploreIcon,
  HandshakeOutlined as PartnerIcon,
  DirectionsCarFilled as CarIcon,
  Restaurant as FoodIcon,
  CheckCircle as CheckIcon,
  Star as StarIcon,
  AccessTime as TimeIcon,
  LocationOn as LocationIcon,
  AccountBalanceWallet as WalletIcon,
  TwoWheeler as BikeIcon,
} from '@mui/icons-material';
import { useColorMode } from '../../context/ThemeContext';

export const HeroSection = () => {
  const { mode } = useColorMode();
  const isDark = mode === 'dark';

  const scrollToSection = (id) => {
    const el = document.getElementById(id);
    if (el) {
      const headerOffset = 76;
      const elementPosition = el.getBoundingClientRect().top;
      const offsetPosition = elementPosition + window.pageYOffset - headerOffset;
      window.scrollTo({
        top: offsetPosition,
        behavior: 'smooth',
      });
    }
  };

  return (
    <Box
      component="section"
      sx={{
        position: 'relative',
        pt: { xs: 5, sm: 8, md: 10 },
        pb: { xs: 8, sm: 11, md: 14 },
        overflow: 'hidden',
        background: isDark
          ? 'radial-gradient(ellipse 80% 50% at 50% -10%, rgba(249, 115, 22, 0.15), transparent 70%), #0f172a'
          : 'radial-gradient(ellipse 80% 50% at 50% -10%, rgba(249, 115, 22, 0.08), transparent 70%), #f8fafc',
      }}
    >
      <Container maxWidth="lg" sx={{ px: { xs: 2, sm: 3, md: 4 } }}>
        {/* Top Centered Content */}
        <Box
          sx={{
            maxWidth: 820,
            mx: 'auto',
            textAlign: 'center',
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            mb: { xs: 6, md: 8 },
          }}
        >
          {/* Pill Badge */}
          <Chip
            label="✨ HỆ SINH THÁI DỊCH VỤ SỐ OMNIGO"
            sx={{
              fontWeight: 800,
              fontSize: '0.78rem',
              letterSpacing: '0.06em',
              py: 0.6,
              px: 1,
              mb: 3,
              borderRadius: '999px',
              bgcolor: isDark ? 'rgba(249, 115, 22, 0.15)' : 'rgba(249, 115, 22, 0.1)',
              color: '#f97316',
              border: `1px solid ${isDark ? 'rgba(249, 115, 22, 0.3)' : 'rgba(249, 115, 22, 0.25)'}`,
            }}
          />

          {/* Main Headline */}
          <Typography
            component="h1"
            sx={{
              fontWeight: 800,
              fontFamily: '"Plus Jakarta Sans", sans-serif',
              fontSize: { xs: '2.15rem', sm: '2.9rem', md: '3.6rem' },
              lineHeight: { xs: 1.2, sm: 1.18, md: 1.15 },
              letterSpacing: '-0.03em',
              color: isDark ? '#ffffff' : '#0f172a',
              mb: 2.5,
            }}
          >
            Di chuyển, đặt món —{' '}
            <Box
              component="span"
              sx={{
                background: 'linear-gradient(135deg, #f97316 20%, #fb923c 60%, #3b82f6 100%)',
                WebkitBackgroundClip: 'text',
                WebkitTextFillColor: 'transparent',
              }}
            >
              tiện hơn cùng Omni Go
            </Box>
          </Typography>

          {/* Subtitle Description */}
          <Typography
            variant="body1"
            sx={{
              fontSize: { xs: '1rem', sm: '1.12rem', md: '1.2rem' },
              color: isDark ? '#cbd5e1' : '#475569',
              lineHeight: 1.6,
              maxWidth: 680,
              mb: 4,
            }}
          >
            Nền tảng kết nối toàn diện nhu cầu di chuyển tức thì, đặt món ngon từ đối tác ẩm thực chất lượng và thanh toán không tiền mặt an toàn, minh bạch.
          </Typography>

          {/* Action CTAs */}
          <Stack
            direction={{ xs: 'column', sm: 'row' }}
            spacing={2}
            sx={{ width: { xs: '100%', sm: 'auto' }, justifyContent: 'center' }}
          >
            <Button
              variant="contained"
              size="large"
              onClick={() => scrollToSection('features')}
              startIcon={<ExploreIcon />}
              sx={{
                borderRadius: '999px',
                px: 3.5,
                py: 1.5,
                fontSize: '1rem',
                fontWeight: 700,
                textTransform: 'none',
                bgcolor: '#f97316',
                color: '#ffffff',
                boxShadow: '0 8px 24px rgba(249, 115, 22, 0.35)',
                '&:hover': {
                  bgcolor: '#ea580c',
                  boxShadow: '0 10px 28px rgba(234, 88, 12, 0.45)',
                  transform: 'translateY(-2px)',
                },
                transition: 'all 0.2s ease',
              }}
            >
              Khám phá tiện ích
            </Button>

            <Button
              variant="outlined"
              size="large"
              onClick={() => scrollToSection('partners')}
              startIcon={<PartnerIcon />}
              sx={{
                borderRadius: '999px',
                px: 3.5,
                py: 1.5,
                fontSize: '1rem',
                fontWeight: 700,
                textTransform: 'none',
                borderColor: isDark ? 'rgba(255, 255, 255, 0.2)' : 'rgba(0, 0, 0, 0.15)',
                color: isDark ? '#ffffff' : '#0f172a',
                bgcolor: isDark ? 'rgba(255, 255, 255, 0.03)' : 'rgba(255, 255, 255, 0.6)',
                '&:hover': {
                  borderColor: '#f97316',
                  bgcolor: isDark ? 'rgba(249, 115, 22, 0.08)' : 'rgba(249, 115, 22, 0.05)',
                  transform: 'translateY(-2px)',
                },
                transition: 'all 0.2s ease',
              }}
            >
              Dành cho đối tác
            </Button>
          </Stack>
        </Box>

        {/* Visual Centerpiece Mockup Area with Floating Cards */}
        <Box
          sx={{
            position: 'relative',
            maxWidth: 1040,
            mx: 'auto',
            pt: { xs: 2, md: 5 },
            pb: { xs: 2, md: 4 },
          }}
        >
          {/* Main App Showcase Card Frame */}
          <Paper
            elevation={0}
            sx={{
              borderRadius: { xs: '20px', md: '28px' },
              border: `1px solid ${isDark ? 'rgba(255, 255, 255, 0.1)' : 'rgba(0, 0, 0, 0.08)'}`,
              bgcolor: isDark ? '#1e293b' : '#ffffff',
              boxShadow: isDark
                ? '0 25px 60px -15px rgba(0, 0, 0, 0.7), 0 0 0 1px rgba(255,255,255,0.05)'
                : '0 25px 60px -15px rgba(15, 23, 42, 0.12), 0 0 0 1px rgba(0,0,0,0.03)',
              overflow: 'hidden',
              position: 'relative',
              p: { xs: 2, sm: 3, md: 4 },
              mt: { xs: 0, lg: 1.5 },
            }}
          >
            {/* Top Mockup Header Bar */}
            <Box
              sx={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                flexWrap: 'wrap',
                gap: 2,
                pl: { xs: 0, lg: '240px' },
                pb: 2.5,
                mb: 3,
                borderBottom: `1px solid ${isDark ? 'rgba(255, 255, 255, 0.08)' : 'rgba(0, 0, 0, 0.06)'}`,
              }}
            >
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                <Box
                  sx={{
                    width: 32,
                    height: 32,
                    borderRadius: '50%',
                    bgcolor: 'rgba(249, 115, 22, 0.15)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    color: '#f97316',
                  }}
                >
                  <LocationIcon sx={{ fontSize: 18 }} />
                </Box>
                <Box>
                  <Typography variant="subtitle2" sx={{ fontWeight: 800, color: 'text.primary', lineHeight: 1.2 }}>
                    Vị trí hiện tại: Quận Cầu Giấy, Hà Nội
                  </Typography>
                  <Typography variant="caption" sx={{ color: 'text.secondary', fontSize: '0.75rem' }}>
                    Sẵn sàng kết nối tài xế & nhà hàng gần bạn
                  </Typography>
                </Box>
              </Box>

              <Chip
                size="small"
                label="Mô phỏng giao diện"
                sx={{
                  fontSize: '0.72rem',
                  fontWeight: 600,
                  bgcolor: isDark ? 'rgba(255, 255, 255, 0.06)' : 'rgba(0, 0, 0, 0.04)',
                  color: 'text.secondary',
                }}
              />
            </Box>

            {/* Central Mockup Grid Content */}
            <Box
              sx={{
                display: 'grid',
                gridTemplateColumns: { xs: '1fr', md: '1.2fr 1fr' },
                gap: 3,
                alignItems: 'center',
              }}
            >
              {/* Left Column: OmniRide Booking Demo Card */}
              <Box
                sx={{
                  p: { xs: 2, sm: 2.5 },
                  borderRadius: '20px',
                  bgcolor: isDark ? '#0f172a' : '#f8fafc',
                  border: `1px solid ${isDark ? 'rgba(255, 255, 255, 0.08)' : 'rgba(0, 0, 0, 0.06)'}`,
                }}
              >
                <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }}>
                  <Typography variant="subtitle1" sx={{ fontWeight: 800, display: 'flex', alignItems: 'center', gap: 1 }}>
                    <CarIcon sx={{ color: '#3b82f6', fontSize: 20 }} />
                    OmniRide • Di Chuyển Nhanh
                  </Typography>
                  <Chip
                    size="small"
                    label="Đang tìm tài xế"
                    color="primary"
                    sx={{ fontWeight: 700, fontSize: '0.72rem' }}
                  />
                </Box>

                {/* Route Route Visualization */}
                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5, mb: 2.5 }}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                    <Box sx={{ width: 10, height: 10, borderRadius: '50%', bgcolor: '#10b981' }} />
                    <Box sx={{ flexGrow: 1 }}>
                      <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block', fontSize: '0.7rem' }}>
                        Điểm đón (Pick-up)
                      </Typography>
                      <Typography variant="body2" sx={{ fontWeight: 700, color: 'text.primary' }}>
                        Toà nhà Keangnam Landmark 72, Mễ Trì
                      </Typography>
                    </Box>
                  </Box>
                  <Box
                    sx={{
                      width: '2px',
                      height: '14px',
                      bgcolor: 'divider',
                      ml: '4px',
                    }}
                  />
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                    <Box sx={{ width: 10, height: 10, borderRadius: '50%', bgcolor: '#f97316' }} />
                    <Box sx={{ flexGrow: 1 }}>
                      <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block', fontSize: '0.7rem' }}>
                        Điểm đến (Drop-off)
                      </Typography>
                      <Typography variant="body2" sx={{ fontWeight: 700, color: 'text.primary' }}>
                        Vincom Center Trần Duy Hưng, Cầu Giấy
                      </Typography>
                    </Box>
                  </Box>
                </Box>

                {/* Vehicle Selection Preview */}
                <Box sx={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 1.5 }}>
                  <Box
                    sx={{
                      p: 1.5,
                      borderRadius: '14px',
                      bgcolor: isDark ? 'rgba(59, 130, 246, 0.15)' : 'rgba(59, 130, 246, 0.08)',
                      border: '1.5px solid #3b82f6',
                      display: 'flex',
                      flexDirection: 'column',
                      gap: 0.5,
                    }}
                  >
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <BikeIcon sx={{ color: '#3b82f6', fontSize: 22 }} />
                      <Typography variant="caption" sx={{ fontWeight: 800, color: '#3b82f6' }}>
                        25.000đ
                      </Typography>
                    </Box>
                    <Typography variant="body2" sx={{ fontWeight: 700, fontSize: '0.82rem' }}>
                      OmniBike
                    </Typography>
                    <Typography variant="caption" sx={{ color: 'text.secondary', fontSize: '0.7rem' }}>
                      Đón sau 3 phút • Tiện lợi
                    </Typography>
                  </Box>

                  <Box
                    sx={{
                      p: 1.5,
                      borderRadius: '14px',
                      bgcolor: isDark ? 'rgba(255, 255, 255, 0.04)' : '#ffffff',
                      border: `1px solid ${isDark ? 'rgba(255, 255, 255, 0.08)' : 'rgba(0, 0, 0, 0.06)'}`,
                      display: 'flex',
                      flexDirection: 'column',
                      gap: 0.5,
                    }}
                  >
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <CarIcon sx={{ color: 'text.secondary', fontSize: 22 }} />
                      <Typography variant="caption" sx={{ fontWeight: 700, color: 'text.primary' }}>
                        55.000đ
                      </Typography>
                    </Box>
                    <Typography variant="body2" sx={{ fontWeight: 700, fontSize: '0.82rem' }}>
                      OmniCar 4 chỗ
                    </Typography>
                    <Typography variant="caption" sx={{ color: 'text.secondary', fontSize: '0.7rem' }}>
                      Thoải mái • Mát mẻ
                    </Typography>
                  </Box>
                </Box>
              </Box>

              {/* Right Column: OmniFood Live Status Demo */}
              <Box
                sx={{
                  p: { xs: 2, sm: 2.5 },
                  borderRadius: '20px',
                  bgcolor: isDark ? '#0f172a' : '#f8fafc',
                  border: `1px solid ${isDark ? 'rgba(255, 255, 255, 0.08)' : 'rgba(0, 0, 0, 0.06)'}`,
                  display: 'flex',
                  flexDirection: 'column',
                  gap: 2,
                }}
              >
                <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                  <Typography variant="subtitle1" sx={{ fontWeight: 800, display: 'flex', alignItems: 'center', gap: 1 }}>
                    <FoodIcon sx={{ color: '#f97316', fontSize: 20 }} />
                    OmniFood • Đơn Đang Xử Lý
                  </Typography>
                  <Chip
                    size="small"
                    icon={<TimeIcon sx={{ fontSize: '14px !important' }} />}
                    label="Còn 12 phút"
                    sx={{
                      fontWeight: 700,
                      fontSize: '0.72rem',
                      bgcolor: 'rgba(249, 115, 22, 0.12)',
                      color: '#f97316',
                    }}
                  />
                </Box>

                {/* Dish Item Mock */}
                <Box
                  sx={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: 1.5,
                    p: 1.5,
                    borderRadius: '14px',
                    bgcolor: isDark ? 'rgba(255, 255, 255, 0.04)' : '#ffffff',
                    border: `1px solid ${isDark ? 'rgba(255, 255, 255, 0.06)' : 'rgba(0, 0, 0, 0.05)'}`,
                  }}
                >
                  <Box
                    sx={{
                      width: 44,
                      height: 44,
                      borderRadius: '10px',
                      bgcolor: '#fff7ed',
                      color: '#f97316',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      fontWeight: 800,
                      fontSize: '1.2rem',
                    }}
                  >
                    🍱
                  </Box>
                  <Box sx={{ flexGrow: 1 }}>
                    <Typography variant="body2" sx={{ fontWeight: 700, color: 'text.primary', lineHeight: 1.2 }}>
                      Cơm Tấm Sườn Bì Chả Đặc Biệt
                    </Typography>
                    <Typography variant="caption" sx={{ color: 'text.secondary', fontSize: '0.72rem' }}>
                      Cơm Tấm Sài Gòn 1 • 2 suất
                    </Typography>
                  </Box>
                  <Typography variant="body2" sx={{ fontWeight: 800, color: '#f97316' }}>
                    120.000đ
                  </Typography>
                </Box>

                {/* Three-party progress indicator */}
                <Box sx={{ px: 0.5 }}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                    <Typography variant="caption" sx={{ fontWeight: 700, color: '#10b981', display: 'flex', alignItems: 'center', gap: 0.5 }}>
                      <CheckIcon sx={{ fontSize: 13 }} /> Quán đã nhận
                    </Typography>
                    <Typography variant="caption" sx={{ fontWeight: 700, color: '#f97316' }}>
                      Đang chuẩn bị món
                    </Typography>
                    <Typography variant="caption" sx={{ color: 'text.secondary' }}>
                      Shipper nhận
                    </Typography>
                  </Box>
                  <Box
                    sx={{
                      height: 6,
                      borderRadius: '999px',
                      bgcolor: isDark ? 'rgba(255, 255, 255, 0.1)' : 'rgba(0, 0, 0, 0.08)',
                      overflow: 'hidden',
                    }}
                  >
                    <Box
                      sx={{
                        width: '65%',
                        height: '100%',
                        borderRadius: '999px',
                        background: 'linear-gradient(90deg, #10b981 0%, #f97316 100%)',
                      }}
                    />
                  </Box>
                </Box>
              </Box>
            </Box>
          </Paper>

          {/* Floating UI Badges surrounding central mockup (Desktop view) */}
          {/* Card 1: Top-Left Driver Match Notification - Positioned with zero overlap */}
          <Paper
            elevation={0}
            sx={{
              display: { xs: 'none', lg: 'flex' },
              alignItems: 'center',
              gap: 1.5,
              position: 'absolute',
              top: 10,
              left: 10,
              p: 1.6,
              borderRadius: '20px',
              bgcolor: isDark ? 'rgba(30, 41, 59, 0.98)' : '#ffffff',
              border: `1px solid ${isDark ? 'rgba(255, 255, 255, 0.15)' : 'rgba(0, 0, 0, 0.1)'}`,
              boxShadow: isDark
                ? '0 16px 36px rgba(0, 0, 0, 0.5)'
                : '0 16px 36px rgba(15, 23, 42, 0.12)',
              backdropFilter: 'blur(10px)',
              maxWidth: 240,
              zIndex: 3,
            }}
          >
            <Avatar sx={{ bgcolor: '#3b82f6', width: 40, height: 40, fontWeight: 700, fontSize: '0.9rem' }}>
              QĐ
            </Avatar>
            <Box>
              <Typography variant="body2" sx={{ fontWeight: 800, lineHeight: 1.2, color: 'text.primary' }}>
                Tài xế Quang Đạt
              </Typography>
              <Typography variant="caption" sx={{ color: 'text.secondary', display: 'flex', alignItems: 'center', gap: 0.4 }}>
                <StarIcon sx={{ color: '#f59e0b', fontSize: 13 }} /> 4.9 • 29B1-888.88
              </Typography>
              <Typography variant="caption" sx={{ color: '#10b981', fontWeight: 700, display: 'block', fontSize: '0.7rem' }}>
                Cách bạn 350m (2 phút)
              </Typography>
            </Box>
          </Paper>

          {/* Card 2: Bottom-Right Instant Payment Verification */}
          <Paper
            elevation={0}
            sx={{
              display: { xs: 'none', lg: 'flex' },
              alignItems: 'center',
              gap: 1.5,
              position: 'absolute',
              bottom: -18,
              right: { lg: 30, xl: 40 },
              p: 1.6,
              borderRadius: '20px',
              bgcolor: isDark ? 'rgba(30, 41, 59, 0.98)' : '#ffffff',
              border: `1px solid ${isDark ? 'rgba(255, 255, 255, 0.15)' : 'rgba(0, 0, 0, 0.1)'}`,
              boxShadow: isDark
                ? '0 16px 36px rgba(0, 0, 0, 0.5)'
                : '0 16px 36px rgba(15, 23, 42, 0.12)',
              backdropFilter: 'blur(10px)',
              maxWidth: 260,
              zIndex: 3,
            }}
          >
            <Box
              sx={{
                width: 40,
                height: 40,
                borderRadius: '12px',
                bgcolor: 'rgba(16, 185, 129, 0.15)',
                color: '#10b981',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
              }}
            >
              <WalletIcon sx={{ fontSize: 22 }} />
            </Box>
            <Box>
              <Typography variant="body2" sx={{ fontWeight: 800, color: 'text.primary', lineHeight: 1.2 }}>
                Thanh toán thành công
              </Typography>
              <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block', fontSize: '0.72rem' }}>
                Ví OmniPay / Cổng VNPAY
              </Typography>
              <Typography variant="caption" sx={{ color: '#10b981', fontWeight: 800 }}>
                Đối soát tức thì • Bảo mật
              </Typography>
            </Box>
          </Paper>
        </Box>
      </Container>
    </Box>
  );
};

export default HeroSection;
