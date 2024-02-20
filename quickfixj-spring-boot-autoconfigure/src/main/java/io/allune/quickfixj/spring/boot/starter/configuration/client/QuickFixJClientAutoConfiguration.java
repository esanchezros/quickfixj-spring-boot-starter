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
package io.allune.quickfixj.spring.boot.starter.configuration.client;

import io.allune.quickfixj.spring.boot.starter.application.EventPublisherApplicationAdapter;
import io.allune.quickfixj.spring.boot.starter.configuration.QuickFixJBootProperties;
import io.allune.quickfixj.spring.boot.starter.connection.ConnectorManager;
import io.allune.quickfixj.spring.boot.starter.connection.SessionSettingsLocator;
import io.allune.quickfixj.spring.boot.starter.exception.ConfigurationException;
import org.quickfixj.jmx.JmxExporter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import quickfix.Application;
import quickfix.CachedFileStoreFactory;
import quickfix.CompositeLogFactory;
import quickfix.ConfigError;
import quickfix.DefaultMessageFactory;
import quickfix.ExecutorFactory;
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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.quickfixj.jmx.JmxExporter.REGISTRATION_REPLACE_EXISTING;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for QuickFix Client (Initiator)
 *
 * @author Eduardo Sanchez-Ros
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(ConnectorManager.class)
@ConditionalOnMissingBean(name = "clientConnectorManager")
@ConditionalOnProperty(name = "quickfixj.client.enabled", havingValue = "true")
@EnableConfigurationProperties(QuickFixJBootProperties.class)
public class QuickFixJClientAutoConfiguration {

	private static final String SYSTEM_VARIABLE_QUICKFIXJ_CLIENT_CONFIG = "quickfixj.client.config";

	private static final String QUICKFIXJ_CLIENT_CONFIG = "quickfixj-client.cfg";

	/**
	 * Creates the default client's {@link SessionSettings session settings} bean used in the creation of the
	 * {@link Initiator initiator} connector
	 *
	 * @param clientSessionSettingsLocator The {@link SessionSettingsLocator} for the client
	 * @param properties                   The {@link QuickFixJBootProperties QuickFix/J Spring Boot properties}
	 * @return The client's {@link SessionSettings session settings} bean
	 */
	@Bean(name = "clientSessionSettings")
	@ConditionalOnMissingBean(name = "clientSessionSettings")
	public SessionSettings clientSessionSettings(
			SessionSettingsLocator clientSessionSettingsLocator,
			QuickFixJBootProperties properties
	) {
		if (isNotEmpty(properties.getClient().getConfigString())) {
			return clientSessionSettingsLocator.loadSettingsFromString(properties.getClient().getConfigString());
		}

		return clientSessionSettingsLocator.loadSettings(
				properties.getClient().getConfig(),
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
	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(MessageStoreFactory.class)
	@ConditionalOnMissingBean(name = "clientMessageStoreFactory")
	@ConditionalOnProperty(prefix = "quickfixj.client", name = "message-store-factory", havingValue = "cachedfile")
	static class CachedFileMessageStoreFactoryConfiguration {

		/**
		 * Creates the client's {@link MessageStoreFactory} of type {@link CachedFileStoreFactory} if
		 * {@code quickfixj.client.message-store-factory} is set to {@code cachedfile}, used in the creation of the
		 * {@link Initiator initiator} connector
		 *
		 * @param clientSessionSettings The client's {@link SessionSettings session settings} bean
		 * @return The client's {@link MessageStoreFactory}
		 */
		@Bean
		public MessageStoreFactory clientMessageStoreFactory(SessionSettings clientSessionSettings) {
			return new CachedFileStoreFactory(clientSessionSettings);
		}
	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(MessageStoreFactory.class)
	@ConditionalOnMissingBean(name = "clientMessageStoreFactory")
	@ConditionalOnProperty(prefix = "quickfixj.client", name = "message-store-factory", havingValue = "file")
	static class FileMessageStoreFactoryConfiguration {

		/**
		 * Creates the client's {@link MessageStoreFactory} of type {@link FileStoreFactory} if
		 * {@code quickfixj.client.message-store-factory} is set to {@code file}, used in the creation of the
		 * {@link Initiator initiator} connector
		 *
		 * @param clientSessionSettings The client's {@link SessionSettings session settings} bean
		 * @return The client's {@link MessageStoreFactory}
		 */
		@Bean
		public MessageStoreFactory clientMessageStoreFactory(SessionSettings clientSessionSettings) {
			return new FileStoreFactory(clientSessionSettings);
		}
	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(MessageStoreFactory.class)
	@ConditionalOnMissingBean(name = "clientMessageStoreFactory")
	@ConditionalOnProperty(prefix = "quickfixj.client", name = "message-store-factory", havingValue = "jdbc")
	static class JdbcMessageStoreFactoryConfiguration {

		/**
		 * Creates the client's {@link MessageStoreFactory} of type {@link JdbcStoreFactory} if
		 * {@code quickfixj.client.message-store-factory} is set to {@code jdbc}, used in the creation of the
		 * {@link Initiator initiator} connector
		 *
		 * @param clientSessionSettings The client's {@link SessionSettings session settings} bean
		 * @return The client's {@link MessageStoreFactory}
		 */
		@Bean
		public MessageStoreFactory clientMessageStoreFactory(SessionSettings clientSessionSettings) {
			return new JdbcStoreFactory(clientSessionSettings);
		}
	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(MessageStoreFactory.class)
	@ConditionalOnMissingBean(name = "clientMessageStoreFactory")
	@ConditionalOnProperty(prefix = "quickfixj.client", name = "message-store-factory", havingValue = "memory", matchIfMissing = true)
	static class MemoryMessageStoreFactoryConfiguration {

		/**
		 * Creates the client's {@link MessageStoreFactory} of type {@link MemoryStoreFactory} if
		 * {@code quickfixj.client.message-store-factory} is set to {@code memory}, used in the creation of the
		 * {@link Initiator initiator} connector
		 *
		 * @return The client's {@link MessageStoreFactory}
		 */
		@Bean
		public MessageStoreFactory clientMessageStoreFactory() {
			return new MemoryStoreFactory();
		}
	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(MessageStoreFactory.class)
	@ConditionalOnMissingBean(name = "clientMessageStoreFactory")
	@ConditionalOnProperty(prefix = "quickfixj.client", name = "message-store-factory", havingValue = "noop")
	static class NoopMessageStoreFactoryConfiguration {
		/**
		 * Creates the client's {@link MessageStoreFactory} of type {@link NoopStoreFactory} if
		 * {@code quickfixj.client.message-store-factory} is set to {@code noop}, used in the creation of the
		 * {@link Initiator initiator} connector
		 *
		 * @return The client's {@link MessageStoreFactory}
		 */
		@Bean
		public MessageStoreFactory clientMessageStoreFactory() {
			return new NoopStoreFactory();
		}
	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(MessageStoreFactory.class)
	@ConditionalOnMissingBean(name = "clientMessageStoreFactory")
	@ConditionalOnProperty(prefix = "quickfixj.client", name = "message-store-factory", havingValue = "sleepycat")
	static class SleepycatMessageStoreFactoryConfiguration {

		/**
		 * Creates the client's {@link MessageStoreFactory} of type {@link SleepycatStoreFactory} if
		 * {@code quickfixj.client.message-store-factory} is set to {@code sleepycat}, used in the creation of the
		 * {@link Initiator initiator} connector
		 *
		 * @param clientSessionSettings The client's {@link SessionSettings session settings} bean
		 * @return The client's {@link MessageStoreFactory}
		 */
		@Bean
		public MessageStoreFactory clientMessageStoreFactory(SessionSettings clientSessionSettings) {
			return new SleepycatStoreFactory(clientSessionSettings);
		}
	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(LogFactory.class)
	@ConditionalOnMissingBean(name = "clientLogFactory")
	@ConditionalOnProperty(prefix = "quickfixj.client", name = "log-factory", havingValue = "file")
	static class FileLogFactoryConfiguration {

		/**
		 * Creates the client's {@link LogFactory} of type {@link FileLogFactory} if
		 * {@code quickfixj.client.log-factory} is set to {@code file}, used in the creation of the
		 * {@link Initiator initiator} connector
		 *
		 * @param clientSessionSettings The client's {@link SessionSettings session settings} bean
		 * @return The client's {@link LogFactory}
		 */
		@Bean
		public LogFactory clientLogFactory(SessionSettings clientSessionSettings) {
			return new FileLogFactory(clientSessionSettings);
		}
	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(LogFactory.class)
	@ConditionalOnMissingBean(name = "clientLogFactory")
	@ConditionalOnProperty(prefix = "quickfixj.client", name = "log-factory", havingValue = "jdbc")
	static class JdbcLogFactoryConfiguration {

		/**
		 * Creates the client's {@link LogFactory} of type {@link JdbcLogFactory} if
		 * {@code quickfixj.client.log-factory} is set to {@code jdbc}, used in the creation of the
		 * {@link Initiator initiator} connector
		 *
		 * @param clientSessionSettings The client's {@link SessionSettings session settings} bean
		 * @return The client's {@link LogFactory}
		 */
		@Bean
		public LogFactory clientLogFactory(SessionSettings clientSessionSettings) {
			return new JdbcLogFactory(clientSessionSettings);
		}
	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(LogFactory.class)
	@ConditionalOnMissingBean(name = "clientLogFactory")
	@ConditionalOnProperty(prefix = "quickfixj.client", name = "log-factory", havingValue = "slf4j")
	static class Slf4jLogFactoryConfiguration {

		/**
		 * Creates the client's {@link LogFactory} of type {@link SLF4JLogFactory} if
		 * {@code quickfixj.client.log-factory} is set to {@code slf4j}, used in the creation of the
		 * {@link Initiator initiator} connector
		 *
		 * @param clientSessionSettings The client's {@link SessionSettings session settings} bean
		 * @return The client's {@link LogFactory}
		 */
		@Bean
		public LogFactory clientLogFactory(SessionSettings clientSessionSettings) {
			return new SLF4JLogFactory(clientSessionSettings);
		}
	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(LogFactory.class)
	@ConditionalOnMissingBean(name = "clientLogFactory")
	@ConditionalOnProperty(prefix = "quickfixj.client", name = "log-factory",
		havingValue = "screen", matchIfMissing = true)
	static class ScreenLogFactoryConfiguration {

		/**
		 * Creates the client's {@link LogFactory} of type {@link ScreenLogFactory} if
		 * {@code quickfixj.client.log-factory} is set to {@code screen}, used in the creation of the
		 * {@link Initiator initiator} connector
		 *
		 * @param clientSessionSettings The client's {@link SessionSettings session settings} bean
		 * @return The client's {@link LogFactory}
		 */
		@Bean
		public LogFactory clientLogFactory(SessionSettings clientSessionSettings) {
			return new ScreenLogFactory(clientSessionSettings);
		}
	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(LogFactory.class)
	@ConditionalOnMissingBean(name = "clientLogFactory")
	@ConditionalOnProperty(prefix = "quickfixj.client", name = "log-factory", havingValue = "compositelog")
	static class CompositeLogFactoryConfiguration {

		/**
		 * Creates the client's {@link LogFactory} of type {@link CompositeLogFactory} if
		 * {@code quickfixj.client.log-factory} is set to {@code compositelog}, used in the creation of the
		 * {@link Initiator initiator} connector
		 *
		 * @param logFactories The client's list of {@link LogFactory log factories} beans to use for creating
		 *                     the {@link CompositeLogFactory}
		 * @return The client's {@link LogFactory}
		 */
		@Bean
		public LogFactory clientLogFactory(List<LogFactory> logFactories) {
			if (logFactories == null || logFactories.isEmpty()) {
				throw new ConfigurationException("The CompositeLogFactory requires at least one LogFactory bean defined in your application");
			}

			return new CompositeLogFactory(logFactories.toArray(new LogFactory[0]));
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

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(SocketInitiator.class)
	@ConditionalOnMissingBean(name = "clientInitiator")
	@ConditionalOnProperty(prefix = "quickfixj.client.concurrent", name = "enabled",
		havingValue = "false", matchIfMissing = true)
	public static class SocketInitiatorConfiguration {

		/**
		 * Creates a single threaded {@link Initiator} bean
		 *
		 * @param clientApplication         The client's {@link Application}
		 * @param clientMessageStoreFactory The client's {@link MessageStoreFactory}
		 * @param clientSessionSettings     The client's {@link SessionSettings}
		 * @param clientLogFactory          The client's {@link LogFactory}
		 * @param clientMessageFactory      The client's {@link MessageFactory}
		 * @param clientExecutorFactory     Optional client's {@link ExecutorFactory}
		 * @return The client's {@link Initiator}
		 * @throws ConfigError exception thrown when a configuration error is detected
		 */
		@Bean
		public Initiator clientInitiator(
				Application clientApplication,
				MessageStoreFactory clientMessageStoreFactory,
				SessionSettings clientSessionSettings,
				LogFactory clientLogFactory,
				MessageFactory clientMessageFactory,
				Optional<ExecutorFactory> clientExecutorFactory
		) throws ConfigError {
			SocketInitiator socketInitiator = SocketInitiator.newBuilder()
					.withApplication(clientApplication)
					.withMessageStoreFactory(clientMessageStoreFactory)
					.withSettings(clientSessionSettings)
					.withLogFactory(clientLogFactory)
					.withMessageFactory(clientMessageFactory)
					.build();
			clientExecutorFactory.ifPresent(socketInitiator::setExecutorFactory);
			return socketInitiator;
		}
	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(ThreadedSocketInitiator.class)
	@ConditionalOnMissingBean(name = "clientInitiator")
	@ConditionalOnProperty(prefix = "quickfixj.client.concurrent", name = "enabled", havingValue = "true")
	public static class ThreadedSocketInitiatorConfiguration {

		/**
		 * Creates a multi threaded {@link Initiator} bean
		 *
		 * @param clientApplication         The client's {@link Application}
		 * @param clientMessageStoreFactory The client's {@link MessageStoreFactory}
		 * @param clientSessionSettings     The client's {@link SessionSettings}
		 * @param clientLogFactory          The client's {@link LogFactory}
		 * @param clientMessageFactory      The client's {@link MessageFactory}
		 * @param clientExecutorFactory     Optional client's {@link ExecutorFactory}
		 * @return The client's {@link Initiator}
		 * @throws ConfigError exception thrown when a configuration error is detected
		 */
		@Bean
		public Initiator clientInitiator(
				Application clientApplication,
				MessageStoreFactory clientMessageStoreFactory,
				SessionSettings clientSessionSettings,
				LogFactory clientLogFactory,
				MessageFactory clientMessageFactory,
				Optional<ExecutorFactory> clientExecutorFactory
		) throws ConfigError {

			ThreadedSocketInitiator socketInitiator = ThreadedSocketInitiator.newBuilder()
					.withApplication(clientApplication)
					.withMessageStoreFactory(clientMessageStoreFactory)
					.withSettings(clientSessionSettings)
					.withLogFactory(clientLogFactory)
					.withMessageFactory(clientMessageFactory)
					.build();
			clientExecutorFactory.ifPresent(socketInitiator::setExecutorFactory);
			return socketInitiator;
		}
	}

	@Bean
	@ConditionalOnClass(ExecutorFactory.class)
	@ConditionalOnMissingBean(name = "clientExecutorFactory")
	@ConditionalOnProperty(prefix = "quickfixj.client.concurrent", name = "useDefaultExecutorFactory", havingValue = "true")
	public ExecutorFactory clientExecutorFactory(Executor clientTaskExecutor) {
		return new ExecutorFactory() {
			@Override
			public Executor getLongLivedExecutor() {
				return clientTaskExecutor;
			}

			@Override
			public Executor getShortLivedExecutor() {
				return clientTaskExecutor;
			}
		};
	}

	@Bean
	@ConditionalOnMissingBean(name = "clientTaskExecutor")
	@ConditionalOnProperty(prefix = "quickfixj.client.concurrent", name = "useDefaultExecutorFactory", havingValue = "true")
	public Executor clientTaskExecutor(QuickFixJBootProperties properties) {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setQueueCapacity(properties.getClient().getConcurrent().getQueueCapacity());
		executor.setCorePoolSize(properties.getClient().getConcurrent().getCorePoolSize());
		executor.setMaxPoolSize(properties.getClient().getConcurrent().getMaxPoolSize());
		executor.setAllowCoreThreadTimeOut(properties.getClient().getConcurrent().isAllowCoreThreadTimeOut());
		executor.setKeepAliveSeconds(properties.getClient().getConcurrent().getKeepAliveSeconds());
		executor.setWaitForTasksToCompleteOnShutdown(properties.getClient().getConcurrent().isWaitForTasksToCompleteOnShutdown());
		executor.setAwaitTerminationSeconds(properties.getClient().getConcurrent().getAwaitTerminationSeconds());
		executor.setThreadNamePrefix(properties.getClient().getConcurrent().getThreadNamePrefix());
		return executor;
	}

	/**
	 * Creates the client's {@link ConnectorManager}
	 *
	 * @param clientInitiator The client's {@link Initiator}
	 * @param properties      The {@link QuickFixJBootProperties} properties
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

	/**
	 * Creates the client's {@link SessionSettingsLocator}
	 *
	 * @param resourceLoader The {@link ResourceLoader} to use for loading the properties
	 * @return the client's {@link SessionSettingsLocator}
	 */
	@Bean
	public SessionSettingsLocator clientSessionSettingsLocator(ResourceLoader resourceLoader) {
		return new SessionSettingsLocator(resourceLoader);
	}
}
