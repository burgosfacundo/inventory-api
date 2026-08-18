package com.burgosfacundo.inventory;

import com.burgosfacundo.inventory.config.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class InventoryApiApplicationTests extends IntegrationTest {

	@Test
	void contextLoads() {
	}
}