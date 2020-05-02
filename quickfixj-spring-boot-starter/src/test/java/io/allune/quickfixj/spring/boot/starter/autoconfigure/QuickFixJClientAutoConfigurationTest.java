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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import io.allune.quickfixj.spring.boot.starter.EnableQuickFixJClient;
import io.allune.quickfixj.spring.boot.starter.application.EventPublisherApplicationAdapter;
import io.allune.quickfixj.spring.boot.starter.autoconfigure.client.QuickFixJClientAutoConfiguration;
import io.allune.quickfixj.spring.boot.starter.connection.ConnectorManager;
import io.allune.quickfixj.spring.boot.starter.connection.SessionSettingsLocator;
import io.allune.quickfixj.spring.boot.starter.exception.ConfigurationException;
import io.allune.quickfixj.spring.boot.starter.template.QuickFixJTemplate;
import quickfix.Application;
import quickfix.ConfigError;
import quickfix.DefaultMessageFactory;
import quickfix.Initiator;
import quickfix.LogFactory;
import quickfix.MemoryStoreFactory;
import quickfix.MessageFactory;
import quickfix.MessageStoreFactory;
import quickfix.ScreenLogFactory;
import quickfix.SessionSettings;
import quickfix.SocketInitiator;
import quickfix.ThreadedSocketInitiator;

/**
 * @author Eduardo Sanchez-Ros
 */
public class QuickFixJClientAutoConfigurationTest {

	@Test
	public void testAutoConfiguredBeansSingleThreadedInitiator() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(SingleThreadedClientInitiatorConfiguration.class);
		ConnectorManager clientConnectionManager = ctx.getBean("clientConnectionManager", ConnectorManager.class);
		assertThat(clientConnectionManager.isRunning()).isFalse();
		assertThat(clientConnectionManager.isAutoStartup()).isFalse();

		Initiator clientInitiator = ctx.getBean(Initiator.class);
		assertThat(clientInitiator).isInstanceOf(SocketInitiator.class);

		hasAutoConfiguredBeans(ctx);
	}

	@Test
	public void testAutoConfiguredBeansMultiThreadedInitiator() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(MultiThreadedClientInitiatorConfiguration.class);
		ConnectorManager clientConnectionManager = ctx.getBean("clientConnectionManager", ConnectorManager.class);
		assertThat(clientConnectionManager.isRunning()).isFalse();
		assertThat(clientConnectionManager.isAutoStartup()).isFalse();

		Initiator clientInitiator = ctx.getBean(Initiator.class);
		assertThat(clientInitiator).isInstanceOf(ThreadedSocketInitiator.class);

		hasAutoConfiguredBeans(ctx);
	}

	@Test
	public void shouldCreateClientThreadedInitiator() throws ConfigError {
		// Given
		Application application = mock(Application.class);
		MessageStoreFactory messageStoreFactory = mock(MessageStoreFactory.class);
		SessionSettings sessionSettings = mock(SessionSettings.class);
		LogFactory logFactory = mock(LogFactory.class);
		MessageFactory messageFactory = mock(MessageFactory.class);

		QuickFixJClientAutoConfiguration autoConfiguration = new QuickFixJClientAutoConfiguration();

		// When
		Initiator initiator = autoConfiguration.clientThreadedInitiator(application, messageStoreFactory, sessionSettings, logFactory, messageFactory);

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

		QuickFixJTemplate clientQuickFixJTemplate = ctx.getBean("clientQuickFixJTemplate", QuickFixJTemplate.class);
		assertThat(clientQuickFixJTemplate).isNotNull();
	}

	@Configuration
	@EnableAutoConfiguration
	@EnableQuickFixJClient
	@PropertySource("classpath:single-threaded-application.properties")
	static class SingleThreadedClientInitiatorConfiguration {

	}

	@Configuration
	@EnableAutoConfiguration
	@EnableQuickFixJClient
	@PropertySource("classpath:multi-threaded-application.properties")
	static class MultiThreadedClientInitiatorConfiguration {

	}

	@Configuration
	@EnableAutoConfiguration
	@EnableQuickFixJClient
	@PropertySource("classpath:single-threaded-application-no-config-defined.properties")
	static class SingleThreadedClientInitiatorConfigurationWithCustomClientSettings {

		@Bean(name = "clientSessionSettings")
		public SessionSettings customClientSessionSettings() {
			return SessionSettingsLocator.loadSettings("classpath:/quickfixj-client-extra.cfg");
		}
	}
}