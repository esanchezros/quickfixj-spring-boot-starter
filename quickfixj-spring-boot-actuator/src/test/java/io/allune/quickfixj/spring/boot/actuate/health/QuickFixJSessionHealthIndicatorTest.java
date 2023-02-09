/*
 * Copyright 2017-2023 the original author or authors.
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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import quickfix.ConfigError;
import quickfix.Connector;
import quickfix.DayConverter;
import quickfix.DefaultSessionSchedule;
import quickfix.MockSystemTimeSource;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionSchedule;
import quickfix.SessionScheduleFactory;
import quickfix.SessionSettings;
import quickfix.SystemTime;
import quickfix.field.converter.UtcTimeOnlyConverter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import static java.util.Calendar.FEBRUARY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static quickfix.SessionHelper.registerSession;
import static quickfix.SessionHelper.unregisterSession;

/**
 * Tests for FIX health indicator
 * <p>
 * NB: Some examples here have been taken from the quickfixj lib
 *
 * @author Eduardo Sanchez-Ros
 */
@SuppressWarnings("SameParameterValue")
@ExtendWith(MockitoExtension.class)
public class QuickFixJSessionHealthIndicatorTest {

	private static final TimeZone UTC_TIMEZONE = TimeZone.getTimeZone("UTC");

	@Mock
	private Connector connector;

	@Mock
	private SessionScheduleFactory sessionScheduleFactory;

	@Mock
	private SessionSettings sessionSettings;

	@InjectMocks
	private QuickFixJSessionHealthIndicator quickFixJSessionHealthIndicator;

	@Mock
	private Session session;

	private MockSystemTimeSource mockSystemTimeSource;
	private Locale defaultLocale;
	private SessionID sessionID;

	@BeforeEach
	public void setUp() {
		mockSystemTimeSource = new MockSystemTimeSource();
		SystemTime.setTimeSource(mockSystemTimeSource);
		defaultLocale = Locale.getDefault();
		Locale.setDefault(Locale.ENGLISH);

		sessionID = new SessionID("FIX.4.2", "SENDER", "TARGET");
		ArrayList<SessionID> sessionIDs = new ArrayList<>();
		sessionIDs.add(sessionID);
		given(connector.getSessions()).willReturn(sessionIDs);
		given(session.getSessionID()).willReturn(sessionID);
		registerSession(session);
	}

	@AfterEach
	public void tearDown() {
		SystemTime.setTimeSource(null);
		Locale.setDefault(defaultLocale);
	}

	@Test
	public void shouldReportServiceHealthIsUPGivenSessionIsLoggedOn() throws Exception {
		// Given
		Calendar start = getTimeStamp(2022, FEBRUARY, 1, 9, 0, 0, UTC_TIMEZONE);
		Calendar end = getTimeStamp(2022, FEBRUARY, 1, 16, 30, 0, UTC_TIMEZONE);
		SessionSchedule sessionSchedule = newSessionSchedule(sessionID, start.getTime(), end.getTime(), -1, -1);
		given(sessionScheduleFactory.create(sessionID, sessionSettings)).willReturn(sessionSchedule);
		given(session.isLoggedOn()).willReturn(true);
		mockSystemTimeSource.setTime(getTimeStamp(2022, FEBRUARY, 1, 11, 0, 0, UTC_TIMEZONE));

		// When
		Health health = quickFixJSessionHealthIndicator.health();

		// Then
		assertThat(health.getStatus()).isEqualTo(Status.UP);
		assertThat(health.getDetails().get(sessionID.toString())).isEqualTo("LoggedOn");
		assertThat(health.getDetails().get("sessionSchedule")).isEqualTo(sessionSchedule.toString());
		unregisterSession(sessionID);
	}

	@Test
	public void shouldReportServiceHealthIsDOWNGivenSessionIsLoggedOff() throws Exception {
		// Given
		Calendar start = getTimeStamp(2022, FEBRUARY, 1, 9, 0, 0, UTC_TIMEZONE);
		Calendar end = getTimeStamp(2022, FEBRUARY, 1, 16, 30, 0, UTC_TIMEZONE);
		SessionSchedule sessionSchedule = newSessionSchedule(sessionID, start.getTime(), end.getTime(), -1, -1);
		given(sessionScheduleFactory.create(sessionID, sessionSettings)).willReturn(sessionSchedule);
		given(session.isLoggedOn()).willReturn(false);
		mockSystemTimeSource.setTime(getTimeStamp(2022, FEBRUARY, 1, 11, 0, 0, UTC_TIMEZONE));

		// When
		Health health = quickFixJSessionHealthIndicator.health();

		// Then
		assertThat(health.getStatus()).isEqualTo(Status.DOWN);
		assertThat(health.getDetails().get(sessionID.toString())).isEqualTo("LoggedOff");
		assertThat(health.getDetails().get("sessionSchedule")).isEqualTo(sessionSchedule.toString());
	}

	@Test
	public void shouldReportServiceHealthIsUNKNOWNGivenSessionIsNotInSession() throws Exception {
		// Given
		Calendar start = getTimeStamp(2022, FEBRUARY, 1, 9, 0, 0, UTC_TIMEZONE);
		Calendar end = getTimeStamp(2022, FEBRUARY, 1, 16, 30, 0, UTC_TIMEZONE);
		SessionSchedule sessionSchedule = newSessionSchedule(sessionID, start.getTime(), end.getTime(), -1, -1);
		given(sessionScheduleFactory.create(sessionID, sessionSettings)).willReturn(sessionSchedule);
		mockSystemTimeSource.setTime(getTimeStamp(2022, FEBRUARY, 1, 18, 0, 0, UTC_TIMEZONE));

		// When
		Health health = quickFixJSessionHealthIndicator.health();

		// Then
		assertThat(health.getStatus()).as("wrong status").isEqualTo(Status.UNKNOWN);
		assertThat(health.getDetails().get(sessionID.toString())).isEqualTo("NotInSession");
		assertThat(health.getDetails().get("sessionSchedule")).isEqualTo(sessionSchedule.toString());
	}

	@Test
	public void shouldReportServiceHealthIsOUTOFSERVICEGivenNotInSession() throws ConfigError {
		// Given
		given(sessionScheduleFactory.create(sessionID, sessionSettings)).willReturn(null);

		// When
		Health health = quickFixJSessionHealthIndicator.health();

		// Then
		assertThat(health.getStatus()).isEqualTo(Status.OUT_OF_SERVICE);
		assertThat(health.getDetails().get(sessionID.toString())).isEqualTo("Error");
	}

	private SessionSchedule newSessionSchedule(
			SessionID sessionID, Date startTime, Date endTime, int startDay, int endDay) throws Exception {
		SessionSettings settings = new SessionSettings();
		if (startDay >= 0) {
			settings.setString(Session.SETTING_START_DAY, DayConverter.toString(startDay));
		}
		if (endDay >= 0) {
			settings.setString(Session.SETTING_END_DAY, DayConverter.toString(endDay));
		}
		settings.setString(Session.SETTING_START_TIME, UtcTimeOnlyConverter.convert(startTime,
				false));
		settings.setString(Session.SETTING_END_TIME, UtcTimeOnlyConverter.convert(endTime, false));

		return new DefaultSessionSchedule(settings, sessionID);
	}

	private Calendar getTimeStamp(
			int year, int month, int day, int hour, int minute, int second, TimeZone timeZone) {
		Calendar c = new GregorianCalendar(year, month, day, hour, minute, second);
		c.setTimeZone(timeZone);
		return c;
	}
}
