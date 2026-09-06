import React, { useState, useEffect } from 'react';
import {
  Drawer,
  Box,
  Typography,
  IconButton,
  Collapse,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
} from '@mui/material';
import {
  HomeOutlined as HomeIcon,
  PeopleAltOutlined as UsersIcon,
  DirectionsCarOutlined as DriversIcon,
  TuneOutlined as PricingIcon,
  ReceiptLongOutlined as BookingsIcon,
  AccountBalanceWalletOutlined as PaymentsIcon,
  StoreOutlined as RestaurantIcon,
  FastfoodOutlined as FoodOrderIcon,
  ExpandMore,
  ExpandLess,
  ArrowBackIosNew as CollapseIcon,
  RadioButtonUnchecked as BulletIcon,
  FiberManualRecord as ActiveBulletIcon,
} from '@mui/icons-material';
import { useLocation, useNavigate } from 'react-router-dom';
import { useColorMode } from '../context/ThemeContext';

const DRAWER_WIDTH = 250;

export const Sidebar = ({ mobileOpen, handleDrawerToggle, isMobile }) => {
  const location = useLocation();
  const navigate = useNavigate();
  const { mode } = useColorMode();
  const isDark = mode === 'dark';

  const [openDashboard, setOpenDashboard] = useState(true);
  const [openBookings, setOpenBookings] = useState(true);
  const [openFoodOrders, setOpenFoodOrders] = useState(true);
  const [openPayments, setOpenPayments] = useState(true);

  // Auto-expand relevant menus based on URL path
  useEffect(() => {
    if (location.pathname.startsWith('/bookings')) {
      setOpenBookings(true);
    }
    if (location.pathname.startsWith('/food-orders')) {
      setOpenFoodOrders(true);
    }
    if (location.pathname.startsWith('/payments')) {
      setOpenPayments(true);
    }
  }, [location.pathname]);

  const handleNav = (path) => {
    navigate(path);
    if (isMobile && handleDrawerToggle) {
      handleDrawerToggle();
    }
  };

  const isCurrent = (path) => {
    if (path === '/') return location.pathname === '/' || location.pathname === '/dashboard';
    const [pathname, search] = path.split('?');
    if (search) {
      return location.pathname === pathname && location.search === `?${search}`;
    }
    return location.pathname === pathname && !location.search;
  };

  const isSectionActive = (basePath) => {
    if (basePath === '/') return location.pathname === '/' || location.pathname === '/dashboard';
    return location.pathname.startsWith(basePath);
  };

  const BOOKING_SUB_ITEMS = [
    { title: 'Tất cả chuyến xe', path: '/bookings' },
    { title: 'Đang hoạt động', path: '/bookings?status=ACTIVE' },
    { title: 'Lịch sử hoàn thành', path: '/bookings?status=COMPLETED' },
    { title: 'Cuốc xe đã hủy', path: '/bookings?status=CANCELLED' },
  ];

  const FOOD_ORDER_SUB_ITEMS = [
    { title: 'Tất cả đơn món', path: '/food-orders' },
    { title: 'Chờ duyệt / Quán nhận', path: '/food-orders?status=PENDING' },
    { title: 'Đang nấu / Chờ tài xế', path: '/food-orders?status=PREPARING' },
    { title: 'Đang giao hàng', path: '/food-orders?status=DELIVERING' },
    { title: 'Giao hoàn tất', path: '/food-orders?status=COMPLETED' },
    { title: 'Đơn đã hủy', path: '/food-orders?status=CANCELLED' },
  ];

  const PAYMENT_SUB_ITEMS = [
    { title: 'Tất cả giao dịch', path: '/payments' },
    { title: 'Thanh toán cuốc xe', path: '/payments?type=TRIP_PAYMENT' },
    { title: 'Giao dịch ví & Rút tiền', path: '/payments?type=WALLET' },
    { title: 'Cổng VNPAY / MoMo', path: '/payments?gateway=ONLINE' },
  ];

  const drawerContent = (
    <Box sx={{ display: 'flex', flexDirection: 'column', height: '100%', bgcolor: 'background.paper' }}>
      {/* Brand Header */}
      <Box
        sx={{
          px: 2.5,
          py: 2,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          borderBottom: 1,
          borderColor: 'divider',
          minHeight: 62,
        }}
      >
        <Box
          sx={{ display: 'flex', alignItems: 'center', gap: 1.2, cursor: 'pointer', userSelect: 'none' }}
          onClick={() => handleNav('/')}
        >
          <Box
            sx={{
              width: 32,
              height: 32,
              borderRadius: '50%',
              bgcolor: '#008cff',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: '#ffffff',
              fontWeight: 900,
              fontSize: '1rem',
              boxShadow: '0 0 12px rgba(0, 140, 255, 0.4)',
            }}
          >
            O
          </Box>
          <Typography
            variant="h6"
            sx={{
              fontWeight: 700,
              fontFamily: '"Poppins", sans-serif',
              fontSize: '1.25rem',
              color: 'text.primary',
              letterSpacing: '-0.02em',
            }}
          >
            OmniGo
          </Typography>
        </Box>

        {isMobile && (
          <IconButton size="small" onClick={handleDrawerToggle} sx={{ color: 'text.secondary', p: 0.5 }}>
            <CollapseIcon sx={{ fontSize: 14 }} />
          </IconButton>
        )}
      </Box>

      {/* Menu List */}
      <Box sx={{ flexGrow: 1, py: 1.5, px: 1.5, overflowY: 'auto' }}>
        {/* Category: DASHBOARD */}
        <ListItem disablePadding sx={{ mb: 0.5 }}>
          <ListItemButton
            onClick={() => setOpenDashboard(!openDashboard)}
            sx={{
              borderRadius: 1.5,
              py: 1,
              px: 1.5,
              bgcolor: isSectionActive('/')
                ? isDark ? 'rgba(0, 140, 255, 0.15)' : 'rgba(0, 140, 255, 0.08)'
                : 'transparent',
              '&:hover': {
                bgcolor: isDark ? 'rgba(255, 255, 255, 0.06)' : 'rgba(0, 0, 0, 0.05)',
              },
            }}
          >
            <ListItemIcon sx={{ minWidth: 32, color: isSectionActive('/') ? '#008cff' : 'text.secondary' }}>
              <HomeIcon sx={{ fontSize: 20 }} />
            </ListItemIcon>
            <ListItemText
              primary={
                <Typography
                  variant="body2"
                  sx={{
                    fontWeight: 700,
                    color: isSectionActive('/') ? (isDark ? '#38bdf8' : '#0070cc') : 'text.primary',
                  }}
                >
                  Dashboard
                </Typography>
              }
            />
            {openDashboard ? (
              <ExpandLess sx={{ fontSize: 18, color: 'text.secondary' }} />
            ) : (
              <ExpandMore sx={{ fontSize: 18, color: 'text.secondary' }} />
            )}
          </ListItemButton>
        </ListItem>

        <Collapse in={openDashboard} timeout="auto" unmountOnExit>
          <List component="div" disablePadding sx={{ pl: 2, mb: 1 }}>
            <ListItemButton
              onClick={() => handleNav('/')}
              sx={{
                py: 0.8,
                px: 2,
                borderRadius: 1.5,
                bgcolor: isCurrent('/')
                  ? isDark ? 'rgba(0, 140, 255, 0.2)' : 'rgba(0, 140, 255, 0.12)'
                  : 'transparent',
                '&:hover': {
                  bgcolor: isDark ? 'rgba(255, 255, 255, 0.08)' : 'rgba(0, 140, 255, 0.18)',
                },
                '&:active, &.Mui-focusVisible, &:focus': {
                  bgcolor: isDark ? 'rgba(0, 140, 255, 0.25)' : 'rgba(0, 140, 255, 0.2)',
                },
              }}
            >
              {isCurrent('/') ? (
                <ActiveBulletIcon sx={{ fontSize: 10, mr: 1.5, color: '#008cff' }} />
              ) : (
                <BulletIcon sx={{ fontSize: 9, mr: 1.5, color: 'text.secondary' }} />
              )}
              <ListItemText
                primary={
                  <Typography
                    variant="body2"
                    sx={{
                      fontSize: '0.85rem',
                      fontWeight: isCurrent('/') ? 700 : 500,
                      color: isCurrent('/')
                        ? (isDark ? '#38bdf8' : '#0070cc')
                        : (isDark ? '#cbd5e1' : '#334155'),
                      userSelect: 'none',
                    }}
                  >
                    Tổng quan vận hành
                  </Typography>
                }
              />
            </ListItemButton>
          </List>
        </Collapse>

        {/* Category: MANAGEMENT */}
        <Typography
          variant="caption"
          sx={{
            px: 1.5,
            pt: 1.5,
            pb: 0.8,
            display: 'block',
            color: 'text.secondary',
            fontWeight: 700,
            textTransform: 'uppercase',
            letterSpacing: '0.06em',
            fontSize: '0.7rem',
          }}
        >
          Quản Trị Vận Hành
        </Typography>

        {/* 1. Khách Hàng */}
        <ListItem disablePadding sx={{ mb: 0.5 }}>
          <ListItemButton
            onClick={() => handleNav('/users')}
            sx={{
              borderRadius: 1.5,
              py: 0.9,
              px: 1.5,
              bgcolor: isCurrent('/users')
                ? isDark ? 'rgba(0, 140, 255, 0.15)' : 'rgba(0, 140, 255, 0.1)'
                : 'transparent',
              '&:hover': { bgcolor: isDark ? 'rgba(255, 255, 255, 0.04)' : 'rgba(0, 0, 0, 0.04)' },
            }}
          >
            <ListItemIcon sx={{ minWidth: 32, color: isCurrent('/users') ? '#008cff' : 'text.secondary' }}>
              <UsersIcon sx={{ fontSize: 20 }} />
            </ListItemIcon>
            <ListItemText
              primary={
                <Typography
                  variant="body2"
                  sx={{
                    fontWeight: isCurrent('/users') ? 700 : 600,
                    color: isCurrent('/users') ? (isDark ? '#38bdf8' : '#0070cc') : 'text.primary',
                    fontSize: '0.88rem',
                  }}
                >
                  Khách hàng
                </Typography>
              }
            />
          </ListItemButton>
        </ListItem>

        {/* 2. Tài Xế & Đội Xe */}
        <ListItem disablePadding sx={{ mb: 0.5 }}>
          <ListItemButton
            onClick={() => handleNav('/drivers')}
            sx={{
              borderRadius: 1.5,
              py: 0.9,
              px: 1.5,
              bgcolor: isCurrent('/drivers')
                ? isDark ? 'rgba(0, 140, 255, 0.15)' : 'rgba(0, 140, 255, 0.1)'
                : 'transparent',
              '&:hover': { bgcolor: isDark ? 'rgba(255, 255, 255, 0.04)' : 'rgba(0, 0, 0, 0.04)' },
            }}
          >
            <ListItemIcon sx={{ minWidth: 32, color: isCurrent('/drivers') ? '#008cff' : 'text.secondary' }}>
              <DriversIcon sx={{ fontSize: 20 }} />
            </ListItemIcon>
            <ListItemText
              primary={
                <Typography
                  variant="body2"
                  sx={{
                    fontWeight: isCurrent('/drivers') ? 700 : 600,
                    color: isCurrent('/drivers') ? (isDark ? '#38bdf8' : '#0070cc') : 'text.primary',
                    fontSize: '0.88rem',
                  }}
                >
                  Tài xế & Đội xe
                </Typography>
              }
            />
          </ListItemButton>
        </ListItem>

        {/* 3. Chuyến Xe (Bookings) with expandable sub-items */}
        <ListItem disablePadding sx={{ mb: 0.5 }}>
          <ListItemButton
            onClick={() => setOpenBookings(!openBookings)}
            sx={{
              borderRadius: 1.5,
              py: 0.9,
              px: 1.5,
              bgcolor: isSectionActive('/bookings')
                ? isDark ? 'rgba(0, 140, 255, 0.15)' : 'rgba(0, 140, 255, 0.08)'
                : 'transparent',
              '&:hover': { bgcolor: isDark ? 'rgba(255, 255, 255, 0.04)' : 'rgba(0, 0, 0, 0.04)' },
            }}
          >
            <ListItemIcon sx={{ minWidth: 32, color: isSectionActive('/bookings') ? '#008cff' : 'text.secondary' }}>
              <BookingsIcon sx={{ fontSize: 20 }} />
            </ListItemIcon>
            <ListItemText
              primary={
                <Typography
                  variant="body2"
                  sx={{
                    fontWeight: isSectionActive('/bookings') ? 700 : 600,
                    color: isSectionActive('/bookings') ? (isDark ? '#38bdf8' : '#0070cc') : 'text.primary',
                    fontSize: '0.88rem',
                  }}
                >
                  Chuyến xe (Bookings)
                </Typography>
              }
            />
            {openBookings ? (
              <ExpandLess sx={{ fontSize: 18, color: 'text.secondary' }} />
            ) : (
              <ExpandMore sx={{ fontSize: 18, color: 'text.secondary' }} />
            )}
          </ListItemButton>
        </ListItem>

        <Collapse in={openBookings} timeout="auto" unmountOnExit>
          <List component="div" disablePadding sx={{ pl: 2, mb: 0.5 }}>
            {BOOKING_SUB_ITEMS.map((sub) => {
              const active = isCurrent(sub.path);
              return (
                <ListItemButton
                  key={sub.path}
                  onClick={() => handleNav(sub.path)}
                  sx={{
                    py: 0.7,
                    px: 2,
                    borderRadius: 1.5,
                    bgcolor: active
                      ? isDark ? 'rgba(0, 140, 255, 0.2)' : 'rgba(0, 140, 255, 0.12)'
                      : 'transparent',
                    '&:hover': {
                      bgcolor: isDark ? 'rgba(255, 255, 255, 0.08)' : 'rgba(0, 140, 255, 0.18)',
                    },
                  }}
                >
                  {active ? (
                    <ActiveBulletIcon sx={{ fontSize: 9, mr: 1.5, color: '#008cff' }} />
                  ) : (
                    <BulletIcon sx={{ fontSize: 8, mr: 1.5, color: 'text.secondary' }} />
                  )}
                  <ListItemText
                    primary={
                      <Typography
                        variant="body2"
                        sx={{
                          fontSize: '0.83rem',
                          fontWeight: active ? 700 : 500,
                          color: active
                            ? (isDark ? '#38bdf8' : '#0062b3')
                            : (isDark ? '#cbd5e1' : '#334155'),
                        }}
                      >
                        {sub.title}
                      </Typography>
                    }
                  />
                </ListItemButton>
              );
            })}
          </List>
        </Collapse>

        {/* Category: FOOD DELIVERY */}
        <Typography
          variant="caption"
          sx={{
            px: 1.5,
            pt: 2,
            pb: 0.8,
            display: 'block',
            color: 'text.secondary',
            fontWeight: 700,
            textTransform: 'uppercase',
            letterSpacing: '0.06em',
            fontSize: '0.7rem',
          }}
        >
          Giao Đồ Ăn (Food Delivery)
        </Typography>

        {/* 1. Nhà Hàng & Đối Tác */}
        <ListItem disablePadding sx={{ mb: 0.5 }}>
          <ListItemButton
            onClick={() => handleNav('/restaurants')}
            sx={{
              borderRadius: 1.5,
              py: 0.9,
              px: 1.5,
              bgcolor: isCurrent('/restaurants')
                ? isDark ? 'rgba(0, 140, 255, 0.15)' : 'rgba(0, 140, 255, 0.1)'
                : 'transparent',
              '&:hover': { bgcolor: isDark ? 'rgba(255, 255, 255, 0.04)' : 'rgba(0, 0, 0, 0.04)' },
            }}
          >
            <ListItemIcon sx={{ minWidth: 32, color: isCurrent('/restaurants') ? '#008cff' : 'text.secondary' }}>
              <RestaurantIcon sx={{ fontSize: 20 }} />
            </ListItemIcon>
            <ListItemText
              primary={
                <Typography
                  variant="body2"
                  sx={{
                    fontWeight: isCurrent('/restaurants') ? 700 : 600,
                    color: isCurrent('/restaurants') ? (isDark ? '#38bdf8' : '#0070cc') : 'text.primary',
                    fontSize: '0.88rem',
                  }}
                >
                  Nhà hàng & Đối tác
                </Typography>
              }
            />
          </ListItemButton>
        </ListItem>

        {/* 2. Đơn Đặt Món (Food Orders) with expandable sub-items */}
        <ListItem disablePadding sx={{ mb: 0.5 }}>
          <ListItemButton
            onClick={() => setOpenFoodOrders(!openFoodOrders)}
            sx={{
              borderRadius: 1.5,
              py: 0.9,
              px: 1.5,
              bgcolor: isSectionActive('/food-orders')
                ? isDark ? 'rgba(0, 140, 255, 0.15)' : 'rgba(0, 140, 255, 0.08)'
                : 'transparent',
              '&:hover': { bgcolor: isDark ? 'rgba(255, 255, 255, 0.04)' : 'rgba(0, 0, 0, 0.04)' },
            }}
          >
            <ListItemIcon sx={{ minWidth: 32, color: isSectionActive('/food-orders') ? '#008cff' : 'text.secondary' }}>
              <FoodOrderIcon sx={{ fontSize: 20 }} />
            </ListItemIcon>
            <ListItemText
              primary={
                <Typography
                  variant="body2"
                  sx={{
                    fontWeight: isSectionActive('/food-orders') ? 700 : 600,
                    color: isSectionActive('/food-orders') ? (isDark ? '#38bdf8' : '#0070cc') : 'text.primary',
                    fontSize: '0.88rem',
                  }}
                >
                  Đơn đặt món (Food)
                </Typography>
              }
            />
            {openFoodOrders ? (
              <ExpandLess sx={{ fontSize: 18, color: 'text.secondary' }} />
            ) : (
              <ExpandMore sx={{ fontSize: 18, color: 'text.secondary' }} />
            )}
          </ListItemButton>
        </ListItem>

        <Collapse in={openFoodOrders} timeout="auto" unmountOnExit>
          <List component="div" disablePadding sx={{ pl: 2, mb: 0.5 }}>
            {FOOD_ORDER_SUB_ITEMS.map((sub) => {
              const active = isCurrent(sub.path);
              return (
                <ListItemButton
                  key={sub.path}
                  onClick={() => handleNav(sub.path)}
                  sx={{
                    py: 0.7,
                    px: 2,
                    borderRadius: 1.5,
                    bgcolor: active
                      ? isDark ? 'rgba(0, 140, 255, 0.2)' : 'rgba(0, 140, 255, 0.12)'
                      : 'transparent',
                    '&:hover': {
                      bgcolor: isDark ? 'rgba(255, 255, 255, 0.08)' : 'rgba(0, 140, 255, 0.18)',
                    },
                  }}
                >
                  {active ? (
                    <ActiveBulletIcon sx={{ fontSize: 9, mr: 1.5, color: '#008cff' }} />
                  ) : (
                    <BulletIcon sx={{ fontSize: 8, mr: 1.5, color: 'text.secondary' }} />
                  )}
                  <ListItemText
                    primary={
                      <Typography
                        variant="body2"
                        sx={{
                          fontSize: '0.83rem',
                          fontWeight: active ? 700 : 500,
                          color: active
                            ? (isDark ? '#38bdf8' : '#0062b3')
                            : (isDark ? '#cbd5e1' : '#334155'),
                        }}
                      >
                        {sub.title}
                      </Typography>
                    }
                  />
                </ListItemButton>
              );
            })}
          </List>
        </Collapse>

        {/* Category: FINANCE */}
        <Typography
          variant="caption"
          sx={{
            px: 1.5,
            pt: 2,
            pb: 0.8,
            display: 'block',
            color: 'text.secondary',
            fontWeight: 700,
            textTransform: 'uppercase',
            letterSpacing: '0.06em',
            fontSize: '0.7rem',
          }}
        >
          Tài Chính & Hệ Thống
        </Typography>

        {/* 4. Thanh Toán & Ví with expandable sub-items */}
        <ListItem disablePadding sx={{ mb: 0.5 }}>
          <ListItemButton
            onClick={() => setOpenPayments(!openPayments)}
            sx={{
              borderRadius: 1.5,
              py: 0.9,
              px: 1.5,
              bgcolor: isSectionActive('/payments')
                ? isDark ? 'rgba(0, 140, 255, 0.15)' : 'rgba(0, 140, 255, 0.08)'
                : 'transparent',
              '&:hover': { bgcolor: isDark ? 'rgba(255, 255, 255, 0.04)' : 'rgba(0, 0, 0, 0.04)' },
            }}
          >
            <ListItemIcon sx={{ minWidth: 32, color: isSectionActive('/payments') ? '#008cff' : 'text.secondary' }}>
              <PaymentsIcon sx={{ fontSize: 20 }} />
            </ListItemIcon>
            <ListItemText
              primary={
                <Typography
                  variant="body2"
                  sx={{
                    fontWeight: isSectionActive('/payments') ? 700 : 600,
                    color: isSectionActive('/payments') ? (isDark ? '#38bdf8' : '#0070cc') : 'text.primary',
                    fontSize: '0.88rem',
                  }}
                >
                  Thanh toán & Ví
                </Typography>
              }
            />
            {openPayments ? (
              <ExpandLess sx={{ fontSize: 18, color: 'text.secondary' }} />
            ) : (
              <ExpandMore sx={{ fontSize: 18, color: 'text.secondary' }} />
            )}
          </ListItemButton>
        </ListItem>

        <Collapse in={openPayments} timeout="auto" unmountOnExit>
          <List component="div" disablePadding sx={{ pl: 2, mb: 0.5 }}>
            {PAYMENT_SUB_ITEMS.map((sub) => {
              const active = isCurrent(sub.path);
              return (
                <ListItemButton
                  key={sub.path}
                  onClick={() => handleNav(sub.path)}
                  sx={{
                    py: 0.7,
                    px: 2,
                    borderRadius: 1.5,
                    bgcolor: active
                      ? isDark ? 'rgba(0, 140, 255, 0.2)' : 'rgba(0, 140, 255, 0.12)'
                      : 'transparent',
                    '&:hover': {
                      bgcolor: isDark ? 'rgba(255, 255, 255, 0.08)' : 'rgba(0, 140, 255, 0.18)',
                    },
                  }}
                >
                  {active ? (
                    <ActiveBulletIcon sx={{ fontSize: 9, mr: 1.5, color: '#008cff' }} />
                  ) : (
                    <BulletIcon sx={{ fontSize: 8, mr: 1.5, color: 'text.secondary' }} />
                  )}
                  <ListItemText
                    primary={
                      <Typography
                        variant="body2"
                        sx={{
                          fontSize: '0.83rem',
                          fontWeight: active ? 700 : 500,
                          color: active
                            ? (isDark ? '#38bdf8' : '#0062b3')
                            : (isDark ? '#cbd5e1' : '#334155'),
                        }}
                      >
                        {sub.title}
                      </Typography>
                    }
                  />
                </ListItemButton>
              );
            })}
          </List>
        </Collapse>

        {/* 5. Cấu Hình Giá Cước */}
        <ListItem disablePadding sx={{ mb: 0.5 }}>
          <ListItemButton
            onClick={() => handleNav('/pricing')}
            sx={{
              borderRadius: 1.5,
              py: 0.9,
              px: 1.5,
              bgcolor: isCurrent('/pricing')
                ? isDark ? 'rgba(0, 140, 255, 0.15)' : 'rgba(0, 140, 255, 0.1)'
                : 'transparent',
              '&:hover': { bgcolor: isDark ? 'rgba(255, 255, 255, 0.04)' : 'rgba(0, 0, 0, 0.04)' },
            }}
          >
            <ListItemIcon sx={{ minWidth: 32, color: isCurrent('/pricing') ? '#008cff' : 'text.secondary' }}>
              <PricingIcon sx={{ fontSize: 20 }} />
            </ListItemIcon>
            <ListItemText
              primary={
                <Typography
                  variant="body2"
                  sx={{
                    fontWeight: isCurrent('/pricing') ? 700 : 600,
                    color: isCurrent('/pricing') ? (isDark ? '#38bdf8' : '#0070cc') : 'text.primary',
                    fontSize: '0.88rem',
                  }}
                >
                  Cấu hình giá cước
                </Typography>
              }
            />
          </ListItemButton>
        </ListItem>
      </Box>

      {/* Footer copyright */}
      <Box sx={{ p: 2, borderTop: 1, borderColor: 'divider', textAlign: 'center' }}>
        <Typography variant="caption" sx={{ color: 'text.secondary', fontSize: '0.75rem', fontWeight: 600 }}>
          OmniGo Platform v2.0
        </Typography>
      </Box>
    </Box>
  );

  return (
    <Box
      component="nav"
      sx={{ width: { md: DRAWER_WIDTH }, flexShrink: { md: 0 } }}
    >
      {/* Mobile Drawer */}
      <Drawer
        variant="temporary"
        open={mobileOpen}
        onClose={handleDrawerToggle}
        ModalProps={{ keepMounted: true }}
        sx={{
          display: { xs: 'block', md: 'none' },
          '& .MuiDrawer-paper': { boxSizing: 'border-box', width: DRAWER_WIDTH },
        }}
      >
        {drawerContent}
      </Drawer>

      {/* Desktop Persistent Sidebar */}
      <Drawer
        variant="permanent"
        sx={{
          display: { xs: 'none', md: 'block' },
          '& .MuiDrawer-paper': { boxSizing: 'border-box', width: DRAWER_WIDTH },
        }}
        open
      >
        {drawerContent}
      </Drawer>
    </Box>
  );
};

export default Sidebar;
