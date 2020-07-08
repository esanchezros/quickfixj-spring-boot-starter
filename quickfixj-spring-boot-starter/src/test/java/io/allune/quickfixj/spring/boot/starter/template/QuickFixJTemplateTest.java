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
package io.allune.quickfixj.spring.boot.starter.template;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static quickfix.FixVersions.FIX50SP2;

/**
 * @author Eduardo Sanchez-Ros
 */
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

	private QuickFixJTemplate quickFixJTemplate;

	private String expectedBeginString;

	private String expectedSender;

	private String expectedTarget;

	private String expectedQualifier;

	@Before
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

		mockMessage(expectedBeginString, expectedSender, expectedTarget);
		mockSessionFound();

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
		mockMessage(expectedBeginString, expectedSender, expectedTarget);
		mockSessionFound();

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
		mockMessage(expectedBeginString, expectedSender, expectedTarget);
		mockSessionFound();

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
		mockMessage(expectedBeginString, expectedSender, expectedTarget);
		mockSessionFound();

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
		mockMessage(expectedBeginString, expectedSender, expectedTarget);
		mockSessionFound();

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
		mockMessage(expectedBeginString, expectedSender, expectedTarget);
		mockSessionIDFound();
		mockSessionFound();
		mockDataDictionary();

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
		mockMessage(expectedBeginString, expectedSender, expectedTarget);
		mockSessionNotFound();

		// When/Then
		assertThatExceptionOfType(SessionNotFoundException.class)
				.isThrownBy(() -> quickFixJTemplate.send(message));
	}

	@Test
	public void shouldThrowMessageValidationException() throws FieldNotFound, IncorrectTagValue, IncorrectDataFormat {
		// Given
		mockMessage(expectedBeginString, expectedSender, expectedTarget);
		mockSessionFound();
		mockSessionIDFound();
		mockDataDictionaryValidationFailure();

		// When/Then
		assertThatExceptionOfType(MessageValidationException.class)
				.isThrownBy(() -> quickFixJTemplate.send(message));
	}

	@Test
	public void shouldThrowFieldNotFoundExceptionWithInvalidTargetCompID() throws FieldNotFound {
		// Given
		mockMessageWithInvalidTargetCompIDHeader();

		// When/Then
		assertThatExceptionOfType(FieldNotFoundException.class)
				.isThrownBy(() -> quickFixJTemplate.send(message))
				.withMessageContaining("Field with ID 56 not found in message");
	}

	@Test
	public void shouldThrowFieldNotFoundExceptionWithInvalidSenderCompID() throws FieldNotFound {
		// Given
		mockMessageWithInvalidSenderCompIDHeader();

		// When/Then
		assertThatExceptionOfType(FieldNotFoundException.class)
				.isThrownBy(() -> quickFixJTemplate.send(message))
				.withMessageContaining("Field with ID 49 not found in message");
	}

	@Test
	public void shouldThrowFieldNotFoundExceptionWithInvalidBeginString() throws FieldNotFound {
		// Given
		mockMessageWithInvalidBeginStringHeader();

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

		mockMessage(expectedBeginString, expectedSender, expectedTarget);

		// When
		quickFixJTemplate.send(message);

		// Then
		verify(newSessionLookupHandler).lookupBySessionID(any());
		verifyZeroInteractions(sessionLookupHandler);
	}

	private void mockMessage(String expectedBeginString, String expectedSender, String expectedTarget) throws FieldNotFound {
		Message.Header header = mock(Message.Header.class);
		given(message.getHeader()).willReturn(header);
		given(header.getString(SenderCompID.FIELD)).willReturn(expectedSender);
		given(header.getString(TargetCompID.FIELD)).willReturn(expectedTarget);
		given(header.getString(BeginString.FIELD)).willReturn(expectedBeginString);
	}

	private void mockMessageWithInvalidBeginStringHeader() throws FieldNotFound {
		mockMessageWithInvalidHeader(BeginString.FIELD);
	}

	private void mockMessageWithInvalidSenderCompIDHeader() throws FieldNotFound {
		mockMessageWithInvalidHeader(SenderCompID.FIELD);
	}

	private void mockMessageWithInvalidTargetCompIDHeader() throws FieldNotFound {
		mockMessageWithInvalidHeader(TargetCompID.FIELD);
	}

	private void mockMessageWithInvalidHeader(int field) throws FieldNotFound {
		Message.Header header = mock(Message.Header.class);
		given(message.getHeader()).willReturn(header);
		given(header.getString(field)).willThrow(FieldNotFound.class);
	}

	private void mockSessionFound() {
		given(sessionLookupHandler.lookupBySessionID(any())).willReturn(session);
	}

	private void mockSessionNotFound() {
		given(sessionLookupHandler.lookupBySessionID(any())).willReturn(null);
	}

	private void mockSessionIDFound() {
		SessionID sessionID = mock(SessionID.class);
		given(sessionID.getBeginString()).willReturn(FIX50SP2);
		given(session.getSessionID()).willReturn(sessionID);
	}

	private void mockDataDictionary() {
		given(session.getDataDictionaryProvider()).willReturn(dataDictionaryProvider);
		given(dataDictionaryProvider.getApplicationDataDictionary(any())).willReturn(applicationDataDictionary);
	}

	private void mockDataDictionaryValidationFailure() throws IncorrectDataFormat, FieldNotFound, IncorrectTagValue {
		mockDataDictionary();
		willThrow(IncorrectDataFormat.class).given(applicationDataDictionary).validate(any(), eq(true));
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