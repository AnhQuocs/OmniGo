const { test } = require('node:test');
const assert = require('node:assert/strict');
const { create } = require('./request-idempotency.js');
function setup() {
    const map = new Map(); let count = 0;
    const storage = { getItem: key => map.get(key), setItem: (key, value) => map.set(key, value), removeItem: key => map.delete(key) };
    return { storage, make: () => create(storage, () => `unique-key-${++count}`) };
}
test('lost order response and reload reuse the key and original payload', () => {
    const { make } = setup(); const api = make();
    const first = { method: 'POST', body: '{"address":"A"}' };
    api.prepare('CUSTOMER:1', '/api/v1/bookings', first);
    const afterReload = make(); const again = { method: 'POST', body: first.body };
    afterReload.prepare('CUSTOMER:1', '/api/v1/bookings', again);
    assert.equal(first.headers['Idempotency-Key'], again.headers['Idempotency-Key']);
    assert.equal(afterReload.pendingCreation('CUSTOMER:1').body, first.body);
});
test('changing unresolved order is rejected, confirmed creation releases intent', () => {
    const { make } = setup(); const api = make();
    const intent = api.prepare('CUSTOMER:1', '/api/v1/bookings', { method: 'POST', body: 'first' });
    assert.throws(() => api.prepare('CUSTOMER:1', '/api/v1/food-orders', { method: 'POST', body: 'different' }));
    api.finish(intent, { ok: true, status: 200 });
    assert.equal(api.pendingCreation('CUSTOMER:1'), null);
});
test('gateway response and server timeout retain payment key; verified failure allows new key', () => {
    const { make } = setup(); const api = make();
    const url = '/api/v1/payments/payment/create?type=FOOD_PAYMENT&bookingId=1&method=MOMO&amount=10';
    const first = api.prepare('CUSTOMER:1',url,{ method: 'POST' });
    api.finish(first,{ ok: true,status: 200 });
    assert.equal(api.prepare('CUSTOMER:1',url,{ method: 'POST' }).key,first.key);
    api.finish(first,{ ok: false,status: 503 });
    assert.equal(api.prepare('CUSTOMER:1',url,{ method: 'POST' }).key,first.key);
    api.finish(first,{ ok: false,status: 422 });
    assert.notEqual(api.prepare('CUSTOMER:1',url,{ method: 'POST' }).key,first.key);
});
test('distinct user scopes and new deposits do not reuse completed intents', () => {
    const { make } = setup(); const api = make();
    const url = '/api/v1/payments/payment/create?type=CUSTOMER_DEPOSIT&method=MOMO&amount=10';
    const a = api.prepare('CUSTOMER:1',url,{ method: 'POST' });
    const b = api.prepare('CUSTOMER:2',url,{ method: 'POST' });
    assert.notEqual(a.key,b.key);
    api.finish(a,{ ok: true,status: 200 });
    assert.notEqual(api.prepare('CUSTOMER:1',url,{ method: 'POST' }).key,a.key);
});

test('HTTP 200 with interrupted or invalid body retains the original key', async () => {
    const { make } = setup(); const api = make();
    const url = '/api/v1/payments/payment/create?type=CUSTOMER_DEPOSIT&method=MOMO&amount=10';
    const first = api.prepare('CUSTOMER:1', url, { method: 'POST' });
    await assert.rejects(api.completeResponse(first, { ok: true, status: 200, clone: () => ({ json: async () => { throw Error('connection lost'); } }) }));
    assert.equal(api.prepare('CUSTOMER:1', url, { method: 'POST' }).key, first.key);
    await assert.rejects(api.completeResponse(first, new Response('{}', { status: 200 })));
    assert.equal(api.prepare('CUSTOMER:1', url, { method: 'POST' }).key, first.key);
    await api.completeResponse(first, new Response(JSON.stringify({ orderId: 'ref', paymentUrl: 'https://pay.example' }), { status: 200 }));
    assert.notEqual(api.prepare('CUSTOMER:1', url, { method: 'POST' }).key, first.key);
});
test('complete rejection body releases key only for definitive failure', async () => {
    const { make } = setup(); const api = make();
    const url = '/api/v1/food-orders';
    const first = api.prepare('CUSTOMER:1', url, { method: 'POST', body: '{}' });
    await api.completeResponse(first, new Response('{"message":"busy"}', { status: 409 }));
    assert.equal(api.pendingCreation('CUSTOMER:1').key, first.key);
    await api.completeResponse(first, new Response('{"message":"failed"}', { status: 422 }));
    assert.equal(api.pendingCreation('CUSTOMER:1'), null);
});
