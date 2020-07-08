/*
 * Copyright 2017-2020 the original author or authors.
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

import io.allune.quickfixj.spring.boot.starter.template.QuickFixJTemplate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import quickfix.Acceptor;
import quickfix.Application;
import quickfix.Initiator;
import quickfix.Message;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.field.ClOrdID;
import quickfix.field.OrigClOrdID;
import quickfix.field.Side;
import quickfix.field.Symbol;
import quickfix.fix41.OrderCancelRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Duration.TEN_SECONDS;
import static org.mockito.Mockito.verify;

/**
 * @author Eduardo Sanchez-Ros
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = QuickFixJServerClientITConfiguration.class)
public class QuickFixJServerClientIntegrationTest {

	@Autowired
	private QuickFixJTemplate serverQuickFixJTemplate;

	@Autowired
	private QuickFixJTemplate clientQuickFixJTemplate;

	@Autowired
	private Acceptor serverAcceptor;

	@Autowired
	private Initiator clientInitiator;

	@Autowired
	private Application serverApplication;

	@Autowired
	private Application clientApplication;

	@Before
	public void setUp() {
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
		System.out.println("All sessions logged in");
	}

	@Test
	public void shouldSendMessageFromServerToClient() throws Exception {
		// Given
		OrderCancelRequest message = new OrderCancelRequest(
				new OrigClOrdID("123"),
				new ClOrdID("321"),
				new Symbol("LNUX"),
				new Side(Side.BUY));

		// When
		String senderCompID = "BANZAI";
		String targetCompID = "EXEC";
		boolean sent = serverQuickFixJTemplate.send(message, senderCompID, targetCompID);

		// Then
		assertThat(sent).isTrue();

		ArgumentCaptor<Message> messageArgumentCaptor = ArgumentCaptor.forClass(Message.class);
		ArgumentCaptor<SessionID> sessionIDArgumentCaptor = ArgumentCaptor.forClass(SessionID.class);
		verify(clientApplication).toApp(messageArgumentCaptor.capture(), sessionIDArgumentCaptor.capture());

		assertThat(messageArgumentCaptor.getValue()).isEqualTo(message);
		assertThat(sessionIDArgumentCaptor.getValue().getSenderCompID()).isEqualTo(senderCompID);
		assertThat(sessionIDArgumentCaptor.getValue().getTargetCompID()).isEqualTo(targetCompID);
	}

	@Test
	public void shouldSendMessageFromClientToServer() throws Exception {
		// Given
		OrderCancelRequest message = new OrderCancelRequest(
				new OrigClOrdID("123"),
				new ClOrdID("321"),
				new Symbol("LNUX"),
				new Side(Side.BUY));

		// When
		String senderCompID = "EXEC";
		String targetCompID = "BANZAI";
		boolean sent = clientQuickFixJTemplate.send(message, senderCompID, targetCompID);

		// Then
		assertThat(sent).isTrue();

		ArgumentCaptor<Message> messageArgumentCaptor = ArgumentCaptor.forClass(Message.class);
		ArgumentCaptor<SessionID> sessionIDArgumentCaptor = ArgumentCaptor.forClass(SessionID.class);
		verify(serverApplication).toApp(messageArgumentCaptor.capture(), sessionIDArgumentCaptor.capture());

		assertThat(messageArgumentCaptor.getValue()).isEqualTo(message);
		assertThat(sessionIDArgumentCaptor.getValue().getSenderCompID()).isEqualTo(senderCompID);
		assertThat(sessionIDArgumentCaptor.getValue().getTargetCompID()).isEqualTo(targetCompID);
	}
}
