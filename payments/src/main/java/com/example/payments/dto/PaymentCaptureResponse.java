package com.example.payments.dto;

public record PaymentCaptureResponse(
        Long orderId,
        String status
) {}

