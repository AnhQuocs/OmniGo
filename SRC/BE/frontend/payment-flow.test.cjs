const { test } = require('node:test');
const assert = require('node:assert/strict');
const { create } = require('./payment-flow.js');
const response = data => ({ ok: true, json: async () => data, text: async () => data });
function flow(request) { return create({ request, base: '', identity: () => 42 }); }
test('gateway link is not a confirmed payment', async () => {
    let opened;
    const api = flow(async url => response(url.includes('payment/create') ? { paymentUrl: 'https://pay.example/one' } : 'PENDING'));
    assert.equal(await api.pay({ kind: 'FOOD', id: 1, amount: 100 }, 'MOMO', url => opened = url), false);
    assert.equal(opened, 'https://pay.example/one');
});
test('already paid order never creates another payment', async () => {
    let requests = 0;
    const api = flow(async () => { requests++; return response('SUCCESS'); });
    assert.equal(await api.pay({ kind: 'RIDE', id: 1 }, 'WALLET'), true);
    assert.equal(requests, 1);
});
test('wallet sentinel alone does not confirm payment or open a URL', async () => {
    const api = flow(async url => response(url.includes('payment/create') ? { paymentUrl: 'WALLET_SUCCESS' } : 'PENDING'));
    assert.equal(await api.pay({ kind: 'RIDE', id: 1, amount: 100 }, 'WALLET', () => assert.fail()), false);
});
test('unpaid food and completed online ride block; canceled and completed cash do not', async () => {
    const api = flow(async url => {
        if (url.endsWith('my-orders')) return response({ data: [{ id: 1, status: 'AWAITING_PAYMENT', paymentMethod: 'MOMO' }, { id: 2, status: 'CANCELLED' }] });
        if (url.endsWith('/customer')) return response({ data: [{ bookingId: 3, status: 'COMPLETED', paymentMethod: 'APP' }, { bookingId: 4, status: 'COMPLETED', paymentMethod: 'CASH' }] });
        return response('PENDING');
    });
    assert.deepEqual((await api.outstanding()).map(x => x.id), [1, 3]);
});
test('network and invalid history data fail closed', async () => {
    await assert.rejects(flow(async () => ({ ok: false })).outstanding());
    await assert.rejects(flow(async () => response({ data: null })).outstanding());
    await assert.rejects(flow(async () => ({ ok: false })).paid('FOOD', 1));
});
test('rapid repeated action is rejected and lock releases after failure', async () => {
    const api = flow(() => {});
    let finish;
    const first = api.exclusive('create-order', () => new Promise(resolve => finish = resolve));
    await assert.rejects(api.exclusive('create-order', () => assert.fail()));
    finish(); await first;
    await assert.rejects(api.exclusive('create-order', async () => { throw Error('offline'); }));
    assert.equal(await api.exclusive('create-order', async () => 1), 1);
});
test('cross-tab lock refusal does not send a request', async () => {
    const api = create({ request: () => assert.fail(), base: '', identity: () => 42,
        locks: { request: (key, options, callback) => callback(null) } });
    await assert.rejects(api.pay({ kind: 'FOOD', id: 1 }, 'MOMO'));
});
