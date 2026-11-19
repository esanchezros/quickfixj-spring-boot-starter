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
import io.allune.fixmock.matching.FixMessagePatternBuilder;
import io.allune.fixmock.server.SimpleFixMockServer;
import io.allune.quickfixj.spring.boot.starter.integration.config.QuickFixJCommonConfiguration;
import io.allune.quickfixj.spring.boot.starter.integration.config.client.QuickFixJClientContextConfiguration;
import io.allune.quickfixj.spring.boot.starter.integration.config.server.QuickFixJServerContextConfiguration;
import io.allune.quickfixj.spring.boot.starter.template.QuickFixJTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import quickfix.Acceptor;
import quickfix.Connector;
import quickfix.Initiator;
import quickfix.Message;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.mina.acceptor.AbstractSocketAcceptor;
import quickfix.field.BeginString;
import quickfix.field.ClOrdID;
import quickfix.field.MsgType;
import quickfix.field.OrigClOrdID;
import quickfix.field.Side;
import quickfix.field.Symbol;
import quickfix.field.TransactTime;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.time.Duration;

import static io.allune.fixmock.FixMock.exactly;
import static io.allune.fixmock.FixMock.orderCancelRequest;
import static io.allune.quickfixj.api.Assertions.assertThat;
import static java.time.LocalDateTime.now;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

@Execution(SAME_THREAD)
public class QuickFixJFixMockInteroperabilityIntegrationTest {

	private static final Duration TIMEOUT = Duration.ofSeconds(10);
	private static final String TEST_PORT_PROPERTY = "quickfixj.test.port";
	private static final String CLIENT_CONFIG_PROPERTY =
			"quickfixj.client.config=classpath:integration/fixmock/quickfixj-client.cfg";
	private static final String SERVER_CONFIG_PROPERTY =
			"quickfixj.server.config=classpath:integration/fixmock/quickfixj-server.cfg";
	private static final SessionID CLIENT_SESSION_ID = new SessionID("FIX.4.4", "BANZAI", "EXEC");
	private static final SessionID SERVER_SESSION_ID = new SessionID("FIX.4.4", "EXEC", "BANZAI");
	private static final String SYMBOL = "EUR/USD";

	@Test
	public void shouldDeliverFromStarterInitiatorToFixMockAcceptor() {
		FixMockServerOptions options = FixMockServerOptions.builder()
				.role(FixMockServerOptions.Role.ACCEPTOR)
				.beginString("FIX.4.4")
				.senderCompId("EXEC")
				.targetCompId("BANZAI")
				.dynamicPort()
				.resetOnLogon(false)
				.resetOnDisconnect(false)
				.resetOnLogout(false)
				.build();

		try (AnnotationConfigApplicationContext parentContext =
					new AnnotationConfigApplicationContext(QuickFixJCommonConfiguration.class);
				FixMockServer fixMock = new SimpleFixMockServer(options)) {
			fixMock.start();
			assertTrue(fixMock.awaitReady(TIMEOUT), "FixMock acceptor should become ready");
			int fixMockPort = fixMock.getPort();
			assertTrue(fixMockPort > 0, "FixMock acceptor should bind an operating-system-assigned port");

			try (AnnotationConfigApplicationContext clientContext = createContext(
					parentContext,
					QuickFixJClientContextConfiguration.class,
					CLIENT_CONFIG_PROPERTY,
					fixMockPort)) {
				Initiator initiator = clientContext.getBean("clientInitiator", Initiator.class);
				awaitStarterLogon("starter initiator", initiator, CLIENT_SESSION_ID);
				assertTrue(fixMock.awaitLogon(TIMEOUT), "FixMock acceptor should observe initiator logon");

				QuickFixJTemplate template = parentContext.getBean(QuickFixJTemplate.class);
				assertPeerReceipt(fixMock, template, "INITIATOR-413", "BANZAI", "EXEC");
			}
		}
	}

	@Test
	public void shouldDeliverFromStarterAcceptorToFixMockInitiator() {
		try (AnnotationConfigApplicationContext parentContext =
					new AnnotationConfigApplicationContext(QuickFixJCommonConfiguration.class);
				AnnotationConfigApplicationContext serverContext = createContext(
						parentContext,
						QuickFixJServerContextConfiguration.class,
						SERVER_CONFIG_PROPERTY,
						0)) {
			Acceptor acceptor = serverContext.getBean("serverAcceptor", Acceptor.class);
			int serverPort = getBoundPort(acceptor);

			FixMockServerOptions options = FixMockServerOptions.builder()
					.role(FixMockServerOptions.Role.INITIATOR)
					.beginString("FIX.4.4")
					.senderCompId("BANZAI")
					.targetCompId("EXEC")
					.initiatorHost("127.0.0.1")
					.initiatorPort(serverPort)
					.resetOnLogon(false)
					.resetOnDisconnect(false)
					.resetOnLogout(false)
					.build();

			try (FixMockServer fixMock = new SimpleFixMockServer(options)) {
				fixMock.start();
				awaitStarterLogon("starter acceptor", acceptor, SERVER_SESSION_ID);
				assertTrue(fixMock.awaitLogon(TIMEOUT), "FixMock initiator should log on to starter acceptor");

				QuickFixJTemplate template = parentContext.getBean(QuickFixJTemplate.class);
				assertPeerReceipt(fixMock, template, "ACCEPTOR-413", "EXEC", "BANZAI");
			}
		}
	}

	private AnnotationConfigApplicationContext createContext(
			AnnotationConfigApplicationContext parentContext,
			Class<?> configuration,
			String configProperty,
			int port
	) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		try {
			context.setParent(parentContext);
			TestPropertyValues.of(configProperty, TEST_PORT_PROPERTY + "=" + port).applyTo(context);
			context.register(configuration);
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

	private void awaitStarterLogon(String role, Connector connector, SessionID sessionID) {
		await().alias(role + " session " + sessionID + " logged on")
				.atMost(TIMEOUT)
				.untilAsserted(() -> {
					assertEquals(1, connector.getSessions().size(), role + " session count");
					assertTrue(connector.getSessions().contains(sessionID), role + " should contain " + sessionID);
					Session session = Session.lookupSession(sessionID);
					assertNotNull(session, role + " session should be registered");
					assertTrue(session.isLoggedOn(), role + " session should be logged on");
				});
	}

	private int getBoundPort(Acceptor acceptor) {
		AbstractSocketAcceptor socketAcceptor = assertInstanceOf(AbstractSocketAcceptor.class, acceptor);
		assertEquals(1, socketAcceptor.getEndpoints().size(), "starter acceptor endpoint count");
		SocketAddress localAddress = socketAcceptor.getEndpoints().iterator().next().getLocalAddress();
		InetSocketAddress inetSocketAddress = assertInstanceOf(InetSocketAddress.class, localAddress);
		int port = inetSocketAddress.getPort();
		assertTrue(port > 0, "starter acceptor should bind an operating-system-assigned port");
		return port;
	}

	private void assertPeerReceipt(
			FixMockServer fixMock,
			QuickFixJTemplate template,
			String clOrdID,
			String senderCompID,
			String targetCompID
	) {
		String origClOrdID = "ORIG-" + clOrdID;
		quickfix.fix44.OrderCancelRequest message = new quickfix.fix44.OrderCancelRequest(
				new OrigClOrdID(origClOrdID),
				new ClOrdID(clOrdID),
				new Side(Side.BUY),
				new TransactTime(now()));
		message.set(new Symbol(SYMBOL));

		assertTrue(
				template.send(message, senderCompID, targetCompID),
				"starter " + senderCompID + "->" + targetCompID + " message should be sent");

		FixMessagePatternBuilder peerMessagePattern = orderCancelRequest()
				.inbound()
				.beginString("FIX.4.4")
				.senderCompId(senderCompID)
				.targetCompId(targetCompID)
				.withTag(ClOrdID.FIELD, clOrdID)
				.withTag(OrigClOrdID.FIELD, origClOrdID)
				.withTag(Symbol.FIELD, SYMBOL)
				.withTag(Side.FIELD, String.valueOf(Side.BUY));

		Message peerMessage = fixMock.awaitMessage(peerMessagePattern, TIMEOUT);
		fixMock.verify(exactly(1), peerMessagePattern);

		assertThat(peerMessage)
				.isVersion44()
				.isOrderCancelRequest()
				.hasFieldValue(ClOrdID.FIELD, clOrdID)
				.hasFieldValue(OrigClOrdID.FIELD, origClOrdID)
				.hasFieldValue(Symbol.FIELD, SYMBOL)
				.hasFieldValue(Side.FIELD, Side.BUY)
				.header()
				.hasBeginString("FIX.4.4")
				.hasMsgType(MsgType.ORDER_CANCEL_REQUEST)
				.hasSenderCompID(senderCompID)
				.hasTargetCompID(targetCompID);
	}
}
