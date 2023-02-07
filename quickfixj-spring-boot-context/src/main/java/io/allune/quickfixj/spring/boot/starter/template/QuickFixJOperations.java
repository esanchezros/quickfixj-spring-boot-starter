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

import io.allune.quickfixj.spring.boot.starter.exception.SessionNotFoundException;
import quickfix.Message;
import quickfix.SessionID;

/**
 * Interface specifying a basic set of QuickFIX/J operations.
 * Implemented by {@link QuickFixJTemplate}.
 *
 * @author Eduardo Sanchez-Ros
 * @see QuickFixJTemplate
 */
public interface QuickFixJOperations {

	/**
	 * Sends a message to the session specified in the message's target
	 * identifiers.
	 *
	 * @param message a FIX message
	 * @return true is send was successful, false otherwise
	 * @throws SessionNotFoundException if session could not be found
	 */
	boolean send(Message message);

	/**
	 * Sends a message to the session specified in the message's target
	 * identifiers. The session qualifier is used to distinguish sessions with
	 * the same target identifiers.
	 *
	 * @param message   a FIX message
	 * @param qualifier a session qualifier
	 * @return true is send was successful, false otherwise
	 * @throws SessionNotFoundException if session could not be found
	 */
	boolean send(Message message, String qualifier);

	/**
	 * Sends a message to the session specified by the provided target company
	 * ID. The sender company ID is provided as an argument rather than from the
	 * message.
	 *
	 * @param message      a FIX message
	 * @param senderCompID the sender's company ID
	 * @param targetCompID the target's company ID
	 * @return true is send was successful, false otherwise
	 * @throws SessionNotFoundException if session could not be found
	 */
	boolean send(Message message, String senderCompID, String targetCompID);

	/**
	 * Sends a message to the session specified by the provided target company
	 * ID. The sender company ID is provided as an argument rather than from the
	 * message. The session qualifier is used to distinguish sessions with the
	 * same target identifiers.
	 *
	 * @param message      a FIX message
	 * @param senderCompID the sender's company ID
	 * @param targetCompID the target's company ID
	 * @param qualifier    a session qualifier
	 * @return true is send was successful, false otherwise
	 * @throws SessionNotFoundException if session could not be found\
	 */
	boolean send(Message message, String senderCompID, String targetCompID, String qualifier);

	/**
	 * Sends a message to the session specified by the provided session ID.
	 *
	 * @param message   a FIX message
	 * @param sessionID the target SessionID
	 * @return true is send was successful, false otherwise
	 * @throws SessionNotFoundException if session could not be found\
	 */
	boolean send(Message message, SessionID sessionID);
}
