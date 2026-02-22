package com.authnexus.centralapplication.Helper;

import java.util.UUID;

public class UserHelper {
    public static UUID parseUUID(String uuidString) {

        try {
            return UUID.fromString(uuidString);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID format: " + uuidString, e);
        }

    }
}
