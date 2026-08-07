package com.burgosfacundo.inventory.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String apiBasePath;

    public WebConfig(@Value("${api.base-path}") String apiBasePath) {
        this.apiBasePath = apiBasePath;
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix(
                apiBasePath,
                HandlerTypePredicate.forAnnotation(RestController.class)
        );
    }
}