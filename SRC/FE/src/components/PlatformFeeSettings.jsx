import { useEffect, useState } from 'react';
import { Card, Typography, TextField, Button, Alert, Stack } from '@mui/material';
import paymentService from '../services/paymentService';

export default function PlatformFeeSettings() {
  const [percentage, setPercentage] = useState('');
  const [busy, setBusy] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);
  const load = async () => {
    setBusy(true); setError(''); setSuccess(false);
    try { const data = await paymentService.getPlatformFee(); setPercentage(String(data.percentage)); }
    catch { setError('Không thể tải tỷ lệ sàn. Vui lòng thử lại.'); }
    finally { setBusy(false); }
  };
  useEffect(() => {
    let active = true;
    paymentService.getPlatformFee()
      .then((data) => { if (active) setPercentage(String(data.percentage)); })
      .catch(() => { if (active) setError('Không thể tải tỷ lệ sàn. Vui lòng thử lại.'); })
      .finally(() => { if (active) setBusy(false); });
    return () => { active = false; };
  }, []);
  const valid = percentage.trim() !== '' && Number.isFinite(Number(percentage)) && Number(percentage) >= 0 && Number(percentage) <= 100 && /^\d+(\.\d{1,2})?$/.test(percentage);
  const save = async () => {
    setBusy(true); setError(''); setSuccess(false);
    try { const data = await paymentService.updatePlatformFee(Number(percentage)); setPercentage(String(data.percentage)); setSuccess(true); }
    catch { setError('Không thể lưu tỷ lệ sàn. Vui lòng thử lại.'); }
    finally { setBusy(false); }
  };
  return <Card sx={{ p: 3 }}>
    <Stack spacing={2}>
      <Typography variant="h6">Tỷ lệ sàn</Typography>
      <Typography>Tỷ lệ dùng chung cho tiền cuốc xe, tiền giao đồ ăn của tài xế và tiền món ăn của quán. Phí được tính khi quyết toán; các giao dịch đã quyết toán giữ nguyên.</Typography>
      {error && <Alert severity="error">{error}</Alert>}
      {success && <Alert severity="success">Đã lưu tỷ lệ sàn.</Alert>}
      <TextField label="Tỷ lệ sàn (%)" type="number" value={percentage} disabled={busy}
        onChange={(event) => { setPercentage(event.target.value); setSuccess(false); }}
        slotProps={{ htmlInput: { min: 0, max: 100, step: 0.01 } }} helperText="Từ 0 đến 100%, tối đa 2 chữ số thập phân." />
      <Stack direction="row" spacing={2}>
        <Button variant="contained" disabled={busy || !valid} onClick={save}>Lưu tỷ lệ sàn</Button>
        <Button disabled={busy} onClick={load}>Tải lại</Button>
      </Stack>
    </Stack>
  </Card>;
}
