package com.example.inventory.service;

import com.example.inventory.model.InventoryItem;
import com.example.inventory.repository.InventoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private KafkaTemplate<String, InventoryService.ReservationEvent> kafkaTemplate;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void reserveWhenEnoughQuantityDecrementsAndPublishesReservedTrue() {
        InventoryItem item = new InventoryItem();
        item.setId(1L);
        item.setSku("SKU-1");
        item.setQuantityAvailable(10);

        when(inventoryRepository.findBySku(eq("SKU-1"))).thenReturn(Optional.of(item));

        inventoryService.reserve("SKU-1", 3);

        // verify save called with decremented quantity
        ArgumentCaptor<InventoryItem> saveCaptor = ArgumentCaptor.forClass(InventoryItem.class);
        verify(inventoryRepository).save(saveCaptor.capture());
        assertEquals(7, saveCaptor.getValue().getQuantityAvailable());

        // verify kafka published with reserved = true
        ArgumentCaptor<InventoryService.ReservationEvent> eventCaptor = ArgumentCaptor.forClass(InventoryService.ReservationEvent.class);
        verify(kafkaTemplate).send(eq("inventory-reservation-result"), eq("SKU-1"), eventCaptor.capture());

        Object evt = eventCaptor.getValue();
        try {
            Class<?> c = evt.getClass();
            Object reserved = c.getField("reserved").get(evt);
            Object qty = c.getField("quantityAvailable").get(evt);
            Object sku = c.getField("sku").get(evt);
            assertEquals("SKU-1", sku);
            assertEquals(7, ((Number) qty).intValue());
            assertEquals(true, reserved);
        } catch (ReflectiveOperationException ex) {
            fail("Failed to inspect reservation event: " + ex.getMessage());
        }
    }

    @Test
    void reserveWhenNotEnoughPublishesReservedFalseAndDoesNotSave() {
        InventoryItem item = new InventoryItem();
        item.setId(2L);
        item.setSku("SKU-2");
        item.setQuantityAvailable(1);

        when(inventoryRepository.findBySku(eq("SKU-2"))).thenReturn(Optional.of(item));

        inventoryService.reserve("SKU-2", 5);

        // should not save because not enough quantity
        verify(inventoryRepository, never()).save(any(InventoryItem.class));

        ArgumentCaptor<InventoryService.ReservationEvent> eventCaptor = ArgumentCaptor.forClass(InventoryService.ReservationEvent.class);
        verify(kafkaTemplate).send(eq("inventory-reservation-result"), eq("SKU-2"), eventCaptor.capture());

        Object evt = eventCaptor.getValue();
        try {
            Class<?> c = evt.getClass();
            Object reserved = c.getField("reserved").get(evt);
            Object qty = c.getField("quantityAvailable").get(evt);
            assertEquals("SKU-2", c.getField("sku").get(evt));
            assertEquals(1, ((Number) qty).intValue());
            assertEquals(false, reserved);
        } catch (ReflectiveOperationException ex) {
            fail("Failed to inspect reservation event: " + ex.getMessage());
        }
    }

    @Test
    void reserveWhenNotFoundPublishesZeroAndFalse() {
        when(inventoryRepository.findBySku(eq("MISSING"))).thenReturn(Optional.empty());

        inventoryService.reserve("MISSING", 2);

        // should not save
        verify(inventoryRepository, never()).save(any());

        ArgumentCaptor<InventoryService.ReservationEvent> eventCaptor = ArgumentCaptor.forClass(InventoryService.ReservationEvent.class);
        verify(kafkaTemplate).send(eq("inventory-reservation-result"), eq("MISSING"), eventCaptor.capture());

        Object evt = eventCaptor.getValue();
        try {
            Class<?> c = evt.getClass();
            assertEquals("MISSING", c.getField("sku").get(evt));
            assertEquals(0, ((Number) c.getField("quantityAvailable").get(evt)).intValue());
            assertEquals(false, c.getField("reserved").get(evt));
        } catch (ReflectiveOperationException ex) {
            fail("Failed to inspect reservation event: " + ex.getMessage());
        }
    }
}

