package com.example.palpay.dto;

public record OrderCreateRequest(
        Double totalAmount,
        String paymentMethodType   // "paypal", "card", "palpay", etc.
) {}