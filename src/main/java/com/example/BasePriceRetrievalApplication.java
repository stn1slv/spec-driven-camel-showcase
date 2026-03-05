package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Main entry point for the Base Price Retrieval application.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class BasePriceRetrievalApplication {

    /**
     * Application main method.
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(BasePriceRetrievalApplication.class, args);
    }
}
