package io.allune.quickfixj.spring.boot.actuate.config;

import io.allune.quickfixj.spring.boot.actuate.endpoint.QuickFixJServerEndpoint;
import io.allune.quickfixj.spring.boot.starter.EnableQuickFixJServer;
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
                "quickfixj.server.autoStartup=false",
                "quickfixj.server.config=classpath:quickfixj.cfg",
                "quickfixj.server.jmx-enabled=true"
        })

public class QuickFixJServerEndpointAutoConfigurationTest {

    @Autowired
    private QuickFixJServerEndpoint quickfixjServerEndpoint;

    @Test
    public void testAutoConfiguredBeans() {
        assertThat(quickfixjServerEndpoint).isNotNull();
        assertThat(quickfixjServerEndpoint.readProperties().size()).isEqualTo(0);
    }

    @Configuration
    @EnableAutoConfiguration
    @EnableQuickFixJServer
    public static class Config {

    }
}