import {
  Box,
  Container,
  Typography,
  Chip,
  Paper,
  Button,
  Stack,
} from '@mui/material';
import {
  TwoWheeler as DriverIcon,
  Storefront as MerchantIcon,
  AdminPanelSettings as AdminIcon,
  CheckCircleOutlined as CheckIcon,
  ArrowForward as ArrowIcon,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import { useColorMode } from '../../context/ThemeContext';

export const PartnerSection = ({ onOpenRegisterModal }) => {
  const navigate = useNavigate();
  const { mode } = useColorMode();
  const isDark = mode === 'dark';
  const { isAuthenticated, user } = useSelector((state) => state.auth);

  return (
    <Box
      id="partners"
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
            label="DÀNH CHO ĐỐI TÁC"
            sx={{
              fontWeight: 800,
              fontSize: '0.78rem',
              letterSpacing: '0.06em',
              py: 0.5,
              px: 1,
              mb: 2,
              borderRadius: '999px',
              bgcolor: isDark ? 'rgba(16, 185, 129, 0.15)' : 'rgba(16, 185, 129, 0.1)',
              color: '#10b981',
              border: `1px solid ${isDark ? 'rgba(16, 185, 129, 0.3)' : 'rgba(16, 185, 129, 0.2)'}`,
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
            Đồng hành & gia tăng thu nhập bền vững
          </Typography>
          <Typography
            variant="body1"
            sx={{
              fontSize: { xs: '0.98rem', sm: '1.08rem' },
              color: isDark ? '#94a3b8' : '#64748b',
              lineHeight: 1.6,
            }}
          >
            Gia nhập cộng đồng đối tác của Omni Go để tiếp cận hàng nghìn khách hàng tiềm năng với công cụ quản lý chuyên nghiệp, minh bạch và an toàn.
          </Typography>
        </Box>

        {/* 2 Main Partner Cards: Drivers & Merchants */}
        <Box
          sx={{
            display: 'grid',
            gridTemplateColumns: { xs: '1fr', md: '1fr 1fr' },
            gap: { xs: 3, md: 4 },
            mb: 4,
          }}
        >
          {/* Card 1: Driver Partner */}
          <Paper
            elevation={0}
            sx={{
              p: { xs: 3.5, sm: 4.5 },
              borderRadius: '26px',
              bgcolor: isDark ? '#1e293b' : '#ffffff',
              border: `1px solid ${isDark ? 'rgba(255, 255, 255, 0.08)' : 'rgba(0, 0, 0, 0.06)'}`,
              boxShadow: isDark
                ? '0 12px 32px rgba(0, 0, 0, 0.3)'
                : '0 12px 32px rgba(15, 23, 42, 0.04)',
              display: 'flex',
              flexDirection: 'column',
              justifyContent: 'space-between',
              transition: 'transform 0.25s ease',
              '&:hover': {
                transform: 'translateY(-4px)',
              },
            }}
          >
            <Box>
              <Box
                sx={{
                  width: 52,
                  height: 52,
                  borderRadius: '16px',
                  bgcolor: 'rgba(59, 130, 246, 0.12)',
                  color: '#3b82f6',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  mb: 2.5,
                }}
              >
                <DriverIcon sx={{ fontSize: 32 }} />
              </Box>

              <Typography variant="h5" sx={{ fontWeight: 800, mb: 1.5, color: 'text.primary' }}>
                Đối tác Tài xế OmniGo
              </Typography>
              <Typography variant="body2" sx={{ color: 'text.secondary', fontSize: '0.92rem', lineHeight: 1.6, mb: 3 }}>
                Gia nhập đội ngũ tài xế tự do, nhận cả chuyến chở khách lẫn đơn giao đồ ăn. Cùng hệ thống điều phối thông minh giúp giảm tối đa thời gian chờ cuốc.
              </Typography>

              <Stack spacing={1.5} sx={{ mb: 4 }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                  <CheckIcon sx={{ color: '#3b82f6', fontSize: 18 }} />
                  <Typography variant="body2" sx={{ fontWeight: 600 }}>
                    Chủ động thời gian bật/tắt nhận chuyến linh hoạt
                  </Typography>
                </Box>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                  <CheckIcon sx={{ color: '#3b82f6', fontSize: 18 }} />
                  <Typography variant="body2" sx={{ fontWeight: 600 }}>
                    Thu nhập minh bạch, đối soát tức thì qua ví tài xế
                  </Typography>
                </Box>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                  <CheckIcon sx={{ color: '#3b82f6', fontSize: 18 }} />
                  <Typography variant="body2" sx={{ fontWeight: 600 }}>
                    Quy trình nộp hồ sơ trực tuyến (CCCD, GPLX) trên app
                  </Typography>
                </Box>
              </Stack>
            </Box>

            <Box
              sx={{
                p: 2,
                borderRadius: '14px',
                bgcolor: isDark ? '#0f172a' : '#f8fafc',
                border: `1px solid ${isDark ? 'rgba(255,255,255,0.06)' : '#e2e8f0'}`,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
              }}
            >
              <Box>
                <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block', fontSize: '0.72rem' }}>
                  ĐĂNG KÝ QUA ỨNG DỤNG
                </Typography>
                <Typography variant="subtitle2" sx={{ fontWeight: 800, color: 'text.primary' }}>
                  Tải app OmniGo Driver trên Android
                </Typography>
              </Box>
              <Chip
                label="Hồ sơ Online"
                size="small"
                color="primary"
                sx={{ fontWeight: 700, fontSize: '0.72rem' }}
              />
            </Box>
          </Paper>

          {/* Card 2: Merchant Partner */}
          <Paper
            elevation={0}
            sx={{
              p: { xs: 3.5, sm: 4.5 },
              borderRadius: '26px',
              bgcolor: isDark ? '#1e293b' : '#ffffff',
              border: `1px solid ${isDark ? 'rgba(255, 255, 255, 0.08)' : 'rgba(0, 0, 0, 0.06)'}`,
              boxShadow: isDark
                ? '0 12px 32px rgba(0, 0, 0, 0.3)'
                : '0 12px 32px rgba(15, 23, 42, 0.04)',
              display: 'flex',
              flexDirection: 'column',
              justifyContent: 'space-between',
              transition: 'transform 0.25s ease',
              '&:hover': {
                transform: 'translateY(-4px)',
              },
            }}
          >
            <Box>
              <Box
                sx={{
                  width: 52,
                  height: 52,
                  borderRadius: '16px',
                  bgcolor: 'rgba(249, 115, 22, 0.12)',
                  color: '#f97316',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  mb: 2.5,
                }}
              >
                <MerchantIcon sx={{ fontSize: 32 }} />
              </Box>

              <Typography variant="h5" sx={{ fontWeight: 800, mb: 1.5, color: 'text.primary' }}>
                Đối tác Quán ăn OmniFood
              </Typography>
              <Typography variant="body2" sx={{ color: 'text.secondary', fontSize: '0.92rem', lineHeight: 1.6, mb: 3 }}>
                Đưa thực đơn quán tiếp cận hàng nghìn thực khách mỗi ngày. Quản lý thực đơn, nhận chuông báo đơn mới và theo dõi doanh thu trực tiếp trên Web Portal.
              </Typography>

              <Stack spacing={1.5} sx={{ mb: 4 }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                  <CheckIcon sx={{ color: '#f97316', fontSize: 18 }} />
                  <Typography variant="body2" sx={{ fontWeight: 600 }}>
                    Tiếp nhận và xử lý đơn hàng theo thời gian thực
                  </Typography>
                </Box>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                  <CheckIcon sx={{ color: '#f97316', fontSize: 18 }} />
                  <Typography variant="body2" sx={{ fontWeight: 600 }}>
                    Tự do điều chỉnh danh mục món ăn & giá bán dễ dàng
                  </Typography>
                </Box>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                  <CheckIcon sx={{ color: '#f97316', fontSize: 18 }} />
                  <Typography variant="body2" sx={{ fontWeight: 600 }}>
                    Báo cáo tài chính, tỷ lệ đơn hoàn tất chi tiết
                  </Typography>
                </Box>
              </Stack>
            </Box>

            <Box sx={{ display: 'flex', gap: 1.5, flexDirection: { xs: 'column', sm: 'row' } }}>
              <Button
                variant="contained"
                onClick={() => {
                  if (onOpenRegisterModal) {
                    onOpenRegisterModal();
                  } else {
                    navigate('/login');
                  }
                }}
                endIcon={<ArrowIcon sx={{ fontSize: 16 }} />}
                sx={{
                  flex: 1,
                  py: 1.3,
                  borderRadius: '12px',
                  fontWeight: 700,
                  fontSize: '0.9rem',
                  textTransform: 'none',
                  bgcolor: '#f97316',
                  color: '#ffffff',
                  '&:hover': { bgcolor: '#ea580c' },
                }}
              >
                Đăng ký mở quán ngay
              </Button>

              <Button
                variant="outlined"
                onClick={() => navigate('/login')}
                sx={{
                  py: 1.3,
                  borderRadius: '12px',
                  fontWeight: 700,
                  fontSize: '0.9rem',
                  textTransform: 'none',
                }}
              >
                Đăng nhập cổng quán
              </Button>
            </Box>
          </Paper>
        </Box>

        {/* System Administration Trust Banner (Short & Informative) */}
        <Paper
          elevation={0}
          sx={{
            p: { xs: 2.5, sm: 3 },
            borderRadius: '20px',
            bgcolor: isDark ? 'rgba(255, 255, 255, 0.03)' : 'rgba(0, 0, 0, 0.02)',
            border: `1px solid ${isDark ? 'rgba(255, 255, 255, 0.06)' : 'rgba(0, 0, 0, 0.06)'}`,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            gap: 2,
            flexWrap: { xs: 'wrap', md: 'nowrap' },
          }}
        >
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            <Box
              sx={{
                width: 44,
                height: 44,
                borderRadius: '12px',
                bgcolor: isDark ? 'rgba(255, 255, 255, 0.06)' : '#ffffff',
                color: 'text.primary',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                flexShrink: 0,
              }}
            >
              <AdminIcon sx={{ fontSize: 24 }} />
            </Box>
            <Box>
              <Typography variant="subtitle2" sx={{ fontWeight: 800, color: 'text.primary' }}>
                Hạ tầng Vận hành & Quản trị Hệ thống
              </Typography>
              <Typography variant="caption" sx={{ color: 'text.secondary', fontSize: '0.8rem' }}>
                Đội ngũ Quản trị viên OmniGo trực tiếp thẩm định hồ sơ đối tác, đối soát giao dịch và duy trì an toàn vận hành 24/7.
              </Typography>
            </Box>
          </Box>

          <Button
            size="small"
            variant="text"
            onClick={() => navigate(isAuthenticated && user?.role === 'ADMIN' ? '/dashboard' : '/login')}
            endIcon={<ArrowIcon sx={{ fontSize: 14 }} />}
            sx={{ fontWeight: 700, textTransform: 'none', color: 'text.primary', flexShrink: 0 }}
          >
            {isAuthenticated && user?.role === 'ADMIN' ? 'Vào Dashboard Quản Trị' : 'Cổng Đăng Nhập Quản Trị'}
          </Button>
        </Paper>
      </Container>
    </Box>
  );
};

export default PartnerSection;
