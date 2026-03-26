package com.example.inventory;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = InventoryApplicationTests.class)
@ImportAutoConfiguration(JacksonAutoConfiguration.class)
class InventoryApplicationTests {

	@Test
	void contextLoads() {
	}

}
