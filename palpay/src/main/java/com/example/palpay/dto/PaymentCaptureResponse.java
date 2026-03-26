package com.example.palpay.dto;

public record PaymentCaptureResponse(
        Long orderId,
        String status                 // "COMPLETED", "FAILED"
) {}