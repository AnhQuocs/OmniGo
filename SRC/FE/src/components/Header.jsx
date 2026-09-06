import React, { useState } from 'react';
import {
  AppBar,
  Toolbar,
  IconButton,
  Typography,
  Box,
  Avatar,
  Menu,
  MenuItem,
  ListItemIcon,
  Divider,
  Badge,
  InputBase,
} from '@mui/material';
import {
  Menu as MenuIcon,
  Search as SearchIcon,
  LightMode as LightIcon,
  DarkMode as DarkIcon,
  NotificationsNoneOutlined as BellIcon,
  ShoppingBagOutlined as BagIcon,
  AppsOutlined as GridIcon,
  LogoutOutlined as LogoutIcon,
} from '@mui/icons-material';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { logoutUser, logout } from '../redux/authSlice';
import { useColorMode } from '../context/ThemeContext';
import toast from 'react-hot-toast';

const DRAWER_WIDTH = 250;

export const Header = ({ handleDrawerToggle }) => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { user } = useSelector((state) => state.auth);
  const { mode, toggleColorMode } = useColorMode();

  const [anchorEl, setAnchorEl] = useState(null);
  const openMenu = Boolean(anchorEl);

  const handleOpenUserMenu = (event) => {
    setAnchorEl(event.currentTarget);
  };

  const handleCloseUserMenu = () => {
    setAnchorEl(null);
  };

  const handleLogout = async () => {
    handleCloseUserMenu();
    try {
      await dispatch(logoutUser()).unwrap();
    } catch {
      dispatch(logout());
    }
    toast.success('Đã đăng xuất tài khoản và xóa phiên làm việc');
    navigate('/login');
  };

  return (
    <AppBar
      position="fixed"
      sx={{
        width: { md: `calc(100% - ${DRAWER_WIDTH}px)` },
        ml: { md: `${DRAWER_WIDTH}px` },
      }}
    >
      <Toolbar sx={{ justifyContent: 'space-between', px: { xs: 1.5, sm: 3 }, minHeight: { xs: '58px', sm: '62px' } }}>
        {/* Left: Mobile Menu & Brand */}
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
          <IconButton
            color="inherit"
            edge="start"
            onClick={handleDrawerToggle}
            size="medium"
            aria-label="open drawer"
            sx={{
              display: { md: 'none' },
              p: 1,
              bgcolor: (theme) => theme.palette.mode === 'dark' ? 'rgba(255,255,255,0.06)' : 'rgba(0,0,0,0.04)',
              borderRadius: 2,
            }}
          >
            <MenuIcon sx={{ fontSize: 22 }} />
          </IconButton>

          {/* Mobile Brand Name */}
          <Box sx={{ display: { xs: 'flex', md: 'none' }, alignItems: 'center', gap: 1 }}>
            <Box
              sx={{
                width: 28,
                height: 28,
                borderRadius: '50%',
                bgcolor: '#008cff',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: '#ffffff',
                fontWeight: 900,
                fontSize: '0.85rem',
              }}
            >
              O
            </Box>
            <Typography
              variant="subtitle1"
              sx={{
                fontWeight: 800,
                fontFamily: '"Poppins", sans-serif',
                fontSize: '1.1rem',
                color: 'text.primary',
                letterSpacing: '-0.02em',
              }}
            >
              OmniGo
            </Typography>
          </Box>
        </Box>

        {/* Right: Icons bar & Profile */}
        <Box sx={{ display: 'flex', alignItems: 'center', gap: { xs: 0.8, sm: 1.5 } }}>
          {/* Sun / Moon Theme Switcher */}
          <IconButton
            onClick={toggleColorMode}
            size="small"
            sx={{
              color: 'text.primary',
              p: { xs: 0.8, sm: 0.9 },
              bgcolor: (theme) => theme.palette.mode === 'dark' ? 'rgba(255,255,255,0.04)' : 'rgba(0,0,0,0.03)',
              borderRadius: 2,
            }}
            title="Đổi giao diện Sáng / Tối"
          >
            {mode === 'light' ? (
              <DarkIcon sx={{ fontSize: 20 }} />
            ) : (
              <LightIcon sx={{ fontSize: 20, color: '#ffb800' }} />
            )}
          </IconButton>

          {/* User Profile Avatar with Name */}
          <Box
            onClick={handleOpenUserMenu}
            sx={{
              display: 'flex',
              alignItems: 'center',
              gap: 1,
              cursor: 'pointer',
              p: 0.5,
              borderRadius: 2,
              '&:hover': {
                bgcolor: (theme) => theme.palette.mode === 'dark' ? 'rgba(255,255,255,0.04)' : 'rgba(0,0,0,0.03)',
              },
            }}
          >
            <Avatar
              sx={{
                width: 34,
                height: 34,
                bgcolor: '#008cff',
                color: '#ffffff',
                fontSize: '0.85rem',
                fontWeight: 700,
              }}
            >
              {user?.fullName?.charAt(0) || user?.phoneNumber?.charAt(0) || 'A'}
            </Avatar>
            <Box sx={{ display: { xs: 'none', md: 'block' }, textAlign: 'left' }}>
              <Typography variant="body2" sx={{ fontWeight: 700, lineHeight: 1.1, color: 'text.primary', fontSize: '0.88rem' }}>
                {user?.fullName || 'Administrator'}
              </Typography>
              <Typography variant="caption" sx={{ color: 'text.secondary', fontSize: '0.75rem' }}>
                {user?.role === 'ADMIN' ? 'System Administrator' : 'Quản trị viên'}
              </Typography>
            </Box>
          </Box>

          <Menu
            anchorEl={anchorEl}
            open={openMenu}
            onClose={handleCloseUserMenu}
            PaperProps={{
              sx: {
                width: 220,
                mt: 1.5,
                borderRadius: 2.5,
              },
            }}
            transformOrigin={{ horizontal: 'right', vertical: 'top' }}
            anchorOrigin={{ horizontal: 'right', vertical: 'bottom' }}
          >
            <Box sx={{ px: 2, py: 1.5 }}>
              <Typography variant="body2" sx={{ fontWeight: 700 }}>
                {user?.fullName || 'Administrator'}
              </Typography>
              <Typography variant="caption" sx={{ color: 'text.secondary' }}>
                SĐT: {user?.phoneNumber || 'Admin'}
              </Typography>
            </Box>
            <Divider />
            <MenuItem onClick={handleLogout} sx={{ py: 1.2, color: 'error.main' }}>
              <ListItemIcon sx={{ color: 'error.main', minWidth: 28 }}>
                <LogoutIcon fontSize="small" />
              </ListItemIcon>
              Đăng xuất
            </MenuItem>
          </Menu>
        </Box>
      </Toolbar>
    </AppBar>
  );
};

export default Header;
