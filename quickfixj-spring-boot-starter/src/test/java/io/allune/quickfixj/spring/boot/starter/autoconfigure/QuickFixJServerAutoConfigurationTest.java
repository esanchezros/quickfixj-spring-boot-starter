/*
 * Copyright 2019 the original author or authors.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;

import javax.management.ObjectName;
import org.junit.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import io.allune.quickfixj.spring.boot.starter.EnableQuickFixJServer;
import io.allune.quickfixj.spring.boot.starter.application.EventPublisherApplicationAdapter;
import io.allune.quickfixj.spring.boot.starter.autoconfigure.server.QuickFixJServerAutoConfiguration;
import io.allune.quickfixj.spring.boot.starter.connection.ConnectorManager;
import io.allune.quickfixj.spring.boot.starter.exception.ConfigurationException;
import io.allune.quickfixj.spring.boot.starter.template.QuickFixJTemplate;
import quickfix.Acceptor;
import quickfix.Application;
import quickfix.ConfigError;
import quickfix.DefaultMessageFactory;
import quickfix.LogFactory;
import quickfix.MemoryStoreFactory;
import quickfix.MessageFactory;
import quickfix.MessageStoreFactory;
import quickfix.ScreenLogFactory;
import quickfix.SessionSettings;
import quickfix.SocketAcceptor;
import quickfix.ThreadedSocketAcceptor;

/**
 * @author Eduardo Sanchez-Ros
 */
public class QuickFixJServerAutoConfigurationTest {

	@Test
	public void testAutoConfiguredBeansSingleThreadedAcceptor() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(SingleThreadedServerAcceptorConfiguration.class);
		ConnectorManager serverConnectionManager = ctx.getBean("serverConnectionManager", ConnectorManager.class);
		assertThat(serverConnectionManager.isRunning()).isFalse();
		assertThat(serverConnectionManager.isAutoStartup()).isFalse();

		Acceptor serverAcceptor = ctx.getBean(Acceptor.class);
		assertThat(serverAcceptor).isInstanceOf(SocketAcceptor.class);

		hasAutoConfiguredBeans(ctx);
	}

	@Test
	public void testAutoConfiguredBeansMultiThreadedAcceptor() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(MultiThreadedServerAcceptorConfiguration.class);
		ConnectorManager serverConnectionManager = ctx.getBean("serverConnectionManager", ConnectorManager.class);
		assertThat(serverConnectionManager.isRunning()).isFalse();
		assertThat(serverConnectionManager.isAutoStartup()).isFalse();

		Acceptor serverAcceptor = ctx.getBean(Acceptor.class);
		assertThat(serverAcceptor).isInstanceOf(ThreadedSocketAcceptor.class);

		hasAutoConfiguredBeans(ctx);
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

		ObjectName serverInitiatorMBean = ctx.getBean("serverInitiatorMBean", ObjectName.class);
		assertThat(serverInitiatorMBean).isNotNull();

		QuickFixJTemplate serverQuickFixJTemplate = ctx.getBean("serverQuickFixJTemplate", QuickFixJTemplate.class);
		assertThat(serverQuickFixJTemplate).isNotNull();
	}

	@Test
	public void shouldCreateServerThreadedAcceptor() throws ConfigError {
		// Given
		Application application = mock(Application.class);
		MessageStoreFactory messageStoreFactory = mock(MessageStoreFactory.class);
		SessionSettings sessionSettings = mock(SessionSettings.class);
		LogFactory logFactory = mock(LogFactory.class);
		MessageFactory messageFactory = mock(MessageFactory.class);

		QuickFixJServerAutoConfiguration autoConfiguration = new QuickFixJServerAutoConfiguration();

		// When
		Acceptor acceptor = autoConfiguration.serverThreadedAcceptor(application, messageStoreFactory, sessionSettings, logFactory, messageFactory);

		// Then
		assertThat(acceptor).isNotNull();
		assertThat(acceptor).isInstanceOf(ThreadedSocketAcceptor.class);
	}

	@Test
	public void shouldThrowConfigurationExceptionCreatingServerInitiatorMBeanGivenNullInitiator() {
		// Given
		QuickFixJServerAutoConfiguration autoConfiguration = new QuickFixJServerAutoConfiguration();

		// When/Then
		assertThatExceptionOfType(ConfigurationException.class)
				.isThrownBy(() -> autoConfiguration.serverInitiatorMBean(null));
	}

	@Configuration
	@EnableAutoConfiguration
	@EnableQuickFixJServer
	@PropertySource("classpath:single-threaded-application.properties")
	static class SingleThreadedServerAcceptorConfiguration {

	}

	@Configuration
	@EnableAutoConfiguration
	@EnableQuickFixJServer
	@PropertySource("classpath:multi-threaded-application.properties")
	static class MultiThreadedServerAcceptorConfiguration {

	}
}