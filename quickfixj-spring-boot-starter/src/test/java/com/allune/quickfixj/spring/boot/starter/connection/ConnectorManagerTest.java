package com.allune.quickfixj.spring.boot.starter.connection;

import org.junit.Test;
import quickfix.Connector;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author Eduardo Sanchez-Ros
 */
public class ConnectorManagerTest {

    @Test
    public void clientLifecycle() throws Exception {

        Connector connector = mock(Connector.class);
        ConnectorManager connectorManager = new ConnectorManager(connector);

        connectorManager.start();
        assertTrue(connectorManager.isRunning());

        connectorManager.stop();
        assertFalse(connectorManager.isRunning());

        verify(connector).start();
        verify(connector).stop();
    }
}