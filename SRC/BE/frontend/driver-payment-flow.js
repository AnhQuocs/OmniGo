// Persist only references, never a local assertion that money has been paid.
const driverPayments = DemoPaymentFlow.create({ request: (...args) => fetchWithAuth(...args), base: API_BASE_URL, identity: () => DRIVER_ID });
let checkingDriverPayments = false;
const pendingDriverPanel = document.createElement('aside');
pendingDriverPanel.style.cssText = 'position:sticky;top:0;z-index:2000;padding:12px;background:#fff3cd;display:none';
document.body.prepend(pendingDriverPanel);
function driverPendingKey() { return `omni:driver:${DRIVER_ID}:pending-payments`; }
function pendingDriverIds() {
    try {
        const ids = JSON.parse(localStorage.getItem(driverPendingKey()) || '[]');
        return Array.isArray(ids) ? ids.filter(id => /^\d+$/.test(String(id))) : [];
    } catch { return []; }
}
function watchDriverPayment(id) {
    const ids = [...new Set([...pendingDriverIds(), String(id)])];
    try {
        localStorage.setItem(driverPendingKey(), JSON.stringify(ids));
    } catch {
        pendingDriverPanel.style.display = 'block';
        pendingDriverPanel.textContent = `Chuyến #${id} đang chờ thanh toán. Không lưu được trạng thái trên trình duyệt; hãy kiểm tra lịch sử trước khi đóng trang.`;
        return;
    }
    checkDriverPayments();
}
async function checkDriverPayments() {
    if (checkingDriverPayments) return;
    checkingDriverPayments = true;
    try {
        const confirmed = [];
        for (const id of pendingDriverIds()) {
            if (await driverPayments.paid('RIDE', id)) confirmed.push(String(id));
        }
        // Merge against current storage so another tab's newly completed trip is retained.
        const remaining = pendingDriverIds().filter(id => !confirmed.includes(String(id)));
        localStorage.setItem(driverPendingKey(), JSON.stringify(remaining));
        pendingDriverPanel.style.display = remaining.length || confirmed.length ? 'block' : 'none';
        pendingDriverPanel.textContent = remaining.length
            ? `Chuyến #${remaining.join(', #')}: chưa xác nhận khách thanh toán. Đóng cửa sổ hoặc hết thời gian chờ không có nghĩa đã trả tiền.`
            : confirmed.length ? `Chuyến #${confirmed.join(', #')}: khách đã thanh toán. Hệ thống đang cập nhật quyết toán vào ví.` : '';
        if (confirmed.length) { fetchWallet(); fetchTransactions(); }
    } catch {
        pendingDriverPanel.style.display = 'block';
        pendingDriverPanel.textContent = 'Chưa kiểm tra được thanh toán. Giữ trạng thái chờ và tự kiểm tra lại khi có kết nối.';
    } finally { checkingDriverPayments = false; }
}
window.addEventListener('load', checkDriverPayments);
window.addEventListener('focus', checkDriverPayments);
setInterval(() => { if (!document.hidden) checkDriverPayments(); }, 15000);
