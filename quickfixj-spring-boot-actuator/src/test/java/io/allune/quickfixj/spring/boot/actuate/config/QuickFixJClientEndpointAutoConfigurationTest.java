package io.allune.quickfixj.spring.boot.actuate.config;

import io.allune.quickfixj.spring.boot.actuate.endpoint.QuickFixJClientEndpoint;
import io.allune.quickfixj.spring.boot.starter.EnableQuickFixJClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Eduardo Sanchez-Ros
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
        properties = {
                "quickfixj.client.autoStartup=false",
                "quickfixj.client.config=classpath:quickfixj.cfg",
                "quickfixj.client.jmx-enabled=true"
        })

public class QuickFixJClientEndpointAutoConfigurationTest {

    @Autowired
    private QuickFixJClientEndpoint quickfixjClientEndpoint;

    @Test
    public void testAutoConfiguredBeans() {
        assertThat(quickfixjClientEndpoint).isNotNull();
        assertThat(quickfixjClientEndpoint.readProperties().size()).isEqualTo(0);
    }

    @Configuration
    @EnableAutoConfiguration
    @EnableQuickFixJClient
    public static class Config {

    }
}