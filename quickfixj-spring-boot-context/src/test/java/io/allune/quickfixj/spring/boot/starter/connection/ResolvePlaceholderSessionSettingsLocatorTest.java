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
package io.allune.quickfixj.spring.boot.starter.connection;

import io.allune.quickfixj.spring.boot.starter.exception.SettingsNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.DefaultResourceLoader;
import quickfix.ConfigError;
import quickfix.SessionSettings;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Richard Jones
 */
public class ResolvePlaceholderSessionSettingsLocatorTest {

	@Test
	void shouldResolveDefaults() throws ConfigError {
		StandardEnvironment environment = new StandardEnvironment();

		SessionSettingsLocator sessionSettingsLocator = new ResolvePlaceholderSessionSettingsLocator(new DefaultResourceLoader(), environment);
		SessionSettings settings = sessionSettingsLocator.loadSettings("classpath:quickfixj-placeholders.cfg", null, null, null);

		assertThat("00:00:00").isEqualTo(settings.getString("StartTime"));
		assertThat("00:00:00").isEqualTo(settings.getString("EndTime"));
		assertThat("30").isEqualTo(settings.getString("HeartBtInt"));
		assertThat("5").isEqualTo(settings.getString("ReconnectInterval"));

	}

	@Test
	void shouldResolvePlaceholders() throws ConfigError {
		StandardEnvironment environment = new StandardEnvironment();
		Map<String, Object> props = Map.of(
			"START_TIME", "00:01:00",
			"END_TIME", "23:59:00"
		);
		environment.getPropertySources().addFirst(
			new MapPropertySource("test", props)
		);

		SessionSettingsLocator sessionSettingsLocator = new ResolvePlaceholderSessionSettingsLocator(new DefaultResourceLoader(), environment);
		SessionSettings settings = sessionSettingsLocator.loadSettings("classpath:quickfixj-placeholders.cfg", null, null, null);


		assertThat("00:01:00").isEqualTo(settings.getString("StartTime"));
		assertThat("23:59:00").isEqualTo(settings.getString("EndTime"));
		assertThat("30").isEqualTo(settings.getString("HeartBtInt"));
		assertThat("5").isEqualTo(settings.getString("ReconnectInterval"));
	}

	@Test
	void shouldFailWhenPlaceholderCannotBeResolved() {
		StandardEnvironment environment = new StandardEnvironment();

		SessionSettingsLocator sessionSettingsLocator = new ResolvePlaceholderSessionSettingsLocator(new DefaultResourceLoader(), environment);

		assertThatThrownBy(() -> sessionSettingsLocator.loadSettings("classpath:quickfixj-placeholders-required.cfg", null, null, null))
			.isInstanceOf(SettingsNotFoundException.class)
			.hasRootCauseInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("START_TIME");
	}
}
