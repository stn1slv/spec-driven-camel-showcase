package com.example.mapping;

/**
 * Representation of the base price domain model.
 */
public record BasePrice(
    int productId,
    Double basePrice,
    String currency
) {
}
