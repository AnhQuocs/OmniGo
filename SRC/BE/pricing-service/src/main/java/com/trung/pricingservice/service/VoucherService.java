package com.trung.pricingservice.service;

import com.trung.pricingservice.dto.request.ApplyVoucherRequest;
import com.trung.pricingservice.dto.request.CommitVoucherRequest;
import com.trung.pricingservice.dto.request.CreateVoucherRequest;
import com.trung.pricingservice.dto.response.ApplyVoucherResponse;
import com.trung.pricingservice.dto.response.VoucherResponse;
import com.trung.pricingservice.entity.VoucherUsage;

import java.util.List;

public interface VoucherService {

    VoucherResponse createVoucher(CreateVoucherRequest request);

    VoucherResponse updateVoucher(String code, CreateVoucherRequest request);

    List<VoucherResponse> getAllVouchers();

    VoucherResponse getVoucherByCode(String code);

    VoucherResponse toggleStatus(String code);

    void deleteVoucher(String code);

    ApplyVoucherResponse applyVoucher(ApplyVoucherRequest request);

    ApplyVoucherResponse commitVoucher(CommitVoucherRequest request);

    List<VoucherResponse> getApplicableVouchers(double orderAmount, String serviceType, String userId);

    List<VoucherResponse> getActiveVouchersForCustomer(String serviceType, String userId);

    void rollbackVoucherUsage(String code, String userId);

    void rollbackVoucherUsage(String code, String userId, String orderId);

    List<VoucherUsage> getVoucherUsages(String code);

    List<VoucherUsage> getUserVoucherUsages(String userId);
}
