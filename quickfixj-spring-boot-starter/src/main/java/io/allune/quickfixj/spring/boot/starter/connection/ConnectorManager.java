/*
 * Copyright 2017-2020 the original author or authors.
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import org.springframework.util.Assert;
import quickfix.ConfigError;
import quickfix.Connector;
import quickfix.RuntimeError;

/**
 * Connection manager for a connector. The connection is initialised when the application context is created and closed
 * (including logging out all active sessions) when the application context is closed
 *
 * @author Eduardo Sanchez-Ros
 */
@Slf4j
public class ConnectorManager implements SmartLifecycle {

	private final Connector connector;

	private final Object lifecycleMonitor = new Object();

	private boolean autoStartup = true;

	private int phase = Integer.MAX_VALUE;

	private boolean running = false;

	public ConnectorManager(Connector connector) {
		Assert.notNull(connector, "'connector' must not be null");
		this.connector = connector;
	}

	/**
	 * Set whether to auto-connect to the remote endpoint after this connector manager
	 * has been initialized and the Spring context has been refreshed.
	 * <p>Default is "true".
	 *
	 * @param autoStartup Whether the connector should be automatically started
	 */
	public void setAutoStartup(boolean autoStartup) {
		this.autoStartup = autoStartup;
	}

	/**
	 * Return the value for the 'autoStartup' property.
	 */
	@Override
	public boolean isAutoStartup() {
		return this.autoStartup;
	}

	/**
	 * Specify the phase in which this connection manager should be started and stopped.
	 *
	 * @param phase The phase number
	 */
	public void setPhase(int phase) {
		this.phase = phase;
	}

	/**
	 * Return the phase in which this connector manager will be started and stopped.
	 */
	@Override
	public int getPhase() {
		return this.phase;
	}

	/**
	 * Start the connector, accepting new connections
	 */
	@Override
	public void start() {
		synchronized (this.lifecycleMonitor) {
			if (!isRunning()) {
				log.info("start: Starting ConnectorManager");
				try {
					connector.start();
				} catch (ConfigError | RuntimeError ex) {
					throw new ConfigurationException(ex.getMessage(), ex);
				} catch (Throwable ex) {
					throw new IllegalStateException("Could not start the connector", ex);
				}

				running = true;
			}
		}
	}

	/**
	 * Stop this connector, logging out existing sessions, closing their connections, and stopping to accept new
	 * connections.
	 */
	@Override
	public void stop() {
		synchronized (this.lifecycleMonitor) {
			if (isRunning()) {
				log.info("stop: Stopping ConnectorManager");
				try {
					connector.stop();
				} finally {
					running = false;
				}
			}
		}
	}

	/**
	 * Stop this connector, invoking the specific callback once all the sessions have been logged out, all connections
	 * closed and it has stopped accepting new connections.
	 */
	@Override
	public void stop(Runnable callback) {
		synchronized (this.lifecycleMonitor) {
			stop();
			callback.run();
		}
	}

	/**
	 * Determine whether this connector is currently running,
	 * that is, whether it has been started and not stopped yet.
	 *
	 * @see #start()
	 * @see #stop()
	 */
	@Override
	public boolean isRunning() {
		synchronized (this.lifecycleMonitor) {
			return running;
		}
	}
}