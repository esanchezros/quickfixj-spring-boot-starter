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
package io.allune.quickfixj.spring.boot.starter.integration;

import io.allune.fixmock.FixMockServer;
import io.allune.fixmock.FixMockServerOptions;
import io.allune.fixmock.server.SimpleFixMockServer;
import io.allune.fixmock.verify.Direction;
import io.allune.fixmock.verify.FixJournalEntry;
import io.allune.quickfixj.spring.boot.starter.integration.config.QuickFixJCommonConfiguration;
import io.allune.quickfixj.spring.boot.starter.integration.config.server.QuickFixJServerContextConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;
import quickfix.Acceptor;
import quickfix.Log;
import quickfix.LogFactory;
import quickfix.Message;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionState;
import quickfix.mina.acceptor.AbstractSocketAcceptor;
import quickfix.field.MsgType;
import quickfix.field.ResetSeqNumFlag;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static io.allune.quickfixj.api.Assertions.assertThat;
import static java.time.Duration.ofSeconds;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QuickFixJResetSequenceIntegrationTest {

	private static final String TEST_PORT_PROPERTY = "quickfixj.test.port";
	private static final String RECEIVED_LOGON_EVENT = "Received logon";
	private static final SessionID TEST_SESSION_ID = new SessionID("FIX.4.4", "EXEC", "BANZAI");

	private AnnotationConfigApplicationContext parentContext;
	private AnnotationConfigApplicationContext serverContext;
	private FixMockServer fixMock;
	private final HeartbeatInterleavingLogFactory logFactory = new HeartbeatInterleavingLogFactory();

	@BeforeEach
	public void setUp() {
		parentContext = new AnnotationConfigApplicationContext(QuickFixJCommonConfiguration.class);
		serverContext = createServerContext();
	}

	@AfterEach
	void tearDown() {
		try {
			if (fixMock != null) {
				fixMock.close();
			}
		} finally {
			try {
				closeContext(serverContext);
			} finally {
				closeContext(parentContext);
			}
		}
	}

	@Test
	public void shouldKeepResetLogonAtSequenceOneWhenHeartbeatTimerInterleaves() throws IOException {
		Acceptor serverAcceptor = serverContext.getBean("serverAcceptor", Acceptor.class);
		Session serverSession = getSession(serverAcceptor);
		serverSession.setNextSenderMsgSeqNum(5);
		serverSession.setNextTargetMsgSeqNum(5);
		makeHeartbeatDue(serverSession);
		logFactory.interleaveNextTimerTickOnLogon(serverSession);

		int serverPort = getBoundPort(serverAcceptor);

		FixMockServerOptions options = FixMockServerOptions.builder()
				.role(FixMockServerOptions.Role.INITIATOR)
				.beginString("FIX.4.4")
				.senderCompId("BANZAI")
				.targetCompId("EXEC")
				.initiatorHost("127.0.0.1")
				.initiatorPort(serverPort)
				.resetOnLogon(true)
				.build();

		fixMock = new SimpleFixMockServer(options);
		fixMock.start();

		assertTrue(fixMock.awaitLogon(ofSeconds(10)), "FixMock initiator should log on");

		Message resetLogonRequest = awaitLogon(false);
		Message resetLogonResponse = awaitLogon(true);

		assertTrue(logFactory.wasTimerTickInterleaved(), "heartbeat timer tick should run before the acceptor Logon response");
		assertThat(resetLogonRequest)
				.isLogon()
				.hasFieldValue(ResetSeqNumFlag.FIELD, true)
				.header()
				.hasMsgSeqNum(1);
		assertThat(resetLogonResponse)
				.isLogon()
				.hasFieldValue(ResetSeqNumFlag.FIELD, true)
				.header()
				.hasMsgSeqNum(1);
	}

	private AnnotationConfigApplicationContext createServerContext() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		try {
			context.setParent(parentContext);
			TestPropertyValues.of(TEST_PORT_PROPERTY + "=0").applyTo(context);
			context.registerBean("serverLogFactory", LogFactory.class, () -> logFactory);
			context.register(QuickFixJServerContextConfiguration.class);
			context.refresh();
			return context;
		} catch (RuntimeException | Error exception) {
			try {
				context.close();
			} catch (RuntimeException closeException) {
				exception.addSuppressed(closeException);
			}
			throw exception;
		}
	}

	private Session getSession(Acceptor acceptor) {
		assertTrue(acceptor.getSessions().contains(TEST_SESSION_ID), "FIX 4.4 acceptor session should be configured");
		Session session = Session.lookupSession(TEST_SESSION_ID);
		return Objects.requireNonNull(session, "acceptor session should be registered");
	}

	private void makeHeartbeatDue(Session session) {
		SessionState state = assertInstanceOf(SessionState.class, ReflectionTestUtils.getField(session, "state"));
		state.setLastSentTime(0);
	}

	private int getBoundPort(Acceptor acceptor) {
		AbstractSocketAcceptor socketAcceptor = assertInstanceOf(AbstractSocketAcceptor.class, acceptor);
		assertEquals(1, socketAcceptor.getEndpoints().size(), "acceptor endpoint count");
		SocketAddress localAddress = socketAcceptor.getEndpoints().iterator().next().getLocalAddress();
		InetSocketAddress inetSocketAddress = assertInstanceOf(InetSocketAddress.class, localAddress);
		int port = inetSocketAddress.getPort();
		assertTrue(port > 0, "operating-system-assigned acceptor port should be positive");
		return port;
	}

	private Message awaitLogon(boolean inbound) {
		return await().alias(inbound ? "acceptor reset Logon response" : "initiator reset Logon request")
				.atMost(ofSeconds(10))
				.until(
						() -> fixMock.getJournalSnapshot().stream()
								.filter(entry -> isLogon(entry, inbound))
								.findFirst()
								.map(FixJournalEntry::getMessage)
								.orElse(null),
						Objects::nonNull
				);
	}

	private boolean isLogon(FixJournalEntry entry, boolean inbound) {
		return entry.getDirection() == Direction.ADMIN
				&& entry.isInbound() == inbound
				&& MsgType.LOGON.equals(entry.getMsgType());
	}

	private void closeContext(AnnotationConfigApplicationContext context) {
		if (context != null) {
			context.close();
		}
	}

	private static final class HeartbeatInterleavingLogFactory implements LogFactory {

		private final AtomicReference<Session> session = new AtomicReference<>();
		private final AtomicBoolean timerTickInterleaved = new AtomicBoolean();

		@Override
		public Log create(SessionID sessionID) {
			return new Log() {
				@Override
				public void clear() {
				}

				@Override
				public void onIncoming(String message) {
				}

				@Override
				public void onOutgoing(String message) {
				}

				@Override
				public void onEvent(String message) {
					// Emitted after QuickFIX/J resets and marks the Logon received, but before the acceptor responds.
					if (RECEIVED_LOGON_EVENT.equals(message) && timerTickInterleaved.compareAndSet(false, true)) {
						try {
							session.get().next();
						} catch (IOException exception) {
							throw new UncheckedIOException(exception);
						}
					}
				}

				@Override
				public void onErrorEvent(String message) {
				}
			};
		}

		private void interleaveNextTimerTickOnLogon(Session session) {
			this.session.set(session);
		}

		private boolean wasTimerTickInterleaved() {
			return timerTickInterleaved.get();
		}
	}
}
