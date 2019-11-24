package io.allune.quickfixj.spring.boot.starter.template;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import quickfix.Session;
import quickfix.SessionID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Session.class)
public class DefaultSessionLookupHandlerTest {

    @Test
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