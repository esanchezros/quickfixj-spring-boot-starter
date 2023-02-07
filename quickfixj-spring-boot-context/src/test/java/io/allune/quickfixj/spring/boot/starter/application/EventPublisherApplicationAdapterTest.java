/*
 * Copyright 2017-2022 the original author or authors.
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
package io.allune.quickfixj.spring.boot.starter.application;

import io.allune.quickfixj.spring.boot.starter.EnableQuickFixJClient;
import io.allune.quickfixj.spring.boot.starter.model.Create;
import io.allune.quickfixj.spring.boot.starter.model.FromAdmin;
import io.allune.quickfixj.spring.boot.starter.model.FromApp;
import io.allune.quickfixj.spring.boot.starter.model.Logon;
import io.allune.quickfixj.spring.boot.starter.model.Logout;
import io.allune.quickfixj.spring.boot.starter.model.ToAdmin;
import io.allune.quickfixj.spring.boot.starter.model.ToApp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import quickfix.Application;
import quickfix.Message;
import quickfix.SessionID;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Durations.FIVE_SECONDS;
import static org.mockito.Mockito.mock;

/**
 * @author Eduardo Sanchez-Ros
 */
@SpringBootTest(
		properties = {
				"quickfixj.client.autoStartup=false",
				"quickfixj.client.config=classpath:quickfixj.cfg",
				"quickfixj.client.jmx-enabled=false"
		})
public class EventPublisherApplicationAdapterTest {

	@Autowired
	private Application clientApplication;

	private static List<Object> receivedEvents = new ArrayList<>();

	@BeforeEach
	public void setUp() {
		receivedEvents.clear();
	}

	@Test
	public void shouldPublishFromAdminMessage() throws Exception {
		Message message = mock(Message.class);
		SessionID sessionId = mock(SessionID.class);
		clientApplication.fromAdmin(message, sessionId);

		await().atMost(FIVE_SECONDS).until(() -> receivedEvents.size() == 1);
		assertThat(receivedEvents.get(0)).isInstanceOf(FromAdmin.class);
		assertThat(((FromAdmin) receivedEvents.get(0)).getMessage()).isEqualTo(message);
		assertThat(((FromAdmin) receivedEvents.get(0)).getSessionId()).isEqualTo(sessionId);
	}

	@Test
	public void shouldPublishFromAppMessage() throws Exception {
		Message message = mock(Message.class);
		SessionID sessionId = mock(SessionID.class);
		clientApplication.fromApp(message, sessionId);

		await().atMost(FIVE_SECONDS).until(() -> receivedEvents.size() > 0);
		assertThat(receivedEvents.get(0)).isInstanceOf(FromApp.class);
		assertThat(((FromApp) receivedEvents.get(0)).getMessage()).isEqualTo(message);
		assertThat(((FromApp) receivedEvents.get(0)).getSessionId()).isEqualTo(sessionId);
	}

	@Test
	public void shouldPublishCreateMessage() {
		SessionID sessionId = mock(SessionID.class);
		clientApplication.onCreate(sessionId);

		await().atMost(FIVE_SECONDS).until(() -> receivedEvents.size() > 0);
		assertThat(receivedEvents.get(0)).isInstanceOf(Create.class);
		assertThat(((Create) receivedEvents.get(0)).getSessionId()).isEqualTo(sessionId);
	}

	@Test
	public void shouldPublishLogonMessage() {
		SessionID sessionId = mock(SessionID.class);
		clientApplication.onLogon(sessionId);

		await().atMost(FIVE_SECONDS).until(() -> receivedEvents.size() > 0);
		assertThat(receivedEvents.get(0)).isInstanceOf(Logon.class);
		assertThat(((Logon) receivedEvents.get(0)).getSessionId()).isEqualTo(sessionId);
	}

	@Test
	public void shouldPublishLogoutMessage() {
		SessionID sessionId = mock(SessionID.class);
		clientApplication.onLogout(sessionId);

		await().atMost(FIVE_SECONDS).until(() -> receivedEvents.size() > 0);
		assertThat(receivedEvents.get(0)).isInstanceOf(Logout.class);
		assertThat(((Logout) receivedEvents.get(0)).getSessionId()).isEqualTo(sessionId);
	}

	@Test
	public void shouldPublishToAdminMessage() {
		Message message = mock(Message.class);
		SessionID sessionId = mock(SessionID.class);
		clientApplication.toAdmin(message, sessionId);

		await().atMost(FIVE_SECONDS).until(() -> receivedEvents.size() > 0);
		assertThat(receivedEvents.get(0)).isInstanceOf(ToAdmin.class);
		assertThat(((ToAdmin) receivedEvents.get(0)).getSessionId()).isEqualTo(sessionId);
	}

	@Test
	public void shouldPublishToAppMessage() throws Exception {
		Message message = mock(Message.class);
		SessionID sessionId = mock(SessionID.class);
		clientApplication.toApp(message, sessionId);

		await().atMost(FIVE_SECONDS).until(() -> receivedEvents.size() > 0);
		assertThat(receivedEvents.get(0)).isInstanceOf(ToApp.class);
		assertThat(((ToApp) receivedEvents.get(0)).getMessage()).isEqualTo(message);
		assertThat(((ToApp) receivedEvents.get(0)).getSessionId()).isEqualTo(sessionId);
	}

	@Configuration
	@EnableAutoConfiguration
	@EnableQuickFixJClient
	static class TestConfig {

		@EventListener
		public void listenFromAdmin(FromAdmin fromAdmin) {
			receivedEvents.add(fromAdmin);
		}

		@EventListener
		public void listenFromApp(FromApp fromApp) {
			receivedEvents.add(fromApp);
		}

		@EventListener
		public void listenOnCreate(Create create) {
			receivedEvents.add(create);
		}

		@EventListener
		public void listenOnLogon(Logon logon) {
			receivedEvents.add(logon);
		}

		@EventListener
		public void listenOnLogout(Logout logout) {
			receivedEvents.add(logout);
		}

		@EventListener
		public void listenToAdmin(ToAdmin toAdmin) {
			receivedEvents.add(toAdmin);
		}

		@EventListener
		public void listenToApp(ToApp toApp) {
			receivedEvents.add(toApp);
		}
	}
}
