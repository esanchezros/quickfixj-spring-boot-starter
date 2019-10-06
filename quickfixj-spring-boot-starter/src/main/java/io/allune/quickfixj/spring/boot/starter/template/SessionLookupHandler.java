package io.allune.quickfixj.spring.boot.starter.template;

import quickfix.Session;
import quickfix.SessionID;

public interface SessionLookupHandler {

    Session lookupBySessionID(SessionID sessionID);
}
