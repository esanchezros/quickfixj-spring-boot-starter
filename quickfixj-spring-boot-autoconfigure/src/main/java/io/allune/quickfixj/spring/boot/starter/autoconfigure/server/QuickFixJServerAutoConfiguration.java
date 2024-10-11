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
package io.allune.quickfixj.spring.boot.starter.autoconfigure.server;

import io.allune.quickfixj.spring.boot.starter.application.EventPublisherApplicationAdapter;
import io.allune.quickfixj.spring.boot.starter.autoconfigure.QuickFixJBootProperties;
import io.allune.quickfixj.spring.boot.starter.connection.ConnectorManager;
import io.allune.quickfixj.spring.boot.starter.connection.SessionSettingsLocator;
import io.allune.quickfixj.spring.boot.starter.exception.ConfigurationException;
import org.quickfixj.jmx.JmxExporter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import quickfix.Acceptor;
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
import quickfix.SocketAcceptor;
import quickfix.ThreadedSocketAcceptor;

import javax.management.ObjectName;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.quickfixj.jmx.JmxExporter.REGISTRATION_REPLACE_EXISTING;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for QuickFIX/J Server (Acceptor)
 *
 * @author Eduardo Sanchez-Ros
 */
@AutoConfiguration
@ConditionalOnClass(ConnectorManager.class)
@ConditionalOnMissingBean(name = "serverConnectorManager")
@ConditionalOnProperty(name = "quickfixj.server.enabled", havingValue = "true")
@EnableConfigurationProperties(QuickFixJBootProperties.class)
public class QuickFixJServerAutoConfiguration {

	private static final String SYSTEM_VARIABLE_QUICKFIXJ_SERVER_CONFIG = "quickfixj.server.config";

	private static final String QUICKFIXJ_SERVER_CONFIG = "quickfixj-server.cfg";

	/**
	 * Creates the default server's {@link SessionSettings session settings} bean used in the creation of the
	 * {@link Acceptor acceptor} connector
	 *
	 * @param serverSessionSettingsLocator The {@link SessionSettingsLocator} for the server
	 * @param properties                   The {@link QuickFixJBootProperties QuickFix/J Spring Boot properties}
	 * @return The server's {@link SessionSettings session settings} bean
	 */
	@Bean(name = "serverSessionSettings")
	@ConditionalOnMissingBean(name = "serverSessionSettings")
	public SessionSettings serverSessionSettings(
			@Qualifier("serverSessionSettingsLocator") SessionSettingsLocator serverSessionSettingsLocator,
			QuickFixJBootProperties properties
	) {
		if (isNotEmpty(properties.getServer().getConfigString())) {
			return serverSessionSettingsLocator.loadSettingsFromString(properties.getServer().getConfigString());
		}

		return serverSessionSettingsLocator.loadSettings(
				properties.getServer().getConfig(),
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

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(MessageStoreFactory.class)
	@ConditionalOnMissingBean(name = "serverMessageStoreFactory")
	@ConditionalOnProperty(prefix = "quickfixj.server", name = "message-store-factory", havingValue = "cachedfile")
	static class CachedFileMessageStoreFactoryConfiguration {

		/**
		 * Creates the server's {@link MessageStoreFactory} of type {@link CachedFileStoreFactory} if
		 * {@code quickfixj.server.message-store-factory} is set to {@code cachedfile}, used in the creation of the
		 * {@link Acceptor acceptor} connector
		 *
		 * @param serverSessionSettings The server's {@link SessionSettings session settings} bean
		 * @return The server's {@link MessageStoreFactory}
		 */
		@Bean
		public MessageStoreFactory serverMessageStoreFactory(@Qualifier("serverSessionSettings") SessionSettings serverSessionSettings) {
			return new CachedFileStoreFactory(serverSessionSettings);
		}
	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(MessageStoreFactory.class)
	@ConditionalOnMissingBean(name = "serverMessageStoreFactory")
	@ConditionalOnProperty(prefix = "quickfixj.server", name = "message-store-factory", havingValue = "file")
	static class FileMessageStoreFactoryConfiguration {
		/**
		 * Creates the server's {@link MessageStoreFactory} of type {@link FileStoreFactory} if
		 * {@code quickfixj.server.message-store-factory} is set to {@code file}, used in the creation of the
		 * {@link Acceptor acceptor} connector
		 *
		 * @param serverSessionSettings The server's {@link SessionSettings session settings} bean
		 * @return The server's {@link MessageStoreFactory}
		 */
		@Bean
		public MessageStoreFactory serverMessageStoreFactory(@Qualifier("serverSessionSettings") SessionSettings serverSessionSettings) {
			return new FileStoreFactory(serverSessionSettings);
		}
	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(MessageStoreFactory.class)
	@ConditionalOnMissingBean(name = "serverMessageStoreFactory")
	@ConditionalOnProperty(prefix = "quickfixj.server", name = "message-store-factory", havingValue = "jdbc")
	static class JdbcMessageStoreFactoryConfiguration {

		/**
		 * Creates the server's {@link MessageStoreFactory} of type {@link JdbcStoreFactory} if
		 * {@code quickfixj.server.message-store-factory} is set to {@code jdbc}, used in the creation of the
		 * {@link Acceptor acceptor} connector
		 *
		 * @param serverSessionSettings The server's {@link SessionSettings session settings} bean
		 * @return The server's {@link MessageStoreFactory}
		 */
		@Bean
		public MessageStoreFactory serverMessageStoreFactory(@Qualifier("serverSessionSettings") SessionSettings serverSessionSettings) {
			return new JdbcStoreFactory(serverSessionSettings);
		}
	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(MessageStoreFactory.class)
	@ConditionalOnMissingBean(name = "serverMessageStoreFactory")
	@ConditionalOnProperty(prefix = "quickfixj.server", name = "message-store-factory", havingValue = "memory", matchIfMissing = true)
	static class MemoryMessageStoreFactoryConfiguration {

		/**
		 * Creates the server's {@link MessageStoreFactory} of type {@link MemoryStoreFactory} if
		 * {@code quickfixj.server.message-store-factory} is set to {@code memory}, used in the creation of the
		 * {@link Acceptor acceptor} connector
		 *
		 * @return The server's {@link MessageStoreFactory}
		 */
		@Bean
		public MessageStoreFactory serverMessageStoreFactory() {
			return new MemoryStoreFactory();
		}
	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(MessageStoreFactory.class)
	@ConditionalOnMissingBean(name = "serverMessageStoreFactory")
	@ConditionalOnProperty(prefix = "quickfixj.server", name = "message-store-factory", havingValue = "noop")
	static class NoopMessageStoreFactoryConfiguration {

		/**
		 * Creates the server's {@link MessageStoreFactory} of type {@link NoopStoreFactory} if
		 * {@code quickfixj.server.message-store-factory} is set to {@code noop}, used in the creation of the
		 * {@link Acceptor acceptor} connector
		 *
		 * @return The server's {@link MessageStoreFactory}
		 */
		@Bean
		public MessageStoreFactory serverMessageStoreFactory() {
			return new NoopStoreFactory();
		}
	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(MessageStoreFactory.class)
	@ConditionalOnMissingBean(name = "serverMessageStoreFactory")
	@ConditionalOnProperty(prefix = "quickfixj.server", name = "message-store-factory", havingValue = "sleepycat")
	static class SleepycatMessageStoreFactoryConfiguration {

		/**
		 * Creates the server's {@link MessageStoreFactory} of type {@link SleepycatStoreFactory} if
		 * {@code quickfixj.server.message-store-factory} is set to {@code sleepycat}, used in the creation of the
		 * {@link Acceptor acceptor} connector
		 *
		 * @param serverSessionSettings The server's {@link SessionSettings session settings} bean
		 * @return The server's {@link MessageStoreFactory}
		 */
		@Bean
		public MessageStoreFactory serverMessageStoreFactory(@Qualifier("serverSessionSettings") SessionSettings serverSessionSettings) {
			return new SleepycatStoreFactory(serverSessionSettings);
		}
	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(LogFactory.class)
	@ConditionalOnMissingBean(name = "serverLogFactory")
	@ConditionalOnProperty(prefix = "quickfixj.server", name = "log-factory", havingValue = "file")
	static class FileLogFactoryConfiguration {

		/**
		 * Creates the server's {@link LogFactory} of type {@link FileLogFactory} if
		 * {@code quickfixj.server.log-factory} is set to {@code file}, used in the creation of the
		 * {@link Acceptor acceptor} connector
		 *
		 * @param serverSessionSettings The server's {@link SessionSettings session settings} bean
		 * @return The server's {@link LogFactory}
		 */
		@Bean
		@Primary
		public LogFactory serverLogFactory(@Qualifier("serverSessionSettings") SessionSettings serverSessionSettings) {
			return new FileLogFactory(serverSessionSettings);
		}
	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(LogFactory.class)
	@ConditionalOnMissingBean(name = "serverLogFactory")
	@ConditionalOnProperty(prefix = "quickfixj.server", name = "log-factory", havingValue = "jdbc")
	static class JdbcLogFactoryConfiguration {

		/**
		 * Creates the server's {@link LogFactory} of type {@link JdbcLogFactory} if
		 * {@code quickfixj.server.log-factory} is set to {@code jdbc}, used in the creation of the
		 * {@link Acceptor acceptor} connector
		 *
		 * @param serverSessionSettings The server's {@link SessionSettings session settings} bean
		 * @return The server's {@link LogFactory}
		 */
		@Bean
		@Primary
		public LogFactory serverLogFactory(@Qualifier("serverSessionSettings") SessionSettings serverSessionSettings) {
			return new JdbcLogFactory(serverSessionSettings);
		}
	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(LogFactory.class)
	@ConditionalOnMissingBean(name = "serverLogFactory")
	@ConditionalOnProperty(prefix = "quickfixj.server", name = "log-factory", havingValue = "slf4j")
	static class Slf4jLogFactoryConfiguration {

		/**
		 * Creates the server's {@link LogFactory} of type {@link SLF4JLogFactory} if
		 * {@code quickfixj.server.log-factory} is set to {@code slf4j}, used in the creation of the
		 * {@link Acceptor acceptor} connector
		 *
		 * @param serverSessionSettings The server's {@link SessionSettings session settings} bean
		 * @return The server's {@link LogFactory}
		 */
		@Bean
		@Primary
		public LogFactory serverLogFactory(@Qualifier("serverSessionSettings") SessionSettings serverSessionSettings) {
			return new SLF4JLogFactory(serverSessionSettings);
		}
	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(LogFactory.class)
	@ConditionalOnMissingBean(name = "serverLogFactory")
	@ConditionalOnProperty(prefix = "quickfixj.server", name = "log-factory",
			havingValue = "screen", matchIfMissing = true)
	static class ScreenLogFactoryConfiguration {

		/**
		 * Creates the server's {@link LogFactory} of type {@link ScreenLogFactory} if
		 * {@code quickfixj.server.log-factory} is set to {@code screen}, used in the creation of the
		 * {@link Acceptor acceptor} connector
		 *
		 * @param serverSessionSettings The server's {@link SessionSettings session settings} bean
		 * @return The server's {@link LogFactory}
		 */
		@Bean
		@Primary
		public LogFactory serverLogFactory(@Qualifier("serverSessionSettings") SessionSettings serverSessionSettings) {
			return new ScreenLogFactory(serverSessionSettings);
		}
	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(LogFactory.class)
	@ConditionalOnMissingBean(name = "serverLogFactory")
	@ConditionalOnProperty(prefix = "quickfixj.server", name = "log-factory", havingValue = "compositelog")
	static class CompositeLogFactoryConfiguration {

		/**
		 * Creates the server's {@link LogFactory} of type {@link CompositeLogFactory} if
		 * {@code quickfixj.server.log-factory} is set to {@code compositelog}, used in the creation of the
		 * {@link Initiator initiator} connector
		 *
		 * @param logFactories The server's list of {@link LogFactory log factories} beans to use for creating
		 *                     the {@link CompositeLogFactory}
		 * @return The server's {@link LogFactory}
		 */
		@Bean
		@Primary
		public LogFactory serverLogFactory(List<LogFactory> logFactories) {
			if (logFactories == null || logFactories.isEmpty()) {
				throw new ConfigurationException("The CompositeLogFactory requires at least one LogFactory bean defined in your application");
			}

			return new CompositeLogFactory(logFactories.toArray(new LogFactory[0]));
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

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(Acceptor.class)
	@ConditionalOnMissingBean(name = "serverAcceptor")
	@ConditionalOnProperty(prefix = "quickfixj.server.concurrent", name = "enabled", havingValue = "false", matchIfMissing = true)
	public static class SocketAcceptorConfiguration {

		/**
		 * Creates a single threaded {@link Acceptor acceptor} bean
		 *
		 * @param serverApplication         The server's {@link Application}
		 * @param serverMessageStoreFactory The server's {@link MessageStoreFactory}
		 * @param serverSessionSettings     The server's {@link SessionSettings}
		 * @param serverLogFactory          The server's {@link LogFactory}
		 * @param serverMessageFactory      The server's {@link MessageFactory}
		 * @param serverExecutorFactory     Optional server's {@link ExecutorFactory}
		 * @return The server's {@link Acceptor acceptor}
		 * @throws ConfigError exception thrown when a configuration error is detected
		 */
		@Bean
		public Acceptor serverAcceptor(
				@Qualifier("serverApplication") Application serverApplication,
				@Qualifier("serverMessageStoreFactory") MessageStoreFactory serverMessageStoreFactory,
				@Qualifier("serverSessionSettings") SessionSettings serverSessionSettings,
				@Qualifier("serverLogFactory") LogFactory serverLogFactory,
				@Qualifier("serverMessageFactory") MessageFactory serverMessageFactory,
				@Qualifier("serverExecutorFactory") Optional<ExecutorFactory> serverExecutorFactory
		) throws ConfigError {

			SocketAcceptor socketAcceptor = SocketAcceptor.newBuilder()
					.withApplication(serverApplication)
					.withMessageStoreFactory(serverMessageStoreFactory)
					.withSettings(serverSessionSettings)
					.withLogFactory(serverLogFactory)
					.withMessageFactory(serverMessageFactory)
					.build();
			serverExecutorFactory.ifPresent(socketAcceptor::setExecutorFactory);
			return socketAcceptor;
		}
	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(Acceptor.class)
	@ConditionalOnMissingBean(name = "serverAcceptor")
	@ConditionalOnProperty(prefix = "quickfixj.server.concurrent", name = "enabled", havingValue = "true")
	public static class ThreadedSocketAcceptorConfiguration {

		/**
		 * Creates a multi threaded {@link Acceptor acceptor} bean
		 *
		 * @param serverApplication         The server's {@link Application}
		 * @param serverMessageStoreFactory The server's {@link MessageStoreFactory}
		 * @param serverSessionSettings     The server's {@link SessionSettings}
		 * @param serverLogFactory          The server's {@link LogFactory}
		 * @param serverMessageFactory      The server's {@link MessageFactory}
		 * @param serverExecutorFactory     Optional server's {@link ExecutorFactory}
		 * @return The server's {@link Acceptor acceptor}
		 * @throws ConfigError exception thrown when a configuration error is detected
		 */
		@Bean
		public Acceptor serverAcceptor(
				@Qualifier("serverApplication") Application serverApplication,
				@Qualifier("serverMessageStoreFactory") MessageStoreFactory serverMessageStoreFactory,
				@Qualifier("serverSessionSettings") SessionSettings serverSessionSettings,
				@Qualifier("serverLogFactory") LogFactory serverLogFactory,
				@Qualifier("serverMessageFactory") MessageFactory serverMessageFactory,
				@Qualifier("serverExecutorFactory") Optional<ExecutorFactory> serverExecutorFactory
		) throws ConfigError {

			ThreadedSocketAcceptor socketAcceptor = ThreadedSocketAcceptor.newBuilder()
					.withApplication(serverApplication)
					.withMessageStoreFactory(serverMessageStoreFactory)
					.withSettings(serverSessionSettings)
					.withLogFactory(serverLogFactory)
					.withMessageFactory(serverMessageFactory)
					.build();
			serverExecutorFactory.ifPresent(socketAcceptor::setExecutorFactory);
			return socketAcceptor;
		}
	}

	@Bean
	@Primary
	@ConditionalOnClass(ExecutorFactory.class)
	@ConditionalOnMissingBean(name = "serverExecutorFactory")
	@ConditionalOnProperty(prefix = "quickfixj.server.concurrent", name = "useDefaultExecutorFactory", havingValue = "true")
	public ExecutorFactory serverExecutorFactory(Executor serverTaskExecutor) {
		return new ExecutorFactory() {
			@Override
			public Executor getLongLivedExecutor() {
				return serverTaskExecutor;
			}

			@Override
			public Executor getShortLivedExecutor() {
				return serverTaskExecutor;
			}
		};
	}

	@Bean
	@Primary
	@ConditionalOnMissingBean(name = "serverTaskExecutor")
	@ConditionalOnProperty(prefix = "quickfixj.server.concurrent", name = "useDefaultExecutorFactory", havingValue = "true")
	public Executor serverTaskExecutor(QuickFixJBootProperties properties) {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setQueueCapacity(properties.getServer().getConcurrent().getQueueCapacity());
		executor.setCorePoolSize(properties.getServer().getConcurrent().getCorePoolSize());
		executor.setMaxPoolSize(properties.getServer().getConcurrent().getMaxPoolSize());
		executor.setAllowCoreThreadTimeOut(properties.getServer().getConcurrent().isAllowCoreThreadTimeOut());
		executor.setKeepAliveSeconds(properties.getServer().getConcurrent().getKeepAliveSeconds());
		executor.setWaitForTasksToCompleteOnShutdown(properties.getServer().getConcurrent().isWaitForTasksToCompleteOnShutdown());
		executor.setAwaitTerminationSeconds(properties.getServer().getConcurrent().getAwaitTerminationSeconds());
		executor.setThreadNamePrefix(properties.getServer().getConcurrent().getThreadNamePrefix());
		return executor;
	}

	/**
	 * Creates the server's {@link ConnectorManager}
	 *
	 * @param serverAcceptor The server's {@link Acceptor acceptor}
	 * @param properties     The {@link QuickFixJBootProperties} properties
	 * @return The server's {@link ConnectorManager}
	 */
	@Bean
	public ConnectorManager serverConnectorManager(
			Acceptor serverAcceptor,
			QuickFixJBootProperties properties
	) {
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

	/**
	 * Creates the server's {@link SessionSettingsLocator}
	 *
	 * @param resourceLoader The {@link ResourceLoader} to use for loading the properties
	 * @return the server's {@link SessionSettingsLocator}
	 */
	@Bean
	public SessionSettingsLocator serverSessionSettingsLocator(ResourceLoader resourceLoader) {
		return new SessionSettingsLocator(resourceLoader);
	}
}
