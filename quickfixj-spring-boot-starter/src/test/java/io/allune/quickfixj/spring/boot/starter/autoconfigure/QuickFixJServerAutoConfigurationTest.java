package io.allune.quickfixj.spring.boot.starter.autoconfigure;

import io.allune.quickfixj.spring.boot.starter.EnableQuickFixJServer;
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

import javax.management.ObjectName;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Eduardo Sanchez-Ros
 */
@RunWith(SpringRunner.class)
@SpringBootTest(properties = {"quickfixj.server.autoStartup=false", "quickfixj.server.config="})
public class QuickFixJServerAutoConfigurationTest {

    @Autowired
    private ConnectorManager serverConnectionManager;

    @Autowired
    private Acceptor serverAcceptor;

    @Autowired
    private Application serverApplication;

    @Autowired
    private MessageStoreFactory serverMessageStoreFactory;

    @Autowired
    private LogFactory serverLogFactory;

    @Autowired
    private MessageFactory serverMessageFactory;

    @Test
    public void testAutoConfiguredBeans() {
        assertThat(serverConnectionManager.isRunning()).isFalse();
        assertThat(serverConnectionManager.isAutoStartup()).isFalse();
        assertThat(serverAcceptor).isInstanceOf(SocketAcceptor.class);
        assertThat(serverApplication).isInstanceOf(ApplicationAdapter.class);
        assertThat(serverMessageStoreFactory).isInstanceOf(MemoryStoreFactory.class);
        assertThat(serverLogFactory).isInstanceOf(ScreenLogFactory.class);
        assertThat(serverMessageFactory).isInstanceOf(DefaultMessageFactory.class);
    }

    @Configuration
    @EnableAutoConfiguration
    @EnableQuickFixJServer
    static class TestConfig {

        @Bean
        public SessionSettings serverSessionSettings() {
            return new SessionSettings();
        }
    }
}