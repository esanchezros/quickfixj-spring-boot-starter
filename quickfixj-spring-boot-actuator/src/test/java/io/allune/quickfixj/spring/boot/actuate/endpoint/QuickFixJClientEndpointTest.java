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
package io.allune.quickfixj.spring.boot.actuate.endpoint;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.endpoint.Sanitizer;
import quickfix.ConfigError;
import quickfix.Initiator;
import quickfix.SessionID;
import quickfix.SessionSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class QuickFixJClientEndpointTest {

	@Mock
	private Initiator initiator;

	@Mock
	private SessionSettings sessionSettings;

	@Spy
	private Sanitizer sanitizer = new Sanitizer();

	@InjectMocks
	private QuickFixJClientEndpoint quickFixJClientEndpoint;

	@Test
	public void shouldReadProperties() throws ConfigError {
		Map<SessionID, Properties> sessions = createSessions();
		given(initiator.getSessions()).willReturn(new ArrayList<>(sessions.keySet()));
		given(sessionSettings.getDefaultProperties()).willReturn(createDefaultProperties());
		for (Map.Entry<SessionID, Properties> entry : sessions.entrySet()) {
			given(sessionSettings.getSessionProperties(entry.getKey())).willReturn(entry.getValue());
		}

		Map<String, Properties> actualProperties = quickFixJClientEndpoint.readProperties();

		assertThat(actualProperties.keySet()).hasSize(sessions.size());
		sessions.forEach((key, value) -> {
			Properties properties = actualProperties.get(key.toString());
			assertThat(properties).isNotNull();
			assertThat(properties).containsAllEntriesOf(properties);
			assertThat(properties).containsEntry("BeginString", properties.get("BeginString"));
			assertThat(properties).containsEntry("SenderCompID", properties.get("SenderCompID"));
			assertThat(properties).containsEntry("TargetCompID", properties.get("TargetCompID"));
			assertThat(properties).containsEntry("ConnectionType", properties.get("ConnectionType"));
			assertThat(properties).containsEntry("SocketAcceptPort", properties.get("SocketAcceptPort"));
			assertThat(properties).containsEntry("FileStorePath", properties.get("FileStorePath"));
			assertThat(properties).containsEntry("SocketKeyStorePassword", "******");
			assertThat(properties).containsEntry("SocketTrustStorePassword", "******");
			assertThat(properties).containsEntry("ProxyPassword", "******");
			assertThat(properties).containsEntry("JdbcPassword", "******");
		});
	}

	@Test
	void shouldThrowIllegalStateExceptionGivenConfigError() throws ConfigError {
		Map<SessionID, Properties> sessions = createSessions();
		given(initiator.getSessions()).willReturn(new ArrayList<>(sessions.keySet()));
		given(sessionSettings.getDefaultProperties()).willReturn(createDefaultProperties());
		given(sessionSettings.getSessionProperties(any())).willThrow(ConfigError.class);

		assertThatThrownBy(() -> quickFixJClientEndpoint.readProperties())
			.isInstanceOf(IllegalStateException.class);
	}

	private Map<SessionID, Properties> createSessions() {
		long systemTime = System.currentTimeMillis();
		SessionID sessionID42 = new SessionID("FIX.4.2", "SENDER" + systemTime, "TARGET" + systemTime);
		SessionID sessionID44 = new SessionID("FIX.4.4", "SENDER" + systemTime, "TARGET" + systemTime);

		Map<SessionID, Properties> sessions = new HashMap<>();
		sessions.put(sessionID42, createProperties());
		sessions.put(sessionID44, createProperties());
		return sessions;
	}

	private Properties createProperties() {
		Properties properties = new Properties();
		properties.put("foo", "mumble");
		properties.put("baz", "fargle");
		properties.put("FileStorePath", "bargle");
		properties.put("SocketKeyStorePassword", "secret");
		properties.put("SocketTrustStorePassword", "secret");
		properties.put("ProxyPassword", "secret");
		properties.put("JdbcPassword", "secret");
		return properties;
	}

	private Properties createDefaultProperties() {
		Properties properties = new Properties();
		properties.put("ConnectionType", "acceptor");
		properties.put("SocketAcceptPort", "5001");
		properties.put("FileStorePath", "store");
		return properties;
	}
}
