package com.backend.skillswap.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum Role {
    ADMIN,
    USER,
    PROVIDER;

    // Converts incoming JSON role to enum (case-insensitive) yani JSON → Enum (case-insensitive)
    @JsonCreator
    public static Role fromValue(String value) {
        return Arrays.stream(values())
                .filter(role -> role.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Invalid role. Allowed values are: ADMIN, USER, PROVIDER"
                        )
                );
    }

    // Sends enum value as string in response (Enum → JSON response)
    @JsonValue
    public String toValue() {
        return this.name();
    }

}
