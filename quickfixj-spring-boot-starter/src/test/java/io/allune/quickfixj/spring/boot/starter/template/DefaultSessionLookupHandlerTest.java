package io.allune.quickfixj.spring.boot.starter.template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import quickfix.Session;
import quickfix.SessionID;

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