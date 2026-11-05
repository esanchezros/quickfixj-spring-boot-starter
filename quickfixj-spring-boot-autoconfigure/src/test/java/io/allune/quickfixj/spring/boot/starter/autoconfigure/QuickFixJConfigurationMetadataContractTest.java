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
package io.allune.quickfixj.spring.boot.starter.autoconfigure;

import org.junit.jupiter.api.Test;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class QuickFixJConfigurationMetadataContractTest {

	private static final String METADATA = "META-INF/spring-configuration-metadata.json";

	private static final List<String> FACTORY_VALUES =
			List.of("cachedfile", "file", "jdbc", "memory", "noop", "sleepycat");

	private static final List<String> LOG_VALUES =
			List.of("compositelog", "file", "jdbc", "slf4j", "screen");

	private static final Set<String> COMMON_PROPERTIES = Set.of(
			"actuator.enabled",
			"auto-startup",
			"concurrent.allow-core-thread-time-out",
			"concurrent.await-termination-seconds",
			"concurrent.core-pool-size",
			"concurrent.enabled",
			"concurrent.keep-alive-seconds",
			"concurrent.max-pool-size",
			"concurrent.queue-capacity",
			"concurrent.thread-name-prefix",
			"concurrent.use-default-executor-factory",
			"concurrent.wait-for-tasks-to-complete-on-shutdown",
			"config",
			"config-string",
			"enabled",
			"force-disconnect",
			"health.enabled",
			"jmx-enabled",
			"log-factory",
			"log-method",
			"message-store-factory",
			"message-store-method",
			"phase",
			"resolve-placeholders");

	@Test
	void generatedMetadataDefinesTheCanonicalContract() throws IOException {
		Map<String, Object> metadata = readMetadata();
		Map<String, Map<String, Object>> properties = entries(metadata, "properties").stream()
				.collect(Collectors.toMap(entry -> (String) entry.get("name"), Function.identity()));

		Set<String> expectedProperties = COMMON_PROPERTIES.stream()
				.flatMap(property -> List.of(
						"quickfixj.client." + property,
						"quickfixj.server." + property).stream())
				.collect(Collectors.toSet());
		expectedProperties.add("quickfixj.server.dynamic.enabled");
		assertThat(properties.keySet()).containsExactlyInAnyOrderElementsOf(expectedProperties);
		assertThat(properties.keySet()).allSatisfy(name -> assertThat(name).isEqualTo(name.toLowerCase()));
		assertCanonicalProperties(properties, "client");
		assertCanonicalProperties(properties, "server");
		assertThat(properties).containsKey("quickfixj.server.dynamic.enabled");

		assertDeprecated(properties, "quickfixj.client.message-store-method",
				"quickfixj.client.message-store-factory");
		assertDeprecated(properties, "quickfixj.server.message-store-method",
				"quickfixj.server.message-store-factory");
		assertDeprecated(properties, "quickfixj.client.log-method", "quickfixj.client.log-factory");
		assertDeprecated(properties, "quickfixj.server.log-method", "quickfixj.server.log-factory");
		assertDeprecated(properties, "quickfixj.client.health.enabled", "quickfixj.client.actuator.enabled");
		assertDeprecated(properties, "quickfixj.server.health.enabled", "quickfixj.server.actuator.enabled");

		Map<String, Map<String, Object>> hints = entries(metadata, "hints").stream()
				.collect(Collectors.toMap(entry -> (String) entry.get("name"), Function.identity()));
		assertHintValues(hints, "quickfixj.client.message-store-factory", FACTORY_VALUES);
		assertHintValues(hints, "quickfixj.server.message-store-factory", FACTORY_VALUES);
		assertHintValues(hints, "quickfixj.client.log-factory", LOG_VALUES);
		assertHintValues(hints, "quickfixj.server.log-factory", LOG_VALUES);
	}

	private void assertCanonicalProperties(Map<String, Map<String, Object>> properties, String role) {
		String prefix = "quickfixj." + role + ".";
		assertThat(properties).containsKeys(
				prefix + "message-store-factory",
				prefix + "log-factory",
				prefix + "actuator.enabled",
				prefix + "config-string",
				prefix + "resolve-placeholders",
				prefix + "concurrent.use-default-executor-factory",
				prefix + "concurrent.queue-capacity",
				prefix + "concurrent.core-pool-size",
				prefix + "concurrent.max-pool-size");
		assertThat(properties.get(prefix + "message-store-factory")).containsEntry("defaultValue", "memory");
		assertThat(properties.get(prefix + "log-factory")).containsEntry("defaultValue", "screen");
		assertThat(properties.get(prefix + "phase")).containsEntry("defaultValue", 2147483647);
		assertThat(properties.get(prefix + "concurrent.queue-capacity"))
				.containsEntry("defaultValue", 2147483647);
		assertThat(properties.get(prefix + "concurrent.max-pool-size"))
				.containsEntry("defaultValue", 2147483647);
	}

	@SuppressWarnings("unchecked")
	private void assertDeprecated(
			Map<String, Map<String, Object>> properties,
			String property,
			String replacement
	) {
		assertThat(properties).containsKey(property);
		Map<String, Object> deprecation = (Map<String, Object>) properties.get(property).get("deprecation");
		assertThat(deprecation)
				.containsEntry("level", "warning")
				.containsEntry("replacement", replacement);
	}

	@SuppressWarnings("unchecked")
	private void assertHintValues(
			Map<String, Map<String, Object>> hints,
			String property,
			List<String> expected
	) {
		assertThat(hints).containsKey(property);
		List<Map<String, Object>> values = (List<Map<String, Object>>) hints.get(property).get("values");
		assertThat(values).extracting(value -> (String) value.get("value")).containsExactlyElementsOf(expected);
	}

	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> entries(Map<String, Object> metadata, String name) {
		return (List<Map<String, Object>>) metadata.get(name);
	}

	private Map<String, Object> readMetadata() throws IOException {
		try (InputStream input = getClass().getClassLoader().getResourceAsStream(METADATA)) {
			assertThat(input).as(METADATA).isNotNull();
			return new ObjectMapper().readValue(input, new TypeReference<Map<String, Object>>() {
			});
		}
	}
}
