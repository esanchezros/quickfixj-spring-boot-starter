package io.allune.quickfixj.spring.boot.starter.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Duration.TEN_SECONDS;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import io.allune.quickfixj.spring.boot.starter.template.QuickFixJTemplate;
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

/**
 * @author Eduardo Sanchez-Ros
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = QuickFixJServerClientITConfiguration.class)
public class QuickFixJServerClientIT {

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
