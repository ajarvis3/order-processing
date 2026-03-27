package com.example.payments.dto;

public record ReservationEvent(
    Long orderId,
    String sku,
    int quantityAvailable,
    boolean reserved
) {}

