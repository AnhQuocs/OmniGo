import { useState } from 'react';
import {
  Box,
  Container,
  Typography,
  Chip,
  Tabs,
  Tab,
  Paper,
  Stack,
  Button,
} from '@mui/material';
import {
  Smartphone as PhoneIcon,
  TwoWheeler as DriverIcon,
  Storefront as MerchantIcon,
  CheckCircle as CheckIcon,
  LocationOn as LocationIcon,
  Star as StarIcon,
} from '@mui/icons-material';
import { useColorMode } from '../../context/ThemeContext';

export const ProductShowcase = () => {
  const { mode } = useColorMode();
  const isDark = mode === 'dark';
  const [activeTab, setActiveTab] = useState(0);

  const tabsData = [
    {
      role: 'Khách hàng',
      platform: 'Ứng dụng Android',
      title: 'Mọi tiện ích di chuyển & ăn uống ngay trong lòng bàn tay',
      description:
        'Đặt chuyến xe đón ngay chỉ với vài thao tác, thưởng thức hàng trăm món ngon hấp dẫn và thanh toán không cần dùng tiền mặt.',
      highlights: [
        'Khảo sát giá cước trước chuyến đi, không lo phụ phí',
        'Theo dõi tài xế và shipper di chuyển trực tiếp trên bản đồ',
        'Lịch sử chuyến xe, đơn món rõ ràng kèm hóa đơn điện tử',
        'Ví điện tử tích hợp, hỗ trợ VNPAY và MoMo tiện lợi',
      ],
      mockupType: 'customer',
    },
    {
      role: 'Đối tác Tài xế',
      platform: 'Ứng dụng Tài xế (Android)',
      title: 'Tối ưu thu nhập & chủ động tuyệt đối thời gian làm việc',
      description:
        'Hệ thống phát cuốc thông minh bán kính gần, đếm ngược 20 giây nhận chuyến và đối soát doanh thu minh bạch sau mỗi hành trình.',
      highlights: [
        'Bật/tắt trạng thái nhận cuốc linh hoạt mọi lúc mọi nơi',
        'Nhận cả cuốc xe chở khách (OmniRide) lẫn đơn giao đồ ăn (OmniFood)',
        'Ví tài xế cập nhật tiền ròng tức thì ngay khi hoàn thành cuốc',
        'Bản đồ dẫn đường tối ưu điểm đón và điểm trả khách',
      ],
      mockupType: 'driver',
    },
    {
      role: 'Đối tác Nhà hàng',
      platform: 'Cổng Quản Trị Web Portal',
      title: 'Quản trị đơn món & thực đơn quán ăn tức thì trên trình duyệt',
      description:
        'Cổng Merchant Web Portal chuyên biệt giúp nhà hàng tiếp nhận đơn hàng thời gian thực, quản lý món ăn và xem biểu đồ tăng trưởng doanh thu.',
      highlights: [
        'Chuông báo nhận đơn thời gian thực không độ trễ',
        'Quản lý danh mục món ăn, giá tiền, hình ảnh trực quan',
        'Phân tích doanh thu theo ngày, tuần và số lượng đơn hàng',
        'Xác nhận chế biến 3 bên đồng bộ tức thì với shipper',
      ],
      mockupType: 'merchant',
    },
  ];

  const currentData = tabsData[activeTab];

  return (
    <Box
      id="showcase"
      component="section"
      sx={{
        py: { xs: 8, sm: 10, md: 12 },
        bgcolor: isDark ? '#111927' : '#ffffff',
        position: 'relative',
      }}
    >
      <Container maxWidth="lg" sx={{ px: { xs: 2, sm: 3, md: 4 } }}>
        {/* Section Header */}
        <Box sx={{ textAlign: 'center', maxWidth: 780, mx: 'auto', mb: { xs: 4, md: 6 } }}>
          <Chip
            label="TRẢI NGHIỆM SẢN PHẨM"
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
            Trải nghiệm tối ưu cho từng người dùng
          </Typography>
          <Typography
            variant="body1"
            sx={{
              fontSize: { xs: '0.98rem', sm: '1.08rem' },
              color: isDark ? '#94a3b8' : '#64748b',
              lineHeight: 1.6,
            }}
          >
            Omni Go phân tách rõ rệt công cụ trải nghiệm để đảm bảo độ tiện dụng cao nhất: ứng dụng di động cho khách hàng và tài xế, cổng Web Portal cho quán ăn.
          </Typography>
        </Box>

        {/* MD3 Tabs Selector */}
        <Box
          sx={{
            display: 'flex',
            justifyContent: 'center',
            mb: { xs: 4, md: 6 },
          }}
        >
          <Tabs
            value={activeTab}
            onChange={(e, val) => setActiveTab(val)}
            variant="scrollable"
            scrollButtons="auto"
            allowScrollButtonsMobile
            sx={{
              bgcolor: isDark ? '#0f172a' : '#f1f5f9',
              p: 0.8,
              borderRadius: '999px',
              minHeight: 'unset',
              '& .MuiTabs-indicator': {
                display: 'none',
              },
            }}
          >
            <Tab
              icon={<PhoneIcon sx={{ fontSize: 18 }} />}
              iconPosition="start"
              label="Khách hàng (Mobile App)"
              sx={{
                textTransform: 'none',
                fontWeight: 700,
                fontSize: '0.88rem',
                minHeight: 'unset',
                py: 1.1,
                px: { xs: 2, sm: 3 },
                borderRadius: '999px',
                color: isDark ? '#94a3b8' : '#64748b',
                '&.Mui-selected': {
                  color: '#ffffff',
                  bgcolor: '#f97316',
                  boxShadow: '0 4px 12px rgba(249, 115, 22, 0.35)',
                },
              }}
            />
            <Tab
              icon={<DriverIcon sx={{ fontSize: 18 }} />}
              iconPosition="start"
              label="Tài xế (Driver App)"
              sx={{
                textTransform: 'none',
                fontWeight: 700,
                fontSize: '0.88rem',
                minHeight: 'unset',
                py: 1.1,
                px: { xs: 2, sm: 3 },
                borderRadius: '999px',
                color: isDark ? '#94a3b8' : '#64748b',
                '&.Mui-selected': {
                  color: '#ffffff',
                  bgcolor: '#3b82f6',
                  boxShadow: '0 4px 12px rgba(59, 130, 246, 0.35)',
                },
              }}
            />
            <Tab
              icon={<MerchantIcon sx={{ fontSize: 18 }} />}
              iconPosition="start"
              label="Nhà hàng (Web Portal)"
              sx={{
                textTransform: 'none',
                fontWeight: 700,
                fontSize: '0.88rem',
                minHeight: 'unset',
                py: 1.1,
                px: { xs: 2, sm: 3 },
                borderRadius: '999px',
                color: isDark ? '#94a3b8' : '#64748b',
                '&.Mui-selected': {
                  color: '#ffffff',
                  bgcolor: '#10b981',
                  boxShadow: '0 4px 12px rgba(16, 185, 129, 0.35)',
                },
              }}
            />
          </Tabs>
        </Box>

        {/* Big Preview Frame (Inspired by Reference Image 2) */}
        <Paper
          elevation={0}
          sx={{
            borderRadius: { xs: '22px', md: '30px' },
            p: { xs: 3, sm: 4, md: 5 },
            bgcolor: isDark ? '#1e293b' : '#f8fafc',
            border: `1px solid ${isDark ? 'rgba(255, 255, 255, 0.08)' : 'rgba(0, 0, 0, 0.06)'}`,
            boxShadow: isDark
              ? '0 20px 50px rgba(0, 0, 0, 0.4)'
              : '0 20px 50px rgba(15, 23, 42, 0.06)',
          }}
        >
          <Box
            sx={{
              display: 'grid',
              gridTemplateColumns: { xs: '1fr', lg: '1fr 1.2fr' },
              gap: { xs: 4, md: 5 },
              alignItems: 'center',
            }}
          >
            {/* Left: Role Description & Benefit List */}
            <Box>
              <Chip
                label={currentData.platform}
                size="small"
                sx={{
                  fontWeight: 800,
                  fontSize: '0.75rem',
                  mb: 2,
                  bgcolor:
                    activeTab === 0
                      ? 'rgba(249, 115, 22, 0.12)'
                      : activeTab === 1
                      ? 'rgba(59, 130, 246, 0.12)'
                      : 'rgba(16, 185, 129, 0.12)',
                  color:
                    activeTab === 0
                      ? '#f97316'
                      : activeTab === 1
                      ? '#3b82f6'
                      : '#10b981',
                }}
              />
              <Typography variant="h4" sx={{ fontWeight: 800, color: 'text.primary', mb: 2, lineHeight: 1.25 }}>
                {currentData.title}
              </Typography>
              <Typography variant="body1" sx={{ color: 'text.secondary', lineHeight: 1.6, mb: 3.5, fontSize: '0.98rem' }}>
                {currentData.description}
              </Typography>

              {/* Highlights List */}
              <Stack spacing={1.8}>
                {currentData.highlights.map((item, idx) => (
                  <Box key={idx} sx={{ display: 'flex', alignItems: 'flex-start', gap: 1.5 }}>
                    <Box
                      sx={{
                        width: 22,
                        height: 22,
                        borderRadius: '50%',
                        bgcolor:
                          activeTab === 0
                            ? 'rgba(249, 115, 22, 0.15)'
                            : activeTab === 1
                            ? 'rgba(59, 130, 246, 0.15)'
                            : 'rgba(16, 185, 129, 0.15)',
                        color:
                          activeTab === 0
                            ? '#f97316'
                            : activeTab === 1
                            ? '#3b82f6'
                            : '#10b981',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        flexShrink: 0,
                        mt: '2px',
                      }}
                    >
                      <CheckIcon sx={{ fontSize: 15 }} />
                    </Box>
                    <Typography variant="body2" sx={{ fontWeight: 600, color: 'text.primary', fontSize: '0.92rem' }}>
                      {item}
                    </Typography>
                  </Box>
                ))}
              </Stack>
            </Box>

            {/* Right: Rich UI Vector Mockup */}
            <Box
              sx={{
                bgcolor: isDark ? '#0f172a' : '#ffffff',
                borderRadius: '24px',
                p: { xs: 2.5, sm: 3 },
                border: `1px solid ${isDark ? 'rgba(255, 255, 255, 0.08)' : 'rgba(0, 0, 0, 0.08)'}`,
                boxShadow: isDark
                  ? '0 12px 30px rgba(0, 0, 0, 0.5)'
                  : '0 12px 30px rgba(0, 0, 0, 0.04)',
              }}
            >
              {/* RENDER MOCKUP 0: CUSTOMER APP */}
              {activeTab === 0 && (
                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5 }}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <Box
                        sx={{
                          width: 28,
                          height: 28,
                          borderRadius: '8px',
                          bgcolor: '#f97316',
                          color: '#fff',
                          fontWeight: 900,
                          fontSize: '0.85rem',
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center',
                        }}
                      >
                        O
                      </Box>
                      <Typography variant="subtitle2" sx={{ fontWeight: 800 }}>
                        OmniGo Khách Hàng
                      </Typography>
                    </Box>
                    <Chip size="small" label="Android App" sx={{ fontSize: '0.7rem', height: 20 }} />
                  </Box>

                  {/* Customer Quick Booking Banner */}
                  <Box
                    sx={{
                      p: 2,
                      borderRadius: '16px',
                      bgcolor: isDark ? 'rgba(249, 115, 22, 0.12)' : '#fff7ed',
                      border: '1px solid rgba(249, 115, 22, 0.25)',
                    }}
                  >
                    <Typography variant="caption" sx={{ fontWeight: 700, color: '#f97316', textTransform: 'uppercase', fontSize: '0.7rem' }}>
                      ⚡ Đặt xe trong 1 chạm
                    </Typography>
                    <Typography variant="subtitle1" sx={{ fontWeight: 800, color: 'text.primary', mt: 0.3 }}>
                      Bạn muốn đi đâu hôm nay?
                    </Typography>
                    <Box
                      sx={{
                        mt: 1.5,
                        p: 1.2,
                        borderRadius: '10px',
                        bgcolor: isDark ? '#1e293b' : '#ffffff',
                        border: `1px solid ${isDark ? 'rgba(255,255,255,0.08)' : '#e2e8f0'}`,
                        display: 'flex',
                        alignItems: 'center',
                        gap: 1,
                      }}
                    >
                      <LocationIcon sx={{ color: '#f97316', fontSize: 18 }} />
                      <Typography variant="body2" sx={{ color: 'text.secondary', fontSize: '0.85rem' }}>
                        Nhập điểm đến (Trường học, Công ty, TTTM...)
                      </Typography>
                    </Box>
                  </Box>

                  {/* Customer Recent Ride History Card */}
                  <Box
                    sx={{
                      p: 2,
                      borderRadius: '16px',
                      bgcolor: isDark ? 'rgba(255,255,255,0.03)' : '#f8fafc',
                      border: `1px solid ${isDark ? 'rgba(255,255,255,0.06)' : '#e2e8f0'}`,
                    }}
                  >
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1.2 }}>
                      <Typography variant="caption" sx={{ fontWeight: 700, color: 'text.secondary' }}>
                        CHUYẾN ĐI VỪA HOÀN TẤT
                      </Typography>
                      <Chip size="small" label="Hoàn thành" color="success" sx={{ fontSize: '0.68rem', height: 20, fontWeight: 700 }} />
                    </Box>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <Box>
                        <Typography variant="body2" sx={{ fontWeight: 700 }}>
                          OmniBike • 4.2 km
                        </Typography>
                        <Typography variant="caption" sx={{ color: 'text.secondary' }}>
                          Thanh toán Ví OmniPay • 28.000đ
                        </Typography>
                      </Box>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.3, color: '#f59e0b' }}>
                        <StarIcon sx={{ fontSize: 16 }} />
                        <Typography variant="caption" sx={{ fontWeight: 800, color: 'text.primary' }}>
                          5.0
                        </Typography>
                      </Box>
                    </Box>
                  </Box>
                </Box>
              )}

              {/* RENDER MOCKUP 1: DRIVER APP */}
              {activeTab === 1 && (
                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5 }}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <Box
                        sx={{
                          width: 28,
                          height: 28,
                          borderRadius: '8px',
                          bgcolor: '#3b82f6',
                          color: '#fff',
                          fontWeight: 900,
                          fontSize: '0.85rem',
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center',
                        }}
                      >
                        T
                      </Box>
                      <Typography variant="subtitle2" sx={{ fontWeight: 800 }}>
                        OmniGo Tài Xế
                      </Typography>
                    </Box>
                    <Chip size="small" label="ONLINE • Sẵn sàng" color="success" sx={{ fontSize: '0.7rem', height: 22, fontWeight: 700 }} />
                  </Box>

                  {/* Dispatching Offer Modal Mockup */}
                  <Box
                    sx={{
                      p: 2.5,
                      borderRadius: '16px',
                      bgcolor: isDark ? 'rgba(59, 130, 246, 0.15)' : '#eff6ff',
                      border: '2px solid #3b82f6',
                      position: 'relative',
                    }}
                  >
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
                      <Typography variant="subtitle2" sx={{ fontWeight: 800, color: '#3b82f6' }}>
                        🔔 CÓ CUỐC XE MỚI!
                      </Typography>
                      <Chip label="18s còn lại" size="small" color="primary" sx={{ fontWeight: 800, fontSize: '0.72rem' }} />
                    </Box>
                    <Typography variant="body2" sx={{ fontWeight: 700, mb: 0.5 }}>
                      Đón tại: 144 Xuân Thủy, Cầu Giấy
                    </Typography>
                    <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block', mb: 2 }}>
                      Quãng đường 3.5km • Thu nhập ròng: 27.200đ
                    </Typography>
                    <Button
                      fullWidth
                      variant="contained"
                      sx={{ bgcolor: '#3b82f6', '&:hover': { bgcolor: '#2563eb' }, fontWeight: 700, borderRadius: '10px' }}
                    >
                      Nhận Cuốc Ngay (Accept)
                    </Button>
                  </Box>

                  {/* Today Income Meter */}
                  <Box
                    sx={{
                      p: 2,
                      borderRadius: '16px',
                      bgcolor: isDark ? 'rgba(255,255,255,0.03)' : '#f8fafc',
                      border: `1px solid ${isDark ? 'rgba(255,255,255,0.06)' : '#e2e8f0'}`,
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center',
                    }}
                  >
                    <Box>
                      <Typography variant="caption" sx={{ color: 'text.secondary' }}>
                        THU NHẬP HÔM NAY (12 CUỐC)
                      </Typography>
                      <Typography variant="h6" sx={{ fontWeight: 800, color: '#10b981' }}>
                        485.000 VNĐ
                      </Typography>
                    </Box>
                    <Chip label="Ví: 1.250.000đ" size="small" sx={{ fontWeight: 700 }} />
                  </Box>
                </Box>
              )}

              {/* RENDER MOCKUP 2: MERCHANT PORTAL */}
              {activeTab === 2 && (
                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5 }}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <Box
                        sx={{
                          width: 28,
                          height: 28,
                          borderRadius: '8px',
                          bgcolor: '#10b981',
                          color: '#fff',
                          fontWeight: 900,
                          fontSize: '0.85rem',
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center',
                        }}
                      >
                        M
                      </Box>
                      <Typography variant="subtitle2" sx={{ fontWeight: 800 }}>
                        Cổng Quản Trị Quán Ăn (Web)
                      </Typography>
                    </Box>
                    <Chip size="small" label="Mở cửa đón khách" color="success" sx={{ fontSize: '0.7rem', height: 20, fontWeight: 700 }} />
                  </Box>

                  {/* Merchant Live Order Queue */}
                  <Box
                    sx={{
                      p: 2,
                      borderRadius: '16px',
                      bgcolor: isDark ? 'rgba(16, 185, 129, 0.12)' : '#ecfdf5',
                      border: '1px solid rgba(16, 185, 129, 0.25)',
                    }}
                  >
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
                      <Typography variant="subtitle2" sx={{ fontWeight: 800, color: '#10b981' }}>
                        Đơn #OF-2026-9824
                      </Typography>
                      <Chip label="Đang nấu" size="small" color="success" sx={{ fontWeight: 700, fontSize: '0.7rem' }} />
                    </Box>
                    <Typography variant="body2" sx={{ fontWeight: 700 }}>
                      2x Bún Chả Hà Nội Đặc Biệt (Nem giòn)
                    </Typography>
                    <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block', mb: 1.5 }}>
                      Khách: Nguyễn Minh Tuấn • Shipper đang đến quán (2 phút)
                    </Typography>
                    <Button
                      size="small"
                      variant="contained"
                      sx={{ bgcolor: '#10b981', '&:hover': { bgcolor: '#059669' }, fontWeight: 700, textTransform: 'none', borderRadius: '8px' }}
                    >
                      Bàn Giao Cho Tài Xế
                    </Button>
                  </Box>

                  {/* Merchant Daily Stats */}
                  <Box sx={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 1.5 }}>
                    <Box
                      sx={{
                        p: 1.5,
                        borderRadius: '14px',
                        bgcolor: isDark ? 'rgba(255,255,255,0.03)' : '#f8fafc',
                        border: `1px solid ${isDark ? 'rgba(255,255,255,0.06)' : '#e2e8f0'}`,
                      }}
                    >
                      <Typography variant="caption" sx={{ color: 'text.secondary' }}>
                        ĐƠN HÀNG HÔM NAY
                      </Typography>
                      <Typography variant="subtitle1" sx={{ fontWeight: 800 }}>
                        48 đơn (96% hoàn thành)
                      </Typography>
                    </Box>
                    <Box
                      sx={{
                        p: 1.5,
                        borderRadius: '14px',
                        bgcolor: isDark ? 'rgba(255,255,255,0.03)' : '#f8fafc',
                        border: `1px solid ${isDark ? 'rgba(255,255,255,0.06)' : '#e2e8f0'}`,
                      }}
                    >
                      <Typography variant="caption" sx={{ color: 'text.secondary' }}>
                        DOANH THU NGÀY
                      </Typography>
                      <Typography variant="subtitle1" sx={{ fontWeight: 800, color: '#f97316' }}>
                        3.450.000đ
                      </Typography>
                    </Box>
                  </Box>
                </Box>
              )}
            </Box>
          </Box>
        </Paper>
      </Container>
    </Box>
  );
};

export default ProductShowcase;
