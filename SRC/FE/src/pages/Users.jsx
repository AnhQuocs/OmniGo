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
  IconButton,
  Tooltip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
} from '@mui/material';
import {
  Search as SearchIcon,
  Refresh as RefreshIcon,
  LockOpen as UnlockIcon,
  Lock as LockIcon,
  Close as CloseIcon,
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
  const [roleFilter, setRoleFilter] = useState('ALL');

  const getRoleChip = (role) => {
    switch (role) {
      case 'ADMIN':
        return (
          <Chip
            label="Quản trị viên"
            size="small"
            sx={{
              bgcolor: 'rgba(239, 68, 68, 0.12)',
              color: '#ef4444',
              border: '1px solid rgba(239, 68, 68, 0.25)',
              fontWeight: 700,
              fontSize: '0.75rem',
            }}
          />
        );
      case 'CUSTOMER':
        return (
          <Chip
            label="Khách hàng"
            size="small"
            sx={{
              bgcolor: 'rgba(0, 140, 255, 0.12)',
              color: '#008cff',
              border: '1px solid rgba(0, 140, 255, 0.25)',
              fontWeight: 700,
              fontSize: '0.75rem',
            }}
          />
        );
      case 'DRIVER':
        return (
          <Chip
            label="Tài xế"
            size="small"
            sx={{
              bgcolor: 'rgba(245, 158, 11, 0.12)',
              color: '#d97706',
              border: '1px solid rgba(245, 158, 11, 0.25)',
              fontWeight: 700,
              fontSize: '0.75rem',
            }}
          />
        );
      case 'RESTAURANT':
        return (
          <Chip
            label="Quán ăn"
            size="small"
            sx={{
              bgcolor: 'rgba(249, 115, 22, 0.12)',
              color: '#ea580c',
              border: '1px solid rgba(249, 115, 22, 0.25)',
              fontWeight: 700,
              fontSize: '0.75rem',
            }}
          />
        );
      default:
        return (
          <Chip
            label={role || 'Người dùng'}
            size="small"
            sx={{
              bgcolor: 'rgba(148, 163, 184, 0.15)',
              color: '#64748b',
              fontWeight: 700,
              fontSize: '0.75rem',
            }}
          />
        );
    }
  };

  // Lock Dialog state
  const [openLockDialog, setOpenLockDialog] = useState(false);
  const [selectedUserForLock, setSelectedUserForLock] = useState(null);
  const [lockReason, setLockReason] = useState('');
  const [lockSubmitting, setLockSubmitting] = useState(false);

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

  const handleOpenLockModal = (user) => {
    setSelectedUserForLock(user);
    setLockReason(user.isLocked ? '' : 'Vi phạm điều khoản sử dụng');
    setOpenLockDialog(true);
  };

  const handleCloseLockModal = () => {
    if (!lockSubmitting) {
      setOpenLockDialog(false);
      setSelectedUserForLock(null);
    }
  };

  const handleToggleLockSubmit = async () => {
    if (!selectedUserForLock) return;
    const isLocking = !selectedUserForLock.isLocked;

    if (isLocking && !lockReason.trim()) {
      toast.error('Vui lòng nhập lý do khóa tài khoản');
      return;
    }

    setLockSubmitting(true);
    try {
      await userService.toggleLockUser(selectedUserForLock.id, {
        isLocked: isLocking,
        reason: isLocking ? lockReason.trim() : '',
      });
      toast.success(isLocking ? 'Đã khóa tài khoản thành công' : 'Đã mở khóa tài khoản thành công');
      setOpenLockDialog(false);
      setSelectedUserForLock(null);
      fetchUsers();
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Thao tác khóa/mở khóa thất bại';
      toast.error(msg);
    } finally {
      setLockSubmitting(false);
    }
  };

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
            Quản Lý Khách Hàng & Người Dùng
          </Typography>
          <Typography variant="body2" sx={{ color: 'text.secondary', mt: 0.3, fontSize: { xs: '0.8rem', sm: '0.85rem' } }}>
            Danh sách tài khoản và quyền khóa / mở khóa truy cập hệ thống
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
      <Card sx={{ border: 1, borderColor: 'divider', minHeight: 'calc(90vh - 120px)', display: 'flex', flexDirection: 'column' }}>
        <CardContent sx={{ p: { xs: 1.8, sm: 3 }, flex: 1, display: 'flex', flexDirection: 'column' }}>
          {/* Controls Bar */}
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2.5, flexDirection: { xs: 'column', md: 'row' }, gap: 2 }}>
            <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', width: { xs: '100%', md: 'auto' } }}>
              {[
                { label: 'Tất Cả', value: 'ALL', count: allUsers.length },
                { label: 'Khách Hàng', value: 'CUSTOMER', count: allUsers.filter((u) => u.role === 'CUSTOMER').length },
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

          <TableContainer sx={{ border: 1, borderColor: 'divider', borderRadius: 2, overflowX: 'auto', width: '100%', flex: 1, minHeight: 380 }}>
            <Table sx={{ minWidth: 700 }} size="small">
              <TableHead>
                <TableRow>
                  <TableCell sx={{ width: 80, fontWeight: 700 }}>ID</TableCell>
                  <TableCell sx={{ fontWeight: 700 }}>Họ và tên</TableCell>
                  <TableCell sx={{ fontWeight: 700 }}>Số điện thoại</TableCell>
                  <TableCell sx={{ fontWeight: 700 }}>Email</TableCell>
                  <TableCell sx={{ fontWeight: 700 }}>Vai trò</TableCell>
                  <TableCell sx={{ fontWeight: 700 }}>Trạng thái</TableCell>
                  <TableCell sx={{ fontWeight: 700 }}>Ngày tạo</TableCell>
                  <TableCell align="center" sx={{ fontWeight: 700 }}>Hành động</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {loading ? (
                  <TableRow>
                    <TableCell colSpan={8} align="center" sx={{ py: 6 }}>
                      <CircularProgress size={32} />
                      <Typography variant="body1" sx={{ mt: 1.5, fontWeight: 600 }}>
                        Đang tải danh sách người dùng...
                      </Typography>
                    </TableCell>
                  </TableRow>
                ) : paginatedUsers.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={8} align="center" sx={{ py: 5 }}>
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
                        {getRoleChip(user.role)}
                      </TableCell>
                      <TableCell>
                        {user.isLocked ? (
                          <Tooltip title={`Lý do: ${user.lockedReason || 'Không có lý do'}`}>
                            <Box sx={{ display: 'inline-flex', alignItems: 'center', gap: 0.8, cursor: 'help' }}>
                              <Box sx={{ width: 8, height: 8, borderRadius: '50%', bgcolor: '#ef4444' }} />
                              <Typography variant="body2" sx={{ color: '#ef4444', fontWeight: 600, fontSize: '0.85rem' }}>
                                Bị khóa
                              </Typography>
                            </Box>
                          </Tooltip>
                        ) : (
                          <Box sx={{ display: 'inline-flex', alignItems: 'center', gap: 0.8 }}>
                            <Box sx={{ width: 8, height: 8, borderRadius: '50%', bgcolor: '#15ca20' }} />
                            <Typography variant="body2" sx={{ color: '#15ca20', fontWeight: 600, fontSize: '0.85rem' }}>
                              Hoạt động
                            </Typography>
                          </Box>
                        )}
                      </TableCell>
                      <TableCell sx={{ color: 'text.secondary', fontSize: '0.9rem', fontWeight: 500 }}>
                        {user.createdAt ? new Date(user.createdAt).toLocaleDateString('vi-VN') : '—'}
                      </TableCell>
                      <TableCell align="center">
                        {user.role !== 'ADMIN' && (
                          <Tooltip title={user.isLocked ? 'Mở khóa tài khoản' : 'Khóa tài khoản'}>
                            <IconButton
                              size="small"
                              onClick={() => handleOpenLockModal(user)}
                              sx={{
                                color: user.isLocked ? '#15ca20' : '#ef4444',
                                bgcolor: user.isLocked ? 'rgba(21, 202, 32, 0.1)' : 'rgba(239, 68, 68, 0.1)',
                                border: '1px solid',
                                borderColor: user.isLocked ? 'rgba(21, 202, 32, 0.25)' : 'rgba(239, 68, 68, 0.25)',
                                borderRadius: 2,
                                p: 0.9,
                                transition: 'all 0.2s',
                                '&:hover': {
                                  bgcolor: user.isLocked ? 'rgba(21, 202, 32, 0.2)' : 'rgba(239, 68, 68, 0.2)',
                                  transform: 'scale(1.05)',
                                },
                              }}
                            >
                              {user.isLocked ? <UnlockIcon sx={{ fontSize: 18 }} /> : <LockIcon sx={{ fontSize: 18 }} />}
                            </IconButton>
                          </Tooltip>
                        )}
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
            sx={{ mt: 'auto', pt: 1.5 }}
          />
        </CardContent>
      </Card>

      {/* Dialog: Lock / Unlock User */}
      <Dialog open={openLockDialog} onClose={handleCloseLockModal} maxWidth="xs" fullWidth>
        <DialogTitle component="div" sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', pb: 1 }}>
          <Typography variant="h6" sx={{ fontWeight: 800 }}>
            {selectedUserForLock?.isLocked ? 'Xác nhận mở khóa tài khoản' : 'Khóa tài khoản người dùng'}
          </Typography>
          <IconButton onClick={handleCloseLockModal} size="small" disabled={lockSubmitting}>
            <CloseIcon fontSize="small" />
          </IconButton>
        </DialogTitle>
        <DialogContent dividers>
          <Typography variant="body2" sx={{ mb: 2, color: 'text.secondary' }}>
            {selectedUserForLock?.isLocked
              ? `Bạn có chắc chắn muốn mở khóa cho tài khoản ${selectedUserForLock?.fullName} (${selectedUserForLock?.phoneNumber}) không?`
              : `Khóa tài khoản sẽ thu hồi quyền đăng nhập và chặn người dùng ${selectedUserForLock?.fullName} (${selectedUserForLock?.phoneNumber}) sử dụng ứng dụng.`}
          </Typography>
          {!selectedUserForLock?.isLocked && (
            <TextField
              label="Lý do khóa tài khoản"
              fullWidth
              multiline
              rows={3}
              required
              value={lockReason}
              onChange={(e) => setLockReason(e.target.value)}
              placeholder="Nhập lý do vi phạm (ví dụ: Spam cuốc xe, gian lận thanh toán...)"
              size="small"
            />
          )}
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={handleCloseLockModal} disabled={lockSubmitting} color="inherit">
            Hủy
          </Button>
          <Button
            onClick={handleToggleLockSubmit}
            variant="contained"
            color={selectedUserForLock?.isLocked ? 'success' : 'error'}
            disabled={lockSubmitting}
          >
            {lockSubmitting ? <CircularProgress size={20} color="inherit" /> : selectedUserForLock?.isLocked ? 'Mở Khóa Ngay' : 'Xác Nhận Khóa'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default Users;
