package com.example.palpay.dto;

public record OrderCreateResponse(
        Long orderId,
        String status
) {}