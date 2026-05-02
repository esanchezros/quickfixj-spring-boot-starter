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

import io.allune.quickfixj.spring.boot.starter.exception.SettingsNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import quickfix.ConfigError;
import quickfix.SessionSettings;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static java.util.Optional.empty;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * {@link SessionSettings} helper class that attempts to load the settings files from the default locations
 *
 * @author Eduardo Sanchez-Ros
 */
@Slf4j
public class SessionSettingsLocator {

	protected final ResourceLoader resourceLoader;

	public SessionSettingsLocator(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	/**
	 * Loads the {@link SessionSettings} from the provided resource locations
	 *
	 * @param locations The resource locations to load the {@link SessionSettings} from
	 * @return The {@link SessionSettings}
	 */
	public SessionSettings loadSettings(String... locations) {
		try {
			for (String location : locations) {
				Optional<Resource> resource = load(location);
				if (resource.isPresent()) {
					log.info("Loading settings from '{}'", location);
					return new SessionSettings(readResource(resource.get()));
				}
			}

			throw new SettingsNotFoundException("Settings file not found");
		} catch (RuntimeException | ConfigError | IOException e) {
			throw new SettingsNotFoundException(e.getMessage(), e);
		}
	}

	/**
	 * Reads the resource into an {@link InputStream}.
	 * @param resource the resource
	 * @return the input stream
	 * @throws IOException if the content stream could not be opened
	 */
	protected InputStream readResource(Resource resource) throws IOException {
		return resource.getInputStream();
	}

	public SessionSettings loadSettingsFromString(String configString) {
		if (isBlank(configString)) {
			throw new SettingsNotFoundException("configString is blank or empty");
		}
		try {
			return new SessionSettings(new ByteArrayInputStream(configString.getBytes()));
		} catch (RuntimeException | ConfigError e) {
			throw new SettingsNotFoundException(e.getMessage(), e);
		}
	}

	protected Optional<Resource> load(String location) {
		if (location == null) {
			return empty();
		}

		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(resourceLoader);
		Resource resource = resolver.getResource(location);
		return resource.exists() ? Optional.of(resource) : empty();
	}
}
