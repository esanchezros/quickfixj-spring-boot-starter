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
package io.allune.quickfixj.spring.boot.actuate.config;

import io.allune.quickfixj.spring.boot.actuate.endpoint.QuickFixJServerEndpoint;
import io.allune.quickfixj.spring.boot.actuate.health.QuickFixJSessionHealthIndicator;
import io.allune.quickfixj.spring.boot.starter.configuration.server.QuickFixJServerConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class QuickFixJServerEndpointAutoConfigurationTest {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withConfiguration(
			AutoConfigurations.of(QuickFixJServerConfiguration.class, QuickFixJServerEndpointAutoConfiguration.class));

	@Test
	public void shouldLoadActuatorEndpoint() {
		contextRunner.withPropertyValues("quickfixj.server.config=classpath:quickfixj-server.cfg")
			.withPropertyValues("management.endpoints.enabled-by-default=false")
			.withPropertyValues("management.endpoints.web.exposure.include=quickfixjserver")
			.withPropertyValues("management.endpoint.quickfixjserver.enabled=true")
			.withPropertyValues("management.health.quickfixjserver.enabled=true")
			.run(this::createsBeans);
	}

	private void createsBeans(AssertableApplicationContext ctx) {
		assertThat(ctx).hasBean("quickfixjServerEndpoint");
		assertThat(ctx.getBean("quickfixjServerEndpoint"))
			.isInstanceOf(QuickFixJServerEndpoint.class);

		assertThat(ctx).hasBean("quickfixjServerSessionHealthIndicator");
		assertThat(ctx).getBean("quickfixjServerSessionHealthIndicator")
			.isInstanceOf(QuickFixJSessionHealthIndicator.class);
	}

	@Test
	public void shouldNotLoadActuatorEndpoint() {
		contextRunner.withPropertyValues("quickfixj.server.config=classpath:quickfixj-server.cfg")
			.withPropertyValues("quickfixj.server.autoStartup=true")
			.withPropertyValues("management.endpoints.enabled-by-default=false")
			.withPropertyValues("management.endpoints.web.exposure.include=quickfixjserver")
			.withPropertyValues("management.endpoint.quickfixjserver.enabled=false")
			.withPropertyValues("management.health.quickfixjserver.enabled=false")
			.run(this::doesNotCreateBeans);
	}

	private void doesNotCreateBeans(AssertableApplicationContext ctx) {
		assertThat(ctx).doesNotHaveBean("quickfixjServerEndpoint");
		assertThat(ctx).doesNotHaveBean("quickfixjServerSessionHealthIndicator");
	}
}
