package io.allune.quickfixj.spring.boot.starter.failureanalyzer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.springframework.boot.diagnostics.FailureAnalysis;

import io.allune.quickfixj.spring.boot.starter.exception.ConfigurationException;
import io.allune.quickfixj.spring.boot.starter.exception.QuickFixJBaseException;
import io.allune.quickfixj.spring.boot.starter.exception.SettingsNotFoundException;

public class QuickFixJAutoConfigFailureAnalyzerTest {

    @Test
    public void shouldAnalyzeConfigurationException() {

        // Given
        QuickFixJAutoConfigFailureAnalyzer analyzer = new QuickFixJAutoConfigFailureAnalyzer();
        ConfigurationException exception = new ConfigurationException("Error", new RuntimeException());

        // When
        FailureAnalysis analyze = analyzer.analyze(null, exception);

        assertThat(analyze.getAction()).contains("Please configure your QuickFixJ settings");
        assertThat(analyze.getDescription()).contains("A configuration error has been detected in the QuickFixJ settings provided.");
        assertThat(analyze.getCause()).isEqualTo(exception);
    }

    @Test
    public void shouldAnalyzeSettingsNotFoundException() {

        // Given
        QuickFixJAutoConfigFailureAnalyzer analyzer = new QuickFixJAutoConfigFailureAnalyzer();
        SettingsNotFoundException exception = new SettingsNotFoundException("Error", new RuntimeException());

        // When
        FailureAnalysis analyze = analyzer.analyze(null, exception);

        assertThat(analyze.getAction()).contains("Please provide a QuickFixJ settings file");
        assertThat(analyze.getDescription()).contains("The QuickFixJ settings file could not be found.");
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
}