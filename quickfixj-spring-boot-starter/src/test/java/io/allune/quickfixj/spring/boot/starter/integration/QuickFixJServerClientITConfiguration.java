package io.allune.quickfixj.spring.boot.starter.integration;

import static org.mockito.Mockito.mock;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import io.allune.quickfixj.spring.boot.starter.EnableQuickFixJClient;
import io.allune.quickfixj.spring.boot.starter.EnableQuickFixJServer;
import quickfix.Application;

/**
 * @author Eduardo Sanchez-Ros
 */
@EnableQuickFixJServer
@EnableQuickFixJClient
@SpringBootApplication
public class QuickFixJServerClientITConfiguration {

	@Bean
	public Application serverApplication() {
		return mock(Application.class);
	}

	@Bean
	public Application clientApplication() {
		return mock(Application.class);
	}
}