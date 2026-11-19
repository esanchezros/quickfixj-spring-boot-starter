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

import io.allune.quickfixj.api.MessageAssert;
import io.allune.quickfixj.spring.boot.starter.integration.config.QuickFixJCommonConfiguration;
import io.allune.quickfixj.spring.boot.starter.integration.config.client.QuickFixJClientContextConfiguration;
import io.allune.quickfixj.spring.boot.starter.integration.config.server.QuickFixJServerContextConfiguration;
import io.allune.quickfixj.spring.boot.starter.template.QuickFixJTemplate;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import quickfix.Acceptor;
import quickfix.Application;
import quickfix.Connector;
import quickfix.FixVersions;
import quickfix.Initiator;
import quickfix.Message;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.mina.acceptor.AbstractSocketAcceptor;
import quickfix.field.ApplVerID;
import quickfix.field.BeginString;
import quickfix.field.ClOrdID;
import quickfix.field.CxlType;
import quickfix.field.MsgType;
import quickfix.field.OrderQty;
import quickfix.field.OrigClOrdID;
import quickfix.field.Side;
import quickfix.field.Symbol;
import quickfix.field.TransactTime;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;

import static io.allune.quickfixj.api.Assertions.assertThat;
import static java.time.LocalDateTime.now;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Durations.TEN_SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static quickfix.field.CxlType.PARTIAL_CANCEL;

/**
 * @author Eduardo Sanchez-Ros
 */
public class QuickFixJServerClientIntegrationTest {

	private static final int EXPECTED_SESSION_COUNT = 6;
	private static final String TEST_PORT_PROPERTY = "quickfixj.test.port";

	private AnnotationConfigApplicationContext parentContext;
	private AnnotationConfigApplicationContext serverContext;
	private AnnotationConfigApplicationContext clientContext;

	@BeforeEach
	public void setUp() {
		parentContext = new AnnotationConfigApplicationContext(QuickFixJCommonConfiguration.class);
		serverContext = createContext(QuickFixJServerContextConfiguration.class, 0);

		Acceptor serverAcceptor = serverContext.getBean("serverAcceptor", Acceptor.class);
		int serverPort = getBoundPort(serverAcceptor);

		clientContext = createContext(QuickFixJClientContextConfiguration.class, serverPort);
		Initiator clientInitiator = clientContext.getBean("clientInitiator", Initiator.class);

		awaitLoggedOn("acceptor", serverAcceptor);
		awaitLoggedOn("initiator", clientInitiator);
	}

	@AfterEach
	void tearDown() {
		try {
			closeContext(clientContext);
		} finally {
			try {
				closeContext(serverContext);
			} finally {
				closeContext(parentContext);
			}
		}
	}

	private AnnotationConfigApplicationContext createContext(Class<?> configuration, int port) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		try {
			context.setParent(parentContext);
			TestPropertyValues.of(TEST_PORT_PROPERTY + "=" + port).applyTo(context);
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

	private int getBoundPort(Acceptor acceptor) {
		assertThat(acceptor).isInstanceOf(AbstractSocketAcceptor.class);
		AbstractSocketAcceptor socketAcceptor = (AbstractSocketAcceptor) acceptor;

		assertThat(socketAcceptor.getEndpoints()).as("acceptor endpoints").hasSize(1);
		SocketAddress localAddress = socketAcceptor.getEndpoints().iterator().next().getLocalAddress();
		assertThat(localAddress).as("acceptor endpoint address").isInstanceOf(InetSocketAddress.class);

		int port = ((InetSocketAddress) localAddress).getPort();
		assertThat(port).as("operating-system-assigned acceptor port").isPositive();
		return port;
	}

	private void awaitLoggedOn(String connectorName, Connector connector) {
		await().alias("all " + connectorName + " sessions logged on")
				.atMost(TEN_SECONDS)
				.untilAsserted(() -> {
					assertThat(connector.getSessions()).as(connectorName + " sessions")
							.hasSize(EXPECTED_SESSION_COUNT)
							.allSatisfy(sessionID -> {
								Session session = Session.lookupSession(sessionID);
								assertThat(session).as("session " + sessionID).isNotNull();
								assertThat(session.isLoggedOn()).as("session " + sessionID + " logged on").isTrue();
							});
				});
	}

	private void closeContext(AnnotationConfigApplicationContext context) {
		if (context != null) {
			context.close();
		}
	}

	@Test
	public void shouldSendMessagesFromServerToClient() {
		// Given
		List<Message> messages = getMessages();
		String senderCompID = "BANZAI";
		String targetCompID = "EXEC";

		QuickFixJTemplate quickFixJTemplate = parentContext.getBean("quickFixJTemplate", QuickFixJTemplate.class);
		Application clientApplication = clientContext.getBean("clientApplication", Application.class);

		// When
		messages.forEach(message -> {
			boolean sent = quickFixJTemplate.send(message, senderCompID, targetCompID);

			// Then
			assertThat(sent).isTrue();
			assertMessageSentCorrectly(message, senderCompID, targetCompID, clientApplication);
			reset(clientApplication);
		});
	}

	@Test
	public void shouldSendMessagesFromClientToServer() {
		// Given
		List<Message> messages = getMessages();
		String senderCompID = "EXEC";
		String targetCompID = "BANZAI";

		QuickFixJTemplate quickFixJTemplate = parentContext.getBean("quickFixJTemplate", QuickFixJTemplate.class);
		Application serverApplication = serverContext.getBean("serverApplication", Application.class);

		// When
		messages.forEach(message -> {
			boolean sent = quickFixJTemplate.send(message, senderCompID, targetCompID);

			// Then
			assertThat(sent).isTrue();
			assertMessageSentCorrectly(message, senderCompID, targetCompID, serverApplication);
			reset(serverApplication);
		});
	}

	private List<Message> getMessages() {
		quickfix.fix40.OrderCancelRequest orderCancelRequestV40 = new quickfix.fix40.OrderCancelRequest(
				new OrigClOrdID("123"),
				new ClOrdID("321"),
				new CxlType(PARTIAL_CANCEL),
				new Symbol("LNUX"),
				new Side(Side.BUY),
				new OrderQty(1));
		quickfix.fix41.OrderCancelRequest orderCancelRequestV41 = new quickfix.fix41.OrderCancelRequest(
				new OrigClOrdID("123"),
				new ClOrdID("321"),
				new Symbol("LNUX"),
				new Side(Side.BUY));
		quickfix.fix42.OrderCancelRequest orderCancelRequestV42 = new quickfix.fix42.OrderCancelRequest(
				new OrigClOrdID("123"),
				new ClOrdID("321"),
				new Symbol("LNUX"),
				new Side(Side.BUY),
				new TransactTime(now()));
		quickfix.fix43.OrderCancelRequest orderCancelRequestV43 = new quickfix.fix43.OrderCancelRequest(
				new OrigClOrdID("123"),
				new ClOrdID("321"),
				new Side(Side.BUY),
				new TransactTime(now()));
		orderCancelRequestV43.set(new Symbol("EUR"));
		quickfix.fix44.OrderCancelRequest orderCancelRequestV44 = new quickfix.fix44.OrderCancelRequest(
				new OrigClOrdID("123"),
				new ClOrdID("321"),
				new Side(Side.BUY),
				new TransactTime(now()));
		orderCancelRequestV44.set(new Symbol("EUR"));
		quickfix.fix50.OrderCancelRequest orderCancelRequestV50 = new quickfix.fix50.OrderCancelRequest(
				new OrigClOrdID("123"),
				new ClOrdID("321"),
				new Side(Side.BUY),
				new TransactTime(now()));
		quickfix.fix50sp1.OrderCancelRequest orderCancelRequestV50sp1 = new quickfix.fix50sp1.OrderCancelRequest(
				new ClOrdID("321"),
				new Side(Side.BUY),
				new TransactTime(now()));
		quickfix.fix50sp2.OrderCancelRequest orderCancelRequestV50sp2 = new quickfix.fix50sp2.OrderCancelRequest(
				new ClOrdID("321"),
				new Side(Side.BUY),
				new TransactTime(now()));
		return asList(
				orderCancelRequestV40,
				orderCancelRequestV41,
				orderCancelRequestV42,
				orderCancelRequestV43,
				orderCancelRequestV44,
				orderCancelRequestV50,
				orderCancelRequestV50sp1,
				orderCancelRequestV50sp2);
	}

	@SneakyThrows
	private void assertMessageSentCorrectly(
			Message message,
			String senderCompID,
			String targetCompID,
			Application application
	) {
		ArgumentCaptor<Message> messageArgumentCaptor = ArgumentCaptor.forClass(Message.class);
		ArgumentCaptor<SessionID> sessionIDArgumentCaptor = ArgumentCaptor.forClass(SessionID.class);
		verify(application).toApp(messageArgumentCaptor.capture(), sessionIDArgumentCaptor.capture());

		Message capturedMessage = messageArgumentCaptor.getValue();
		assertEquals(message, capturedMessage);
		MessageAssert capturedMessageAssert = assertThat(capturedMessage)
				.isOrderCancelRequest();
		boolean applicationVersionResolvable = !FixVersions.BEGINSTRING_FIXT11.equals(
				capturedMessage.getHeader().getString(BeginString.FIELD))
				|| capturedMessage.getHeader().isSetField(ApplVerID.FIELD);
		// AssertJ QuickFIX/J needs ApplVerID to select a dictionary for FIXT field and header assertions.
		if (applicationVersionResolvable) {
			capturedMessageAssert
					.hasFieldValue(ClOrdID.FIELD, "321")
					.header()
					.hasMsgType(MsgType.ORDER_CANCEL_REQUEST)
					.hasSenderCompID(senderCompID)
					.hasTargetCompID(targetCompID);
		}
		assertThat(sessionIDArgumentCaptor.getValue().getSenderCompID()).isEqualTo(senderCompID);
		assertThat(sessionIDArgumentCaptor.getValue().getTargetCompID()).isEqualTo(targetCompID);
	}
}
