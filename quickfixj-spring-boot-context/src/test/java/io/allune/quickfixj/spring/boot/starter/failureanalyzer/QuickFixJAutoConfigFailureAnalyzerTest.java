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
package io.allune.quickfixj.spring.boot.starter.failureanalyzer;

import io.allune.quickfixj.spring.boot.starter.exception.ConfigurationException;
import io.allune.quickfixj.spring.boot.starter.exception.QuickFixJBaseException;
import io.allune.quickfixj.spring.boot.starter.exception.SettingsNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.diagnostics.FailureAnalysis;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Eduardo Sanchez-Ros
 */
public class QuickFixJAutoConfigFailureAnalyzerTest {

	@Test
	public void shouldAnalyzeConfigurationException() {

		// Given
		QuickFixJAutoConfigFailureAnalyzer analyzer = new QuickFixJAutoConfigFailureAnalyzer();
		ConfigurationException exception = new ConfigurationException("Error", new RuntimeException("Error message"));

		// When
		FailureAnalysis analyze = analyzer.analyze(null, exception);

		assertThat(analyze.getAction()).contains("Please configure your QuickFIX/J settings");
		assertThat(analyze.getDescription()).contains("A configuration error has been detected in the QuickFIX/J settings provided: Error message");
		assertThat(analyze.getCause()).isEqualTo(exception);
	}

	@Test
	public void shouldAnalyzeSettingsNotFoundException() {

		// Given
		QuickFixJAutoConfigFailureAnalyzer analyzer = new QuickFixJAutoConfigFailureAnalyzer();
		SettingsNotFoundException exception = new SettingsNotFoundException("Error", new RuntimeException(new RuntimeException("Error message")));

		// When
		FailureAnalysis analyze = analyzer.analyze(null, exception);

		assertThat(analyze.getAction()).contains("Please provide a QuickFIX/J settings file");
		assertThat(analyze.getDescription()).contains("The QuickFIX/J settings file could not be found: Error message");
		assertThat(analyze.getCause()).isEqualTo(exception);
	}

	@Test
	public void shouldAnalyzeAnyException() {

		// Given
		QuickFixJAutoConfigFailureAnalyzer analyzer = new QuickFixJAutoConfigFailureAnalyzer();
		QuickFixJBaseException exception = new QuickFixJBaseException("Error message", new RuntimeException());

		// When
		FailureAnalysis analyze = analyzer.analyze(null, exception);

		assertThat(analyze.getAction()).contains("Error message");
		assertThat(analyze.getDescription()).contains("Error message");
		assertThat(analyze.getCause()).isEqualTo(exception);
	}

	@Test
	public void shouldAnalyzeConfigurationExceptionWithNullRootCause() {

		// Given
		QuickFixJAutoConfigFailureAnalyzer analyzer = new QuickFixJAutoConfigFailureAnalyzer();
		ConfigurationException exception = new ConfigurationException("Error", null);

		// When
		FailureAnalysis analyze = analyzer.analyze(null, exception);

		assertThat(analyze.getAction()).contains("Please configure your QuickFIX/J settings");
		assertThat(analyze.getDescription()).contains("A configuration error has been detected in the QuickFIX/J settings provided: Error");
		assertThat(analyze.getCause()).isEqualTo(exception);
	}
}
