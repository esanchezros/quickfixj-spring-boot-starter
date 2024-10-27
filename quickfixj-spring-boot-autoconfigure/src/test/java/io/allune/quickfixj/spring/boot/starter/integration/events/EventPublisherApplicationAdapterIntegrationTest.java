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
package io.allune.quickfixj.spring.boot.starter.integration.events;

import io.allune.quickfixj.spring.boot.starter.integration.events.EventPublisherApplicationAdapterITConfiguration.Events;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import quickfix.Application;
import quickfix.Message;
import quickfix.SessionID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Durations.FIVE_SECONDS;
import static org.mockito.Mockito.mock;

/**
 * @author Eduardo Sanchez-Ros
 */
@SpringBootTest(
		classes = EventPublisherApplicationAdapterITConfiguration.class,
		properties = {
				"spring.main.allow-bean-definition-overriding=true",
				"quickfixj.client.enabled=true",
				"quickfixj.client.autoStartup=false",
				"quickfixj.client.config=classpath:quickfixj.cfg",
				"quickfixj.client.jmx-enabled=false"
		})
@DirtiesContext
public class EventPublisherApplicationAdapterIntegrationTest {

	@Autowired
	private Application clientApplication;

	@Autowired
	private Events events;

	@BeforeEach
	public void setUp() {
		events.receivedEvents().clear();
	}

	@Test
	public void shouldPublishFromAdminMessage() throws Exception {
		Message message = mock(Message.class);
		SessionID sessionId = mock(SessionID.class);
		clientApplication.fromAdmin(message, sessionId);

		await().atMost(FIVE_SECONDS).until(() -> events.receivedEvents().size() == 1);
		assertThat(events.receivedEvents().get(0)).isInstanceOf(FromAdmin.class);
		assertThat(((FromAdmin) events.receivedEvents().get(0)).getMessage()).isEqualTo(message);
		assertThat(((FromAdmin) events.receivedEvents().get(0)).getSessionId()).isEqualTo(sessionId);
	}

	@Test
	public void shouldPublishFromAppMessage() throws Exception {
		Message message = mock(Message.class);
		SessionID sessionId = mock(SessionID.class);
		clientApplication.fromApp(message, sessionId);

		await().atMost(FIVE_SECONDS).until(() -> !events.receivedEvents().isEmpty());
		assertThat(events.receivedEvents().get(0)).isInstanceOf(FromApp.class);
		assertThat(((FromApp) events.receivedEvents().get(0)).getMessage()).isEqualTo(message);
		assertThat(((FromApp) events.receivedEvents().get(0)).getSessionId()).isEqualTo(sessionId);
	}

	@Test
	public void shouldPublishCreateMessage() {
		SessionID sessionId = mock(SessionID.class);
		clientApplication.onCreate(sessionId);

		await().atMost(FIVE_SECONDS).until(() -> !events.receivedEvents().isEmpty());
		assertThat(events.receivedEvents().get(0)).isInstanceOf(Create.class);
		assertThat(((Create) events.receivedEvents().get(0)).getSessionId()).isEqualTo(sessionId);
	}

	@Test
	public void shouldPublishLogonMessage() {
		SessionID sessionId = mock(SessionID.class);
		clientApplication.onLogon(sessionId);

		await().atMost(FIVE_SECONDS).until(() -> !events.receivedEvents().isEmpty());
		assertThat(events.receivedEvents().get(0)).isInstanceOf(Logon.class);
		assertThat(((Logon) events.receivedEvents().get(0)).getSessionId()).isEqualTo(sessionId);
	}

	@Test
	public void shouldPublishLogoutMessage() {
		SessionID sessionId = mock(SessionID.class);
		clientApplication.onLogout(sessionId);

		await().atMost(FIVE_SECONDS).until(() -> !events.receivedEvents().isEmpty());
		assertThat(events.receivedEvents().get(0)).isInstanceOf(Logout.class);
		assertThat(((Logout) events.receivedEvents().get(0)).getSessionId()).isEqualTo(sessionId);
	}

	@Test
	public void shouldPublishToAdminMessage() {
		Message message = mock(Message.class);
		SessionID sessionId = mock(SessionID.class);
		clientApplication.toAdmin(message, sessionId);

		await().atMost(FIVE_SECONDS).until(() -> !events.receivedEvents().isEmpty());
		assertThat(events.receivedEvents().get(0)).isInstanceOf(ToAdmin.class);
		assertThat(((ToAdmin) events.receivedEvents().get(0)).getSessionId()).isEqualTo(sessionId);
	}

	@Test
	public void shouldPublishToAppMessage() throws Exception {
		Message message = mock(Message.class);
		SessionID sessionId = mock(SessionID.class);
		clientApplication.toApp(message, sessionId);

		await().atMost(FIVE_SECONDS).until(() -> !events.receivedEvents().isEmpty());
		assertThat(events.receivedEvents().get(0)).isInstanceOf(ToApp.class);
		assertThat(((ToApp) events.receivedEvents().get(0)).getMessage()).isEqualTo(message);
		assertThat(((ToApp) events.receivedEvents().get(0)).getSessionId()).isEqualTo(sessionId);
	}
}
