package com.authnexus.centralapplication.domains.dto;

public record LoginRequest(
        String email,
        String password
) {

}
