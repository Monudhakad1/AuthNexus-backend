package com.authnexus.centralapplication.domains.dto;

public record ErrorResponse(
        String message,
        int status
) {
}
