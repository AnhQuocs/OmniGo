import React, { useState, useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TablePagination,
  TextField,
  InputAdornment,
  CircularProgress,
  Alert,
  Button,
  Chip,
  ButtonGroup,
} from '@mui/material';
import {
  Search as SearchIcon,
  Refresh as RefreshIcon,
  TwoWheeler as DriverIcon,
  FiberManualRecord as DotIcon,
} from '@mui/icons-material';
import toast from 'react-hot-toast';
import driverService from '../services/driverService';

export const Drivers = () => {
  const [drivers, setDrivers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');

  const fetchDrivers = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await driverService.getAllDrivers({ page: 0, size: 100 });
      let allUsers = [];

      if (response && response.content) {
        allUsers = response.content;
      } else if (Array.isArray(response)) {
        allUsers = response;
      } else if (response?.data) {
        allUsers = Array.isArray(response.data) ? response.data : [response.data];
      }

      // Filter: Only accounts with role DRIVER
      const onlyDrivers = allUsers.filter((u) => u.role === 'DRIVER');
      setDrivers(onlyDrivers);
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Không thể tải danh sách tài xế từ Backend';
      setError(msg);
      toast.error(msg);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDrivers();
  }, []);

  const getDriverStatusChip = (driver) => {
    const status = driver.status || (driver.isActive === true ? 'ONLINE' : 'OFFLINE');
    if (status === 'BUSY' || status === 'IN_TRIP') {
      return (
        <Chip
          icon={<DotIcon sx={{ fontSize: '10px !important', color: '#ffb800 !important' }} />}
          label="Đang Chở Khách"
          size="small"
          sx={{
            bgcolor: 'rgba(255, 184, 0, 0.12)',
            color: '#d97706',
            fontWeight: 700,
            fontSize: '0.78rem',
            border: '1px solid rgba(255, 184, 0, 0.3)',
          }}
        />
      );
    }
    if (status === 'ONLINE' || driver.isActive === true) {
      return (
        <Chip
          icon={<DotIcon sx={{ fontSize: '10px !important', color: '#15ca20 !important' }} />}
          label="Trực Tuyến"
          size="small"
          sx={{
            bgcolor: 'rgba(21, 202, 32, 0.12)',
            color: '#15ca20',
            fontWeight: 700,
            fontSize: '0.78rem',
            border: '1px solid rgba(21, 202, 32, 0.3)',
          }}
        />
      );
    }
    return (
      <Chip
        icon={<DotIcon sx={{ fontSize: '10px !important', color: '#94a3b8 !important' }} />}
        label="Ngoại Tuyến"
        size="small"
        sx={{
          bgcolor: 'rgba(148, 163, 184, 0.12)',
          color: 'text.secondary',
          fontWeight: 700,
          fontSize: '0.78rem',
          border: '1px solid rgba(148, 163, 184, 0.25)',
        }}
      />
    );
  };

  const filteredDrivers = drivers.filter((d) => {
    const term = searchTerm.toLowerCase();
    const matchesSearch =
      (d.id && String(d.id).includes(term)) ||
      (d.fullName && d.fullName.toLowerCase().includes(term)) ||
      (d.phoneNumber && d.phoneNumber.includes(term)) ||
      (d.email && d.email.toLowerCase().includes(term));

    if (!matchesSearch) return false;
    const isOnline = d.status === 'ONLINE' || d.isActive === true;
    if (statusFilter === 'ALL') return true;
    if (statusFilter === 'ONLINE') return isOnline;
    if (statusFilter === 'OFFLINE') return !isOnline;
    return true;
  });

  const onlineCount = drivers.filter((d) => d.status === 'ONLINE' || d.isActive === true).length;
  const offlineCount = drivers.length - onlineCount;

  const paginatedDrivers = filteredDrivers.slice(
    page * rowsPerPage,
    page * rowsPerPage + rowsPerPage
  );

  return (
    <Box sx={{ width: '100%' }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2.5, flexWrap: 'wrap', gap: 1.5 }}>
        <Box>
          <Typography variant="h5" sx={{ fontWeight: 700, fontFamily: '"Poppins", sans-serif' }}>
            Quản Lý Tài Xế & Đội Xe
          </Typography>
          <Typography variant="body2" sx={{ color: 'text.secondary', mt: 0.2 }}>
            Theo dõi trạng thái trực tuyến/ngoại tuyến của đối tác tài xế (`role: DRIVER`)
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={<RefreshIcon />}
          onClick={fetchDrivers}
          disabled={loading}
          sx={{ bgcolor: '#008cff', '&:hover': { bgcolor: '#0070cc' }, borderRadius: 2 }}
        >
          Làm Mới Dữ Liệu
        </Button>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 2.5 }}>
          {error}
        </Alert>
      )}

      {/* Main Table Card */}
      <Card sx={{ p: 2.5 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2.5, flexWrap: 'wrap', gap: 2 }}>
          <ButtonGroup variant="outlined">
            {[
              { label: 'Tất Cả', value: 'ALL', count: drivers.length },
              { label: 'Trực Tuyến', value: 'ONLINE', count: onlineCount },
              { label: 'Ngoại Tuyến', value: 'OFFLINE', count: offlineCount },
            ].map((tab) => (
              <Button
                key={tab.value}
                variant={statusFilter === tab.value ? 'contained' : 'outlined'}
                onClick={() => {
                  setStatusFilter(tab.value);
                  setPage(0);
                }}
                sx={{
                  px: 2,
                  py: 0.8,
                  fontWeight: 600,
                  fontSize: '0.85rem',
                  bgcolor: statusFilter === tab.value ? '#008cff' : 'transparent',
                }}
              >
                {tab.label} ({tab.count})
              </Button>
            ))}
          </ButtonGroup>

          <Box sx={{ maxWidth: 340, width: '100%' }}>
            <TextField
              size="small"
              placeholder="Tìm kiếm tài xế theo Tên, SĐT, Email..."
              fullWidth
              value={searchTerm}
              onChange={(e) => {
                setSearchTerm(e.target.value);
                setPage(0);
              }}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <SearchIcon sx={{ color: 'text.secondary', fontSize: 18 }} />
                  </InputAdornment>
                ),
              }}
            />
          </Box>
        </Box>

        <TableContainer>
          <Table sx={{ minWidth: 700 }}>
            <TableHead>
              <TableRow>
                <TableCell sx={{ width: 90 }}>MÃ ID</TableCell>
                <TableCell>HỌ VÀ TÊN TÀI XẾ</TableCell>
                <TableCell>SỐ ĐIỆN THOẠI</TableCell>
                <TableCell>ĐỊA CHỈ EMAIL</TableCell>
                <TableCell>NGÀY THAM GIA</TableCell>
                <TableCell align="center" sx={{ width: 200 }}>TRẠNG THÁI HOẠT ĐỘNG</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {loading ? (
                <TableRow>
                  <TableCell colSpan={6} align="center" sx={{ py: 6 }}>
                    <CircularProgress size={28} />
                    <Typography variant="body2" sx={{ mt: 1, color: 'text.secondary' }}>
                      Đang tải danh sách tài xế từ Backend...
                    </Typography>
                  </TableCell>
                </TableRow>
              ) : paginatedDrivers.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={6} align="center" sx={{ py: 5, color: 'text.secondary' }}>
                    {searchTerm ? 'Không tìm thấy tài xế phù hợp' : 'Hệ thống chưa có tài khoản nào có role DRIVER'}
                  </TableCell>
                </TableRow>
              ) : (
                paginatedDrivers.map((driver) => (
                  <TableRow key={driver.id} hover>
                    <TableCell sx={{ fontWeight: 800, fontFamily: 'monospace', color: '#008cff' }}>
                      #{driver.id}
                    </TableCell>
                    <TableCell sx={{ fontWeight: 700, color: 'text.primary' }}>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <DriverIcon sx={{ fontSize: 18, color: '#008cff' }} />
                        {driver.fullName || 'Tài xế đối tác'}
                      </Box>
                    </TableCell>
                    <TableCell sx={{ fontWeight: 600, fontFamily: 'monospace' }}>
                      {driver.phoneNumber}
                    </TableCell>
                    <TableCell sx={{ color: 'text.secondary' }}>
                      {driver.email || '—'}
                    </TableCell>
                    <TableCell sx={{ color: 'text.secondary', fontSize: '0.85rem' }}>
                      {driver.createdAt ? new Date(driver.createdAt).toLocaleDateString('vi-VN') : '—'}
                    </TableCell>
                    <TableCell align="center">
                      {getDriverStatusChip(driver)}
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </TableContainer>

        <TablePagination
          rowsPerPageOptions={[5, 10, 25, 50]}
          component="div"
          count={filteredDrivers.length}
          rowsPerPage={rowsPerPage}
          page={page}
          onPageChange={(_, newPage) => setPage(newPage)}
          onRowsPerPageChange={(e) => {
            setRowsPerPage(parseInt(e.target.value, 10));
            setPage(0);
          }}
          labelRowsPerPage="Dòng trên trang:"
          labelDisplayedRows={({ from, to, count }) => `${from}-${to} trên tổng ${count}`}
        />
      </Card>
    </Box>
  );
};

export default Drivers;
