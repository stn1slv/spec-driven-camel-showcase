package com.example.api;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Application configuration properties.
 */
@ConfigurationProperties(prefix = "app.fakestore")
public record AppConfig(String baseUrl) {
}
