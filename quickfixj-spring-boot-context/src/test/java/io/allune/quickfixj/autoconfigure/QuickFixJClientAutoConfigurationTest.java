/*
 * Copyright 2017-2022 the original author or authors.
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
package io.allune.quickfixj.autoconfigure;

import io.allune.quickfixj.spring.boot.starter.autoconfigure.client.QuickFixJClientAutoConfiguration;
import io.allune.quickfixj.spring.boot.starter.EnableQuickFixJClient;
import io.allune.quickfixj.spring.boot.starter.application.EventPublisherApplicationAdapter;
import io.allune.quickfixj.spring.boot.starter.connection.ConnectorManager;
import io.allune.quickfixj.spring.boot.starter.connection.SessionSettingsLocator;
import io.allune.quickfixj.spring.boot.starter.exception.ConfigurationException;
import io.allune.quickfixj.spring.boot.starter.template.QuickFixJTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import quickfix.Application;
import quickfix.CachedFileStoreFactory;
import quickfix.ConfigError;
import quickfix.DefaultMessageFactory;
import quickfix.ExecutorFactory;
import quickfix.FileStoreFactory;
import quickfix.FixVersions;
import quickfix.Initiator;
import quickfix.JdbcStoreFactory;
import quickfix.LogFactory;
import quickfix.MemoryStoreFactory;
import quickfix.MessageFactory;
import quickfix.MessageStoreFactory;
import quickfix.NoopStoreFactory;
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
public class QuickFixJClientAutoConfigurationTest {

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

		QuickFixJClientAutoConfiguration.ThreadedSocketInitiatorConfiguration initiatorConfiguration = new QuickFixJClientAutoConfiguration.ThreadedSocketInitiatorConfiguration();

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
		QuickFixJClientAutoConfiguration autoConfiguration = new QuickFixJClientAutoConfiguration();

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
	@EnableAutoConfiguration
	@EnableQuickFixJClient
	@PropertySource("classpath:client-single-threaded/single-threaded-application.properties")
	static class SingleThreadedClientInitiatorConfiguration {
	}

	@Configuration
	@EnableAutoConfiguration
	@EnableQuickFixJClient
	@PropertySource("classpath:client-single-threaded/single-threaded-application-executor-factory.properties")
	static class SingleThreadedExecutorFactoryClientInitiatorConfiguration {
	}

	@Configuration
	@EnableAutoConfiguration
	@EnableQuickFixJClient
	@PropertySource("classpath:client-single-threaded/single-threaded-application-config-string.properties")
	static class SingleThreadedClientConfigStringConfiguration {
	}

	@Configuration
	@EnableAutoConfiguration
	@EnableQuickFixJClient
	@PropertySource(value = "classpath:client-single-threaded/single-threaded-application-config-string.yml",
			factory = YamlPropertySourceFactory.class)
	static class SingleThreadedClientConfigStringYamlConfiguration {
	}

	@Configuration
	@EnableAutoConfiguration
	@EnableQuickFixJClient
	@PropertySource("classpath:client-multi-threaded/multi-threaded-application.properties")
	static class MultiThreadedClientInitiatorConfiguration {
	}

	@Configuration
	@EnableAutoConfiguration
	@EnableQuickFixJClient
	@PropertySource("classpath:client-multi-threaded/multi-threaded-application-executor-factory.properties")
	static class MultiThreadedExecutorFactoryClientInitiatorConfiguration {
	}

	@Configuration
	@EnableAutoConfiguration
	@EnableQuickFixJClient
	@PropertySource("classpath:client-single-threaded/single-threaded-application-no-config-defined.properties")
	static class SingleThreadedClientInitiatorConfigurationWithCustomClientSettings {

		@Bean(name = "clientSessionSettings")
		public SessionSettings customClientSessionSettings(SessionSettingsLocator clientSessionSettingsLocator) {
			return clientSessionSettingsLocator.loadSettings("classpath:quickfixj-client-extra.cfg");
		}
	}

	@Configuration
	@EnableAutoConfiguration
	@EnableQuickFixJClient
	@PropertySource("classpath:client-message-store/client-cachedfile-store-factory.properties")
	static class ClientCachedFileStoreFactoryConfiguration {
	}

	@Configuration
	@EnableAutoConfiguration
	@EnableQuickFixJClient
	@PropertySource("classpath:client-message-store/client-file-store-factory.properties")
	static class ClientFileStoreFactoryConfiguration {
	}

	@Configuration
	@EnableAutoConfiguration
	@EnableQuickFixJClient
	@PropertySource("classpath:client-message-store/client-jdbc-store-factory.properties")
	static class ClientJdbcStoreFactoryConfiguration {
	}

	@Configuration
	@EnableAutoConfiguration
	@EnableQuickFixJClient
	@PropertySource("classpath:client-message-store/client-memory-store-factory.properties")
	static class ClientMemoryStoreFactoryConfiguration {
	}

	@Configuration
	@EnableAutoConfiguration
	@EnableQuickFixJClient
	@PropertySource("classpath:client-message-store/client-noop-store-factory.properties")
	static class ClientNoopStoreFactoryConfiguration {
	}

	@Configuration
	@EnableAutoConfiguration
	@EnableQuickFixJClient
	@PropertySource("classpath:client-message-store/client-sleepycat-store-factory.properties")
	static class ClientSleepycatStoreFactoryConfiguration {
	}
}
