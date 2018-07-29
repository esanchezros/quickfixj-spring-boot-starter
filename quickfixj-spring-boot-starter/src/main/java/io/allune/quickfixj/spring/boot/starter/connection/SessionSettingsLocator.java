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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import quickfix.ConfigError;
import quickfix.SessionSettings;

import java.io.IOException;

import static java.lang.Thread.currentThread;

/**
 * {@link SessionSettings} helper class that attempts to load the settings files from the default locations
 *
 * @author Eduardo Sanchez-Ros
 */
public class SessionSettingsLocator {

    private static final Log logger = LogFactory.getLog(SessionSettingsLocator.class);

    private SessionSettingsLocator() {
        //
    }

    public static SessionSettings loadSettings(String applicationConfigLocation, String systemProperty, String fileSystemLocation,
                                               String classpathLocation) {

        try {
            SessionSettings settings;
            settings = loadFromApplicationConfig(applicationConfigLocation);
            if (settings != null) return settings;

            settings = loadFromSystemProperty(systemProperty);
            if (settings != null) return settings;

            settings = loadFromFileSystem(fileSystemLocation);
            if (settings != null) return settings;

            settings = loadFromClassPath(classpathLocation);
            if (settings != null) return settings;

            throw new SettingsNotFoundException("Settings file not found");
        } catch (RuntimeException | ConfigError | IOException e) {
            throw new SettingsNotFoundException(e.getMessage(), e);
        }
    }

    private static SessionSettings loadFromApplicationConfig(String applicationConfigLocation) throws ConfigError, IOException {
        Resource resource;
        if (applicationConfigLocation != null) {
            resource = loadResource(applicationConfigLocation);
            if (resource != null && resource.exists()) {
                logger.info("Loading settings from application property '" + applicationConfigLocation + "'");
                return new SessionSettings(resource.getInputStream());
            }
        }
        return null;
    }

    private static SessionSettings loadFromSystemProperty(String systemProperty) throws ConfigError, IOException {
        // Try loading the settings from the system property
        if (systemProperty != null) {
            String configSystemProperty = System.getProperty(systemProperty);
            if (configSystemProperty != null) {
                Resource resource = loadResource(configSystemProperty);
                if (resource != null && resource.exists()) {
                    logger.info("Loading settings from System property '" + systemProperty + "'");
                    return new SessionSettings(resource.getInputStream());
                }
            }
        }
        return null;
    }

    private static SessionSettings loadFromFileSystem(String fileSystemLocation) throws ConfigError, IOException {
        // Try loading the settings file from the same location in the filesystem
        if (fileSystemLocation != null) {
            Resource resource = loadResource(fileSystemLocation);
            if (resource != null && resource.exists()) {
                logger.info("Loading settings from default filesystem location '" + fileSystemLocation + "'");
                return new SessionSettings(resource.getInputStream());
            }
        }
        return null;
    }

    private static SessionSettings loadFromClassPath(String classpathLocation) throws ConfigError, IOException {
        // Try loading the settings file from the classpath
        if (classpathLocation != null) {
            Resource resource = loadResource(classpathLocation);
            if (resource != null && resource.exists()) {
                logger.info("Loading settings from default classpath location '" + classpathLocation + "'");
                return new SessionSettings(resource.getInputStream());
            }
        }
        return null;
    }

    private static Resource loadResource(String location) {
        ClassLoader classLoader = currentThread().getContextClassLoader();
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(classLoader);
        return resolver.getResource(location);
    }
}
