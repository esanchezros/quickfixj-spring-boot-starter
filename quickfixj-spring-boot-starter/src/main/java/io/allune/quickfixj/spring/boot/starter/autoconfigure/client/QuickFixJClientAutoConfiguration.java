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

package io.allune.quickfixj.spring.boot.starter.autoconfigure.client;

import javax.management.ObjectName;
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

import io.allune.quickfixj.spring.boot.starter.application.EventPublisherApplicationAdapter;
import io.allune.quickfixj.spring.boot.starter.autoconfigure.QuickFixJBootProperties;
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

	@Bean
	@ConditionalOnMissingBean(name = "clientSessionSettings")
	public SessionSettings clientSessionSettings(QuickFixJBootProperties properties) {
		return SessionSettingsLocator.loadSettings(properties.getClient().getConfig(),
				System.getProperty(SYSTEM_VARIABLE_QUICKFIXJ_CLIENT_CONFIG),
				"file:./" + QUICKFIXJ_CLIENT_CONFIG,
				"classpath:/" + QUICKFIXJ_CLIENT_CONFIG);
	}

	@Bean
	@ConditionalOnMissingBean(name = "clientApplication", value = Application.class)
	public Application clientApplication(ApplicationEventPublisher applicationEventPublisher) {
		return new EventPublisherApplicationAdapter(applicationEventPublisher);
	}

	@Bean
	@ConditionalOnMissingBean(name = "clientMessageStoreFactory", value = MessageStoreFactory.class)
	public MessageStoreFactory clientMessageStoreFactory() {
		return new MemoryStoreFactory();
	}

	@Bean
	@ConditionalOnMissingBean(name = "clientLogFactory", value = LogFactory.class)
	public LogFactory clientLogFactory(SessionSettings clientSessionSettings) {
		return new ScreenLogFactory(clientSessionSettings);
	}

	@Bean
	@ConditionalOnMissingBean(name = "clientMessageFactory", value = MessageFactory.class)
	public MessageFactory clientMessageFactory() {
		return new DefaultMessageFactory();
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnProperty(prefix = "quickfixj.client.concurrent", name = "enabled", havingValue = "false", matchIfMissing = true)
	public Initiator clientInitiator(
			Application clientApplication,
			MessageStoreFactory clientMessageStoreFactory,
			SessionSettings clientSessionSettings,
			LogFactory clientLogFactory,
			MessageFactory clientMessageFactory) throws ConfigError {

		return SocketInitiator.newBuilder()
				.withApplication(clientApplication)
				.withMessageStoreFactory(clientMessageStoreFactory)
				.withSettings(clientSessionSettings)
				.withLogFactory(clientLogFactory)
				.withMessageFactory(clientMessageFactory)
				.build();
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnProperty(prefix = "quickfixj.client.concurrent", name = "enabled", havingValue = "true")
	public Initiator clientThreadedInitiator(
			Application clientApplication,
			MessageStoreFactory clientMessageStoreFactory,
			SessionSettings clientSessionSettings,
			LogFactory clientLogFactory,
			MessageFactory clientMessageFactory) throws ConfigError {

		return ThreadedSocketInitiator.newBuilder()
				.withApplication(clientApplication)
				.withMessageStoreFactory(clientMessageStoreFactory)
				.withSettings(clientSessionSettings)
				.withLogFactory(clientLogFactory)
				.withMessageFactory(clientMessageFactory)
				.build();
	}

	@Bean
	public ConnectorManager clientConnectionManager(Initiator clientInitiator, QuickFixJBootProperties properties) {
		ConnectorManager connectorManager = new ConnectorManager(clientInitiator);
		if (properties.getClient() != null) {
			connectorManager.setAutoStartup(properties.getClient().isAutoStartup());
			connectorManager.setPhase(properties.getClient().getPhase());
		}
		return connectorManager;
	}

	@Bean
	@ConditionalOnProperty(prefix = "quickfixj.client", name = "jmx-enabled", havingValue = "true")
	@ConditionalOnClass(JmxExporter.class)
	@ConditionalOnSingleCandidate(Initiator.class)
	@ConditionalOnMissingBean(name = "clientInitiatorMBean", value = ObjectName.class)
	public ObjectName clientInitiatorMBean(Initiator clientInitiator) {
		try {
			JmxExporter exporter = new JmxExporter();
			return exporter.register(clientInitiator);
		} catch (Exception e) {
			throw new ConfigurationException(e.getMessage(), e);
		}
	}

	@Bean
	@ConditionalOnMissingBean(name = "clientQuickFixJTemplate", value = QuickFixJTemplate.class)
	public QuickFixJTemplate clientQuickFixJTemplate() {
		return new QuickFixJTemplate();
	}
}
