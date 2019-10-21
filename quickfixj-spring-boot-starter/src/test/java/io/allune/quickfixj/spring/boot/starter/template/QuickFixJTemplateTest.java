package io.allune.quickfixj.spring.boot.starter.template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static quickfix.FixVersions.FIX50SP2;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import quickfix.DataDictionary;
import quickfix.DataDictionaryProvider;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.Message;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.field.BeginString;
import quickfix.field.SenderCompID;
import quickfix.field.TargetCompID;

public class QuickFixJTemplateTest {

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();

	@Mock
	private SessionLookupHandler sessionLookupHandler;

	@Mock
	private Session session;

	@Mock
	private Message message;

	@Mock
	private DataDictionary applicationDataDictionary;

	@Mock
	private DataDictionaryProvider dataDictionaryProvider;

	@InjectMocks
	private QuickFixJTemplate quickFixJTemplate;

	@Test
	public void shouldSendMessage() throws FieldNotFound {
		// Given
		String expectedSender = "Sender";
		String expectedTarget = "Target";
		mockMessage(expectedSender, expectedTarget);

		mockSessionFound();
		given(session.send(message)).willReturn(true);

		// When
		boolean sent = quickFixJTemplate.send(message);

		// Then
		ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
		verify(session).send(messageCaptor.capture());

		assertThat(sent).isTrue();
		assertThat(messageCaptor.getValue().getHeader().getString(SenderCompID.FIELD)).isEqualTo(expectedSender);
		assertThat(messageCaptor.getValue().getHeader().getString(TargetCompID.FIELD)).isEqualTo(expectedTarget);
	}

	@Test
	public void shouldSendMessageWithValidation() throws FieldNotFound, IncorrectTagValue, IncorrectDataFormat {
		// Given
		String expectedSender = "Sender";
		String expectedTarget = "Target";
		mockMessage(expectedSender, expectedTarget);
		mockSessionFound();
		mockDataDictionary();
		given(session.send(message)).willReturn(true);

		// When
		boolean sent = quickFixJTemplate.send(message);

		// Then
		ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
		verify(applicationDataDictionary).validate(messageCaptor.capture(), eq(true));
		verify(session).send(messageCaptor.getValue());

		assertThat(sent).isTrue();
		assertThat(messageCaptor.getValue().getHeader().getString(SenderCompID.FIELD)).isEqualTo(expectedSender);
		assertThat(messageCaptor.getValue().getHeader().getString(TargetCompID.FIELD)).isEqualTo(expectedTarget);
	}

	@Test
	public void shouldThrowSessionNotFoundException() throws FieldNotFound {
		// Given
		String expectedSender = "Sender";
		String expectedTarget = "Target";
		mockMessage(expectedSender, expectedTarget);
		mockSessionNotFound();

		// When/Then
		assertThatExceptionOfType(SessionNotFoundException.class).isThrownBy(() ->
				quickFixJTemplate.send(message));
	}

	@Test
	public void shouldThrowMessageValidationException() throws FieldNotFound, IncorrectTagValue, IncorrectDataFormat {
		// Given
		String expectedSender = "Sender";
		String expectedTarget = "Target";
		mockMessage(expectedSender, expectedTarget);
		mockSessionFound();
		mockDataDictionaryValidationFailure();

		// When/Then
		assertThatExceptionOfType(MessageValidationException.class).isThrownBy(() ->
				quickFixJTemplate.send(message));
	}

	private void mockMessage(String expectedSender, String expectedTarget) throws FieldNotFound {
		Message.Header header = mock(Message.Header.class);
		given(message.getHeader()).willReturn(header);
		given(header.getString(SenderCompID.FIELD)).willReturn(expectedSender);
		given(header.getString(TargetCompID.FIELD)).willReturn(expectedTarget);
		given(header.getString(BeginString.FIELD)).willReturn("Begin");
	}

	private void mockSessionFound() {
		given(sessionLookupHandler.lookupBySessionID(any())).willReturn(session);
		SessionID sessionID = mock(SessionID.class);
		given(sessionID.getBeginString()).willReturn(FIX50SP2);
		given(session.getSessionID()).willReturn(sessionID);
	}

	private void mockSessionNotFound() {
		given(sessionLookupHandler.lookupBySessionID(any())).willReturn(null);
	}

	private void mockDataDictionary() {
		given(session.getDataDictionaryProvider()).willReturn(dataDictionaryProvider);
		given(dataDictionaryProvider.getApplicationDataDictionary(any())).willReturn(applicationDataDictionary);
	}

	private void mockDataDictionaryValidationFailure() throws IncorrectDataFormat, FieldNotFound, IncorrectTagValue {
		mockDataDictionary();
		willThrow(IncorrectDataFormat.class).given(applicationDataDictionary).validate(any(), eq(true));
	}
}