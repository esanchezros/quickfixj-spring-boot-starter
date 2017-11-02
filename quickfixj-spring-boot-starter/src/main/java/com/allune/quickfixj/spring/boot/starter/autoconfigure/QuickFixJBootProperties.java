package com.allune.quickfixj.spring.boot.starter.autoconfigure;

import com.allune.quickfixj.spring.boot.starter.connection.ConnectorManager;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Holds all the relevant starter properties which can be configured with
 * Spring Boots application.properties / application.yml configuration files.
 *
 * @author Eduardo Sanchez-Ros
 */
@Data
@ConfigurationProperties(prefix = QuickFixJBootProperties.PROPERTY_PREFIX)
public class QuickFixJBootProperties {

    public static final String PROPERTY_PREFIX = "quickfixj";

    private Client client = new Client();

    private Server server = new Server();

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Client {

        /**
         * Whether the {@link ConnectorManager} should get started automatically
         */
        private boolean autoStartup = true;

        /**
         * The phase value of the {@link ConnectorManager}.
         */
        private int phase = Integer.MAX_VALUE;

        /**
         * The location of the configuration file to use to initialize QuickFixJ client.
         */
        private String config;

        /**
         * Whether to register the Jmx MBeans for the client
         */
        private boolean jmxEnabled = false;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Server {

        /**
         * Whether the {@link ConnectorManager} should get started automatically
         */
        private boolean autoStartup = true;

        /**
         * The phase value of the {@link ConnectorManager}.
         */
        private int phase = Integer.MAX_VALUE;

        /**
         * The location of the configuration file to use to initialize QuickFixJ client.
         */
        private String config;

        /**
         * Whether to register the Jmx MBeans for the server
         */
        private boolean jmxEnabled = false;
    }
}
