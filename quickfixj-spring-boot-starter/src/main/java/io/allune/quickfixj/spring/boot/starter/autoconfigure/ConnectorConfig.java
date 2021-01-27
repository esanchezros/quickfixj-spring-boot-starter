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
package io.allune.quickfixj.spring.boot.starter.autoconfigure;

import io.allune.quickfixj.spring.boot.starter.connection.ConnectorManager;
import lombok.Getter;
import lombok.Setter;

/**
 * Defines the configuration for the {@link ConnectorManager} for the Initiator and Acceptor
 *
 * @author Eduardo Sanchez-Ros
 */
@Getter
@Setter
public class ConnectorConfig {

	/**
	 * Whether the {@link ConnectorManager} should get started automatically
	 */
	private boolean autoStartup = true;

	/**
	 * The phase value of the {@link ConnectorManager}.
	 */
	private int phase = Integer.MAX_VALUE;

	/**
	 * The location of the configuration file to use to initialize QuickFIX/J client.
	 */
	private String config;

	/**
	 * Whether to register the Jmx MBeans for the client
	 */
	private boolean jmxEnabled = false;

	/**
	 * Configures the concurrent options
	 */
	private Concurrent concurrent = new Concurrent();

	/**
	 * Configures the message store factory to use
	 */
	private MessageStoreMethod messageStoreMethod = MessageStoreMethod.MEMORY;

	/**
	 * Configures the log store factory to use
	 */
	private LogMethod logMethod = LogMethod.SCREEN;

	/**
	 * Configures if sessions should be disconnected forcibly when the connector is stopped
	 */
	private boolean forceDisconnect = false;

	/**
	 * Defines the threading model that the {@link quickfix.Connector} should use.
	 *
	 * @author Eduardo Sanchez-Ros
	 */
	@Getter
	@Setter
	public static class Concurrent {

		/**
		 * Whether the connector to create should use a separate thread per session ({@code enabled=true}) or a
		 * single thread ({@code enabled=false}) to process messages.
		 */
		private boolean enabled = false;
	}
}