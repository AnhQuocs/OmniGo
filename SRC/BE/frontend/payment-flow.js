/* Shared demo payment contract: only the authenticated status endpoint confirms capture. */
(function (root) {
    function create({ request, base, identity, locks = globalThis.navigator?.locks }) {
        const busy = new Set();
        async function exclusive(key, action) {
            key = `omni:${identity()}:${key}`;
            if (busy.has(key)) throw new Error('Yêu cầu đang xử lý. Vui lòng đợi và kiểm tra trạng thái.');
            busy.add(key);
            try {
                if (locks) return await locks.request(key, { ifAvailable: true }, lock => {
                    if (!lock) throw new Error('Yêu cầu đang xử lý ở tab khác.');
                    return action();
                });
                return await action();
            } finally { busy.delete(key); }
        }
        async function paid(kind, id) {
            const res = await request(`${base}/api/v1/payments/${kind === 'FOOD' ? 'food-order' : 'booking'}/${id}/payment-status`);
            if (!res.ok) throw new Error('Chưa kiểm tra được thanh toán. Vui lòng thử lại.');
            return (await res.text()).trim() === 'SUCCESS';
        }
        async function list(path) {
            const res = await request(base + path);
            if (!res.ok) throw new Error('Không tải được đơn cũ; chưa thể đặt đơn mới.');
            const data = await res.json();
            const rows = Array.isArray(data) ? data : data.data;
            if (!Array.isArray(rows)) throw new Error('Dữ liệu đơn hàng chưa đầy đủ; vui lòng thử lại.');
            return rows;
        }
        async function outstanding() {
            const [food, rides] = await Promise.all([list('/api/v1/food-orders/my-orders'), list('/api/v1/bookings/customer')]);
            const result = [];
            for (const [kind, rows] of [['FOOD', food], ['RIDE', rides]]) {
                for (const row of rows) {
                    if (['CANCELLED', 'CANCELED', 'REJECTED'].includes(row.status)) continue;
                    const id = row.id ?? row.bookingId;
                    const method = String(row.paymentMethod || '').toUpperCase();
                    const cash = method === 'CASH';
                    // Cash is collected on completion. Until then it is still an outstanding order.
                    if (cash && row.status === 'COMPLETED') continue;
                    if (!cash && await paid(kind, id)) continue;
                    result.push({ kind, id, amount: kind === 'FOOD' ? row.totalPrice : row.price,
                        method, status: row.status,
                        payable: !cash && (kind === 'FOOD' ? row.status === 'AWAITING_PAYMENT' : row.status === 'COMPLETED') });
                }
            }
            return result;
        }
        async function pay(order, method, openGateway) {
            return exclusive(`pay:${order.kind}:${order.id}`, async () => {
                if (await paid(order.kind, order.id)) return true;
                const query = new URLSearchParams({ method, type: order.kind === 'FOOD' ? 'FOOD_PAYMENT' : 'TRIP_PAYMENT', bookingId: order.id, amount: order.amount });
                const res = await request(`${base}/api/v1/payments/payment/create?${query}`, { method: 'POST' });
                const data = await res.json().catch(() => ({}));
                if (!res.ok) throw new Error(data.message || 'Thanh toán chưa được xác nhận. Hãy kiểm tra lại đơn cũ.');
                if (await paid(order.kind, order.id)) return true;
                if (data.paymentUrl && !['WALLET_SUCCESS', 'ALREADY_PAID'].includes(data.paymentUrl)) {
                    const url = new URL(data.paymentUrl);
                    if (url.protocol !== 'https:') throw new Error('Địa chỉ cổng thanh toán không hợp lệ.');
                    openGateway(url.href);
                }
                return false;
            });
        }
        return { exclusive, paid, outstanding, pay };
    }
    root.DemoPaymentFlow = { create };
    if (typeof module !== 'undefined') module.exports = { create };
})(globalThis);
