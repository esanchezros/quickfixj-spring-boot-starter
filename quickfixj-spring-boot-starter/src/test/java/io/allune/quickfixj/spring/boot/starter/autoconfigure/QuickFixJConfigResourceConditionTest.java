package io.allune.quickfixj.spring.boot.starter.autoconfigure;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.mock;

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