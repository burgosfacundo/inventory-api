package com.burgosfacundo.inventory.config;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

@Testcontainers
public abstract class IntegrationTest {

    @Container
    @ServiceConnection
    protected static final MySQLContainer mysql =
            new MySQLContainer("mysql:8.4");
}
