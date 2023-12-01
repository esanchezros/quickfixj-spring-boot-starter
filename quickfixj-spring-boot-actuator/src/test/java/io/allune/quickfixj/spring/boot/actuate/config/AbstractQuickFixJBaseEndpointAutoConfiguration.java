/*
 * Copyright 2017-2023 the original author or authors.
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
package io.allune.quickfixj.spring.boot.actuate.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import quickfix.SessionID;

import java.io.IOException;
import java.util.Map;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

public class AbstractQuickFixJBaseEndpointAutoConfiguration {

	protected static Consumer<EntityExchangeResult<byte[]>> assertSessionProperties() {
		return entityExchangeResult -> {
			try {
				ObjectMapper mapper = new ObjectMapper();
				TypeReference<Map<String, Map<String, String>>> typeRef
					= new TypeReference<Map<String, Map<String, String>>>() {
				};
				Map<String, Map<String, String>> sessionProperties =
					mapper.readValue(entityExchangeResult.getResponseBody(), typeRef);

				assertThat(sessionProperties).hasSize(2);
				SessionID sessionID40 =
					new SessionID("FIX.4.0", "SCompID", "SSubID",
						"SLocID", "TCompID", "TSubID", "TLocID", SessionID.NOT_SET);
				Map<String, String> sessionID40Properties = sessionProperties.get(sessionID40.toString());
				assertThat(sessionID40Properties).containsEntry("SocketKeyStorePassword", "******");
				assertThat(sessionID40Properties).containsEntry("SocketTrustStorePassword", "******");
				assertThat(sessionID40Properties).containsEntry("ProxyPassword", "******");
				assertThat(sessionID40Properties).containsEntry("JdbcPassword", "******");

				SessionID sessionID44 = new SessionID("FIX.4.4", "SCompID", "SSubID",
					"SLocID", "TCompID", "TSubID", "TLocID", SessionID.NOT_SET);
				Map<String, String> sessionID44Properties = sessionProperties.get(sessionID44.toString());
				assertThat(sessionID44Properties).containsEntry("SocketKeyStorePassword", "******");
				assertThat(sessionID44Properties).containsEntry("SocketTrustStorePassword", "******");
				assertThat(sessionID44Properties).containsEntry("ProxyPassword", "******");
				assertThat(sessionID44Properties).containsEntry("JdbcPassword", "******");
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		};
	}
}
