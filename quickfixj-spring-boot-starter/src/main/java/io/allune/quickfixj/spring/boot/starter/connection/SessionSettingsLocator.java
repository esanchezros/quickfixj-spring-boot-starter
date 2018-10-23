/*
 * Copyright 2018 the original author or authors.
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
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import quickfix.ConfigError;
import quickfix.SessionSettings;

import java.io.IOException;
import java.util.Optional;

import static java.lang.Thread.currentThread;
import static java.util.Optional.empty;

/**
 * {@link SessionSettings} helper class that attempts to load the settings files from the default locations
 *
 * @author Eduardo Sanchez-Ros
 */
@Slf4j
public class SessionSettingsLocator {

    private SessionSettingsLocator() {
        //
    }

    public static SessionSettings loadSettings(String... locations) {

        try {
            for (String location : locations) {
                Optional<Resource> resource = load(location);

                if (resource.isPresent()) {
                    log.info("Loading settings from '{}'", location);

                    return new SessionSettings(resource.get().getInputStream());
                }
            }

            throw new SettingsNotFoundException("Settings file not found");
        } catch (RuntimeException | ConfigError | IOException e) {
            throw new SettingsNotFoundException(e.getMessage(), e);
        }
    }


    private static Optional<Resource> load(String location) {
        if (location == null) {
            return empty();
        }

        ClassLoader classLoader = currentThread().getContextClassLoader();
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(classLoader);

        Resource resource = resolver.getResource(location);
        return resource.exists() ? Optional.of(resource) : empty();
    }
}
