package com.example.inventory.dto;

public record InventoryRequest(
   String sku,
    Integer quantity
) {}

