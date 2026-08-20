package com.burgosfacundo.inventory.config;

import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
public abstract class IntegrationTest {
}