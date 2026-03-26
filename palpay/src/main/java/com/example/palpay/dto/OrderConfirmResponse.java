package com.example.palpay.dto;

public record OrderConfirmResponse(
        Long orderId,
        String status
) {}
