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
import quickfix.*;
import quickfix.field.ApplVerID;
import quickfix.field.BeginString;
import quickfix.field.SenderCompID;
import quickfix.field.TargetCompID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.*;

/**
 * @author Eduardo Sanchez-Ros
 */
@ExtendWith(MockitoExtension.class)
class QuickFixJTemplateTest {

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

	@Mock
	private ValidationSettings validationSettings;

	private QuickFixJTemplate quickFixJTemplate;

	private String expectedBeginString;

	private String expectedSender;

	private String expectedTarget;

	private String expectedQualifier;

	@BeforeEach
	void setUp() {
		expectedBeginString = "FIX.4.1";
		expectedSender = "Sender";
		expectedTarget = "Target";
		expectedQualifier = "Qualifier";
		quickFixJTemplate = new QuickFixJTemplate(sessionLookupHandler);
	}

	@Test
	void shouldSendMessage() throws FieldNotFound {
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
	void shouldSendMessageWithQualifier() throws FieldNotFound {
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
	void shouldSendMessageWithSenderAndTarget() throws FieldNotFound {
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
	void shouldSendMessageWithSenderAndTargetAndQualifier() throws FieldNotFound {
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
	void shouldSendMessageWithSessionID() throws FieldNotFound {
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
	void shouldSendMessageWithValidation() throws FieldNotFound, IncorrectTagValue, IncorrectDataFormat {
		// Given
		Message.Header header = mock(Message.Header.class);
		given(message.getHeader()).willReturn(header);
		given(header.getString(SenderCompID.FIELD)).willReturn(expectedSender);
		given(header.getString(TargetCompID.FIELD)).willReturn(expectedTarget);
		given(header.getString(BeginString.FIELD)).willReturn(expectedBeginString);
		given(sessionLookupHandler.lookupBySessionID(any())).willReturn(session);
		given(session.getDataDictionaryProvider()).willReturn(dataDictionaryProvider);
		given(dataDictionaryProvider.getApplicationDataDictionary(any())).willReturn(applicationDataDictionary);
		given(session.getValidationSettings()).willReturn(validationSettings);

		SessionID expectedSessionID = new SessionID(expectedBeginString, expectedSender, expectedTarget);
		given(session.send(message)).willReturn(true);

		// When
		boolean sent = quickFixJTemplate.send(message);

		// Then#
		assertSessionID(expectedSessionID);
		assertThat(sent).isTrue();
		assertMessageSent(expectedSender, expectedTarget);
		ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
		verify(applicationDataDictionary).validate(messageCaptor.capture(), eq(true), eq(validationSettings));
	}

	@Test
	void shouldThrowSessionNotFoundException() throws FieldNotFound {
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
	void shouldThrowMessageValidationException() throws FieldNotFound, IncorrectTagValue, IncorrectDataFormat {
		// Given
		Message.Header header = mock(Message.Header.class);
		given(message.getHeader()).willReturn(header);
		given(header.getString(SenderCompID.FIELD)).willReturn(expectedSender);
		given(header.getString(TargetCompID.FIELD)).willReturn(expectedTarget);
		given(header.getString(BeginString.FIELD)).willReturn(expectedBeginString);
		given(sessionLookupHandler.lookupBySessionID(any())).willReturn(session);
		given(session.getDataDictionaryProvider()).willReturn(dataDictionaryProvider);
		given(dataDictionaryProvider.getApplicationDataDictionary(any())).willReturn(applicationDataDictionary);
		given(session.getValidationSettings()).willReturn(validationSettings);
		willThrow(IncorrectDataFormat.class).given(applicationDataDictionary).validate(any(), eq(true), eq(validationSettings));

		// When/Then
		assertThatExceptionOfType(MessageValidationException.class)
			.isThrownBy(() -> quickFixJTemplate.send(message));
	}

	@Test
	void shouldThrowFieldNotFoundExceptionWithInvalidTargetCompID() throws FieldNotFound {
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
	void shouldThrowFieldNotFoundExceptionWithInvalidSenderCompID() throws FieldNotFound {
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
	void shouldThrowFieldNotFoundExceptionWithInvalidBeginString() throws FieldNotFound {
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
	void shouldBeAbleToChangeSessionLookupHandler() throws FieldNotFound {
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
	void shouldNotThrowMessageValidationExceptionGivenValidationIsDisabled() throws FieldNotFound, IncorrectTagValue, IncorrectDataFormat {
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
		verify(applicationDataDictionary, never()).validate(any(), eq(validationSettings));
	}

	@Test
	void shouldDoValidationForSpecificVersion() throws FieldNotFound, IncorrectTagValue, IncorrectDataFormat {
		// Given
		Message.Header header = mock(Message.Header.class);
		given(message.getHeader()).willReturn(header);
		given(header.getString(ApplVerID.FIELD)).willReturn(ApplVerID.FIX43);
		given(header.getString(SenderCompID.FIELD)).willReturn(expectedSender);
		given(header.getString(TargetCompID.FIELD)).willReturn(expectedTarget);
		given(header.getString(BeginString.FIELD)).willReturn(expectedBeginString);
		given(sessionLookupHandler.lookupBySessionID(any())).willReturn(session);
		given(session.getDataDictionaryProvider()).willReturn(dataDictionaryProvider);
		given(dataDictionaryProvider.getApplicationDataDictionary(new ApplVerID(ApplVerID.FIX43))).willReturn(applicationDataDictionary);
		given(session.getValidationSettings()).willReturn(validationSettings);
		quickFixJTemplate.setDoValidation(true);

		// When/Then
		assertThatCode(() -> quickFixJTemplate.send(message)).doesNotThrowAnyException();

		verify(session).getDataDictionaryProvider();
		verify(dataDictionaryProvider).getApplicationDataDictionary(any());
		verify(applicationDataDictionary).validate(any(), any(Boolean.class), eq(validationSettings));
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
