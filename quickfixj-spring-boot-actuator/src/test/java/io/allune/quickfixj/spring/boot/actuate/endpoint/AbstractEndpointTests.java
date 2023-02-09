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
package io.allune.quickfixj.spring.boot.actuate.endpoint;

import org.springframework.boot.actuate.endpoint.EndpointId;
import org.springframework.boot.actuate.endpoint.InvocationContext;
import org.springframework.boot.actuate.endpoint.SecurityContext;
import org.springframework.boot.actuate.endpoint.invoke.convert.ConversionServiceParameterValueMapper;
import org.springframework.boot.actuate.endpoint.invoke.reflect.ReflectiveOperationInvoker;
import org.springframework.boot.actuate.endpoint.invoker.cache.CachingOperationInvokerAdvisor;
import org.springframework.boot.actuate.endpoint.web.EndpointMediaTypes;
import org.springframework.boot.actuate.endpoint.web.ExposableWebEndpoint;
import org.springframework.boot.actuate.endpoint.web.PathMapper;
import org.springframework.boot.actuate.endpoint.web.WebOperation;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpointDiscoverer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Abstract base class for endpoint tests.
 * <p>
 * Based on WebEndpointDiscovererTests class from Spring Boot actuate tests
 */
abstract class AbstractEndpointTests {

	private final EndpointId endpointId;

	AbstractEndpointTests(EndpointId endpointId) {
		this.endpointId = endpointId;
	}

	void assertActuatorEndpointLoaded(Class<?> testConfigClass) {
		load(testConfigClass, (discoverer) -> {
			Map<EndpointId, ExposableWebEndpoint> endpoints = mapEndpoints(discoverer.getEndpoints());
			assertThat(endpoints).containsOnlyKeys(endpointId);
		});
	}

	void assertActuatorEndpointNotLoaded(Class<?> testConfigClass) {
		load(testConfigClass, (discoverer) -> {
			Map<EndpointId, ExposableWebEndpoint> endpoints = mapEndpoints(discoverer.getEndpoints());
			assertThat(endpoints).doesNotContainKey(endpointId);
		});
	}

	@SuppressWarnings("unchecked")
	void assertReadProperties(Class<?> testConfigClass) {
		load(testConfigClass, (discoverer) -> {
			Map<EndpointId, ExposableWebEndpoint> endpoints = mapEndpoints(discoverer.getEndpoints());
			assertThat(endpoints).containsKey(endpointId);

			ExposableWebEndpoint endpoint = endpoints.get(endpointId);
			assertThat(endpoint.getOperations()).hasSize(1);

			WebOperation operation = endpoint.getOperations().iterator().next();
			Object invoker = ReflectionTestUtils.getField(operation, "invoker");
			assertThat(invoker).isInstanceOf(ReflectiveOperationInvoker.class);

			Map<String, Properties> properties = (Map<String, Properties>) ((ReflectiveOperationInvoker) invoker).invoke(
					new InvocationContext(mock(SecurityContext.class), Collections.emptyMap()));
			assertThat(properties).hasSize(1);
		});
	}

	private void load(Class<?> configuration, Consumer<WebEndpointDiscoverer> consumer) {
		this.load((id) -> null, EndpointId::toString, configuration, consumer);
	}

	private void load(Function<EndpointId, Long> timeToLive,
			PathMapper endpointPathMapper,
			Class<?> configuration,
			Consumer<WebEndpointDiscoverer> consumer) {

		try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(configuration)) {
			ConversionServiceParameterValueMapper parameterMapper = new ConversionServiceParameterValueMapper(DefaultConversionService.getSharedInstance());
			EndpointMediaTypes mediaTypes = new EndpointMediaTypes(
					Collections.singletonList("application/json"),
					Collections.singletonList("application/json"));

			WebEndpointDiscoverer discoverer = new WebEndpointDiscoverer(context,
					parameterMapper,
					mediaTypes,
					Collections.singletonList(endpointPathMapper),
					Collections.singleton(new CachingOperationInvokerAdvisor(timeToLive)),
					Collections.emptyList());

			consumer.accept(discoverer);
		}
	}

	private Map<EndpointId, ExposableWebEndpoint> mapEndpoints(Collection<ExposableWebEndpoint> endpoints) {
		Map<EndpointId, ExposableWebEndpoint> endpointById = new HashMap<>();
		endpoints.forEach((endpoint) -> endpointById.put(endpoint.getEndpointId(), endpoint));
		return endpointById;
	}
}
