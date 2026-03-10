package com.screenbridge.mirror.application;

public final class ValidationException extends Exception {
    private final String title;

    public ValidationException(String title, String message) {
        super(message);
        this.title = title;
    }

    public String title() {
        return title;
    }
}
