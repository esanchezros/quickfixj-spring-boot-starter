package io.allune.quickfixj.spring.boot.starter.autoconfigure.client;

import org.springframework.context.annotation.Bean;

/**
 * Responsible for adding in a marker bean to trigger activation of 
 * {@link QuickFixJClientAutoConfiguration}
 *
 * @author Eduardo Sanchez-Ros
 */
public class QuickFixJClientMarkerConfiguration {
    @Bean
	public Marker quickFixJClientMarkerBean() {
		return new Marker();
	}

	class Marker {
    	//
	}
}