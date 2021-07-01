/*
 * Copyright 2017-2020 the original author or authors.
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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.core.io.DefaultResourceLoader;
import quickfix.SessionSettings;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Eduardo Sanchez-Ros
 */
public class SessionSettingsLocatorTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void shouldLoadDefaultFromSystemProperty() throws URISyntaxException {
		SessionSettingsLocator sessionSettingsLocator = new SessionSettingsLocator(new DefaultResourceLoader());
		SessionSettings settings = sessionSettingsLocator.loadSettings("classpath:quickfixj.cfg", null, null, null);
		assertThat(settings).isNotNull();

		URL resource = SessionSettingsLocatorTest.class.getResource("/quickfixj.cfg");
		File file = Paths.get(resource.toURI()).toFile();
		settings = sessionSettingsLocator.loadSettings(null, null, "file://" + file.getAbsolutePath(), null);
		assertThat(settings).isNotNull();

		settings = sessionSettingsLocator.loadSettings(null, null, null, "classpath:quickfixj.cfg");
		assertThat(settings).isNotNull();
	}

	@Test
	public void shouldThrowSettingsNotFoundExceptionIfNoneFound() {
		SessionSettingsLocator sessionSettingsLocator = new SessionSettingsLocator(new DefaultResourceLoader());
		thrown.expect(SettingsNotFoundException.class);
		sessionSettingsLocator.loadSettings(null, null, null, null);
	}
}