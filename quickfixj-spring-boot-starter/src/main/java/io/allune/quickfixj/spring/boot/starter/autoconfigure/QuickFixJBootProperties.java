/*
 * Copyright 2019 the original author or authors.
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

import org.springframework.boot.context.properties.ConfigurationProperties;

import io.allune.quickfixj.spring.boot.starter.connection.ConnectorManager;
import lombok.Getter;
import lombok.Setter;

/**
 * Holds all the relevant starter properties which can be configured with
 * Spring Boot's application.properties / application.yml configuration files.
 *
 * @author Eduardo Sanchez-Ros
 */
@Getter
@Setter
@ConfigurationProperties(prefix = QuickFixJBootProperties.PROPERTY_PREFIX)
public class QuickFixJBootProperties {

	public static final String PROPERTY_PREFIX = "quickfixj";

	private Config client = new Config();

	private Config server = new Config();

	@Getter
	@Setter
	public static class Config {

		/**
		 * Whether the {@link ConnectorManager} should get started automatically
		 */
		private boolean autoStartup = true;

		/**
		 * The phase value of the {@link ConnectorManager}.
		 */
		private int phase = Integer.MAX_VALUE;

		/**
		 * The location of the configuration file to use to initialize QuickFixJ client.
		 */
		private String config;

		/**
		 * Whether to register the Jmx MBeans for the client
		 */
		private boolean jmxEnabled = false;

		/**
		 * Configures the concurrent options
		 */
		private Concurrent concurrent = new Concurrent();
	}

	@Getter
	@Setter
	public static class Concurrent {

		/**
		 * Whether it should be multithreaded or single threaded
		 */
		private boolean enabled = false;
	}
}
