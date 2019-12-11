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

/**
 * Exception thrown when a field is not found in a message.
 *
 * @author Eduardo Sanchez-Ros
 */
public class FieldNotFoundException extends QuickFixJException {

	/**
	 * Construct a new instance of {@code FieldNotFoundException} with the given message and
	 * exception.
	 *
	 * @param msg the message
	 * @param ex  the exception
	 */
	public FieldNotFoundException(String msg, Throwable ex) {
		super(msg, ex);
	}
}
