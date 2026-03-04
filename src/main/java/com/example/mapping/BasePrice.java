package com.example.mapping;

public record BasePrice(
    int productId,
    Double basePrice,
    String currency
) {
}
