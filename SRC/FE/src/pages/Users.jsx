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
  Chip,
  TextField,
  InputAdornment,
  CircularProgress,
  Alert,
  Button,
  ButtonGroup,
} from '@mui/material';
import {
  Search as SearchIcon,
  Refresh as RefreshIcon,
} from '@mui/icons-material';
import toast from 'react-hot-toast';
import userService from '../services/userService';

export const Users = () => {
  const [allUsers, setAllUsers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [searchTerm, setSearchTerm] = useState('');
  const [roleFilter, setRoleFilter] = useState('CUSTOMER');

  const fetchUsers = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await userService.getAllUsers({ page: 0, size: 100 });
      let list = [];
      if (response && response.content) {
        list = response.content;
      } else if (Array.isArray(response)) {
        list = response;
      } else if (response?.data) {
        list = Array.isArray(response.data) ? response.data : [response.data];
      }
      setAllUsers(list);
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Không thể tải danh sách người dùng từ Backend';
      setError(msg);
      toast.error(msg);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  const filteredUsers = allUsers.filter((u) => {
    const term = searchTerm.toLowerCase();
    const matchSearch =
      (u.fullName && u.fullName.toLowerCase().includes(term)) ||
      (u.phoneNumber && u.phoneNumber.includes(term)) ||
      (u.email && u.email.toLowerCase().includes(term));

    if (!matchSearch) return false;
    if (roleFilter === 'ALL') return true;
    return u.role === roleFilter;
  });

  const paginatedUsers = filteredUsers.slice(
    page * rowsPerPage,
    page * rowsPerPage + rowsPerPage
  );

  return (
    <Box className="page-enter-animation" sx={{ width: '100%', maxWidth: '100%', overflowX: 'hidden' }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: { xs: 'flex-start', sm: 'center' }, mb: 2.5, flexDirection: { xs: 'column', sm: 'row' }, gap: 1.5 }}>
        <Box>
          <Typography variant="h5" sx={{ fontWeight: 800 }}>
            Quản Lý Khách Hàng
          </Typography>
          <Typography variant="body2" sx={{ color: 'text.secondary', mt: 0.3, fontSize: { xs: '0.8rem', sm: '0.85rem' } }}>
            Danh sách tài khoản người dùng đăng ký trên hệ thống
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={<RefreshIcon />}
          onClick={fetchUsers}
          disabled={loading}
          sx={{ width: { xs: '100%', sm: 'auto' }, borderRadius: 2 }}
        >
          Làm Mới
        </Button>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 2.5 }}>
          {error}
        </Alert>
      )}

      {/* Main Container Card */}
      <Card sx={{ border: 1, borderColor: 'divider' }}>
        <CardContent sx={{ p: { xs: 1.8, sm: 3 } }}>
          {/* Controls Bar */}
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2.5, flexDirection: { xs: 'column', md: 'row' }, gap: 2 }}>
            <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', width: { xs: '100%', md: 'auto' } }}>
              {[
                { label: 'Khách Hàng', value: 'CUSTOMER', count: allUsers.filter((u) => u.role === 'CUSTOMER').length },
                { label: 'Tất Cả', value: 'ALL', count: allUsers.length },
                { label: 'Quản Trị Viên', value: 'ADMIN', count: allUsers.filter((u) => u.role === 'ADMIN').length },
              ].map((tab) => (
                <Button
                  key={tab.value}
                  variant={roleFilter === tab.value ? 'contained' : 'outlined'}
                  onClick={() => {
                    setRoleFilter(tab.value);
                    setPage(0);
                  }}
                  size="small"
                  sx={{
                    px: { xs: 1.5, sm: 2 },
                    py: 0.8,
                    fontWeight: 700,
                    fontSize: { xs: '0.78rem', sm: '0.85rem' },
                    borderRadius: 2,
                    flex: { xs: 1, sm: 'none' },
                  }}
                >
                  {tab.label} ({tab.count})
                </Button>
              ))}
            </Box>

            <Box sx={{ width: { xs: '100%', md: 360 } }}>
              <TextField
                placeholder="Tìm theo Tên, SĐT, Email..."
                fullWidth
                size="small"
                value={searchTerm}
                onChange={(e) => {
                  setSearchTerm(e.target.value);
                  setPage(0);
                }}
                slotProps={{
                  input: {
                    startAdornment: (
                      <InputAdornment position="start">
                        <SearchIcon sx={{ color: 'text.secondary', fontSize: 18 }} />
                      </InputAdornment>
                    ),
                  },
                }}
              />
            </Box>
          </Box>

          <TableContainer sx={{ border: 1, borderColor: 'divider', borderRadius: 2, overflowX: 'auto', width: '100%' }}>
            <Table sx={{ minWidth: 620 }} size="small">
              <TableHead>
                <TableRow>
                  <TableCell sx={{ width: 90, fontWeight: 700 }}>MÃ ID</TableCell>
                  <TableCell sx={{ fontWeight: 700 }}>HỌ VÀ TÊN</TableCell>
                  <TableCell sx={{ fontWeight: 700 }}>SỐ ĐIỆN THOẠI</TableCell>
                  <TableCell sx={{ fontWeight: 700 }}>ĐỊA CHỈ EMAIL</TableCell>
                  <TableCell sx={{ fontWeight: 700 }}>VAI TRÒ</TableCell>
                  <TableCell sx={{ fontWeight: 700 }}>NGÀY TẠO</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {loading ? (
                  <TableRow>
                    <TableCell colSpan={6} align="center" sx={{ py: 6 }}>
                      <CircularProgress size={32} />
                      <Typography variant="body1" sx={{ mt: 1.5, fontWeight: 600 }}>
                        Đang tải danh sách người dùng...
                      </Typography>
                    </TableCell>
                  </TableRow>
                ) : paginatedUsers.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={6} align="center" sx={{ py: 5 }}>
                      <Typography variant="body1" sx={{ color: 'text.secondary', fontWeight: 600 }}>
                        {searchTerm ? 'Không tìm thấy kết quả phù hợp' : 'Chưa có tài khoản nào theo bộ lọc này'}
                      </Typography>
                    </TableCell>
                  </TableRow>
                ) : (
                  paginatedUsers.map((user) => (
                    <TableRow key={user.id} hover>
                      <TableCell sx={{ fontWeight: 700, fontFamily: 'monospace', fontSize: '0.95rem' }}>
                        #{user.id}
                      </TableCell>
                      <TableCell sx={{ fontWeight: 700, color: 'text.primary', fontSize: '1rem' }}>
                        {user.fullName || 'Chưa cập nhật'}
                      </TableCell>
                      <TableCell sx={{ fontWeight: 600, fontFamily: 'monospace', fontSize: '0.95rem' }}>
                        {user.phoneNumber}
                      </TableCell>
                      <TableCell sx={{ color: 'text.primary', fontSize: '0.95rem' }}>
                        {user.email || '—'}
                      </TableCell>
                      <TableCell>
                        <Chip
                          label={user.role}
                          color={user.role === 'ADMIN' ? 'primary' : 'default'}
                          sx={{ fontWeight: 700 }}
                        />
                      </TableCell>
                      <TableCell sx={{ color: 'text.secondary', fontSize: '0.9rem', fontWeight: 500 }}>
                        {user.createdAt ? new Date(user.createdAt).toLocaleDateString('vi-VN') : '—'}
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </TableContainer>

          <TablePagination
            rowsPerPageOptions={[5, 10, 25]}
            component="div"
            count={filteredUsers.length}
            rowsPerPage={rowsPerPage}
            page={page}
            onPageChange={(e, newPage) => setPage(newPage)}
            onRowsPerPageChange={(e) => {
              setRowsPerPage(parseInt(e.target.value, 10));
              setPage(0);
            }}
            labelRowsPerPage="Số dòng mỗi trang:"
            sx={{ mt: 1 }}
          />
        </CardContent>
      </Card>
    </Box>
  );
};

export default Users;
