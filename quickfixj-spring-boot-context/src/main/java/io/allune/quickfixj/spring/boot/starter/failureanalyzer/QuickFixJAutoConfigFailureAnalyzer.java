/*
 * Copyright 2017-2023 the original author or authors.
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

import com.google.common.base.Throwables;
import io.allune.quickfixj.spring.boot.starter.exception.ConfigurationException;
import io.allune.quickfixj.spring.boot.starter.exception.QuickFixJBaseException;
import io.allune.quickfixj.spring.boot.starter.exception.SettingsNotFoundException;
import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

/**
 * The failure analyzer is responsible to provide readable information of exception which
 * occur on startup. All exception based on the {@link QuickFixJBaseException} are handled here.
 *
 * @author Eduardo Sanchez-Ros
 */
public class QuickFixJAutoConfigFailureAnalyzer extends AbstractFailureAnalyzer<QuickFixJBaseException> {

	@Override
	protected FailureAnalysis analyze(Throwable rootFailure, QuickFixJBaseException cause) {
		String descriptionMessage = cause.getMessage();
		String actionMessage = cause.getMessage();
		String rootCauseMessage = getRootCauseMessage(cause);

		if (cause instanceof ConfigurationException) {
			descriptionMessage = "A configuration error has been detected in the QuickFIX/J settings provided: " + rootCauseMessage;
			actionMessage = "Please configure your QuickFIX/J settings as per the documentation: https://www.quickfixj.org/usermanual/2.1.0/usage/configuration.html";
		}

		if (cause instanceof SettingsNotFoundException) {
			descriptionMessage = "The QuickFIX/J settings file could not be found: " + rootCauseMessage;
			actionMessage = "Please provide a QuickFIX/J settings file on the property 'config' for the client/server section in your configuration file.";
		}

		return new FailureAnalysis(descriptionMessage, actionMessage, cause);
	}

	private String getRootCauseMessage(QuickFixJBaseException cause) {
		String rootCauseMessage = "no root cause found";
		Throwable rootCause = Throwables.getRootCause(cause);
		if (rootCause != null) {
			rootCauseMessage = rootCause.getMessage() != null ? rootCause.getMessage() : "";
		}
		return rootCauseMessage;
	}

}
