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

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Holds all the relevant starter properties which can be configured with
 * Spring Boot's application.properties / application.yml configuration files.
 *
 * @author Eduardo Sanchez-Ros
 */
@Data
@ConfigurationProperties(prefix = QuickFixJBootProperties.PROPERTY_PREFIX)
public class QuickFixJBootProperties {

	public static final String PROPERTY_PREFIX = "quickfixj";

	private ConnectorConfig client = new ConnectorConfig();

	private ConnectorConfig server = new ConnectorConfig();
}
