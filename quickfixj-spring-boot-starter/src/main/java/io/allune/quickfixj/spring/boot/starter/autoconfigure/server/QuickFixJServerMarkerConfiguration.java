package io.allune.quickfixj.spring.boot.starter.autoconfigure.server;

import org.springframework.context.annotation.Bean;

/**
 * Responsible for adding in a marker bean to trigger activation of 
 * {@link QuickFixJServerAutoConfiguration}
 *
 * @author Eduardo Sanchez-Ros
 */
public class QuickFixJServerMarkerConfiguration {
    @Bean
	public Marker quickFixJServerMarkerBean() {
		return new Marker();
	}

	class Marker {
    	//
	}
}