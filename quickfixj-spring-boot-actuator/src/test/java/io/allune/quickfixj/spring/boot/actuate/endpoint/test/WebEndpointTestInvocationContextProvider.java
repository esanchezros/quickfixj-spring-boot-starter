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
package io.allune.quickfixj.spring.boot.actuate.endpoint.test;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.springframework.boot.actuate.endpoint.invoke.convert.ConversionServiceParameterValueMapper;
import org.springframework.boot.actuate.endpoint.web.EndpointLinksResolver;
import org.springframework.boot.actuate.endpoint.web.EndpointMapping;
import org.springframework.boot.actuate.endpoint.web.EndpointMediaTypes;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpointDiscoverer;
import org.springframework.boot.actuate.endpoint.web.servlet.WebMvcEndpointHandlerMapping;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.annotation.MergedAnnotations.SearchStrategy;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.ClassUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.DefaultUriBuilderFactory.EncodingMode;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * {@link TestTemplateInvocationContextProvider} for
 * {@link WebEndpointTest @WebEndpointTest}.
 *
 * @author Andy Wilkinson
 */

/**
 * Borrowed from Spring Boot
 * https://github.com/spring-projects/spring-boot/blob/8de81cb06e7039e95989f6035565f810edeaba4b/spring-boot-project/spring-boot-actuator/src/test/java/org/springframework/boot/actuate/endpoint/web/test/WebEndpointTestInvocationContextProvider.java
 */
class WebEndpointTestInvocationContextProvider implements TestTemplateInvocationContextProvider {

	@Override
	public boolean supportsTestTemplate(ExtensionContext context) {
		return true;
	}

	@Override
	public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(
			ExtensionContext extensionContext) {
		return Stream.of(
				new WebEndpointsInvocationContext("WebMvc",
						WebEndpointTestInvocationContextProvider::createWebMvcContext));
	}

	private static ConfigurableApplicationContext createWebMvcContext(List<Class<?>> classes) {
		AnnotationConfigServletWebServerApplicationContext context = new AnnotationConfigServletWebServerApplicationContext();
		classes.add(WebMvcEndpointConfiguration.class);
		context.register(ClassUtils.toClassArray(classes));
		context.refresh();
		return context;
	}

	static class WebEndpointsInvocationContext
			implements TestTemplateInvocationContext, BeforeEachCallback, AfterEachCallback, ParameterResolver {

		private static final Duration TIMEOUT = Duration.ofMinutes(5);

		private final String name;

		private final Function<List<Class<?>>, ConfigurableApplicationContext> contextFactory;

		private ConfigurableApplicationContext context;

		<T extends ConfigurableApplicationContext & AnnotationConfigRegistry> WebEndpointsInvocationContext(String name,
				Function<List<Class<?>>, ConfigurableApplicationContext> contextFactory) {
			this.name = name;
			this.contextFactory = contextFactory;
		}

		@Override
		public void beforeEach(ExtensionContext extensionContext) throws Exception {
			List<Class<?>> configurationClasses = Stream
				.of(extensionContext.getRequiredTestClass().getDeclaredClasses())
				.filter(this::isConfiguration)
				.collect(Collectors.toCollection(ArrayList::new));
			this.context = this.contextFactory.apply(configurationClasses);
		}

		private boolean isConfiguration(Class<?> candidate) {
			return MergedAnnotations.from(candidate, SearchStrategy.TYPE_HIERARCHY).isPresent(Configuration.class);
		}

		@Override
		public void afterEach(ExtensionContext context) throws Exception {
			if (this.context != null) {
				this.context.close();
			}
		}

		@Override
		public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
			Class<?> type = parameterContext.getParameter().getType();
			return type.equals(WebTestClient.class) || type.isAssignableFrom(ConfigurableApplicationContext.class);
		}

		@Override
		public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
			Class<?> type = parameterContext.getParameter().getType();
			if (type.equals(WebTestClient.class)) {
				return createWebTestClient();
			}
			else {
				return this.context;
			}
		}

		@Override
		public List<Extension> getAdditionalExtensions() {
			return Collections.singletonList(this);
		}

		@Override
		public String getDisplayName(int invocationIndex) {
			return this.name;
		}

		private WebTestClient createWebTestClient() {
			DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory(
					"http://localhost:" + determinePort());
			uriBuilderFactory.setEncodingMode(EncodingMode.NONE);
			return WebTestClient.bindToServer()
				.uriBuilderFactory(uriBuilderFactory)
				.responseTimeout(TIMEOUT)
				.codecs((codecs) -> codecs.defaultCodecs().maxInMemorySize(-1))
				.filter((request, next) -> {
					if (HttpMethod.GET == request.method()) {
						return next.exchange(request).retry(10);
					}
					return next.exchange(request);
				})
				.build();
		}

		private int determinePort() {
			if (this.context instanceof AnnotationConfigServletWebServerApplicationContext) {
				AnnotationConfigServletWebServerApplicationContext webServerContext =
					(AnnotationConfigServletWebServerApplicationContext) this.context;
				return webServerContext.getWebServer().getPort();
			}
			return this.context.getBean(PortHolder.class).getPort();
		}
	}

	@Configuration(proxyBeanMethods = false)
	@ImportAutoConfiguration({ JacksonAutoConfiguration.class, HttpMessageConvertersAutoConfiguration.class,
			WebMvcAutoConfiguration.class, DispatcherServletAutoConfiguration.class })
	static class WebMvcEndpointConfiguration {

		private final ApplicationContext applicationContext;

		WebMvcEndpointConfiguration(ApplicationContext applicationContext) {
			this.applicationContext = applicationContext;
		}

		@Bean
		TomcatServletWebServerFactory tomcat() {
			return new TomcatServletWebServerFactory(0);
		}

		@Bean
		WebMvcEndpointHandlerMapping webEndpointServletHandlerMapping() {
			EndpointMediaTypes endpointMediaTypes = EndpointMediaTypes.DEFAULT;
			WebEndpointDiscoverer discoverer = new WebEndpointDiscoverer(this.applicationContext,
					new ConversionServiceParameterValueMapper(), endpointMediaTypes, null, Collections.emptyList(),
					Collections.emptyList());
			return new WebMvcEndpointHandlerMapping(new EndpointMapping("/actuator"), discoverer.getEndpoints(),
					endpointMediaTypes, new CorsConfiguration(), new EndpointLinksResolver(discoverer.getEndpoints()),
					true);
		}

	}

}
