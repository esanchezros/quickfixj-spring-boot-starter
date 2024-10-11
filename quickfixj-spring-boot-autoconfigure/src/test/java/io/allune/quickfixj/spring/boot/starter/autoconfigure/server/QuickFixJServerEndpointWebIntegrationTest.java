/*
 * Copyright 2017-2024 the original author or authors.
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
package io.allune.quickfixj.spring.boot.starter.autoconfigure.server;

import io.allune.quickfixj.spring.boot.starter.autoconfigure.AbstractQuickFixJBaseEndpointAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
public class QuickFixJServerEndpointWebIntegrationTest extends AbstractQuickFixJBaseEndpointAutoConfiguration {

	@Autowired
	private WebTestClient webClient;

	@Value(value = "${local.server.port}")
	private int port;

	@Test
	void shouldReadProperties() {
		webClient.get()
				.uri("http://localhost:" + port + "/actuator/quickfixjserver")
				.exchange()
				.expectStatus()
				.isOk()
				.expectBody()
				.consumeWith(assertSessionProperties());
	}

	@Configuration
	@EnableAutoConfiguration
	@PropertySource("classpath:server-actuator/server-actuator.properties")
	public static class QuickFixJServerEndpointTestConfig {
	}
}
