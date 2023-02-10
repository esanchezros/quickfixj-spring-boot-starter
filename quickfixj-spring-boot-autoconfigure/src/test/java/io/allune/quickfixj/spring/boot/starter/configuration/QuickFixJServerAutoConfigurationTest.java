/*
 * Copyright 2017-2023 the original author or authors.
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

import io.allune.quickfixj.spring.boot.starter.configuration.server.QuickFixJServerAutoConfiguration;
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
import quickfix.Acceptor;
import quickfix.Application;
import quickfix.CachedFileStoreFactory;
import quickfix.ConfigError;
import quickfix.DefaultMessageFactory;
import quickfix.ExecutorFactory;
import quickfix.FileStoreFactory;
import quickfix.FixVersions;
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
import quickfix.SocketAcceptor;
import quickfix.ThreadedSocketAcceptor;
import quickfix.mina.SessionConnector;

import javax.management.ObjectName;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.Executor;

import static io.allune.quickfixj.spring.boot.starter.configuration.server.QuickFixJServerAutoConfiguration.ThreadedSocketAcceptorConfiguration;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;

/**
 * @author Eduardo Sanchez-Ros
 */
public class QuickFixJServerAutoConfigurationTest {

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
	public void testAutoConfiguredBeansSingleThreadedAcceptor() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(SingleThreadedServerAcceptorConfiguration.class);
		ConnectorManager serverConnectorManager = ctx.getBean("serverConnectorManager", ConnectorManager.class);
		assertThat(serverConnectorManager.isRunning()).isFalse();
		assertThat(serverConnectorManager.isAutoStartup()).isFalse();
		assertThat(serverConnectorManager.isForceDisconnect()).isTrue();

		Acceptor serverAcceptor = ctx.getBean(Acceptor.class);
		assertThat(serverAcceptor).isInstanceOf(SocketAcceptor.class);

		hasAutoConfiguredBeans(ctx);
		ctx.stop();
	}

	@Test
	public void testAutoConfiguredBeansSingleThreadedExecutorFactoryAcceptor() throws NoSuchFieldException, IllegalAccessException {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(SingleThreadedExecutorFactoryServerAcceptorConfiguration.class);
		ConnectorManager serverConnectorManager = ctx.getBean("serverConnectorManager", ConnectorManager.class);
		assertThat(serverConnectorManager.isRunning()).isFalse();
		assertThat(serverConnectorManager.isAutoStartup()).isFalse();
		assertThat(serverConnectorManager.isForceDisconnect()).isTrue();

		Acceptor serverAcceptor = ctx.getBean(Acceptor.class);
		assertThat(serverAcceptor).isInstanceOf(SocketAcceptor.class);

		hasAutoConfiguredBeans(ctx);

		ExecutorFactory serverExecutorFactory = ctx.getBean("serverExecutorFactory", ExecutorFactory.class);
		assertThat(serverExecutorFactory).isNotNull();

		Executor serverTaskExecutor = ctx.getBean("serverTaskExecutor", Executor.class);
		assertThat(serverTaskExecutor).isNotNull();

		assertHasExecutors(serverAcceptor, serverTaskExecutor);
		ctx.stop();
	}


	@Test
	public void testAutoConfiguredBeansSingleConfigString() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(SingleThreadedServerConfigStringConfiguration.class);
		SessionSettings serverSessionSettings = ctx.getBean("serverSessionSettings", SessionSettings.class);
		assertThat(serverSessionSettings).isNotNull();

		Acceptor serverAcceptor = ctx.getBean(Acceptor.class);
		assertThat(serverAcceptor).isInstanceOf(SocketAcceptor.class);
		List<SessionID> expectedSessionIDs = asList(
				new SessionID(FixVersions.BEGINSTRING_FIX40, "EXEC", "BANZAI"),
				new SessionID(FixVersions.BEGINSTRING_FIX41, "EXEC", "BANZAI"),
				new SessionID(FixVersions.BEGINSTRING_FIX42, "EXEC", "BANZAI"),
				new SessionID(FixVersions.BEGINSTRING_FIX43, "EXEC", "BANZAI"),
				new SessionID(FixVersions.BEGINSTRING_FIX44, "EXEC", "BANZAI"),
				new SessionID(FixVersions.BEGINSTRING_FIXT11, "EXEC", "BANZAI")
		);

		expectedSessionIDs.forEach(expectedSessionID -> {
			try {
				Properties sessionProperties = serverSessionSettings.getSessionProperties(expectedSessionID);
				assertThat(sessionProperties).isNotNull();
			} catch (ConfigError e) {
				fail("SessionID " + expectedSessionID + " not found");
			}
		});
		ctx.stop();
	}

	@Test
	public void testAutoConfiguredBeansSingleConfigStringYaml() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(SingleThreadedServerConfigStringYamlConfiguration.class);
		SessionSettings serverSessionSettings = ctx.getBean("serverSessionSettings", SessionSettings.class);
		assertThat(serverSessionSettings).isNotNull();

		Acceptor serverAcceptor = ctx.getBean(Acceptor.class);
		assertThat(serverAcceptor).isInstanceOf(SocketAcceptor.class);
		List<SessionID> expectedSessionIDs = asList(
				new SessionID(FixVersions.BEGINSTRING_FIX40, "EXEC", "BANZAI"),
				new SessionID(FixVersions.BEGINSTRING_FIX41, "EXEC", "BANZAI"),
				new SessionID(FixVersions.BEGINSTRING_FIX42, "EXEC", "BANZAI"),
				new SessionID(FixVersions.BEGINSTRING_FIX43, "EXEC", "BANZAI"),
				new SessionID(FixVersions.BEGINSTRING_FIX44, "EXEC", "BANZAI"),
				new SessionID(FixVersions.BEGINSTRING_FIXT11, "EXEC", "BANZAI")
		);

		expectedSessionIDs.forEach(expectedSessionID -> {
			try {
				Properties sessionProperties = serverSessionSettings.getSessionProperties(expectedSessionID);
				assertThat(sessionProperties).isNotNull();
			} catch (ConfigError e) {
				fail("SessionID " + expectedSessionID + " not found");
			}
		});
		ctx.stop();
	}

	@Test
	public void testAutoConfiguredBeansMultiThreadedAcceptor() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(MultiThreadedServerAcceptorConfiguration.class);
		ConnectorManager serverConnectorManager = ctx.getBean("serverConnectorManager", ConnectorManager.class);
		assertThat(serverConnectorManager.isRunning()).isFalse();
		assertThat(serverConnectorManager.isAutoStartup()).isFalse();
		assertThat(serverConnectorManager.isForceDisconnect()).isTrue();

		Acceptor serverAcceptor = ctx.getBean(Acceptor.class);
		assertThat(serverAcceptor).isInstanceOf(ThreadedSocketAcceptor.class);

		hasAutoConfiguredBeans(ctx);
		ctx.stop();
	}

	@Test
	public void testAutoConfiguredBeansMultiThreadedExecutorFactoryAcceptor() throws NoSuchFieldException, IllegalAccessException {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(MultiThreadedExecutorFactoryServerAcceptorConfiguration.class);
		ConnectorManager serverConnectorManager = ctx.getBean("serverConnectorManager", ConnectorManager.class);
		assertThat(serverConnectorManager.isRunning()).isFalse();
		assertThat(serverConnectorManager.isAutoStartup()).isFalse();
		assertThat(serverConnectorManager.isForceDisconnect()).isTrue();

		Acceptor serverAcceptor = ctx.getBean(Acceptor.class);
		assertThat(serverAcceptor).isInstanceOf(ThreadedSocketAcceptor.class);

		hasAutoConfiguredBeans(ctx);

		ExecutorFactory serverExecutorFactory = ctx.getBean("serverExecutorFactory", ExecutorFactory.class);
		assertThat(serverExecutorFactory).isNotNull();

		Executor serverTaskExecutor = ctx.getBean("serverTaskExecutor", Executor.class);
		assertThat(serverTaskExecutor).isNotNull();

		assertHasExecutors(serverAcceptor, serverTaskExecutor);
		ctx.stop();
	}

	@Test
	public void shouldCreateServerThreadedAcceptor() throws ConfigError {
		// Given
		Application application = mock(Application.class);
		MessageStoreFactory messageStoreFactory = mock(MessageStoreFactory.class);
		SessionSettings sessionSettings = mock(SessionSettings.class);
		LogFactory logFactory = mock(LogFactory.class);
		MessageFactory messageFactory = mock(MessageFactory.class);

		ThreadedSocketAcceptorConfiguration acceptorConfiguration = new ThreadedSocketAcceptorConfiguration();

		// When
		Acceptor acceptor = acceptorConfiguration.serverAcceptor(application, messageStoreFactory, sessionSettings,
				logFactory, messageFactory, Optional.empty());

		// Then
		assertThat(acceptor).isNotNull();
		assertThat(acceptor).isInstanceOf(ThreadedSocketAcceptor.class);
	}

	@Test
	public void shouldThrowConfigurationExceptionCreatingServerAcceptorMBeanGivenNullAcceptor() {
		// Given
		QuickFixJServerAutoConfiguration autoConfiguration = new QuickFixJServerAutoConfiguration();

		// When/Then
		assertThatExceptionOfType(ConfigurationException.class)
				.isThrownBy(() -> autoConfiguration.serverAcceptorMBean(null));
	}

	@Test
	public void testAutoConfiguredBeansSingleThreadedAcceptorWithCustomServerSettings() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(SingleThreadedServerAcceptorConfigurationWithCustomClientSettings.class);
		SessionSettings customClientSessionSettings = ctx.getBean("serverSessionSettings", SessionSettings.class);
		assertThat(customClientSessionSettings.getDefaultProperties().getProperty("SenderCompID")).isEqualTo("CUSTOM-EXEC");
		ctx.stop();
	}

	@Test
	public void testAutoConfiguredBeansServerCachedFileStoreFactoryConfiguration() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(ServerCachedFileStoreFactoryConfiguration.class);
		MessageStoreFactory serverMessageStoreFactory = ctx.getBean("serverMessageStoreFactory", MessageStoreFactory.class);
		assertThat(serverMessageStoreFactory).isInstanceOf(CachedFileStoreFactory.class);
		ctx.stop();
	}

	@Test
	public void testAutoConfiguredBeansServerFileStoreFactoryConfiguration() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(ServerFileStoreFactoryConfiguration.class);
		MessageStoreFactory serverMessageStoreFactory = ctx.getBean("serverMessageStoreFactory", MessageStoreFactory.class);
		assertThat(serverMessageStoreFactory).isInstanceOf(FileStoreFactory.class);
		ctx.stop();
	}

	@Test
	public void testAutoConfiguredBeansServerJdbcStoreFactoryConfiguration() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(ServerJdbcStoreFactoryConfiguration.class);
		MessageStoreFactory serverMessageStoreFactory = ctx.getBean("serverMessageStoreFactory", MessageStoreFactory.class);
		assertThat(serverMessageStoreFactory).isInstanceOf(JdbcStoreFactory.class);
		ctx.stop();
	}

	@Test
	public void testAutoConfiguredBeansServerMemoryStoreFactoryConfiguration() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(ServerMemoryStoreFactoryConfiguration.class);
		MessageStoreFactory serverMessageStoreFactory = ctx.getBean("serverMessageStoreFactory", MessageStoreFactory.class);
		assertThat(serverMessageStoreFactory).isInstanceOf(MemoryStoreFactory.class);
		ctx.stop();
	}

	@Test
	public void testAutoConfiguredBeansServerNoopStoreFactoryConfiguration() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(ServerNoopStoreFactoryConfiguration.class);
		MessageStoreFactory serverMessageStoreFactory = ctx.getBean("serverMessageStoreFactory", MessageStoreFactory.class);
		assertThat(serverMessageStoreFactory).isInstanceOf(NoopStoreFactory.class);
		ctx.stop();
	}

	@Test
	public void testAutoConfiguredBeansServerSleepycatStoreFactoryConfiguration() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(ServerSleepycatStoreFactoryConfiguration.class);
		MessageStoreFactory serverMessageStoreFactory = ctx.getBean("serverMessageStoreFactory", MessageStoreFactory.class);
		assertThat(serverMessageStoreFactory).isInstanceOf(SleepycatStoreFactory.class);
		ctx.stop();
	}

	private void hasAutoConfiguredBeans(AnnotationConfigApplicationContext ctx) {
		Application serverApplication = ctx.getBean("serverApplication", Application.class);
		assertThat(serverApplication).isInstanceOf(EventPublisherApplicationAdapter.class);

		MessageStoreFactory serverMessageStoreFactory = ctx.getBean("serverMessageStoreFactory", MessageStoreFactory.class);
		assertThat(serverMessageStoreFactory).isInstanceOf(MemoryStoreFactory.class);

		LogFactory serverLogFactory = ctx.getBean("serverLogFactory", LogFactory.class);
		assertThat(serverLogFactory).isInstanceOf(ScreenLogFactory.class);

		MessageFactory serverMessageFactory = ctx.getBean("serverMessageFactory", MessageFactory.class);
		assertThat(serverMessageFactory).isInstanceOf(DefaultMessageFactory.class);

		SessionSettings serverSessionSettings = ctx.getBean("serverSessionSettings", SessionSettings.class);
		assertThat(serverSessionSettings).isNotNull();

		ObjectName serverInitiatorMBean = ctx.getBean("serverAcceptorMBean", ObjectName.class);
		assertThat(serverInitiatorMBean).isNotNull();

		QuickFixJTemplate quickFixJTemplate = ctx.getBean("quickFixJTemplate", QuickFixJTemplate.class);
		assertThat(quickFixJTemplate).isNotNull();

		ctx.stop();
	}

	private void assertHasExecutors(Acceptor serverAcceptor, Executor taskExecutor)
			throws NoSuchFieldException, IllegalAccessException {
		Field longLivedExecutor = getField(SessionConnector.class, "longLivedExecutor");
		longLivedExecutor.setAccessible(true);
		Executor actualLongLivedExecutor = (Executor) longLivedExecutor.get(serverAcceptor);
		assertThat(taskExecutor).isEqualTo(actualLongLivedExecutor);

		Field shortLivedExecutor = getField(SessionConnector.class, "shortLivedExecutor");
		shortLivedExecutor.setAccessible(true);
		Executor actualShortLivedExecutor = (Executor) shortLivedExecutor.get(serverAcceptor);
		assertThat(taskExecutor).isEqualTo(actualShortLivedExecutor);
	}

	@Configuration
	@EnableAutoConfiguration
	@PropertySource("classpath:server-single-threaded/single-threaded-application.properties")
	static class SingleThreadedServerAcceptorConfiguration {
	}

	@Configuration
	@EnableAutoConfiguration
	@PropertySource("classpath:server-single-threaded/single-threaded-application-executor-factory.properties")
	static class SingleThreadedExecutorFactoryServerAcceptorConfiguration {
	}

	@Configuration
	@EnableAutoConfiguration
	@PropertySource("classpath:server-single-threaded/single-threaded-application-config-string.properties")
	static class SingleThreadedServerConfigStringConfiguration {
	}

	@Configuration
	@EnableAutoConfiguration
	@PropertySource(value = "classpath:server-single-threaded/single-threaded-application-config-string.yml",
			factory = YamlPropertySourceFactory.class)
	static class SingleThreadedServerConfigStringYamlConfiguration {
	}

	@Configuration
	@EnableAutoConfiguration
	@PropertySource("classpath:server-multi-threaded/multi-threaded-application.properties")
	static class MultiThreadedServerAcceptorConfiguration {
	}

	@Configuration
	@EnableAutoConfiguration
	@PropertySource("classpath:server-multi-threaded/multi-threaded-application-executor-factory.properties")
	static class MultiThreadedExecutorFactoryServerAcceptorConfiguration {
	}

	@Configuration
	@EnableAutoConfiguration
	@PropertySource("classpath:server-single-threaded/single-threaded-application-no-config-defined.properties")
	static class SingleThreadedServerAcceptorConfigurationWithCustomClientSettings {

		@Bean(name = "serverSessionSettings")
		public SessionSettings serverSessionSettings(SessionSettingsLocator serverSessionSettingsLocator) {
			return serverSessionSettingsLocator.loadSettings("classpath:quickfixj-server-extra.cfg");
		}
	}

	@Configuration
	@EnableAutoConfiguration
	@PropertySource("classpath:server-message-store/server-cachedfile-store-factory.properties")
	static class ServerCachedFileStoreFactoryConfiguration {
	}

	@Configuration
	@EnableAutoConfiguration
	@PropertySource("classpath:server-message-store/server-file-store-factory.properties")
	static class ServerFileStoreFactoryConfiguration {
	}

	@Configuration
	@EnableAutoConfiguration
	@PropertySource("classpath:server-message-store/server-jdbc-store-factory.properties")
	static class ServerJdbcStoreFactoryConfiguration {
	}

	@Configuration
	@EnableAutoConfiguration
	@PropertySource("classpath:server-message-store/server-memory-store-factory.properties")
	static class ServerMemoryStoreFactoryConfiguration {
	}

	@Configuration
	@EnableAutoConfiguration
	@PropertySource("classpath:server-message-store/server-noop-store-factory.properties")
	static class ServerNoopStoreFactoryConfiguration {
	}

	@Configuration
	@EnableAutoConfiguration
	@PropertySource("classpath:server-message-store/server-sleepycat-store-factory.properties")
	static class ServerSleepycatStoreFactoryConfiguration {
	}
}
