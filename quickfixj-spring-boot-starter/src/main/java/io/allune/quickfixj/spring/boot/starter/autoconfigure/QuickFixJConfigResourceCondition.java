/*
 * Copyright 2017 the original author or authors.
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

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.ResourceCondition;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.Assert;

/**
 * {@link SpringBootCondition} used to check if the QuickFixJ configuration is available.
 * This either kicks in if a default configuration has been found or if configurable
 * property referring to the resource to use has been set.
 *
 * @author Eduardo Sanchez-Ros
 */
public abstract class QuickFixJConfigResourceCondition extends ResourceCondition {

	private final String configSystemProperty;

	public QuickFixJConfigResourceCondition(String configSystemProperty, String prefix, String propertyName,
									 String... resourceLocations) {
		super("QuickFixJ", prefix, propertyName, resourceLocations);
		Assert.hasLength(configSystemProperty, "ConfigSystemProperty must not be null or empty");
		this.configSystemProperty = configSystemProperty;
	}

	@Override
	protected ConditionOutcome getResourceOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
		if (System.getProperty(configSystemProperty) != null) {
			return ConditionOutcome.match(startConditionMessage()
					.because("System property '" + configSystemProperty + "' is set."));
		}
		return super.getResourceOutcome(context, metadata);
	}

}
