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
package io.allune.quickfixj.spring.boot.starter.integration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import quickfix.Application;

import static org.mockito.Mockito.mock;

/**
 * @author Eduardo Sanchez-Ros
 */
@SpringBootApplication
public class QuickFixJServerClientITConfiguration {

	@Bean
	@Qualifier("serverApplication")
	public Application serverApplication() {
		return mock(Application.class);
	}

	@Bean
	@Qualifier("clientApplication")
	public Application clientApplication() {
		return mock(Application.class);
	}
}
