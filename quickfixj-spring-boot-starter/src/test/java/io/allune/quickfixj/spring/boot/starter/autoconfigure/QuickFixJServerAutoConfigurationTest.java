/*
 * Copyright 2017 the original author or authors.
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
@SpringBootTest(
        properties = {
                "quickfixj.server.autoStartup=false",
                "quickfixj.server.config=classpath:quickfixj.cfg",
                "quickfixj.server.jmx-enabled=true"
        })
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

    @Autowired
    private SessionSettings serverSessionSettings;

    @Autowired
    private ObjectName serverInitiatorMBean;

    @Test
    public void testAutoConfiguredBeans() {
        assertThat(serverConnectionManager.isRunning()).isFalse();
        assertThat(serverConnectionManager.isAutoStartup()).isFalse();
        assertThat(serverAcceptor).isInstanceOf(SocketAcceptor.class);
        assertThat(serverApplication).isInstanceOf(ApplicationAdapter.class);
        assertThat(serverMessageStoreFactory).isInstanceOf(MemoryStoreFactory.class);
        assertThat(serverLogFactory).isInstanceOf(ScreenLogFactory.class);
        assertThat(serverMessageFactory).isInstanceOf(DefaultMessageFactory.class);
        assertThat(serverSessionSettings).isNotNull();
        assertThat(serverInitiatorMBean).isNotNull();
    }

    @Configuration
    @EnableAutoConfiguration
    @EnableQuickFixJServer
    static class TestConfig {
    }
}