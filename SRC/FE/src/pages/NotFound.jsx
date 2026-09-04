import React from 'react';
import { Box, Typography, Button, Card, CardContent } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { Home as HomeIcon } from '@mui/icons-material';

export const NotFound = () => {
  const navigate = useNavigate();
  return (
    <Box
      sx={{
        minHeight: '70vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
      }}
    >
      <Card sx={{ maxWidth: 480, textAlign: 'center', p: 4, bgcolor: '#111827' }}>
        <CardContent>
          <Typography variant="h2" sx={{ fontWeight: 900, color: '#38BDF8', mb: 1 }}>
            404
          </Typography>
          <Typography variant="h6" sx={{ fontWeight: 700, mb: 1, color: '#F8FAFC' }}>
            Trang Không Tồn Tại
          </Typography>
          <Typography variant="body2" sx={{ color: '#94A3B8', mb: 3 }}>
            Đường dẫn bạn truy cập không hợp lệ hoặc đã được chuyển sang vị trí khác trong hệ thống OmniGo.
          </Typography>
          <Button
            variant="contained"
            startIcon={<HomeIcon />}
            onClick={() => navigate('/')}
            sx={{ fontWeight: 700 }}
          >
            Về Trang Chủ Admin
          </Button>
        </CardContent>
      </Card>
    </Box>
  );
};

export default NotFound;
