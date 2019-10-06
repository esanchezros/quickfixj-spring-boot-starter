package io.allune.quickfixj.spring.boot.starter.template;

public class MessageValidationException extends QuickFixJClientException {
    public MessageValidationException(String msg) {
        super(msg);
    }

    public MessageValidationException(String msg, Throwable ex) {
        super(msg, ex);
    }
}
