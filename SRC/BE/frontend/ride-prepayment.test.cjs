const {test}=require('node:test');
const assert=require('node:assert/strict');
const fs=require('node:fs');
const vm=require('node:vm');
const html=fs.readFileSync(__dirname+'/driver-app.html','utf8');
const source=html.slice(html.indexOf('    function ridePaymentLabel('),html.indexOf('    function closeMatchBoxAndReset('));
function setup(booking) {
    const elements=new Map(); const requests=[];
    const ctx=vm.createContext({currentTripBooking:null,currentBookingId:null,currentFoodOrderId:null,currentTripPrice:0,isOnTrip:false,
        API_BASE_URL:'',DRIVER_ID:11,getStoredToken:()=> 'token',window:{addEventListener(){}},
        document:{getElementById:id=>{if(!elements.has(id)) elements.set(id,{style:{},innerHTML:'',textContent:''});return elements.get(id);}},
        fetchWithAuth:async (url)=>{requests.push(url);return {ok:true,json:async()=>url.includes('/complete') ? {...booking,status:'COMPLETED'}:{data:[booking]}};},
        confirm:()=>true,alert(){},setTimeout(){},clearInterval(){},fetchWallet(){},fetchTransactions(){},console,
        watchDriverPayment(){assert.fail('Prepaid trip must not wait for a new payment');}});
    vm.runInContext(source,ctx);return {ctx,elements,requests};
}
test('restores in-progress prepaid ride with destination button and no payment chooser',async()=>{
    const {ctx,elements,requests}=setup({id:99,status:'IN_PROGRESS',paymentMethod:'WALLET',prepaid:true,paid:true});
    await ctx.restoreActiveRide();
    assert.equal(ctx.currentBookingId,99);
    assert.match(elements.get('action-buttons').innerHTML,/ĐÃ ĐẾN NƠI/);
    assert.match(elements.get('action-buttons').innerHTML,/không thu tiền/);
    assert.doesNotMatch(elements.get('action-buttons').innerHTML,/finishTrip\('(?:CASH|APP)'/);
    ctx.finishTrip(); await new Promise(resolve=>setImmediate(resolve));
    assert.ok(requests.some(url=>url.includes('/99/complete?paymentMethod=WALLET')));
    assert.match(elements.get('action-buttons').innerHTML,/đã trả trước/);
});
test('cash trip explicitly tells driver to collect cash',async()=>{
    const {ctx,elements}=setup({id:35,status:'IN_PROGRESS',paymentMethod:'CASH'});
    await ctx.restoreActiveRide();
    assert.match(elements.get('action-buttons').innerHTML,/Tiền mặt/);
    assert.doesNotMatch(elements.get('action-buttons').innerHTML,/Đã thanh toán trước/);
});
test('accepted and arrived trips restore their next actions',async()=>{
    for(const [status,label] of [['ACCEPTED','ĐÃ ĐẾN ĐIỂM ĐÓN'],['ARRIVED','BẮT ĐẦU HÀNH TRÌNH']]) {
        const {ctx,elements}=setup({id:99,status,paymentMethod:'CASH'});
        await ctx.restoreActiveRide();assert.ok(elements.get('action-buttons').innerHTML.includes(label));
    }
});
