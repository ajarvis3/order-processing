package com.example.inventory.exceptions;

public class InventoryNotFoundException extends Exception {
    public InventoryNotFoundException(String message) {
        super(message);
    }
}
