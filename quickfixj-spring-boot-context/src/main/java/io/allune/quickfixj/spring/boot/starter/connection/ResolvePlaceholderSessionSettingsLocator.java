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
package io.allune.quickfixj.spring.boot.starter.connection;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StreamUtils;
import quickfix.SessionSettings;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * {@link SessionSettings} helper class that attempts to load the settings files from the default locations
 *
 * @author Richard Jones
 */
@Slf4j
public class ResolvePlaceholderSessionSettingsLocator extends SessionSettingsLocator {

	private final ConfigurableEnvironment environment;

	public ResolvePlaceholderSessionSettingsLocator(ResourceLoader resourceLoader, ConfigurableEnvironment environment) {
		super(resourceLoader);
		this.environment = environment;
	}

	/**
	 * Reads the resource and resolves any placeholders. Unresolvable placeholders without a default
	 * value cause an {@link IllegalArgumentException} to be thrown so configuration errors fail fast
	 * rather than being silently passed through to QuickFIX/J as literal {@code ${...}} values.
	 *
	 * @param resource the resource
	 * @return the input stream of the resource with placeholders resolved
	 * @throws IOException if the content stream could not be opened
	 * @throws IllegalArgumentException if a placeholder cannot be resolved
	 * @see ConfigurableEnvironment#resolveRequiredPlaceholders(String)
	 */
	@Override
	public InputStream readResource(Resource resource) throws IOException {
		String content = StreamUtils.copyToString(
			resource.getInputStream(),
			StandardCharsets.UTF_8
		);

		String resolved = environment.resolveRequiredPlaceholders(content);
		return new ByteArrayInputStream(resolved.getBytes(StandardCharsets.UTF_8));
	}
}
