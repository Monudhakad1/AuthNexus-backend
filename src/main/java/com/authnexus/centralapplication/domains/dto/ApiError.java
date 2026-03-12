package com.authnexus.centralapplication.domains.dto;

public record ApiError(
        int status,
        String error,
        String message,
        String path,
        Object o) {
    public  static ApiError of(int status, String error, String message ,String path) {
        return new ApiError(status, error, message, path, null);
    }

    public static ApiError of(int status, String error, String message, String path,boolean notDateTime)  {
        return new ApiError(status, error, message, path, null);
    }
}
