package com.example.mapping;

import java.net.URI;

/**
 * RFC 9457 Problem Details for HTTP APIs standard representation.
 */
public record ProblemDetail(
        URI type,
        String title,
        int status,
        String detail,
        URI instance
) {
}
