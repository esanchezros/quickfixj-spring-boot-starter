/*
 * Copyright 2017-2024 the original author or authors.
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
package io.allune.quickfixj.spring.boot.actuate.health;

import lombok.Getter;
import org.springframework.boot.health.contributor.AbstractHealthIndicator;
import org.springframework.boot.health.contributor.Health;
import quickfix.Connector;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionSchedule;
import quickfix.SessionScheduleFactory;
import quickfix.SessionSettings;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class QuickFixJSessionHealthIndicator extends AbstractHealthIndicator {

	private final Connector connector;

	private final SessionScheduleFactory sessionScheduleFactory;

	private final SessionSettings sessionSettings;

	private final Map<SessionID, SessionSchedule> sessionScheduleMap = new ConcurrentHashMap<>();

	public QuickFixJSessionHealthIndicator(
			Connector connector,
			SessionScheduleFactory sessionScheduleFactory,
			SessionSettings sessionSettings) {
		this.connector = connector;
		this.sessionScheduleFactory = sessionScheduleFactory;
		this.sessionSettings = sessionSettings;
	}

	@Override
	protected void doHealthCheck(Health.Builder builder) {
		connector.getSessions().forEach(sessionID -> {
			SessionSchedule sessionSchedule =
					sessionScheduleMap.computeIfAbsent(sessionID,
							id -> getSessionSchedule(sessionID).orElse(null));

			SessionStatus sessionStatus = getSessionStatus(sessionID, sessionSchedule);
			switch (sessionStatus) {
				case LOGGED_ON:
					builder.up().withDetail(sessionID.toString(), "LoggedOn");
					break;
				case LOGGED_OFF:
					builder.down().withDetail(sessionID.toString(), "LoggedOff");
					break;
				case NOT_IN_SESSION:
					builder.unknown().withDetail(sessionID.toString(), "NotInSession");
					break;
				case ERROR:
				default:
					builder.outOfService().withDetail(sessionID.toString(), "Error");
					break;
			}

			if (sessionSchedule != null) {
				builder.withDetail("sessionSchedule", sessionSchedule.toString());
			}
		});
	}

	private Optional<SessionSchedule> getSessionSchedule(SessionID sessionID) {
		try {
			return Optional.of(sessionScheduleFactory.create(sessionID, sessionSettings));
		} catch (Exception e) {
			return Optional.empty();
		}
	}

	private SessionStatus getSessionStatus(SessionID sessionID, SessionSchedule sessionSchedule) {
		if (sessionID == null || sessionSchedule == null) {
			return SessionStatus.ERROR;
		}

		try {
			if (sessionSchedule.isSessionTime()) {
				Session session = Session.lookupSession(sessionID);
				if (session != null && session.isLoggedOn()) {
					return SessionStatus.LOGGED_ON;
				} else {
					return SessionStatus.LOGGED_OFF;
				}
			} else {
				return SessionStatus.NOT_IN_SESSION;
			}
		} catch (Exception e) {
			return SessionStatus.ERROR;
		}
	}

	@Getter
	public enum SessionStatus {

		LOGGED_ON("LoggedOn"),
		LOGGED_OFF("LoggedOff"),
		NOT_IN_SESSION("NotInSession"),
		ERROR("Error");

		private final String description;

		SessionStatus(String description) {
			this.description = description;
		}
	}
}
