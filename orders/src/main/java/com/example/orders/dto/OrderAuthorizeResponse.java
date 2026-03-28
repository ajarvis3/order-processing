package com.example.orders.dto;

public record OrderAuthorizeResponse(
        Long orderId,
        String authorizationId,       // PalPay authorization ID
        String status                 // "AUTHORIZED", "FAILED"
) {}