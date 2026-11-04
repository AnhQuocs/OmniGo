package com.trung.paymentservice.service;
import com.trung.paymentservice.service.impl.*;
import com.trung.paymentservice.config.*;
import com.trung.paymentservice.repository.*;
import com.trung.paymentservice.entity.Transaction;
import com.trung.paymentservice.dto.request.MomoIpnRequest;
import com.trung.paymentservice.util.enums.*;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class ProviderNotificationTest {
    private final PaymentWorkflowService workflow=mock(PaymentWorkflowService.class);
    private final TransactionRepository txs=mock(TransactionRepository.class);
    private final VnpayConfig vnpConfig=new VnpayConfig();
    private final VnpayEncoder encoder=new VnpayEncoder();
    private VnpayService vnp() {
        vnpConfig.setTmnCode("TEST"); vnpConfig.setHashSecret("test-secret");
        return new VnpayService(workflow,vnpConfig,encoder,txs,mock(WalletService.class),mock(WalletRepository.class),mock(RestTemplate.class));
    }
    private Map<String,String> params() {
        Map<String,String> p=new TreeMap<>(); p.put("vnp_TmnCode","TEST"); p.put("vnp_TxnRef","ref"); p.put("vnp_Amount","12000000");
        p.put("vnp_ResponseCode","00"); p.put("vnp_TransactionStatus","00"); p.put("vnp_TransactionNo","receipt"); return p;
    }
    private void sign(Map<String,String> p) {
        p.remove("vnp_SecureHash");
        String url=encoder.getPaymentUrl(p,"test-secret","https://test.invalid");
        p.put("vnp_SecureHash",url.substring(url.lastIndexOf("vnp_SecureHash=")+15));
    }
    @Test void forgedVnpayNotificationCannotMutateMoney() {
        var service=vnp(); var p=params(); p.put("vnp_SecureHash","fake");
        assertEquals("97",service.processIpn(p).get("RspCode")); verifyNoInteractions(workflow);
    }
    @Test void validSignatureStillRequiresBothSuccessCodesAndAmountInDong() {
        var service=vnp(); var p=params(); sign(p);
        when(workflow.accept("ref",PaymentMethod.VNPAY,new BigDecimal("120000"),"receipt",true)).thenReturn(true);
        assertEquals("00",service.processIpn(p).get("RspCode"));
        p.put("vnp_TransactionStatus","02"); sign(p); service.processIpn(p);
        verify(workflow).accept("ref",PaymentMethod.VNPAY,new BigDecimal("120000"),"receipt",false);
    }
    @Test void tamperedAmountInvalidatesSignature() {
        var service=vnp(); var p=params(); sign(p); p.put("vnp_Amount","100");
        assertEquals("97",service.processIpn(p).get("RspCode")); verifyNoInteractions(workflow);
    }
    @Test void momoRejectsMissingOrWrongMerchantSignature() {
        var config=new MomoConfig();config.setPartnerCode("TEST");config.setSecretKey("secret");config.setAccessKey("access");
        var service=new MomoService(mock(PendingPaymentStore.class),workflow,config,new MomoEncoder(),txs,mock(WalletRepository.class),mock(WalletService.class),mock(RestTemplate.class));
        var req=new MomoIpnRequest();req.setOrderId("ref");req.setPartnerCode("TEST");req.setSignature("fake");
        assertThrows(ResponseStatusException.class,()->service.processMomoIpn(req)); verifyNoInteractions(workflow,txs);
    }
    @Test void validMomoNotificationUsesPersistedRequestIdAndPassesVerifiedAmount() throws Exception {
        var config=new MomoConfig();config.setPartnerCode("TEST");config.setSecretKey("secret");config.setAccessKey("access");
        var service=new MomoService(mock(PendingPaymentStore.class),workflow,config,new MomoEncoder(),txs,mock(WalletRepository.class),mock(WalletService.class),mock(RestTemplate.class));
        var req=new MomoIpnRequest(); req.setOrderId("ref");req.setPartnerCode("TEST");req.setRequestId("request");req.setAmount(120000L);
        req.setExtraData("");req.setMessage("OK");req.setOrderInfo("Food");req.setOrderType("momo_wallet");req.setPayType("qr");
        req.setResponseTime("123");req.setResultCode(0);req.setTransId("receipt");
        String raw="accessKey=access&amount=120000&extraData=&message=OK&orderId=ref&orderInfo=Food&orderType=momo_wallet&partnerCode=TEST&payType=qr&requestId=request&responseTime=123&resultCode=0&transId=receipt";
        req.setSignature(new MomoEncoder().signHmacSHA256(raw,"secret"));
        Transaction tx=Transaction.builder().requestId("request").build(); when(txs.findByOrderId("ref")).thenReturn(Optional.of(tx));
        service.processMomoIpn(req);verify(workflow).accept("ref",PaymentMethod.MOMO,new BigDecimal("120000"),"receipt",true);
        tx.setRequestId("different");assertThrows(ResponseStatusException.class,()->service.processMomoIpn(req));
    }
}
