package com.authnexus.centralapplication.domains.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        Long expiresIn,
        String tokenType,
        UserDto user
) {

    public static TokenResponse of(String accessToken, String freshToken, Long expiresIn, UserDto user) {
        return new TokenResponse(accessToken, freshToken, expiresIn, "Bearer ", user);
    }
}
