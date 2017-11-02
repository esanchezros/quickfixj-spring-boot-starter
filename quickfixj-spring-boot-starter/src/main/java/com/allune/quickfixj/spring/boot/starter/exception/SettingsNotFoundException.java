package com.allune.quickfixj.spring.boot.starter.exception;

/**
 * @author Eduardo Sanchez-Ros
 */
public class SettingsNotFoundException extends QuickFixJBaseException {
    public SettingsNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public SettingsNotFoundException(String message) {
        super(message);
    }
}
