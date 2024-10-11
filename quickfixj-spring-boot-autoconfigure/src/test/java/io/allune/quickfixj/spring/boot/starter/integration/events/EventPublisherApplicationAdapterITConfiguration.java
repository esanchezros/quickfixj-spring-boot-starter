/*
 * Copyright 2017-2024 the original author or authors.
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
package io.allune.quickfixj.spring.boot.starter.integration.events;

import io.allune.quickfixj.spring.boot.starter.model.Create;
import io.allune.quickfixj.spring.boot.starter.model.FromAdmin;
import io.allune.quickfixj.spring.boot.starter.model.FromApp;
import io.allune.quickfixj.spring.boot.starter.model.Logon;
import io.allune.quickfixj.spring.boot.starter.model.Logout;
import io.allune.quickfixj.spring.boot.starter.model.ToAdmin;
import io.allune.quickfixj.spring.boot.starter.model.ToApp;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class EventPublisherApplicationAdapterITConfiguration {

	private final Events events = new Events();

	@Bean
	public Events events() {
		return events;
	}

	@EventListener
	public void listenFromAdmin(FromAdmin fromAdmin) {
		events.receivedEvents().add(fromAdmin);
	}

	@EventListener
	public void listenFromApp(FromApp fromApp) {
		events.receivedEvents().add(fromApp);
	}

	@EventListener
	public void listenOnCreate(Create create) {
		events.receivedEvents().add(create);
	}

	@EventListener
	public void listenOnLogon(Logon logon) {
		events.receivedEvents().add(logon);
	}

	@EventListener
	public void listenOnLogout(Logout logout) {
		events.receivedEvents().add(logout);
	}

	@EventListener
	public void listenToAdmin(ToAdmin toAdmin) {
		events.receivedEvents().add(toAdmin);
	}

	@EventListener
	public void listenToApp(ToApp toApp) {
		events.receivedEvents().add(toApp);
	}

	public static class Events {
		private final List<Object> receivedEvents = new ArrayList<>();

		public List<Object> receivedEvents() {
			return receivedEvents;
		}
	}
}