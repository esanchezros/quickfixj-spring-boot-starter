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
import org.springframework.core.io.DefaultResourceLoader;
import quickfix.ConfigError;
import quickfix.FieldConvertError;
import quickfix.SessionID;
import quickfix.SessionSettings;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Eduardo Sanchez-Ros
 */
public class SessionSettingsLocatorTest {

	@Test
	public void shouldLoadDefaultFromSystemProperty() {
		SessionSettingsLocator sessionSettingsLocator = new SessionSettingsLocator(new DefaultResourceLoader());
		SessionSettings settings = sessionSettingsLocator.loadSettings("classpath:quickfixj.cfg", null, null, null);
		assertThat(settings).isNotNull();

		File file = new File(SessionSettingsLocatorTest.class.getResource("/quickfixj.cfg").getFile());
		settings = sessionSettingsLocator.loadSettings(null, null, "file:///" + file.getAbsolutePath(), null);
		assertThat(settings).isNotNull();

		settings = sessionSettingsLocator.loadSettings(null, null, null, "classpath:quickfixj.cfg");
		assertThat(settings).isNotNull();
	}

	@Test
	public void shouldThrowSettingsNotFoundExceptionIfNoneFound() {
		SessionSettingsLocator sessionSettingsLocator = new SessionSettingsLocator(new DefaultResourceLoader());
		assertThatThrownBy(() -> sessionSettingsLocator.loadSettings(null, null, null, null))
				.isInstanceOf(SettingsNotFoundException.class);
	}

	@Test
	void testLoadSettingsFromString_validConfig() throws ConfigError, FieldConvertError {
		String configString = "[DEFAULT]\n" +
				"ConnectionType=initiator\n" +
				"BeginString=FIX.4.2\n" +
				"SenderCompID=CLIENT1\n" +
				"TargetCompID=SERVER\n" +
				"\n" +
				"[SESSION]\n" +
				"SocketConnectPort=5001\n" +
				"SocketConnectHost=localhost\n";

		SessionSettingsLocator sessionSettingsLocator = new SessionSettingsLocator(new DefaultResourceLoader());
		SessionSettings settings = sessionSettingsLocator.loadSettingsFromString(configString);

		assertThat("initiator").isEqualTo(settings.getString("ConnectionType"));
		assertThat("FIX.4.2").isEqualTo(settings.getString("BeginString"));
		assertThat("CLIENT1").isEqualTo(settings.getString("SenderCompID"));
		assertThat("SERVER").isEqualTo(settings.getString("TargetCompID"));

		SessionID sessionID = new SessionID("FIX.4.2", "CLIENT1", "SERVER");
		assertThat(5001).isEqualTo(settings.getLong(sessionID, "SocketConnectPort"));
		assertThat("localhost").isEqualTo(settings.getString(sessionID, "SocketConnectHost"));
	}

	@Test
	void testLoadSettingsFromString_emptyConfig() {
		String configString = "";
		SessionSettingsLocator sessionSettingsLocator = new SessionSettingsLocator(new DefaultResourceLoader());
		assertThatThrownBy(() -> sessionSettingsLocator.loadSettingsFromString(configString))
				.isInstanceOf(SettingsNotFoundException.class);
	}

	@Test
	void testLoadSettingsFromString_invalidConfig() {
		String configString = "[INVALID]\n" +
				"ThisIsNotAValidSetting";

		SessionSettingsLocator sessionSettingsLocator = new SessionSettingsLocator(new DefaultResourceLoader());
		assertDoesNotThrow(() -> {
			sessionSettingsLocator.loadSettingsFromString(configString);
		});
	}
}
