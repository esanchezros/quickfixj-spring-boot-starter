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

import io.allune.quickfixj.spring.boot.starter.integration.config.QuickFixJCommonConfiguration;
import io.allune.quickfixj.spring.boot.starter.integration.config.client.QuickFixJClientContextConfiguration;
import io.allune.quickfixj.spring.boot.starter.integration.config.server.QuickFixJServerContextConfiguration;
import io.allune.quickfixj.spring.boot.starter.template.QuickFixJTemplate;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import quickfix.Acceptor;
import quickfix.Application;
import quickfix.Initiator;
import quickfix.Message;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.field.ClOrdID;
import quickfix.field.CxlType;
import quickfix.field.OrderQty;
import quickfix.field.OrigClOrdID;
import quickfix.field.Side;
import quickfix.field.Symbol;
import quickfix.field.TransactTime;

import java.util.List;

import static java.time.LocalDateTime.now;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Durations.TEN_SECONDS;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static quickfix.field.CxlType.PARTIAL_CANCEL;

/**
 * @author Eduardo Sanchez-Ros
 */
public class QuickFixJServerClientIntegrationTest {

	private AnnotationConfigApplicationContext parentContext;
	private AnnotationConfigApplicationContext serverContext;
	private AnnotationConfigApplicationContext clientContext;

	@BeforeEach
	public void setUp() {
		parentContext = new AnnotationConfigApplicationContext(QuickFixJCommonConfiguration.class);
		serverContext = new AnnotationConfigApplicationContext(QuickFixJServerContextConfiguration.class);
		clientContext = new AnnotationConfigApplicationContext(QuickFixJClientContextConfiguration.class);

		serverContext.setParent(parentContext);
		clientContext.setParent(parentContext);

		serverContext.start();
		clientContext.start();

		Acceptor serverAcceptor = serverContext.getBean("serverAcceptor", Acceptor.class);
		Initiator clientInitiator = clientContext.getBean("clientInitiator", Initiator.class);

		await().atMost(TEN_SECONDS).until(() ->
				serverAcceptor.getSessions()
						.stream()
						.allMatch(sessionID -> Session.lookupSession(sessionID).isLoggedOn())
		);
		await().atMost(TEN_SECONDS).until(() ->
				clientInitiator.getSessions()
						.stream()
						.allMatch(sessionID -> Session.lookupSession(sessionID).isLoggedOn())
		);
	}

	@AfterEach
	void tearDown() {
		System.out.println("closing clientContext now...");
		clientContext.close();
		System.out.println("closing serverContext now...");
		serverContext.close();
		System.out.println("closing parent now...");
		parentContext.close();
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

		assertThat(messageArgumentCaptor.getValue()).isEqualTo(message);
		assertThat(sessionIDArgumentCaptor.getValue().getSenderCompID()).isEqualTo(senderCompID);
		assertThat(sessionIDArgumentCaptor.getValue().getTargetCompID()).isEqualTo(targetCompID);
	}
}
