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
package io.allune.quickfixj.spring.boot.starter.autoconfigure.server;

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
import quickfix.Acceptor;
import quickfix.Application;
import quickfix.CachedFileStoreFactory;
import quickfix.ConfigError;
import quickfix.DefaultMessageFactory;
import quickfix.FileLogFactory;
import quickfix.FileStoreFactory;
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
import quickfix.SocketAcceptor;
import quickfix.ThreadedSocketAcceptor;

import javax.management.ObjectName;

import static org.quickfixj.jmx.JmxExporter.REGISTRATION_REPLACE_EXISTING;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for QuickFIX/J Server (Acceptor)
 *
 * @author Eduardo Sanchez-Ros
 */
@Configuration
@EnableConfigurationProperties(QuickFixJBootProperties.class)
@ConditionalOnBean(value = QuickFixJServerMarkerConfiguration.Marker.class)
public class QuickFixJServerAutoConfiguration {

	private static final String SYSTEM_VARIABLE_QUICKFIXJ_SERVER_CONFIG = "quickfixj.server.config";

	private static final String QUICKFIXJ_SERVER_CONFIG = "quickfixj-server.cfg";

	/**
	 * Creates the default server's {@link SessionSettings session settings} bean used in the creation of the
	 * {@link Acceptor acceptor} connector
	 *
	 * @param properties The {@link QuickFixJBootProperties QuickFix/J Spring Boot properties}
	 * @return The server's {@link SessionSettings session settings} bean
	 */
	@Bean
	@ConditionalOnMissingBean(name = "serverSessionSettings")
	public SessionSettings serverSessionSettings(QuickFixJBootProperties properties) {
		return SessionSettingsLocator.loadSettings(properties.getServer().getConfig(),
				System.getProperty(SYSTEM_VARIABLE_QUICKFIXJ_SERVER_CONFIG),
				"file:./" + QUICKFIXJ_SERVER_CONFIG,
				"classpath:/" + QUICKFIXJ_SERVER_CONFIG);
	}

	/**
	 * Creates the default server's {@link Application application} bean used in the creation of the
	 * {@link Acceptor acceptor} connector
	 *
	 * @param applicationEventPublisher Spring's default {@link ApplicationEventPublisher}
	 * @return The default server's {@link Application application} bean
	 */
	@Bean
	@ConditionalOnMissingBean(name = "serverApplication")
	public Application serverApplication(ApplicationEventPublisher applicationEventPublisher) {
		return new EventPublisherApplicationAdapter(applicationEventPublisher);
	}

	/**
	 * Grouping the creation of the server's {@link MessageStoreFactory}
	 */
	@Configuration
	static class MessageStoreFactoryConfiguration {

		/**
		 * Creates the server's {@link MessageStoreFactory} of type {@link CachedFileStoreFactory} if
		 * {@code quickfixj.server.message-store-factory} is set to {@code cachedfile}, used in the creation of the
		 * {@link Acceptor acceptor} connector
		 *
		 * @param serverSessionSettings The server's {@link SessionSettings session settings} bean
		 * @return The server's {@link MessageStoreFactory}
		 */
		@Bean(name = "serverMessageStoreFactory")
		@ConditionalOnMissingBean(name = "serverMessageStoreFactory")
		@ConditionalOnProperty(prefix = "quickfixj.server", name = "message-store-factory", havingValue = "cachedfile")
		public MessageStoreFactory serverCachedFileStoreFactory(SessionSettings serverSessionSettings) {
			return new CachedFileStoreFactory(serverSessionSettings);
		}

		/**
		 * Creates the server's {@link MessageStoreFactory} of type {@link FileStoreFactory} if
		 * {@code quickfixj.server.message-store-factory} is set to {@code file}, used in the creation of the
		 * {@link Acceptor acceptor} connector
		 *
		 * @param serverSessionSettings The server's {@link SessionSettings session settings} bean
		 * @return The server's {@link MessageStoreFactory}
		 */
		@Bean(name = "serverMessageStoreFactory")
		@ConditionalOnMissingBean(name = "serverMessageStoreFactory")
		@ConditionalOnProperty(prefix = "quickfixj.server", name = "message-store-factory", havingValue = "file")
		public MessageStoreFactory serverFileStoreFactory(SessionSettings serverSessionSettings) {
			return new FileStoreFactory(serverSessionSettings);
		}

		/**
		 * Creates the server's {@link MessageStoreFactory} of type {@link JdbcStoreFactory} if
		 * {@code quickfixj.server.message-store-factory} is set to {@code jdbc}, used in the creation of the
		 * {@link Acceptor acceptor} connector
		 *
		 * @param serverSessionSettings The server's {@link SessionSettings session settings} bean
		 * @return The server's {@link MessageStoreFactory}
		 */
		@Bean(name = "serverMessageStoreFactory")
		@ConditionalOnMissingBean(name = "serverMessageStoreFactory")
		@ConditionalOnProperty(prefix = "quickfixj.server", name = "message-store-factory", havingValue = "jdbc")
		public MessageStoreFactory serverJdbcStoreFactory(SessionSettings serverSessionSettings) {
			return new JdbcStoreFactory(serverSessionSettings);
		}

		/**
		 * Creates the server's {@link MessageStoreFactory} of type {@link MemoryStoreFactory} if
		 * {@code quickfixj.server.message-store-factory} is set to {@code memory}, used in the creation of the
		 * {@link Acceptor acceptor} connector
		 *
		 * @return The server's {@link MessageStoreFactory}
		 */
		@Bean(name = "serverMessageStoreFactory")
		@ConditionalOnMissingBean(name = "serverMessageStoreFactory")
		@ConditionalOnProperty(prefix = "quickfixj.server", name = "message-store-factory", havingValue = "memory", matchIfMissing = true)
		public MessageStoreFactory serverMemoryStoreFactory() {
			return new MemoryStoreFactory();
		}

		/**
		 * Creates the server's {@link MessageStoreFactory} of type {@link NoopStoreFactory} if
		 * {@code quickfixj.server.message-store-factory} is set to {@code noop}, used in the creation of the
		 * {@link Acceptor acceptor} connector
		 *
		 * @return The server's {@link MessageStoreFactory}
		 */
		@Bean(name = "serverMessageStoreFactory")
		@ConditionalOnMissingBean(name = "serverMessageStoreFactory")
		@ConditionalOnProperty(prefix = "quickfixj.server", name = "message-store-factory", havingValue = "noop")
		public MessageStoreFactory serverNoopStoreFactory() {
			return new NoopStoreFactory();
		}

		/**
		 * Creates the server's {@link MessageStoreFactory} of type {@link SleepycatStoreFactory} if
		 * {@code quickfixj.server.message-store-factory} is set to {@code sleepycat}, used in the creation of the
		 * {@link Acceptor acceptor} connector
		 *
		 * @param serverSessionSettings The server's {@link SessionSettings session settings} bean
		 * @return The server's {@link MessageStoreFactory}
		 */
		@Bean(name = "serverMessageStoreFactory")
		@ConditionalOnMissingBean(name = "serverMessageStoreFactory")
		@ConditionalOnProperty(prefix = "quickfixj.server", name = "message-store-factory", havingValue = "sleepycat")
		public MessageStoreFactory serverSleepycatStoreFactory(SessionSettings serverSessionSettings) {
			return new SleepycatStoreFactory(serverSessionSettings);
		}
	}

	/**
	 * Grouping the creation of the server's {@link LogFactory}
	 */
	@Configuration
	static class LogFactoryConfiguration {

		/**
		 * Creates the server's {@link LogFactory} of type {@link FileLogFactory} if
		 * {@code quickfixj.server.log-factory} is set to {@code file}, used in the creation of the
		 * {@link Acceptor acceptor} connector
		 *
		 * @param serverSessionSettings The server's {@link SessionSettings session settings} bean
		 * @return The server's {@link LogFactory}
		 */
		@Bean(name = "serverLogFactory")
		@ConditionalOnMissingBean(name = "serverLogFactory")
		@ConditionalOnProperty(prefix = "quickfixj.server", name = "log-factory", havingValue = "file")
		public LogFactory serverFileLogFactory(SessionSettings serverSessionSettings) {
			return new FileLogFactory(serverSessionSettings);
		}

		/**
		 * Creates the server's {@link LogFactory} of type {@link JdbcLogFactory} if
		 * {@code quickfixj.server.log-factory} is set to {@code jdbc}, used in the creation of the
		 * {@link Acceptor acceptor} connector
		 *
		 * @param serverSessionSettings The server's {@link SessionSettings session settings} bean
		 * @return The server's {@link LogFactory}
		 */
		@Bean(name = "serverLogFactory")
		@ConditionalOnMissingBean(name = "serverLogFactory")
		@ConditionalOnProperty(prefix = "quickfixj.server", name = "log-factory", havingValue = "jdbc")
		public LogFactory serverJdbcLogFactory(SessionSettings serverSessionSettings) {
			return new JdbcLogFactory(serverSessionSettings);
		}

		/**
		 * Creates the server's {@link LogFactory} of type {@link SLF4JLogFactory} if
		 * {@code quickfixj.server.log-factory} is set to {@code slf4j}, used in the creation of the
		 * {@link Acceptor acceptor} connector
		 *
		 * @param serverSessionSettings The server's {@link SessionSettings session settings} bean
		 * @return The server's {@link LogFactory}
		 */
		@Bean(name = "serverLogFactory")
		@ConditionalOnMissingBean(name = "serverLogFactory")
		@ConditionalOnProperty(prefix = "quickfixj.server", name = "log-factory", havingValue = "slf4j")
		public LogFactory serverSlf4jLogFactory(SessionSettings serverSessionSettings) {
			return new SLF4JLogFactory(serverSessionSettings);
		}

		/**
		 * Creates the server's {@link LogFactory} of type {@link ScreenLogFactory} if
		 * {@code quickfixj.server.log-factory} is set to {@code screen}, used in the creation of the
		 * {@link Acceptor acceptor} connector
		 *
		 * @param serverSessionSettings The server's {@link SessionSettings session settings} bean
		 * @return The server's {@link LogFactory}
		 */
		@Bean(name = "serverLogFactory")
		@ConditionalOnMissingBean(name = "serverLogFactory")
		@ConditionalOnProperty(prefix = "quickfixj.server", name = "log-factory", havingValue = "screen", matchIfMissing = true)
		public LogFactory serverScreenLogFactory(SessionSettings serverSessionSettings) {
			return new ScreenLogFactory(serverSessionSettings);
		}
	}

	/**
	 * Creates the default server's {@link MessageFactory}
	 *
	 * @return The default server's {@link MessageFactory application} bean
	 */
	@Bean
	@ConditionalOnMissingBean(name = "serverMessageFactory")
	public MessageFactory serverMessageFactory() {
		return new DefaultMessageFactory();
	}

	@Configuration
	public static class SocketAcceptorConfiguration {

		/**
		 * Creates a single threaded {@link Acceptor acceptor} bean
		 *
		 * @param serverApplication         The server's {@link Application}
		 * @param serverMessageStoreFactory The server's {@link MessageStoreFactory}
		 * @param serverSessionSettings     The server's {@link SessionSettings}
		 * @param serverLogFactory          The server's {@link LogFactory}
		 * @param serverMessageFactory      The server's {@link MessageFactory}
		 * @return The server's {@link Acceptor acceptor}
		 * @throws ConfigError exception thrown when a configuration error is detected
		 */
		@Bean
		@ConditionalOnMissingBean
		@ConditionalOnProperty(prefix = "quickfixj.server.concurrent", name = "enabled", havingValue = "false", matchIfMissing = true)
		public Acceptor serverAcceptor(
				Application serverApplication,
				MessageStoreFactory serverMessageStoreFactory,
				SessionSettings serverSessionSettings,
				LogFactory serverLogFactory,
				MessageFactory serverMessageFactory) throws ConfigError {

			return SocketAcceptor.newBuilder()
					.withApplication(serverApplication)
					.withMessageStoreFactory(serverMessageStoreFactory)
					.withSettings(serverSessionSettings)
					.withLogFactory(serverLogFactory)
					.withMessageFactory(serverMessageFactory)
					.build();
		}
	}

	@Configuration
	public static class ThreadedSocketAcceptorConfiguration {

		/**
		 * Creates a multi threaded {@link Acceptor acceptor} bean
		 *
		 * @param serverApplication         The server's {@link Application}
		 * @param serverMessageStoreFactory The server's {@link MessageStoreFactory}
		 * @param serverSessionSettings     The server's {@link SessionSettings}
		 * @param serverLogFactory          The server's {@link LogFactory}
		 * @param serverMessageFactory      The server's {@link MessageFactory}
		 * @return The server's {@link Acceptor acceptor}
		 * @throws ConfigError exception thrown when a configuration error is detected
		 */
		@Bean
		@ConditionalOnMissingBean
		@ConditionalOnProperty(prefix = "quickfixj.server.concurrent", name = "enabled", havingValue = "true")
		public Acceptor serverAcceptor(
				Application serverApplication,
				MessageStoreFactory serverMessageStoreFactory,
				SessionSettings serverSessionSettings,
				LogFactory serverLogFactory,
				MessageFactory serverMessageFactory) throws ConfigError {

			return ThreadedSocketAcceptor.newBuilder()
					.withApplication(serverApplication)
					.withMessageStoreFactory(serverMessageStoreFactory)
					.withSettings(serverSessionSettings)
					.withLogFactory(serverLogFactory)
					.withMessageFactory(serverMessageFactory)
					.build();
		}
	}

	/**
	 * Creates the server's {@link ConnectorManager}
	 *
	 * @param serverAcceptor   The server's {@link Acceptor acceptor}
	 * @param properties        The {@link QuickFixJBootProperties} properties
	 * @return The server's {@link ConnectorManager}
	 */
	@Bean
	public ConnectorManager serverConnectorManager(Acceptor serverAcceptor, QuickFixJBootProperties properties) {
		ConnectorManager connectorManager = new ConnectorManager(serverAcceptor);
		if (properties.getServer() != null) {
			connectorManager.setAutoStartup(properties.getServer().isAutoStartup());
			connectorManager.setPhase(properties.getServer().getPhase());
			connectorManager.setForceDisconnect(properties.getServer().isForceDisconnect());
		}
		return connectorManager;
	}

	/**
	 * Creates the server's JMX Bean
	 *
	 * @param serverAcceptor The server's {@link Acceptor acceptor}
	 * @return The server's JMX bean
	 */
	@Bean
	@ConditionalOnProperty(prefix = "quickfixj.server", name = "jmx-enabled", havingValue = "true")
	@ConditionalOnClass(JmxExporter.class)
	@ConditionalOnSingleCandidate(Acceptor.class)
	@ConditionalOnMissingBean(name = "serverAcceptorMBean")
	public ObjectName serverAcceptorMBean(Acceptor serverAcceptor) {
		try {
			JmxExporter exporter = new JmxExporter();
			exporter.setRegistrationBehavior(REGISTRATION_REPLACE_EXISTING);
			return exporter.register(serverAcceptor);
		} catch (Exception e) {
			throw new ConfigurationException(e.getMessage(), e);
		}
	}
}
