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
import { logout } from '../redux/authSlice';
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

  const handleLogout = () => {
    handleCloseUserMenu();
    dispatch(logout());
    toast.success('Đã đăng xuất tài khoản');
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
      <Toolbar sx={{ justifyContent: 'space-between', px: { xs: 2, sm: 3 }, minHeight: '62px !important' }}>
        {/* Left: Mobile Menu & Rocker Search Input */}
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <IconButton
            color="inherit"
            edge="start"
            onClick={handleDrawerToggle}
            size="small"
            sx={{ display: { md: 'none' } }}
          >
            <MenuIcon />
          </IconButton>

          <Box
            sx={{
              display: 'flex',
              alignItems: 'center',
              bgcolor: mode === 'dark' ? 'rgba(255, 255, 255, 0.05)' : '#f1f5f9',
              px: 2,
              py: 0.6,
              borderRadius: '20px',
              border: 1,
              borderColor: 'divider',
              width: { xs: 160, sm: 240 },
            }}
          >
            <SearchIcon sx={{ fontSize: 18, color: 'text.secondary', mr: 1 }} />
            <InputBase
              placeholder="Search..."
              sx={{
                fontSize: '0.88rem',
                color: 'text.primary',
                '& input::placeholder': { color: 'text.secondary', opacity: 1 },
              }}
            />
          </Box>
        </Box>

        {/* Right: Icons bar & Profile */}
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.2 }}>
          {/* Sun / Moon Theme Switcher */}
          <IconButton onClick={toggleColorMode} size="small" sx={{ color: 'text.primary', p: 0.9 }}>
            {mode === 'light' ? (
              <DarkIcon sx={{ fontSize: 20 }} />
            ) : (
              <LightIcon sx={{ fontSize: 20, color: '#ffb800' }} />
            )}
          </IconButton>

          {/* Grid apps icon */}
          <IconButton size="small" sx={{ color: 'text.secondary', display: { xs: 'none', sm: 'inline-flex' } }}>
            <GridIcon sx={{ fontSize: 20 }} />
          </IconButton>

          {/* Notification bell with badge 7 */}
          <IconButton size="small" sx={{ color: 'text.secondary', display: { xs: 'none', sm: 'inline-flex' } }}>
            <Badge badgeContent={7} color="error">
              <BellIcon sx={{ fontSize: 20 }} />
            </Badge>
          </IconButton>

          {/* Shopping bag with badge 8 */}
          <IconButton size="small" sx={{ color: 'text.secondary', display: { xs: 'none', sm: 'inline-flex' } }}>
            <Badge badgeContent={8} color="error">
              <BagIcon sx={{ fontSize: 20 }} />
            </Badge>
          </IconButton>

          {/* User Profile Avatar with Name */}
          <Box
            onClick={handleOpenUserMenu}
            sx={{
              display: 'flex',
              alignItems: 'center',
              gap: 1.2,
              cursor: 'pointer',
              ml: 1,
              pl: 1,
            }}
          >
            <Avatar
              sx={{
                width: 36,
                height: 36,
                bgcolor: '#008cff',
                color: '#ffffff',
                fontSize: '0.9rem',
                fontWeight: 700,
              }}
            >
              {user?.fullName?.charAt(0) || user?.phoneNumber?.charAt(0) || 'P'}
            </Avatar>
            <Box sx={{ display: { xs: 'none', md: 'block' }, textAlign: 'left' }}>
              <Typography variant="body2" sx={{ fontWeight: 700, lineHeight: 1.1, color: 'text.primary', fontSize: '0.88rem' }}>
                {user?.fullName || 'Pauline Seitz'}
              </Typography>
              <Typography variant="caption" sx={{ color: 'text.secondary', fontSize: '0.75rem' }}>
                {user?.role === 'ADMIN' ? 'System Administrator' : 'Web Designer'}
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
