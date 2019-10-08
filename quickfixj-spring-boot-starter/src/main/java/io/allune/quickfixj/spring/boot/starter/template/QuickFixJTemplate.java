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

import org.springframework.util.Assert;

import quickfix.DataDictionary;
import quickfix.DataDictionaryProvider;
import quickfix.FixVersions;
import quickfix.LogUtil;
import quickfix.Message;
import quickfix.MessageUtils;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.field.ApplVerID;

public class QuickFixJTemplate implements QuickFixJOperations {

	private SessionLookupHandler sessionLookupHandler;

	public QuickFixJTemplate() {
		this.sessionLookupHandler = new DefaultSessionLookupHandler();
	}

	public QuickFixJTemplate(SessionLookupHandler sessionLookupHandler) {
		this.sessionLookupHandler = sessionLookupHandler;
	}

	@Override
	public boolean send(Message message, SessionID sessionID) {
		Assert.notNull(message, "'message' must not be null");
		Assert.notNull(sessionID, "'sessionID' must not be null");

		Session session = sessionLookupHandler.lookupBySessionID(sessionID);
		if (session == null) {
			throw new SessionNotFoundException("Session not found: " + sessionID.toString());
		}

		// TODO: Make configurable
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

	@Override
	public boolean send(Message message, String qualifier) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean send(Message message) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean send(Message message, String senderCompID, String targetCompID) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean send(Message message, String senderCompID, String targetCompID, String qualifier) {
		throw new UnsupportedOperationException();
	}

	// TODO: Extract to DefaultMessageUtils  <- think of better naming
	private ApplVerID getApplicationVersionID(Session session) {
		String beginString = session.getSessionID().getBeginString();
		if (FixVersions.BEGINSTRING_FIXT11.equals(beginString)) {
			return new ApplVerID(ApplVerID.FIX50);
		} else {
			return MessageUtils.toApplVerID(beginString);
		}
	}
}
