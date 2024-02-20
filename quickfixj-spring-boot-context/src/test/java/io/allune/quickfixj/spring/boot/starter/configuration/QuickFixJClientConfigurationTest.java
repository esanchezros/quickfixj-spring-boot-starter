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
package io.allune.quickfixj.spring.boot.starter.configuration;

import io.allune.quickfixj.spring.boot.starter.EnableQuickFixJClient;
import io.allune.quickfixj.spring.boot.starter.application.EventPublisherApplicationAdapter;
import io.allune.quickfixj.spring.boot.starter.configuration.client.QuickFixJClientConfiguration;
import io.allune.quickfixj.spring.boot.starter.connection.ConnectorManager;
import io.allune.quickfixj.spring.boot.starter.connection.SessionSettingsLocator;
import io.allune.quickfixj.spring.boot.starter.exception.ConfigurationException;
import io.allune.quickfixj.spring.boot.starter.template.QuickFixJTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import quickfix.Application;
import quickfix.CachedFileStoreFactory;
import quickfix.CompositeLogFactory;
import quickfix.ConfigError;
import quickfix.DefaultMessageFactory;
import quickfix.ExecutorFactory;
import quickfix.FileLogFactory;
import quickfix.FileStoreFactory;
import quickfix.FixVersions;
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
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.SleepycatStoreFactory;
import quickfix.SocketInitiator;
import quickfix.ThreadedSocketInitiator;
import quickfix.mina.SessionConnector;

import javax.management.ObjectName;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.Executor;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;

/**
 * @author Eduardo Sanchez-Ros
 */
public class QuickFixJClientConfigurationTest {

	private static Field getField(Class clazz, String fieldName)
			throws NoSuchFieldException {
		try {
			return clazz.getDeclaredField(fieldName);
		} catch (NoSuchFieldException e) {
			Class superClass = clazz.getSuperclass();
			if (superClass == null) {
				throw e;
			} else {
				return getField(superClass, fieldName);
			}
		}
	}

	@Test
	public void testAutoConfiguredBeansSingleThreadedInitiator() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(SingleThreadedClientInitiatorConfiguration.class);
		ConnectorManager clientConnectorManager = ctx.getBean("clientConnectorManager", ConnectorManager.class);
		assertThat(clientConnectorManager.isRunning()).isFalse();
		assertThat(clientConnectorManager.isAutoStartup()).isFalse();
		assertThat(clientConnectorManager.isForceDisconnect()).isTrue();

		Initiator clientInitiator = ctx.getBean(Initiator.class);
		assertThat(clientInitiator).isInstanceOf(SocketInitiator.class);

		hasAutoConfiguredBeans(ctx);
		ctx.stop();
	}

	@Test
	public void testAutoConfiguredBeansSingleThreadedExecutorFactoryInitiator() throws NoSuchFieldException, IllegalAccessException {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(SingleThreadedExecutorFactoryClientInitiatorConfiguration.class);
		ConnectorManager clientConnectorManager = ctx.getBean("clientConnectorManager", ConnectorManager.class);
		assertThat(clientConnectorManager.isRunning()).isFalse();
		assertThat(clientConnectorManager.isAutoStartup()).isFalse();
		assertThat(clientConnectorManager.isForceDisconnect()).isTrue();

		Initiator clientInitiator = ctx.getBean(Initiator.class);
		assertThat(clientInitiator).isInstanceOf(SocketInitiator.class);

		hasAutoConfiguredBeans(ctx);

		ExecutorFactory clientExecutorFactory = ctx.getBean("clientExecutorFactory", ExecutorFactory.class);
		assertThat(clientExecutorFactory).isNotNull();

		Executor clientTaskExecutor = ctx.getBean("clientTaskExecutor", Executor.class);
		assertThat(clientTaskExecutor).isNotNull();

		assertHasExecutors(clientInitiator, clientTaskExecutor);
		ctx.stop();
	}

	@Test
	public void testAutoConfiguredBeansSingleConfigString() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(SingleThreadedClientConfigStringConfiguration.class);
		SessionSettings clientSessionSettings = ctx.getBean("clientSessionSettings", SessionSettings.class);
		assertThat(clientSessionSettings).isNotNull();

		Initiator clientInitiator = ctx.getBean(Initiator.class);
		assertThat(clientInitiator).isInstanceOf(SocketInitiator.class);
		List<SessionID> expectedSessionIDs = asList(
				new SessionID(FixVersions.BEGINSTRING_FIX40, "BANZAI", "EXEC"),
				new SessionID(FixVersions.BEGINSTRING_FIX41, "BANZAI", "EXEC"),
				new SessionID(FixVersions.BEGINSTRING_FIX42, "BANZAI", "EXEC"),
				new SessionID(FixVersions.BEGINSTRING_FIX43, "BANZAI", "EXEC"),
				new SessionID(FixVersions.BEGINSTRING_FIX44, "BANZAI", "EXEC"),
				new SessionID(FixVersions.BEGINSTRING_FIXT11, "BANZAI", "EXEC")
		);

		expectedSessionIDs.forEach(expectedSessionID -> {
			try {
				Properties sessionProperties = clientSessionSettings.getSessionProperties(expectedSessionID);
				assertThat(sessionProperties).isNotNull();
			} catch (ConfigError e) {
				fail("SessionID " + expectedSessionID + " not found");
			}
		});
		ctx.stop();
	}

	@Test
	public void testAutoConfiguredBeansSingleConfigStringYaml() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(SingleThreadedClientConfigStringYamlConfiguration.class);
		SessionSettings clientSessionSettings = ctx.getBean("clientSessionSettings", SessionSettings.class);
		assertThat(clientSessionSettings).isNotNull();

		Initiator clientInitiator = ctx.getBean(Initiator.class);
		assertThat(clientInitiator).isInstanceOf(SocketInitiator.class);
		List<SessionID> expectedSessionIDs = asList(
				new SessionID(FixVersions.BEGINSTRING_FIX40, "BANZAI", "EXEC"),
				new SessionID(FixVersions.BEGINSTRING_FIX41, "BANZAI", "EXEC"),
				new SessionID(FixVersions.BEGINSTRING_FIX42, "BANZAI", "EXEC"),
				new SessionID(FixVersions.BEGINSTRING_FIX43, "BANZAI", "EXEC"),
				new SessionID(FixVersions.BEGINSTRING_FIX44, "BANZAI", "EXEC"),
				new SessionID(FixVersions.BEGINSTRING_FIXT11, "BANZAI", "EXEC")
		);

		expectedSessionIDs.forEach(expectedSessionID -> {
			try {
				Properties sessionProperties = clientSessionSettings.getSessionProperties(expectedSessionID);
				assertThat(sessionProperties).isNotNull();
			} catch (ConfigError e) {
				fail("SessionID " + expectedSessionID + " not found");
			}
		});
		ctx.stop();
	}

	@Test
	public void testAutoConfiguredBeansMultiThreadedInitiator() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(MultiThreadedClientInitiatorConfiguration.class);
		ConnectorManager clientConnectorManager = ctx.getBean("clientConnectorManager", ConnectorManager.class);
		assertThat(clientConnectorManager.isRunning()).isFalse();
		assertThat(clientConnectorManager.isAutoStartup()).isFalse();
		assertThat(clientConnectorManager.isForceDisconnect()).isTrue();

		Initiator clientInitiator = ctx.getBean(Initiator.class);
		assertThat(clientInitiator).isInstanceOf(ThreadedSocketInitiator.class);

		hasAutoConfiguredBeans(ctx);
		ctx.stop();
	}

	@Test
	public void testAutoConfiguredBeansMultiThreadedExecutorFactoryInitiator() throws NoSuchFieldException, IllegalAccessException {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(MultiThreadedExecutorFactoryClientInitiatorConfiguration.class);
		ConnectorManager clientConnectorManager = ctx.getBean("clientConnectorManager", ConnectorManager.class);
		assertThat(clientConnectorManager.isRunning()).isFalse();
		assertThat(clientConnectorManager.isAutoStartup()).isFalse();
		assertThat(clientConnectorManager.isForceDisconnect()).isTrue();

		Initiator clientInitiator = ctx.getBean(Initiator.class);
		assertThat(clientInitiator).isInstanceOf(ThreadedSocketInitiator.class);

		hasAutoConfiguredBeans(ctx);

		ExecutorFactory clientExecutorFactory = ctx.getBean("clientExecutorFactory", ExecutorFactory.class);
		assertThat(clientExecutorFactory).isNotNull();

		Executor clientTaskExecutor = ctx.getBean("clientTaskExecutor", Executor.class);
		assertThat(clientTaskExecutor).isNotNull();

		assertHasExecutors(clientInitiator, clientTaskExecutor);
		ctx.stop();
	}

	@Test
	public void shouldCreateClientThreadedInitiator() throws ConfigError {
		// Given
		Application application = mock(Application.class);
		MessageStoreFactory messageStoreFactory = mock(MessageStoreFactory.class);
		SessionSettings sessionSettings = mock(SessionSettings.class);
		LogFactory logFactory = mock(LogFactory.class);
		MessageFactory messageFactory = mock(MessageFactory.class);

		QuickFixJClientConfiguration.ThreadedSocketInitiatorConfiguration initiatorConfiguration = new QuickFixJClientConfiguration.ThreadedSocketInitiatorConfiguration();

		// When
		Initiator initiator = initiatorConfiguration.clientInitiator(application, messageStoreFactory, sessionSettings,
				logFactory, messageFactory, Optional.empty());

		// Then
		assertThat(initiator).isNotNull();
		assertThat(initiator).isInstanceOf(ThreadedSocketInitiator.class);
	}

	@Test
	public void shouldThrowConfigurationExceptionCreatingClientInitiatorMBeanGivenNullInitiator() {
		// Given
		QuickFixJClientConfiguration autoConfiguration = new QuickFixJClientConfiguration();

		// When/Then
		assertThatExceptionOfType(ConfigurationException.class)
				.isThrownBy(() -> autoConfiguration.clientInitiatorMBean(null));
	}

	@Test
	public void testAutoConfiguredBeansSingleThreadedInitiatorWithCustomClientSettings() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(SingleThreadedClientInitiatorConfigurationWithCustomClientSettings.class);
		SessionSettings customClientSessionSettings = ctx.getBean("clientSessionSettings", SessionSettings.class);
		assertThat(customClientSessionSettings.getDefaultProperties().getProperty("SenderCompID")).isEqualTo("CUSTOM-BANZAI");
		ctx.stop();
	}

	@Test
	public void testAutoConfiguredBeansClientCachedFileStoreFactoryConfiguration() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(ClientCachedFileStoreFactoryConfiguration.class);
		MessageStoreFactory clientMessageStoreFactory = ctx.getBean("clientMessageStoreFactory", MessageStoreFactory.class);
		assertThat(clientMessageStoreFactory).isInstanceOf(CachedFileStoreFactory.class);
		ctx.stop();
	}

	@Test
	public void testAutoConfiguredBeansClientFileStoreFactoryConfiguration() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(ClientFileStoreFactoryConfiguration.class);
		MessageStoreFactory clientMessageStoreFactory = ctx.getBean("clientMessageStoreFactory", MessageStoreFactory.class);
		assertThat(clientMessageStoreFactory).isInstanceOf(FileStoreFactory.class);
		ctx.stop();
	}

	@Test
	public void testAutoConfiguredBeansClientJdbcStoreFactoryConfiguration() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(ClientJdbcStoreFactoryConfiguration.class);
		MessageStoreFactory clientMessageStoreFactory = ctx.getBean("clientMessageStoreFactory", MessageStoreFactory.class);
		assertThat(clientMessageStoreFactory).isInstanceOf(JdbcStoreFactory.class);
		ctx.stop();
	}

	@Test
	public void testAutoConfiguredBeansClientMemoryStoreFactoryConfiguration() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(ClientMemoryStoreFactoryConfiguration.class);
		MessageStoreFactory clientMessageStoreFactory = ctx.getBean("clientMessageStoreFactory", MessageStoreFactory.class);
		assertThat(clientMessageStoreFactory).isInstanceOf(MemoryStoreFactory.class);
		ctx.stop();
	}

	@Test
	public void testAutoConfiguredBeansClientNoopStoreFactoryConfiguration() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(ClientNoopStoreFactoryConfiguration.class);
		MessageStoreFactory clientMessageStoreFactory = ctx.getBean("clientMessageStoreFactory", MessageStoreFactory.class);
		assertThat(clientMessageStoreFactory).isInstanceOf(NoopStoreFactory.class);
		ctx.stop();
	}

	@Test
	public void testAutoConfiguredBeansClientSleepycatStoreFactoryConfiguration() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(ClientSleepycatStoreFactoryConfiguration.class);
		MessageStoreFactory clientMessageStoreFactory = ctx.getBean("clientMessageStoreFactory", MessageStoreFactory.class);
		assertThat(clientMessageStoreFactory).isInstanceOf(SleepycatStoreFactory.class);
		ctx.stop();
	}

	@Test
	public void testAutoConfiguredBeansClientCompositeLogFactoryConfiguration() throws NoSuchFieldException, IllegalAccessException {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(ClientCompositeLogFactoryConfiguration.class);
		LogFactory clientLogFactory = ctx.getBean("clientLogFactory", LogFactory.class);
		assertThat(clientLogFactory).isInstanceOf(CompositeLogFactory.class);

		Field privateField = CompositeLogFactory.class.getDeclaredField("logFactories");
		privateField.setAccessible(true);
		LogFactory[] currentLogFactories = (LogFactory[])privateField.get(clientLogFactory);

		assertThat(currentLogFactories).isNotEmpty();
		assertThat(currentLogFactories).hasSize(2);
		assertThat(currentLogFactories[0]).isInstanceOf(ScreenLogFactory.class);
		assertThat(currentLogFactories[1]).isInstanceOf(SLF4JLogFactory.class);

		ctx.stop();
	}

	@Test
	public void testAutoConfiguredBeansClientFileLogFactoryConfiguration() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(ClientFileLogFactoryConfiguration.class);
		LogFactory clientLogFactory = ctx.getBean("clientLogFactory", LogFactory.class);
		assertThat(clientLogFactory).isInstanceOf(FileLogFactory.class);
		ctx.stop();
	}

	@Test
	public void testAutoConfiguredBeansClientJdbcLogFactoryConfiguration() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(ClientJdbcLogFactoryConfiguration.class);
		LogFactory clientLogFactory = ctx.getBean("clientLogFactory", LogFactory.class);
		assertThat(clientLogFactory).isInstanceOf(JdbcLogFactory.class);
		ctx.stop();
	}

	@Test
	public void testAutoConfiguredBeansClientSlf4jLogFactoryConfiguration() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(ClientSlf4jLogFactoryConfiguration.class);
		LogFactory clientLogFactory = ctx.getBean("clientLogFactory", LogFactory.class);
		assertThat(clientLogFactory).isInstanceOf(SLF4JLogFactory.class);
		ctx.stop();
	}

	@Test
	public void testAutoConfiguredBeansClientScreenLogFactoryConfiguration() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(ClientScreenLogFactoryConfiguration.class);
		LogFactory clientLogFactory = ctx.getBean("clientLogFactory", LogFactory.class);
		assertThat(clientLogFactory).isInstanceOf(ScreenLogFactory.class);
		ctx.stop();
	}

	private void hasAutoConfiguredBeans(AnnotationConfigApplicationContext ctx) {
		Application clientApplication = ctx.getBean("clientApplication", Application.class);
		assertThat(clientApplication).isInstanceOf(EventPublisherApplicationAdapter.class);

		MessageStoreFactory clientMessageStoreFactory = ctx.getBean("clientMessageStoreFactory", MessageStoreFactory.class);
		assertThat(clientMessageStoreFactory).isInstanceOf(MemoryStoreFactory.class);

		LogFactory clientLogFactory = ctx.getBean("clientLogFactory", LogFactory.class);
		assertThat(clientLogFactory).isInstanceOf(ScreenLogFactory.class);

		MessageFactory clientMessageFactory = ctx.getBean("clientMessageFactory", MessageFactory.class);
		assertThat(clientMessageFactory).isInstanceOf(DefaultMessageFactory.class);

		SessionSettings clientSessionSettings = ctx.getBean("clientSessionSettings", SessionSettings.class);
		assertThat(clientSessionSettings).isNotNull();

		ObjectName clientInitiatorMBean = ctx.getBean("clientInitiatorMBean", ObjectName.class);
		assertThat(clientInitiatorMBean).isNotNull();

		QuickFixJTemplate quickFixJTemplate = ctx.getBean("quickFixJTemplate", QuickFixJTemplate.class);
		assertThat(quickFixJTemplate).isNotNull();
	}

	private void assertHasExecutors(Initiator clientInitiator, Executor taskExecutor) throws NoSuchFieldException, IllegalAccessException {
		Field longLivedExecutor = getField(SessionConnector.class, "longLivedExecutor");
		longLivedExecutor.setAccessible(true);
		Executor actualLongLivedExecutor = (Executor) longLivedExecutor.get(clientInitiator);
		assertThat(taskExecutor).isEqualTo(actualLongLivedExecutor);

		Field shortLivedExecutor = getField(SessionConnector.class, "shortLivedExecutor");
		shortLivedExecutor.setAccessible(true);
		Executor actualShortLivedExecutor = (Executor) shortLivedExecutor.get(clientInitiator);
		assertThat(taskExecutor).isEqualTo(actualShortLivedExecutor);
	}

	@Configuration
	@EnableQuickFixJClient
	@PropertySource("classpath:client-single-threaded/single-threaded-application.properties")
	static class SingleThreadedClientInitiatorConfiguration {
	}

	@Configuration
	@EnableQuickFixJClient
	@PropertySource("classpath:client-single-threaded/single-threaded-application-executor-factory.properties")
	static class SingleThreadedExecutorFactoryClientInitiatorConfiguration {
	}

	@Configuration
	@EnableQuickFixJClient
	@PropertySource("classpath:client-single-threaded/single-threaded-application-config-string.properties")
	static class SingleThreadedClientConfigStringConfiguration {
	}

	@Configuration
	@EnableQuickFixJClient
	@PropertySource(value = "classpath:client-single-threaded/single-threaded-application-config-string.yml",
			factory = YamlPropertySourceFactory.class)
	static class SingleThreadedClientConfigStringYamlConfiguration {
	}

	@Configuration
	@EnableQuickFixJClient
	@PropertySource("classpath:client-multi-threaded/multi-threaded-application.properties")
	static class MultiThreadedClientInitiatorConfiguration {
	}

	@Configuration
	@EnableQuickFixJClient
	@PropertySource("classpath:client-multi-threaded/multi-threaded-application-executor-factory.properties")
	static class MultiThreadedExecutorFactoryClientInitiatorConfiguration {
	}

	@Configuration
	@EnableQuickFixJClient
	@PropertySource("classpath:client-single-threaded/single-threaded-application-no-config-defined.properties")
	static class SingleThreadedClientInitiatorConfigurationWithCustomClientSettings {

		@Bean(name = "clientSessionSettings")
		public SessionSettings customClientSessionSettings(SessionSettingsLocator clientSessionSettingsLocator) {
			return clientSessionSettingsLocator.loadSettings("classpath:quickfixj-client-extra.cfg");
		}
	}

	@Configuration
	@EnableQuickFixJClient
	@PropertySource("classpath:client-message-store/client-cachedfile-store-factory.properties")
	static class ClientCachedFileStoreFactoryConfiguration {
	}

	@Configuration
	@EnableQuickFixJClient
	@PropertySource("classpath:client-message-store/client-file-store-factory.properties")
	static class ClientFileStoreFactoryConfiguration {
	}

	@Configuration
	@EnableQuickFixJClient
	@PropertySource("classpath:client-message-store/client-jdbc-store-factory.properties")
	static class ClientJdbcStoreFactoryConfiguration {
	}

	@Configuration
	@EnableQuickFixJClient
	@PropertySource("classpath:client-message-store/client-memory-store-factory.properties")
	static class ClientMemoryStoreFactoryConfiguration {
	}

	@Configuration
	@EnableQuickFixJClient
	@PropertySource("classpath:client-message-store/client-noop-store-factory.properties")
	static class ClientNoopStoreFactoryConfiguration {
	}

	@Configuration
	@EnableQuickFixJClient
	@PropertySource("classpath:client-message-store/client-sleepycat-store-factory.properties")
	static class ClientSleepycatStoreFactoryConfiguration {
	}

	@Configuration
	@EnableQuickFixJClient
	@PropertySource("classpath:client-log-factory/client-composite-log-factory.properties")
	static class ClientCompositeLogFactoryConfiguration {

		@Bean
		public LogFactory screenLogFactory(SessionSettings clientSessionSettings) {
			return new ScreenLogFactory(clientSessionSettings);
		}

		@Bean
		public LogFactory slf4jLogFactory(SessionSettings clientSessionSettings) {
			return new SLF4JLogFactory(clientSessionSettings);
		}
	}

	@Configuration
	@EnableQuickFixJClient
	@PropertySource("classpath:client-log-factory/client-file-log-factory.properties")
	static class ClientFileLogFactoryConfiguration {
	}

	@Configuration
	@EnableQuickFixJClient
	@PropertySource("classpath:client-log-factory/client-jdbc-log-factory.properties")
	static class ClientJdbcLogFactoryConfiguration {
	}

	@Configuration
	@EnableQuickFixJClient
	@PropertySource("classpath:client-log-factory/client-slf4j-log-factory.properties")
	static class ClientSlf4jLogFactoryConfiguration {
	}

	@Configuration
	@EnableQuickFixJClient
	@PropertySource("classpath:client-log-factory/client-screen-log-factory.properties")
	static class ClientScreenLogFactoryConfiguration {
	}
}
