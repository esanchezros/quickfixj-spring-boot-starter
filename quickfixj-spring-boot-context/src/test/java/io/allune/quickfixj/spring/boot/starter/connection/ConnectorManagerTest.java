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
package io.allune.quickfixj.spring.boot.starter.connection;

import io.allune.quickfixj.spring.boot.starter.exception.ConfigurationException;
import org.junit.jupiter.api.Test;
import quickfix.ConfigError;
import quickfix.Connector;
import quickfix.RuntimeError;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
}
