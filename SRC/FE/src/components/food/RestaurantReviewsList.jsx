import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Rating,
  LinearProgress,
  Stack,
  Chip,
  Card,
  CardContent,
  Avatar,
  Divider,
  Button,
  TextField,
  CircularProgress,
  Dialog,
  Pagination,
} from '@mui/material';
import StarIcon from '@mui/icons-material/Star';
import StorefrontIcon from '@mui/icons-material/Storefront';
import DeliveryDiningIcon from '@mui/icons-material/DeliveryDining';
import ReplyIcon from '@mui/icons-material/Reply';
import foodService from '../../services/foodService';
import toast from 'react-hot-toast';

export default function RestaurantReviewsList({ restaurantId, isMerchantOwner = false }) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [selectedRating, setSelectedRating] = useState(null);
  const [page, setPage] = useState(0);
  const [replyingReviewId, setReplyingReviewId] = useState(null);
  const [replyText, setReplyText] = useState('');
  const [replyLoading, setReplyLoading] = useState(false);
  const [previewImage, setPreviewImage] = useState(null);
  const [filterUnreplied, setFilterUnreplied] = useState(false);

  const fetchReviews = async () => {
    setLoading(true);
    try {
      const res = await foodService.getRestaurantReviews(restaurantId, selectedRating, page, 10);
      setData(res);
    } catch (err) {
      console.warn('Lỗi tải đánh giá quán:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (restaurantId) {
      fetchReviews();
    }
  }, [restaurantId, selectedRating, page]);

  const handleSendReply = async (reviewId) => {
    if (!replyText.trim()) {
      toast.error('Vui lòng nhập nội dung phản hồi');
      return;
    }

    setReplyLoading(true);
    try {
      await foodService.replyReview(reviewId, { reply: replyText.trim() });
      toast.success('Gửi phản hồi thành công!');
      setReplyingReviewId(null);
      setReplyText('');
      fetchReviews();
    } catch (err) {
      toast.error(err.response?.data?.message || err.message || 'Lỗi gửi phản hồi');
    } finally {
      setReplyLoading(false);
    }
  };

  if (loading && !data) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', py: 5 }}>
        <CircularProgress />
      </Box>
    );
  }

  const avg = data?.averageRating || 5.0;
  const total = data?.totalReviews || 0;
  const counts = data?.ratingCounts || {};

  return (
    <Box sx={{ mt: 2 }}>
      {/* 1. TỔNG QUAN ĐIỂM ĐÁNH GIÁ */}
      <Card variant="outlined" sx={{ mb: 3, p: 2.5, borderRadius: 2.5, bgcolor: '#fbfcfd' }}>
        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={4} alignItems="center">
          {/* Cột trái: Điểm số to */}
          <Box sx={{ textAlign: 'center', minWidth: 150 }}>
            <Typography variant="h3" fontWeight={800} color="primary.main">
              {avg.toFixed(1)}
            </Typography>
            <Rating
              value={avg}
              precision={0.1}
              readOnly
              size="medium"
              emptyIcon={<StarIcon style={{ opacity: 0.3 }} fontSize="inherit" />}
              sx={{ my: 0.5 }}
            />
            <Typography variant="body2" color="text.secondary">
              ({total} lượt đánh giá)
            </Typography>
          </Box>

          {/* Cột phải: Thanh phân bổ sao */}
          <Box sx={{ flex: 1, width: '100%' }}>
            {[5, 4, 3, 2, 1].map((star) => {
              const count = counts[star] || 0;
              const percent = total > 0 ? (count / total) * 100 : 0;
              return (
                <Stack key={star} direction="row" spacing={1.5} alignItems="center" sx={{ mb: 0.8 }}>
                  <Typography variant="caption" sx={{ width: 35, fontWeight: 600 }}>
                    {star} sao
                  </Typography>
                  <LinearProgress
                    variant="determinate"
                    value={percent}
                    sx={{
                      flex: 1,
                      height: 8,
                      borderRadius: 4,
                      bgcolor: '#e2e8f0',
                      '& .MuiLinearProgress-bar': {
                        bgcolor: star >= 4 ? '#ffb400' : star === 3 ? '#ff9800' : '#f44336',
                        borderRadius: 4,
                      },
                    }}
                  />
                  <Typography variant="caption" color="text.secondary" sx={{ width: 45, textAlign: 'right' }}>
                    {count} ({Math.round(percent)}%)
                  </Typography>
                </Stack>
              );
            })}
          </Box>
        </Stack>
      </Card>

      {/* 2. BỘ LỌC SAO */}
      <Stack direction="row" spacing={1} sx={{ mb: 3, flexWrap: 'wrap', gap: 1 }}>
        <Chip
          label="Tất cả"
          variant={selectedRating === null ? 'filled' : 'outlined'}
          color={selectedRating === null ? 'primary' : 'default'}
          onClick={() => {
            setSelectedRating(null);
            setPage(0);
          }}
          sx={{ fontWeight: 600 }}
        />
        {[5, 4, 3, 2, 1].map((s) => (
          <Chip
            key={s}
            icon={<StarIcon sx={{ fontSize: '16px !important' }} />}
            label={`${s} sao (${counts[s] || 0})`}
            variant={selectedRating === s ? 'filled' : 'outlined'}
            color={selectedRating === s ? 'primary' : 'default'}
            onClick={() => {
              setSelectedRating(s);
              setPage(0);
            }}
            sx={{ fontWeight: 600 }}
          />
        ))}
        {isMerchantOwner && (
          <Chip
            icon={<ReplyIcon sx={{ fontSize: '16px !important' }} />}
            label={`Chưa phản hồi (${(data?.reviews?.content || []).filter(r => !r.merchantReply && !r.restaurantReply).length})`}
            variant={filterUnreplied ? 'filled' : 'outlined'}
            color={filterUnreplied ? 'warning' : 'default'}
            onClick={() => {
              setFilterUnreplied(!filterUnreplied);
              setPage(0);
            }}
            sx={{ fontWeight: 700 }}
          />
        )}
      </Stack>

      {/* 3. DANH SÁCH CÁC REVIEW */}
      {(() => {
        const displayedReviews = (data?.reviews?.content || []).filter(
          (r) => !filterUnreplied || (!r.merchantReply && !r.restaurantReply)
        );

        if (displayedReviews.length === 0) {
          return (
            <Box sx={{ textAlign: 'center', py: 6, color: 'text.secondary' }}>
              <Typography variant="body1">Chưa có đánh giá nào phù hợp với bộ lọc.</Typography>
            </Box>
          );
        }

        return (
          <Stack spacing={2}>
            {displayedReviews.map((review) => {
            // Mask customer name (VD: Nguyen Van A -> Ng*** A)
            const name = review.customerName || 'Khách hàng';
            const maskedName = name.length > 3 ? `${name.substring(0, 2)}***${name.slice(-1)}` : name;

            return (
              <Card key={review.id} variant="outlined" sx={{ borderRadius: 2 }}>
                <CardContent sx={{ p: 2.5 }}>
                  <Stack direction="row" spacing={1.5} alignItems="center">
                    <Avatar sx={{ width: 38, height: 38, bgcolor: 'primary.light', fontWeight: 700 }}>
                      {name.charAt(0).toUpperCase()}
                    </Avatar>
                    <Box>
                      <Stack direction="row" spacing={1} alignItems="center">
                        <Typography variant="subtitle2" fontWeight={700}>
                          {maskedName}
                        </Typography>
                        <Rating
                          value={review.restaurantRating}
                          readOnly
                          size="small"
                          emptyIcon={<StarIcon style={{ opacity: 0.3 }} fontSize="inherit" />}
                        />
                      </Stack>
                      <Typography variant="caption" color="text.secondary">
                        {review.createdAt ? new Date(review.createdAt).toLocaleDateString('vi-VN') : ''}
                        {review.isEdited && ' • Đã chỉnh sửa'}
                      </Typography>
                    </Box>
                  </Stack>

                  {/* Nhận xét món ăn */}
                  <Typography variant="body2" sx={{ mt: 1.5, mb: 1, color: '#334155' }}>
                    {review.restaurantComment || 'Khách hàng không để lại nhận xét bằng lời.'}
                  </Typography>

                  {/* Hình ảnh món ăn đính kèm */}
                  {(() => {
                    const reviewImages = review.restaurantImages || review.images || [];
                    if (!reviewImages || reviewImages.length === 0) return null;
                    return (
                      <Stack direction="row" spacing={1} sx={{ mt: 1, mb: 1.5, flexWrap: 'wrap', gap: 1 }}>
                        {reviewImages.map((img, i) => (
                          <Box
                            key={i}
                            onClick={() => setPreviewImage(img)}
                            sx={{
                              width: 80,
                              height: 80,
                              borderRadius: 1.5,
                              overflow: 'hidden',
                              cursor: 'pointer',
                              border: '1px solid #e2e8f0',
                              '&:hover': { opacity: 0.85, transform: 'scale(1.03)' },
                              transition: 'transform 0.15s ease',
                            }}
                          >
                            <img
                              src={img}
                              alt="review-dish"
                              style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                            />
                          </Box>
                        ))}
                      </Stack>
                    );
                  })()}

                  {/* Đánh giá tài xế */}
                  {review.driverRating && (
                    <Box
                      sx={{
                        display: 'inline-flex',
                        alignItems: 'center',
                        gap: 1,
                        bgcolor: '#f1f5f9',
                        px: 1.5,
                        py: 0.5,
                        borderRadius: 2,
                        mt: 0.5,
                      }}
                    >
                      <DeliveryDiningIcon sx={{ fontSize: 16, color: '#64748b' }} />
                      <Typography variant="caption" color="text.secondary">
                        Tài xế:
                      </Typography>
                      <Rating value={review.driverRating} readOnly size="small" sx={{ fontSize: 14 }} />
                      {review.driverComment && (
                        <Typography variant="caption" color="text.secondary">
                          - "{review.driverComment}"
                        </Typography>
                      )}
                    </Box>
                  )}

                  {/* PHẢN HỒI CỦA QUÁN (MERCHANT REPLY) */}
                  {review.merchantReply && (
                    <Box
                      sx={{
                        mt: 2,
                        p: 1.8,
                        borderRadius: 2,
                        bgcolor: '#f8fafc',
                        borderLeft: '3px solid #3b82f6',
                      }}
                    >
                      <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 0.5 }}>
                        <StorefrontIcon sx={{ fontSize: 16, color: '#2563eb' }} />
                        <Typography variant="caption" fontWeight={700} color="primary.main">
                          Phản hồi của Quán
                        </Typography>
                        {review.merchantRepliedAt && (
                          <Typography variant="caption" color="text.secondary">
                            • {new Date(review.merchantRepliedAt).toLocaleDateString('vi-VN')}
                          </Typography>
                        )}
                      </Stack>
                      <Typography variant="body2" color="text.primary">
                        {review.merchantReply}
                      </Typography>
                    </Box>
                  )}

                  {/* Nút phản hồi dành cho Chủ quán */}
                  {isMerchantOwner && !review.merchantReply && (
                    <Box sx={{ mt: 2 }}>
                      {replyingReviewId === review.id ? (
                        <Box sx={{ mt: 1 }}>
                          <TextField
                            fullWidth
                            size="small"
                            multiline
                            rows={2}
                            placeholder="Nhập phản hồi của quán tới khách hàng..."
                            value={replyText}
                            onChange={(e) => setReplyText(e.target.value)}
                            sx={{ mb: 1 }}
                          />
                          <Stack direction="row" spacing={1}>
                            <Button
                              size="small"
                              variant="contained"
                              disabled={replyLoading}
                              onClick={() => handleSendReply(review.id)}
                            >
                              Gửi phản hồi
                            </Button>
                            <Button
                              size="small"
                              color="inherit"
                              onClick={() => {
                                setReplyingReviewId(null);
                                setReplyText('');
                              }}
                            >
                              Hủy
                            </Button>
                          </Stack>
                        </Box>
                      ) : (
                        <Button
                          size="small"
                          startIcon={<ReplyIcon />}
                          onClick={() => {
                            setReplyingReviewId(review.id);
                            setReplyText('');
                          }}
                        >
                          Trả lời đánh giá này
                        </Button>
                      )}
                    </Box>
                  )}
                </CardContent>
              </Card>
            );
          })}
        </Stack>
      );
    })()}

      {/* Phân trang */}
      {data?.reviews?.totalPages > 1 && (
        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 3 }}>
          <Pagination
            count={data.reviews.totalPages}
            page={page + 1}
            onChange={(_, val) => setPage(val - 1)}
            color="primary"
          />
        </Box>
      )}

      {/* Dialog phóng to ảnh */}
      <Dialog open={Boolean(previewImage)} onClose={() => setPreviewImage(null)} maxWidth="md">
        <Box sx={{ p: 1, bgcolor: '#000', display: 'flex', justifyContent: 'center' }}>
          <img
            src={previewImage}
            alt="Dish Zoom"
            style={{ maxWidth: '100%', maxHeight: '80vh', objectFit: 'contain' }}
          />
        </Box>
      </Dialog>
    </Box>
  );
}
