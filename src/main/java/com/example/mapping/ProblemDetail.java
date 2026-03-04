package com.example.mapping;

import java.net.URI;

public record ProblemDetail(
        URI type,
        String title,
        int status,
        String detail,
        URI instance
) {
}
