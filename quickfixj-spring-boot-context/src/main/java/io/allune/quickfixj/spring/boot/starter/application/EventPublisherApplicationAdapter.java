/*
 * Copyright 2017-2023 the original author or authors.
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
package io.allune.quickfixj.spring.boot.starter.application;

import io.allune.quickfixj.spring.boot.starter.model.Create;
import io.allune.quickfixj.spring.boot.starter.model.FromAdmin;
import io.allune.quickfixj.spring.boot.starter.model.FromApp;
import io.allune.quickfixj.spring.boot.starter.model.Logon;
import io.allune.quickfixj.spring.boot.starter.model.Logout;
import io.allune.quickfixj.spring.boot.starter.model.ToAdmin;
import io.allune.quickfixj.spring.boot.starter.model.ToApp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import quickfix.Application;
import quickfix.Message;
import quickfix.SessionID;

import java.util.function.Consumer;

/**
 * Implements the {@link Application} interface publishing the received payload as a Spring {@link ApplicationEvent} to all
 * matching listeners registered.
 * <p>
 * In case the {@link ApplicationEventPublisher#publishEvent(Object)} method throws an exception, this exception will be propagated up to
 * the {@link quickfix.Session#next()} method. Depending on the value of {@code RejectMessageOnUnhandledException} in the quickfixj
 * configuration, the message will be redelivered or dismissed.
 *
 *
 * <p>If this configuration is enabled, an uncaught Exception or Error in the application's message processing will lead to a (BusinessMessage)Reject being sent to
 * the counterparty and the incoming message sequence number will be incremented.
 *
 * <p>If disabled (default), the problematic incoming message is discarded and the message sequence number is not incremented. Processing of the next valid message
 * will cause detection of a sequence gap and a ResendRequest will be generated.
 *
 * @author Eduardo Sanchez-Ros
 */
@Slf4j
public class EventPublisherApplicationAdapter implements Application {

	private Consumer<Object> publishEventConsumer;

	public EventPublisherApplicationAdapter(ApplicationEventPublisher applicationEventPublisher) {
		this.publishEventConsumer = applicationEventPublisher::publishEvent;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void fromAdmin(Message message, SessionID sessionId) {
		publishEvent(FromAdmin.of(message, sessionId));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void fromApp(Message message, SessionID sessionId) {
		publishEvent(FromApp.of(message, sessionId));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onCreate(SessionID sessionId) {
		publishEvent(Create.of(sessionId));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onLogon(SessionID sessionId) {
		publishEvent(Logon.of(sessionId));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onLogout(SessionID sessionId) {
		publishEvent(Logout.of(sessionId));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void toAdmin(Message message, SessionID sessionId) {
		publishEvent(ToAdmin.of(message, sessionId));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void toApp(Message message, SessionID sessionId) {
		publishEvent(ToApp.of(message, sessionId));
	}

	private <T> void publishEvent(T event) {
		try {
			publishEventConsumer.accept(event);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
}
