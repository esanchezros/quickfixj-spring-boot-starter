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
package io.allune.quickfixj.spring.boot.starter.connection;

import io.allune.quickfixj.spring.boot.starter.exception.ConfigurationException;
import org.junit.jupiter.api.Test;
import quickfix.ConfigError;
import quickfix.Connector;
import quickfix.RuntimeError;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author Eduardo Sanchez-Ros
 */
public class ConnectorManagerTest {

	@Test
	public void shouldStartAndStopConnector() throws Exception {

		// Given
		Connector connector = mock(Connector.class);
		ConnectorManager connectorManager = new ConnectorManager(connector);

		// When
		connectorManager.start();
		assertThat(connectorManager.isRunning()).isTrue();

		connectorManager.stop();
		assertThat(connectorManager.isRunning()).isFalse();

		// Then
		verify(connector).start();
		verify(connector).stop(false);
	}

	@Test
	public void shouldStartConnectorAndStopWithCallback() throws Exception {

		Connector connector = mock(Connector.class);
		ConnectorManager connectorManager = new ConnectorManager(connector);

		connectorManager.start();
		assertThat(connectorManager.isRunning()).isTrue();
		verify(connector).start();

		Runnable callback = mock(Runnable.class);
		connectorManager.stop(callback);
		assertThat(connectorManager.isRunning()).isFalse();

		verify(connector).stop(false);
		verify(callback).run();
	}

	@Test
	public void shouldStartAndStopForciblyConnector() throws Exception {

		// Given
		Connector connector = mock(Connector.class);
		ConnectorManager connectorManager = new ConnectorManager(connector);
		connectorManager.setForceDisconnect(true);

		// When
		connectorManager.start();
		assertThat(connectorManager.isRunning()).isTrue();

		connectorManager.stop();
		assertThat(connectorManager.isRunning()).isFalse();

		// Then
		verify(connector).start();
		verify(connector).stop(true);
	}

	@Test
	public void shouldThrowConfigurationExceptionUponConfigErrorFailure() throws Exception {

		Connector connector = mock(Connector.class);
		willThrow(ConfigError.class).given(connector).start();
		ConnectorManager connectorManager = new ConnectorManager(connector);

		assertThatThrownBy(connectorManager::start)
				.isInstanceOf(ConfigurationException.class);
		assertThat(connectorManager.isRunning()).isFalse();

		verify(connector).start();
	}

	@Test
	public void shouldThrowConfigurationExceptionUponRuntimeErrorFailure() throws Exception {

		Connector connector = mock(Connector.class);
		willThrow(RuntimeError.class).given(connector).start();
		ConnectorManager connectorManager = new ConnectorManager(connector);

		assertThatThrownBy(connectorManager::start)
				.isInstanceOf(ConfigurationException.class);
		assertThat(connectorManager.isRunning()).isFalse();

		verify(connector).start();
	}

	@Test
	void testIsAutoStartupDefaultTrue() {
		Connector connector = mock(Connector.class);
		ConnectorManager connectorManager = new ConnectorManager(connector);
		// By default autoStartup should be true
		assertTrue(connectorManager.isAutoStartup(), "The autoStartup should be true by default");
	}

	@Test
	void testIsAutoStartupSetToFalse() {
		Connector connector = mock(Connector.class);
		ConnectorManager connectorManager = new ConnectorManager(connector);
		// Set autoStartup to false
		connectorManager.setAutoStartup(false);
		assertFalse(connectorManager.isAutoStartup(), "The autoStartup should be false after setting it");
	}

	@Test
	void testIsAutoStartupSetToTrue() {
		Connector connector = mock(Connector.class);
		ConnectorManager connectorManager = new ConnectorManager(connector);

		// First set it to false
		connectorManager.setAutoStartup(false);
		assertFalse(connectorManager.isAutoStartup(), "The autoStartup should be false after setting it");

		// Then set it back to true
		connectorManager.setAutoStartup(true);
		assertTrue(connectorManager.isAutoStartup(), "The autoStartup should be true after setting it back");
	}

	@Test
	void testGetPhaseDefaultValue() {
		Connector connector = mock(Connector.class);
		ConnectorManager connectorManager = new ConnectorManager(connector);
		// By default the phase should be Integer.MAX_VALUE
		assertEquals(Integer.MAX_VALUE, connectorManager.getPhase(), "The default phase should be Integer.MAX_VALUE");
	}

	@Test
	void testSetPhaseCustomValue() {
		Connector connector = mock(Connector.class);
		ConnectorManager connectorManager = new ConnectorManager(connector);
		// Set phase to a specific value and check
		int customPhase = 42;
		connectorManager.setPhase(customPhase);
		assertEquals(customPhase, connectorManager.getPhase(), "The phase should be the custom value set");
	}

	@Test
	void testIsForceDisconnectDefaultValue() {
		Connector connector = mock(Connector.class);
		ConnectorManager connectorManager = new ConnectorManager(connector);
		// By default, forceDisconnect should be false
		assertFalse(connectorManager.isForceDisconnect(), "The default forceDisconnect should be false");
	}

	@Test
	void testSetForceDisconnectToTrue() {
		Connector connector = mock(Connector.class);
		ConnectorManager connectorManager = new ConnectorManager(connector);
		// Set forceDisconnect to true and check
		connectorManager.setForceDisconnect(true);
		assertTrue(connectorManager.isForceDisconnect(), "The forceDisconnect should be true after setting it");
	}

	@Test
	void testSetForceDisconnectToFalse() {
		Connector connector = mock(Connector.class);
		ConnectorManager connectorManager = new ConnectorManager(connector);
		// Initially set forceDisconnect to true
		connectorManager.setForceDisconnect(true);
		// Set forceDisconnect to false and check
		connectorManager.setForceDisconnect(false);
		assertFalse(connectorManager.isForceDisconnect(), "The forceDisconnect should be false after setting it back");
	}
}
