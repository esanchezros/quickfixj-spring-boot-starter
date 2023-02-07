/*
 * Copyright 2017-2022 the original author or authors.
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
package io.allune.quickfixj.spring.boot.starter.template;

import io.allune.quickfixj.spring.boot.starter.exception.FieldNotFoundException;
import io.allune.quickfixj.spring.boot.starter.exception.MessageValidationException;
import io.allune.quickfixj.spring.boot.starter.exception.SessionNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * @author Eduardo Sanchez-Ros
 */
@ExtendWith(MockitoExtension.class)
public class QuickFixJTemplateTest {

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

	private QuickFixJTemplate quickFixJTemplate;

	private String expectedBeginString;

	private String expectedSender;

	private String expectedTarget;

	private String expectedQualifier;

	@BeforeEach
	public void setUp() {
		expectedBeginString = "FIX.4.1";
		expectedSender = "Sender";
		expectedTarget = "Target";
		expectedQualifier = "Qualifier";
		quickFixJTemplate = new QuickFixJTemplate(sessionLookupHandler);
	}

	@Test
	public void shouldSendMessage() throws FieldNotFound {
		// Given

		Message.Header header = mock(Message.Header.class);
		given(message.getHeader()).willReturn(header);
		given(header.getString(SenderCompID.FIELD)).willReturn(expectedSender);
		given(header.getString(TargetCompID.FIELD)).willReturn(expectedTarget);
		given(header.getString(BeginString.FIELD)).willReturn(expectedBeginString);
		given(sessionLookupHandler.lookupBySessionID(any())).willReturn(session);

		SessionID expectedSessionID = new SessionID(expectedBeginString, expectedSender, expectedTarget);
		given(session.send(message)).willReturn(true);

		// When
		boolean sent = quickFixJTemplate.send(message);

		// Then
		assertThat(sent).isTrue();
		assertSessionID(expectedSessionID);
		assertMessageSent(expectedSender, expectedTarget);
	}

	@Test
	public void shouldSendMessageWithQualifier() throws FieldNotFound {
		// Given
		Message.Header header = mock(Message.Header.class);
		given(message.getHeader()).willReturn(header);
		given(header.getString(SenderCompID.FIELD)).willReturn(expectedSender);
		given(header.getString(TargetCompID.FIELD)).willReturn(expectedTarget);
		given(header.getString(BeginString.FIELD)).willReturn(expectedBeginString);
		given(sessionLookupHandler.lookupBySessionID(any())).willReturn(session);

		SessionID expectedSessionID = new SessionID(expectedBeginString, expectedSender, expectedTarget, expectedQualifier);
		given(session.send(message)).willReturn(true);

		// When
		boolean sent = quickFixJTemplate.send(message, expectedQualifier);

		// Then
		assertThat(sent).isTrue();
		assertSessionID(expectedSessionID);
		assertMessageSent(expectedSender, expectedTarget);
	}

	@Test
	public void shouldSendMessageWithSenderAndTarget() throws FieldNotFound {
		// Given
		Message.Header header = mock(Message.Header.class);
		given(message.getHeader()).willReturn(header);
		given(header.getString(SenderCompID.FIELD)).willReturn(expectedSender);
		given(header.getString(TargetCompID.FIELD)).willReturn(expectedTarget);
		given(header.getString(BeginString.FIELD)).willReturn(expectedBeginString);
		given(sessionLookupHandler.lookupBySessionID(any())).willReturn(session);

		SessionID expectedSessionID = new SessionID(expectedBeginString, expectedSender, expectedTarget);
		given(session.send(message)).willReturn(true);

		// When
		boolean sent = quickFixJTemplate.send(message, expectedSender, expectedTarget);

		// Then
		assertThat(sent).isTrue();
		assertSessionID(expectedSessionID);
		assertMessageSent(expectedSender, expectedTarget);
	}

	@Test
	public void shouldSendMessageWithSenderAndTargetAndQualifier() throws FieldNotFound {
		// Given
		Message.Header header = mock(Message.Header.class);
		given(message.getHeader()).willReturn(header);
		given(header.getString(SenderCompID.FIELD)).willReturn(expectedSender);
		given(header.getString(TargetCompID.FIELD)).willReturn(expectedTarget);
		given(header.getString(BeginString.FIELD)).willReturn(expectedBeginString);
		given(sessionLookupHandler.lookupBySessionID(any())).willReturn(session);

		SessionID expectedSessionID = new SessionID(expectedBeginString, expectedSender, expectedTarget, expectedQualifier);
		given(session.send(message)).willReturn(true);

		// When
		boolean sent = quickFixJTemplate.send(message, expectedSender, expectedTarget, expectedQualifier);

		// Then
		assertThat(sent).isTrue();
		assertSessionID(expectedSessionID);
		assertMessageSent(expectedSender, expectedTarget);
	}

	@Test
	public void shouldSendMessageWithSessionID() throws FieldNotFound {
		// Given
		Message.Header header = mock(Message.Header.class);
		given(message.getHeader()).willReturn(header);
		given(header.getString(SenderCompID.FIELD)).willReturn(expectedSender);
		given(header.getString(TargetCompID.FIELD)).willReturn(expectedTarget);
		given(sessionLookupHandler.lookupBySessionID(any())).willReturn(session);

		SessionID expectedSessionID = new SessionID(expectedBeginString, expectedSender, expectedTarget, expectedQualifier);
		given(session.send(message)).willReturn(true);

		// When
		boolean sent = quickFixJTemplate.send(message, expectedSessionID);

		// Then
		assertThat(sent).isTrue();
		assertSessionID(expectedSessionID);
		assertMessageSent(expectedSender, expectedTarget);
	}

	@Test
	public void shouldSendMessageWithValidation() throws FieldNotFound, IncorrectTagValue, IncorrectDataFormat {
		// Given
		Message.Header header = mock(Message.Header.class);
		given(message.getHeader()).willReturn(header);
		given(header.getString(SenderCompID.FIELD)).willReturn(expectedSender);
		given(header.getString(TargetCompID.FIELD)).willReturn(expectedTarget);
		given(header.getString(BeginString.FIELD)).willReturn(expectedBeginString);
		given(sessionLookupHandler.lookupBySessionID(any())).willReturn(session);
		given(session.getDataDictionaryProvider()).willReturn(dataDictionaryProvider);
		given(dataDictionaryProvider.getApplicationDataDictionary(any())).willReturn(applicationDataDictionary);

		SessionID expectedSessionID = new SessionID(expectedBeginString, expectedSender, expectedTarget);
		given(session.send(message)).willReturn(true);

		// When
		boolean sent = quickFixJTemplate.send(message);

		// Then#
		assertSessionID(expectedSessionID);
		assertThat(sent).isTrue();
		assertMessageSent(expectedSender, expectedTarget);
		ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
		verify(applicationDataDictionary).validate(messageCaptor.capture(), eq(true));
	}

	@Test
	public void shouldThrowSessionNotFoundException() throws FieldNotFound {
		// Given
		Message.Header header = mock(Message.Header.class);
		given(message.getHeader()).willReturn(header);
		given(header.getString(SenderCompID.FIELD)).willReturn(expectedSender);
		given(header.getString(TargetCompID.FIELD)).willReturn(expectedTarget);
		given(header.getString(BeginString.FIELD)).willReturn(expectedBeginString);
		given(sessionLookupHandler.lookupBySessionID(any())).willReturn(null);

		// When/Then
		assertThatExceptionOfType(SessionNotFoundException.class)
				.isThrownBy(() -> quickFixJTemplate.send(message));
	}

	@Test
	public void shouldThrowMessageValidationException() throws FieldNotFound, IncorrectTagValue, IncorrectDataFormat {
		// Given
		Message.Header header = mock(Message.Header.class);
		given(message.getHeader()).willReturn(header);
		given(header.getString(SenderCompID.FIELD)).willReturn(expectedSender);
		given(header.getString(TargetCompID.FIELD)).willReturn(expectedTarget);
		given(header.getString(BeginString.FIELD)).willReturn(expectedBeginString);
		given(sessionLookupHandler.lookupBySessionID(any())).willReturn(session);
		given(session.getDataDictionaryProvider()).willReturn(dataDictionaryProvider);
		given(dataDictionaryProvider.getApplicationDataDictionary(any())).willReturn(applicationDataDictionary);
		willThrow(IncorrectDataFormat.class).given(applicationDataDictionary).validate(any(), eq(true));

		// When/Then
		assertThatExceptionOfType(MessageValidationException.class)
				.isThrownBy(() -> quickFixJTemplate.send(message));
	}

	@Test
	public void shouldThrowFieldNotFoundExceptionWithInvalidTargetCompID() throws FieldNotFound {
		// Given
		Message.Header header = mock(Message.Header.class);
		given(message.getHeader()).willReturn(header);
		given(header.getString(BeginString.FIELD)).willReturn(expectedBeginString);
		given(header.getString(SenderCompID.FIELD)).willReturn(expectedSender);
		given(header.getString(TargetCompID.FIELD)).willThrow(FieldNotFound.class);

		// When/Then
		assertThatExceptionOfType(FieldNotFoundException.class)
				.isThrownBy(() -> quickFixJTemplate.send(message))
				.withMessageContaining("Field with ID 56 not found in message");
	}

	@Test
	public void shouldThrowFieldNotFoundExceptionWithInvalidSenderCompID() throws FieldNotFound {
		// Given
		Message.Header header = mock(Message.Header.class);
		given(message.getHeader()).willReturn(header);
		given(header.getString(BeginString.FIELD)).willReturn(expectedBeginString);
		given(header.getString(SenderCompID.FIELD)).willThrow(FieldNotFound.class);

		// When/Then
		assertThatExceptionOfType(FieldNotFoundException.class)
				.isThrownBy(() -> quickFixJTemplate.send(message))
				.withMessageContaining("Field with ID 49 not found in message");
	}

	@Test
	public void shouldThrowFieldNotFoundExceptionWithInvalidBeginString() throws FieldNotFound {
		// Given
		Message.Header header = mock(Message.Header.class);
		given(message.getHeader()).willReturn(header);
		given(header.getString(BeginString.FIELD)).willThrow(FieldNotFound.class);

		// When/Then
		assertThatExceptionOfType(FieldNotFoundException.class)
				.isThrownBy(() -> quickFixJTemplate.send(message))
				.withMessageContaining("Field with ID 8 not found in message");
	}

	@Test
	public void shouldBeAbleToChangeSessionLookupHandler() throws FieldNotFound {
		// Given
		SessionLookupHandler newSessionLookupHandler = mock(SessionLookupHandler.class);
		given(newSessionLookupHandler.lookupBySessionID(any())).willReturn(session);
		quickFixJTemplate.setSessionLookupHandler(newSessionLookupHandler);

		Message.Header header = mock(Message.Header.class);
		given(message.getHeader()).willReturn(header);
		given(header.getString(SenderCompID.FIELD)).willReturn(expectedSender);
		given(header.getString(TargetCompID.FIELD)).willReturn(expectedTarget);
		given(header.getString(BeginString.FIELD)).willReturn(expectedBeginString);

		// When
		quickFixJTemplate.send(message);

		// Then
		verify(newSessionLookupHandler).lookupBySessionID(any());
		verifyNoInteractions(sessionLookupHandler);
	}

	@Test
	public void shouldNotThrowMessageValidationExceptionGivenValidationIsDisabled() throws FieldNotFound, IncorrectTagValue, IncorrectDataFormat {
		// Given
		Message.Header header = mock(Message.Header.class);
		given(message.getHeader()).willReturn(header);
		given(header.getString(SenderCompID.FIELD)).willReturn(expectedSender);
		given(header.getString(TargetCompID.FIELD)).willReturn(expectedTarget);
		given(header.getString(BeginString.FIELD)).willReturn(expectedBeginString);
		given(sessionLookupHandler.lookupBySessionID(any())).willReturn(session);
		quickFixJTemplate.setDoValidation(false);

		// When/Then
		assertThatCode(() -> quickFixJTemplate.send(message)).doesNotThrowAnyException();

		verify(session, never()).getDataDictionaryProvider();
		verify(dataDictionaryProvider, never()).getApplicationDataDictionary(any());
		verify(applicationDataDictionary, never()).validate(any());
	}

	private void assertSessionID(SessionID expectedSessionID) {
		ArgumentCaptor<SessionID> sessionIDCaptor = ArgumentCaptor.forClass(SessionID.class);
		verify(sessionLookupHandler).lookupBySessionID(sessionIDCaptor.capture());
		assertThat(sessionIDCaptor.getValue()).isEqualTo(expectedSessionID);
	}

	private void assertMessageSent(String expectedSender, String expectedTarget) throws FieldNotFound {
		ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
		verify(session).send(messageCaptor.capture());
		assertThat(messageCaptor.getValue().getHeader().getString(SenderCompID.FIELD)).isEqualTo(expectedSender);
		assertThat(messageCaptor.getValue().getHeader().getString(TargetCompID.FIELD)).isEqualTo(expectedTarget);
	}
}
