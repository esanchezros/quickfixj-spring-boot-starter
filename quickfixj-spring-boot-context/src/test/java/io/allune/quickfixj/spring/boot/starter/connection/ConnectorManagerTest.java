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
import org.mockito.InOrder;
import quickfix.ConfigError;
import quickfix.Connector;
import quickfix.RuntimeError;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
	public void shouldNotStartConnectorAgainWhileRunning() throws Exception {
		Connector connector = mock(Connector.class);
		ConnectorManager connectorManager = new ConnectorManager(connector);

		connectorManager.start();
		connectorManager.start();

		assertThat(connectorManager.isRunning()).isTrue();
		verify(connector, times(1)).start();
	}

	@Test
	public void shouldStartConnectorAndStopWithCallback() throws Exception {

		Connector connector = mock(Connector.class);
		ConnectorManager connectorManager = new ConnectorManager(connector);

		connectorManager.start();
		assertThat(connectorManager.isRunning()).isTrue();
		verify(connector).start();

		Runnable callback = mock(Runnable.class);
		doAnswer(invocation -> {
			assertThat(connectorManager.isRunning()).isFalse();
			return null;
		}).when(callback).run();
		connectorManager.stop(callback);
		assertThat(connectorManager.isRunning()).isFalse();

		InOrder stopOrder = inOrder(connector, callback);
		stopOrder.verify(connector).stop(false);
		stopOrder.verify(callback).run();
	}

	@Test
	public void shouldInvokeStopCallbackWhenAlreadyStopped() {
		Connector connector = mock(Connector.class);
		ConnectorManager connectorManager = new ConnectorManager(connector);
		Runnable callback = mock(Runnable.class);

		connectorManager.stop(callback);

		assertThat(connectorManager.isRunning()).isFalse();
		verify(connector, never()).stop(anyBoolean());
		verify(callback, times(1)).run();
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
	public void shouldNotStopConnectorAgainWhenAlreadyStopped() throws Exception {
		Connector connector = mock(Connector.class);
		ConnectorManager connectorManager = new ConnectorManager(connector);

		connectorManager.start();
		connectorManager.stop();
		connectorManager.stop();

		assertThat(connectorManager.isRunning()).isFalse();
		verify(connector, times(1)).stop(false);
	}

	@Test
	public void shouldSupportStartStopStartCycle() throws Exception {
		Connector connector = mock(Connector.class);
		ConnectorManager connectorManager = new ConnectorManager(connector);

		connectorManager.start();
		connectorManager.stop();
		connectorManager.start();

		assertThat(connectorManager.isRunning()).isTrue();
		verify(connector, times(2)).start();
		verify(connector, times(1)).stop(false);
	}

	@Test
	public void shouldThrowConfigurationExceptionUponConfigErrorFailure() throws Exception {

		Connector connector = mock(Connector.class);
		ConfigError failure = new ConfigError("invalid configuration");
		willThrow(failure).given(connector).start();
		ConnectorManager connectorManager = new ConnectorManager(connector);

		assertThatThrownBy(connectorManager::start)
				.isInstanceOf(ConfigurationException.class)
				.hasCause(failure);
		assertThat(connectorManager.isRunning()).isFalse();

		verify(connector).start();
	}

	@Test
	public void shouldThrowConfigurationExceptionUponRuntimeErrorFailure() throws Exception {

		Connector connector = mock(Connector.class);
		RuntimeError failure = new RuntimeError("runtime failure");
		willThrow(failure).given(connector).start();
		ConnectorManager connectorManager = new ConnectorManager(connector);

		assertThatThrownBy(connectorManager::start)
				.isInstanceOf(ConfigurationException.class)
				.hasCause(failure);
		assertThat(connectorManager.isRunning()).isFalse();

		verify(connector).start();
	}

	@Test
	public void shouldRetryConnectorAfterStartFailure() throws Exception {
		Connector connector = mock(Connector.class);
		ConfigError failure = new ConfigError("invalid configuration");
		doThrow(failure).doNothing().when(connector).start();
		ConnectorManager connectorManager = new ConnectorManager(connector);

		assertThatThrownBy(connectorManager::start)
				.isInstanceOf(ConfigurationException.class)
				.hasCause(failure);
		assertThat(connectorManager.isRunning()).isFalse();

		connectorManager.start();

		assertThat(connectorManager.isRunning()).isTrue();
		verify(connector, times(2)).start();
	}

	@Test
	public void shouldWrapUnexpectedRuntimeExceptionUponStartFailure() throws Exception {
		Connector connector = mock(Connector.class);
		IllegalArgumentException failure = new IllegalArgumentException("unexpected failure");
		doThrow(failure).when(connector).start();
		ConnectorManager connectorManager = new ConnectorManager(connector);

		assertThatThrownBy(connectorManager::start)
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("Could not start the connector")
				.hasCause(failure);
		assertThat(connectorManager.isRunning()).isFalse();
	}

	@Test
	public void shouldPropagateJvmErrorUponStartFailure() throws Exception {
		Connector connector = mock(Connector.class);
		AssertionError failure = new AssertionError("fatal failure");
		doThrow(failure).when(connector).start();
		ConnectorManager connectorManager = new ConnectorManager(connector);

		assertThatThrownBy(connectorManager::start).isSameAs(failure);
		assertThat(connectorManager.isRunning()).isFalse();
	}

	@Test
	public void shouldRemainStoppedWhenConnectorStopFails() throws Exception {
		Connector connector = mock(Connector.class);
		IllegalStateException failure = new IllegalStateException("stop failure");
		doThrow(failure).when(connector).stop(false);
		ConnectorManager connectorManager = new ConnectorManager(connector);
		connectorManager.start();

		assertThatThrownBy(connectorManager::stop).isSameAs(failure);
		assertThat(connectorManager.isRunning()).isFalse();
		verify(connector, times(1)).stop(false);
	}

	@Test
	public void shouldInvokeCallbackWhenConnectorStopFails() throws Exception {
		Connector connector = mock(Connector.class);
		IllegalStateException failure = new IllegalStateException("stop failure");
		doThrow(failure).when(connector).stop(false);
		ConnectorManager connectorManager = new ConnectorManager(connector);
		Runnable callback = mock(Runnable.class);
		doAnswer(invocation -> {
			assertThat(connectorManager.isRunning()).isFalse();
			return null;
		}).when(callback).run();
		connectorManager.start();

		assertThatThrownBy(() -> connectorManager.stop(callback)).isSameAs(failure);
		assertThat(connectorManager.isRunning()).isFalse();
		connectorManager.stop();
		verify(connector, times(1)).stop(false);
		verify(callback, times(1)).run();
	}

	@Test
	public void shouldSerializeRestartUntilStopCallbackCompletes() throws Exception {
		Connector connector = mock(Connector.class);
		AtomicInteger startInvocationCount = new AtomicInteger();
		CountDownLatch restartDelegateInvoked = new CountDownLatch(1);
		doAnswer(invocation -> {
			if (startInvocationCount.incrementAndGet() == 2) {
				restartDelegateInvoked.countDown();
			}
			return null;
		}).when(connector).start();
		ConnectorManager connectorManager = new ConnectorManager(connector);
		connectorManager.start();

		CountDownLatch callbackStarted = new CountDownLatch(1);
		CountDownLatch releaseCallback = new CountDownLatch(1);
		Runnable callback = () -> {
			callbackStarted.countDown();
			try {
				if (!releaseCallback.await(5, TimeUnit.SECONDS)) {
					throw new AssertionError("Timed out waiting to release stop callback");
				}
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
				throw new AssertionError("Interrupted while waiting to release stop callback", ex);
			}
		};
		ExecutorService executor = Executors.newFixedThreadPool(2);

		try {
			Future<?> stopFuture = executor.submit(() -> connectorManager.stop(callback));
			assertThat(callbackStarted.await(5, TimeUnit.SECONDS)).isTrue();

			CountDownLatch restartTaskStarted = new CountDownLatch(1);
			Future<?> restartFuture = executor.submit(() -> {
				restartTaskStarted.countDown();
				connectorManager.start();
			});
			assertThat(restartTaskStarted.await(5, TimeUnit.SECONDS)).isTrue();
			assertThat(restartDelegateInvoked.await(200, TimeUnit.MILLISECONDS)).isFalse();
			assertThat(restartFuture.isDone()).isFalse();

			releaseCallback.countDown();
			stopFuture.get(5, TimeUnit.SECONDS);
			restartFuture.get(5, TimeUnit.SECONDS);

			assertThat(restartDelegateInvoked.getCount()).isZero();
			assertThat(connectorManager.isRunning()).isTrue();
			verify(connector, times(2)).start();
		} finally {
			releaseCallback.countDown();
			executor.shutdownNow();
		}
	}

	@Test
	public void shouldPropagateCallbackFailureAfterSuccessfulStop() throws Exception {
		Connector connector = mock(Connector.class);
		ConnectorManager connectorManager = new ConnectorManager(connector);
		IllegalStateException callbackFailure = new IllegalStateException("callback failure");
		Runnable callback = mock(Runnable.class);
		doThrow(callbackFailure).when(callback).run();
		connectorManager.start();

		assertThatThrownBy(() -> connectorManager.stop(callback)).isSameAs(callbackFailure);
		assertThat(connectorManager.isRunning()).isFalse();
		verify(callback, times(1)).run();
	}

	@Test
	public void shouldSuppressCallbackFailureWhenConnectorStopAlsoFails() throws Exception {
		Connector connector = mock(Connector.class);
		IllegalStateException stopFailure = new IllegalStateException("stop failure");
		doThrow(stopFailure).when(connector).stop(false);
		ConnectorManager connectorManager = new ConnectorManager(connector);
		IllegalArgumentException callbackFailure = new IllegalArgumentException("callback failure");
		Runnable callback = mock(Runnable.class);
		doThrow(callbackFailure).when(callback).run();
		connectorManager.start();

		assertThatThrownBy(() -> connectorManager.stop(callback))
				.isSameAs(stopFailure)
				.satisfies(thrown -> assertThat(thrown.getSuppressed()).containsExactly(callbackFailure));
		assertThat(connectorManager.isRunning()).isFalse();
		verify(callback, times(1)).run();
	}

	@Test
	public void shouldNotSuppressFailureWhenStopAndCallbackThrowSameInstance() throws Exception {
		Connector connector = mock(Connector.class);
		IllegalStateException failure = new IllegalStateException("shared failure");
		doThrow(failure).when(connector).stop(false);
		ConnectorManager connectorManager = new ConnectorManager(connector);
		Runnable callback = mock(Runnable.class);
		doThrow(failure).when(callback).run();
		connectorManager.start();

		assertThatThrownBy(() -> connectorManager.stop(callback))
				.isSameAs(failure)
				.satisfies(thrown -> assertThat(thrown.getSuppressed()).isEmpty());
		assertThat(connectorManager.isRunning()).isFalse();
		verify(callback, times(1)).run();
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
