package io.allune.quickfixj.spring.boot.starter.connection;

import static io.allune.quickfixj.spring.boot.starter.connection.SessionSettingsLocator.loadSettings;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import io.allune.quickfixj.spring.boot.starter.exception.SettingsNotFoundException;
import quickfix.SessionSettings;

public class SessionSettingsLocatorTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldLoadDefaultFromSystemProperty() throws URISyntaxException {
        SessionSettings settings = loadSettings("classpath:quickfixj.cfg", null, null, null);
        assertThat(settings).isNotNull();

        URL resource = SessionSettingsLocatorTest.class.getResource("/quickfixj.cfg");
        File file = Paths.get(resource.toURI()).toFile();
        settings = loadSettings(null, null, "file://" + file.getAbsolutePath(), null);
        assertThat(settings).isNotNull();

        settings = loadSettings(null, null, null, "classpath:quickfixj.cfg");
        assertThat(settings).isNotNull();
    }

    @Test
    public void shouldThrowSettingsNotFoundExceptionIfNoneFound() {
        thrown.expect(SettingsNotFoundException.class);
        loadSettings(null, null, null, null);
    }
}