/* Keys survive reloads/timeouts. A response lost in transit must reuse the original intent. */
(function (root) {
    function create(storage, uuid) {
        function prepare(identity, input, options) {
            if ((options.method || 'GET').toUpperCase() !== 'POST') return null;
            const url = new URL(input, 'http://localhost');
            const order = ['/api/v1/bookings', '/api/v1/food-orders'].includes(url.pathname);
            const payment = url.pathname === '/api/v1/payments/payment/create';
            if (!order && !payment) return null;
            url.searchParams.sort();
            const fingerprint = `${url.pathname}${url.search}|${options.body || ''}`;
            const slot = `omni:intent:${identity}:${order ? 'order' : url.pathname + ':' + (url.searchParams.get('type') || '') + ':' + (url.searchParams.get('bookingId') || 'deposit')}`;
            let intent = JSON.parse(storage.getItem(slot) || 'null');
            if (intent && intent.fingerprint !== fingerprint)
                throw new Error('Yêu cầu trước chưa rõ kết quả. Hãy dùng nút kiểm tra / gửi lại yêu cầu phía trên trước khi đặt đơn mới.');
            if (!intent) {
                intent = { key: uuid(), fingerprint };
                storage.setItem(slot, JSON.stringify(intent));
            }
            options.headers = { ...options.headers, 'Idempotency-Key': intent.key };
            return { slot, key: intent.key, order, clearOnSuccess: order || !url.searchParams.get('bookingId') };
        }
        function finish(intent, response) {
            if (!intent) return;
            // Payment keys remain stable while retrying the same order, even after a gateway URL is returned.
            const clear = (intent.clearOnSuccess && response.ok) || [400, 422].includes(response.status);
            if (clear) {
                const stored = JSON.parse(storage.getItem(intent.slot) || 'null');
                if (stored?.key === intent.key) storage.removeItem(intent.slot);
            }
        }
        async function completeResponse(intent, response) {
            if (!intent) return response;
            // Consume the entire body before releasing the key; fetch resolves on headers alone.
            let body;
            try {
                body = await response.clone().json();
            } catch (error) {
                throw new Error('Chưa xác nhận được kết quả từ máy chủ (HTTP ' + response.status + '). Hãy dùng nút kiểm tra / gửi lại yêu cầu; hệ thống giữ nguyên mã yêu cầu để tránh tạo đơn trùng.');
            }
            if (response.ok) {
                const result = body?.data || body || {};
                const valid = intent.order ? (result.id || result.bookingId) : (result.orderId && result.paymentUrl);
                if (!valid || body?.success === false) throw new Error('Phản hồi chưa đầy đủ. Giữ yêu cầu cũ để kiểm tra lại.');
            }
            finish(intent, response);
            return response;
        }
        function pendingCreation(identity) {
            const intent = JSON.parse(storage.getItem(`omni:intent:${identity}:order`) || 'null');
            if (!intent) return null;
            const separator = intent.fingerprint.indexOf('|');
            const path = intent.fingerprint.slice(0, separator);
            if (!['/api/v1/bookings', '/api/v1/food-orders'].includes(path)) return null;
            return { path, body: intent.fingerprint.slice(separator + 1), key: intent.key };
        }
        return { prepare, finish, completeResponse, pendingCreation };
    }
    root.DemoRequestIdempotency = { create };
    if (typeof module !== 'undefined') module.exports = { create };
})(globalThis);
