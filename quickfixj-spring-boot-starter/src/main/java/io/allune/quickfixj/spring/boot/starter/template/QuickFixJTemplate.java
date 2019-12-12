/*
 * Copyright 2019 the original author or authors.
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

import static quickfix.SessionID.NOT_SET;

import org.springframework.util.Assert;

import lombok.Builder;
import lombok.NonNull;
import quickfix.DataDictionary;
import quickfix.DataDictionaryProvider;
import quickfix.FieldNotFound;
import quickfix.FixVersions;
import quickfix.LogUtil;
import quickfix.Message;
import quickfix.MessageUtils;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.field.ApplVerID;
import quickfix.field.BeginString;
import quickfix.field.SenderCompID;
import quickfix.field.TargetCompID;

/**
 * Synchronous client to perform requests, exposing a simple, template
 * method API over the QuickFixJ client
 *
 * @author Eduardo Sanchez-Ros
 */
public class QuickFixJTemplate implements QuickFixJOperations {

	private SessionLookupHandler sessionLookupHandler;

	public QuickFixJTemplate() {
		this.sessionLookupHandler = new DefaultSessionLookupHandler();
	}

	public QuickFixJTemplate(SessionLookupHandler sessionLookupHandler) {
		this.sessionLookupHandler = sessionLookupHandler;
	}

	public void setSessionLookupHandler(SessionLookupHandler sessionLookupHandler) {
		this.sessionLookupHandler = sessionLookupHandler;
	}

	@Override
	public boolean send(Message message) {
		Assert.notNull(message, "'message' must not be null");

		SessionID sessionID = QuickFixJSessionID.quickFixJSessionIDBuilder()
				.message(message)
				.build().toSessionID();
		return doSend(message, sessionID);
	}

	@Override
	public boolean send(Message message, String qualifier) {
		Assert.notNull(message, "'message' must not be null");

		SessionID sessionID = QuickFixJSessionID.quickFixJSessionIDBuilder()
				.message(message)
				.qualifier(qualifier)
				.build().toSessionID();
		return doSend(message, sessionID);
	}

	@Override
	public boolean send(Message message, String senderCompID, String targetCompID) {
		Assert.notNull(message, "'message' must not be null");

		SessionID sessionID = QuickFixJSessionID.quickFixJSessionIDBuilder()
				.message(message)
				.senderCompID(senderCompID)
				.targetCompID(targetCompID)
				.build().toSessionID();
		return doSend(message, sessionID);
	}

	@Override
	public boolean send(Message message, String senderCompID, String targetCompID, String qualifier) {
		Assert.notNull(message, "'message' must not be null");

		SessionID sessionID = QuickFixJSessionID.quickFixJSessionIDBuilder()
				.message(message)
				.senderCompID(senderCompID)
				.targetCompID(targetCompID)
				.qualifier(qualifier)
				.build().toSessionID();
		return doSend(message, sessionID);
	}

	@Override
	public boolean send(Message message, SessionID sessionID) {
		Assert.notNull(message, "'message' must not be null");
		Assert.notNull(sessionID, "'sessionID' must not be null");

		return doSend(message, sessionID);
	}

	protected boolean doSend(Message message, SessionID sessionID) {
		Session session = sessionLookupHandler.lookupBySessionID(sessionID);
		if (session == null) {
			throw new SessionNotFoundException("Session not found: " + sessionID.toString());
		}

		DataDictionaryProvider dataDictionaryProvider = session.getDataDictionaryProvider();
		if (dataDictionaryProvider != null) {
			try {
				ApplVerID applVerID = getApplicationVersionID(session);
				DataDictionary applicationDataDictionary = dataDictionaryProvider.getApplicationDataDictionary(applVerID);
				applicationDataDictionary.validate(message, true);
			} catch (Exception e) {
				LogUtil.logThrowable(sessionID, "Message failed validation: " + e.getMessage(), e);
				throw new MessageValidationException("Message failed validation: " + e.getMessage(), e);
			}
		}

		return session.send(message);
	}

	private static ApplVerID getApplicationVersionID(Session session) {
		String beginString = session.getSessionID().getBeginString();
		if (FixVersions.BEGINSTRING_FIXT11.equals(beginString)) {
			return new ApplVerID(ApplVerID.FIX50);
		} else {
			return MessageUtils.toApplVerID(beginString);
		}
	}

	@Builder(builderClassName = "QuickFixJSessionIDBuilder", builderMethodName = "quickFixJSessionIDBuilder")
	static class QuickFixJSessionID {

		@NonNull
		public Message message;

		public String beginString;

		public String senderCompID;

		public String targetCompID;

		public String qualifier;

		public SessionID toSessionID() {

			if (beginString == null) {
				beginString = getFieldFromMessageHeader(message, BeginString.FIELD);
			}
			if (senderCompID == null) {
				senderCompID = getFieldFromMessageHeader(message, SenderCompID.FIELD);
			}
			if (targetCompID == null) {
				targetCompID = getFieldFromMessageHeader(message, TargetCompID.FIELD);
			}
			if (qualifier == null) {
				qualifier = NOT_SET;
			}
			return new SessionID(beginString, senderCompID, targetCompID, qualifier);
		}

		private static String getFieldFromMessageHeader(final Message message, int fieldTag) {
			try {
				return message.getHeader().getString(fieldTag);
			} catch (FieldNotFound fieldNotFound) {
				throw new FieldNotFoundException("Field with ID " + fieldTag + " not found in message", fieldNotFound);
			}
		}
	}
}
