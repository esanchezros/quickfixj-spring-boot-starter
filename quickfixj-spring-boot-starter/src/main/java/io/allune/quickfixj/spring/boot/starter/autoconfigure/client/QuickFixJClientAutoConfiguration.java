package io.allune.quickfixj.spring.boot.starter.autoconfigure.client;

import io.allune.quickfixj.spring.boot.starter.autoconfigure.QuickFixJBootProperties;
import io.allune.quickfixj.spring.boot.starter.autoconfigure.QuickFixJConfigResourceCondition;
import io.allune.quickfixj.spring.boot.starter.connection.ConnectorManager;
import io.allune.quickfixj.spring.boot.starter.connection.SessionSettingsLocator;
import io.allune.quickfixj.spring.boot.starter.exception.ConfigurationException;
import org.quickfixj.jmx.JmxExporter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import quickfix.*;

import javax.management.JMException;
import javax.management.ObjectName;

/**
 * {@link EnableAutoConfiguration Auto-configuration} that configures the
 * {@link ConnectorManager} from the properties.
 *
 * @author Eduardo Sanchez-Ros
 */
@Configuration
@EnableConfigurationProperties(QuickFixJBootProperties.class)
@ConditionalOnBean(QuickFixJClientMarkerConfiguration.Marker.class)
@Conditional(QuickFixJClientAutoConfiguration.ClientConfigAvailableCondition.class)
public class QuickFixJClientAutoConfiguration {

    static final String SYSTEM_VARIABLE_QUICKFIXJ_CLIENT_CONFIG = "quickfixj.client.config";

    static final String FILESYSTEM_QUICKFIXJ_CLIENT_CONFIG = "file:./quickfixj-client.cfg";

    static final String CLASSPATH_QUICKFIXJ_CLIENT_CONFIG = "classpath:/quickfixj-client.cfg";

    @Bean
    @ConditionalOnMissingBean(name = "clientSessionSettings")
    public SessionSettings clientSessionSettings(QuickFixJBootProperties properties) {
        return SessionSettingsLocator.loadSettings(properties.getClient().getConfig(),
                SYSTEM_VARIABLE_QUICKFIXJ_CLIENT_CONFIG, FILESYSTEM_QUICKFIXJ_CLIENT_CONFIG,
                CLASSPATH_QUICKFIXJ_CLIENT_CONFIG);
    }

    @Bean
    @ConditionalOnMissingBean(name = "clientApplication")
    public Application clientApplication() {
        return new ApplicationAdapter();
    }

    @Bean
    @ConditionalOnMissingBean(name = "clientMessageStoreFactory")
    public MessageStoreFactory clientMessageStoreFactory() {
        return new MemoryStoreFactory();
    }

    @Bean
    @ConditionalOnMissingBean(name = "clientLogFactory")
    public LogFactory clientLogFactory() {
        return new ScreenLogFactory(true, true, true);
    }

    @Bean
    @ConditionalOnMissingBean(name = "clientMessageFactory")
    public MessageFactory clientMessageFactory() {
        return new DefaultMessageFactory();
    }

    @Bean
    @ConditionalOnMissingBean(name = "clientInitiator")
    public Initiator clientInitiator(Application clientApplication, MessageStoreFactory clientMessageStoreFactory,
                                     SessionSettings clientSessionSettings, LogFactory clientLogFactory,
                                     MessageFactory clientMessageFactory) throws ConfigError {

        return new SocketInitiator(clientApplication, clientMessageStoreFactory, clientSessionSettings,
                clientLogFactory, clientMessageFactory);
    }

    @Bean
    public ConnectorManager clientConnectionManager(Initiator clientInitiator, QuickFixJBootProperties properties) {
        ConnectorManager connectorManager = new ConnectorManager(clientInitiator);
        if (properties.getClient() != null) {
            connectorManager.setAutoStartup(properties.getClient().isAutoStartup());
            connectorManager.setPhase(properties.getClient().getPhase());
        }
        return connectorManager;
    }

    @Bean
    @ConditionalOnProperty(prefix = "quickfixj.client", name = "jmxEnabled", havingValue = "true")
    @ConditionalOnClass(JmxExporter.class)
    @ConditionalOnSingleCandidate(Initiator.class)
    @ConditionalOnMissingBean(name = "clientInitiatorMBean")
    public ObjectName clientInitiatorMBean(Initiator clientInitiator) {
        try {
            JmxExporter exporter = new JmxExporter();
            return exporter.register(clientInitiator);
        } catch (JMException e) {
            throw new ConfigurationException(e.getMessage(), e);
        }
    }

    /**
     * {@link ClientConfigAvailableCondition} that checks if the client configuration file is defined in
     * {@code quickfixj.client.config} configuration key or in the default locations.
     */
    static class ClientConfigAvailableCondition extends QuickFixJConfigResourceCondition {

        ClientConfigAvailableCondition() {
            super(SYSTEM_VARIABLE_QUICKFIXJ_CLIENT_CONFIG, "quickfixj.client", "config",
                    FILESYSTEM_QUICKFIXJ_CLIENT_CONFIG, CLASSPATH_QUICKFIXJ_CLIENT_CONFIG);
        }
    }
}