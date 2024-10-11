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
package io.allune.quickfixj.spring.boot.starter.application;

import io.allune.quickfixj.spring.boot.starter.model.Create;
import io.allune.quickfixj.spring.boot.starter.model.FromAdmin;
import io.allune.quickfixj.spring.boot.starter.model.FromApp;
import io.allune.quickfixj.spring.boot.starter.model.Logon;
import io.allune.quickfixj.spring.boot.starter.model.Logout;
import io.allune.quickfixj.spring.boot.starter.model.ToAdmin;
import io.allune.quickfixj.spring.boot.starter.model.ToApp;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import quickfix.Message;
import quickfix.SessionID;

import static org.mockito.Mockito.*;

public class EventPublisherApplicationAdapterTest {

	@Test
	public void testFromAdmin() {
		// mock dependencies
		ApplicationEventPublisher applicationEventPublisher = mock(ApplicationEventPublisher.class);
		EventPublisherApplicationAdapter adapter = new EventPublisherApplicationAdapter(applicationEventPublisher);

		Message message = mock(Message.class);
		SessionID sessionId = mock(SessionID.class);

		// invoke the method under test.
		adapter.fromAdmin(message, sessionId);

		// Mock will record the interactions. We just need to verify if the call was made
		verify(applicationEventPublisher).publishEvent(isA(FromAdmin.class));
	}

	@Test
	public void testFromApp() {
		// mock dependencies
		ApplicationEventPublisher applicationEventPublisher = mock(ApplicationEventPublisher.class);
		EventPublisherApplicationAdapter adapter = new EventPublisherApplicationAdapter(applicationEventPublisher);

		Message message = mock(Message.class);
		SessionID sessionId = mock(SessionID.class);

		// invoke the method under test.
		adapter.fromApp(message, sessionId);

		// Mock will record the interactions. We just need to verify if the call was made
		verify(applicationEventPublisher).publishEvent(isA(FromApp.class));
	}

	@Test
	public void testOnCreate() {
		// mock dependencies
		ApplicationEventPublisher applicationEventPublisher = mock(ApplicationEventPublisher.class);
		EventPublisherApplicationAdapter adapter = new EventPublisherApplicationAdapter(applicationEventPublisher);

		SessionID sessionId = mock(SessionID.class);

		// invoke the method under test.
		adapter.onCreate(sessionId);

		// Mock will record the interactions. We just need to verify if the call was made
		verify(applicationEventPublisher).publishEvent(isA(Create.class));
	}

	@Test
	public void testOnLogon() {
		// mock dependencies
		ApplicationEventPublisher applicationEventPublisher = mock(ApplicationEventPublisher.class);
		EventPublisherApplicationAdapter adapter = new EventPublisherApplicationAdapter(applicationEventPublisher);

		SessionID sessionId = mock(SessionID.class);

		// invoke the method under test.
		adapter.onLogon(sessionId);

		// Mock will record the interactions. We just need to verify if the call was made
		verify(applicationEventPublisher).publishEvent(isA(Logon.class));
	}

	@Test
	public void testOnLogout() {
		// mock dependencies
		ApplicationEventPublisher applicationEventPublisher = mock(ApplicationEventPublisher.class);
		EventPublisherApplicationAdapter adapter = new EventPublisherApplicationAdapter(applicationEventPublisher);

		SessionID sessionId = mock(SessionID.class);

		// invoke the method under test.
		adapter.onLogout(sessionId);

		// Mock will record the interactions. We just need to verify if the call was made
		verify(applicationEventPublisher).publishEvent(isA(Logout.class));
	}

	@Test
	public void testToAdmin() {
		// mock dependencies
		ApplicationEventPublisher applicationEventPublisher = mock(ApplicationEventPublisher.class);
		EventPublisherApplicationAdapter adapter = new EventPublisherApplicationAdapter(applicationEventPublisher);

		Message message = mock(Message.class);
		SessionID sessionId = mock(SessionID.class);

		// invoke the method under test.
		adapter.toAdmin(message, sessionId);

		// Mock will record the interactions. We just need to verify if the call was made
		verify(applicationEventPublisher).publishEvent(isA(ToAdmin.class));
	}

	@Test
	public void testToApp() {
		// mock dependencies
		ApplicationEventPublisher applicationEventPublisher = mock(ApplicationEventPublisher.class);
		EventPublisherApplicationAdapter adapter = new EventPublisherApplicationAdapter(applicationEventPublisher);

		Message message = mock(Message.class);
		SessionID sessionId = mock(SessionID.class);

		// invoke the method under test.
		adapter.toApp(message, sessionId);

		// Mock will record the interactions. We just need to verify if the call was made
		verify(applicationEventPublisher).publishEvent(isA(ToApp.class));
	}
}