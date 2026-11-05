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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import quickfix.Acceptor;
import quickfix.SocketAcceptor;
import quickfix.ThreadedSocketAcceptor;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
class QuickFixJServerAcceptorSelectionTest {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(QuickFixJServerAutoConfiguration.class))
			.withPropertyValues(
					"quickfixj.server.enabled=true",
					"quickfixj.server.auto-startup=false",
					"quickfixj.server.config=classpath:quickfixj-server.cfg");

	@ParameterizedTest(name = "{0}")
	@MethodSource("acceptorSelections")
	void selectsExactlyOneAcceptor(
			String scenario,
			List<String> properties,
			Class<? extends Acceptor> expectedType,
			boolean dynamicEnabled
	) {
		contextRunner.withPropertyValues(properties.toArray(String[]::new))
				.run(context -> {
					assertThat(context).hasNotFailed();
					assertThat(context).hasSingleBean(Acceptor.class);
					assertThat(context).hasBean("serverAcceptor");
					assertThat(context.getBean(Acceptor.class)).isExactlyInstanceOf(expectedType);
					assertThat(context.getBean(QuickFixJServerDynamicProperties.class).isEnabled())
							.isEqualTo(dynamicEnabled);

					if (dynamicEnabled) {
						assertThat(context).hasSingleBean(ThreadedSocketAcceptorDynamicSessionProviderRegistrar.class);
					} else {
						assertThat(context).doesNotHaveBean(ThreadedSocketAcceptorDynamicSessionProviderRegistrar.class);
					}
				});
	}

	private static Stream<Arguments> acceptorSelections() {
		return Stream.of(
				Arguments.of("both properties missing", List.of(), SocketAcceptor.class, false),
				Arguments.of(
						"both properties disabled",
						List.of(
								"quickfixj.server.concurrent.enabled=false",
								"quickfixj.server.dynamic.enabled=false"),
						SocketAcceptor.class,
						false),
				Arguments.of(
						"concurrent enabled and dynamic missing",
						List.of("quickfixj.server.concurrent.enabled=true"),
						ThreadedSocketAcceptor.class,
						false),
				Arguments.of(
						"dynamic enabled and concurrent disabled",
						List.of(
								"quickfixj.server.config=classpath:quickfixj-server-dynamic.cfg",
								"quickfixj.server.concurrent.enabled=false",
								"quickfixj.server.dynamic.enabled=true"),
						ThreadedSocketAcceptor.class,
						true),
				Arguments.of(
						"dynamic enabled and concurrent missing",
						List.of(
								"quickfixj.server.config=classpath:quickfixj-server-dynamic.cfg",
								"quickfixj.server.dynamic.enabled=true"),
						ThreadedSocketAcceptor.class,
						true),
				Arguments.of(
						"both properties enabled",
						List.of(
								"quickfixj.server.config=classpath:quickfixj-server-dynamic.cfg",
								"quickfixj.server.concurrent.enabled=true",
								"quickfixj.server.dynamic.enabled=true"),
						ThreadedSocketAcceptor.class,
						true)
		);
	}
}
