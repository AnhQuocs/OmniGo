import { useState } from 'react';
import {
  Box,
  Container,
  Typography,
  Chip,
  Accordion,
  AccordionSummary,
  AccordionDetails,
} from '@mui/material';
import {
  ExpandMore as ExpandMoreIcon,
} from '@mui/icons-material';
import { useColorMode } from '../../context/ThemeContext';

export const FaqSection = () => {
  const { mode } = useColorMode();
  const isDark = mode === 'dark';

  const [expanded, setExpanded] = useState('panel0');

  const handleChange = (panel) => (event, isExpanded) => {
    setExpanded(isExpanded ? panel : false);
  };

  const faqs = [
    {
      id: 'panel0',
      question: 'Omni Go là gì và cung cấp những tiện ích nào?',
      answer:
        'Omni Go là nền tảng siêu ứng dụng đa dịch vụ kết hợp: Đặt chuyến xe thông minh (OmniRide), Giao nhận đồ ăn từ chuỗi quán ăn đối tác (OmniFood) và Thanh toán không tiền mặt bảo mật. Tất cả được liên kết và điều phối tự động theo thời gian thực.',
    },
    {
      id: 'panel1',
      question: 'Khách hàng sử dụng dịch vụ Omni Go trên nền tảng nào?',
      answer:
        'Khách hàng trải nghiệm đầy đủ các tiện ích đặt xe, gọi món ăn, theo dõi hành trình trực tiếp và thanh toán thông qua Ứng dụng di động Omni Go trên hệ điều hành Android.',
    },
    {
      id: 'panel2',
      question: 'Làm thế nào để đăng ký trở thành Đối tác Tài xế Omni Go?',
      answer:
        'Tài xế tải ứng dụng OmniGo Driver trên Android, đăng ký số điện thoại và chụp ảnh các giấy tờ pháp lý hợp lệ (2 mặt CCCD, 2 mặt Giấy phép lái xe, thông tin biển số xe). Sau khi Quản trị viên thẩm định và duyệt hồ sơ, tài xế có thể bật trạng thái Online và bắt đầu nhận chuyến.',
    },
    {
      id: 'panel3',
      question: 'Nhà hàng, quán ăn đăng ký và quản lý bán hàng như thế nào?',
      answer:
        'Chủ quán ăn đăng ký tài khoản trực tuyến qua nút "Đăng ký đối tác nhà hàng" trên Cổng Web Portal. Sau khi quản trị viên xét duyệt thông tin và giấy phép kinh doanh, quán có thể đăng nhập để cấu hình thực đơn, nhận chuông báo đơn mới và quản lý doanh thu trực tiếp trên trình duyệt.',
    },
    {
      id: 'panel4',
      question: 'Hệ thống hỗ trợ các phương thức thanh toán nào?',
      answer:
        'Người dùng có thể linh hoạt thanh toán bằng Tiền mặt khi nhận xe/món ăn, sử dụng Ví điện tử OmniPay hoặc thanh toán trực tuyến qua cổng MoMo và VNPAY với quy trình đối soát tự động minh bạch.',
    },
  ];

  return (
    <Box
      id="faq"
      component="section"
      sx={{
        py: { xs: 8, sm: 10, md: 12 },
        bgcolor: isDark ? '#0f172a' : '#f8fafc',
        position: 'relative',
      }}
    >
      <Container maxWidth="md" sx={{ px: { xs: 2, sm: 3 } }}>
        {/* Header */}
        <Box sx={{ textAlign: 'center', mb: { xs: 5, md: 7 } }}>
          <Chip
            label="CÂU HỎI THƯỜNG GẶP"
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
              fontSize: { xs: '1.85rem', sm: '2.3rem', md: '2.6rem' },
              color: isDark ? '#ffffff' : '#0f172a',
              letterSpacing: '-0.02em',
              mb: 2,
            }}
          >
            Giải đáp thắc mắc về Omni Go
          </Typography>
          <Typography
            variant="body1"
            sx={{
              fontSize: { xs: '0.98rem', sm: '1.05rem' },
              color: isDark ? '#94a3b8' : '#64748b',
              lineHeight: 1.6,
            }}
          >
            Thông tin chi tiết về hệ sinh thái, cách thức vận hành và hướng dẫn dành cho người dùng và đối tác.
          </Typography>
        </Box>

        {/* Accordions */}
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          {faqs.map((faq) => {
            const isItemExpanded = expanded === faq.id;
            return (
              <Accordion
                key={faq.id}
                expanded={isItemExpanded}
                onChange={handleChange(faq.id)}
                elevation={0}
                sx={{
                  borderRadius: '18px !important',
                  overflow: 'hidden',
                  bgcolor: isDark ? '#1e293b' : '#ffffff',
                  border: `1px solid ${
                    isItemExpanded
                      ? isDark
                        ? '#3b82f6'
                        : '#93c5fd'
                      : isDark
                      ? 'rgba(255, 255, 255, 0.08)'
                      : 'rgba(0, 0, 0, 0.06)'
                  }`,
                  boxShadow: isItemExpanded
                    ? isDark
                      ? '0 8px 24px rgba(0, 0, 0, 0.35)'
                      : '0 8px 24px rgba(59, 130, 246, 0.08)'
                    : 'none',
                  transition: 'all 0.2s ease',
                  '&:before': {
                    display: 'none',
                  },
                }}
              >
                <AccordionSummary
                  expandIcon={
                    <ExpandMoreIcon
                      sx={{
                        color: isItemExpanded ? '#f97316' : 'text.secondary',
                        transition: 'transform 0.25s ease',
                      }}
                    />
                  }
                  sx={{
                    px: { xs: 2.5, sm: 3 },
                    py: 1.2,
                    '& .MuiAccordionSummary-content': {
                      my: 1,
                    },
                  }}
                >
                  <Typography
                    variant="subtitle1"
                    sx={{
                      fontWeight: 700,
                      color: isItemExpanded ? (isDark ? '#38bdf8' : '#0284c7') : 'text.primary',
                      fontSize: { xs: '0.95rem', sm: '1.05rem' },
                    }}
                  >
                    {faq.question}
                  </Typography>
                </AccordionSummary>
                <AccordionDetails
                  sx={{
                    px: { xs: 2.5, sm: 3 },
                    pb: 2.5,
                    pt: 0,
                    borderTop: `1px solid ${isDark ? 'rgba(255,255,255,0.04)' : 'rgba(0,0,0,0.03)'}`,
                  }}
                >
                  <Typography
                    variant="body2"
                    sx={{
                      color: isDark ? '#cbd5e1' : '#475569',
                      fontSize: '0.92rem',
                      lineHeight: 1.7,
                      pt: 1.5,
                    }}
                  >
                    {faq.answer}
                  </Typography>
                </AccordionDetails>
              </Accordion>
            );
          })}
        </Box>
      </Container>
    </Box>
  );
};

export default FaqSection;
