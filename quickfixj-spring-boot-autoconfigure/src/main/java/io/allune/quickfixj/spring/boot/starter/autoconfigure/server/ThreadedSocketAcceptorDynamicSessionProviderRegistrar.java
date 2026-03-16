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
package io.allune.quickfixj.spring.boot.starter.autoconfigure.server;

import quickfix.Acceptor;
import quickfix.Application;
import quickfix.ConfigError;
import quickfix.FieldConvertError;
import quickfix.LogFactory;
import quickfix.MessageFactory;
import quickfix.MessageStoreFactory;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.ThreadedSocketAcceptor;
import quickfix.mina.acceptor.DynamicAcceptorSessionProvider;

import java.net.InetSocketAddress;
import java.util.Iterator;

/**
 * Registers {@link DynamicAcceptorSessionProvider} instances for
 * {@link ThreadedSocketAcceptor} template sessions.
 *
 * @author ChildrenGreens
 */
public class ThreadedSocketAcceptorDynamicSessionProviderRegistrar {

	void registerDynamicSessionProviders(
			ThreadedSocketAcceptor acceptor,
			SessionSettings settings,
			Application application,
			MessageStoreFactory messageStoreFactory,
			LogFactory logFactory,
			MessageFactory messageFactory
	) throws ConfigError {
		try {
			Iterator<SessionID> iterator = settings.sectionIterator();
			while (iterator.hasNext()) {
				SessionID sessionId = iterator.next();
				if (!isAcceptorTemplate(settings, sessionId)) {
					continue;
				}

				acceptor.setSessionProvider(
						getAcceptorAddress(settings, sessionId),
						new DynamicAcceptorSessionProvider(
								settings,
								sessionId,
								application,
								messageStoreFactory,
								logFactory,
								messageFactory
						)
				);
			}
		} catch (FieldConvertError e) {
			throw new ConfigError(e);
		}
	}

	boolean isAcceptorTemplate(SessionSettings settings, SessionID sessionId) throws ConfigError {
		try {
			return settings.isSetting(sessionId, Acceptor.SETTING_ACCEPTOR_TEMPLATE)
					&& settings.getBool(sessionId, Acceptor.SETTING_ACCEPTOR_TEMPLATE);
		} catch (ConfigError | FieldConvertError e) {
			throw new ConfigError("Invalid AcceptorTemplate setting for session " + sessionId, e);
		}
	}

	InetSocketAddress getAcceptorAddress(SessionSettings settings, SessionID sessionId)
			throws ConfigError, FieldConvertError {
		long configuredPort = settings.getLong(sessionId, Acceptor.SETTING_SOCKET_ACCEPT_PORT);
		final int port;
		try {
			port = Math.toIntExact(configuredPort);
		} catch (ArithmeticException e) {
			throw new ConfigError("SocketAcceptPort out of int range for session " + sessionId + ": " + configuredPort, e);
		}
		if (port < 0 || port > 65_535) {
			throw new ConfigError("SocketAcceptPort out of valid range for session " + sessionId + ": " + configuredPort);
		}
		return new InetSocketAddress(port);
	}
}
