package com.trung.paymentservice.service;

import com.trung.paymentservice.entity.Wallet;
import com.trung.paymentservice.event.BookingCompletedEvent;
import com.trung.paymentservice.util.enums.UserType;
import com.trung.paymentservice.dto.request.RefundRequest;

import java.math.BigDecimal;

import com.trung.paymentservice.dto.request.FoodOrderPayoutRequest;

public interface WalletService {
    Wallet getOrCreateWallet(Long userId, UserType userType);
    void deductCommission(BookingCompletedEvent event);
    Wallet creditWallet(Long userId, UserType userType, Double amount);
    Wallet depositWallet(Long userId, UserType userType, Double amount, String note);
    void withdrawWallet(Long driverId, Double amount);
    void withdrawWallet(Long userId, UserType userType, Double amount);
    void cancelPendingTransactionsByBookingId(Long bookingId);
    void processFoodOrderPayout(FoodOrderPayoutRequest request);
    void processRefund(RefundRequest request);
}

