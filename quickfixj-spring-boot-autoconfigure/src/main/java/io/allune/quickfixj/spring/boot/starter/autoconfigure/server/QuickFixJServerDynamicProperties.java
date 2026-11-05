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

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for server-side QuickFIX/J dynamic sessions.
 *
 * @author Eduardo Sanchez-Ros
 */
@Data
@ConfigurationProperties(prefix = "quickfixj.server.dynamic")
public class QuickFixJServerDynamicProperties {

	/**
	 * Whether to register dynamic session providers for
	 * {@link quickfix.ThreadedSocketAcceptor} template sessions. Enabling dynamic
	 * sessions selects {@link quickfix.ThreadedSocketAcceptor} even when concurrent
	 * mode is disabled.
	 */
	private boolean enabled = false;
}
