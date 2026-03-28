package com.example.palpay.dto;

public record OrderCreateRequest(
        Double totalAmount,
        String paymentMethodType   // "palpay", "card", "palpay", etc.
) {}