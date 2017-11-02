package com.allune.quickfixj.spring.boot.starter.exception;

/**
 * @author Eduardo Sanchez-Ros
 */
public class QuickFixJBaseException extends RuntimeException {

    public QuickFixJBaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public QuickFixJBaseException(String message) {
        super(message);
    }
}
