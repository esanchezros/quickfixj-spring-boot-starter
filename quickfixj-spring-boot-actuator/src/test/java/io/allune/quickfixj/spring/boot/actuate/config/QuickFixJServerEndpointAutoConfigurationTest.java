/*
 * Copyright 2017-2022 the original author or authors.
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
package io.allune.quickfixj.spring.boot.actuate.config;

import io.allune.quickfixj.spring.boot.actuate.endpoint.QuickFixJServerEndpoint;
import io.allune.quickfixj.spring.boot.starter.EnableQuickFixJServer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Eduardo Sanchez-Ros
 */
@SpringBootTest(
		properties = {
				"quickfixj.server.autoStartup=false",
				"quickfixj.server.config=classpath:quickfixj-server.cfg",
				"quickfixj.server.jmx-enabled=true"
		})
@ActiveProfiles("server")
public class QuickFixJServerEndpointAutoConfigurationTest {

	@Autowired
	private QuickFixJServerEndpoint quickfixjServerEndpoint;

	@Test
	public void testAutoConfiguredBeans() {
		assertThat(quickfixjServerEndpoint).isNotNull();
		assertThat(quickfixjServerEndpoint.readProperties().size()).isEqualTo(0);
	}

	@Configuration
	@EnableAutoConfiguration
	@EnableQuickFixJServer
	public static class Config {

	}
}
