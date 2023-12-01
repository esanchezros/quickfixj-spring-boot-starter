/*
 * Copyright 2017-2023 the original author or authors.
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

import io.allune.quickfixj.spring.boot.actuate.endpoint.QuickFixJClientEndpoint;
import io.allune.quickfixj.spring.boot.actuate.health.QuickFixJSessionHealthIndicator;
import io.allune.quickfixj.spring.boot.starter.configuration.client.QuickFixJClientConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class QuickFixJClientEndpointAutoConfigurationTest {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withConfiguration(
			AutoConfigurations.of(QuickFixJClientConfiguration.class, QuickFixJClientEndpointAutoConfiguration.class));

	@Test
	public void shouldLoadActuatorEndpoint() {
		contextRunner.withPropertyValues("quickfixj.client.config=classpath:quickfixj-client.cfg")
			.withPropertyValues("quickfixj.client.autoStartup=true")
			.withPropertyValues("management.endpoints.enabled-by-default=false")
			.withPropertyValues("management.endpoints.web.exposure.include=quickfixjclient")
			.withPropertyValues("management.endpoint.quickfixjclient.enabled=true")
			.withPropertyValues("management.health.quickfixjclient.enabled=true")
			.run(this::createsBeans);
	}

	private void createsBeans(AssertableApplicationContext ctx) {
		assertThat(ctx).hasBean("quickfixjClientEndpoint");
		assertThat(ctx.getBean("quickfixjClientEndpoint"))
			.isInstanceOf(QuickFixJClientEndpoint.class);

		assertThat(ctx).hasBean("quickfixjClientSessionHealthIndicator");
		assertThat(ctx).getBean("quickfixjClientSessionHealthIndicator")
			.isInstanceOf(QuickFixJSessionHealthIndicator.class);
	}

	@Test
	public void shouldNotLoadActuatorEndpoint() {
		contextRunner.withPropertyValues("quickfixj.client.config=classpath:quickfixj-client.cfg")
			.withPropertyValues("quickfixj.client.autoStartup=true")
			.withPropertyValues("management.endpoints.enabled-by-default=false")
			.withPropertyValues("management.endpoints.web.exposure.include=quickfixjclient")
			.withPropertyValues("management.endpoint.quickfixjclient.enabled=false")
			.withPropertyValues("management.health.quickfixjclient.enabled=false")
			.run(this::doesNotCreateBeans);
	}

	private void doesNotCreateBeans(AssertableApplicationContext ctx) {
		assertThat(ctx).doesNotHaveBean("quickfixjClientEndpoint");
		assertThat(ctx).doesNotHaveBean("quickfixjClientSessionHealthIndicator");
	}
}
