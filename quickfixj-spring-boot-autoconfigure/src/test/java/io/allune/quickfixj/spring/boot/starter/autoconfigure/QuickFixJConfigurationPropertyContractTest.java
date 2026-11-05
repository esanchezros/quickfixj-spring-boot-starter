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
package io.allune.quickfixj.spring.boot.starter.autoconfigure;

import io.allune.quickfixj.spring.boot.starter.autoconfigure.client.QuickFixJClientActuatorAutoConfiguration;
import io.allune.quickfixj.spring.boot.starter.autoconfigure.client.QuickFixJClientAutoConfiguration;
import io.allune.quickfixj.spring.boot.starter.autoconfigure.server.QuickFixJServerActuatorAutoConfiguration;
import io.allune.quickfixj.spring.boot.starter.autoconfigure.server.QuickFixJServerAutoConfiguration;
import io.allune.quickfixj.spring.boot.starter.autoconfigure.server.QuickFixJServerDynamicProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import quickfix.CachedFileStoreFactory;
import quickfix.FileLogFactory;
import quickfix.MemoryStoreFactory;
import quickfix.ScreenLogFactory;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings({"removal", "PMD.JUnitTestsShouldIncludeAssert"})
class QuickFixJConfigurationPropertyContractTest {

	private final ApplicationContextRunner clientContextRunner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(
					QuickFixJClientAutoConfiguration.class,
					QuickFixJClientActuatorAutoConfiguration.class))
			.withPropertyValues(
					"quickfixj.client.enabled=true",
					"quickfixj.client.auto-startup=false",
					"quickfixj.client.config=classpath:quickfixj-client.cfg");

	private final ApplicationContextRunner serverContextRunner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(
					QuickFixJServerAutoConfiguration.class,
					QuickFixJServerActuatorAutoConfiguration.class))
			.withPropertyValues(
					"quickfixj.server.enabled=true",
					"quickfixj.server.auto-startup=false",
					"quickfixj.server.config=classpath:quickfixj-server.cfg");

	@Test
	void canonicalPropertiesBindAndControlAutoConfiguration() {
		clientContextRunner.withPropertyValues(
					"quickfixj.client.message-store-factory=cachedfile",
					"quickfixj.client.log-factory=file",
					"quickfixj.client.actuator.enabled=true")
				.run(context -> {
					assertThat(context).hasNotFailed();
					QuickFixJBootProperties properties = context.getBean(QuickFixJBootProperties.class);
					assertThat(properties.getClient().getMessageStoreFactory()).isEqualTo(MessageStoreMethod.CACHEDFILE);
					assertThat(properties.getClient().getLogFactory()).isEqualTo(LogMethod.FILE);
					assertThat(properties.getClient().getActuator().isEnabled()).isTrue();
					assertThat(context.getBean("clientMessageStoreFactory")).isInstanceOf(CachedFileStoreFactory.class);
					assertThat(context.getBean("clientLogFactory")).isInstanceOf(FileLogFactory.class);
					assertThat(context).hasBean("sessionSchedule");
				});
	}

	@Test
	void accidentalMetadataPropertiesRemainNonOperational() {
		clientContextRunner.withPropertyValues(
					"quickfixj.client.message-store-method=jdbc",
					"quickfixj.client.log-method=jdbc",
					"quickfixj.client.health.enabled=true")
				.run(context -> {
					assertThat(context).hasNotFailed();
					QuickFixJBootProperties properties = context.getBean(QuickFixJBootProperties.class);
					assertThat(properties.getClient().getMessageStoreFactory()).isEqualTo(MessageStoreMethod.MEMORY);
					assertThat(properties.getClient().getLogFactory()).isEqualTo(LogMethod.SCREEN);
					assertThat(properties.getClient().getActuator().isEnabled()).isFalse();
					assertThat(properties.getClient().getMessageStoreMethod()).isEqualTo(MessageStoreMethod.JDBC);
					assertThat(properties.getClient().getLogMethod()).isEqualTo(LogMethod.JDBC);
					assertThat(properties.getClient().getHealth().isEnabled()).isTrue();
					assertThat(context.getBean("clientMessageStoreFactory")).isInstanceOf(MemoryStoreFactory.class);
					assertThat(context.getBean("clientLogFactory")).isInstanceOf(ScreenLogFactory.class);
					assertThat(context).doesNotHaveBean("sessionSchedule");
				});
	}

	@Test
	void canonicalServerPropertiesBindAndControlAutoConfiguration() {
		serverContextRunner.withPropertyValues(
					"quickfixj.server.message-store-factory=cachedfile",
					"quickfixj.server.log-factory=file",
					"quickfixj.server.actuator.enabled=true")
				.run(context -> {
					assertThat(context).hasNotFailed();
					QuickFixJBootProperties properties = context.getBean(QuickFixJBootProperties.class);
					assertThat(properties.getServer().getMessageStoreFactory()).isEqualTo(MessageStoreMethod.CACHEDFILE);
					assertThat(properties.getServer().getLogFactory()).isEqualTo(LogMethod.FILE);
					assertThat(properties.getServer().getActuator().isEnabled()).isTrue();
					assertThat(context.getBean("serverMessageStoreFactory")).isInstanceOf(CachedFileStoreFactory.class);
					assertThat(context.getBean("serverLogFactory")).isInstanceOf(FileLogFactory.class);
					assertThat(context).hasBean("sessionSchedule");
				});
	}

	@Test
	void accidentalServerMetadataPropertiesRemainNonOperational() {
		serverContextRunner.withPropertyValues(
					"quickfixj.server.message-store-method=jdbc",
					"quickfixj.server.log-method=jdbc",
					"quickfixj.server.health.enabled=true")
				.run(context -> {
					assertThat(context).hasNotFailed();
					QuickFixJBootProperties properties = context.getBean(QuickFixJBootProperties.class);
					assertThat(properties.getServer().getMessageStoreFactory()).isEqualTo(MessageStoreMethod.MEMORY);
					assertThat(properties.getServer().getLogFactory()).isEqualTo(LogMethod.SCREEN);
					assertThat(properties.getServer().getActuator().isEnabled()).isFalse();
					assertThat(properties.getServer().getMessageStoreMethod()).isEqualTo(MessageStoreMethod.JDBC);
					assertThat(properties.getServer().getLogMethod()).isEqualTo(LogMethod.JDBC);
					assertThat(properties.getServer().getHealth().isEnabled()).isTrue();
					assertThat(context.getBean("serverMessageStoreFactory")).isInstanceOf(MemoryStoreFactory.class);
					assertThat(context.getBean("serverLogFactory")).isInstanceOf(ScreenLogFactory.class);
					assertThat(context).doesNotHaveBean("sessionSchedule");
				});
	}

	@Test
	void serverDynamicPropertyHasDedicatedTypedConfiguration() {
		serverContextRunner.withPropertyValues(
					"quickfixj.server.concurrent.enabled=true",
					"quickfixj.server.dynamic.enabled=true")
				.run(context -> {
					assertThat(context).hasNotFailed();
					assertThat(context.getBean(QuickFixJServerDynamicProperties.class).isEnabled()).isTrue();
				});
	}

	@Test
	void misspelledLegacyEnumValueDoesNotBecomeOperational() {
		clientContextRunner.withPropertyValues("quickfixj.client.message-store-factory=cachaedfile")
				.run(context -> assertThat(context).hasFailed());
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"quickfixj.client.concurrent.use-default-executor-factory=true",
			"quickfixj.client.concurrent.useDefaultExecutorFactory=true"
	})
	void clientSupportsCanonicalAndLegacyExecutorPropertySpellings(String property) {
		clientContextRunner.withPropertyValues(property)
				.run(context -> {
					assertThat(context).hasNotFailed();
					assertThat(context).hasBean("clientExecutorFactory");
					assertThat(context).hasBean("clientTaskExecutor");
				});
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"quickfixj.server.concurrent.use-default-executor-factory=true",
			"quickfixj.server.concurrent.useDefaultExecutorFactory=true"
	})
	void serverSupportsCanonicalAndLegacyExecutorPropertySpellings(String property) {
		serverContextRunner.withPropertyValues(property)
				.run(context -> {
					assertThat(context).hasNotFailed();
					assertThat(context).hasBean("serverExecutorFactory");
					assertThat(context).hasBean("serverTaskExecutor");
				});
	}

	@Test
	void canonicalExecutorPropertyTakesPrecedenceOverLegacySpelling() {
		clientContextRunner.withPropertyValues(
					"quickfixj.client.concurrent.use-default-executor-factory=false",
					"quickfixj.client.concurrent.useDefaultExecutorFactory=true")
				.run(context -> {
					assertThat(context).hasNotFailed();
					assertThat(context).doesNotHaveBean("clientExecutorFactory");
					assertThat(context).doesNotHaveBean("clientTaskExecutor");
				});
	}

	@Test
	void retainsLegacyAccessorDescriptors() throws NoSuchMethodException {
		assertThat(ConnectorConfig.class.getMethod("getMessageStoreMethod").getReturnType())
				.isEqualTo(MessageStoreMethod.class);
		assertThat(ConnectorConfig.class.getMethod("setMessageStoreMethod", MessageStoreMethod.class))
				.isNotNull();
		assertThat(ConnectorConfig.class.getMethod("getLogMethod").getReturnType())
				.isEqualTo(LogMethod.class);
		assertThat(ConnectorConfig.class.getMethod("setLogMethod", LogMethod.class)).isNotNull();
		assertThat(ConnectorConfig.class.getMethod("getHealth").getReturnType()).isEqualTo(Health.class);
		assertThat(ConnectorConfig.class.getMethod("setHealth", Health.class)).isNotNull();
	}
}
