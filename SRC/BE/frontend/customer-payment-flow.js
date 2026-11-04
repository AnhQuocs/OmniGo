const customerPayments = DemoPaymentFlow.create({
    request: (...args) => fetchWithAuth(...args), base: API_BASE_URL, identity: () => customerId
});
let checkingOutstanding = null;
const debtPanel = document.createElement('section');
debtPanel.style.cssText = 'position:sticky;top:0;z-index:2000;padding:12px;background:#fff3cd;color:#664d03;border:1px solid #ffecb5;display:none';
document.body.prepend(debtPanel);

function refreshOutstanding() {
    if (!customerId) return Promise.resolve([]);
    if (checkingOutstanding) return checkingOutstanding;
    checkingOutstanding = customerPayments.outstanding().then(orders => {
        debtPanel.replaceChildren();
        debtPanel.style.display = orders.length ? 'block' : 'none';
        const pending = requestIntents.pendingCreation('CUSTOMER:' + customerId);
        if (pending) {
            debtPanel.style.display = 'block';
            const retry = document.createElement('button');
            retry.textContent = 'Kiểm tra / gửi lại yêu cầu đặt đơn chưa rõ kết quả';
            retry.onclick = async () => {
                retry.disabled = true;
                try {
                    await customerPayments.exclusive('create-order', async () => {
                        const res = await fetchWithAuth(API_BASE_URL + pending.path, {
                            method: 'POST', headers: { 'Content-Type': 'application/json' }, body: pending.body
                        });
                        const data = await res.json().catch(() => ({}));
                        if (!res.ok) throw new Error(data.message || data.detail || 'Chưa khôi phục được đơn. Hãy thử lại cùng yêu cầu.');
                        const order = data.data || data;
                        if (pending.path === '/api/v1/bookings') {
                            currentBookingId = order.bookingId || order.id;
                            handleBookingEvent({ ...order, bookingId: currentBookingId });
                        } else { fetchMyFoodOrders(); }
                        alert(`Đã khôi phục đơn #${order.bookingId || order.id}. Không tạo thêm đơn trùng.`);
                    });
                } catch (error) { alert(error.message); }
                finally { retry.disabled = false; refreshOutstanding().catch(() => {}); }
            };
            debtPanel.append(retry);
        }
        if (orders.length) {
            const title = document.createElement('strong');
            title.textContent = 'Bạn cần hoàn tất đơn và thanh toán trước khi đặt đơn mới.';
            debtPanel.append(title);
        }
        for (const order of orders) {
            const row = document.createElement('div');
            row.textContent = `${order.kind === 'FOOD' ? 'Đồ ăn' : 'Chuyến xe'} #${order.id}: ${Number(order.amount || 0).toLocaleString('vi-VN')} đ — ${order.method === 'CASH' ? 'Tiền mặt' : (order.method || 'Chưa rõ phương thức')} — ${order.status} — ${order.payable ? 'Chưa xác nhận thanh toán' : 'Đang chờ hoàn tất đơn / thu tiền'}. `;
            if (order.payable) {
                const methods = ['WALLET'];
                for (const method of methods) {
                    const button = document.createElement('button');
                    button.textContent = 'Thanh toán bằng ví OmniPay';
                    button.onclick = async () => {
                        button.disabled = true;
                        try { await payExistingOrder(order, method); } finally { button.disabled = false; }
                    };
                    row.append(button);
                }
            }
            debtPanel.append(row);
        }
        return orders;
    }).catch(error => {
        debtPanel.style.display = 'block';
        debtPanel.textContent = `${error.message} Việc đặt đơn mới tạm khóa cho đến khi kiểm tra được trạng thái.`;
        throw error;
    }).finally(() => { checkingOutstanding = null; });
    return checkingOutstanding;
}

async function guardedOrder(action) {
    try {
        if (!customerId) throw new Error('Vui lòng đăng nhập trước khi đặt đơn.');
        await customerPayments.exclusive('create-order', async () => {
            if ((await refreshOutstanding()).length) throw new Error('Vui lòng hoàn tất thanh toán đơn cũ ở thông báo phía trên trước khi đặt đơn mới.');
            await action();
            await refreshOutstanding();
        });
    } catch (error) { alert(error.message); }
}
function bookRide() { return guardedOrder(bookRideUnchecked); }
function submitFoodOrder() { return guardedOrder(submitFoodOrderUnchecked); }

async function payExistingOrder(order, method) {
    method = 'WALLET';
    try {
        const confirmed = await customerPayments.pay(order, method, () => {
            throw new Error('Đơn đang có giao dịch qua cổng thanh toán cần được xác minh trước khi trả bằng ví OmniPay.');
        });
        if (confirmed) {
            alert('Đã xác nhận thanh toán thành công. Hệ thống đang cập nhật đơn và ví.');
            fetchCustomerWallet(true);
            if (order.kind === 'FOOD') fetchMyFoodOrders();
            if (order.kind === 'RIDE' && String(currentBookingId) === String(order.id)) {
                clearInterval(paymentCheckInterval);
                document.getElementById('trip-card').style.display = 'none';
                currentBookingId = null;
                assignedDriverId = null;
            }
        } else {
            alert('Chưa xác nhận thanh toán bằng ví OmniPay. Vui lòng kiểm tra số dư và trạng thái giao dịch trước khi thử lại.');
        }
        await refreshOutstanding();
    } catch (error) {
        alert(`${error.message}\nKhông tạo lại đơn hoặc đổi phương thức khi giao dịch đang xử lý. Hệ thống sẽ kiểm tra lại trạng thái.`);
    }
}
window.addEventListener('load', () => refreshOutstanding().catch(() => {}));
window.addEventListener('focus', () => refreshOutstanding().catch(() => {}));
setInterval(() => { if (!document.hidden) refreshOutstanding().catch(() => {}); }, 15000);
