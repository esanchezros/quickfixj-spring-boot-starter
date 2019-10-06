package io.allune.quickfixj.spring.boot.starter.template;

import quickfix.Session;
import quickfix.SessionID;

public class DefaultSessionLookupHandler implements SessionLookupHandler {

    @Override
    public Session lookupBySessionID(SessionID sessionID) {
        return Session.lookupSession(sessionID);
    }
}
