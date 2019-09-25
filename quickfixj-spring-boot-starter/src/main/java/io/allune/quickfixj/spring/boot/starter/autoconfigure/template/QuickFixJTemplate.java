package io.allune.quickfixj.spring.boot.starter.autoconfigure.template;

import org.springframework.util.Assert;
import quickfix.Message;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionNotFound;

public class QuickFixJTemplate implements QuickFixJOperations {

    @Override
    public boolean send(Message message, SessionID sessionID) {
        Assert.notNull(message, "'message' must not be null");
        Assert.notNull(sessionID, "'sessionID' must not be null");

        try {
            return Session.sendToTarget(message, sessionID);
        } catch (SessionNotFound ex) {
            // TODO: Create runtime exception
            throw new RuntimeException(ex);
        }
    }
}
