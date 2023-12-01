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
/*
	Borrowed from Spring Boot
	https://github.com/spring-projects/spring-boot/blob/8de81cb06e7039e95989f6035565f810edeaba4b/spring-boot-project/spring-boot-actuator/src/test/java/org/springframework/boot/actuate/endpoint/web/test/PortHolder.java
 */
package io.allune.quickfixj.spring.boot.actuate.endpoint.test;

public class PortHolder {

	private int port;

	int getPort() {
		return this.port;
	}

	void setPort(int port) {
		this.port = port;
	}

}
