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
package io.allune.quickfixj.spring.boot.starter.autoconfigure;

import io.allune.quickfixj.spring.boot.starter.connection.ConnectorManager;
import io.allune.quickfixj.spring.boot.starter.connection.SessionSettingsLocator;
import lombok.Data;
import org.springframework.boot.context.properties.DeprecatedConfigurationProperty;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Defines the configuration for the {@link ConnectorManager} for the Client (Initiator) and Server (Acceptor).
 *
 * @author Eduardo Sanchez-Ros
 */
@Data
public class ConnectorConfig {

	/**
	 * Whether to enable the autoconfiguration of the QuickFIX/J client/server.
	 */
	private boolean enabled;

	/**
	 * Whether the {@link ConnectorManager} should get started automatically.
	 */
	private boolean autoStartup = true;

	/**
	 * The phase value of the {@link ConnectorManager}.
	 */
	private int phase = Integer.MAX_VALUE;

	/**
	 * The location of the configuration file to use to initialize QuickFIX/J client/server.
	 */
	private String config;

	/**
	 * Whether to resolve {@code ${...}} placeholders against the Spring {@link org.springframework.core.env.Environment}
	 * when loading the configuration from {@link #config the configuration file}. Unresolvable placeholders cause
	 * startup to fail. This flag has no effect on {@link #configString}, since values bound from
	 * {@code application.yml}/properties are already resolved during property binding.
	 *
	 * @see SessionSettingsLocator#loadSettings(String...)
	 */
	private boolean resolvePlaceholders = false;

	/**
	 * The configuration string to use to initialize QuickFIX/J client/server.
	 */
	private String configString;

	/**
	 * Whether to register the Jmx MBeans for the client/server.
	 */
	private boolean jmxEnabled = false;

	/**
	 * Configures the concurrent options.
	 */
	@NestedConfigurationProperty
	private Concurrent concurrent = new Concurrent();

	/**
	 * Configures the message store factory to use.
	 */
	private MessageStoreMethod messageStoreFactory = MessageStoreMethod.MEMORY;

	/**
	 * Retains the accidentally advertised message store method property for
	 * compatibility. This property does not control auto-configuration.
	 */
	private MessageStoreMethod messageStoreMethod = MessageStoreMethod.MEMORY;

	/**
	 * Configures the log store factory to use.
	 */
	private LogMethod logFactory = LogMethod.SCREEN;

	/**
	 * Retains the accidentally advertised log method property for compatibility.
	 * This property does not control auto-configuration.
	 */
	private LogMethod logMethod = LogMethod.SCREEN;

	/**
	 * Configures if sessions should be disconnected forcibly when the connector is stopped.
	 */
	private boolean forceDisconnect = false;

	/**
	 * Configures the actuator health options.
	 */
	@NestedConfigurationProperty
	private Health actuator = new Health();

	/**
	 * Retains the accidentally advertised health property for compatibility. This
	 * property does not control actuator auto-configuration.
	 */
	@NestedConfigurationProperty
	private Health health = new Health();

	/**
	 * Returns the message store factory selected by
	 * {@code quickfixj.[client|server].message-store-factory}.
	 *
	 * @return the message store factory type
	 */
	public MessageStoreMethod getMessageStoreFactory() {
		return messageStoreFactory;
	}

	/**
	 * Sets the message store factory selected by
	 * {@code quickfixj.[client|server].message-store-factory}.
	 *
	 * @param messageStoreFactory the message store factory type
	 */
	public void setMessageStoreFactory(MessageStoreMethod messageStoreFactory) {
		this.messageStoreFactory = messageStoreFactory;
	}

	/**
	 * @return the configured message store factory type
	 * @deprecated Use {@link #getMessageStoreFactory()}.
	 */
	@Deprecated(since = "4.4.0", forRemoval = true)
	@DeprecatedConfigurationProperty(
			reason = "The generated property name did not control message store auto-configuration.",
			since = "4.4.0")
	public MessageStoreMethod getMessageStoreMethod() {
		return messageStoreMethod;
	}

	/**
	 * @param messageStoreMethod the message store factory type
	 * @deprecated Use {@link #setMessageStoreFactory(MessageStoreMethod)}.
	 */
	@Deprecated(since = "4.4.0", forRemoval = true)
	public void setMessageStoreMethod(MessageStoreMethod messageStoreMethod) {
		this.messageStoreMethod = messageStoreMethod;
	}

	/**
	 * Returns the log factory selected by
	 * {@code quickfixj.[client|server].log-factory}.
	 *
	 * @return the log factory type
	 */
	public LogMethod getLogFactory() {
		return logFactory;
	}

	/**
	 * Sets the log factory selected by
	 * {@code quickfixj.[client|server].log-factory}.
	 *
	 * @param logFactory the log factory type
	 */
	public void setLogFactory(LogMethod logFactory) {
		this.logFactory = logFactory;
	}

	/**
	 * @return the configured log factory type
	 * @deprecated Use {@link #getLogFactory()}.
	 */
	@Deprecated(since = "4.4.0", forRemoval = true)
	@DeprecatedConfigurationProperty(
			reason = "The generated property name did not control log factory auto-configuration.",
			since = "4.4.0")
	public LogMethod getLogMethod() {
		return logMethod;
	}

	/**
	 * @param logMethod the log factory type
	 * @deprecated Use {@link #setLogFactory(LogMethod)}.
	 */
	@Deprecated(since = "4.4.0", forRemoval = true)
	public void setLogMethod(LogMethod logMethod) {
		this.logMethod = logMethod;
	}

	/**
	 * Returns the actuator options configured by
	 * {@code quickfixj.[client|server].actuator}.
	 *
	 * @return the actuator options
	 */
	public Health getActuator() {
		return actuator;
	}

	/**
	 * Sets the actuator options configured by
	 * {@code quickfixj.[client|server].actuator}.
	 *
	 * @param actuator the actuator options
	 */
	public void setActuator(Health actuator) {
		this.actuator = actuator;
	}

	/**
	 * @return the actuator options
	 * @deprecated Use {@link #getActuator()}.
	 */
	@Deprecated(since = "4.4.0", forRemoval = true)
	@DeprecatedConfigurationProperty(
			reason = "The generated health property did not enable the actuator integration.",
			since = "4.4.0")
	public Health getHealth() {
		return health;
	}

	/**
	 * @param health the actuator options
	 * @deprecated Use {@link #setActuator(Health)}.
	 */
	@Deprecated(since = "4.4.0", forRemoval = true)
	public void setHealth(Health health) {
		this.health = health;
	}
}
