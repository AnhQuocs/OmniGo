const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');
const html = fs.readFileSync(__dirname + '/customer-app.html', 'utf8');
const source = html.slice(html.indexOf('    async function applyVoucherCode('), html.indexOf('    function logSystem('));
for (const [kind, amount, discount] of [['FOOD_DELIVERY',144000,11520],['RIDE_HAILING',74000,5920]]) {
    test(kind + ' previews the same gross amount as the server', async () => {
        let sent;
        const ctx = vm.createContext({ currentActiveServiceForVoucher:kind, currentTripBasePrice:74000,
            cart:[{price:65000,quantity:1}],currentFoodDeliveryFee:79000,customerId:42,API_BASE_URL:'',
            appliedRideVoucher:null,appliedFoodVoucher:null,
            document:{getElementById:()=>null},alert(){},closeModal(){},updateRidePricingDisplay(){},renderCartModalItems(){},
            fetchWithAuth:async (url, options)=>{ sent=JSON.parse(options.body);return {ok:true,json:async()=>({success:true,data:{valid:true,discountAmount:discount,voucher:{code:'OMNI8'}}})}; }
        });
        vm.runInContext(source,ctx);
        await ctx.applyVoucherCode('OMNI8');
        assert.equal(sent.orderAmount,amount);
        const applied=kind==='FOOD_DELIVERY'?ctx.appliedFoodVoucher:ctx.appliedRideVoucher;
        assert.equal(applied.originalAmount,amount);
        assert.equal(applied.discountAmount,discount);
    });
}
