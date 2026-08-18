package com.burgosfacundo.inventory.warehouse.integration.geoapify;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration(proxyBeanMethods = false)
@Profile("!demo")
@EnableConfigurationProperties(GeoapifyProperties.class)
public class GeoapifyConfiguration {
}