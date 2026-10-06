package com.parkezz.payment.service;

import com.parkezz.payment.dto.PaymentRequest;
import com.parkezz.payment.dto.PaymentResponse;

public interface PaymentService {
    PaymentResponse pay(PaymentRequest request, Long userId, String authorizationHeader);
    PaymentResponse getById(Long id, Long userId);
}
