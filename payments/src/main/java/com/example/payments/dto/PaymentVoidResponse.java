package com.example.payments.dto;

public record PaymentVoidResponse(
        Long orderId,
        String status
) {}

