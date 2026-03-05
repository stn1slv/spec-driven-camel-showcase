package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Main application class.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class BasePriceRetrievalApplication {

  /**
   * Main entry point.
   *
   * @param args Command line arguments.
   */
  public static void main(String[] args) {
    SpringApplication.run(BasePriceRetrievalApplication.class, args);
  }
}
