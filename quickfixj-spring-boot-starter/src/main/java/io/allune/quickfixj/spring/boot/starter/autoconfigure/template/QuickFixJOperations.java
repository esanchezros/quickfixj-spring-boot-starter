package io.allune.quickfixj.spring.boot.starter.autoconfigure.template;

import quickfix.Message;
import quickfix.SessionID;

public interface QuickFixJOperations {
    boolean send(Message message, SessionID sessionID);
}
