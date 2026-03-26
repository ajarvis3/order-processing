package com.example.palpay.dto;

public record PaymentVoidResponse(
        Long orderId,
        String status                 // "VOIDED", "FAILED"
) {}