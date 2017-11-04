package io.allune.quickfixj.spring.boot.starter.connection;

import io.allune.quickfixj.spring.boot.starter.exception.SettingsNotFoundException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import quickfix.ConfigError;
import quickfix.SessionSettings;

import java.io.IOException;

import static java.lang.Thread.currentThread;

/**
 * @author Eduardo Sanchez-Ros
 */
public class SessionSettingsLocator {

    private static final Log logger = LogFactory.getLog(SessionSettingsLocator.class);

    private SessionSettingsLocator() {
        //
    }

    public static SessionSettings loadSettings(String applicationProperty, String systemProperty, String fileSystemLocation,
                                               String classpathLocation) {

        try {
            Resource resource;
            if (applicationProperty != null) {
                resource = loadResource(applicationProperty);
                if (resource != null && resource.exists()) {
                    logger.info("Loading settings from application property '" + applicationProperty + "'");
                    return new SessionSettings(resource.getInputStream());
                }
            }

            // Try loading the settings from the system property
            String configSystemProperty = System.getProperty(systemProperty);
            if (configSystemProperty != null) {
                resource = loadResource(configSystemProperty);
                if (resource!= null && resource.exists()) {
                    logger.info("Loading settings from System property '" + systemProperty + "'");
                    return new SessionSettings(resource.getInputStream());
                }
            }

            // Try loading the settings file from the same location in the filesystem
            resource = loadResource(fileSystemLocation);
            if (resource != null && resource.exists()) {
                logger.info("Loading settings from default filesystem location '" + fileSystemLocation + "'");
                return new SessionSettings(resource.getInputStream());
            }

            // Try loading the settings file from the classpath
            resource = loadResource(classpathLocation);
            if ( resource != null && resource.exists()) {
                logger.info("Loading settings from default classpath location '" + classpathLocation + "'");
                return new SessionSettings(resource.getInputStream());
            }

            throw new SettingsNotFoundException("Settings file not found");
        } catch (RuntimeException | ConfigError | IOException e) {
            throw new SettingsNotFoundException(e.getMessage(), e);
        }
    }

    private static Resource loadResource(String location) {
        ClassLoader classLoader = currentThread().getContextClassLoader();
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(classLoader);
        return resolver.getResource(location);
    }
}
