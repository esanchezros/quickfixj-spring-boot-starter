package io.allune.quickfixj.spring.boot.starter.connection;

import io.allune.quickfixj.spring.boot.starter.exception.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.util.Assert;
import quickfix.ConfigError;
import quickfix.Connector;
import quickfix.RuntimeError;

/**
 * Connection manager for a connector. The connection is initialised when the application context is created and closed
 * (including logging out all active sessions) when the application context is closed
 *
 * @author Eduardo Sanchez-Ros
 */
public class ConnectorManager implements SmartLifecycle {

    private final Log logger = LogFactory.getLog(getClass());

    private final Connector connector;

    private final Object lifecycleMonitor = new Object();

    private boolean autoStartup = true;

    private int phase = Integer.MAX_VALUE;

    private boolean running = false;

    public ConnectorManager(Connector connector) {
        Assert.notNull(connector, "'connector' must not be null");
        this.connector = connector;
    }

    /**
     * Set whether to auto-connect to the remote endpoint after this connector manager
     * has been initialized and the Spring context has been refreshed.
     * <p>Default is "true".
     */
    public void setAutoStartup(boolean autoStartup) {
        this.autoStartup = autoStartup;
    }

    /**
     * Return the value for the 'autoStartup' property.
     */
    @Override
    public boolean isAutoStartup() {
        return this.autoStartup;
    }

    /**
     * Specify the phase in which this connection manager should be started and stopped.
     */
    public void setPhase(int phase) {
        this.phase = phase;
    }

    /**
     * Return the phase in which this connector manager will be started and stopped.
     */
    @Override
    public int getPhase() {
        return this.phase;
    }

    /**
     * Start the connector, accepting new connections
     */
    @Override
    public void start() {
        synchronized (this.lifecycleMonitor) {
            // Check whether there is already an instance running
            if (!isRunning()) {
                if (logger.isInfoEnabled()) {
                    logger.info("start: Starting ConnectorManager");
                }
                try {
                    // Start the connection with the server
                    connector.start();
                } catch (ConfigError | RuntimeError ex) {
                    throw new ConfigurationException(ex.getMessage(), ex);
                } catch (Throwable ex) {
                    throw new IllegalStateException("Could not start the connector", ex);
                }

                running = true;
            }
        }
    }

    /**
     * Stop this connector, logging out existing sessions, closing their connections, and stopping to accept new
     * connections.
     */
    @Override
    public void stop() {
        synchronized (this.lifecycleMonitor) {
            if (isRunning()) {
                if (logger.isInfoEnabled()) {
                    logger.info("stop: Stopping ConnectorManager");
                }
                try {
                    // Stop the connection with FIX acceptor
                    connector.stop();
                } finally {
                    running = false;
                }
            }
        }
    }

    /**
     * Stop this connector, invoking the specific callback once all the sessions have been logged out, all connections
     * closed and it has stopped accepting new connections.
     */
    @Override
    public void stop(Runnable callback) {
        synchronized (this.lifecycleMonitor) {
            stop();
            callback.run();
        }
    }

    /**
     * Determine whether this connector is currently running,
     * that is, whether it has been started and not stopped yet.
     * @see #start()
     * @see #stop()
     */
    @Override
    public boolean isRunning() {
        synchronized (this.lifecycleMonitor) {
            return running;
        }
    }
}