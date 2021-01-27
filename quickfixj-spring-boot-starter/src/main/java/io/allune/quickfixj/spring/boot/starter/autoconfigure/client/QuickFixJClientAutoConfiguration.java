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
package io.allune.quickfixj.spring.boot.starter.autoconfigure.client;

import io.allune.quickfixj.spring.boot.starter.application.EventPublisherApplicationAdapter;
import io.allune.quickfixj.spring.boot.starter.autoconfigure.QuickFixJBootProperties;
import io.allune.quickfixj.spring.boot.starter.connection.ConnectorManager;
import io.allune.quickfixj.spring.boot.starter.connection.SessionSettingsLocator;
import io.allune.quickfixj.spring.boot.starter.exception.ConfigurationException;
import org.quickfixj.jmx.JmxExporter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import quickfix.Application;
import quickfix.CachedFileStoreFactory;
import quickfix.ConfigError;
import quickfix.DefaultMessageFactory;
import quickfix.FileLogFactory;
import quickfix.FileStoreFactory;
import quickfix.Initiator;
import quickfix.JdbcLogFactory;
import quickfix.JdbcStoreFactory;
import quickfix.LogFactory;
import quickfix.MemoryStoreFactory;
import quickfix.MessageFactory;
import quickfix.MessageStoreFactory;
import quickfix.NoopStoreFactory;
import quickfix.SLF4JLogFactory;
import quickfix.ScreenLogFactory;
import quickfix.SessionSettings;
import quickfix.SleepycatStoreFactory;
import quickfix.SocketInitiator;
import quickfix.ThreadedSocketInitiator;

import javax.management.ObjectName;

import static org.quickfixj.jmx.JmxExporter.REGISTRATION_REPLACE_EXISTING;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for QuickFix Client (Initiator)
 *
 * @author Eduardo Sanchez-Ros
 */
@Configuration
@EnableConfigurationProperties(QuickFixJBootProperties.class)
@ConditionalOnBean(QuickFixJClientMarkerConfiguration.Marker.class)
public class QuickFixJClientAutoConfiguration {

	private static final String SYSTEM_VARIABLE_QUICKFIXJ_CLIENT_CONFIG = "quickfixj.client.config";

	private static final String QUICKFIXJ_CLIENT_CONFIG = "quickfixj-client.cfg";

	/**
	 * Creates the default client's {@link SessionSettings session settings} bean used in the creation of the
	 * {@link Initiator initiator} connector
	 *
	 * @param properties The {@link QuickFixJBootProperties QuickFix/J Spring Boot properties}
	 * @return The client's {@link SessionSettings session settings} bean
	 */
	@Bean(name = "clientSessionSettings")
	@ConditionalOnMissingBean(name = "clientSessionSettings")
	public SessionSettings clientSessionSettings(QuickFixJBootProperties properties) {
		return SessionSettingsLocator.loadSettings(properties.getClient().getConfig(),
				System.getProperty(SYSTEM_VARIABLE_QUICKFIXJ_CLIENT_CONFIG),
				"file:./" + QUICKFIXJ_CLIENT_CONFIG,
				"classpath:/" + QUICKFIXJ_CLIENT_CONFIG);
	}

	/**
	 * Creates the default client's {@link Application application} bean used in the creation of the
	 * {@link Initiator initiator} connector
	 *
	 * @param applicationEventPublisher Spring's default {@link ApplicationEventPublisher}
	 * @return The default client's {@link Application application} bean
	 */
	@Bean
	@ConditionalOnMissingBean(name = "clientApplication")
	public Application clientApplication(ApplicationEventPublisher applicationEventPublisher) {
		return new EventPublisherApplicationAdapter(applicationEventPublisher);
	}

	/**
	 * Grouping the creation of the client's {@link MessageStoreFactory}
	 */
	@Configuration
	static class MessageStoreFactoryConfiguration {

		/**
		 * Creates the client's {@link MessageStoreFactory} of type {@link CachedFileStoreFactory} if
		 * {@code quickfixj.client.message-store-factory} is set to {@code cachedfile}, used in the creation of the
		 * {@link Initiator initiator} connector
		 *
		 * @param clientSessionSettings The client's {@link SessionSettings session settings} bean
		 * @return The client's {@link MessageStoreFactory}
		 */
		@Bean(name = "clientMessageStoreFactory")
		@ConditionalOnMissingBean(name = "clientMessageStoreFactory")
		@ConditionalOnProperty(prefix = "quickfixj.client", name = "message-store-factory", havingValue = "cachedfile")
		public MessageStoreFactory clientCachedFileStoreFactory(SessionSettings clientSessionSettings) {
			return new CachedFileStoreFactory(clientSessionSettings);
		}

		/**
		 * Creates the client's {@link MessageStoreFactory} of type {@link FileStoreFactory} if
		 * {@code quickfixj.client.message-store-factory} is set to {@code file}, used in the creation of the
		 * {@link Initiator initiator} connector
		 *
		 * @param clientSessionSettings The client's {@link SessionSettings session settings} bean
		 * @return The client's {@link MessageStoreFactory}
		 */
		@Bean(name = "clientMessageStoreFactory")
		@ConditionalOnMissingBean(name = "clientMessageStoreFactory")
		@ConditionalOnProperty(prefix = "quickfixj.client", name = "message-store-factory", havingValue = "file")
		public MessageStoreFactory clientFileStoreFactory(SessionSettings clientSessionSettings) {
			return new FileStoreFactory(clientSessionSettings);
		}

		/**
		 * Creates the client's {@link MessageStoreFactory} of type {@link JdbcStoreFactory} if
		 * {@code quickfixj.client.message-store-factory} is set to {@code jdbc}, used in the creation of the
		 * {@link Initiator initiator} connector
		 *
		 * @param clientSessionSettings The client's {@link SessionSettings session settings} bean
		 * @return The client's {@link MessageStoreFactory}
		 */
		@Bean(name = "clientMessageStoreFactory")
		@ConditionalOnMissingBean(name = "clientMessageStoreFactory")
		@ConditionalOnProperty(prefix = "quickfixj.client", name = "message-store-factory", havingValue = "jdbc")
		public MessageStoreFactory clientJdbcStoreFactory(SessionSettings clientSessionSettings) {
			return new JdbcStoreFactory(clientSessionSettings);
		}

		/**
		 * Creates the client's {@link MessageStoreFactory} of type {@link MemoryStoreFactory} if
		 * {@code quickfixj.client.message-store-factory} is set to {@code memory}, used in the creation of the
		 * {@link Initiator initiator} connector
		 *
		 * @return The client's {@link MessageStoreFactory}
		 */
		@Bean(name = "clientMessageStoreFactory")
		@ConditionalOnMissingBean(name = "clientMessageStoreFactory")
		@ConditionalOnProperty(prefix = "quickfixj.client", name = "message-store-factory", havingValue = "memory", matchIfMissing = true)
		public MessageStoreFactory clientMemoryStoreFactory() {
			return new MemoryStoreFactory();
		}

		/**
		 * Creates the client's {@link MessageStoreFactory} of type {@link NoopStoreFactory} if
		 * {@code quickfixj.client.message-store-factory} is set to {@code noop}, used in the creation of the
		 * {@link Initiator initiator} connector
		 *
		 * @return The client's {@link MessageStoreFactory}
		 */
		@Bean(name = "clientMessageStoreFactory")
		@ConditionalOnMissingBean(name = "clientMessageStoreFactory")
		@ConditionalOnProperty(prefix = "quickfixj.client", name = "message-store-factory", havingValue = "noop")
		public MessageStoreFactory clientNoopStoreFactory() {
			return new NoopStoreFactory();
		}

		/**
		 * Creates the client's {@link MessageStoreFactory} of type {@link SleepycatStoreFactory} if
		 * {@code quickfixj.client.message-store-factory} is set to {@code sleepycat}, used in the creation of the
		 * {@link Initiator initiator} connector
		 *
		 * @param clientSessionSettings The client's {@link SessionSettings session settings} bean
		 * @return The client's {@link MessageStoreFactory}
		 */
		@Bean(name = "clientMessageStoreFactory")
		@ConditionalOnMissingBean(name = "clientMessageStoreFactory")
		@ConditionalOnProperty(prefix = "quickfixj.client", name = "message-store-factory", havingValue = "sleepycat")
		public MessageStoreFactory clientSleepycatStoreFactory(SessionSettings clientSessionSettings) {
			return new SleepycatStoreFactory(clientSessionSettings);
		}
	}

	/**
	 * Grouping the creation of the client's {@link LogFactory}
	 */
	@Configuration
	static class LogFactoryConfiguration {

		/**
		 * Creates the client's {@link LogFactory} of type {@link FileLogFactory} if
		 * {@code quickfixj.client.log-factory} is set to {@code file}, used in the creation of the
		 * {@link Initiator initiator} connector
		 *
		 * @param clientSessionSettings The client's {@link SessionSettings session settings} bean
		 * @return The client's {@link LogFactory}
		 */
		@Bean(name = "clientLogFactory")
		@ConditionalOnMissingBean(name = "clientLogFactory")
		@ConditionalOnProperty(prefix = "quickfixj.client", name = "log-factory", havingValue = "file")
		public LogFactory clientFileLogFactory(SessionSettings clientSessionSettings) {
			return new FileLogFactory(clientSessionSettings);
		}

		/**
		 * Creates the client's {@link LogFactory} of type {@link JdbcLogFactory} if
		 * {@code quickfixj.client.log-factory} is set to {@code jdbc}, used in the creation of the
		 * {@link Initiator initiator} connector
		 *
		 * @param clientSessionSettings The client's {@link SessionSettings session settings} bean
		 * @return The client's {@link LogFactory}
		 */
		@Bean(name = "clientLogFactory")
		@ConditionalOnMissingBean(name = "clientLogFactory")
		@ConditionalOnProperty(prefix = "quickfixj.client", name = "log-factory", havingValue = "jdbc")
		public LogFactory clientJdbcLogFactory(SessionSettings clientSessionSettings) {
			return new JdbcLogFactory(clientSessionSettings);
		}

		/**
		 * Creates the client's {@link LogFactory} of type {@link SLF4JLogFactory} if
		 * {@code quickfixj.client.log-factory} is set to {@code slf4j}, used in the creation of the
		 * {@link Initiator initiator} connector
		 *
		 * @param clientSessionSettings The client's {@link SessionSettings session settings} bean
		 * @return The client's {@link LogFactory}
		 */
		@Bean(name = "clientLogFactory")
		@ConditionalOnMissingBean(name = "clientLogFactory")
		@ConditionalOnProperty(prefix = "quickfixj.client", name = "log-factory", havingValue = "slf4j")
		public LogFactory clientSlf4jLogFactory(SessionSettings clientSessionSettings) {
			return new SLF4JLogFactory(clientSessionSettings);
		}

		/**
		 * Creates the client's {@link LogFactory} of type {@link ScreenLogFactory} if
		 * {@code quickfixj.client.log-factory} is set to {@code screen}, used in the creation of the
		 * {@link Initiator initiator} connector
		 *
		 * @param clientSessionSettings The client's {@link SessionSettings session settings} bean
		 * @return The client's {@link LogFactory}
		 */
		@Bean(name = "clientLogFactory")
		@ConditionalOnMissingBean(name = "clientLogFactory")
		@ConditionalOnProperty(prefix = "quickfixj.client", name = "log-factory", havingValue = "screen", matchIfMissing = true)
		public LogFactory clientScreenLogFactory(SessionSettings clientSessionSettings) {
			return new ScreenLogFactory(clientSessionSettings);
		}
	}

	/**
	 * Creates the default client's {@link MessageFactory}
	 *
	 * @return The default client's {@link MessageFactory application} bean
	 */
	@Bean
	@ConditionalOnMissingBean(name = "clientMessageFactory")
	public MessageFactory clientMessageFactory() {
		return new DefaultMessageFactory();
	}

	@Configuration
	public static class SocketInitiatorConfiguration {

		/**
		 * Creates a single threaded {@link Initiator} bean
		 *
		 * @param clientApplication         The client's {@link Application}
		 * @param clientMessageStoreFactory The client's {@link MessageStoreFactory}
		 * @param clientSessionSettings     The client's {@link SessionSettings}
		 * @param clientLogFactory          The client's {@link LogFactory}
		 * @param clientMessageFactory      The client's {@link MessageFactory}
		 * @return The client's {@link Initiator}
		 * @throws ConfigError exception thrown when a configuration error is detected
		 */
		@Bean
		@ConditionalOnMissingBean
		@ConditionalOnProperty(prefix = "quickfixj.client.concurrent", name = "enabled", havingValue = "false", matchIfMissing = true)
		public Initiator clientInitiator(Application clientApplication, MessageStoreFactory clientMessageStoreFactory,
		                                 SessionSettings clientSessionSettings, LogFactory clientLogFactory, MessageFactory clientMessageFactory) throws ConfigError {

			return SocketInitiator.newBuilder()
					.withApplication(clientApplication)
					.withMessageStoreFactory(clientMessageStoreFactory)
					.withSettings(clientSessionSettings)
					.withLogFactory(clientLogFactory)
					.withMessageFactory(clientMessageFactory)
					.build();
		}
	}

	@Configuration
	public static class ThreadedSocketInitiatorConfiguration {

		/**
		 * Creates a multi threaded {@link Initiator} bean
		 *
		 * @param clientApplication         The client's {@link Application}
		 * @param clientMessageStoreFactory The client's {@link MessageStoreFactory}
		 * @param clientSessionSettings     The client's {@link SessionSettings}
		 * @param clientLogFactory          The client's {@link LogFactory}
		 * @param clientMessageFactory      The client's {@link MessageFactory}
		 * @return The client's {@link Initiator}
		 * @throws ConfigError exception thrown when a configuration error is detected
		 */
		@Bean
		@ConditionalOnMissingBean
		@ConditionalOnProperty(prefix = "quickfixj.client.concurrent", name = "enabled", havingValue = "true")
		public Initiator clientInitiator(Application clientApplication, MessageStoreFactory clientMessageStoreFactory,
		                                 SessionSettings clientSessionSettings, LogFactory clientLogFactory, MessageFactory clientMessageFactory) throws ConfigError {

			return ThreadedSocketInitiator.newBuilder()
					.withApplication(clientApplication)
					.withMessageStoreFactory(clientMessageStoreFactory)
					.withSettings(clientSessionSettings)
					.withLogFactory(clientLogFactory)
					.withMessageFactory(clientMessageFactory)
					.build();
		}
	}

	/**
	 * Creates the client's {@link ConnectorManager}
	 *
	 * @param clientInitiator   The client's {@link Initiator}
	 * @param properties        The {@link QuickFixJBootProperties} properties
	 * @return The client's {@link ConnectorManager}
	 */
	@Bean
	public ConnectorManager clientConnectorManager(Initiator clientInitiator, QuickFixJBootProperties properties) {
		ConnectorManager connectorManager = new ConnectorManager(clientInitiator);
		if (properties.getClient() != null) {
			connectorManager.setAutoStartup(properties.getClient().isAutoStartup());
			connectorManager.setPhase(properties.getClient().getPhase());
			connectorManager.setForceDisconnect(properties.getClient().isForceDisconnect());
		}
		return connectorManager;
	}

	/**
	 * Creates the client's JMX Bean
	 *
	 * @param clientInitiator The client's {@link Initiator}
	 * @return The client's JMX bean
	 */
	@Bean
	@ConditionalOnProperty(prefix = "quickfixj.client", name = "jmx-enabled", havingValue = "true")
	@ConditionalOnClass(JmxExporter.class)
	@ConditionalOnSingleCandidate(Initiator.class)
	@ConditionalOnMissingBean(name = "clientInitiatorMBean")
	public ObjectName clientInitiatorMBean(Initiator clientInitiator) {
		try {
			JmxExporter exporter = new JmxExporter();
			exporter.setRegistrationBehavior(REGISTRATION_REPLACE_EXISTING);
			return exporter.register(clientInitiator);
		} catch (Exception e) {
			throw new ConfigurationException(e.getMessage(), e);
		}
	}
}
