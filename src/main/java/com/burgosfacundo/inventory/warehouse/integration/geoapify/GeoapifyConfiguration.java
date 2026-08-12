package com.burgosfacundo.inventory.warehouse.integration.geoapify;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(GeoapifyProperties.class)
public class GeoapifyConfiguration {
}