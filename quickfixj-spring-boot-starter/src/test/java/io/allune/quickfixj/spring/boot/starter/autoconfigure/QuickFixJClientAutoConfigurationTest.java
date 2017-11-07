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

import io.allune.quickfixj.spring.boot.starter.EnableQuickFixJClient;
import io.allune.quickfixj.spring.boot.starter.autoconfigure.client.QuickFixJClientAutoConfiguration;
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
                "quickfixj.client.autoStartup=false",
                "quickfixj.client.config=classpath:quickfixj.cfg",
                "quickfixj.client.jmx-enabled=true"
        })
public class QuickFixJClientAutoConfigurationTest {

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

    @Autowired
    private SessionSettings clientSessionSettings;

    @Autowired
    private ObjectName clientInitiatorMBean;

    @Test
    public void testAutoConfiguredBeans() {
        assertThat(clientConnectionManager.isRunning()).isFalse();
        assertThat(clientConnectionManager.isAutoStartup()).isFalse();
        assertThat(clientInitiator).isInstanceOf(SocketInitiator.class);
        assertThat(clientApplication).isInstanceOf(ApplicationAdapter.class);
        assertThat(clientMessageStoreFactory).isInstanceOf(MemoryStoreFactory.class);
        assertThat(clientLogFactory).isInstanceOf(ScreenLogFactory.class);
        assertThat(clientMessageFactory).isInstanceOf(DefaultMessageFactory.class);
        assertThat(clientSessionSettings).isNotNull();
        assertThat(clientInitiatorMBean).isNotNull();
    }

    @Configuration
    @EnableAutoConfiguration
    @EnableQuickFixJClient
    static class TestConfig {
    }
}