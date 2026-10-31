package com.trung.userdriverservice.service;

import com.trung.userdriverservice.dto.request.OtpSendRequest;
import com.trung.userdriverservice.dto.request.OtpVerifyRequest;
import com.trung.userdriverservice.dto.response.OtpSendResponse;
import com.trung.userdriverservice.dto.response.OtpVerifyResponse;
import com.trung.userdriverservice.exception.BadRequestException;
import com.trung.userdriverservice.exception.ResourceNotFoundException;

public interface OtpService {
    OtpSendResponse sendOtp(OtpSendRequest request) throws BadRequestException, ResourceNotFoundException;
    OtpVerifyResponse verifyOtp(OtpVerifyRequest request) throws BadRequestException;
}

