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
package io.allune.quickfixj.spring.boot.starter.connection;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.junit.Test;
import org.springframework.core.io.DefaultResourceLoader;

import io.allune.quickfixj.spring.boot.starter.exception.SettingsNotFoundException;
import quickfix.SessionSettings;

/**
 * @author Eduardo Sanchez-Ros
 */
public class SessionSettingsLocatorTest {

	@Test
	public void shouldLoadDefaultFromSystemProperty() {
		SessionSettingsLocator sessionSettingsLocator = new SessionSettingsLocator(new DefaultResourceLoader());
		SessionSettings settings = sessionSettingsLocator.loadSettings("classpath:quickfixj.cfg", null, null, null);
		assertThat(settings).isNotNull();

		File file = new File(SessionSettingsLocatorTest.class.getClass().getResource("/quickfixj.cfg").getFile());
		settings = sessionSettingsLocator.loadSettings(null, null, "file:///" + file.getAbsolutePath(), null);
		assertThat(settings).isNotNull();

		settings = sessionSettingsLocator.loadSettings(null, null, null, "classpath:quickfixj.cfg");
		assertThat(settings).isNotNull();
	}

	@Test(expected = SettingsNotFoundException.class)
	public void shouldThrowSettingsNotFoundExceptionIfNoneFound() {
		SessionSettingsLocator sessionSettingsLocator = new SessionSettingsLocator(new DefaultResourceLoader());
		sessionSettingsLocator.loadSettings(null, null, null, null);
	}
}