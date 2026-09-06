import React, { useState, useEffect } from 'react';
import {
  Box,
  Card,
  Grid,
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
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Avatar,
  Divider,
} from '@mui/material';
import {
  Search as SearchIcon,
  Refresh as RefreshIcon,
  Fastfood as FoodIcon,
  CheckCircle as SuccessIcon,
  TwoWheeler as DriverIcon,
  Cancel as CancelIcon,
  AttachMoney as MoneyIcon,
  Visibility as ViewIcon,
  Close as CloseIcon,
  Restaurant as ResIcon,
  Person as PersonIcon,
} from '@mui/icons-material';
import { useLocation, useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import foodService from '../services/foodService';

export const FoodOrders = () => {
  const location = useLocation();
  const navigate = useNavigate();

  const [orders, setOrders] = useState([]);
  const [stats, setStats] = useState({
    totalOrders: 0,
    completedOrders: 0,
    deliveringOrders: 0,
    preparingOrders: 0,
    pendingOrders: 0,
    cancelledOrders: 0,
    totalRevenue: 0,
    totalDeliveryFees: 0,
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');

  // Detail Modal State
  const [selectedOrder, setSelectedOrder] = useState(null);

  // Sync status filter from URL
  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const statusParam = params.get('status');
    if (statusParam) {
      setStatusFilter(statusParam.toUpperCase());
    } else {
      setStatusFilter('ALL');
    }
    setPage(0);
  }, [location.search]);

  const fetchData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [orderList, statsData] = await Promise.all([
        foodService.getAllFoodOrders(),
        foodService.getFoodStats(),
      ]);
      setOrders(Array.isArray(orderList) ? orderList : []);
      if (statsData) setStats(statsData);
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Không thể tải danh sách đơn giao đồ ăn';
      setError(msg);
      toast.error(msg);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleStatusTab = (val) => {
    setStatusFilter(val);
    setPage(0);
    if (val === 'ALL') {
      navigate('/food-orders');
    } else {
      navigate(`/food-orders?status=${val}`);
    }
  };

  const handleMarkPaid = async (orderId) => {
    if (!window.confirm(`Xác nhận đánh dấu đơn hàng #${orderId} đã thanh toán?`)) return;
    try {
      await foodService.markOrderPaid(orderId);
      toast.success(`Đã cập nhật trạng thái thanh toán đơn #${orderId}`);
      fetchData();
      if (selectedOrder?.id === orderId) {
        setSelectedOrder((prev) => ({ ...prev, isPaid: true }));
      }
    } catch (err) {
      toast.error(err.response?.data?.message || 'Lỗi cập nhật thanh toán');
    }
  };

  // Filtering
  const filteredOrders = orders.filter((o) => {
    const matchSearch =
      searchTerm === '' ||
      o.id?.toString().includes(searchTerm) ||
      o.customerName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      o.customerPhone?.includes(searchTerm) ||
      o.restaurantName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      o.deliveryAddress?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      o.driverId?.toString().includes(searchTerm);

    let matchStatus = true;
    if (statusFilter === 'PENDING') {
      matchStatus = o.status === 'PENDING' || o.status === 'ACCEPTED';
    } else if (statusFilter === 'PREPARING') {
      matchStatus = o.status === 'PREPARING' || o.status === 'READY_FOR_PICKUP';
    } else if (statusFilter === 'DELIVERING') {
      matchStatus = o.status === 'DELIVERING';
    } else if (statusFilter === 'COMPLETED') {
      matchStatus = o.status === 'COMPLETED';
    } else if (statusFilter === 'CANCELLED') {
      matchStatus =
        o.status === 'CANCELLED' || o.status === 'REJECTED' || o.status === 'NO_DRIVER_FOUND';
    }

    return matchSearch && matchStatus;
  });

  const paginatedList = filteredOrders.slice(
    page * rowsPerPage,
    page * rowsPerPage + rowsPerPage
  );

  const getStatusChip = (status) => {
    switch (status) {
      case 'PENDING':
        return <Chip label="⏳ Chờ Duyệt" size="small" sx={{ bgcolor: 'rgba(255, 170, 0, 0.15)', color: '#ffaa00', fontWeight: 700 }} />;
      case 'ACCEPTED':
        return <Chip label="👌 Quán Đã Nhận" size="small" sx={{ bgcolor: 'rgba(0, 140, 255, 0.15)', color: '#008cff', fontWeight: 700 }} />;
      case 'PREPARING':
        return <Chip label="🍳 Đang Nấu Món" size="small" sx={{ bgcolor: 'rgba(124, 58, 237, 0.15)', color: '#7c3aed', fontWeight: 700 }} />;
      case 'READY_FOR_PICKUP':
        return <Chip label="🛵 Chờ Tài Xế Đến" size="small" sx={{ bgcolor: 'rgba(249, 115, 22, 0.15)', color: '#f97316', fontWeight: 700 }} />;
      case 'DELIVERING':
        return <Chip label="🚀 Đang Giao Hàng" size="small" sx={{ bgcolor: 'rgba(59, 130, 246, 0.15)', color: '#3b82f6', fontWeight: 700 }} />;
      case 'COMPLETED':
        return <Chip label="✅ Giao Hoàn Tất" size="small" sx={{ bgcolor: 'rgba(21, 202, 32, 0.15)', color: '#15ca20', fontWeight: 700 }} />;
      case 'CANCELLED':
        return <Chip label="❌ Khách Đã Hủy" size="small" sx={{ bgcolor: 'rgba(255, 51, 102, 0.15)', color: '#ff3366', fontWeight: 700 }} />;
      case 'REJECTED':
        return <Chip label="🚫 Quán Từ Chối" size="small" sx={{ bgcolor: 'rgba(255, 51, 102, 0.15)', color: '#ff3366', fontWeight: 700 }} />;
      case 'NO_DRIVER_FOUND':
        return <Chip label="⚠️ Không Thấy Tài Xế" size="small" sx={{ bgcolor: 'rgba(255, 51, 102, 0.15)', color: '#ff3366', fontWeight: 700 }} />;
      default:
        return <Chip label={status} size="small" />;
    }
  };

  return (
    <Box sx={{ p: { xs: 2, md: 3 } }}>
      {/* Header */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box>
          <Typography variant="h5" sx={{ fontWeight: 800, color: 'text.primary' }}>
            🍲 Giám Sát Đơn Giao Đồ Ăn (Food Orders)
          </Typography>
          <Typography variant="body2" sx={{ color: 'text.secondary' }}>
            Theo dõi hành trình đơn hàng, khách hàng, nhà hàng, tài xế giao và trạng thái thanh toán thời gian thực
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={<RefreshIcon />}
          onClick={fetchData}
          disabled={loading}
          sx={{
            bgcolor: '#f97316',
            '&:hover': { bgcolor: '#ea580c' },
            borderRadius: 2,
            textTransform: 'none',
            fontWeight: 700,
          }}
        >
          Làm Mới
        </Button>
      </Box>

      {/* Stats Cards */}
      <Grid container spacing={2.5} sx={{ mb: 3 }}>
        <Grid item xs={12} sm={6} md={3}>
          <Card
            sx={{
              p: 2.5,
              borderRadius: 2.5,
              bgcolor: 'background.paper',
              border: 1,
              borderColor: 'divider',
              display: 'flex',
              alignItems: 'center',
              gap: 2,
            }}
          >
            <Avatar sx={{ bgcolor: 'rgba(249, 115, 22, 0.12)', color: '#f97316', width: 48, height: 48 }}>
              <FoodIcon />
            </Avatar>
            <Box>
              <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600 }}>
                Tổng Đơn Đặt Món
              </Typography>
              <Typography variant="h5" sx={{ fontWeight: 800, color: 'text.primary' }}>
                {stats.totalOrders || orders.length}
              </Typography>
            </Box>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card
            sx={{
              p: 2.5,
              borderRadius: 2.5,
              bgcolor: 'background.paper',
              border: 1,
              borderColor: 'divider',
              display: 'flex',
              alignItems: 'center',
              gap: 2,
            }}
          >
            <Avatar sx={{ bgcolor: 'rgba(21, 202, 32, 0.12)', color: '#15ca20', width: 48, height: 48 }}>
              <SuccessIcon />
            </Avatar>
            <Box>
              <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600 }}>
                Giao Thành Công
              </Typography>
              <Typography variant="h5" sx={{ fontWeight: 800, color: '#15ca20' }}>
                {stats.completedOrders || 0}
              </Typography>
            </Box>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card
            sx={{
              p: 2.5,
              borderRadius: 2.5,
              bgcolor: 'background.paper',
              border: 1,
              borderColor: 'divider',
              display: 'flex',
              alignItems: 'center',
              gap: 2,
            }}
          >
            <Avatar sx={{ bgcolor: 'rgba(59, 130, 246, 0.12)', color: '#3b82f6', width: 48, height: 48 }}>
              <DriverIcon />
            </Avatar>
            <Box>
              <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600 }}>
                Đang Nấu / Đang Giao
              </Typography>
              <Typography variant="h5" sx={{ fontWeight: 800, color: '#3b82f6' }}>
                {(stats.deliveringOrders || 0) + (stats.preparingOrders || 0)}
              </Typography>
            </Box>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card
            sx={{
              p: 2.5,
              borderRadius: 2.5,
              bgcolor: 'background.paper',
              border: 1,
              borderColor: 'divider',
              display: 'flex',
              alignItems: 'center',
              gap: 2,
            }}
          >
            <Avatar sx={{ bgcolor: 'rgba(0, 140, 255, 0.12)', color: '#008cff', width: 48, height: 48 }}>
              <MoneyIcon />
            </Avatar>
            <Box>
              <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600 }}>
                Doanh Thu Ẩm Thực
              </Typography>
              <Typography variant="h6" sx={{ fontWeight: 800, color: '#008cff' }}>
                {Number(stats.totalRevenue || 0).toLocaleString('vi-VN')} đ
              </Typography>
            </Box>
          </Card>
        </Grid>
      </Grid>

      {/* Main Content Card */}
      <Card sx={{ borderRadius: 2.5, bgcolor: 'background.paper', border: 1, borderColor: 'divider' }}>
        {/* Filter Tabs & Search */}
        <Box
          sx={{
            p: 2,
            display: 'flex',
            flexDirection: { xs: 'column', md: 'row' },
            gap: 2,
            justifyContent: 'space-between',
            alignItems: 'center',
            borderBottom: 1,
            borderColor: 'divider',
          }}
        >
          <ButtonGroup size="small" variant="outlined" sx={{ flexWrap: 'wrap' }}>
            <Button
              variant={statusFilter === 'ALL' ? 'contained' : 'outlined'}
              onClick={() => handleStatusTab('ALL')}
              sx={{ textTransform: 'none', fontWeight: 600 }}
            >
              Tất cả ({orders.length})
            </Button>
            <Button
              variant={statusFilter === 'PENDING' ? 'contained' : 'outlined'}
              onClick={() => handleStatusTab('PENDING')}
              sx={{ textTransform: 'none', fontWeight: 600 }}
            >
              Chờ duyệt ({stats.pendingOrders || 0})
            </Button>
            <Button
              variant={statusFilter === 'PREPARING' ? 'contained' : 'outlined'}
              onClick={() => handleStatusTab('PREPARING')}
              sx={{ textTransform: 'none', fontWeight: 600 }}
            >
              Đang nấu ({stats.preparingOrders || 0})
            </Button>
            <Button
              variant={statusFilter === 'DELIVERING' ? 'contained' : 'outlined'}
              onClick={() => handleStatusTab('DELIVERING')}
              sx={{ textTransform: 'none', fontWeight: 600 }}
            >
              Đang giao ({stats.deliveringOrders || 0})
            </Button>
            <Button
              variant={statusFilter === 'COMPLETED' ? 'contained' : 'outlined'}
              onClick={() => handleStatusTab('COMPLETED')}
              sx={{ textTransform: 'none', fontWeight: 600, color: statusFilter === 'COMPLETED' ? '#fff' : '#15ca20' }}
            >
              Hoàn tất ({stats.completedOrders || 0})
            </Button>
            <Button
              variant={statusFilter === 'CANCELLED' ? 'contained' : 'outlined'}
              onClick={() => handleStatusTab('CANCELLED')}
              sx={{ textTransform: 'none', fontWeight: 600, color: statusFilter === 'CANCELLED' ? '#fff' : '#ff3366' }}
            >
              Đã hủy ({stats.cancelledOrders || 0})
            </Button>
          </ButtonGroup>

          <TextField
            size="small"
            placeholder="Tìm mã đơn, tên khách, SĐT, quán..."
            value={searchTerm}
            onChange={(e) => { setSearchTerm(e.target.value); setPage(0); }}
            sx={{ width: { xs: '100%', sm: 300 } }}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon fontSize="small" sx={{ color: 'text.secondary' }} />
                </InputAdornment>
              ),
            }}
          />
        </Box>

        {error && (
          <Alert severity="error" sx={{ m: 2 }}>
            {error}
          </Alert>
        )}

        {/* Table */}
        <TableContainer>
          <Table sx={{ minWidth: 850 }}>
            <TableHead sx={{ bgcolor: 'action.hover' }}>
              <TableRow>
                <TableCell sx={{ fontWeight: 700, width: 70 }}>#Mã Đơn</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Khách Hàng</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Nhà Hàng</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Tài Xế Giao</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Món Đã Đặt</TableCell>
                <TableCell sx={{ fontWeight: 700, textAlign: 'right' }}>Tổng Tiền</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Trạng Thái</TableCell>
                <TableCell sx={{ fontWeight: 700, textAlign: 'center' }}>Thao Tác</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {loading ? (
                <TableRow>
                  <TableCell colSpan={8} align="center" sx={{ py: 6 }}>
                    <CircularProgress size={36} />
                    <Typography variant="body2" sx={{ mt: 1.5, color: 'text.secondary' }}>
                      Đang tải danh sách đơn đặt món...
                    </Typography>
                  </TableCell>
                </TableRow>
              ) : paginatedList.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={8} align="center" sx={{ py: 6 }}>
                    <FoodIcon sx={{ fontSize: 48, color: 'text.secondary', mb: 1 }} />
                    <Typography variant="body1" sx={{ fontWeight: 600, color: 'text.secondary' }}>
                      Không có đơn đặt món nào phù hợp
                    </Typography>
                  </TableCell>
                </TableRow>
              ) : (
                paginatedList.map((order) => {
                  const dateStr = order.createdAt
                    ? new Date(order.createdAt).toLocaleTimeString('vi-VN', {
                        hour: '2-digit',
                        minute: '2-digit',
                        day: '2-digit',
                        month: '2-digit',
                      })
                    : 'N/A';

                  const itemsList = order.orderItems || order.items || [];
                  const itemsSummary = itemsList
                    .map((i) => `${i.itemName || i.dishName || 'Món'} (x${i.quantity})`)
                    .join(', ');

                  return (
                    <TableRow key={order.id} hover sx={{ '&:last-child td, &:last-child th': { border: 0 } }}>
                      <TableCell sx={{ fontWeight: 700 }}>
                        <Typography variant="body2" sx={{ fontWeight: 800, color: '#f97316' }}>
                          #{order.id}
                        </Typography>
                        <Typography variant="caption" sx={{ color: 'text.secondary', fontSize: '0.72rem' }}>
                          🕒 {dateStr}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2" sx={{ fontWeight: 700 }}>
                          {order.customerName || `Khách #${order.customerId}`}
                        </Typography>
                        <Typography variant="caption" sx={{ color: 'text.secondary' }}>
                          📞 {order.customerPhone || 'Chưa có SĐT'}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2" sx={{ fontWeight: 600 }}>
                          {order.restaurantName || `Quán #${order.restaurantId}`}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        {order.driverId ? (
                          <Chip
                            icon={<DriverIcon sx={{ fontSize: '14px !important' }} />}
                            label={`Tài xế #${order.driverId}`}
                            size="small"
                            sx={{ bgcolor: 'rgba(59, 130, 246, 0.12)', color: '#3b82f6', fontWeight: 600 }}
                          />
                        ) : (
                          <Typography variant="caption" sx={{ color: 'text.secondary', fontStyle: 'italic' }}>
                            Chưa nhận
                          </Typography>
                        )}
                      </TableCell>
                      <TableCell sx={{ maxWidth: 220, fontSize: '0.85rem' }}>
                        <Typography
                          variant="body2"
                          sx={{
                            whiteSpace: 'nowrap',
                            overflow: 'hidden',
                            textOverflow: 'ellipsis',
                            maxWidth: 200,
                            fontSize: '0.82rem',
                          }}
                          title={itemsSummary}
                        >
                          🍛 {itemsSummary || 'Chi tiết món ăn'}
                        </Typography>
                      </TableCell>
                      <TableCell align="right">
                        <Typography variant="body2" sx={{ fontWeight: 800, color: '#008cff' }}>
                          {Number(order.totalPrice || order.totalAmount || 0).toLocaleString('vi-VN')} đ
                        </Typography>
                        <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 0.5, mt: 0.5 }}>
                          <Chip
                            label={order.paymentMethod || 'CASH'}
                            size="small"
                            sx={{ fontSize: '0.68rem', height: 18 }}
                          />
                          <Chip
                            label={order.isPaid ? 'Đã Trả' : 'Chưa Trả'}
                            size="small"
                            color={order.isPaid ? 'success' : 'default'}
                            sx={{ fontSize: '0.68rem', height: 18 }}
                          />
                        </Box>
                      </TableCell>
                      <TableCell>{getStatusChip(order.status)}</TableCell>
                      <TableCell align="center">
                        <Button
                          size="small"
                          variant="outlined"
                          startIcon={<ViewIcon />}
                          onClick={() => setSelectedOrder(order)}
                          sx={{ textTransform: 'none', borderRadius: 1.5, fontSize: '0.8rem', fontWeight: 600 }}
                        >
                          Chi Tiết
                        </Button>
                      </TableCell>
                    </TableRow>
                  );
                })
              )}
            </TableBody>
          </Table>
        </TableContainer>

        {/* Pagination */}
        <TablePagination
          component="div"
          count={filteredOrders.length}
          page={page}
          onPageChange={(_, newPage) => setPage(newPage)}
          rowsPerPage={rowsPerPage}
          onRowsPerPageChange={(e) => {
            setRowsPerPage(parseInt(e.target.value, 10));
            setPage(0);
          }}
          labelRowsPerPage="Số hàng mỗi trang:"
          rowsPerPageOptions={[5, 10, 25, 50]}
        />
      </Card>

      {/* Modal: Chi tiết Đơn hàng */}
      <Dialog
        open={Boolean(selectedOrder)}
        onClose={() => setSelectedOrder(null)}
        maxWidth="sm"
        fullWidth
        PaperProps={{ sx: { borderRadius: 3 } }}
      >
        <DialogTitle sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
            <FoodIcon sx={{ color: '#f97316' }} />
            <Box>
              <Typography variant="h6" sx={{ fontWeight: 800 }}>
                Chi Tiết Đơn Hàng #{selectedOrder?.id}
              </Typography>
              <Typography variant="caption" sx={{ color: 'text.secondary' }}>
                Trạng thái: {getStatusChip(selectedOrder?.status)}
              </Typography>
            </Box>
          </Box>
          <IconButton onClick={() => setSelectedOrder(null)} size="small">
            <CloseIcon />
          </IconButton>
        </DialogTitle>
        <Divider />
        <DialogContent sx={{ p: 3 }}>
          {selectedOrder && (
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
              {/* Customer & Restaurant Grid */}
              <Grid container spacing={2}>
                <Grid item xs={12} sm={6}>
                  <Card sx={{ p: 1.5, bgcolor: 'background.default', border: 1, borderColor: 'divider' }}>
                    <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 700, display: 'flex', alignItems: 'center', gap: 0.5 }}>
                      <PersonIcon sx={{ fontSize: 16 }} /> Người Nhận Hàng
                    </Typography>
                    <Typography variant="body2" sx={{ fontWeight: 700, mt: 0.5 }}>
                      {selectedOrder.customerName || `Khách #${selectedOrder.customerId}`}
                    </Typography>
                    <Typography variant="caption" sx={{ color: 'text.secondary' }}>
                      📞 {selectedOrder.customerPhone || 'Chưa có SĐT'}
                    </Typography>
                    <Typography variant="caption" sx={{ display: 'block', mt: 0.5 }}>
                      📍 {selectedOrder.deliveryAddress}
                    </Typography>
                  </Card>
                </Grid>

                <Grid item xs={12} sm={6}>
                  <Card sx={{ p: 1.5, bgcolor: 'background.default', border: 1, borderColor: 'divider' }}>
                    <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 700, display: 'flex', alignItems: 'center', gap: 0.5 }}>
                      <ResIcon sx={{ fontSize: 16 }} /> Nhà Hàng & Tài Xế
                    </Typography>
                    <Typography variant="body2" sx={{ fontWeight: 700, mt: 0.5 }}>
                      {selectedOrder.restaurantName || `Nhà Hàng #${selectedOrder.restaurantId}`}
                    </Typography>
                    <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block' }}>
                      🛵 {selectedOrder.driverId ? `Tài xế nhận đơn: #${selectedOrder.driverId}` : 'Chưa có tài xế'}
                    </Typography>
                    {selectedOrder.note && (
                      <Typography variant="caption" sx={{ color: '#c2410c', fontWeight: 600, display: 'block', mt: 0.5 }}>
                        📝 Ghi chú: {selectedOrder.note}
                      </Typography>
                    )}
                  </Card>
                </Grid>
              </Grid>

              {/* Items List */}
              <Box>
                <Typography variant="subtitle2" sx={{ fontWeight: 700, mb: 1 }}>
                  🍽️ Danh Sách Món Ăn:
                </Typography>
                <TableContainer sx={{ border: 1, borderColor: 'divider', borderRadius: 2 }}>
                  <Table size="small">
                    <TableHead sx={{ bgcolor: 'action.hover' }}>
                      <TableRow>
                        <TableCell sx={{ fontWeight: 700 }}>Tên Món</TableCell>
                        <TableCell sx={{ fontWeight: 700 }} align="center">SL</TableCell>
                        <TableCell sx={{ fontWeight: 700 }} align="right">Đơn Giá</TableCell>
                        <TableCell sx={{ fontWeight: 700 }} align="right">Thành Tiền</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {(selectedOrder.orderItems || selectedOrder.items || []).map((item, idx) => (
                        <TableRow key={idx}>
                          <TableCell sx={{ fontWeight: 600 }}>{item.itemName || item.dishName || 'Món'}</TableCell>
                          <TableCell align="center">x{item.quantity}</TableCell>
                          <TableCell align="right">{Number(item.price || 0).toLocaleString('vi-VN')} đ</TableCell>
                          <TableCell align="right" sx={{ fontWeight: 700 }}>
                            {Number(item.subtotal || (item.price * item.quantity) || 0).toLocaleString('vi-VN')} đ
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              </Box>

              {/* Pricing Summary */}
              <Box sx={{ p: 2, bgcolor: 'background.default', borderRadius: 2, border: 1, borderColor: 'divider' }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.5, fontSize: '0.88rem' }}>
                  <Typography variant="body2" sx={{ color: 'text.secondary' }}>Phí giao hàng (Ship):</Typography>
                  <Typography variant="body2" sx={{ fontWeight: 600 }}>
                    {Number(selectedOrder.deliveryFee || 0).toLocaleString('vi-VN')} đ
                  </Typography>
                </Box>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1, fontSize: '0.88rem' }}>
                  <Typography variant="body2" sx={{ color: 'text.secondary' }}>Phương thức:</Typography>
                  <Typography variant="body2" sx={{ fontWeight: 600 }}>
                    {selectedOrder.paymentMethod} ({selectedOrder.isPaid ? 'Đã thanh toán' : 'Chưa thanh toán'})
                  </Typography>
                </Box>
                <Divider sx={{ my: 1 }} />
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <Typography variant="subtitle1" sx={{ fontWeight: 800 }}>TỔNG THANH TOÁN:</Typography>
                  <Typography variant="h6" sx={{ fontWeight: 800, color: '#f97316' }}>
                    {Number(selectedOrder.totalPrice || selectedOrder.totalAmount || 0).toLocaleString('vi-VN')} đ
                  </Typography>
                </Box>
              </Box>
            </Box>
          )}
        </DialogContent>
        <DialogActions sx={{ p: 2, justifyContent: 'space-between' }}>
          {selectedOrder && !selectedOrder.isPaid && (
            <Button
              onClick={() => handleMarkPaid(selectedOrder.id)}
              variant="outlined"
              color="success"
              sx={{ textTransform: 'none', fontWeight: 700 }}
            >
              ✅ Xác Nhận Đã Thanh Toán
            </Button>
          )}
          <Button onClick={() => setSelectedOrder(null)} variant="contained" sx={{ textTransform: 'none', ml: 'auto' }}>
            Đóng
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default FoodOrders;
