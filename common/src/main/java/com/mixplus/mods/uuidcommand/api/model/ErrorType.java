package com.mixplus.mods.uuidcommand.api.model;

public enum ErrorType {
    TIMEOUT("HTTP request timed out"),
    UNKNOWN_HOST("Failed to resolve host"),
    IO_ERROR("HTTP request failed");

    private final String message;

    ErrorType(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }
}
