package com.example.palpay.dto;

public record OrderAuthorizeResponse(
        Long orderId,
        String authorizationId,       // PayPal authorization ID
        String status                 // "AUTHORIZED", "FAILED"
) {}