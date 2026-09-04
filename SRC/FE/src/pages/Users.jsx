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
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box>
          <Typography variant="h5" sx={{ fontWeight: 800 }}>
            Quản Lý Khách Hàng
          </Typography>
          <Typography variant="body1" sx={{ color: 'text.secondary', mt: 0.5 }}>
            Danh sách tài khoản người dùng đăng ký trên hệ thống
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={<RefreshIcon />}
          onClick={fetchUsers}
          disabled={loading}
        >
          Làm Mới
        </Button>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      {/* Main Container Card */}
      <Card sx={{ border: 1, borderColor: 'divider' }}>
        <CardContent sx={{ p: 3 }}>
          {/* Controls Bar */}
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3, flexWrap: 'wrap', gap: 2 }}>
            <ButtonGroup variant="outlined" sx={{ borderRadius: 2 }}>
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
                  sx={{
                    px: 2,
                    py: 1,
                    fontWeight: 700,
                    fontSize: '0.9rem',
                    borderRadius: '0 !important',
                  }}
                >
                  {tab.label} ({tab.count})
                </Button>
              ))}
            </ButtonGroup>

            <Box sx={{ maxWidth: 360, width: '100%' }}>
              <TextField
                placeholder="Tìm kiếm theo Tên, SĐT, Email..."
                fullWidth
                value={searchTerm}
                onChange={(e) => {
                  setSearchTerm(e.target.value);
                  setPage(0);
                }}
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <SearchIcon sx={{ color: 'text.secondary' }} />
                    </InputAdornment>
                  ),
                }}
              />
            </Box>
          </Box>

          <TableContainer sx={{ border: 1, borderColor: 'divider', borderRadius: 2 }}>
            <Table sx={{ minWidth: 700 }}>
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
