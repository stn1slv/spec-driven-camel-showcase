package com.example.api;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.fakestore")
public record AppConfig(String baseUrl) {
}
