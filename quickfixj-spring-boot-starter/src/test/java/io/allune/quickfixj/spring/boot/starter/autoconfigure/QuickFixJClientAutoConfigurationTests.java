package io.allune.quickfixj.spring.boot.starter.autoconfigure;

import io.allune.quickfixj.spring.boot.starter.EnableQuickFixJClient;
import io.allune.quickfixj.spring.boot.starter.connection.ConnectorManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;
import quickfix.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Eduardo Sanchez-Ros
 */
@RunWith(SpringRunner.class)
@SpringBootTest(properties = {"quickfixj.client.autoStartup=false"})
public class QuickFixJClientAutoConfigurationTests {

    @Autowired
    private ConnectorManager clientConnectionManager;

    @Autowired
    private Initiator clientInitiator;

    @Autowired
    private Application clientApplication;

    @Autowired
    private MessageStoreFactory clientMessageStoreFactory;

    @Autowired
    private LogFactory clientLogFactory;

    @Autowired
    private MessageFactory clientMessageFactory;

    @Test
    public void testAutoConfiguredBeans() {
        assertThat(clientConnectionManager.isRunning()).isFalse();
        assertThat(clientConnectionManager.isAutoStartup()).isFalse();
        assertThat(clientInitiator).isInstanceOf(SocketInitiator.class);
        assertThat(clientApplication).isInstanceOf(ApplicationAdapter.class);
        assertThat(clientMessageStoreFactory).isInstanceOf(MemoryStoreFactory.class);
        assertThat(clientLogFactory).isInstanceOf(ScreenLogFactory.class);
        assertThat(clientMessageFactory).isInstanceOf(DefaultMessageFactory.class);

    }

    @Configuration
    @EnableAutoConfiguration
    @EnableQuickFixJClient
    static class TestConfig {

        @Bean
        public SessionSettings clientSessionSettings() {
            return new SessionSettings();
        }
    }
}