package io.allune.quickfixj.spring.boot.starter.template;

import org.springframework.core.NestedRuntimeException;

/**
 * Base class for exceptions thrown by {@link RestTemplate} whenever it encounters
 * client-side HTTP errors.
 *
 * @author Eduardo Sanchez-Ros
 */
public class QuickFixJClientException extends NestedRuntimeException {

    private static final long serialVersionUID = -4084444984163796577L;


    /**
     * Construct a new instance of {@code HttpClientException} with the given message.
     *
     * @param msg the message
     */
    public QuickFixJClientException(String msg) {
        super(msg);
    }

    /**
     * Construct a new instance of {@code HttpClientException} with the given message and
     * exception.
     *
     * @param msg the message
     * @param ex  the exception
     */
    public QuickFixJClientException(String msg, Throwable ex) {
        super(msg, ex);
    }

}