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
package io.allune.quickfixj.spring.boot.starter.template;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import quickfix.Session;
import quickfix.SessionID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * @author Eduardo Sanchez-Ros
 */
@RunWith(PowerMockRunner.class)
public class DefaultSessionLookupHandlerTest {

	@Test
	@PrepareForTest(Session.class)
	public void shouldReturnSessionGivenSessionId() {
		// Given
		mockStatic(Session.class);
		SessionID sessionID = mock(SessionID.class);
		Session expectedSession = mock(Session.class);
		when(Session.lookupSession(sessionID)).thenReturn(expectedSession);
		DefaultSessionLookupHandler defaultSessionLookupHandler = new DefaultSessionLookupHandler();

		// When
		Session actualSession = defaultSessionLookupHandler.lookupBySessionID(sessionID);

		// Then
		assertThat(actualSession).isEqualTo(expectedSession);
	}
}