package com.screenbridge.mirror.application;

/**
 * 表示面向用户的参数校验异常，并携带弹窗标题。
 */
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
