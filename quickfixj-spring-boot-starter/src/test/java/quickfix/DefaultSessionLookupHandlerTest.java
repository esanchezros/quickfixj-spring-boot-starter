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
package quickfix;

import io.allune.quickfixj.spring.boot.starter.template.DefaultSessionLookupHandler;
import io.allune.quickfixj.spring.boot.starter.template.UnitTestApplication;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Eduardo Sanchez-Ros
 */
public class DefaultSessionLookupHandlerTest {

	@Test
	public void shouldReturnSessionGivenSessionId() throws IOException {
		// Given
		long systemTime = System.currentTimeMillis();
		SessionID sessionID = new SessionID("FIX.4.2", "SENDER" + systemTime, "TARGET" + systemTime);
		SessionSettings settings = new SessionSettings();
		ScreenLogFactory factory = new ScreenLogFactory(settings);

		try (Session expectedSession = new Session(new UnitTestApplication(), new MemoryStoreFactory(),
				sessionID, new DefaultDataDictionaryProvider(), null, factory,
				new DefaultMessageFactory(), 0)) {
			Session.registerSession(expectedSession);

			DefaultSessionLookupHandler defaultSessionLookupHandler = new DefaultSessionLookupHandler();

			// When
			Session actualSession = defaultSessionLookupHandler.lookupBySessionID(sessionID);

			// Then
			assertThat(actualSession).isEqualTo(expectedSession);
		}
	}
}