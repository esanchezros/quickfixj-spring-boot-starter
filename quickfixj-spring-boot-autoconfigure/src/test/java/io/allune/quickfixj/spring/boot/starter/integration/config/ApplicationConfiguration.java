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
///*
// * Copyright 2017-2024 the original author or authors.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package io.allune.quickfixj.spring.boot.starter.integration.config;
//
//import org.springframework.boot.SpringBootConfiguration;
//import org.springframework.boot.WebApplicationType;
//import org.springframework.boot.builder.SpringApplicationBuilder;
//
//
//@SpringBootConfiguration
//public class ApplicationConfiguration {
//
//	public static void main(String[] args) {
//		SpringApplicationBuilder parent =
//				new SpringApplicationBuilder(QuickFixJCommonConfiguration.class);
//		parent.child(QuickFixJServerYourCompanyContextConfiguration.class).web(WebApplicationType.SERVLET);
//		parent.child(QuickFixJClientNDAQContextConfiguration.class).web(WebApplicationType.SERVLET);
//		parent.child(QuickFixJClientFOREXContextConfiguration.class).web(WebApplicationType.SERVLET);
//		parent.child(QuickFixJClientDOWJONESContextConfiguration.class).web(WebApplicationType.SERVLET);
//		parent.run(args);
//	}
//}