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
package io.allune.quickfixj.spring.boot.starter.configuration;

import io.allune.quickfixj.spring.boot.actuate.endpoint.QuickFixJServerEndpoint;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Eduardo Sanchez-Ros
 */
public class QuickFixJServerActuatorAutoConfigurationTest {

	@Test
	public void testServerActuator() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(ServerActuatorConfiguration.class);
		QuickFixJServerEndpoint quickfixjServerEndpoint = ctx.getBean("quickfixjServerEndpoint", QuickFixJServerEndpoint.class);
		assertThat(quickfixjServerEndpoint).isNotNull();
		assertThat(quickfixjServerEndpoint.readProperties().size()).isEqualTo(6);
		ctx.stop();
	}

	@Configuration
	@EnableAutoConfiguration
	@PropertySource("classpath:server-actuator/server-actuator.properties")
	static class ServerActuatorConfiguration {
	}
}
