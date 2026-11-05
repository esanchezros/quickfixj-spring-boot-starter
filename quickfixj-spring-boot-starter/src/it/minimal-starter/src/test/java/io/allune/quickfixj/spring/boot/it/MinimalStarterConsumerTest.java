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
package io.allune.quickfixj.spring.boot.it;

import io.allune.quickfixj.spring.boot.starter.template.QuickFixJTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.health.contributor.Health;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MinimalStarterConsumerTest {

	@Test
	void startsFromPackagedStarterWithTheCurrentDependencyContract() {
		SpringApplication application = new SpringApplication(ConsumerConfiguration.class);
		application.setWebApplicationType(WebApplicationType.NONE);
		application.setDefaultProperties(java.util.Map.of("spring.main.banner-mode", "off"));

		try (ConfigurableApplicationContext context = application.run()) {
			assertNotNull(context.getBean(QuickFixJTemplate.class));
			assertNotNull(Health.up().build());
			assertThrows(ClassNotFoundException.class, () -> Class.forName("quickfix.fix44.NewOrderSingle"));
		}
	}

	@Configuration(proxyBeanMethods = false)
	@EnableAutoConfiguration
	static class ConsumerConfiguration {
	}
}
