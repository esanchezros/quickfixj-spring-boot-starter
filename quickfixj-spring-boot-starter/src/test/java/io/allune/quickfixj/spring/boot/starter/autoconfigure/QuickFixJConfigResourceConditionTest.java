/*
 * Copyright 2017-2020 the original author or authors.
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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author Eduardo Sanchez-Ros
 */
public class QuickFixJConfigResourceConditionTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testGetResourceOutcomeForSystemProperty() {
		System.setProperty("anyProperty", "anyValue");
		QuickFixJConfigResourceCondition resourceCondition =
				new ClientConfigAvailableCondition("anyProperty");

		ConditionOutcome conditionOutcome =
				resourceCondition.getResourceOutcome(mock(ConditionContext.class), mock(AnnotatedTypeMetadata.class));
		assertThat(conditionOutcome).isNotNull();
		assertThat(conditionOutcome.getMessage()).contains("ResourceCondition (quickfixj.client) System property 'anyProperty' is set.");
	}

	@Test
	public void testGetResourceOutcomeForSystemPropertyNotSet() {
		QuickFixJConfigResourceCondition resourceCondition =
				new ClientConfigAvailableCondition("any");

		ConditionContext context = mock(ConditionContext.class);
		AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);
		ConditionOutcome conditionOutcome =
				resourceCondition.getResourceOutcome(context, metadata);
		assertThat(conditionOutcome).isNotNull();
		assertThat(conditionOutcome.getMessage()).contains("ResourceCondition (quickfixj.client) did not find resource");
	}

	@Test
	public void testGetResourceOutcomeForEmptySystemProperty() {
		thrown.expect(IllegalArgumentException.class);
		new ClientConfigAvailableCondition("");
	}

	@Test
	public void testGetResourceOutcomeForNullSystemProperty() {
		thrown.expect(IllegalArgumentException.class);
		new ClientConfigAvailableCondition(null);
	}

	static class ClientConfigAvailableCondition extends QuickFixJConfigResourceCondition {

		ClientConfigAvailableCondition(String configSystemProperty) {
			super(configSystemProperty, "quickfixj.client", "config");
		}
	}
}