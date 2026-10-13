import {
  Box,
  Container,
  Typography,
  Chip,
  Paper,
} from '@mui/material';
import {
  AppRegistration as Step1Icon,
  TouchApp as Step2Icon,
  DoneAll as Step3Icon,
} from '@mui/icons-material';
import { useColorMode } from '../../context/ThemeContext';

export const HowItWorksSection = () => {
  const { mode } = useColorMode();
  const isDark = mode === 'dark';

  const steps = [
    {
      step: '01',
      title: 'Chọn nền tảng phù hợp',
      desc: 'Khách hàng và đối tác tài xế sử dụng ứng dụng di động Android. Đối tác nhà hàng và quản trị viên vận hành trực tiếp trên Cổng Web Portal.',
      icon: <Step1Icon sx={{ fontSize: 28 }} />,
      badge: 'Nền tảng chuyên biệt',
    },
    {
      step: '02',
      title: 'Đăng ký & Xác thực',
      desc: 'Đăng ký nhanh chóng bằng số điện thoại. Đối tác tài xế nộp ảnh CCCD/GPLX, đối tác quán ăn hoàn tất thông tin quán và giấy phép kinh doanh để gửi duyệt.',
      icon: <Step2Icon sx={{ fontSize: 28 }} />,
      badge: 'Bảo mật & Chuẩn hóa',
    },
    {
      step: '03',
      title: 'Sử dụng & Trải nghiệm',
      desc: 'Khách hàng đặt xe và món ăn yêu thích tức thì; đối tác tài xế nhận cuốc tăng thu nhập; nhà hàng tiếp nhận và bàn giao đơn hàng nhịp nhàng.',
      icon: <Step3Icon sx={{ fontSize: 28 }} />,
      badge: 'Vận hành tức thì',
    },
  ];

  return (
    <Box
      id="how-it-works"
      component="section"
      sx={{
        py: { xs: 8, sm: 10, md: 12 },
        bgcolor: isDark ? '#111927' : '#ffffff',
        position: 'relative',
      }}
    >
      <Container maxWidth="lg" sx={{ px: { xs: 2, sm: 3, md: 4 } }}>
        <Box sx={{ textAlign: 'center', maxWidth: 760, mx: 'auto', mb: { xs: 5, md: 8 } }}>
          <Chip
            label="BẮT ĐẦU DỄ DÀNG"
            sx={{
              fontWeight: 800,
              fontSize: '0.78rem',
              letterSpacing: '0.06em',
              py: 0.5,
              px: 1,
              mb: 2,
              borderRadius: '999px',
              bgcolor: isDark ? 'rgba(249, 115, 22, 0.15)' : 'rgba(249, 115, 22, 0.1)',
              color: '#f97316',
              border: `1px solid ${isDark ? 'rgba(249, 115, 22, 0.3)' : 'rgba(249, 115, 22, 0.2)'}`,
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
            Hành trình trải nghiệm trong 3 bước
          </Typography>
          <Typography
            variant="body1"
            sx={{
              fontSize: { xs: '0.98rem', sm: '1.08rem' },
              color: isDark ? '#94a3b8' : '#64748b',
              lineHeight: 1.6,
            }}
          >
            Quy trình được tối ưu hóa ngắn gọn, minh bạch để người dùng và đối tác có thể bắt đầu sử dụng nhanh nhất.
          </Typography>
        </Box>

        <Box
          sx={{
            display: 'grid',
            gridTemplateColumns: { xs: '1fr', md: 'repeat(3, 1fr)' },
            gap: 3,
          }}
        >
          {steps.map((item, index) => (
            <Paper
              key={index}
              elevation={0}
              sx={{
                p: { xs: 3, sm: 4 },
                borderRadius: '24px',
                bgcolor: isDark ? '#1e293b' : '#f8fafc',
                border: `1px solid ${isDark ? 'rgba(255, 255, 255, 0.08)' : 'rgba(0, 0, 0, 0.06)'}`,
                boxShadow: isDark
                  ? '0 10px 30px rgba(0, 0, 0, 0.25)'
                  : '0 10px 30px rgba(15, 23, 42, 0.03)',
                display: 'flex',
                flexDirection: 'column',
                justifyContent: 'space-between',
                position: 'relative',
                transition: 'transform 0.25s ease',
                '&:hover': {
                  transform: 'translateY(-4px)',
                },
              }}
            >
              <Box>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
                  <Box
                    sx={{
                      width: 50,
                      height: 50,
                      borderRadius: '16px',
                      bgcolor: isDark ? 'rgba(249, 115, 22, 0.15)' : 'rgba(249, 115, 22, 0.1)',
                      color: '#f97316',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                    }}
                  >
                    {item.icon}
                  </Box>
                  <Typography
                    variant="h3"
                    sx={{
                      fontWeight: 900,
                      fontFamily: '"Plus Jakarta Sans", sans-serif',
                      color: isDark ? 'rgba(255, 255, 255, 0.12)' : 'rgba(0, 0, 0, 0.08)',
                      letterSpacing: '-0.03em',
                    }}
                  >
                    {item.step}
                  </Typography>
                </Box>

                <Chip
                  size="small"
                  label={item.badge}
                  sx={{
                    fontWeight: 700,
                    fontSize: '0.72rem',
                    mb: 1.5,
                    bgcolor: isDark ? 'rgba(255,255,255,0.06)' : 'rgba(0,0,0,0.05)',
                  }}
                />

                <Typography variant="h6" sx={{ fontWeight: 800, mb: 1.2, color: 'text.primary' }}>
                  {item.title}
                </Typography>
                <Typography variant="body2" sx={{ color: 'text.secondary', lineHeight: 1.6, fontSize: '0.9rem' }}>
                  {item.desc}
                </Typography>
              </Box>
            </Paper>
          ))}
        </Box>
      </Container>
    </Box>
  );
};

export default HowItWorksSection;
