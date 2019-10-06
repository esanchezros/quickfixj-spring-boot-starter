package io.allune.quickfixj.spring.boot.starter.template;

import quickfix.Message;
import quickfix.SessionID;

public interface QuickFixJOperations {
    abstract boolean send(Message message, SessionID sessionID);

    boolean send(Message message, String qualifier);

    boolean send(Message message);

    boolean send(Message message, String senderCompID, String targetCompID);

    boolean send(Message message, String senderCompID, String targetCompID, String qualifier);
}
