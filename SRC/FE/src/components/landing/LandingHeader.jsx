import { useState, useEffect } from 'react';
import {
  AppBar,
  Toolbar,
  Container,
  Box,
  Typography,
  Button,
  IconButton,
  Drawer,
  List,
  ListItem,
  ListItemButton,
  ListItemText,
} from '@mui/material';
import {
  LightMode as LightIcon,
  DarkMode as DarkIcon,
  Menu as MenuIcon,
  Close as CloseIcon,
  ArrowForward as ArrowForwardIcon,
  Dashboard as DashboardIcon,
  Store as StoreIcon,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import { useColorMode } from '../../context/ThemeContext';

export const LandingHeader = () => {
  const navigate = useNavigate();
  const { mode, toggleColorMode } = useColorMode();
  const isDark = mode === 'dark';

  const { isAuthenticated, user } = useSelector((state) => state.auth);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [isScrolled, setIsScrolled] = useState(false);

  useEffect(() => {
    const handleScroll = () => {
      setIsScrolled(window.scrollY > 20);
    };
    window.addEventListener('scroll', handleScroll, { passive: true });
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  const navLinks = [
    { label: 'Tiện ích', href: '#features' },
    { label: 'Trải nghiệm', href: '#showcase' },
    { label: 'Đối tác', href: '#partners' },
    { label: 'Bắt đầu', href: '#how-it-works' },
    { label: 'FAQ', href: '#faq' },
  ];

  const handleNavClick = (e, href) => {
    e.preventDefault();
    setMobileMenuOpen(false);
    const element = document.querySelector(href);
    if (element) {
      const headerOffset = 76;
      const elementPosition = element.getBoundingClientRect().top;
      const offsetPosition = elementPosition + window.pageYOffset - headerOffset;
      window.scrollTo({
        top: offsetPosition,
        behavior: 'smooth',
      });
    }
  };

  const renderAuthCta = () => {
    if (!isAuthenticated || !user) {
      return (
        <Button
          variant="contained"
          onClick={() => navigate('/login')}
          endIcon={<ArrowForwardIcon sx={{ fontSize: 16 }} />}
          sx={{
            borderRadius: '999px',
            px: 2.5,
            py: 0.9,
            fontWeight: 700,
            fontSize: '0.88rem',
            textTransform: 'none',
            bgcolor: '#f97316',
            color: '#ffffff',
            boxShadow: '0 4px 14px rgba(249, 115, 22, 0.35)',
            '&:hover': {
              bgcolor: '#ea580c',
              boxShadow: '0 6px 18px rgba(234, 88, 12, 0.45)',
            },
          }}
        >
          Đăng nhập
        </Button>
      );
    }

    if (user.role === 'ADMIN') {
      return (
        <Button
          variant="contained"
          onClick={() => navigate('/dashboard')}
          startIcon={<DashboardIcon sx={{ fontSize: 18 }} />}
          sx={{
            borderRadius: '999px',
            px: 2.5,
            py: 0.9,
            fontWeight: 700,
            fontSize: '0.88rem',
            textTransform: 'none',
            bgcolor: '#f97316',
            color: '#ffffff',
            '&:hover': { bgcolor: '#ea580c' },
          }}
        >
          Vào Dashboard
        </Button>
      );
    }

    if (user.role === 'RESTAURANT') {
      return (
        <Button
          variant="contained"
          onClick={() => navigate('/merchant/orders')}
          startIcon={<StoreIcon sx={{ fontSize: 18 }} />}
          sx={{
            borderRadius: '999px',
            px: 2.5,
            py: 0.9,
            fontWeight: 700,
            fontSize: '0.88rem',
            textTransform: 'none',
            bgcolor: '#10b981',
            color: '#ffffff',
            '&:hover': { bgcolor: '#059669' },
          }}
        >
          Cổng Quán Ăn
        </Button>
      );
    }

    return (
      <Button
        variant="contained"
        onClick={() => navigate('/login')}
        sx={{
          borderRadius: '999px',
          px: 2.5,
          py: 0.9,
          fontWeight: 700,
          textTransform: 'none',
        }}
      >
        Tài khoản
      </Button>
    );
  };

  return (
    <AppBar
      position="sticky"
      elevation={0}
      sx={{
        top: 0,
        zIndex: 1100,
        transition: 'all 0.25s ease-in-out',
        bgcolor: isScrolled
          ? isDark
            ? 'rgba(15, 23, 42, 0.85)'
            : 'rgba(255, 255, 255, 0.88)'
          : isDark
          ? 'rgba(15, 23, 42, 0.6)'
          : 'rgba(255, 255, 255, 0.7)',
        backdropFilter: 'blur(16px)',
        borderBottom: `1px solid ${
          isScrolled
            ? isDark
              ? 'rgba(255, 255, 255, 0.08)'
              : 'rgba(0, 0, 0, 0.06)'
            : 'transparent'
        }`,
      }}
    >
      <Container maxWidth="lg" sx={{ px: { xs: 2, sm: 3, md: 4 } }}>
        <Toolbar
          disableGutters
          sx={{
            minHeight: { xs: 64, md: 72 },
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
          }}
        >
          {/* Brand Logo */}
          <Box
            component="a"
            href="/"
            onClick={(e) => {
              e.preventDefault();
              window.scrollTo({ top: 0, behavior: 'smooth' });
            }}
            sx={{
              display: 'flex',
              alignItems: 'center',
              gap: 1.2,
              textDecoration: 'none',
              cursor: 'pointer',
              userSelect: 'none',
            }}
            aria-label="Về đầu trang OmniGo"
          >
            <Box
              sx={{
                width: 38,
                height: 38,
                borderRadius: '12px',
                bgcolor: '#f97316',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: '#ffffff',
                fontWeight: 900,
                fontSize: '1.25rem',
                boxShadow: '0 4px 12px rgba(249, 115, 22, 0.35)',
                transition: 'transform 0.2s ease',
                '&:hover': {
                  transform: 'scale(1.04)',
                },
              }}
            >
              O
            </Box>
            <Box sx={{ display: 'flex', flexDirection: 'column' }}>
              <Typography
                variant="h6"
                sx={{
                  fontWeight: 800,
                  fontFamily: '"Plus Jakarta Sans", sans-serif',
                  fontSize: { xs: '1.2rem', md: '1.35rem' },
                  letterSpacing: '-0.02em',
                  color: isDark ? '#ffffff' : '#0f172a',
                  lineHeight: 1,
                }}
              >
                OmniGo
              </Typography>
              <Typography
                variant="caption"
                sx={{
                  fontSize: '0.68rem',
                  fontWeight: 700,
                  color: '#f97316',
                  letterSpacing: '0.08em',
                  textTransform: 'uppercase',
                  lineHeight: 1.2,
                  mt: 0.3,
                }}
              >
                Ride • Food • Pay
              </Typography>
            </Box>
          </Box>

          {/* Desktop Navigation Links */}
          <Box
            component="nav"
            sx={{
              display: { xs: 'none', md: 'flex' },
              alignItems: 'center',
              gap: 1,
              bgcolor: isDark ? 'rgba(255, 255, 255, 0.04)' : 'rgba(0, 0, 0, 0.03)',
              p: 0.6,
              borderRadius: '999px',
              border: `1px solid ${isDark ? 'rgba(255, 255, 255, 0.06)' : 'rgba(0, 0, 0, 0.05)'}`,
            }}
            aria-label="Điều hướng chính"
          >
            {navLinks.map((link) => (
              <Box
                key={link.href}
                component="a"
                href={link.href}
                onClick={(e) => handleNavClick(e, link.href)}
                sx={{
                  px: 2,
                  py: 0.7,
                  borderRadius: '999px',
                  fontSize: '0.88rem',
                  fontWeight: 600,
                  color: isDark ? '#cbd5e1' : '#475569',
                  textDecoration: 'none',
                  transition: 'all 0.2s ease',
                  '&:hover': {
                    color: isDark ? '#ffffff' : '#0f172a',
                    bgcolor: isDark ? 'rgba(255, 255, 255, 0.08)' : 'rgba(0, 0, 0, 0.06)',
                  },
                }}
              >
                {link.label}
              </Box>
            ))}
          </Box>

          {/* Right Action Controls */}
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
            {/* Theme Toggle Button */}
            <IconButton
              onClick={toggleColorMode}
              size="small"
              aria-label={`Chuyển sang chế độ ${isDark ? 'Sáng' : 'Tối'}`}
              sx={{
                width: 40,
                height: 40,
                borderRadius: '12px',
                border: `1px solid ${isDark ? 'rgba(255, 255, 255, 0.1)' : 'rgba(0, 0, 0, 0.08)'}`,
                bgcolor: isDark ? 'rgba(255, 255, 255, 0.04)' : 'rgba(0, 0, 0, 0.03)',
                color: isDark ? '#f8fafc' : '#1e293b',
                transition: 'all 0.2s ease',
                '&:hover': {
                  bgcolor: isDark ? 'rgba(255, 255, 255, 0.1)' : 'rgba(0, 0, 0, 0.06)',
                  transform: 'rotate(15deg)',
                },
              }}
            >
              {isDark ? (
                <LightIcon sx={{ fontSize: 20, color: '#f59e0b' }} />
              ) : (
                <DarkIcon sx={{ fontSize: 20, color: '#475569' }} />
              )}
            </IconButton>

            {/* Desktop Auth CTA */}
            <Box sx={{ display: { xs: 'none', md: 'block' } }}>
              {renderAuthCta()}
            </Box>

            {/* Mobile Hamburger Menu Toggle */}
            <IconButton
              edge="end"
              onClick={() => setMobileMenuOpen(true)}
              aria-label="Mở menu điều hướng"
              aria-expanded={mobileMenuOpen}
              sx={{
                display: { md: 'none' },
                width: 40,
                height: 40,
                borderRadius: '12px',
                border: `1px solid ${isDark ? 'rgba(255, 255, 255, 0.1)' : 'rgba(0, 0, 0, 0.08)'}`,
                bgcolor: isDark ? 'rgba(255, 255, 255, 0.04)' : 'rgba(0, 0, 0, 0.03)',
                color: isDark ? '#ffffff' : '#0f172a',
              }}
            >
              <MenuIcon sx={{ fontSize: 22 }} />
            </IconButton>
          </Box>
        </Toolbar>
      </Container>

      {/* Mobile Navigation Drawer */}
      <Drawer
        anchor="right"
        open={mobileMenuOpen}
        onClose={() => setMobileMenuOpen(false)}
        ModalProps={{ keepMounted: true }}
        PaperProps={{
          sx: {
            width: 290,
            bgcolor: isDark ? '#0f172a' : '#ffffff',
            backgroundImage: 'none',
            p: 2.5,
            display: 'flex',
            flexDirection: 'column',
          },
        }}
      >
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 3 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Box
              sx={{
                width: 32,
                height: 32,
                borderRadius: '10px',
                bgcolor: '#f97316',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: '#ffffff',
                fontWeight: 900,
                fontSize: '1.1rem',
              }}
            >
              O
            </Box>
            <Typography variant="h6" sx={{ fontWeight: 800, color: 'text.primary' }}>
              OmniGo
            </Typography>
          </Box>
          <IconButton
            onClick={() => setMobileMenuOpen(false)}
            aria-label="Đóng menu"
            size="small"
            sx={{
              borderRadius: '10px',
              border: `1px solid ${isDark ? 'rgba(255,255,255,0.1)' : 'rgba(0,0,0,0.08)'}`,
            }}
          >
            <CloseIcon fontSize="small" />
          </IconButton>
        </Box>

        <List sx={{ flexGrow: 1, py: 0 }}>
          {navLinks.map((link) => (
            <ListItem key={link.href} disablePadding sx={{ mb: 1 }}>
              <ListItemButton
                component="a"
                href={link.href}
                onClick={(e) => handleNavClick(e, link.href)}
                sx={{
                  borderRadius: 2,
                  py: 1.2,
                  px: 2,
                  '&:hover': {
                    bgcolor: isDark ? 'rgba(255,255,255,0.06)' : 'rgba(0,0,0,0.04)',
                  },
                }}
              >
                <ListItemText
                  primary={link.label}
                  primaryTypographyProps={{
                    fontWeight: 600,
                    fontSize: '0.98rem',
                    color: 'text.primary',
                  }}
                />
              </ListItemButton>
            </ListItem>
          ))}
        </List>

        <Box sx={{ pt: 2, borderTop: 1, borderColor: 'divider', display: 'flex', flexDirection: 'column', gap: 1.5 }}>
          {renderAuthCta()}
          <Typography
            variant="caption"
            sx={{ textAlign: 'center', color: 'text.secondary', fontSize: '0.75rem', mt: 1 }}
          >
            OmniGo Platform • Siêu ứng dụng Đa Dịch Vụ
          </Typography>
        </Box>
      </Drawer>
    </AppBar>
  );
};

export default LandingHeader;
