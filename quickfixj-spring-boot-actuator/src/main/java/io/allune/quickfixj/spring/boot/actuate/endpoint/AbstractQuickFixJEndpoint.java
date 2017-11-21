/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.allune.quickfixj.spring.boot.actuate.endpoint;

import org.springframework.boot.actuate.endpoint.AbstractEndpoint;
import org.springframework.boot.actuate.endpoint.Endpoint;
import quickfix.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static quickfix.SessionID.NOT_SET;

/**
 * Base class for QuickFixJ {@link Endpoint}s.
 *
 * @author Eduardo Sanchez-Ros
 */
public class AbstractQuickFixJEndpoint extends AbstractEndpoint<Map<String, Properties>> {

    private final Connector connector;
    private final SessionSettings sessionSettings;

    AbstractQuickFixJEndpoint(String id, Connector connector, SessionSettings sessionSettings) {
        super(id);
        this.connector = connector;
        this.sessionSettings = sessionSettings;
    }

    @Override
    public Map<String, Properties> invoke() {
        Map<String, Properties> reports = new HashMap<>();
        connector.getSessions().forEach(sessionId -> {
            try {
//                Session session = Session.lookupSession(sessionId);

                Properties p = new Properties();
                p.putAll(sessionSettings.getDefaultProperties());
                p.putAll(sessionSettings.getSessionProperties(sessionId));
                p.putAll(addSessionIdProperties(sessionId));

                reports.put(sessionId.toString(), p);
            } catch (ConfigError e) {
                throw new IllegalStateException(e);
            }
        });
        return reports;
    }

    private Properties addSessionIdProperties(SessionID sessionID) {
        Properties properties = new Properties();
        properties.put("beginString", sessionID.getBeginString());
        properties.put("senderCompID", sessionID.getSenderCompID());
        String senderSubID = sessionID.getSenderSubID();
        if (!senderSubID.equals(NOT_SET)) {
            properties.put("senderSubID", senderSubID);
        }
        String senderLocationID = sessionID.getSenderLocationID();
        if (!senderLocationID.equals(NOT_SET)) {
            properties.put("senderLocationID", senderLocationID);
        }
        properties.put("targetCompID", sessionID.getTargetCompID());
        String targetSubID = sessionID.getTargetSubID();
        if (!targetSubID.equals(NOT_SET)) {
            properties.put("targetSubID", targetSubID);
        }
        String targetLocationID = sessionID.getTargetLocationID();
        if (!targetLocationID.equals(NOT_SET)) {
            properties.put("targetLocationID", targetLocationID);
        }
        String sessionQualifier = sessionID.getSessionQualifier();
        if (!sessionQualifier.equals(NOT_SET)) {
            properties.put("qualifier", sessionQualifier);
        }

        return properties;
    }
}
