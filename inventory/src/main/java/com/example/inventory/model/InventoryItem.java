package com.example.inventory.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "inventory")
public class InventoryItem {
    @Id
    private Long id;

    private String sku;

    private Integer quantityAvailable;

    public InventoryItem() {
    }

    public InventoryItem(String sku, Integer quantityAvailable) {
        this.sku = sku;
        this.quantityAvailable = quantityAvailable;
    }

    public Integer getQuantityAvailable() {
        return quantityAvailable;
    }
    public void setQuantityAvailable(Integer quantityAvailable) {
        this.quantityAvailable = quantityAvailable;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }
    public void setSku(String sku) {
        this.sku = sku;
    }

}