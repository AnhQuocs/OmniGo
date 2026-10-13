import { useEffect } from 'react';
import { Box } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import LandingHeader from '../components/landing/LandingHeader';
import HeroSection from '../components/landing/HeroSection';
import BentoFeatures from '../components/landing/BentoFeatures';
import ProductShowcase from '../components/landing/ProductShowcase';
import PartnerSection from '../components/landing/PartnerSection';
import HowItWorksSection from '../components/landing/HowItWorksSection';
import FaqSection from '../components/landing/FaqSection';
import LandingFooter from '../components/landing/LandingFooter';

export const LandingPage = () => {
  const navigate = useNavigate();

  useEffect(() => {
    document.title = 'OmniGo - Siêu ứng dụng Đa Dịch Vụ: Đặt xe, Giao đồ ăn & Thanh toán';
    let metaDescription = document.querySelector('meta[name="description"]');
    if (!metaDescription) {
      metaDescription = document.createElement('meta');
      metaDescription.name = 'description';
      document.head.appendChild(metaDescription);
    }
    metaDescription.content =
      'OmniGo kết nối dịch vụ đặt chuyến xe OmniRide, giao món ngon OmniFood và thanh toán không tiền mặt an toàn, thông minh trên một nền tảng thống nhất.';
  }, []);

  const handleOpenRegister = () => {
    navigate('/login?register=partner');
  };

  return (
    <Box
      sx={{
        minHeight: '100vh',
        bgcolor: 'background.default',
        color: 'text.primary',
        display: 'flex',
        flexDirection: 'column',
      }}
    >
      {/* 1. Sticky Navigation Header */}
      <LandingHeader onOpenRegisterModal={handleOpenRegister} />

      {/* Main Content Sections */}
      <Box component="main" sx={{ flexGrow: 1 }}>
        {/* 2. Hero Section */}
        <HeroSection />

        {/* 3. Bento Grid Features */}
        <BentoFeatures />

        {/* 4. Product Showcase & Previews */}
        <ProductShowcase />

        {/* 5. Partner Ecosystem */}
        <PartnerSection onOpenRegisterModal={handleOpenRegister} />

        {/* 6. How it works */}
        <HowItWorksSection />

        {/* 7. FAQ */}
        <FaqSection />
      </Box>

      {/* 8. Footer with Final CTA */}
      <LandingFooter onOpenRegisterModal={handleOpenRegister} />
    </Box>
  );
};

export default LandingPage;
