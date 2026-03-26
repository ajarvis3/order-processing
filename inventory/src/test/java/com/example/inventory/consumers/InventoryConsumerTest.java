package com.example.inventory.consumers;

import com.example.inventory.dto.InventoryRequest;
import com.example.inventory.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InventoryConsumerTest {

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private InventoryConsumer inventoryConsumer;

    @Test
    void consume_delegatesToService() {
        InventoryRequest msg = new InventoryRequest("SKU-10", 4);
        inventoryConsumer.consume(msg);
        verify(inventoryService).reserve("SKU-10", 4);
    }
}

