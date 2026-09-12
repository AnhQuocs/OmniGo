 import React, { useState, useEffect } from 'react';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import {
  Box,
  Typography,
  IconButton,
  Avatar,
  Menu,
  MenuItem,
  Chip,
  Badge,
  CircularProgress,
  Divider,
  Drawer,
} from '@mui/material';
import {
  Assignment as OrderIcon,
  RestaurantMenu as MenuIcon,
  Settings as SettingsIcon,
  Map as MapIcon,
  Notifications as BellIcon,
  HeadsetMic as SupportIcon,
  KeyboardArrowDown as ArrowDownIcon,
  Logout as LogoutIcon,
  CheckCircle as OpenIcon,
  Schedule as BusyIcon,
  Cancel as ClosedIcon,
  Store as StoreIcon,
  Menu as HamburgerIcon,
} from '@mui/icons-material';
import { useDispatch, useSelector } from 'react-redux';
import { logoutUser } from '../../redux/authSlice';
import foodService from '../../services/foodService';
import toast from 'react-hot-toast';

export const MerchantLayout = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const dispatch = useDispatch();
  const { user } = useSelector((state) => state.auth);

  const [restaurant, setRestaurant] = useState(null);
  const [loadingRes, setLoadingRes] = useState(true);
  const [statusAnchor, setStatusAnchor] = useState(null);
  const [userAnchor, setUserAnchor] = useState(null);
  const [updatingStatus, setUpdatingStatus] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);

  // Fetch Current Restaurant Info
  const fetchMyRestaurant = async () => {
    try {
      setLoadingRes(true);
      const res = await foodService.getMyRestaurant();
      // Handle both raw object, or wrapped in res.data
      const actual = res?.data?.id ? res.data : res?.id ? res : null;
      if (actual && actual.id) {
        setRestaurant(actual);
      } else {
        setRestaurant(null);
      }
    } catch (err) {
      console.warn('Chưa có thông tin nhà hàng hoặc lỗi:', err.message);
      setRestaurant(null);
    } finally {
      setLoadingRes(false);
    }
  };

  useEffect(() => {
    fetchMyRestaurant();
  }, []);

  const handleStatusChange = async (newStatus) => {
    setStatusAnchor(null);
    if (!restaurant || !restaurant.id) return;
    try {
      setUpdatingStatus(true);
      await foodService.updateRestaurantStatus(restaurant.id, newStatus);
      setRestaurant((prev) => ({ ...prev, status: newStatus }));
      toast.success(`Đã đổi trạng thái quán sang: ${newStatus === 'OPEN' ? 'Mở cửa' : newStatus === 'BUSY' ? 'Đang bận' : 'Tạm đóng cửa'}`);
    } catch (err) {
      toast.error('Lỗi khi đổi trạng thái quán');
    } finally {
      setUpdatingStatus(false);
    }
  };

  const handleLogout = async () => {
    setUserAnchor(null);
    await dispatch(logoutUser());
    toast.success('Đã đăng xuất tài khoản');
    navigate('/login', { replace: true });
  };

  const navItems = [
    { label: 'Đơn hàng', path: '/merchant/orders', icon: <OrderIcon fontSize="small" /> },
    { label: 'Thực đơn', path: '/merchant/menu', icon: <MenuIcon fontSize="small" /> },
    { label: 'Cài đặt', path: '/merchant/settings', icon: <SettingsIcon fontSize="small" /> },
    { label: 'Bản đồ', path: '/merchant/map', icon: <MapIcon fontSize="small" /> },
  ];

  const currentStatus = restaurant?.status || 'OPEN';

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh', bgcolor: '#F8FAFC' }}>
      {/* TOP NAVBAR */}
      <Box
        component="header"
        sx={{
          height: 72,
          bgcolor: '#FFFFFF',
          borderBottom: '1px solid #E2E8F0',
          px: { xs: 2, md: 3.5 },
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          position: 'sticky',
          top: 0,
          zIndex: 1100,
        }}
      >
        {/* Left: Hamburger menu (mobile) + Brand Logo */}
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <IconButton
            onClick={() => setMobileOpen(true)}
            sx={{ display: { md: 'none' }, color: '#0F172A', p: 1 }}
            title="Mở menu"
          >
            <HamburgerIcon />
          </IconButton>
          <Box
            onClick={() => navigate('/merchant/orders')}
            sx={{ display: 'flex', alignItems: 'center', gap: 1.5, cursor: 'pointer' }}
          >
          <Box
            sx={{
              width: 42,
              height: 42,
              borderRadius: 3,
              background: 'linear-gradient(135deg, #F97316 0%, #EA580C 100%)',
              color: '#FFFFFF',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: '1.4rem',
              boxShadow: '0 4px 10px rgba(249, 115, 22, 0.3)',
            }}
          >
            🍲
          </Box>
          <Box>
            <Typography variant="subtitle1" sx={{ fontWeight: 800, lineHeight: 1.1, color: '#0F172A', fontSize: '1.1rem' }}>
              OmniFood
            </Typography>
            <Typography variant="caption" sx={{ color: '#64748B', fontWeight: 600, fontSize: '0.75rem' }}>
              Merchant Portal
            </Typography>
          </Box>
        </Box>
      </Box>

        {/* Center: Restaurant Information */}
        <Box
          sx={{
            display: { xs: 'none', sm: 'flex' },
            alignItems: 'center',
            gap: 1.5,
            px: 2,
            py: 0.8,
            borderRadius: 3,
            bgcolor: '#F8FAFC',
            border: '1px solid #E2E8F0',
            maxWidth: 450,
          }}
        >
          <Avatar
            src={restaurant?.imageUrl || 'https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=100'}
            alt={restaurant?.name || 'Restaurant'}
            sx={{ width: 40, height: 40, borderRadius: 2 }}
          />
          <Box sx={{ overflow: 'hidden' }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
              <Typography variant="body2" sx={{ fontWeight: 800, color: '#0F172A', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                {restaurant?.name || (loadingRes ? 'Đang tải thông tin quán...' : 'Chưa đăng ký quán ăn')}
              </Typography>
            </Box>
            <Typography variant="caption" sx={{ color: '#64748B', display: 'block', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
              {restaurant?.address || 'Vui lòng cập nhật thông tin tại mục Cài đặt'}
            </Typography>
          </Box>
        </Box>

        {/* Right: Actions (Status, Notifications, User) */}
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
          {/* Status Dropdown Pill */}
          {restaurant && (
            <Chip
              icon={
                updatingStatus ? (
                  <CircularProgress size={14} color="inherit" />
                ) : currentStatus === 'OPEN' ? (
                  <Box sx={{ width: 8, height: 8, borderRadius: '50%', bgcolor: '#10B981', ml: 0.5 }} />
                ) : currentStatus === 'BUSY' ? (
                  <Box sx={{ width: 8, height: 8, borderRadius: '50%', bgcolor: '#F59E0B', ml: 0.5 }} />
                ) : (
                  <Box sx={{ width: 8, height: 8, borderRadius: '50%', bgcolor: '#EF4444', ml: 0.5 }} />
                )
              }
              label={
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5, fontWeight: 700, fontSize: '0.82rem' }}>
                  {currentStatus === 'OPEN' ? 'OPEN' : currentStatus === 'BUSY' ? 'BUSY' : 'CLOSED'}
                  <ArrowDownIcon sx={{ fontSize: 16 }} />
                </Box>
              }
              onClick={(e) => setStatusAnchor(e.currentTarget)}
              sx={{
                bgcolor: currentStatus === 'OPEN' ? '#ECFDF5' : currentStatus === 'BUSY' ? '#FEF3C7' : '#FEF2F2',
                color: currentStatus === 'OPEN' ? '#047857' : currentStatus === 'BUSY' ? '#B45309' : '#B91C1C',
                border: '1px solid',
                borderColor: currentStatus === 'OPEN' ? '#A7F3D0' : currentStatus === 'BUSY' ? '#FDE68A' : '#FECACA',
                height: 34,
                cursor: 'pointer',
                '&:hover': {
                  bgcolor: currentStatus === 'OPEN' ? '#D1FAE5' : currentStatus === 'BUSY' ? '#FDE68A' : '#FEE2E2',
                },
              }}
            />
          )}

          <Menu
            anchorEl={statusAnchor}
            open={Boolean(statusAnchor)}
            onClose={() => setStatusAnchor(null)}
            PaperProps={{ sx: { borderRadius: 3, minWidth: 160, p: 0.5 } }}
          >
            <MenuItem onClick={() => handleStatusChange('OPEN')} sx={{ fontSize: '0.88rem', fontWeight: 600, gap: 1 }}>
              <OpenIcon sx={{ color: '#10B981', fontSize: 18 }} /> Đang mở cửa (OPEN)
            </MenuItem>
            <MenuItem onClick={() => handleStatusChange('BUSY')} sx={{ fontSize: '0.88rem', fontWeight: 600, gap: 1 }}>
              <BusyIcon sx={{ color: '#F59E0B', fontSize: 18 }} /> Quán đang bận (BUSY)
            </MenuItem>
            <MenuItem onClick={() => handleStatusChange('CLOSED')} sx={{ fontSize: '0.88rem', fontWeight: 600, gap: 1 }}>
              <ClosedIcon sx={{ color: '#EF4444', fontSize: 18 }} /> Tạm đóng cửa (CLOSED)
            </MenuItem>
          </Menu>

          {/* User Profile Pill */}
          <Box
            onClick={(e) => setUserAnchor(e.currentTarget)}
            sx={{
              display: 'flex',
              alignItems: 'center',
              gap: 1,
              p: 0.6,
              pr: 1.5,
              borderRadius: 5,
              bgcolor: '#F1F5F9',
              cursor: 'pointer',
              border: '1px solid #E2E8F0',
              '&:hover': { bgcolor: '#E2E8F0' },
            }}
          >
            <Avatar sx={{ width: 32, height: 32, bgcolor: '#F97316', fontSize: '0.85rem', fontWeight: 700 }}>
              {user?.fullName ? user.fullName[0] : 'U'}
            </Avatar>
            <Typography variant="body2" sx={{ fontWeight: 700, color: '#1E293B', fontSize: '0.85rem' }}>
              {user?.fullName || 'Chủ quán'}
            </Typography>
            <ArrowDownIcon sx={{ fontSize: 16, color: '#64748B' }} />
          </Box>

          <Menu
            anchorEl={userAnchor}
            open={Boolean(userAnchor)}
            onClose={() => setUserAnchor(null)}
            PaperProps={{ sx: { borderRadius: 3, minWidth: 180, p: 0.5 } }}
          >
            <MenuItem onClick={() => { setUserAnchor(null); navigate('/merchant/settings'); }} sx={{ fontSize: '0.88rem', fontWeight: 600, gap: 1 }}>
              <SettingsIcon fontSize="small" sx={{ color: '#64748B' }} /> Cài đặt quán
            </MenuItem>
            <Divider sx={{ my: 0.5 }} />
            <MenuItem onClick={handleLogout} sx={{ fontSize: '0.88rem', fontWeight: 600, color: '#EF4444', gap: 1 }}>
              <LogoutIcon fontSize="small" /> Đăng xuất
            </MenuItem>
          </Menu>
        </Box>
      </Box>

      {/* BODY: SIDEBAR + MAIN CONTENT */}
      <Box sx={{ display: 'flex', flex: 1, p: { xs: 1.5, md: 3 }, gap: { xs: 1.5, md: 3 }, maxWidth: 1600, width: '100%', mx: 'auto' }}>
        {/* LEFT SIDEBAR */}
        <Box
          component="aside"
          sx={{
            width: 220,
            flexShrink: 0,
            display: { xs: 'none', md: 'flex' },
            flexDirection: 'column',
            justifyContent: 'space-between',
          }}
        >
          {/* Navigation Items */}
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
            {navItems.map((item) => {
              const isActive = location.pathname === item.path;
              return (
                <Box
                  key={item.path}
                  onClick={() => navigate(item.path)}
                  sx={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: 1.5,
                    px: 2,
                    py: 1.2,
                    borderRadius: 3,
                    cursor: 'pointer',
                    fontWeight: 700,
                    fontSize: '0.92rem',
                    transition: 'all 0.15s ease-in-out',
                    bgcolor: isActive ? '#FFF7ED' : 'transparent',
                    color: isActive ? '#EA580C' : '#475569',
                    border: '1px solid',
                    borderColor: isActive ? '#FED7AA' : 'transparent',
                    '&:hover': {
                      bgcolor: isActive ? '#FFF7ED' : '#F1F5F9',
                      color: isActive ? '#EA580C' : '#0F172A',
                    },
                  }}
                >
                  <Box sx={{ color: isActive ? '#F97316' : '#64748B', display: 'flex' }}>
                    {item.icon}
                  </Box>
                  {item.label}
                </Box>
              );
            })}
          </Box>

          {/* Bottom Support Widget */}
          <Box
            sx={{
              p: 2,
              borderRadius: 3,
              bgcolor: '#FFFFFF',
              border: '1px solid #E2E8F0',
              display: 'flex',
              alignItems: 'center',
              gap: 1.5,
              boxShadow: '0 2px 6px rgba(0,0,0,0.02)',
            }}
          >
            <Box
              sx={{
                width: 36,
                height: 36,
                borderRadius: 2.5,
                bgcolor: '#F1F5F9',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: '#475569',
              }}
            >
              <SupportIcon fontSize="small" />
            </Box>
            <Box>
              <Typography variant="caption" sx={{ color: '#64748B', fontWeight: 600, display: 'block' }}>
                Hỗ trợ kỹ thuật
              </Typography>
              <Typography variant="body2" sx={{ fontWeight: 800, color: '#0F172A', fontSize: '0.85rem' }}>
                1900 1234
              </Typography>
            </Box>
          </Box>
        </Box>

        {/* MAIN CONTENT AREA */}
        <Box component="main" sx={{ flex: 1, minWidth: 0 }}>
          <Outlet context={{ restaurant, loadingRes, refreshRestaurant: fetchMyRestaurant }} />
        </Box>
      </Box>

      {/* Mobile Responsive Drawer */}
      <Drawer
        anchor="left"
        open={mobileOpen}
        onClose={() => setMobileOpen(false)}
        PaperProps={{
          sx: {
            width: 260,
            p: 2.5,
            display: 'flex',
            flexDirection: 'column',
            justifyContent: 'space-between',
            bgcolor: '#FFFFFF',
          },
        }}
      >
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 2 }}>
            <Box
              sx={{
                width: 38,
                height: 38,
                borderRadius: 2.5,
                background: 'linear-gradient(135deg, #F97316 0%, #EA580C 100%)',
                color: '#FFFFFF',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '1.3rem',
              }}
            >
              🍲
            </Box>
            <Typography variant="subtitle1" sx={{ fontWeight: 800, color: '#0F172A' }}>
              OmniFood Merchant
            </Typography>
          </Box>

          <Divider sx={{ mb: 1 }} />

          {navItems.map((item) => {
            const isActive = location.pathname === item.path;
            return (
              <Box
                key={item.path}
                onClick={() => {
                  navigate(item.path);
                  setMobileOpen(false);
                }}
                sx={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: 1.5,
                  px: 2,
                  py: 1.2,
                  borderRadius: 3,
                  cursor: 'pointer',
                  fontWeight: 700,
                  fontSize: '0.92rem',
                  bgcolor: isActive ? '#FFF7ED' : 'transparent',
                  color: isActive ? '#EA580C' : '#475569',
                  border: '1px solid',
                  borderColor: isActive ? '#FED7AA' : 'transparent',
                }}
              >
                <Box sx={{ color: isActive ? '#F97316' : '#64748B', display: 'flex' }}>
                  {item.icon}
                </Box>
                {item.label}
              </Box>
            );
          })}
        </Box>

        <Box sx={{ pt: 2, borderTop: '1px solid #F1F5F9' }}>
          <Typography variant="caption" sx={{ color: '#64748B', display: 'block' }}>
            Hotline hỗ trợ: 1900 1234
          </Typography>
        </Box>
      </Drawer>
    </Box>
  );
};

export default MerchantLayout;
