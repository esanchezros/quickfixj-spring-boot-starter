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
package io.allune.quickfixj.spring.boot.actuate.endpoint;

import org.springframework.boot.actuate.endpoint.Sanitizer;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import quickfix.Acceptor;
import quickfix.SessionSettings;

/**
 * {@link Endpoint} to expose QuickFIX/J server info.
 *
 * @author Eduardo Sanchez-Ros
 */
@Endpoint(id = "quickfixjserver")
public class QuickFixJServerEndpoint extends AbstractQuickFixJEndpoint {

	public QuickFixJServerEndpoint(Acceptor serverAcceptor,
								   SessionSettings serverSessionSettings,
								   Sanitizer sanitizer
	) {
		super(serverAcceptor, serverSessionSettings, sanitizer);
	}
}
