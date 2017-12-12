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


package io.allune.quickfixj.spring.boot.actuate.endpoint;

import io.allune.quickfixj.spring.boot.starter.EnableQuickFixJServer;
import org.junit.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import quickfix.Acceptor;
import quickfix.SessionSettings;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

public class QuickFixJServerEndpointTest extends AbstractEndpointTests<QuickFixJServerEndpoint> {

    public QuickFixJServerEndpointTest() {
        super(Config.class, QuickFixJServerEndpoint.class, "quickfixjserver", true,
                "endpoints.quickfixjserver", new HashMap<String, Object>() {
                    {
                        put("quickfixj.server.config", "classpath:quickfixj-server.cfg");
                        put("quickfixj.server.autoStartup", "true");
                    }
                });
    }

    @Test
    public void invoke() throws Exception {
        this.context.refresh();
        assertThat(getEndpointBean().invoke()).hasSize(1);
    }

    @Configuration
    @EnableAutoConfiguration
    @EnableQuickFixJServer
    public static class Config {

        private Acceptor serverAcceptor;

        private SessionSettings serverSessionSettings;

        public Config(Acceptor serverAcceptor, SessionSettings serverSessionSettings) {
            this.serverAcceptor = serverAcceptor;
            this.serverSessionSettings = serverSessionSettings;
        }

        @Bean
        public QuickFixJServerEndpoint endpoint() {
            return new QuickFixJServerEndpoint(serverAcceptor, serverSessionSettings);
        }

    }
}