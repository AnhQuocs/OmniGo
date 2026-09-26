import {
  Box,
  Container,
  Typography,
  Button,
  Divider,
  Stack,
} from '@mui/material';
import {
  ArrowForward as ArrowIcon,
  Login as LoginIcon,
  Store as StoreIcon,
  Dashboard as DashboardIcon,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import { useColorMode } from '../../context/ThemeContext';

export const LandingFooter = ({ onOpenRegisterModal }) => {
  const navigate = useNavigate();
  const { mode } = useColorMode();
  const isDark = mode === 'dark';
  const { isAuthenticated, user } = useSelector((state) => state.auth);

  const scrollToTop = () => {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

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
      component="footer"
      sx={{
        bgcolor: isDark ? '#0b111e' : '#f1f5f9',
        pt: { xs: 8, md: 10 },
        pb: 5,
        borderTop: `1px solid ${isDark ? 'rgba(255, 255, 255, 0.08)' : 'rgba(0, 0, 0, 0.08)'}`,
      }}
    >
      <Container maxWidth="lg" sx={{ px: { xs: 2, sm: 3, md: 4 } }}>
        {/* Pre-Footer Action Box */}
        <Box
          sx={{
            p: { xs: 3.5, sm: 5, md: 6 },
            borderRadius: { xs: '22px', md: '30px' },
            bgcolor: isDark ? '#1e293b' : '#ffffff',
            border: `1px solid ${isDark ? 'rgba(255, 255, 255, 0.08)' : 'rgba(0, 0, 0, 0.06)'}`,
            boxShadow: isDark
              ? '0 16px 40px rgba(0, 0, 0, 0.4)'
              : '0 16px 40px rgba(15, 23, 42, 0.05)',
            textAlign: 'center',
            maxWidth: 880,
            mx: 'auto',
            mb: { xs: 7, md: 9 },
          }}
        >
          <Typography
            variant="h4"
            sx={{
              fontWeight: 800,
              fontFamily: '"Plus Jakarta Sans", sans-serif',
              fontSize: { xs: '1.7rem', sm: '2.1rem', md: '2.4rem' },
              color: isDark ? '#ffffff' : '#0f172a',
              letterSpacing: '-0.02em',
              mb: 1.5,
            }}
          >
            Trải nghiệm siêu ứng dụng Omni Go ngay hôm nay
          </Typography>
          <Typography
            variant="body1"
            sx={{
              color: isDark ? '#cbd5e1' : '#64748b',
              fontSize: { xs: '0.95rem', md: '1.05rem' },
              maxWidth: 620,
              mx: 'auto',
              mb: 4,
              lineHeight: 1.6,
            }}
          >
            Nền tảng kết nối liền mạch tiện ích đặt xe, giao đồ ăn nhanh chóng và thanh toán tiện lợi cho hàng triệu khách hàng và đối tác.
          </Typography>

          <Stack
            direction={{ xs: 'column', sm: 'row' }}
            spacing={2}
            sx={{ justifyContent: 'center' }}
          >
            <Button
              variant="contained"
              size="large"
              onClick={() => scrollToSection('features')}
              endIcon={<ArrowIcon sx={{ fontSize: 18 }} />}
              sx={{
                borderRadius: '999px',
                px: 4,
                py: 1.4,
                fontWeight: 700,
                fontSize: '0.98rem',
                textTransform: 'none',
                bgcolor: '#f97316',
                color: '#ffffff',
                boxShadow: '0 6px 20px rgba(249, 115, 22, 0.35)',
                '&:hover': {
                  bgcolor: '#ea580c',
                },
              }}
            >
              Khám phá tiện ích
            </Button>

            <Button
              variant="outlined"
              size="large"
              onClick={() => {
                if (onOpenRegisterModal) {
                  onOpenRegisterModal();
                } else {
                  navigate('/login');
                }
              }}
              startIcon={<StoreIcon />}
              sx={{
                borderRadius: '999px',
                px: 3.5,
                py: 1.4,
                fontWeight: 700,
                fontSize: '0.98rem',
                textTransform: 'none',
              }}
            >
              Đăng ký mở quán OmniFood
            </Button>
          </Stack>
        </Box>

        {/* Footer Navigation Columns */}
        <Box
          sx={{
            display: 'grid',
            gridTemplateColumns: { xs: '1fr', sm: '2fr 1fr 1fr' },
            gap: 4,
            mb: 5,
          }}
        >
          {/* Brand Info */}
          <Box>
            <Box
              sx={{ display: 'flex', alignItems: 'center', gap: 1.2, mb: 1.5, cursor: 'pointer' }}
              onClick={scrollToTop}
            >
              <Box
                sx={{
                  width: 34,
                  height: 34,
                  borderRadius: '10px',
                  bgcolor: '#f97316',
                  color: '#ffffff',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontWeight: 900,
                  fontSize: '1.1rem',
                }}
              >
                O
              </Box>
              <Typography variant="h6" sx={{ fontWeight: 800, color: 'text.primary', letterSpacing: '-0.02em' }}>
                OmniGo
              </Typography>
            </Box>
            <Typography variant="body2" sx={{ color: 'text.secondary', maxWidth: 360, lineHeight: 1.6, fontSize: '0.88rem' }}>
              Hệ thống siêu ứng dụng đa dịch vụ: Đặt xe di chuyển, Giao nhận đồ ăn trực tuyến và Thanh toán không tiền mặt bảo mật.
            </Typography>
          </Box>

          {/* Quick Links */}
          <Box>
            <Typography variant="subtitle2" sx={{ fontWeight: 800, color: 'text.primary', mb: 2, textTransform: 'uppercase', fontSize: '0.75rem', letterSpacing: '0.05em' }}>
              Khám Phá
            </Typography>
            <Stack spacing={1.2}>
              <Box
                component="a"
                href="#features"
                onClick={(e) => {
                  e.preventDefault();
                  scrollToSection('features');
                }}
                sx={{ color: 'text.secondary', textDecoration: 'none', fontSize: '0.88rem', '&:hover': { color: '#f97316' } }}
              >
                Tiện ích nổi bật
              </Box>
              <Box
                component="a"
                href="#showcase"
                onClick={(e) => {
                  e.preventDefault();
                  scrollToSection('showcase');
                }}
                sx={{ color: 'text.secondary', textDecoration: 'none', fontSize: '0.88rem', '&:hover': { color: '#f97316' } }}
              >
                Trải nghiệm sản phẩm
              </Box>
              <Box
                component="a"
                href="#partners"
                onClick={(e) => {
                  e.preventDefault();
                  scrollToSection('partners');
                }}
                sx={{ color: 'text.secondary', textDecoration: 'none', fontSize: '0.88rem', '&:hover': { color: '#f97316' } }}
              >
                Dành cho đối tác
              </Box>
              <Box
                component="a"
                href="#faq"
                onClick={(e) => {
                  e.preventDefault();
                  scrollToSection('faq');
                }}
                sx={{ color: 'text.secondary', textDecoration: 'none', fontSize: '0.88rem', '&:hover': { color: '#f97316' } }}
              >
                Câu hỏi thường gặp
              </Box>
            </Stack>
          </Box>

          {/* Portal Access */}
          <Box>
            <Typography variant="subtitle2" sx={{ fontWeight: 800, color: 'text.primary', mb: 2, textTransform: 'uppercase', fontSize: '0.75rem', letterSpacing: '0.05em' }}>
              Cổng Quản Trị & Đối Tác
            </Typography>
            <Stack spacing={1.2}>
              <Box
                component="a"
                href="/login"
                onClick={(e) => {
                  e.preventDefault();
                  navigate('/login');
                }}
                sx={{ color: 'text.secondary', textDecoration: 'none', fontSize: '0.88rem', display: 'flex', alignItems: 'center', gap: 0.8, '&:hover': { color: '#f97316' } }}
              >
                <LoginIcon sx={{ fontSize: 16 }} /> Đăng nhập hệ thống
              </Box>
              <Box
                component="a"
                href="/merchant/orders"
                onClick={(e) => {
                  e.preventDefault();
                  navigate(isAuthenticated && user?.role === 'RESTAURANT' ? '/merchant/orders' : '/login');
                }}
                sx={{ color: 'text.secondary', textDecoration: 'none', fontSize: '0.88rem', display: 'flex', alignItems: 'center', gap: 0.8, '&:hover': { color: '#f97316' } }}
              >
                <StoreIcon sx={{ fontSize: 16 }} /> Cổng quán ăn (Merchant)
              </Box>
              <Box
                component="a"
                href="/dashboard"
                onClick={(e) => {
                  e.preventDefault();
                  navigate(isAuthenticated && user?.role === 'ADMIN' ? '/dashboard' : '/login');
                }}
                sx={{ color: 'text.secondary', textDecoration: 'none', fontSize: '0.88rem', display: 'flex', alignItems: 'center', gap: 0.8, '&:hover': { color: '#f97316' } }}
              >
                <DashboardIcon sx={{ fontSize: 16 }} /> Quản trị sàn (Admin)
              </Box>
            </Stack>
          </Box>
        </Box>

        <Divider sx={{ my: 3, borderColor: isDark ? 'rgba(255,255,255,0.06)' : 'rgba(0,0,0,0.06)' }} />

        {/* Bottom Copyright */}
        <Box
          sx={{
            display: 'flex',
            flexDirection: { xs: 'column', sm: 'row' },
            justifyContent: 'space-between',
            alignItems: 'center',
            gap: 1.5,
          }}
        >
          <Typography variant="caption" sx={{ color: 'text.secondary', fontSize: '0.8rem' }}>
            © {new Date().getFullYear()} OmniGo Platform. Bản quyền thuộc về hệ thống OmniGo.
          </Typography>
          <Typography variant="caption" sx={{ color: 'text.secondary', fontSize: '0.78rem' }}>
            Nền tảng Đa Dịch Vụ: Đặt xe • Đồ ăn • Thanh toán
          </Typography>
        </Box>
      </Container>
    </Box>
  );
};

export default LandingFooter;
