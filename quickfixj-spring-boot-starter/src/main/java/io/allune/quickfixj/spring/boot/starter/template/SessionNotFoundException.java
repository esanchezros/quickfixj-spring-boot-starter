package io.allune.quickfixj.spring.boot.starter.template;

import java.io.IOException;

/**
 * Exception thrown when an I/O error occurs.
 *
 * @author Arjen Poutsma
 * @since 3.0
 */
public class SessionNotFoundException extends QuickFixJClientException {

    /**
     * Construct a new {@code HttpIOException} with the given message.
     *
     * @param msg the message
     */
    public SessionNotFoundException(String msg) {
        super(msg);
    }

    /**
     * Construct a new {@code HttpIOException} with the given message and {@link IOException}.
     *
     * @param msg the message
     * @param ex  the {@code IOException}
     */
    public SessionNotFoundException(String msg, Throwable ex) {
        super(msg, ex);
    }
}
