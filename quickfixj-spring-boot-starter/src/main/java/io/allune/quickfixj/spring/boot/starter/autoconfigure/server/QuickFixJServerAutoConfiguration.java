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

package io.allune.quickfixj.spring.boot.starter.autoconfigure.server;

import static org.quickfixj.jmx.JmxExporter.REGISTRATION_REPLACE_EXISTING;

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
 * {@link EnableAutoConfiguration Auto-configuration} for QuickFixJ Server (Acceptor)
 *
 * @author Eduardo Sanchez-Ros
 */
@Configuration
@EnableConfigurationProperties(QuickFixJBootProperties.class)
@ConditionalOnBean(value = QuickFixJServerMarkerConfiguration.Marker.class)
public class QuickFixJServerAutoConfiguration {

	private static final String SYSTEM_VARIABLE_QUICKFIXJ_SERVER_CONFIG = "quickfixj.server.config";

	private static final String QUICKFIXJ_SERVER_CONFIG = "quickfixj-server.cfg";

	@Bean
	@ConditionalOnMissingBean(name = "serverSessionSettings")
	public SessionSettings serverSessionSettings(QuickFixJBootProperties properties) {
		return SessionSettingsLocator.loadSettings(properties.getServer().getConfig(),
				System.getProperty(SYSTEM_VARIABLE_QUICKFIXJ_SERVER_CONFIG),
				"file:./" + QUICKFIXJ_SERVER_CONFIG,
				"classpath:/" + QUICKFIXJ_SERVER_CONFIG);
	}

	@Bean
	@ConditionalOnMissingBean(name = "serverApplication")
	public Application serverApplication(ApplicationEventPublisher applicationEventPublisher) {
		return new EventPublisherApplicationAdapter(applicationEventPublisher);
	}

	@Bean
	@ConditionalOnMissingBean(name = "serverMessageStoreFactory")
	public MessageStoreFactory serverMessageStoreFactory() {
		return new MemoryStoreFactory();
	}

	@Bean
	@ConditionalOnMissingBean(name = "serverLogFactory")
	public LogFactory serverLogFactory(SessionSettings serverSessionSettings) {
		return new ScreenLogFactory(serverSessionSettings);
	}

	@Bean
	@ConditionalOnMissingBean(name = "serverMessageFactory")
	public MessageFactory serverMessageFactory() {
		return new DefaultMessageFactory();
	}

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

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnProperty(prefix = "quickfixj.server.concurrent", name = "enabled", havingValue = "true")
	public Acceptor serverThreadedAcceptor(
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

	@Bean
	public ConnectorManager serverConnectionManager(Acceptor serverAcceptor, QuickFixJBootProperties properties) {
		ConnectorManager connectorManager = new ConnectorManager(serverAcceptor);
		connectorManager.setAutoStartup(properties.getServer().isAutoStartup());
		connectorManager.setPhase(properties.getServer().getPhase());
		return connectorManager;
	}

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

	@Bean
	@ConditionalOnMissingBean(name = "serverQuickFixJTemplate")
	public QuickFixJTemplate serverQuickFixJTemplate() {
		return new QuickFixJTemplate();
	}
}
